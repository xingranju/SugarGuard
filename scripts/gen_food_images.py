"""
生成食物图标图片并更新数据库（image_path + notes）
"""
import pymysql
import random
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import hashlib

DB_CONFIG = {
    "host": "localhost", "port": 3306, "user": "root",
    "password": "123456", "database": "Android_health_db", "charset": "utf8mb4"
}
USER_ID = 20
UPLOAD_DIR = Path(r"E:\code\Anroid\MyApplication\uploads\meals")

FOOD_COLORS = {
    "牛奶": ("#FFFDE7", "#F9A825"), "豆浆": ("#FFF8E1", "#FF8F00"),
    "全麦面包": ("#EFEBE9", "#6D4C41"), "煎蛋": ("#FFFDE7", "#FDD835"),
    "小米粥": ("#FFF8E1", "#FFB300"), "包子": ("#FBE9E7", "#E64A19"),
    "燕麦片": ("#F1F8E9", "#7CB342"), "酸奶": ("#F3E5F5", "#AB47BC"),
    "香蕉": ("#FFFDE7", "#FBC02D"), "三明治": ("#E8F5E9", "#43A047"),
    "鸡蛋饼": ("#FFF3E0", "#FB8C00"), "玉米": ("#FFFDE7", "#F9A825"),
    "花卷": ("#FBE9E7", "#BF360C"), "馒头": ("#EFEBE9", "#8D6E63"),
    "八宝粥": ("#FCE4EC", "#C62828"),
    "米饭+红烧肉": ("#FFEBEE", "#C62828"), "番茄炒蛋+米饭": ("#FFF3E0", "#E65100"),
    "宫保鸡丁+米饭": ("#FBE9E7", "#BF360C"), "鱼香肉丝+米饭": ("#FFF8E1", "#FF6F00"),
    "青椒肉丝+米饭": ("#E8F5E9", "#2E7D32"), "麻婆豆腐+米饭": ("#FFEBEE", "#B71C1C"),
    "西红柿牛腩+米饭": ("#FFEBEE", "#D32F2F"), "清炒时蔬+米饭": ("#E8F5E9", "#388E3C"),
    "糖醋排骨+米饭": ("#FCE4EC", "#AD1457"), "回锅肉+米饭": ("#FBE9E7", "#D84315"),
    "牛肉面": ("#EFEBE9", "#5D4037"), "鸡腿饭": ("#FFF3E0", "#E65100"),
    "沙拉+鸡胸肉": ("#E8F5E9", "#1B5E20"), "饺子": ("#ECEFF1", "#455A64"),
    "炒面": ("#FFF8E1", "#F57F17"), "盖浇饭": ("#FFF3E0", "#EF6C00"),
    "黄焖鸡+米饭": ("#FFF8E1", "#FF8F00"), "土豆烧牛肉+米饭": ("#EFEBE9", "#4E342E"),
    "蔬菜汤+馒头": ("#E8F5E9", "#66BB6A"), "清蒸鱼+米饭": ("#E3F2FD", "#1565C0"),
    "炒青菜+粥": ("#F1F8E9", "#558B2F"), "紫菜蛋花汤+面条": ("#E8EAF6", "#283593"),
    "水煮虾+米饭": ("#FFF3E0", "#FF6D00"), "白灼西兰花+鸡胸肉": ("#E8F5E9", "#2E7D32"),
    "凉拌黄瓜+小米粥": ("#E8F5E9", "#4CAF50"), "玉米排骨汤+米饭": ("#FFF8E1", "#FFA000"),
    "酸辣土豆丝+米饭": ("#FFFDE7", "#F9A825"), "清炒豆角+米饭": ("#F1F8E9", "#689F38"),
    "番茄鸡蛋面": ("#FFF3E0", "#FF5722"), "烤红薯": ("#FFF3E0", "#E65100"),
    "火锅(蔬菜为主)": ("#FFEBEE", "#F44336"), "砂锅粥": ("#FFF8E1", "#FFB300"),
    "苹果": ("#FFEBEE", "#E53935"), "橙子": ("#FFF3E0", "#FF9800"),
    "葡萄": ("#F3E5F5", "#7B1FA2"), "草莓": ("#FCE4EC", "#E91E63"),
    "坚果": ("#EFEBE9", "#795548"), "奶茶": ("#EFEBE9", "#6D4C41"),
    "可乐": ("#FFEBEE", "#B71C1C"), "果汁": ("#FFF3E0", "#FF6D00"),
    "饼干": ("#FFF8E1", "#FFA000"), "蛋糕": ("#FCE4EC", "#EC407A"),
    "冰淇淋": ("#E8F5E9", "#66BB6A"), "巧克力": ("#EFEBE9", "#4E342E"),
    "西瓜": ("#E8F5E9", "#43A047"), "蓝莓": ("#E8EAF6", "#3F51B5"),
    "无糖茶": ("#E8F5E9", "#2E7D32"), "黑咖啡": ("#EFEBE9", "#3E2723"),
    "柠檬水": ("#FFFDE7", "#C0CA33"),
}

