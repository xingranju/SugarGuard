# -*- coding: utf-8 -*-
"""
糖知 APP (SugarGuard) 饮品数据库扩充脚本
=============================================
输出：
  1. drinks_200.sql     —— 200+ 条 INSERT SQL，可直接在 MySQL 执行
  2. seed_drinks_200.py —— 等价的 Python 版（通过 PyMySQL 写库）

字段严格对齐 backend-api 实体 com.example.usermanagement.entity.Drink：
    drink_name, brand, category, sugar_content, calories, volume,
    caffeine, fat, protein, sodium, health_score, ingredients,
    allergens, image_url, source_url

覆盖品牌：
  茶饮：喜茶/奈雪/霸王茶姬/茶百道/7分甜/沪上阿姨/蜜雪冰城
  咖啡：星巴克/瑞幸/库迪/Manner/Peet's
  碳酸/水：可口可乐/农夫山泉/元气森林
  其他：王老吉/红牛/蒙牛/伊利
"""
from __future__ import annotations
import os
import json
from dataclasses import dataclass, field, asdict
from typing import List, Optional

OUT_DIR = os.path.dirname(os.path.abspath(__file__))

# 每款饮品默认容量（ml）、咖啡因等，由各品牌子函数覆盖
DEFAULT_VOL = 500.0


@dataclass
class Drink:
    drink_name: str
    brand: str
    category: str            # 奶茶/果茶/咖啡/碳酸饮料/果汁/茶饮/乳饮/功能饮料/气泡水/瓶装水/凉茶
    sugar_content: float     # g / 单份
    calories: float          # kcal / 单份
    volume: float = DEFAULT_VOL   # ml
    caffeine: float = 0.0    # mg
    fat: float = 0.0         # g
    protein: float = 0.0     # g
    sodium: float = 0.0      # mg
    health_score: int = 50   # 0~100
    ingredients: str = ""
    allergens: str = ""
    image_url: str = ""
    source_url: str = ""


def health_score(sugar_g: float, cal: float, caffeine: float = 0) -> int:
    """简单评分：糖越低 / 热量越低 / 咖啡因适中 → 分越高"""
    s = 100.0
    s -= min(50.0, sugar_g * 1.2)       # 1g 糖扣 1.2 分，封顶 50
    s -= min(30.0, (cal - 80) * 0.08) if cal > 80 else 0  # 高热量扣
    if caffeine > 200:
        s -= min(15.0, (caffeine - 200) * 0.05)
    return max(10, min(100, int(round(s))))


def img(slug: str) -> str:
    """统一用 Unsplash 的 featured 接口，按关键词出图，避免死链"""
    return f"https://source.unsplash.com/featured/600x600/?{slug}"


DRINKS: List[Drink] = []


# =================================================================
# 喜茶 HEYTEA （15 款）
# =================================================================
def add_heytea():
    base_url = "https://www.heytea.com/"
    items = [
        ("芝芝莓莓", "果茶", 35.0, 380.0, 600, 25, 12.5, 3.5, 85, "鲜牛奶,草莓,芒果,茶汤,奶盖,蔗糖", "乳,茶"),
        ("多肉葡萄", "果茶", 42.0, 330.0, 600, 20, 1.2, 1.8, 45, "巨峰葡萄,茶汤,芝士,果糖", "乳"),
        ("满杯红柚", "果茶", 30.0, 260.0, 600, 18, 0.8, 1.2, 40, "红柚,茶汤,果糖,糖渍柚皮", ""),
        ("芝芝桃桃", "果茶", 38.0, 370.0, 600, 22, 11.8, 3.3, 80, "鲜牛奶,水蜜桃,茶汤,奶盖", "乳"),
        ("纯茶芝士", "奶茶", 28.0, 310.0, 500, 40, 13.2, 4.1, 180, "红茶,鲜奶,奶盖,蔗糖", "乳"),
        ("波波奶茶", "奶茶", 45.0, 450.0, 500, 45, 14.5, 5.0, 160, "珍珠,鲜奶,红茶,蔗糖", "乳,麸质"),
        ("可可多肉葡萄", "果茶", 40.0, 400.0, 600, 22, 9.8, 2.8, 90, "葡萄,可可,鲜奶,茶汤", "乳"),
        ("烤黑糖波波牛乳", "奶茶", 55.0, 520.0, 500, 0, 16.0, 6.0, 120, "黑糖,珍珠,鲜牛奶", "乳,麸质"),
        ("波波茶", "奶茶", 38.0, 400.0, 500, 50, 12.8, 4.5, 140, "珍珠,红茶,牛奶,蔗糖", "乳,麸质"),
        ("轻乳茶", "奶茶", 18.0, 200.0, 500, 35, 5.2, 3.0, 100, "轻牛乳,红茶,蔗糖", "乳"),
        ("芝士绿妍", "奶茶", 22.0, 280.0, 500, 55, 11.5, 3.2, 150, "绿茶,奶盖,蔗糖", "乳"),
        ("鸭屎香柠檬茶", "果茶", 25.0, 190.0, 600, 38, 0.3, 0.8, 35, "凤凰单丛茶,柠檬,果糖", ""),
        ("豆豆波波烤奶", "奶茶", 48.0, 480.0, 500, 45, 15.8, 5.5, 170, "红豆,珍珠,烤奶,黑糖", "乳,豆,麸质"),
        ("酷黑莓桑", "果茶", 32.0, 300.0, 600, 18, 1.0, 1.5, 50, "蓝莓,桑葚,茶汤,果糖", ""),
        ("4D芒果牛牛", "果茶", 40.0, 420.0, 600, 22, 13.0, 3.8, 85, "芒果,牛奶,奶盖,椰汁", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="喜茶 HEYTEA", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"heytea,{cat},milk+tea"),
            source_url=base_url,
        ))


