"""
为 testuser 的饮食记录补充图片和备注
1. 下载食物图片到 uploads/meals/ 目录
2. 更新 meal_records 表的 image_path 和 notes
"""
import pymysql
import requests
import os
import time
import random
from pathlib import Path

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "Android_health_db",
    "charset": "utf8mb4"
}

USER_ID = 20

UPLOAD_DIR = Path(r"E:\code\Anroid\MyApplication\uploads\meals")

FOOD_IMAGE_URLS = {
    "牛奶": "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=300&h=300&fit=crop",
    "豆浆": "https://images.unsplash.com/photo-1612929633738-8fe44f7ec841?w=300&h=300&fit=crop",
    "全麦面包": "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=300&h=300&fit=crop",
    "煎蛋": "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=300&h=300&fit=crop",
    "小米粥": "https://images.unsplash.com/photo-1604152135912-04a022e23696?w=300&h=300&fit=crop",
    "包子": "https://images.unsplash.com/photo-1589187151053-5ec8818e661b?w=300&h=300&fit=crop",
    "燕麦片": "https://images.unsplash.com/photo-1517673132405-a56a62b18caf?w=300&h=300&fit=crop",
    "酸奶": "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=300&h=300&fit=crop",
    "香蕉": "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=300&h=300&fit=crop",
    "三明治": "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=300&h=300&fit=crop",
    "鸡蛋饼": "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=300&h=300&fit=crop",
    "玉米": "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=300&h=300&fit=crop",
    "米饭+红烧肉": "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=300&h=300&fit=crop",
    "番茄炒蛋+米饭": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=300&h=300&fit=crop",
    "宫保鸡丁+米饭": "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=300&h=300&fit=crop",
    "鱼香肉丝+米饭": "https://images.unsplash.com/photo-1585032226651-759b368d7246?w=300&h=300&fit=crop",
    "青椒肉丝+米饭": "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=300&h=300&fit=crop",
    "麻婆豆腐+米饭": "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?w=300&h=300&fit=crop",
    "西红柿牛腩+米饭": "https://images.unsplash.com/photo-1574484284002-952d92456975?w=300&h=300&fit=crop",
    "清炒时蔬+米饭": "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=300&h=300&fit=crop",
    "糖醋排骨+米饭": "https://images.unsplash.com/photo-1544025162-d76694265947?w=300&h=300&fit=crop",
    "回锅肉+米饭": "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=300&h=300&fit=crop",
    "牛肉面": "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=300&h=300&fit=crop",
    "鸡腿饭": "https://images.unsplash.com/photo-1598515213692-fe3eb3e2b51c?w=300&h=300&fit=crop",
    "沙拉+鸡胸肉": "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300&h=300&fit=crop",
    "饺子": "https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=300&h=300&fit=crop",
    "炒面": "https://images.unsplash.com/photo-1585032226651-759b368d7246?w=300&h=300&fit=crop",
    "盖浇饭": "https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=300&h=300&fit=crop",
    "蔬菜汤+馒头": "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=300&h=300&fit=crop",
    "清蒸鱼+米饭": "https://images.unsplash.com/photo-1510130113581-a4b5e6a1e2f6?w=300&h=300&fit=crop",
    "炒青菜+粥": "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=300&h=300&fit=crop",
    "水煮虾+米饭": "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=300&h=300&fit=crop",
    "白灼西兰花+鸡胸肉": "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=300&h=300&fit=crop",
    "凉拌黄瓜+小米粥": "https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=300&h=300&fit=crop",
    "苹果": "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=300&h=300&fit=crop",
    "橙子": "https://images.unsplash.com/photo-1582979512210-99b6a53386f9?w=300&h=300&fit=crop",
    "葡萄": "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=300&h=300&fit=crop",
    "草莓": "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=300&h=300&fit=crop",
    "坚果": "https://images.unsplash.com/photo-1599599810694-b5b37304c041?w=300&h=300&fit=crop",
    "奶茶": "https://images.unsplash.com/photo-1558857563-b371033873b8?w=300&h=300&fit=crop",
    "可乐": "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=300&h=300&fit=crop",
    "果汁": "https://images.unsplash.com/photo-1534353473418-4cfa6c56fd38?w=300&h=300&fit=crop",
    "饼干": "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=300&h=300&fit=crop",
    "蛋糕": "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=300&h=300&fit=crop",
    "冰淇淋": "https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=300&h=300&fit=crop",
    "巧克力": "https://images.unsplash.com/photo-1481391319762-47dff72954d9?w=300&h=300&fit=crop",
    "西瓜": "https://images.unsplash.com/photo-1563114773-84221bd62daa?w=300&h=300&fit=crop",
    "蓝莓": "https://images.unsplash.com/photo-1498557850523-fd3d118b962e?w=300&h=300&fit=crop",
    "无糖茶": "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=300&h=300&fit=crop",
    "黑咖啡": "https://images.unsplash.com/photo-1514432324607-a09d9b4aefda?w=300&h=300&fit=crop",
    "柠檬水": "https://images.unsplash.com/photo-1523371054106-bbf80586c38c?w=300&h=300&fit=crop",
    "番茄鸡蛋面": "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=300&h=300&fit=crop",
    "烤红薯": "https://images.unsplash.com/photo-1590165482129-1b8b27698780?w=300&h=300&fit=crop",
    "火锅(蔬菜为主)": "https://images.unsplash.com/photo-1558030137-a56c1b004d28?w=300&h=300&fit=crop",
    "砂锅粥": "https://images.unsplash.com/photo-1604152135912-04a022e23696?w=300&h=300&fit=crop",
    "黄焖鸡+米饭": "https://images.unsplash.com/photo-1598515213692-fe3eb3e2b51c?w=300&h=300&fit=crop",
    "土豆烧牛肉+米饭": "https://images.unsplash.com/photo-1574484284002-952d92456975?w=300&h=300&fit=crop",
    "紫菜蛋花汤+面条": "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=300&h=300&fit=crop",
    "玉米排骨汤+米饭": "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=300&h=300&fit=crop",
    "酸辣土豆丝+米饭": "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=300&h=300&fit=crop",
    "清炒豆角+米饭": "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=300&h=300&fit=crop",
    "花卷": "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=300&h=300&fit=crop",
    "馒头": "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=300&h=300&fit=crop",
    "八宝粥": "https://images.unsplash.com/photo-1604152135912-04a022e23696?w=300&h=300&fit=crop",
}