FOOD_EMOJIS = {
    "牛奶": "🥛", "豆浆": "🫘", "全麦面包": "🍞", "煎蛋": "🍳",
    "小米粥": "🥣", "包子": "🥟", "燕麦片": "🥣", "酸奶": "🥛",
    "香蕉": "🍌", "三明治": "🥪", "鸡蛋饼": "🥞", "玉米": "🌽",
    "花卷": "🍞", "馒头": "🍞", "八宝粥": "🥣",
    "米饭+红烧肉": "🍖", "番茄炒蛋+米饭": "🍅", "宫保鸡丁+米饭": "🍗",
    "鱼香肉丝+米饭": "🥩", "青椒肉丝+米饭": "🌶", "麻婆豆腐+米饭": "🔥",
    "西红柿牛腩+米饭": "🐂", "清炒时蔬+米饭": "🥬", "糖醋排骨+米饭": "🍖",
    "回锅肉+米饭": "🥩", "牛肉面": "🍜", "鸡腿饭": "🍗",
    "沙拉+鸡胸肉": "🥗", "饺子": "🥟", "炒面": "🍝", "盖浇饭": "🍛",
    "黄焖鸡+米饭": "🍗", "土豆烧牛肉+米饭": "🥘",
    "蔬菜汤+馒头": "🥣", "清蒸鱼+米饭": "🐟", "炒青菜+粥": "🥬",
    "紫菜蛋花汤+面条": "🍜", "水煮虾+米饭": "🦐", "白灼西兰花+鸡胸肉": "🥦",
    "凉拌黄瓜+小米粥": "🥒", "玉米排骨汤+米饭": "🌽",
    "酸辣土豆丝+米饭": "🥔", "清炒豆角+米饭": "🫛",
    "番茄鸡蛋面": "🍝", "烤红薯": "🍠",
    "火锅(蔬菜为主)": "🍲", "砂锅粥": "🥘",
    "苹果": "🍎", "橙子": "🍊", "葡萄": "🍇", "草莓": "🍓",
    "坚果": "🥜", "奶茶": "🧋", "可乐": "🥤", "果汁": "🧃",
    "饼干": "🍪", "蛋糕": "🍰", "冰淇淋": "🍦", "巧克力": "🍫",
    "西瓜": "🍉", "蓝莓": "🫐", "无糖茶": "🍵", "黑咖啡": "☕",
    "柠檬水": "🍋",
}