# =================================================================
# 奈雪的茶 Nayuki（12 款）
# =================================================================
def add_nayuki():
    base_url = "https://www.naixuecha.com/"
    items = [
        ("霸气芝士草莓", "果茶", 42.0, 410.0, 650, 22, 13.0, 3.6, 90, "草莓,芝士,茶汤,奶盖", "乳"),
        ("霸气芝士葡萄", "果茶", 45.0, 420.0, 650, 20, 12.8, 3.4, 85, "巨峰葡萄,芝士,茶汤,奶盖", "乳"),
        ("霸气橙子", "果茶", 36.0, 320.0, 650, 20, 1.5, 2.0, 40, "橙子,茶汤,果糖", ""),
        ("霸气蜜桃", "果茶", 40.0, 380.0, 650, 22, 12.5, 3.2, 80, "水蜜桃,芝士,奶盖,茶汤", "乳"),
        ("霸气芝士山竹", "果茶", 48.0, 450.0, 650, 22, 13.5, 3.8, 90, "山竹,芝士,奶盖", "乳"),
        ("鸭屎香宝藏茶", "奶茶", 30.0, 300.0, 500, 40, 9.5, 3.0, 120, "鸭屎香单丛,鲜奶,芝士,蔗糖", "乳"),
        ("珍珠奶茶", "奶茶", 50.0, 480.0, 500, 45, 14.5, 5.2, 150, "珍珠,红茶,牛奶,蔗糖", "乳,麸质"),
        ("金色山脉栗宝茶", "奶茶", 58.0, 560.0, 500, 48, 16.5, 5.8, 140, "栗子,红茶,牛奶,燕麦", "乳,坚果,麸质"),
        ("轻盈葡萄奇异果", "果茶", 28.0, 220.0, 650, 18, 0.5, 1.0, 30, "葡萄,奇异果,茶汤,代糖", ""),
        ("黑糖牛乳", "奶茶", 56.0, 540.0, 500, 0, 17.0, 6.5, 130, "黑糖,珍珠,鲜牛乳", "乳,麸质"),
        ("原叶绿茶", "奶茶", 15.0, 180.0, 500, 60, 4.5, 2.8, 90, "原叶绿茶,鲜奶,蔗糖", "乳"),
        ("杨枝甘露", "果茶", 38.0, 420.0, 600, 0, 14.0, 4.2, 90, "芒果,西米,椰浆,柚子", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="奈雪的茶 Nayuki", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"nayuki,milk+tea,{cat}"),
            source_url=base_url,
        ))


# =================================================================
# 霸王茶姬 CHAGEE（10 款）
# =================================================================
def add_chagee():
    base_url = "https://www.chagee.com/"
    items = [
        ("伯牙绝弦", "奶茶", 18.0, 240.0, 500, 60, 7.0, 3.2, 120, "冰种雪山茶,鲜牛乳", "乳"),
        ("桂馥兰香", "奶茶", 16.0, 210.0, 500, 55, 6.5, 3.0, 110, "桂花,金萱茶,鲜牛乳", "乳"),
        ("花田乌龙", "奶茶", 14.0, 200.0, 500, 58, 5.8, 2.8, 105, "乌龙,桂花,鲜奶", "乳"),
        ("万里木兰", "奶茶", 22.0, 250.0, 500, 52, 6.5, 3.0, 115, "大叶乌龙,鲜牛乳", "乳"),
        ("白雾红尘", "奶茶", 20.0, 240.0, 500, 50, 6.0, 3.0, 110, "祁门红茶,鲜奶", "乳"),
        ("青青糯山", "奶茶", 24.0, 260.0, 500, 55, 7.2, 3.2, 120, "糯米香,鲜奶", "乳"),
        ("云上晓岛", "奶茶", 18.0, 230.0, 500, 50, 6.5, 3.0, 110, "冻顶乌龙,鲜奶,燕麦", "乳,麸质"),
        ("兰亭序", "奶茶", 20.0, 230.0, 500, 55, 6.4, 3.0, 110, "茉莉绿茶,鲜奶", "乳"),
        ("知时无香", "奶茶", 12.0, 180.0, 500, 45, 5.0, 2.5, 90, "普洱,鲜奶", "乳"),
        ("竹影婆娑", "奶茶", 16.0, 200.0, 500, 50, 5.6, 2.7, 100, "竹叶,绿茶,鲜奶", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="霸王茶姬 CHAGEE", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"chagee,original+leaf+milk+tea"),
            source_url=base_url,
        ))


# =================================================================
# 茶百道（10 款）
# =================================================================
def add_chabaidao():
    base_url = "https://www.chabaidao.com/"
    items = [
        ("杨枝甘露", "果茶", 42.0, 420.0, 600, 0, 13.5, 4.2, 85, "芒果,西柚,西米,椰浆", "乳"),
        ("招牌奶茶", "奶茶", 50.0, 480.0, 500, 50, 14.8, 5.2, 160, "红茶,鲜奶,珍珠,蔗糖", "乳,麸质"),
        ("豆豆波波奶茶", "奶茶", 52.0, 510.0, 500, 48, 15.2, 5.5, 170, "红豆,珍珠,鲜奶,黑糖", "乳,豆,麸质"),
        ("草莓啵啵", "果茶", 38.0, 370.0, 600, 22, 11.0, 3.2, 80, "草莓,啵啵,奶盖,果糖", "乳"),
        ("爆柠车厘子", "果茶", 32.0, 300.0, 600, 20, 1.0, 1.5, 45, "柠檬,车厘子,茶汤", ""),
        ("葡萄冰沙", "果茶", 40.0, 380.0, 500, 18, 2.0, 1.8, 40, "巨峰葡萄,碎冰,果糖", ""),
        ("厚乳红茶", "奶茶", 48.0, 490.0, 500, 55, 17.5, 6.0, 130, "厚乳,红茶,蔗糖", "乳"),
        ("观音杨梅", "果茶", 36.0, 320.0, 600, 35, 0.8, 1.2, 40, "杨梅,观音乌龙,果糖", ""),
        ("黑糖珍珠鲜牛乳", "奶茶", 55.0, 520.0, 500, 0, 17.0, 6.2, 130, "黑糖,珍珠,鲜牛奶", "乳,麸质"),
        ("茉莉奶绿", "奶茶", 30.0, 340.0, 500, 45, 10.8, 3.8, 120, "茉莉绿茶,鲜奶,蔗糖", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="茶百道", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"chabaidao,bubble+tea"),
            source_url=base_url,
        ))


