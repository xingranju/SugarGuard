package com.example.myapplication.db

import com.example.myapplication.db.entity.DrinkEntity
import com.example.myapplication.db.entity.FoodEntity

object FoodDatabaseGenerator {

    private val imgBase = "https://images.unsplash.com/photo-"

    private val drinkImages = mapOf(
        "奶茶" to "${imgBase}1558857563-b371033873b8?w=400&h=400&fit=crop",
        "咖啡" to "${imgBase}1509042239860-f550ce710b93?w=400&h=400&fit=crop",
        "果汁" to "${imgBase}1621506289937-a8e4df240d0b?w=400&h=400&fit=crop",
        "碳酸" to "${imgBase}1554866585-cd94860890b7?w=400&h=400&fit=crop",
        "茶饮" to "${imgBase}1564890369478-c89ca6d9cde9?w=400&h=400&fit=crop",
        "乳饮" to "${imgBase}1563636619-e9143da7973b?w=400&h=400&fit=crop",
        "功能" to "${imgBase}1527960471264-932f39eb5846?w=400&h=400&fit=crop",
        "气泡" to "${imgBase}1622483767028-3f66f32aef97?w=400&h=400&fit=crop",
        "酒类" to "${imgBase}1470337458703-46ad1756a187?w=400&h=400&fit=crop",
        "豆浆" to "${imgBase}1555939594-58d7cb561ad1?w=400&h=400&fit=crop"
    )

    private val foodImages = mapOf(
        "主食" to "${imgBase}1516684732162-798a0062be99?w=400&h=300&fit=crop",
        "水果" to "${imgBase}1560806887-1e4cd0b6cbd6?w=400&h=300&fit=crop",
        "蔬菜" to "${imgBase}1459411552884-841db9b3cc2a?w=400&h=300&fit=crop",
        "蛋白质" to "${imgBase}1604503468506-a8da13d82571?w=400&h=300&fit=crop",
        "甜品" to "${imgBase}1578985545062-69928b1d9587?w=400&h=300&fit=crop",
        "快餐" to "${imgBase}1568901346375-23c9450c58cd?w=400&h=300&fit=crop",
        "小吃" to "${imgBase}1565299624946-b28f40a0ae38?w=400&h=300&fit=crop",
        "面食" to "${imgBase}1569718212165-3a8278d5f624?w=400&h=300&fit=crop",
        "海鲜" to "${imgBase}1519708227418-c8fd9a32b7a2?w=400&h=300&fit=crop",
        "坚果" to "${imgBase}1563412885-51bb84deaf70?w=400&h=300&fit=crop",
        "沙拉" to "${imgBase}1512621776951-a57141f2eefd?w=400&h=300&fit=crop",
        "烧烤" to "${imgBase}1529006557810-274b9b2fc783?w=400&h=300&fit=crop",
        "火锅" to "${imgBase}1504674900247-0877df9cc836?w=400&h=300&fit=crop",
        "粥品" to "${imgBase}1517673400267-0251440c45dc?w=400&h=300&fit=crop",
        "早餐" to "${imgBase}1482049016688-2d3e1b311543?w=400&h=300&fit=crop",
        "寿司" to "${imgBase}1553621042-f6e147245754?w=400&h=300&fit=crop",
        "汤品" to "${imgBase}1547592166-23ac45744acd?w=400&h=300&fit=crop",
        "炒菜" to "${imgBase}1512058564366-18510be2db19?w=400&h=300&fit=crop"
    )

