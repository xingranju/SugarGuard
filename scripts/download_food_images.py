"""
下载真实食物图片并更新数据库
使用 Foodish API + Pexels CDN 获取真实食物照片
"""
import pymysql
import requests
import random
import time
from pathlib import Path

DB_CONFIG = {
    "host": "localhost", "port": 3306, "user": "root",
    "password": "123456", "database": "Android_health_db", "charset": "utf8mb4"
}
USER_ID = 20
UPLOAD_DIR = Path(r"E:\code\Anroid\MyApplication\backend-api\uploads\meals")

FOODISH_CATEGORIES = {
    "rice": ["米饭+红烧肉", "番茄炒蛋+米饭", "宫保鸡丁+米饭", "鱼香肉丝+米饭",
             "青椒肉丝+米饭", "麻婆豆腐+米饭", "西红柿牛腩+米饭", "清炒时蔬+米饭",
             "糖醋排骨+米饭", "回锅肉+米饭", "鸡腿饭", "盖浇饭",
             "黄焖鸡+米饭", "土豆烧牛肉+米饭", "清蒸鱼+米饭", "水煮虾+米饭",
             "玉米排骨汤+米饭", "酸辣土豆丝+米饭", "清炒豆角+米饭"],
    "pasta": ["牛肉面", "炒面", "紫菜蛋花汤+面条", "番茄鸡蛋面"],
    "dessert": ["蛋糕", "冰淇淋", "巧克力", "饼干"],
    "burger": ["三明治"],
    "pizza": [],
    "biryani": [],
}

PEXELS_URLS = {
    "牛奶": "https://images.pexels.com/photos/248412/pexels-photo-248412.jpeg?auto=compress&w=300&h=300&fit=crop",
    "豆浆": "https://images.pexels.com/photos/5946618/pexels-photo-5946618.jpeg?auto=compress&w=300&h=300&fit=crop",
    "全麦面包": "https://images.pexels.com/photos/1775043/pexels-photo-1775043.jpeg?auto=compress&w=300&h=300&fit=crop",
    "煎蛋": "https://images.pexels.com/photos/824635/pexels-photo-824635.jpeg?auto=compress&w=300&h=300&fit=crop",
    "小米粥": "https://images.pexels.com/photos/6072094/pexels-photo-6072094.jpeg?auto=compress&w=300&h=300&fit=crop",
    "包子": "https://images.pexels.com/photos/6646347/pexels-photo-6646347.jpeg?auto=compress&w=300&h=300&fit=crop",
    "燕麦片": "https://images.pexels.com/photos/543730/pexels-photo-543730.jpeg?auto=compress&w=300&h=300&fit=crop",
    "酸奶": "https://images.pexels.com/photos/1435706/pexels-photo-1435706.jpeg?auto=compress&w=300&h=300&fit=crop",
    "香蕉": "https://images.pexels.com/photos/2872755/pexels-photo-2872755.jpeg?auto=compress&w=300&h=300&fit=crop",
    "鸡蛋饼": "https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg?auto=compress&w=300&h=300&fit=crop",
    "玉米": "https://images.pexels.com/photos/547263/pexels-photo-547263.jpeg?auto=compress&w=300&h=300&fit=crop",
    "花卷": "https://images.pexels.com/photos/1775043/pexels-photo-1775043.jpeg?auto=compress&w=300&h=300&fit=crop",
    "馒头": "https://images.pexels.com/photos/1775043/pexels-photo-1775043.jpeg?auto=compress&w=300&h=300&fit=crop",
    "八宝粥": "https://images.pexels.com/photos/6072094/pexels-photo-6072094.jpeg?auto=compress&w=300&h=300&fit=crop",
    "白灼西兰花+鸡胸肉": "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?auto=compress&w=300&h=300&fit=crop",
    "沙拉+鸡胸肉": "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?auto=compress&w=300&h=300&fit=crop",
    "饺子": "https://images.pexels.com/photos/7363671/pexels-photo-7363671.jpeg?auto=compress&w=300&h=300&fit=crop",
    "蔬菜汤+馒头": "https://images.pexels.com/photos/539451/pexels-photo-539451.jpeg?auto=compress&w=300&h=300&fit=crop",
    "炒青菜+粥": "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?auto=compress&w=300&h=300&fit=crop",
    "凉拌黄瓜+小米粥": "https://images.pexels.com/photos/1199957/pexels-photo-1199957.jpeg?auto=compress&w=300&h=300&fit=crop",
    "火锅(蔬菜为主)": "https://images.pexels.com/photos/2347311/pexels-photo-2347311.jpeg?auto=compress&w=300&h=300&fit=crop",
    "砂锅粥": "https://images.pexels.com/photos/6072094/pexels-photo-6072094.jpeg?auto=compress&w=300&h=300&fit=crop",
    "烤红薯": "https://images.pexels.com/photos/4110476/pexels-photo-4110476.jpeg?auto=compress&w=300&h=300&fit=crop",
    "苹果": "https://images.pexels.com/photos/102104/pexels-photo-102104.jpeg?auto=compress&w=300&h=300&fit=crop",
    "橙子": "https://images.pexels.com/photos/161559/background-bitter-breakfast-bright-161559.jpeg?auto=compress&w=300&h=300&fit=crop",
    "葡萄": "https://images.pexels.com/photos/60021/grapes-wine-fruit-vines-60021.jpeg?auto=compress&w=300&h=300&fit=crop",
    "草莓": "https://images.pexels.com/photos/46174/strawberries-berries-fruit-freshness-46174.jpeg?auto=compress&w=300&h=300&fit=crop",
    "坚果": "https://images.pexels.com/photos/1295572/pexels-photo-1295572.jpeg?auto=compress&w=300&h=300&fit=crop",
    "奶茶": "https://images.pexels.com/photos/1581484/pexels-photo-1581484.jpeg?auto=compress&w=300&h=300&fit=crop",
    "可乐": "https://images.pexels.com/photos/50593/coca-cola-cold-drink-soft-drink-coke-50593.jpeg?auto=compress&w=300&h=300&fit=crop",
    "果汁": "https://images.pexels.com/photos/1132047/pexels-photo-1132047.jpeg?auto=compress&w=300&h=300&fit=crop",
    "西瓜": "https://images.pexels.com/photos/1068534/pexels-photo-1068534.jpeg?auto=compress&w=300&h=300&fit=crop",
    "蓝莓": "https://images.pexels.com/photos/1395958/pexels-photo-1395958.jpeg?auto=compress&w=300&h=300&fit=crop",
    "无糖茶": "https://images.pexels.com/photos/1417945/pexels-photo-1417945.jpeg?auto=compress&w=300&h=300&fit=crop",
    "黑咖啡": "https://images.pexels.com/photos/312418/pexels-photo-312418.jpeg?auto=compress&w=300&h=300&fit=crop",
    "柠檬水": "https://images.pexels.com/photos/96974/pexels-photo-96974.jpeg?auto=compress&w=300&h=300&fit=crop",
}