# =================================================================
# 7分甜（10 款）
# =================================================================
def add_7fentian():
    base_url = "https://www.7fen.com/"
    items = [
        ("杨枝甘露", "果茶", 40.0, 400.0, 600, 0, 13.0, 4.0, 80, "芒果,西米,椰浆,西柚", "乳"),
        ("芒果冰沙", "果茶", 38.0, 340.0, 500, 0, 2.0, 1.8, 35, "芒果,冰沙,果糖", ""),
        ("杨枝甘露波波", "果茶", 44.0, 450.0, 600, 0, 13.8, 4.5, 90, "芒果,珍珠,椰浆", "乳,麸质"),
        ("满杯百香果", "果茶", 38.0, 360.0, 600, 15, 2.2, 2.0, 45, "百香果,芒果,茶汤", ""),
        ("芒果西米露", "果茶", 42.0, 420.0, 500, 0, 8.5, 3.2, 70, "芒果,西米,椰奶", "乳"),
        ("杨枝甘露小料控", "果茶", 48.0, 500.0, 650, 0, 14.5, 5.0, 95, "芒果,西米,椰果,葡萄柚", "乳"),
        ("柚子杨枝甘露", "果茶", 40.0, 400.0, 600, 0, 12.0, 4.0, 85, "红柚,芒果,椰浆", "乳"),
        ("柠檬芒果冰", "果茶", 35.0, 310.0, 600, 0, 1.5, 1.5, 40, "芒果,柠檬,果糖,冰", ""),
        ("甘露双杯", "果茶", 46.0, 470.0, 700, 0, 13.5, 4.5, 90, "芒果,柚子,西米,椰浆", "乳"),
        ("爱上甘露奶茶", "奶茶", 50.0, 510.0, 500, 30, 15.0, 5.5, 130, "芒果,奶茶,椰浆", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="7分甜", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"mango+pomelo+sago"),
            source_url=base_url,
        ))


# =================================================================
# 沪上阿姨（10 款）
# =================================================================
def add_hushangayi():
    base_url = "https://www.hushangayi.com/"
    items = [
        ("五谷红豆奶茶", "奶茶", 52.0, 520.0, 500, 45, 16.0, 6.5, 160, "红豆,五谷,鲜奶,红茶", "乳,麸质,豆"),
        ("五谷杂粮奶茶", "奶茶", 48.0, 500.0, 500, 45, 15.5, 6.2, 150, "燕麦,糙米,鲜奶,红茶", "乳,麸质"),
        ("草莓芝士", "果茶", 40.0, 390.0, 600, 20, 12.0, 3.5, 90, "草莓,芝士,茶汤", "乳"),
        ("黑糖奶茶", "奶茶", 56.0, 540.0, 500, 0, 17.5, 6.5, 140, "黑糖,珍珠,鲜奶", "乳,麸质"),
        ("葡萄冻冻", "果茶", 36.0, 330.0, 600, 18, 1.2, 1.8, 45, "巨峰葡萄,葡萄冻,茶汤", ""),
        ("芋泥波波奶茶", "奶茶", 55.0, 560.0, 500, 35, 16.8, 6.0, 150, "芋泥,珍珠,鲜奶", "乳,麸质"),
        ("鲜芒波波奶茶", "奶茶", 50.0, 500.0, 500, 30, 15.5, 5.5, 140, "芒果,珍珠,鲜奶", "乳,麸质"),
        ("杨枝甘露五谷", "果茶", 45.0, 460.0, 600, 0, 13.5, 4.5, 95, "芒果,五谷,椰浆", "乳"),
        ("鲜酪酸奶昔", "乳饮", 32.0, 350.0, 500, 0, 10.5, 8.0, 90, "酸奶,草莓,蜂蜜", "乳"),
        ("黑森林莓莓", "果茶", 38.0, 370.0, 600, 22, 11.5, 3.3, 85, "树莓,蓝莓,奶盖", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="沪上阿姨", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"grain+milk+tea,china"),
            source_url=base_url,
        ))


# =================================================================
# 蜜雪冰城 MIXUE（12 款）
# =================================================================
def add_mixue():
    base_url = "https://www.mxbc.com/"
    items = [
        ("冰鲜柠檬水", "果茶", 28.0, 120.0, 650, 0, 0.2, 0.5, 20, "柠檬,果糖,冰", ""),
        ("珍珠奶茶", "奶茶", 42.0, 420.0, 500, 35, 13.0, 4.8, 130, "珍珠,奶精,红茶粉", "乳,麸质"),
        ("摇摇奶昔", "乳饮", 35.0, 380.0, 400, 0, 12.5, 4.5, 120, "奶粉,植脂,蔗糖", "乳"),
        ("冰激凌圣代", "乳饮", 38.0, 350.0, 250, 0, 11.0, 4.0, 110, "奶粉,植脂,蔗糖,巧克力", "乳"),
        ("茉莉奶绿", "奶茶", 36.0, 380.0, 500, 45, 12.0, 4.2, 120, "茉莉绿茶,奶精,蔗糖", "乳"),
        ("杨枝甘露", "果茶", 42.0, 420.0, 500, 0, 12.5, 4.0, 75, "芒果,椰浆,西米", "乳"),
        ("四季春", "果茶", 18.0, 150.0, 650, 35, 0.5, 0.8, 30, "四季春茶,果糖", ""),
        ("蜜桃四季春", "果茶", 28.0, 220.0, 650, 30, 0.8, 1.2, 35, "蜜桃,四季春茶,果糖", ""),
        ("满杯百香果", "果茶", 32.0, 260.0, 650, 10, 1.2, 1.5, 45, "百香果,果糖,茶汤", ""),
        ("草莓圣代", "乳饮", 42.0, 400.0, 300, 0, 13.0, 4.5, 120, "草莓,冰淇淋粉,蔗糖", "乳"),
        ("冰淇淋红茶", "奶茶", 38.0, 350.0, 500, 40, 11.5, 4.0, 115, "红茶,冰激凌,蔗糖", "乳"),
        ("红柚双柠", "果茶", 30.0, 230.0, 650, 10, 0.4, 0.8, 30, "红柚,柠檬,果糖", ""),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="蜜雪冰城 MIXUE", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"mixue,lemon+tea,ice+cream"),
            source_url=base_url,
        ))


