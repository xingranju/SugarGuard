"""
饮品识别工具 - 基于自训练饮品分类模型（ONNX Runtime推理）
"""
import numpy as np
from PIL import Image
from typing import Dict, List
import logging
import os

logger = logging.getLogger(__name__)

try:
    import onnxruntime as ort
except ImportError:
    ort = None
    logger.warning("未安装 onnxruntime，饮品识别模型不可用")

DRINK_MODEL_DIR = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "..", "..", "..", "drink_model", "drink_model", "trained_model"
)
DRINK_MODEL_PATH = os.path.join(DRINK_MODEL_DIR, "drink_classifier.onnx")
DRINK_LABELS_PATH = os.path.join(DRINK_MODEL_DIR, "labels.txt")


class DrinkRecognitionTool:
    """饮品专用识别工具（ONNX Runtime推理）"""

    def __init__(self):
        if ort is None:
            raise RuntimeError("需要安装 onnxruntime 才能使用饮品识别模型")

        if not os.path.exists(DRINK_MODEL_PATH):
            raise FileNotFoundError(f"饮品模型文件不存在: {DRINK_MODEL_PATH}")

        self.labels = self._load_labels()
        self.session = ort.InferenceSession(
            DRINK_MODEL_PATH,
            providers=["CPUExecutionProvider"]
        )

        input_info = self.session.get_inputs()[0]
        self.input_name = input_info.name
        input_shape = input_info.shape  # [1, C, H, W] channels-first
        self.channels_first = (input_shape[1] == 3 or input_shape[1] == 1)
        if self.channels_first:
            self.input_height = input_shape[2]
            self.input_width = input_shape[3]
        else:
            self.input_height = input_shape[1]
            self.input_width = input_shape[2]

        output_info = self.session.get_outputs()[0]
        num_classes = output_info.shape[1] if len(output_info.shape) > 1 else output_info.shape[0]

        logger.info(
            f"饮品模型加载成功(ONNX): 输入={self.input_width}x{self.input_height}, "
            f"channels_first={self.channels_first}, "
            f"输出类别={num_classes}, 标签数={len(self.labels)}"
        )

    def _load_labels(self) -> List[str]:
        if not os.path.exists(DRINK_LABELS_PATH):
            raise FileNotFoundError(f"标签文件不存在: {DRINK_LABELS_PATH}")
        with open(DRINK_LABELS_PATH, "r", encoding="utf-8") as f:
            return [line.strip() for line in f if line.strip()]

    def recognize(self, image: Image.Image, top_k: int = 3) -> List[Dict]:
        """
        识别图像中的饮品

        Returns:
            识别结果列表，每个结果包含label/label_cn/confidence
        """
        try:
            if image.mode != "RGB":
                image = image.convert("RGB")
            image_resized = image.resize((self.input_width, self.input_height))

            input_data = np.array(image_resized, dtype=np.float32) / 255.0
            if self.channels_first:
                input_data = np.transpose(input_data, (2, 0, 1))  # HWC → CHW
            input_data = np.expand_dims(input_data, axis=0)

            outputs = self.session.run(None, {self.input_name: input_data})
            output_data = outputs[0][0]

            if output_data.max() > 10.0 or output_data.min() < -10.0:
                exp_scores = np.exp(output_data - np.max(output_data))
                probabilities = exp_scores / exp_scores.sum()
            else:
                probabilities = output_data
                if probabilities.min() < 0 or probabilities.sum() < 0.99:
                    exp_scores = np.exp(output_data - np.max(output_data))
                    probabilities = exp_scores / exp_scores.sum()

            top_indices = np.argsort(probabilities)[::-1][:top_k]

            results = []
            for idx in top_indices:
                if idx < len(self.labels):
                    label = self.labels[idx]
                    score = float(probabilities[idx])
                    results.append({
                        "label": label,
                        "label_cn": label,
                        "confidence": score,
                        "confidence_percent": f"{score * 100:.2f}%",
                        "source": "drink_model"
                    })

            if results:
                logger.info(
                    f"饮品识别 Top-1: {results[0]['label']} "
                    f"({results[0]['confidence_percent']})"
                )

            return results

        except Exception as e:
            logger.error(f"饮品识别失败: {e}")
            raise


_drink_recognition_tool = None


def get_drink_recognition_tool():
    """获取饮品识别工具单例，加载失败返回None"""
    global _drink_recognition_tool
    if _drink_recognition_tool is None:
        try:
            _drink_recognition_tool = DrinkRecognitionTool()
        except Exception as e:
            logger.warning(f"饮品识别模型加载失败，将仅使用VIT模型: {e}")
            return None
    return _drink_recognition_tool