    fun generateAllDrinks(): List<DrinkEntity> {
        val drinks = mutableListOf<DrinkEntity>()
        var id = 100

        val milkTeaBrands = listOf("喜茶", "奈雪", "一点点", "CoCo", "茶百道", "沪上阿姨", "书亦烧仙草", "古茗", "蜜雪冰城", "益禾堂", "鹿角巷", "七分甜", "乐乐茶", "茶颜悦色", "霸王茶姬")
        val milkTeaFlavors = listOf(
            "珍珠奶茶" to Triple(38f, 420f, 30), "芋泥波波" to Triple(42f, 480f, 25), "杨枝甘露" to Triple(35f, 350f, 40),
            "多肉葡萄" to Triple(30f, 310f, 45), "烤奶" to Triple(28f, 360f, 35), "芝士乌龙" to Triple(25f, 300f, 45),
            "红糖鹿丸" to Triple(40f, 430f, 28), "抹茶拿铁" to Triple(22f, 280f, 50), "椰椰芒果" to Triple(32f, 340f, 42),
            "草莓芝士" to Triple(33f, 350f, 38), "桃桃乌龙" to Triple(28f, 290f, 48), "满杯红柚" to Triple(26f, 270f, 52),
            "黑糖珍珠" to Triple(45f, 500f, 20), "柠檬绿茶" to Triple(20f, 220f, 55), "芒芒甘露" to Triple(34f, 360f, 40),
            "仙草奶茶" to Triple(30f, 350f, 38), "蜜桃四季春" to Triple(25f, 260f, 50), "冰淇淋奶茶" to Triple(40f, 450f, 25),
            "鲜芋仙" to Triple(36f, 400f, 32), "四季春茶" to Triple(18f, 200f, 58)
        )
        val sugarLevels = listOf("全糖" to 1.0f, "七分糖" to 0.7f, "五分糖" to 0.5f, "三分糖" to 0.3f, "无糖" to 0.05f)

        for (brand in milkTeaBrands) {
            for ((flavor, nutrition) in milkTeaFlavors) {
                for ((level, mult) in sugarLevels) {
                    drinks.add(DrinkEntity(
                        drinkId = id++, drinkName = "$flavor($level)", brand = brand, category = "奶茶",
                        sugarContent = (nutrition.first * mult), calories = nutrition.second * (0.6f + mult * 0.4f),
                        volume = 500f, caffeine = 30f, healthScore = (nutrition.third / mult).toInt().coerceIn(10, 95),
                        imageUrl = drinkImages["奶茶"], createdAt = "2026-04-01"
                    ))
                }
            }
        }

        val coffeeBrands = listOf("星巴克", "瑞幸", "Manner", "Seesaw", "Peet's", "Costa", "太平洋", "上岛咖啡", "Tim Hortons", "McCafé")
        val coffeeTypes = listOf(
            "美式" to Triple(0f, 15f, 80), "拿铁" to Triple(12f, 190f, 55), "卡布奇诺" to Triple(10f, 150f, 60),
            "焦糖玛奇朵" to Triple(33f, 250f, 35), "摩卡" to Triple(30f, 290f, 35), "冰美式" to Triple(0f, 10f, 85),
            "生椰拿铁" to Triple(8f, 180f, 55), "燕麦拿铁" to Triple(15f, 210f, 50), "香草拿铁" to Triple(20f, 230f, 45),
            "浓缩咖啡" to Triple(0f, 5f, 90), "冷萃" to Triple(0f, 10f, 85), "Dirty" to Triple(5f, 120f, 70),
            "澳白" to Triple(8f, 140f, 65), "手冲单品" to Triple(0f, 5f, 92)
        )
        for (brand in coffeeBrands) {
            for ((type, nutrition) in coffeeTypes) {
                drinks.add(DrinkEntity(
                    drinkId = id++, drinkName = type, brand = brand, category = "咖啡",
                    sugarContent = nutrition.first, calories = nutrition.second, volume = 350f,
                    caffeine = 150f, healthScore = nutrition.third,
                    imageUrl = drinkImages["咖啡"], createdAt = "2026-04-01"
                ))
            }
        }

        val sodas = listOf(
            "可口可乐" to 35f, "百事可乐" to 36f, "雪碧" to 33f, "芬达橙味" to 34f, "芬达葡萄" to 36f,
            "七喜" to 30f, "美年达" to 32f, "冰锐" to 28f, "脉动" to 20f, "维他柠檬茶" to 25f,
            "维他奶" to 12f, "椰树椰汁" to 18f, "王老吉" to 28f, "加多宝" to 28f, "健力宝" to 22f,
            "零度可乐" to 0f, "无糖雪碧" to 0f, "无糖百事" to 0f, "元气森林" to 0f, "东方树叶" to 0f
        )
        for ((name, sugar) in sodas) {
            drinks.add(DrinkEntity(
                drinkId = id++, drinkName = name, brand = "", category = "碳酸饮料",
                sugarContent = sugar, calories = sugar * 4f, volume = 330f,
                healthScore = if (sugar == 0f) 70 else (60 - sugar).toInt().coerceIn(10, 60),
                imageUrl = drinkImages["碳酸"], createdAt = "2026-04-01"
            ))
        }

        val juices = listOf(
            "鲜榨橙汁" to 21f, "西瓜汁" to 15f, "苹果汁" to 24f, "葡萄汁" to 28f, "芒果汁" to 25f,
            "草莓汁" to 18f, "蓝莓汁" to 16f, "猕猴桃汁" to 20f, "番茄汁" to 8f, "胡萝卜汁" to 10f,
            "甘蔗汁" to 30f, "柠檬水" to 5f, "百香果汁" to 18f, "椰子水" to 6f, "石榴汁" to 22f,
            "梨汁" to 20f, "桃汁" to 19f, "菠萝汁" to 24f, "荔枝汁" to 26f, "火龙果汁" to 15f
        )
        for ((name, sugar) in juices) {
            drinks.add(DrinkEntity(
                drinkId = id++, drinkName = name, brand = "自制", category = "果汁",
                sugarContent = sugar, calories = sugar * 4.5f, volume = 250f,
                healthScore = (80 - sugar * 0.5f).toInt().coerceIn(40, 85),
                imageUrl = drinkImages["果汁"], createdAt = "2026-04-01"
            ))
        }

        val teas = listOf(
            "绿茶", "红茶", "乌龙茶", "普洱茶", "铁观音", "龙井", "碧螺春", "白茶", "黄茶", "大红袍",
            "茉莉花茶", "菊花茶", "玫瑰花茶", "薄荷茶", "金银花茶", "桂花茶", "枸杞茶", "红枣茶", "柠檬红茶", "蜂蜜柚子茶"
        )
        for (name in teas) {
            val hasSugar = name.contains("蜂蜜") || name.contains("红枣") || name.contains("枸杞")
            drinks.add(DrinkEntity(
                drinkId = id++, drinkName = name, brand = "自泡", category = "茶饮",
                sugarContent = if (hasSugar) 8f else 0f, calories = if (hasSugar) 35f else 2f, volume = 250f,
                healthScore = if (hasSugar) 75 else 95,
                imageUrl = drinkImages["茶饮"], createdAt = "2026-04-01"
            ))
        }

        val milkProducts = listOf(
            "纯牛奶" to 5f, "酸奶" to 15f, "无糖酸奶" to 5f, "脱脂牛奶" to 5f, "豆浆" to 3f,
            "无糖豆浆" to 0f, "杏仁奶" to 4f, "燕麦奶" to 7f, "椰奶" to 6f, "核桃奶" to 8f,
            "花生奶" to 10f, "黑芝麻糊" to 15f, "藕粉" to 12f, "红豆薏米水" to 10f, "银耳莲子羹" to 18f
        )
        for ((name, sugar) in milkProducts) {
            drinks.add(DrinkEntity(
                drinkId = id++, drinkName = name, brand = "", category = "乳饮",
                sugarContent = sugar, calories = sugar * 8f + 50f, volume = 250f,
                healthScore = (85 - sugar).toInt().coerceIn(50, 90),
                imageUrl = drinkImages["乳饮"], createdAt = "2026-04-01"
            ))
        }

        val energyDrinks = listOf(
            "红牛" to 27f, "怪兽" to 28f, "战马" to 25f, "东鹏特饮" to 24f, "乐虎" to 22f,
            "魔爪" to 26f, "佳得乐" to 15f, "宝矿力" to 13f, "尖叫" to 20f, "力保健" to 18f
        )
        for ((name, sugar) in energyDrinks) {
            drinks.add(DrinkEntity(
                drinkId = id++, drinkName = name, brand = "", category = "功能饮料",
                sugarContent = sugar, calories = sugar * 4f + 10f, volume = 250f,
                healthScore = (50 - sugar * 0.5f).toInt().coerceIn(20, 50),
                imageUrl = drinkImages["功能"], createdAt = "2026-04-01"
            ))
        }

        val iceLevels = listOf("去冰", "少冰", "正常冰", "多冰", "温热")
        val extraMilkTeaFlavors = listOf(
            "芝士奶盖" to Triple(28f, 320f, 38), "黑糖波波" to Triple(42f, 460f, 22), "芋圆奶茶" to Triple(35f, 400f, 30),
            "焦糖布丁" to Triple(32f, 380f, 32), "蜜桃乌龙" to Triple(22f, 240f, 55), "鸳鸯奶茶" to Triple(25f, 280f, 42),
            "绿豆沙冰" to Triple(28f, 250f, 45), "柠檬冻茶" to Triple(18f, 200f, 60), "西瓜奶昔" to Triple(24f, 260f, 48),
            "奥利奥奶昔" to Triple(38f, 450f, 22), "摇摇杯" to Triple(20f, 220f, 55), "益力多绿茶" to Triple(16f, 180f, 58)
        )
        for (brand in milkTeaBrands) {
            for ((flavor, nutrition) in extraMilkTeaFlavors) {
                for (ice in iceLevels) {
                    drinks.add(DrinkEntity(
                        drinkId = id++, drinkName = "$flavor·$ice", brand = brand, category = "奶茶",
                        sugarContent = nutrition.first, calories = nutrition.second,
                        volume = 500f, caffeine = 25f, healthScore = nutrition.third,
                        imageUrl = drinkImages["奶茶"], createdAt = "2026-04-01"
                    ))
                }
            }
        }

        val fruitTeaBrands = listOf("喜茶", "奈雪", "乐乐茶", "茶颜悦色", "霸王茶姬", "古茗", "沪上阿姨", "一点点")
        val fruitTeaTypes = listOf(
            "霸气橙子" to 20f, "霸气芝士草莓" to 28f, "霸气杨梅" to 22f, "椰椰芒芒" to 26f,
            "满杯百香果" to 18f, "多肉车厘子" to 30f, "超级杯水果茶" to 25f, "雪顶茉莉" to 15f,
            "芝士茉莉" to 20f, "柠檬养乐多" to 22f, "青提爆柠茶" to 18f, "芒果多多" to 28f,
            "蜜瓜冰沙" to 24f, "火龙果冰沙" to 20f, "牛油果奶昔" to 15f
        )
        for (brand in fruitTeaBrands) {
            for ((tea, sugar) in fruitTeaTypes) {
                for ((level, mult) in sugarLevels) {
                    drinks.add(DrinkEntity(
                        drinkId = id++, drinkName = "$tea($level)", brand = brand, category = "果茶",
                        sugarContent = sugar * mult, calories = sugar * 4f * (0.6f + mult * 0.4f),
                        volume = 500f, healthScore = (75 - sugar * mult).toInt().coerceIn(15, 80),
                        imageUrl = drinkImages["果汁"], createdAt = "2026-04-01"
                    ))
                }
            }
        }

        return drinks
    }