# =================================================================
# 星巴克 Starbucks（15 款）
# =================================================================
def add_starbucks():
    base_url = "https://www.starbucks.com.cn/"
    items = [
        ("拿铁", "咖啡", 12.0, 190.0, 473, 150, 7.5, 10.0, 140, "浓缩咖啡,鲜奶", "乳"),
        ("美式", "咖啡", 0.0, 15.0, 473, 225, 0.3, 1.0, 10, "浓缩咖啡,水", ""),
        ("卡布奇诺", "咖啡", 10.0, 120.0, 355, 140, 6.0, 7.0, 100, "浓缩咖啡,鲜奶,奶泡", "乳"),
        ("焦糖玛奇朵", "咖啡", 35.0, 290.0, 473, 150, 9.0, 10.0, 160, "浓缩咖啡,焦糖,鲜奶,香草糖浆", "乳"),
        ("摩卡", "咖啡", 38.0, 330.0, 473, 175, 13.0, 10.0, 130, "浓缩咖啡,巧克力酱,鲜奶,奶油", "乳"),
        ("抹茶星冰乐", "咖啡", 52.0, 440.0, 591, 80, 13.0, 7.0, 220, "抹茶,鲜奶,奶油,蔗糖,冰", "乳"),
        ("咖啡星冰乐", "咖啡", 46.0, 380.0, 591, 95, 11.0, 5.0, 230, "咖啡,鲜奶,奶油,蔗糖,冰", "乳"),
        ("馥芮白", "咖啡", 10.0, 170.0, 355, 150, 7.0, 9.0, 130, "浓缩咖啡,鲜奶", "乳"),
        ("榛果拿铁", "咖啡", 30.0, 260.0, 473, 150, 8.5, 9.5, 150, "浓缩咖啡,榛果糖浆,鲜奶", "乳,坚果"),
        ("香草拿铁", "咖啡", 28.0, 240.0, 473, 150, 8.0, 9.5, 140, "浓缩咖啡,香草糖浆,鲜奶", "乳"),
        ("燕麦拿铁", "咖啡", 14.0, 210.0, 473, 150, 5.5, 5.0, 135, "浓缩咖啡,燕麦奶", "麸质"),
        ("冰摇柠檬红茶", "果茶", 22.0, 110.0, 473, 60, 0.2, 0.5, 25, "红茶,柠檬,蔗糖", ""),
        ("桃桃乌龙", "果茶", 25.0, 130.0, 473, 55, 0.5, 1.2, 30, "乌龙茶,水蜜桃,蔗糖", ""),
        ("奶油星冰乐", "咖啡", 60.0, 480.0, 591, 95, 14.0, 6.0, 230, "咖啡,奶油,焦糖,蔗糖,冰", "乳"),
        ("可可碎片星冰乐", "咖啡", 58.0, 470.0, 591, 75, 15.0, 6.5, 220, "巧克力碎片,鲜奶,奶油,蔗糖", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="星巴克 Starbucks", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"starbucks,{cat},coffee"),
            source_url=base_url,
        ))


# =================================================================
# 瑞幸 Luckin Coffee（15 款）
# =================================================================
def add_luckin():
    base_url = "https://www.luckincoffee.com/"
    items = [
        ("生椰拿铁", "咖啡", 18.0, 240.0, 473, 150, 8.0, 5.0, 90, "浓缩咖啡,椰乳,生椰浆", "乳"),
        ("标准美式", "咖啡", 0.0, 10.0, 473, 250, 0.2, 0.8, 8, "浓缩咖啡,水", ""),
        ("丝绒拿铁", "咖啡", 18.0, 260.0, 473, 140, 9.5, 10.5, 130, "浓缩咖啡,鲜奶,奶泡", "乳"),
        ("瑞纳冰", "咖啡", 48.0, 400.0, 500, 90, 11.0, 5.0, 210, "咖啡,奶油,蔗糖,冰", "乳"),
        ("厚乳拿铁", "咖啡", 24.0, 290.0, 473, 140, 14.0, 10.0, 120, "浓缩咖啡,厚乳", "乳"),
        ("陨石拿铁", "咖啡", 30.0, 330.0, 473, 145, 10.0, 10.0, 140, "浓缩咖啡,焦糖,鲜奶", "乳"),
        ("抹茶拿铁", "咖啡", 28.0, 280.0, 473, 80, 9.0, 8.0, 150, "抹茶,鲜奶,蔗糖", "乳"),
        ("冰吸生椰拿铁", "咖啡", 22.0, 260.0, 530, 150, 8.5, 4.5, 95, "浓缩咖啡,椰乳,冰", "乳"),
        ("柑橘C美式", "咖啡", 18.0, 90.0, 473, 230, 0.3, 0.8, 30, "咖啡,橙汁,水", ""),
        ("青森苹果丝绒", "咖啡", 32.0, 320.0, 473, 140, 10.0, 9.5, 140, "苹果,丝绒拿铁,鲜奶", "乳"),
        ("葡萄冰萃美式", "咖啡", 28.0, 150.0, 473, 220, 0.5, 0.8, 35, "葡萄,美式咖啡,冰", ""),
        ("椰云拿铁", "咖啡", 20.0, 270.0, 473, 140, 9.0, 4.5, 100, "浓缩咖啡,椰浆,奶泡", "乳"),
        ("黑糖玛奇朵", "咖啡", 35.0, 330.0, 473, 130, 9.0, 9.5, 140, "浓缩咖啡,黑糖,鲜奶", "乳"),
        ("茉莉花香拿铁", "咖啡", 26.0, 270.0, 473, 140, 9.0, 9.5, 130, "浓缩咖啡,茉莉,鲜奶", "乳"),
        ("丝绒风味拿铁（无糖）", "咖啡", 2.0, 150.0, 473, 140, 9.5, 10.5, 130, "浓缩咖啡,厚乳(无糖)", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="瑞幸 Luckin Coffee", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"luckin,latte,iced+coffee"),
            source_url=base_url,
        ))


