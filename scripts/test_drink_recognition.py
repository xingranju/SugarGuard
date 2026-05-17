"""
双模型饮品识别测试脚本
直接加载 VIT + 饮品专用模型，用 ml_kit_dataset_by_drink 测试集评估识别准确率
"""
import sys, os, json, time
from pathlib import Path
from datetime import datetime

PROJECT_ROOT = Path("E:/code/Anroid/MyApplication")
AI_SERVICE_DIR = PROJECT_ROOT / "ai-service"
sys.path.insert(0, str(AI_SERVICE_DIR))

os.environ['TRANSFORMERS_OFFLINE'] = '1'
os.environ['HF_HUB_OFFLINE'] = '1'

from PIL import Image
import numpy as np

DATASET_DIR = PROJECT_ROOT / "ml_kit_dataset_by_drink"
RESULTS_FILE = PROJECT_ROOT / "scripts" / "recognition_test_results.json"

def load_drink_model():
    """加载饮品 ONNX 模型"""
    try:
        import onnxruntime as ort
    except ImportError:
        print("[WARN] onnxruntime 未安装，饮品模型不可用")
        return None, None

    model_dir = PROJECT_ROOT / "drink_model" / "drink_model" / "trained_model"
    onnx_path = model_dir / "drink_classifier.onnx"
    labels_path = model_dir / "labels.txt"

    if not onnx_path.exists():
        print(f"[WARN] 饮品模型文件不存在: {onnx_path}")
        return None, None

    session = ort.InferenceSession(str(onnx_path), providers=["CPUExecutionProvider"])
    with open(labels_path, "r", encoding="utf-8") as f:
        labels = [line.strip() for line in f if line.strip()]

    inp = session.get_inputs()[0]
    print(f"[OK] 饮品模型加载成功: {inp.name}, shape={inp.shape}, labels={len(labels)}")
    return session, labels


def drink_predict(session, labels, image: Image.Image, top_k=3):
    """饮品模型推理"""
    inp = session.get_inputs()[0]
    shape = inp.shape
    channels_first = (shape[1] == 3 or shape[1] == 1)
    h = shape[2] if channels_first else shape[1]
    w = shape[3] if channels_first else shape[2]

    img = image.convert("RGB").resize((w, h))
    data = np.array(img, dtype=np.float32) / 255.0
    if channels_first:
        data = np.transpose(data, (2, 0, 1))
    data = np.expand_dims(data, axis=0)

    outputs = session.run(None, {inp.name: data})
    output_data = outputs[0][0]

    if output_data.max() > 10.0 or output_data.min() < -10.0:
        exp_scores = np.exp(output_data - np.max(output_data))
        probs = exp_scores / exp_scores.sum()
    else:
        probs = output_data
        if probs.min() < 0 or probs.sum() < 0.99:
            exp_scores = np.exp(output_data - np.max(output_data))
            probs = exp_scores / exp_scores.sum()

    top_indices = np.argsort(probs)[::-1][:top_k]
    results = []
    for idx in top_indices:
        if idx < len(labels):
            results.append({
                "label": labels[idx],
                "confidence": float(probs[idx]),
                "source": "drink_model"
            })
    return results


def load_vit_model():
    """加载 VIT 模型"""
    try:
        from transformers import pipeline, AutoImageProcessor, AutoModelForImageClassification
        import torch
    except ImportError:
        print("[WARN] transformers 未安装，VIT 模型不可用")
        return None

    model_name = "google/vit-base-patch16-224"
    device = 0 if torch.cuda.is_available() else -1
    print(f"[INFO] 加载 VIT 模型: {model_name} (device={'GPU' if device==0 else 'CPU'})")

    dtype = torch.float16 if torch.cuda.is_available() else torch.float32
    model = AutoModelForImageClassification.from_pretrained(
        model_name, local_files_only=True, torch_dtype=dtype
    )
    processor = AutoImageProcessor.from_pretrained(model_name, local_files_only=True)
    classifier = pipeline("image-classification", model=model, feature_extractor=processor, device=device)
    print("[OK] VIT 模型加载成功")
    return classifier