    fun generateAllFoods(): List<FoodEntity> {
        val foods = mutableListOf<FoodEntity>()
        var id = 100

        data class FoodTemplate(val name: String, val cat: String, val sugar: Float, val cal: Float, val protein: Float, val fat: Float, val carb: Float, val gi: Int)

        val templates = listOf(
            FoodTemplate("白米饭", "主食", 0.1f, 116f, 2.6f, 0.3f, 25.9f, 83),
            FoodTemplate("糙米饭", "主食", 0.5f, 111f, 2.6f, 0.9f, 23f, 56),
            FoodTemplate("红薯", "主食", 4.2f, 86f, 1.6f, 0.1f, 20f, 61),
            FoodTemplate("土豆", "主食", 0.8f, 77f, 2f, 0.1f, 17f, 70),
            FoodTemplate("玉米", "主食", 3.2f, 96f, 3.3f, 1.2f, 19f, 55),
            FoodTemplate("馒头", "主食", 1.8f, 223f, 7f, 1.1f, 47f, 88),
            FoodTemplate("面条", "主食", 0.6f, 138f, 4.5f, 0.5f, 28.5f, 49),
            FoodTemplate("米粉", "主食", 0.3f, 113f, 1.5f, 0.3f, 26f, 70),
            FoodTemplate("饺子", "主食", 1.5f, 210f, 9f, 7f, 28f, 55),
            FoodTemplate("包子", "主食", 3f, 220f, 7f, 3f, 40f, 60),
            FoodTemplate("烧饼", "主食", 2f, 260f, 8f, 5f, 46f, 65),
            FoodTemplate("油条", "主食", 0.5f, 386f, 6f, 17f, 51f, 75),
            FoodTemplate("年糕", "主食", 1f, 154f, 3.3f, 0.2f, 35f, 80),
            FoodTemplate("粽子", "主食", 5f, 200f, 4f, 2f, 42f, 72),
            FoodTemplate("煎饼果子", "主食", 2f, 230f, 8f, 10f, 30f, 62),

            FoodTemplate("苹果", "水果", 10.4f, 52f, 0.3f, 0.2f, 13.8f, 36),
            FoodTemplate("香蕉", "水果", 12.2f, 89f, 1.1f, 0.3f, 22.8f, 51),
            FoodTemplate("橙子", "水果", 9.4f, 47f, 0.9f, 0.1f, 11.8f, 42),
            FoodTemplate("葡萄", "水果", 16.3f, 69f, 0.7f, 0.2f, 18.1f, 46),
            FoodTemplate("草莓", "水果", 4.9f, 32f, 0.7f, 0.3f, 7.7f, 25),
            FoodTemplate("蓝莓", "水果", 10f, 57f, 0.7f, 0.3f, 14.5f, 25),
            FoodTemplate("芒果", "水果", 14f, 60f, 0.8f, 0.4f, 15f, 51),
            FoodTemplate("西瓜", "水果", 6.2f, 30f, 0.6f, 0.2f, 7.6f, 72),
            FoodTemplate("桃子", "水果", 8.4f, 39f, 0.9f, 0.3f, 9.5f, 28),
            FoodTemplate("梨", "水果", 9.8f, 57f, 0.4f, 0.1f, 15.2f, 38),
            FoodTemplate("猕猴桃", "水果", 9f, 61f, 1.1f, 0.5f, 14.7f, 39),
            FoodTemplate("樱桃", "水果", 12.8f, 63f, 1.1f, 0.2f, 16f, 22),
            FoodTemplate("荔枝", "水果", 15.2f, 66f, 0.8f, 0.4f, 16.5f, 50),
            FoodTemplate("龙眼", "水果", 16.2f, 60f, 1.2f, 0.1f, 15.4f, 48),
            FoodTemplate("山竹", "水果", 13f, 73f, 0.4f, 0.6f, 18f, 35),
            FoodTemplate("火龙果", "水果", 9.8f, 51f, 1.1f, 0.4f, 11.8f, 56),
            FoodTemplate("柚子", "水果", 8.6f, 38f, 0.8f, 0.1f, 9.6f, 25),
            FoodTemplate("柿子", "水果", 14f, 71f, 0.4f, 0.2f, 18.6f, 70),
            FoodTemplate("榴莲", "水果", 13.7f, 147f, 1.5f, 5.3f, 27.1f, 49),
            FoodTemplate("菠萝", "水果", 10f, 50f, 0.5f, 0.1f, 13.1f, 66),
            FoodTemplate("椰子", "水果", 6.2f, 354f, 3.3f, 33.5f, 15.2f, 45),
            FoodTemplate("哈密瓜", "水果", 7.9f, 34f, 0.8f, 0.1f, 8.2f, 65),
            FoodTemplate("杨梅", "水果", 7f, 28f, 0.8f, 0.2f, 6.7f, 25),
            FoodTemplate("石榴", "水果", 13.7f, 83f, 1.7f, 1.2f, 18.7f, 31),
            FoodTemplate("百香果", "水果", 11.2f, 97f, 2.2f, 0.7f, 23.4f, 30),

            FoodTemplate("西兰花", "蔬菜", 1.7f, 34f, 2.8f, 0.4f, 6.6f, 10),
            FoodTemplate("番茄", "蔬菜", 2.6f, 18f, 0.9f, 0.2f, 3.9f, 15),
            FoodTemplate("菠菜", "蔬菜", 0.4f, 23f, 2.9f, 0.4f, 3.6f, 6),
            FoodTemplate("胡萝卜", "蔬菜", 4.7f, 41f, 0.9f, 0.2f, 9.6f, 16),
            FoodTemplate("黄瓜", "蔬菜", 1.7f, 15f, 0.6f, 0.1f, 3.6f, 15),
            FoodTemplate("生菜", "蔬菜", 1.3f, 15f, 1.4f, 0.2f, 2.9f, 15),
            FoodTemplate("青椒", "蔬菜", 2.4f, 20f, 0.9f, 0.2f, 4.6f, 15),
            FoodTemplate("茄子", "蔬菜", 3.5f, 25f, 1f, 0.2f, 5.9f, 15),
            FoodTemplate("芹菜", "蔬菜", 1.3f, 16f, 0.7f, 0.2f, 3f, 15),
            FoodTemplate("洋葱", "蔬菜", 4.2f, 40f, 1.1f, 0.1f, 9.3f, 10),
            FoodTemplate("蘑菇", "蔬菜", 2f, 22f, 3.1f, 0.3f, 3.3f, 15),
            FoodTemplate("豆芽", "蔬菜", 1.5f, 31f, 3.2f, 0.1f, 5.9f, 15),
            FoodTemplate("白菜", "蔬菜", 1.2f, 13f, 1f, 0.2f, 2.2f, 6),
            FoodTemplate("花菜", "蔬菜", 1.9f, 25f, 1.9f, 0.3f, 5f, 15),
            FoodTemplate("秋葵", "蔬菜", 1.5f, 33f, 1.9f, 0.2f, 7f, 20),
            FoodTemplate("芦笋", "蔬菜", 1.9f, 20f, 2.2f, 0.1f, 3.9f, 15),
            FoodTemplate("南瓜", "蔬菜", 2.8f, 26f, 1f, 0.1f, 6.5f, 75),
            FoodTemplate("冬瓜", "蔬菜", 1.8f, 12f, 0.4f, 0.2f, 2.6f, 10),
            FoodTemplate("苦瓜", "蔬菜", 1.3f, 17f, 1f, 0.1f, 3.7f, 15),
            FoodTemplate("莲藕", "蔬菜", 1.5f, 74f, 2.6f, 0.1f, 17.2f, 38),

            FoodTemplate("鸡胸肉", "蛋白质", 0f, 165f, 31f, 3.6f, 0f, 0),
            FoodTemplate("牛里脊", "蛋白质", 0f, 250f, 26f, 15f, 0f, 0),
            FoodTemplate("猪里脊", "蛋白质", 0f, 143f, 21f, 6f, 0f, 0),
            FoodTemplate("三文鱼", "蛋白质", 0f, 208f, 20f, 13f, 0f, 0),
            FoodTemplate("虾仁", "蛋白质", 0f, 99f, 24f, 0.3f, 0.2f, 0),
            FoodTemplate("鸡蛋", "蛋白质", 0.6f, 155f, 13f, 11f, 1.1f, 0),
            FoodTemplate("豆腐", "蛋白质", 0.7f, 76f, 8f, 4.8f, 1.9f, 15),
            FoodTemplate("鲈鱼", "蛋白质", 0f, 97f, 19.3f, 2f, 0f, 0),
            FoodTemplate("带鱼", "蛋白质", 0f, 127f, 18f, 5.6f, 0f, 0),
            FoodTemplate("鱿鱼", "蛋白质", 0.5f, 92f, 18f, 1.2f, 3.1f, 0),
            FoodTemplate("排骨", "蛋白质", 0f, 290f, 18.3f, 23.1f, 0f, 0),
            FoodTemplate("鸭肉", "蛋白质", 0f, 201f, 19.7f, 13.3f, 0f, 0),

            FoodTemplate("巧克力蛋糕", "甜品", 35f, 370f, 5f, 15f, 53f, 38),
            FoodTemplate("提拉米苏", "甜品", 25f, 283f, 6f, 13f, 35f, 42),
            FoodTemplate("冰淇淋", "甜品", 21f, 207f, 3.5f, 11f, 24f, 51),
            FoodTemplate("泡芙", "甜品", 18f, 262f, 5f, 14f, 30f, 45),
            FoodTemplate("马卡龙", "甜品", 40f, 430f, 5f, 17f, 60f, 55),
            FoodTemplate("奶油蛋糕", "甜品", 30f, 350f, 4f, 18f, 42f, 40),
            FoodTemplate("月饼", "甜品", 28f, 421f, 8f, 22f, 50f, 60),
            FoodTemplate("汤圆", "甜品", 15f, 174f, 3.2f, 1.5f, 38f, 85),
            FoodTemplate("糯米糍", "甜品", 20f, 220f, 3f, 2f, 48f, 75),
            FoodTemplate("蛋挞", "甜品", 15f, 256f, 5.3f, 16f, 24f, 45),
            FoodTemplate("芝士蛋糕", "甜品", 22f, 321f, 6f, 23f, 25f, 40),
            FoodTemplate("红豆沙", "甜品", 25f, 180f, 6f, 0.5f, 38f, 55),
            FoodTemplate("龟苓膏", "甜品", 8f, 50f, 1f, 0.1f, 12f, 30),
            FoodTemplate("豆花", "甜品", 10f, 80f, 4f, 2f, 12f, 35),
            FoodTemplate("杏仁豆腐", "甜品", 12f, 100f, 3f, 3f, 15f, 35),

            FoodTemplate("汉堡", "快餐", 7f, 295f, 17f, 14f, 24f, 66),
            FoodTemplate("薯条", "快餐", 0.3f, 312f, 3.4f, 15f, 41f, 75),
            FoodTemplate("披萨", "快餐", 3.6f, 266f, 11f, 10f, 33f, 60),
            FoodTemplate("炸鸡腿", "快餐", 0.5f, 250f, 20f, 15f, 10f, 45),
            FoodTemplate("鸡米花", "快餐", 1f, 270f, 18f, 16f, 15f, 50),
            FoodTemplate("热狗", "快餐", 3f, 290f, 10f, 18f, 22f, 55),
            FoodTemplate("麻辣烫", "快餐", 5f, 200f, 10f, 8f, 25f, 45),
            FoodTemplate("煎饺", "快餐", 2f, 220f, 8f, 10f, 25f, 55),
            FoodTemplate("春卷", "快餐", 1f, 194f, 5f, 10f, 22f, 55),
            FoodTemplate("烤肠", "快餐", 2f, 310f, 12f, 25f, 8f, 40),

            FoodTemplate("核桃", "坚果", 2.6f, 654f, 15.2f, 65f, 14f, 15),
            FoodTemplate("杏仁", "坚果", 3.9f, 579f, 21f, 50f, 22f, 15),
            FoodTemplate("花生", "坚果", 4.7f, 567f, 25.8f, 49f, 16f, 14),
            FoodTemplate("腰果", "坚果", 5.9f, 553f, 18f, 44f, 30f, 22),
            FoodTemplate("开心果", "坚果", 7.7f, 560f, 20f, 45f, 28f, 15),
            FoodTemplate("松子", "坚果", 3.6f, 673f, 14f, 68f, 13f, 15),
            FoodTemplate("板栗", "坚果", 10.6f, 213f, 2.4f, 1.5f, 46f, 60),
            FoodTemplate("葵花籽", "坚果", 2.6f, 584f, 21f, 51f, 20f, 15),

            FoodTemplate("红烧肉", "炒菜", 8f, 370f, 15f, 30f, 12f, 45),
            FoodTemplate("宫保鸡丁", "炒菜", 6f, 220f, 18f, 12f, 15f, 42),
            FoodTemplate("麻婆豆腐", "炒菜", 3f, 150f, 10f, 8f, 10f, 30),
            FoodTemplate("鱼香肉丝", "炒菜", 8f, 190f, 14f, 10f, 12f, 38),
            FoodTemplate("糖醋排骨", "炒菜", 25f, 280f, 15f, 18f, 15f, 55),
            FoodTemplate("蚝油牛肉", "炒菜", 5f, 200f, 20f, 10f, 8f, 25),
            FoodTemplate("清蒸鲈鱼", "炒菜", 1f, 110f, 20f, 3f, 1f, 0),
            FoodTemplate("西红柿炒蛋", "炒菜", 4f, 130f, 8f, 8f, 6f, 30),
            FoodTemplate("干煸四季豆", "炒菜", 3f, 160f, 6f, 10f, 12f, 25),
            FoodTemplate("酸菜鱼", "炒菜", 2f, 180f, 18f, 10f, 5f, 20),
            FoodTemplate("回锅肉", "炒菜", 5f, 320f, 14f, 25f, 10f, 35),
            FoodTemplate("水煮牛肉", "炒菜", 3f, 250f, 22f, 15f, 8f, 25),
            FoodTemplate("辣子鸡", "炒菜", 4f, 260f, 20f, 16f, 10f, 30),
            FoodTemplate("地三鲜", "炒菜", 5f, 180f, 4f, 12f, 15f, 40),
            FoodTemplate("蒜蓉西兰花", "炒菜", 2f, 60f, 4f, 3f, 6f, 15),

            FoodTemplate("阳春面", "面食", 1f, 150f, 5f, 1f, 30f, 55),
            FoodTemplate("兰州拉面", "面食", 2f, 180f, 8f, 3f, 32f, 58),
            FoodTemplate("刀削面", "面食", 1f, 170f, 6f, 2f, 33f, 56),
            FoodTemplate("炸酱面", "面食", 5f, 220f, 10f, 6f, 35f, 50),
            FoodTemplate("肉丝面", "面食", 1f, 200f, 10f, 5f, 30f, 52),
            FoodTemplate("牛肉面", "面食", 2f, 250f, 15f, 8f, 32f, 50),
            FoodTemplate("意大利面", "面食", 2f, 220f, 8f, 5f, 38f, 49),
            FoodTemplate("炒面", "面食", 3f, 230f, 8f, 8f, 32f, 55),
            FoodTemplate("方便面", "面食", 3f, 470f, 9f, 20f, 62f, 73),
            FoodTemplate("凉面", "面食", 2f, 160f, 5f, 3f, 28f, 50),

            FoodTemplate("小笼包", "小吃", 3f, 200f, 8f, 6f, 28f, 55),
            FoodTemplate("生煎包", "小吃", 3f, 240f, 8f, 10f, 28f, 58),
            FoodTemplate("烧麦", "小吃", 2f, 210f, 7f, 6f, 32f, 55),
            FoodTemplate("葱油饼", "小吃", 1f, 280f, 6f, 12f, 38f, 62),
            FoodTemplate("肉夹馍", "小吃", 3f, 260f, 12f, 10f, 30f, 55),
            FoodTemplate("凉皮", "小吃", 2f, 150f, 4f, 3f, 28f, 48),
            FoodTemplate("臭豆腐", "小吃", 1f, 200f, 8f, 14f, 10f, 30),
            FoodTemplate("鸡蛋灌饼", "小吃", 2f, 250f, 9f, 10f, 32f, 60),
            FoodTemplate("手抓饼", "小吃", 2f, 270f, 6f, 12f, 36f, 62),
            FoodTemplate("豆腐脑", "小吃", 3f, 50f, 4f, 2f, 4f, 20),

            FoodTemplate("皮蛋瘦肉粥", "粥品", 2f, 80f, 5f, 2f, 12f, 70),
            FoodTemplate("白粥", "粥品", 0.5f, 46f, 1f, 0.1f, 10f, 85),
            FoodTemplate("八宝粥", "粥品", 15f, 120f, 3f, 1f, 25f, 60),
            FoodTemplate("南瓜粥", "粥品", 3f, 40f, 1f, 0.1f, 9f, 55),
            FoodTemplate("小米粥", "粥品", 1f, 47f, 1.4f, 0.7f, 9f, 61),
            FoodTemplate("燕麦粥", "粥品", 0.5f, 68f, 2.4f, 1.4f, 12f, 55),
            FoodTemplate("紫薯粥", "粥品", 4f, 55f, 1f, 0.1f, 12f, 55),
            FoodTemplate("绿豆粥", "粥品", 2f, 53f, 2.5f, 0.3f, 10.5f, 35),

            FoodTemplate("凯撒沙拉", "沙拉", 2f, 127f, 7f, 7f, 9f, 20),
            FoodTemplate("金枪鱼沙拉", "沙拉", 1f, 150f, 15f, 8f, 5f, 15),
            FoodTemplate("鸡肉沙拉", "沙拉", 2f, 140f, 18f, 5f, 8f, 15),
            FoodTemplate("水果沙拉", "沙拉", 12f, 100f, 1f, 3f, 18f, 35),
            FoodTemplate("藜麦沙拉", "沙拉", 1f, 160f, 8f, 5f, 22f, 35),

            FoodTemplate("紫菜蛋花汤", "汤品", 1f, 30f, 2f, 1f, 3f, 15),
            FoodTemplate("西红柿蛋汤", "汤品", 3f, 40f, 3f, 2f, 4f, 20),
            FoodTemplate("酸辣汤", "汤品", 2f, 50f, 3f, 2f, 6f, 25),
            FoodTemplate("玉米排骨汤", "汤品", 3f, 80f, 6f, 3f, 8f, 30),
            FoodTemplate("鸡汤", "汤品", 0.5f, 45f, 5f, 2.5f, 0.5f, 5),
            FoodTemplate("味噌汤", "汤品", 2f, 35f, 3f, 1f, 4f, 20),
            FoodTemplate("冬瓜排骨汤", "汤品", 1f, 60f, 5f, 3f, 3f, 15),
            FoodTemplate("银耳汤", "汤品", 8f, 50f, 1f, 0.1f, 12f, 30),

            FoodTemplate("寿司(鲑鱼)", "寿司", 5f, 150f, 8f, 3f, 22f, 55),
            FoodTemplate("寿司(金枪鱼)", "寿司", 5f, 130f, 10f, 1f, 22f, 55),
            FoodTemplate("手卷", "寿司", 4f, 120f, 6f, 2f, 20f, 50),
            FoodTemplate("刺身拼盘", "寿司", 0f, 100f, 20f, 2f, 0f, 0),

            FoodTemplate("麻辣火锅", "火锅", 3f, 300f, 15f, 20f, 12f, 30),
            FoodTemplate("清汤火锅", "火锅", 1f, 150f, 12f, 8f, 8f, 20),
            FoodTemplate("番茄火锅", "火锅", 5f, 180f, 10f, 8f, 15f, 35),
            FoodTemplate("菌菇火锅", "火锅", 2f, 120f, 8f, 5f, 10f, 20),

            FoodTemplate("烤羊肉串", "烧烤", 2f, 250f, 18f, 18f, 3f, 15),
            FoodTemplate("烤鸡翅", "烧烤", 3f, 220f, 17f, 15f, 5f, 20),
            FoodTemplate("烤茄子", "烧烤", 4f, 100f, 2f, 6f, 10f, 20),
            FoodTemplate("烤韭菜", "烧烤", 2f, 50f, 2f, 2f, 5f, 15),
            FoodTemplate("烤玉米", "烧烤", 5f, 120f, 3f, 2f, 22f, 55),
            FoodTemplate("烤鱼", "烧烤", 3f, 180f, 20f, 8f, 5f, 15)
        )

        val cookStyles = listOf("清蒸", "红烧", "水煮", "干煸", "蒜蓉", "葱油", "糖醋", "麻辣", "酱爆", "白灼")
        val portions = listOf("小份" to 0.6f, "中份" to 1.0f, "大份" to 1.5f)

        for (t in templates) {
            for ((portion, mult) in portions) {
                foods.add(FoodEntity(
                    foodId = id++, foodName = "${t.name}($portion)", category = t.cat,
                    sugarContent = t.sugar * mult, calories = t.cal * mult, protein = t.protein * mult,
                    fat = t.fat * mult, carbohydrate = t.carb * mult, servingSize = portion,
                    imageUrl = foodImages[t.cat] ?: foodImages["炒菜"],
                    giValue = t.gi, createdAt = "2026-04-01"
                ))
            }
        }

        val proteins = listOf("鸡胸肉", "牛肉", "猪肉", "鱼肉", "虾仁", "豆腐", "鸡蛋")
        val vegs = listOf("西兰花", "青椒", "胡萝卜", "蘑菇", "洋葱", "芹菜", "白菜", "茄子")
        for (style in cookStyles) {
            for (protein in proteins) {
                foods.add(FoodEntity(
                    foodId = id++, foodName = "$style$protein", category = "炒菜",
                    sugarContent = if (style == "糖醋") 20f else 3f,
                    calories = 200f, protein = 18f, fat = 10f, carbohydrate = 8f,
                    imageUrl = foodImages["炒菜"], giValue = 25, createdAt = "2026-04-01"
                ))
            }
            for (veg in vegs) {
                foods.add(FoodEntity(
                    foodId = id++, foodName = "$style$veg", category = "炒菜",
                    sugarContent = if (style == "糖醋") 15f else 2f,
                    calories = 80f, protein = 3f, fat = 5f, carbohydrate = 8f,
                    imageUrl = foodImages["蔬菜"], giValue = 15, createdAt = "2026-04-01"
                ))
            }
        }

        val regions = listOf("川菜", "粤菜", "湘菜", "鲁菜", "苏菜", "浙菜", "闽菜", "徽菜")
        val regionalDishes = mapOf(
            "川菜" to listOf("水煮鱼", "回锅肉", "鱼香茄子", "酸菜鱼", "夫妻肺片", "担担面", "钟水饺", "龙抄手", "火锅", "口水鸡",
                "辣子鸡丁", "毛血旺", "棒棒鸡", "麻辣豆腐", "蒜泥白肉", "泡椒凤爪", "干锅花菜", "干锅牛蛙", "冒菜", "串串香"),
            "粤菜" to listOf("白切鸡", "叉烧", "烧鹅", "虾饺", "肠粉", "煲仔饭", "老火靓汤", "菠萝咕噜肉", "豉汁蒸排骨", "蜜汁叉烧",
                "清蒸石斑鱼", "蒸凤爪", "干炒牛河", "蛋黄焗南瓜", "蚝油生菜", "白灼虾", "腐竹焖鸭", "冬瓜盅", "糖水", "双皮奶"),
            "湘菜" to listOf("剁椒鱼头", "小炒黄牛肉", "辣椒炒肉", "毛氏红烧肉", "口味虾", "酸辣鸡杂", "干锅鸡", "血鸭", "腊味合蒸", "湘西外婆菜"),
            "鲁菜" to listOf("葱烧海参", "糖醋鲤鱼", "九转大肠", "爆炒腰花", "四喜丸子", "油爆双脆", "拔丝地瓜", "德州扒鸡", "锅塌豆腐", "蒜蓉大虾"),
            "苏菜" to listOf("松鼠桂鱼", "蟹粉豆腐", "狮子头", "盐水鸭", "大煮干丝", "三套鸭", "水晶虾仁", "软兜长鱼", "文思豆腐", "淮扬煎饺"),
            "浙菜" to listOf("西湖醋鱼", "东坡肉", "龙井虾仁", "叫化鸡", "宋嫂鱼羹", "西湖牛肉羹", "干炸响铃", "蒋公鱼头", "桂花糯米藕", "糖醋小排"),
            "闽菜" to listOf("佛跳墙", "荔枝肉", "醉排骨", "蛏溜奇", "炒米粉", "沙茶面", "蚵仔煎", "鼎边糊", "太极芋泥", "扁食"),
            "徽菜" to listOf("臭鳜鱼", "毛豆腐", "黄山炖鸽", "问政山笋", "火腿炖甲鱼", "腊八豆腐", "刀板香", "虎皮毛豆腐", "绩溪挞粿", "一品锅")
        )
        for ((region, dishes) in regionalDishes) {
            for (dish in dishes) {
                val isSugary = dish.contains("糖") || dish.contains("蜜") || dish.contains("甜")
                foods.add(FoodEntity(
                    foodId = id++, foodName = "$dish($region)", category = region,
                    sugarContent = if (isSugary) 20f else (2f + (dish.hashCode() % 10).toFloat().coerceIn(0f, 8f)),
                    calories = (150f + (dish.hashCode() % 200).toFloat().coerceIn(0f, 200f)),
                    protein = 12f, fat = 10f, carbohydrate = 15f,
                    imageUrl = foodImages["炒菜"], giValue = 35, createdAt = "2026-04-01"
                ))
            }
        }

        val chainBrands = listOf("麦当劳", "肯德基", "必胜客", "汉堡王", "赛百味", "吉野家", "真功夫", "永和大王", "杨铭宇黄焖鸡", "老乡鸡")
        val chainMenus = listOf(
            "招牌套餐" to Triple(12f, 650f, 35), "单人堡餐" to Triple(10f, 580f, 40), "小食拼盘" to Triple(5f, 380f, 45),
            "沙拉套餐" to Triple(3f, 250f, 65), "早餐套餐" to Triple(8f, 420f, 50), "甜品套餐" to Triple(25f, 350f, 25),
            "鸡肉卷" to Triple(6f, 380f, 48), "牛肉饭" to Triple(4f, 450f, 52), "海鲜粥" to Triple(2f, 200f, 70),
            "炸鸡桶" to Triple(3f, 800f, 25), "鸡腿堡" to Triple(8f, 520f, 38), "鱼堡" to Triple(6f, 380f, 45)
        )
        for (brand in chainBrands) {
            for ((menu, nutrition) in chainMenus) {
                foods.add(FoodEntity(
                    foodId = id++, foodName = "$brand $menu", category = "快餐",
                    sugarContent = nutrition.first, calories = nutrition.second,
                    protein = 20f, fat = 15f, carbohydrate = 35f,
                    imageUrl = foodImages["快餐"], giValue = nutrition.third, createdAt = "2026-04-01"
                ))
            }
        }

        val intlCuisines = listOf(
            "日式咖喱饭" to Triple(8f, 420f, "小吃"), "韩式石锅拌饭" to Triple(5f, 380f, "小吃"),
            "泰式冬阴功汤" to Triple(4f, 150f, "汤品"), "越南河粉" to Triple(3f, 350f, "面食"),
            "印度咖喱鸡" to Triple(6f, 300f, "炒菜"), "墨西哥卷饼" to Triple(5f, 320f, "快餐"),
            "法式焗蜗牛" to Triple(1f, 200f, "蛋白质"), "意式肉酱面" to Triple(6f, 380f, "面食"),
            "韩式炸鸡" to Triple(5f, 350f, "快餐"), "日式天妇罗" to Triple(3f, 280f, "小吃"),
            "泰式炒河粉" to Triple(8f, 350f, "面食"), "韩式泡菜汤" to Triple(3f, 120f, "汤品"),
            "日式拉面" to Triple(4f, 450f, "面食"), "越南春卷" to Triple(2f, 150f, "小吃"),
            "印度馕" to Triple(3f, 260f, "主食"), "希腊沙拉" to Triple(2f, 120f, "沙拉"),
            "西班牙海鲜饭" to Triple(3f, 380f, "海鲜"), "法式蜗牛" to Triple(1f, 180f, "蛋白质"),
            "意式焗饭" to Triple(4f, 350f, "主食"), "韩式年糕" to Triple(15f, 200f, "小吃"),
            "日式饭团" to Triple(3f, 180f, "主食"), "韩式紫菜包饭" to Triple(5f, 250f, "主食"),
            "泰式芒果糯米饭" to Triple(20f, 300f, "甜品"), "日式抹茶蛋糕" to Triple(18f, 280f, "甜品"),
            "法式马卡龙" to Triple(35f, 400f, "甜品"), "意式奶冻" to Triple(20f, 250f, "甜品"),
            "土耳其烤肉" to Triple(2f, 280f, "烧烤"), "中东沙威玛" to Triple(4f, 320f, "快餐"),
            "新加坡海南鸡饭" to Triple(2f, 400f, "主食"), "马来西亚椰浆饭" to Triple(5f, 380f, "主食")
        )
        for ((name, triple) in intlCuisines) {
            for ((portion, mult) in portions) {
                foods.add(FoodEntity(
                    foodId = id++, foodName = "$name($portion)", category = triple.third,
                    sugarContent = triple.first * mult, calories = triple.second * mult,
                    protein = 12f * mult, fat = 10f * mult, carbohydrate = 25f * mult,
                    imageUrl = foodImages[triple.third] ?: foodImages["炒菜"], giValue = 40, createdAt = "2026-04-01"
                ))
            }
        }

        val snackItems = listOf(
            "薯片" to 3f, "饼干" to 15f, "巧克力" to 50f, "糖果" to 70f, "果冻" to 18f,
            "话梅" to 10f, "海苔" to 2f, "牛肉干" to 5f, "鱿鱼丝" to 8f, "猪肉脯" to 12f,
            "蛋黄酥" to 15f, "凤梨酥" to 25f, "老婆饼" to 20f, "蛋卷" to 18f, "沙琪玛" to 30f,
            "麻花" to 8f, "米花糖" to 35f, "雪饼" to 12f, "仙贝" to 5f, "小馒头" to 10f,
            "夹心饼干" to 22f, "威化饼" to 20f, "蛋糕卷" to 25f, "铜锣烧" to 22f, "蛋黄派" to 18f,
            "能量棒" to 15f, "坚果棒" to 8f, "果干" to 30f, "酸奶片" to 12f, "奶片" to 15f,
            "锅巴" to 3f, "虾条" to 4f, "小鱼干" to 2f, "豆干" to 3f, "辣条" to 10f,
            "膨化食品" to 5f, "爆米花" to 8f, "麻薯" to 20f, "绿豆糕" to 25f, "桃酥" to 15f
        )
        for (item in snackItems) {
            foods.add(FoodEntity(
                foodId = id++, foodName = item.first, category = "零食",
                sugarContent = item.second, calories = item.second * 5f + 100f,
                protein = 3f, fat = 8f, carbohydrate = item.second * 1.5f + 15f,
                imageUrl = foodImages["小吃"], giValue = 50, createdAt = "2026-04-01"
            ))
        }

        val breakfasts = listOf(
            "油条豆浆" to 5f, "煎饼果子" to 3f, "鸡蛋灌饼" to 2f, "小笼包" to 3f,
            "生煎包" to 3f, "胡辣汤" to 2f, "豆腐脑" to 5f, "肉粥" to 2f,
            "牛奶麦片" to 12f, "吐司配果酱" to 15f, "水果燕麦碗" to 10f, "蔬菜蛋饼" to 2f,
            "三明治" to 5f, "培根蛋松饼" to 8f, "法式吐司" to 12f, "牛角包" to 8f,
            "蒸蛋" to 1f, "酱肉包" to 3f, "糯米鸡" to 5f, "粢饭团" to 3f,
            "馄饨" to 2f, "肉夹馍" to 3f, "烧饼夹蛋" to 2f, "葱花饼" to 1f,
            "手抓饼加蛋" to 3f, "红豆吐司" to 15f, "芝麻球" to 12f, "南瓜饼" to 10f,
            "玉米棒" to 5f, "紫薯包" to 8f
        )
        for (item in breakfasts) {
            foods.add(FoodEntity(
                foodId = id++, foodName = item.first, category = "早餐",
                sugarContent = item.second, calories = item.second * 8f + 150f,
                protein = 8f, fat = 6f, carbohydrate = 25f,
                imageUrl = foodImages["早餐"], giValue = 55, createdAt = "2026-04-01"
            ))
        }

        return foods
    }
}