FOOD_NOTES = {
    "牛奶": ["今天的牛奶很新鲜", "早上一杯牛奶，营养满满", "脱脂牛奶，控糖好搭档"],
    "豆浆": ["自磨豆浆，无糖", "豆浆配油条，经典早餐", "加了少许红枣"],
    "全麦面包": ["全麦面包，低GI选择", "配了花生酱", "健康早餐首选"],
    "煎蛋": ["一个荷包蛋", "全熟煎蛋", "鸡蛋蛋白质丰富"],
    "小米粥": ["清淡养胃", "加了红枣和枸杞", "暖暖的"],
    "包子": ["菜肉包子两个", "猪肉大葱包子", "一荤一素搭配"],
    "燕麦片": ["即食燕麦泡牛奶", "加了蓝莓和坚果", "低GI早餐"],
    "酸奶": ["无糖酸奶", "希腊酸奶", "益生菌酸奶"],
    "香蕉": ["补充能量", "运动后来一根", "今天的香蕉很甜"],
    "三明治": ["鸡蛋蔬菜三明治", "全麦三明治", "加了火腿和生菜"],
    "鸡蛋饼": ["葱花鸡蛋饼", "加了火腿的鸡蛋饼"],
    "玉米": ["甜玉米很香", "水煮玉米", "糯玉米一根"],
    "花卷": ["葱花花卷", "松软可口"],
    "馒头": ["白馒头", "自己蒸的馒头"],
    "八宝粥": ["自煮八宝粥", "加了红豆薏米"],
    "米饭+红烧肉": ["食堂红烧肉套餐", "肉质软烂入味"],
    "番茄炒蛋+米饭": ["家常番茄炒蛋", "酸甜可口"],
    "宫保鸡丁+米饭": ["微辣花生很香", "鸡丁嫩滑"],
    "鱼香肉丝+米饭": ["食堂鱼香肉丝", "酸甜微辣好下饭"],
    "青椒肉丝+米饭": ["清淡口味", "青椒很脆"],
    "麻婆豆腐+米饭": ["微辣豆腐", "嫩豆腐配米饭"],
    "西红柿牛腩+米饭": ["牛腩入味", "番茄汤浓郁"],
    "清炒时蔬+米饭": ["清淡健康", "今天是炒小白菜"],
    "糖醋排骨+米饭": ["酸甜糖醋排骨", "有点甜注意控糖"],
    "回锅肉+米饭": ["经典川菜", "蒜苗配回锅肉"],
    "牛肉面": ["红烧牛肉面", "面条劲道牛肉入味"],
    "鸡腿饭": ["烤鸡腿饭分量足", "卤鸡腿便当"],
    "沙拉+鸡胸肉": ["减脂餐", "橄榄油拌沙拉"],
    "饺子": ["猪肉白菜水饺", "韭菜鸡蛋饺子"],
    "炒面": ["蔬菜炒面", "鸡蛋炒面"],
    "盖浇饭": ["鱼香茄子盖浇饭", "快餐盖浇饭"],
    "黄焖鸡+米饭": ["黄焖鸡套餐", "微辣好下饭"],
    "土豆烧牛肉+米饭": ["炖了很久的牛肉", "土豆软烂"],
    "蔬菜汤+馒头": ["清淡晚餐", "蔬菜汤好喝"],
    "清蒸鱼+米饭": ["鲈鱼清蒸鲜嫩", "高蛋白低脂"],
    "炒青菜+粥": ["晚上吃清淡点", "白粥配青菜"],
    "紫菜蛋花汤+面条": ["清淡面条", "汤很鲜"],
    "水煮虾+米饭": ["白灼虾蘸醋吃", "高蛋白晚餐"],
    "白灼西兰花+鸡胸肉": ["健身餐", "减脂期标配"],
    "凉拌黄瓜+小米粥": ["夏天凉拌菜", "清爽开胃"],
    "玉米排骨汤+米饭": ["排骨汤很鲜", "营养丰富"],
    "酸辣土豆丝+米饭": ["经典酸辣味", "土豆丝脆脆的"],
    "清炒豆角+米饭": ["豆角炒得入味", "清淡健康"],
    "番茄鸡蛋面": ["热汤面", "番茄味浓郁"],
    "烤红薯": ["街边烤红薯", "软糯香甜"],
    "火锅(蔬菜为主)": ["和朋友吃火锅", "多涮了蔬菜"],
    "砂锅粥": ["海鲜砂锅粥", "暖胃好选择"],
    "苹果": ["下午茶来个苹果", "红富士苹果"],
    "橙子": ["新鲜橙子维C丰富", "补充维生素"],
    "葡萄": ["今天的葡萄很甜", "巨峰葡萄"],
    "草莓": ["新鲜草莓酸甜", "当季水果"],
    "坚果": ["每日坚果一小把", "核桃杏仁腰果"],
    "奶茶": ["忍不住喝了杯奶茶", "三分糖奶茶"],
    "可乐": ["无糖可乐", "偶尔放纵一下"],
    "果汁": ["鲜榨橙汁", "补充维生素"],
    "饼干": ["全麦饼干", "办公室小零食"],
    "蛋糕": ["切了一小块蛋糕", "甜食要克制"],
    "冰淇淋": ["天热吃了个冰淇淋", "偶尔吃一次"],
    "巧克力": ["黑巧克力一小块", "70%可可提神"],
    "西瓜": ["夏天的西瓜最好吃", "冰镇西瓜"],
    "蓝莓": ["新鲜蓝莓", "护眼水果"],
    "无糖茶": ["无糖绿茶", "0糖0卡"],
    "黑咖啡": ["提神醒脑", "美式黑咖啡"],
    "柠檬水": ["自制柠檬水", "清爽解渴"],
}