FOOD_NOTES = {
    "牛奶": ["今天的牛奶很新鲜", "早上一杯牛奶，营养满满", "脱脂牛奶，控糖好搭档"],
    "豆浆": ["自磨豆浆，无糖", "豆浆配油条，经典早餐", "豆浆加了少许红枣"],
    "全麦面包": ["全麦面包，低GI选择", "配了花生酱", "健康早餐首选"],
    "煎蛋": ["一个荷包蛋", "全熟煎蛋", "鸡蛋蛋白质丰富"],
    "小米粥": ["清淡养胃", "加了红枣和枸杞", "早起一碗粥，暖暖的"],
    "包子": ["菜肉包子，两个", "猪肉大葱包子", "一荤一素，营养搭配"],
    "燕麦片": ["即食燕麦泡牛奶", "加了蓝莓和坚果", "低GI早餐"],
    "酸奶": ["无糖酸奶", "希腊酸奶，蛋白质高", "益生菌酸奶"],
    "香蕉": ["早上一根香蕉补充能量", "运动后来一根", "今天的香蕉很甜"],
    "三明治": ["鸡蛋蔬菜三明治", "全麦三明治", "加了火腿和生菜"],
    "鸡蛋饼": ["葱花鸡蛋饼", "加了火腿的鸡蛋饼", "妈妈做的鸡蛋饼"],
    "玉米": ["甜玉米，很香", "水煮玉米", "糯玉米一根"],
    "米饭+红烧肉": ["食堂红烧肉套餐", "肉质软烂入味", "今天多吃了两块"],
    "番茄炒蛋+米饭": ["家常番茄炒蛋", "酸甜可口", "经典家常菜"],
    "宫保鸡丁+米饭": ["微辣，花生很香", "鸡丁嫩滑", "食堂宫保鸡丁"],
    "鱼香肉丝+米饭": ["食堂鱼香肉丝", "酸甜微辣", "配米饭很下饭"],
    "青椒肉丝+米饭": ["清淡口味", "青椒很脆", "家常小炒"],
    "麻婆豆腐+米饭": ["微辣豆腐", "嫩豆腐很好吃", "配米饭超级好"],
    "西红柿牛腩+米饭": ["牛腩炖了很久，入味", "番茄汤浓郁", "冬天暖暖的"],
    "清炒时蔬+米饭": ["清淡健康", "今天是炒小白菜", "多吃蔬菜"],
    "糖醋排骨+米饭": ["酸甜糖醋排骨", "有点甜，控糖需注意", "偶尔吃一次"],
    "回锅肉+米饭": ["经典川菜", "蒜苗配回锅肉", "有点油腻"],
    "牛肉面": ["红烧牛肉面", "面条劲道，牛肉入味", "中午吃碗面"],
    "鸡腿饭": ["烤鸡腿饭，分量十足", "卤鸡腿便当", "蛋白质补充"],
    "沙拉+鸡胸肉": ["减脂餐", "橄榄油拌沙拉", "低脂高蛋白"],
    "饺子": ["猪肉白菜水饺", "韭菜鸡蛋饺子", "妈妈包的饺子"],
    "炒面": ["蔬菜炒面", "鸡蛋炒面", "今天的炒面不错"],
    "盖浇饭": ["鱼香茄子盖浇饭", "宫爆鸡丁盖浇饭", "快餐盖浇饭"],
    "蔬菜汤+馒头": ["清淡晚餐", "蔬菜汤好喝", "少油少盐"],
    "清蒸鱼+米饭": ["鲈鱼清蒸，鲜嫩", "清蒸更健康", "高蛋白低脂"],
    "炒青菜+粥": ["晚上吃清淡点", "白粥配青菜", "养胃晚餐"],
    "水煮虾+米饭": ["白灼虾，蘸醋吃", "虾仁Q弹", "高蛋白晚餐"],
    "白灼西兰花+鸡胸肉": ["健身餐", "减脂期标配", "蛋白质满满"],
    "凉拌黄瓜+小米粥": ["夏天凉拌菜", "清爽开胃", "轻食晚餐"],
    "苹果": ["下午茶来个苹果", "红富士苹果", "饭后一个苹果"],
    "橙子": ["新鲜橙子，维C丰富", "下午剥了个橙子", "补充维生素"],
    "葡萄": ["今天的葡萄很甜", "巨峰葡萄", "水果加餐"],
    "草莓": ["新鲜草莓，酸甜", "奶油草莓", "当季水果"],
    "坚果": ["每日坚果一小把", "核桃杏仁腰果", "健康零食"],
    "奶茶": ["忍不住喝了杯奶茶", "三分糖奶茶", "今天奖励自己"],
    "可乐": ["无糖可乐", "运动后来了瓶可乐", "偶尔放纵一下"],
    "果汁": ["鲜榨橙汁", "混合果汁", "补充维生素"],
    "饼干": ["全麦饼干", "下午茶配饼干", "办公室小零食"],
    "蛋糕": ["生日蛋糕切了一小块", "提拉米苏", "甜食要克制"],
    "冰淇淋": ["天热吃了个冰淇淋", "香草冰淇淋", "偶尔吃一次"],
    "巧克力": ["黑巧克力一小块", "70%可可含量", "下午提神"],
    "西瓜": ["夏天的西瓜最好吃", "冰镇西瓜", "消暑必备"],
    "蓝莓": ["新鲜蓝莓", "富含花青素", "护眼水果"],
    "无糖茶": ["无糖绿茶", "下午来杯茶", "0糖0卡"],
    "黑咖啡": ["提神醒脑", "美式黑咖啡", "下午一杯咖啡"],
    "柠檬水": ["自制柠檬水", "清爽解渴", "加了薄荷叶"],
    "番茄鸡蛋面": ["热汤面", "简单快手的晚餐", "番茄味浓郁"],
    "烤红薯": ["街边烤红薯", "软糯香甜", "冬天最爱"],
    "火锅(蔬菜为主)": ["和朋友吃火锅", "多涮了蔬菜", "汤底选了清汤"],
    "砂锅粥": ["海鲜砂锅粥", "暖胃好选择", "晚上喝粥舒服"],
    "黄焖鸡+米饭": ["黄焖鸡套餐", "微辣，好下饭", "中午外卖"],
    "土豆烧牛肉+米饭": ["炖了很久的牛肉", "土豆软烂", "高蛋白午餐"],
    "紫菜蛋花汤+面条": ["清淡面条", "汤很鲜", "简单晚餐"],
    "玉米排骨汤+米饭": ["排骨汤很鲜", "炖了两小时", "营养丰富"],
    "酸辣土豆丝+米饭": ["经典酸辣味", "土豆丝脆脆的", "简单家常菜"],
    "清炒豆角+米饭": ["豆角炒得很入味", "清淡健康", "夏天多吃蔬菜"],
    "花卷": ["葱花花卷", "松软可口", "早餐主食"],
    "馒头": ["白馒头", "自己蒸的馒头", "主食搭配"],
    "八宝粥": ["自煮八宝粥", "加了红豆薏米", "营养早餐"],
}