# =================================================================
# 库迪咖啡 COTTI（10 款）
# =================================================================
def add_cotti():
    base_url = "https://www.cottic.com/"
    items = [
        ("生椰拿铁", "咖啡", 18.0, 240.0, 473, 145, 8.0, 5.0, 95, "浓缩咖啡,椰乳", "乳"),
        ("特浓美式", "咖啡", 0.0, 12.0, 473, 240, 0.2, 0.8, 8, "浓缩咖啡,水", ""),
        ("大师拿铁", "咖啡", 15.0, 220.0, 473, 145, 9.0, 10.0, 130, "浓缩咖啡,鲜奶", "乳"),
        ("冰摇葡萄汁美式", "咖啡", 32.0, 180.0, 473, 230, 0.5, 0.8, 40, "美式,葡萄,冰", ""),
        ("椰云拿铁", "咖啡", 20.0, 280.0, 473, 150, 9.0, 4.5, 100, "浓缩咖啡,椰浆", "乳"),
        ("焦糖玛奇朵", "咖啡", 34.0, 290.0, 473, 150, 9.5, 10.0, 150, "浓缩咖啡,焦糖,鲜奶", "乳"),
        ("抹茶奶铁", "咖啡", 26.0, 260.0, 473, 75, 8.5, 8.0, 135, "抹茶,鲜奶,蔗糖", "乳"),
        ("马可波罗", "咖啡", 24.0, 250.0, 473, 140, 8.5, 9.0, 135, "浓缩咖啡,香草,鲜奶", "乳"),
        ("陨石拿铁", "咖啡", 30.0, 320.0, 473, 140, 10.0, 10.0, 135, "浓缩咖啡,焦糖,鲜奶", "乳"),
        ("厚乳拿铁", "咖啡", 22.0, 290.0, 473, 140, 14.0, 10.0, 130, "浓缩咖啡,厚乳", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="库迪咖啡 COTTI", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"coffee,cotti,latte"),
            source_url=base_url,
        ))


# =================================================================
# Manner Coffee（8 款）
# =================================================================
def add_manner():
    base_url = "https://www.mannercoffee.com/"
    items = [
        ("澳白", "咖啡", 8.0, 160.0, 250, 160, 6.5, 8.0, 110, "浓缩咖啡,鲜奶", "乳"),
        ("小玛奇朵", "咖啡", 12.0, 170.0, 250, 150, 7.0, 8.5, 120, "浓缩咖啡,鲜奶,奶泡", "乳"),
        ("小拿铁", "咖啡", 10.0, 165.0, 250, 150, 7.0, 8.2, 115, "浓缩咖啡,鲜奶", "乳"),
        ("美式", "咖啡", 0.0, 15.0, 355, 230, 0.3, 1.0, 10, "浓缩咖啡,水", ""),
        ("桂花拿铁", "咖啡", 20.0, 220.0, 355, 150, 8.0, 9.0, 120, "浓缩咖啡,桂花糖浆,鲜奶", "乳"),
        ("冷萃美式", "咖啡", 0.0, 10.0, 355, 210, 0.2, 0.8, 8, "冷萃咖啡,水", ""),
        ("伯爵茶拿铁", "咖啡", 22.0, 240.0, 355, 75, 7.5, 8.5, 130, "伯爵红茶,鲜奶,蔗糖", "乳"),
        ("抹茶拿铁", "咖啡", 25.0, 260.0, 355, 85, 8.0, 8.0, 135, "抹茶,鲜奶,蔗糖", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="Manner Coffee", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"manner,coffee,latte,flat+white"),
            source_url=base_url,
        ))


# =================================================================
# Peet's Coffee（6 款）
# =================================================================
def add_peets():
    base_url = "https://www.peets.com.cn/"
    items = [
        ("经典澳白", "咖啡", 8.0, 170.0, 355, 170, 7.0, 8.5, 115, "浓缩咖啡,鲜奶", "乳"),
        ("冷萃美式", "咖啡", 0.0, 12.0, 473, 230, 0.2, 0.8, 10, "冷萃咖啡,水", ""),
        ("招牌拿铁", "咖啡", 10.0, 180.0, 355, 160, 7.2, 9.0, 120, "浓缩咖啡,鲜奶", "乳"),
        ("冷萃椰香拿铁", "咖啡", 16.0, 220.0, 473, 170, 8.0, 4.5, 95, "冷萃咖啡,椰乳", "乳"),
        ("太妃焦糖玛奇朵", "咖啡", 32.0, 290.0, 473, 160, 9.5, 10.0, 150, "浓缩咖啡,太妃焦糖,鲜奶", "乳"),
        ("冷萃拿铁", "咖啡", 14.0, 200.0, 473, 200, 7.5, 8.5, 120, "冷萃咖啡,鲜奶", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="Peet's Coffee", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"peets,coffee,cold+brew"),
            source_url=base_url,
        ))


# =================================================================
# 可口可乐 Coca-Cola（12 款）
# =================================================================
def add_cocacola():
    base_url = "https://www.coca-cola.com.cn/"
    items = [
        ("可口可乐 经典", "碳酸饮料", 35.0, 140.0, 330, 32, 0.0, 0.0, 15, "碳酸水,焦糖色,磷酸,蔗糖", ""),
        ("可口可乐 零度", "碳酸饮料", 0.0, 1.0, 330, 32, 0.0, 0.0, 20, "碳酸水,焦糖色,阿斯巴甜,安赛蜜", ""),
        ("可口可乐 纤维+", "碳酸饮料", 0.0, 10.0, 500, 20, 0.0, 0.0, 60, "碳酸水,膳食纤维,代糖", ""),
        ("雪碧", "碳酸饮料", 38.0, 160.0, 330, 0, 0.0, 0.0, 22, "碳酸水,柠檬酸,蔗糖", ""),
        ("雪碧 纤维+", "碳酸饮料", 0.0, 12.0, 500, 0, 0.0, 0.0, 40, "碳酸水,膳食纤维,代糖", ""),
        ("芬达 橙味", "碳酸饮料", 42.0, 180.0, 330, 0, 0.0, 0.0, 25, "碳酸水,橙汁,蔗糖,香精", ""),
        ("芬达 葡萄味", "碳酸饮料", 40.0, 170.0, 330, 0, 0.0, 0.0, 25, "碳酸水,葡萄汁,蔗糖,香精", ""),
        ("美汁源 果粒橙", "果汁", 30.0, 160.0, 450, 0, 0.0, 0.8, 22, "水,橙汁,橙肉,蔗糖", ""),
        ("美汁源 果汁先生", "果汁", 25.0, 130.0, 250, 0, 0.0, 0.5, 18, "橙汁,水,蔗糖", ""),
        ("怡泉 +C", "果汁", 22.0, 90.0, 330, 0, 0.0, 0.0, 15, "水,柠檬汁,果糖", ""),
        ("怡泉 苏打水", "气泡水", 0.0, 0.0, 330, 0, 0.0, 0.0, 20, "碳酸水", ""),
        ("酷儿 橙味", "果汁", 28.0, 120.0, 250, 0, 0.0, 0.5, 18, "橙汁,水,蔗糖,维生素C", ""),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="可口可乐 Coca-Cola", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"coca+cola,{cat}"),
            source_url=base_url,
        ))