def hex_to_rgb(h: str):
    h = h.lstrip("#")
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))


def gen_food_image(food_name: str, bg_hex: str, fg_hex: str, emoji: str, size=300) -> Image.Image:
    bg = hex_to_rgb(bg_hex)
    fg = hex_to_rgb(fg_hex)
    img = Image.new("RGB", (size, size), bg)
    draw = ImageDraw.Draw(img)

    try:
        emoji_font = ImageFont.truetype("C:/Windows/Fonts/seguiemj.ttf", 80)
    except Exception:
        emoji_font = ImageFont.load_default()

    try:
        text_font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 28)
    except Exception:
        try:
            text_font = ImageFont.truetype("C:/Windows/Fonts/simhei.ttf", 28)
        except Exception:
            text_font = ImageFont.load_default()

    center = size // 2
    r = size // 2 - 20
    draw.ellipse([center - r, center - r - 15, center + r, center + r - 15], fill=fg + (40,) if len(fg) == 3 else fg, outline=fg)

    try:
        bbox = draw.textbbox((0, 0), emoji, font=emoji_font)
        ew, eh = bbox[2] - bbox[0], bbox[3] - bbox[1]
        draw.text((center - ew // 2, center - eh // 2 - 30), emoji, font=emoji_font, fill="white")
    except Exception:
        pass

    display_name = food_name.split("+")[0] if "+" in food_name else food_name
    if len(display_name) > 5:
        display_name = display_name[:5]
    try:
        bbox = draw.textbbox((0, 0), display_name, font=text_font)
        tw = bbox[2] - bbox[0]
        draw.text((center - tw // 2, center + 50), display_name, font=text_font, fill=fg)
    except Exception:
        pass

    return img


def main():
    UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

    generated = {}
    for food_name in FOOD_COLORS:
        bg, fg = FOOD_COLORS[food_name]
        emoji = FOOD_EMOJIS.get(food_name, "🍽")
        safe_name = food_name.replace("+", "_").replace("(", "").replace(")", "").replace("/", "_")
        filename = f"{safe_name}.jpg"
        filepath = UPLOAD_DIR / filename

        img = gen_food_image(food_name, bg, fg, emoji)
        img.save(str(filepath), "JPEG", quality=90)
        generated[food_name] = f"uploads/meals/{filename}"

    print(f"generated {len(generated)} food images")

    conn = pymysql.connect(**DB_CONFIG)
    try:
        cursor = conn.cursor()
        updated_img = 0
        updated_notes = 0

        for food_name, image_rel_path in generated.items():
            notes_list = FOOD_NOTES.get(food_name, ["日常饮食记录"])

            cursor.execute(
                "SELECT meal_id FROM meal_records WHERE user_id = %s AND food_name = %s",
                (USER_ID, food_name)
            )
            meal_ids = [r[0] for r in cursor.fetchall()]

            for mid in meal_ids:
                note = random.choice(notes_list)
                cursor.execute(
                    "UPDATE meal_records SET image_path = %s, notes = %s WHERE meal_id = %s",
                    (image_rel_path, note, mid)
                )
                updated_img += 1
                updated_notes += 1

        conn.commit()

        cursor.execute(
            "SELECT COUNT(*) FROM meal_records WHERE user_id=%s AND image_path IS NOT NULL AND image_path!=''",
            (USER_ID,)
        )
        with_image = cursor.fetchone()[0]
        cursor.execute(
            "SELECT COUNT(*) FROM meal_records WHERE user_id=%s AND notes IS NOT NULL AND notes!=''",
            (USER_ID,)
        )
        with_notes = cursor.fetchone()[0]
        cursor.execute("SELECT COUNT(*) FROM meal_records WHERE user_id=%s", (USER_ID,))
        total = cursor.fetchone()[0]

        print(f"updated {updated_img} image_path, {updated_notes} notes")
        print(f"records with image: {with_image}/{total}")
        print(f"records with notes: {with_notes}/{total}")

    finally:
        conn.close()


if __name__ == "__main__":
    main()