def download_image(url: str, save_path: str) -> bool:
    try:
        headers = {"User-Agent": "Mozilla/5.0"}
        resp = requests.get(url, headers=headers, timeout=15, stream=True)
        if resp.status_code == 200:
            with open(save_path, "wb") as f:
                for chunk in resp.iter_content(8192):
                    f.write(chunk)
            return True
        else:
            print(f"  HTTP {resp.status_code} for {url}")
            return False
    except Exception as e:
        print(f"  download error: {e}")
        return False


def main():
    UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

    downloaded = {}
    total = len(FOOD_IMAGE_URLS)
    success_count = 0

    print(f"downloading {total} food images...")
    for i, (food_name, url) in enumerate(FOOD_IMAGE_URLS.items(), 1):
        safe_name = food_name.replace("+", "_").replace("(", "").replace(")", "").replace("/", "_")
        filename = f"{safe_name}.jpg"
        filepath = UPLOAD_DIR / filename

        if filepath.exists() and filepath.stat().st_size > 1000:
            print(f"  [{i}/{total}] {food_name} - already exists")
            downloaded[food_name] = f"uploads/meals/{filename}"
            success_count += 1
            continue

        ok = download_image(url, str(filepath))
        if ok and filepath.stat().st_size > 1000:
            print(f"  [{i}/{total}] {food_name} - OK ({filepath.stat().st_size} bytes)")
            downloaded[food_name] = f"uploads/meals/{filename}"
            success_count += 1
        else:
            print(f"  [{i}/{total}] {food_name} - FAILED")
            if filepath.exists():
                filepath.unlink()

        time.sleep(0.3)

    print(f"\ndownloaded {success_count}/{total} images")

    conn = pymysql.connect(**DB_CONFIG)
    try:
        cursor = conn.cursor()

        for food_name, image_rel_path in downloaded.items():
            notes_list = FOOD_NOTES.get(food_name, ["日常饮食记录"])
            note = random.choice(notes_list)

            cursor.execute(
                """
                UPDATE meal_records
                SET image_path = %s, notes = %s
                WHERE user_id = %s AND food_name = %s AND (image_path IS NULL OR image_path = '')
                """,
                (image_rel_path, note, USER_ID, food_name)
            )

        conn.commit()

        cursor.execute(
            "SELECT COUNT(*) FROM meal_records WHERE user_id = %s AND image_path IS NOT NULL AND image_path != ''",
            (USER_ID,)
        )
        with_image = cursor.fetchone()[0]

        cursor.execute(
            "SELECT COUNT(*) FROM meal_records WHERE user_id = %s AND notes IS NOT NULL AND notes != ''",
            (USER_ID,)
        )
        with_notes = cursor.fetchone()[0]

        cursor.execute("SELECT COUNT(*) FROM meal_records WHERE user_id = %s", (USER_ID,))
        total_records = cursor.fetchone()[0]

        print(f"\nupdated DB:")
        print(f"  records with image: {with_image}/{total_records}")
        print(f"  records with notes: {with_notes}/{total_records}")

    finally:
        conn.close()


if __name__ == "__main__":
    main()