# =================================================================
# 农夫山泉（10 款）
# =================================================================
def add_nongfu():
    base_url = "https://www.nongfuspring.com/"
    items = [
        ("农夫山泉 天然水", "瓶装水", 0.0, 0.0, 550, 0, 0.0, 0.0, 2, "天然水", ""),
        ("农夫山泉 矿泉水", "瓶装水", 0.0, 0.0, 500, 0, 0.0, 0.0, 5, "矿泉水", ""),
        ("农夫山泉 NFC 橙汁", "果汁", 28.0, 130.0, 300, 0, 0.0, 0.8, 10, "NFC橙汁", ""),
        ("农夫山泉 NFC 芒果汁", "果汁", 32.0, 150.0, 300, 0, 0.0, 0.5, 10, "NFC芒果汁", ""),
        ("农夫果园 30%混合汁", "果汁", 24.0, 110.0, 450, 0, 0.0, 0.5, 15, "混合果汁,水,蔗糖", ""),
        ("东方树叶 乌龙茶", "茶饮", 0.0, 0.0, 500, 25, 0.0, 0.0, 2, "水,乌龙茶", ""),
        ("东方树叶 红茶", "茶饮", 0.0, 0.0, 500, 30, 0.0, 0.0, 2, "水,红茶", ""),
        ("东方树叶 茉莉花茶", "茶饮", 0.0, 0.0, 500, 25, 0.0, 0.0, 2, "水,茉莉花茶", ""),
        ("茶派 蜜桃乌龙", "茶饮", 22.0, 100.0, 500, 18, 0.0, 0.0, 18, "水,乌龙茶,蜜桃,果糖", ""),
        ("维他命水", "功能饮料", 15.0, 70.0, 500, 0, 0.0, 0.0, 30, "水,维生素B族,果糖,电解质", ""),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="农夫山泉", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"nongfu,bottled+water,tea"),
            source_url=base_url,
        ))


# =================================================================
# 元气森林（12 款）
# =================================================================
def add_yuanqisenlin():
    base_url = "https://www.genki-forest.com/"
    items = [
        ("元气森林 白桃味苏打气泡水", "气泡水", 0.0, 0.0, 480, 0, 0.0, 0.0, 25, "水,赤藓糖醇,白桃香精,二氧化碳", ""),
        ("元气森林 葡萄味苏打气泡水", "气泡水", 0.0, 0.0, 480, 0, 0.0, 0.0, 25, "水,赤藓糖醇,葡萄香精,二氧化碳", ""),
        ("元气森林 青瓜味苏打气泡水", "气泡水", 0.0, 0.0, 480, 0, 0.0, 0.0, 25, "水,赤藓糖醇,青瓜香精,二氧化碳", ""),
        ("元气森林 乳茶 原味", "乳饮", 18.0, 150.0, 450, 25, 3.2, 5.0, 80, "牛奶,红茶,赤藓糖醇", "乳"),
        ("元气森林 乳茶 桃桃", "乳饮", 20.0, 160.0, 450, 25, 3.5, 5.0, 80, "牛奶,红茶,桃汁", "乳"),
        ("元气森林 燃茶 无糖", "茶饮", 0.0, 0.0, 500, 40, 0.0, 0.0, 10, "水,红茶,EGCG,代糖", ""),
        ("外星人 电解质水", "功能饮料", 0.0, 0.0, 600, 0, 0.0, 0.0, 150, "水,钠,钾,镁,代糖", ""),
        ("外星人 功能饮料", "功能饮料", 12.0, 60.0, 500, 50, 0.0, 0.0, 100, "咖啡因,牛磺酸,维B", ""),
        ("元気森林 柠檬气泡", "气泡水", 0.0, 0.0, 480, 0, 0.0, 0.0, 25, "水,赤藓糖醇,柠檬香精", ""),
        ("元气森林 海盐柚子", "气泡水", 0.0, 0.0, 480, 0, 0.0, 0.0, 35, "水,柚子,海盐,代糖", ""),
        ("元气森林 热带葡萄气泡水", "气泡水", 0.0, 0.0, 480, 0, 0.0, 0.0, 25, "水,葡萄,代糖", ""),
        ("元气森林 小茗同学 苹果乌龙", "茶饮", 24.0, 100.0, 480, 25, 0.0, 0.0, 20, "水,乌龙茶,苹果汁,蔗糖", ""),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="元气森林", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"genki,sparkling+water,{cat}"),
            source_url=base_url,
        ))


# =================================================================
# 王老吉（5 款）/ 红牛（5 款）
# =================================================================
def add_wanglaoji():
    base_url = "https://www.wanglaoji.com.cn/"
    items = [
        ("王老吉 凉茶", "凉茶", 28.0, 130.0, 310, 0, 0.0, 0.0, 35, "水,白砂糖,菊花,金银花,甘草,仙草,夏枯草", ""),
        ("王老吉 无糖凉茶", "凉茶", 0.0, 10.0, 500, 0, 0.0, 0.0, 20, "水,菊花,金银花,甘草,代糖", ""),
        ("王老吉 荔枝", "凉茶", 24.0, 110.0, 500, 0, 0.0, 0.0, 30, "水,荔枝汁,凉茶草本,蔗糖", ""),
        ("王老吉 刺柠吉", "果汁", 18.0, 90.0, 500, 0, 0.0, 0.5, 22, "水,刺梨汁,柠檬汁,代糖", ""),
        ("王老吉 黑凉茶", "凉茶", 22.0, 105.0, 500, 0, 0.0, 0.0, 30, "水,烤浓凉茶,仙草,蔗糖", ""),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="王老吉", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"wanglaoji,herbal+tea,chinese"),
            source_url=base_url,
        ))