FOOD_EN_TO_CN = {
    "espresso": "浓缩咖啡", "cup": "杯装饮品", "coffee mug": "咖啡杯",
    "teapot": "茶壶", "beer glass": "啤酒杯", "wine bottle": "红酒",
    "water bottle": "矿泉水", "pop bottle": "汽水", "beer bottle": "啤酒瓶",
    "red wine": "红酒", "eggnog": "蛋酒", "ice cream": "冰淇淋",
    "chocolate sauce": "巧克力酱", "pizza": "披萨",
}


def vit_predict(classifier, image: Image.Image, top_k=3):
    """VIT 模型推理"""
    img = image.convert("RGB").resize((224, 224))
    results = classifier(img, top_k=top_k)
    formatted = []
    for r in results:
        score = float(r['score'])
        if score < 0.05:
            continue
        en_label = r['label']
        cn_label = FOOD_EN_TO_CN.get(en_label.lower().replace("_", " "), "")
        formatted.append({
            "label": en_label,
            "label_cn": cn_label,
            "confidence": score,
            "source": "vit"
        })
    return formatted


def dual_model_recognize(vit_classifier, drink_session, drink_labels, image):
    """双模型识别，返回最终选择和两个模型的详细结果"""
    vit_results = vit_predict(vit_classifier, image) if vit_classifier else []
    drink_results = drink_predict(drink_session, drink_labels, image) if drink_session else []

    vit_top = vit_results[0] if vit_results else None
    drink_top = drink_results[0] if drink_results else None

    if drink_top and vit_top:
        if drink_top["confidence"] >= vit_top["confidence"]:
            chosen = drink_top
            chosen_source = "drink_model"
        else:
            chosen = vit_top
            chosen_source = "vit"
    elif drink_top:
        chosen = drink_top
        chosen_source = "drink_model"
    elif vit_top:
        chosen = vit_top
        chosen_source = "vit"
    else:
        chosen = {"label": "UNKNOWN", "confidence": 0}
        chosen_source = "none"

    return {
        "chosen_label": chosen["label"],
        "chosen_confidence": chosen["confidence"],
        "chosen_source": chosen_source,
        "vit_top": vit_top,
        "drink_top": drink_top,
    }