def download(url, save_path, timeout=15):
    try:
        r = requests.get(url, headers={"User-Agent": "Mozilla/5.0"}, timeout=timeout, stream=True)
        if r.status_code == 200:
            with open(save_path, "wb") as f:
                for chunk in r.iter_content(8192):
                    f.write(chunk)
            return True
    except Exception as e:
        print(f"  error: {e}")
    return False


def safe_filename(name):
    return name.replace("+", "_").replace("(", "").replace(")", "").replace("/", "_").replace(" ", "_")


def main():
    UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

    food_to_path = {}

    # --- Step 1: Foodish API ---
    for category, foods in FOODISH_CATEGORIES.items():
        if not foods:
            continue
        for food in foods:
            fname = safe_filename(food)
            filepath = UPLOAD_DIR / f"{fname}.jpg"
            if filepath.exists() and filepath.stat().st_size > 5000:
                food_to_path[food] = f"uploads/meals/{fname}.jpg"
                continue

            try:
                r = requests.get(f"https://foodish-api.com/api/images/{category}", timeout=10)
                if r.status_code == 200:
                    img_url = r.json().get("image", "")
                    if img_url and download(img_url, str(filepath)):
                        if filepath.stat().st_size > 3000:
                            food_to_path[food] = f"uploads/meals/{fname}.jpg"
                            print(f"  [Foodish/{category}] {food} OK")
                        else:
                            filepath.unlink(missing_ok=True)
                time.sleep(0.5)
            except Exception as e:
                print(f"  [Foodish] {food} error: {e}")

    # --- Step 2: Pexels CDN ---
    for food, url in PEXELS_URLS.items():
        if food in food_to_path:
            continue
        fname = safe_filename(food)
        filepath = UPLOAD_DIR / f"{fname}.jpg"
        if filepath.exists() and filepath.stat().st_size > 5000:
            food_to_path[food] = f"uploads/meals/{fname}.jpg"
            continue

        if download(url, str(filepath)):
            if filepath.stat().st_size > 3000:
                food_to_path[food] = f"uploads/meals/{fname}.jpg"
                print(f"  [Pexels] {food} OK")
            else:
                filepath.unlink(missing_ok=True)
        time.sleep(0.3)

    print(f"\ntotal images: {len(food_to_path)}")

    # --- Step 3: update DB ---
    conn = pymysql.connect(**DB_CONFIG)
    try:
        cur = conn.cursor()
        updated = 0
        for food, path in food_to_path.items():
            cur.execute(
                "UPDATE meal_records SET image_path=%s WHERE user_id=%s AND food_name=%s",
                (path, USER_ID, food)
            )
            updated += cur.rowcount

        conn.commit()

        cur.execute(
            "SELECT COUNT(*) FROM meal_records WHERE user_id=%s AND image_path IS NOT NULL AND image_path!=''",
            (USER_ID,)
        )
        with_img = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM meal_records WHERE user_id=%s", (USER_ID,))
        total = cur.fetchone()[0]

        print(f"updated {updated} rows")
        print(f"records with image: {with_img}/{total}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