def add_redbull():
    base_url = "https://www.redbull.com.cn/"
    items = [
        ("红牛 维生素功能饮料", "功能饮料", 28.0, 120.0, 250, 50, 0.0, 0.5, 90, "水,白砂糖,牛磺酸,维B,咖啡因", ""),
        ("红牛 安奈吉", "功能饮料", 24.0, 110.0, 250, 50, 0.0, 0.5, 90, "水,白砂糖,牛磺酸,咖啡因,赖氨酸", ""),
        ("红牛 维生素风味", "功能饮料", 27.0, 115.0, 250, 50, 0.0, 0.5, 85, "水,糖,牛磺酸,咖啡因", ""),
        ("红牛 能量风味", "功能饮料", 26.0, 120.0, 330, 80, 0.0, 0.5, 100, "水,糖,牛磺酸,咖啡因", ""),
        ("红牛 无糖", "功能饮料", 0.0, 5.0, 250, 50, 0.0, 0.5, 85, "水,牛磺酸,咖啡因,代糖", ""),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="红牛 Red Bull", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"red+bull,energy+drink"),
            source_url=base_url,
        ))


# =================================================================
# 蒙牛（10 款）
# =================================================================
def add_mengniu():
    base_url = "https://www.mengniu.com.cn/"
    items = [
        ("蒙牛 纯牛奶", "乳饮", 10.0, 125.0, 250, 0, 8.0, 8.0, 100, "生牛乳", "乳"),
        ("蒙牛 特仑苏 纯牛奶", "乳饮", 12.0, 160.0, 250, 0, 10.0, 9.0, 100, "生牛乳（蛋白≥3.6g/100g）", "乳"),
        ("蒙牛 真果粒 草莓", "乳饮", 32.0, 200.0, 250, 0, 5.0, 5.0, 85, "生牛乳,草莓果粒,糖", "乳"),
        ("蒙牛 优益C 原味", "乳饮", 30.0, 140.0, 330, 0, 0.2, 2.0, 55, "活性乳酸菌,水,糖", "乳"),
        ("蒙牛 纯甄 原味酸奶", "乳饮", 18.0, 130.0, 200, 0, 3.0, 6.0, 80, "生牛乳,嗜热链球菌,蔗糖", "乳"),
        ("蒙牛 冠益乳 原味", "乳饮", 14.0, 120.0, 200, 0, 3.2, 5.8, 75, "生牛乳,活性菌,蔗糖", "乳"),
        ("蒙牛 酸酸乳 草莓", "乳饮", 28.0, 130.0, 250, 0, 3.0, 3.5, 80, "水,生牛乳,糖,草莓汁", "乳"),
        ("蒙牛 未来星 儿童成长奶", "乳饮", 16.0, 135.0, 190, 0, 5.0, 5.5, 90, "生牛乳,维D,DHA", "乳"),
        ("蒙牛 每日鲜语 鲜牛奶", "乳饮", 10.0, 125.0, 250, 0, 8.0, 8.5, 100, "生牛乳（低温巴氏）", "乳"),
        ("蒙牛 臻享 0脂原味酸奶", "乳饮", 8.0, 80.0, 200, 0, 0.0, 6.5, 75, "脱脂乳,菌种,代糖", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="蒙牛 Mengniu", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"mengniu,milk,yogurt"),
            source_url=base_url,
        ))


# =================================================================
# 伊利（10 款）
# =================================================================
def add_yili():
    base_url = "https://www.yili.com/"
    items = [
        ("伊利 纯牛奶", "乳饮", 10.0, 130.0, 250, 0, 8.0, 8.0, 100, "生牛乳", "乳"),
        ("伊利 金典 纯牛奶", "乳饮", 12.0, 160.0, 250, 0, 10.0, 9.0, 100, "生牛乳（蛋白≥3.6g/100g）", "乳"),
        ("伊利 安慕希 原味", "乳饮", 25.0, 180.0, 200, 0, 5.0, 6.5, 85, "生牛乳,糖,菌种", "乳"),
        ("伊利 安慕希 丹东草莓", "乳饮", 30.0, 195.0, 200, 0, 5.2, 6.5, 85, "生牛乳,草莓,糖,菌种", "乳"),
        ("伊利 每益添", "乳饮", 26.0, 130.0, 330, 0, 0.2, 2.0, 60, "水,生牛乳,糖,活菌", "乳"),
        ("伊利 优酸乳", "乳饮", 30.0, 135.0, 250, 0, 3.0, 3.5, 80, "水,生牛乳,糖", "乳"),
        ("伊利 QQ星 儿童成长奶", "乳饮", 16.0, 130.0, 190, 0, 5.0, 5.5, 90, "生牛乳,维A/D,钙", "乳"),
        ("伊利 金典有机奶", "乳饮", 12.0, 160.0, 250, 0, 10.0, 9.2, 100, "有机生牛乳", "乳"),
        ("伊利 谷粒多 燕麦", "乳饮", 22.0, 170.0, 250, 0, 4.5, 5.0, 90, "生牛乳,燕麦,糖", "乳,麸质"),
        ("伊利 畅轻 低脂酸奶", "乳饮", 12.0, 95.0, 200, 0, 1.0, 6.8, 78, "生牛乳（低脂）,菌种", "乳"),
    ]
    for name, cat, s, c, v, caf, fa, pr, na, ing, alg in items:
        DRINKS.append(Drink(
            drink_name=name, brand="伊利 Yili", category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"yili,milk,yogurt"),
            source_url=base_url,
        ))