def main():
    print("=" * 60)
    print("饮品识别双模型测试")
    print(f"测试集: {DATASET_DIR}")
    print(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    drink_session, drink_labels = load_drink_model()
    vit_classifier = load_vit_model()

    if not drink_session and not vit_classifier:
        print("[ERROR] 两个模型都无法加载，退出")
        return

    categories = sorted([
        d for d in os.listdir(DATASET_DIR)
        if os.path.isdir(os.path.join(DATASET_DIR, d))
    ])
    print(f"\n共 {len(categories)} 个饮品类别")

    correct_drink = 0
    correct_dual = 0
    total_images = 0
    correct_images = []
    incorrect_images = []
    category_results = []

    for i, cat_name in enumerate(categories):
        cat_dir = os.path.join(DATASET_DIR, cat_name)
        images = [f for f in os.listdir(cat_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png'))]

        if not images:
            continue

        cat_correct = 0
        cat_total = len(images)
        cat_details = []

        for img_name in images:
            img_path = os.path.join(cat_dir, img_name)
            total_images += 1

            try:
                image = Image.open(img_path)
                result = dual_model_recognize(vit_classifier, drink_session, drink_labels, image)

                is_correct = result["chosen_label"] == cat_name
                drink_label_matches = (
                    result["drink_top"]["label"] == cat_name if result["drink_top"] else False
                )

                if drink_label_matches:
                    correct_drink += 1
                if is_correct:
                    correct_dual += 1
                    cat_correct += 1
                    correct_images.append({
                        "category": cat_name,
                        "file": img_name,
                        "label": result["chosen_label"],
                        "confidence": round(result["chosen_confidence"], 4),
                        "source": result["chosen_source"],
                    })
                else:
                    incorrect_images.append({
                        "category": cat_name,
                        "file": img_name,
                        "predicted": result["chosen_label"],
                        "confidence": round(result["chosen_confidence"], 4),
                        "source": result["chosen_source"],
                        "drink_pred": result["drink_top"]["label"] if result["drink_top"] else None,
                        "drink_conf": round(result["drink_top"]["confidence"], 4) if result["drink_top"] else None,
                        "vit_pred": result["vit_top"]["label"] if result["vit_top"] else None,
                        "vit_conf": round(result["vit_top"]["confidence"], 4) if result["vit_top"] else None,
                    })

                cat_details.append({
                    "file": img_name,
                    "correct": is_correct,
                    "predicted": result["chosen_label"],
                    "confidence": round(result["chosen_confidence"], 4),
                    "source": result["chosen_source"],
                })
            except Exception as e:
                print(f"  [ERROR] {img_path}: {e}")
                incorrect_images.append({
                    "category": cat_name,
                    "file": img_name,
                    "predicted": "ERROR",
                    "confidence": 0,
                    "source": "error",
                    "error": str(e),
                })

        cat_acc = cat_correct / cat_total * 100 if cat_total > 0 else 0
        status = "OK" if cat_correct == cat_total else "PARTIAL" if cat_correct > 0 else "FAIL"
        print(f"[{i+1:3d}/{len(categories)}] {cat_name}: {cat_correct}/{cat_total} ({cat_acc:.0f}%) [{status}]")

        category_results.append({
            "category": cat_name,
            "correct": cat_correct,
            "total": cat_total,
            "accuracy": round(cat_acc, 1),
            "status": status,
            "details": cat_details,
        })

    drink_acc = correct_drink / total_images * 100 if total_images > 0 else 0
    dual_acc = correct_dual / total_images * 100 if total_images > 0 else 0

    print("\n" + "=" * 60)
    print(f"测试结果汇总")
    print(f"  总图片数: {total_images}")
    print(f"  饮品模型正确: {correct_drink} ({drink_acc:.1f}%)")
    print(f"  双模型(最终)正确: {correct_dual} ({dual_acc:.1f}%)")
    print(f"  正确识别图片数: {len(correct_images)}")
    print(f"  错误识别图片数: {len(incorrect_images)}")

    fully_correct_cats = [c for c in category_results if c["status"] == "OK"]
    partial_cats = [c for c in category_results if c["status"] == "PARTIAL"]
    fail_cats = [c for c in category_results if c["status"] == "FAIL"]

    print(f"\n  完全正确类别: {len(fully_correct_cats)}/{len(categories)}")
    print(f"  部分正确类别: {len(partial_cats)}/{len(categories)}")
    print(f"  完全错误类别: {len(fail_cats)}/{len(categories)}")
    print("=" * 60)

    output = {
        "test_time": datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        "dataset": str(DATASET_DIR),
        "summary": {
            "total_images": total_images,
            "total_categories": len(categories),
            "drink_model_correct": correct_drink,
            "drink_model_accuracy": round(drink_acc, 2),
            "dual_model_correct": correct_dual,
            "dual_model_accuracy": round(dual_acc, 2),
            "fully_correct_categories": len(fully_correct_cats),
            "partial_correct_categories": len(partial_cats),
            "fail_categories": len(fail_cats),
        },
        "correct_images": correct_images,
        "incorrect_images": incorrect_images,
        "category_results": category_results,
    }

    with open(RESULTS_FILE, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    print(f"\n详细结果已保存: {RESULTS_FILE}")


if __name__ == "__main__":
    main()