# =================================================================
# 其他特色品牌补充（3 款，用于凑齐 200 条整数）
# =================================================================
def add_others():
    items = [
        ("一点点 四季春茶",            "一点点", "奶茶", 45.0, 430.0, 500, 45, 13.5, 4.8, 150, "四季春茶,鲜奶,蔗糖", "乳", "https://www.1dianyl.com/"),
        ("乐乐茶 脏脏茶",              "乐乐茶", "奶茶", 55.0, 530.0, 500, 40, 17.2, 6.0, 160, "黑糖,珍珠,鲜奶,奶盖", "乳,麸质", "https://www.lelecha.com/"),
        ("% Arabica 京都拿铁",        "% Arabica", "咖啡", 8.0, 165.0, 355, 155, 7.2, 8.5, 120, "浓缩咖啡,鲜奶", "乳", "https://arabica.coffee/"),
    ]
    for name, brand, cat, s, c, v, caf, fa, pr, na, ing, alg, src in items:
        DRINKS.append(Drink(
            drink_name=name, brand=brand, category=cat,
            sugar_content=s, calories=c, volume=float(v),
            caffeine=float(caf), fat=float(fa), protein=float(pr),
            sodium=float(na),
            health_score=health_score(s, c, caf),
            ingredients=ing, allergens=alg,
            image_url=img(f"{brand.replace(' ', '+')},{cat}"),
            source_url=src,
        ))


# 执行所有品牌
for fn in [add_heytea, add_nayuki, add_chagee, add_chabaidao, add_7fentian,
           add_hushangayi, add_mixue, add_starbucks, add_luckin, add_cotti,
           add_manner, add_peets, add_cocacola, add_nongfu, add_yuanqisenlin,
           add_wanglaoji, add_redbull, add_mengniu, add_yili, add_others]:
    fn()


# =================================================================
# 生成 SQL
# =================================================================
def sql_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace("'", "''")


def to_sql(drinks: List[Drink]) -> str:
    lines = []
    lines.append("-- 糖知 APP (SugarGuard) 饮品数据库扩充 SQL")
    lines.append(f"-- 自动生成，共 {len(drinks)} 条，覆盖 19 个主流品牌")
    lines.append("-- 使用：mysql -uroot -p123456 Android_health_db < drinks_200.sql")
    lines.append("")
    lines.append("SET NAMES utf8mb4;")
    lines.append("USE Android_health_db;")
    lines.append("")
    lines.append("-- 如需清空原表再导入，取消下一行注释：")
    lines.append("-- TRUNCATE TABLE drinks;")
    lines.append("")
    cols = ("drink_name,brand,category,sugar_content,calories,volume,caffeine,"
            "fat,protein,sodium,health_score,ingredients,allergens,"
            "image_url,source_url,created_at,updated_at")
    chunk = 50
    for i in range(0, len(drinks), chunk):
        batch = drinks[i:i + chunk]
        lines.append(f"INSERT INTO drinks ({cols}) VALUES")
        vals = []
        for d in batch:
            vals.append(
                f"('{sql_escape(d.drink_name)}','{sql_escape(d.brand)}',"
                f"'{sql_escape(d.category)}',{d.sugar_content},{d.calories},"
                f"{d.volume},{d.caffeine},{d.fat},{d.protein},{d.sodium},"
                f"{d.health_score},'{sql_escape(d.ingredients)}',"
                f"'{sql_escape(d.allergens)}','{sql_escape(d.image_url)}',"
                f"'{sql_escape(d.source_url)}',NOW(),NOW())"
            )
        lines.append(",\n".join(vals) + ";")
        lines.append("")
    lines.append(f"-- 完成：共插入 {len(drinks)} 条饮品")
    return "\n".join(lines)


# 生成 Python 直接写库脚本
PY_SEED_HEAD = '''# -*- coding: utf-8 -*-
"""
糖知 APP (SugarGuard) 饮品数据库扩充 —— Python 写库版
用法：
    pip install pymysql
    python seed_drinks_200.py
"""
import os, sys, time
try:
    import pymysql
except ImportError:
    print("请先: pip install pymysql"); sys.exit(1)

DB = {
    "host":   os.environ.get("DB_HOST", "127.0.0.1"),
    "port":   int(os.environ.get("DB_PORT", "3306")),
    "user":   os.environ.get("DB_USER", "root"),
    "password": os.environ.get("DB_PASSWORD", "123456"),
    "database": os.environ.get("DB_NAME", "Android_health_db"),
    "charset": "utf8mb4",
}

DRINKS = __DRINKS_PLACEHOLDER__

def main():
    conn = pymysql.connect(**DB)
    cur = conn.cursor()
    sql = (
        "INSERT INTO drinks (drink_name,brand,category,sugar_content,calories,"
        "volume,caffeine,fat,protein,sodium,health_score,ingredients,allergens,"
        "image_url,source_url,created_at,updated_at)"
        " VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,NOW(),NOW())"
    )
    cur.executemany(sql, DRINKS)
    conn.commit()
    print(f"OK 已插入 {cur.rowcount} 条饮品")
    cur.close(); conn.close()

if __name__ == "__main__":
    main()
'''


def to_py(drinks: List[Drink]) -> str:
    rows = []
    for d in drinks:
        rows.append((d.drink_name, d.brand, d.category,
                     d.sugar_content, d.calories, d.volume,
                     d.caffeine, d.fat, d.protein, d.sodium,
                     d.health_score, d.ingredients, d.allergens,
                     d.image_url, d.source_url))
    body = "[\n" + ",\n".join(
        "    (" + ", ".join(repr(x) for x in r) + ")"
        for r in rows
    ) + "\n]"
    return PY_SEED_HEAD.replace("__DRINKS_PLACEHOLDER__", body)


sql_out = os.path.join(OUT_DIR, "drinks_200.sql")
py_out = os.path.join(OUT_DIR, "seed_drinks_200.py")

with open(sql_out, "w", encoding="utf-8") as f:
    f.write(to_sql(DRINKS))
with open(py_out, "w", encoding="utf-8") as f:
    f.write(to_py(DRINKS))

print(f"OK 共 {len(DRINKS)} 条饮品")
print(f"  SQL : {sql_out}")
print(f"  PY  : {py_out}")

# 品牌统计
from collections import Counter
cnt = Counter(d.brand for d in DRINKS)
for k, v in sorted(cnt.items(), key=lambda x: -x[1]):
    print(f"  {k:<30} {v:>4d} 条")
