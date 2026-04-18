package com.example.myapplication.db

import com.example.myapplication.db.entity.*
import java.text.SimpleDateFormat
import java.util.*

object DatabaseSeeder {

    suspend fun seedAll(database: AppDatabase) {
        seedDefaultUser(database)
        seedTestUser(database)
        seedDrinks(database)
        seedFoods(database)
        seedDefaultHealthProfile(database)
        seedTestUserHealthProfile(database)
        seedSampleHealthRecords(database)
        seedTestUserHealthRecords(database)
        seedSampleMealRecords(database)
        seedTestUserMealRecords(database)
        seedExtendedDatabase(database)
    }

    private suspend fun seedExtendedDatabase(db: AppDatabase) {
        try {
            val extraDrinks = FoodDatabaseGenerator.generateAllDrinks()
            for (drink in extraDrinks) {
                try { db.drinkDao().insert(drink) } catch (_: Exception) {}
            }
            val extraFoods = FoodDatabaseGenerator.generateAllFoods()
            for (food in extraFoods) {
                try { db.foodDao().insert(food) } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    private fun now(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun today(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private suspend fun seedDefaultUser(db: AppDatabase) {
        db.userDao().insert(
            UserEntity(
                id = 1, username = "demo", password = "123456",
                email = "demo@sugarguard.com", phone = "13800138000",
                gender = "male", birthday = "2002-06-15",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop",
                createdAt = now(), updatedAt = now()
            )
        )
    }

    private suspend fun seedTestUser(db: AppDatabase) {
        db.userDao().insert(
            UserEntity(
                id = 2, username = "testuser", password = "test123456",
                email = "test@sg.com", phone = "13912345678",
                gender = "male", birthday = "2003-03-20",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop",
                createdAt = now(), updatedAt = now()
            )
        )
    }

    private suspend fun seedTestUserHealthProfile(db: AppDatabase) {
        db.userHealthProfileDao().insert(
            UserHealthProfileEntity(
                userId = 2, age = 23, gender = "male",
                height = 172f, weight = 65f,
                healthConditions = "无特殊健康问题",
                allergies = "无已知过敏",
                medications = "无",
                activityLevel = "light",
                sugarLimit = 25f,
                calorieLimit = 2000f,
                waterGoal = 2000f,
                bmi = 22.0f,
                createdAt = now(), updatedAt = now()
            )
        )
    }

    private suspend fun seedTestUserHealthRecords(db: AppDatabase) {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 6 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val date = sdf.format(cal.time)
            val moods = listOf("excellent", "good", "good", "normal", "good", "excellent", "good")
            db.dailyHealthRecordDao().insert(
                DailyHealthRecordEntity(
                    userId = 2, recordDate = date,
                    totalSugarIntake = (18f + (Math.random() * 22f)).toFloat(),
                    totalCalories = (1400f + (Math.random() * 600f)).toFloat(),
                    waterIntake = (1000f + (Math.random() * 1200f)).toFloat(),
                    exerciseMinutes = (10f + (Math.random() * 50f)).toFloat(),
                    sleepHours = (6f + (Math.random() * 2.5f)).toFloat(),
                    systolicBp = (108f + (Math.random() * 22f)).toFloat(),
                    diastolicBp = (62f + (Math.random() * 18f)).toFloat(),
                    bloodGlucose = (4.2f + (Math.random() * 2.3f)).toFloat(),
                    weight = (64f + (Math.random() * 3f)).toFloat(),
                    mood = moods[i],
                    notes = null,
                    createdAt = now()
                )
            )
        }
    }

    private suspend fun seedTestUserMealRecords(db: AppDatabase) {
        val td = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val meals = listOf(
            MealRecordEntity(
                userId = 2, mealDate = td, mealTime = "07:30:00",
                mealType = "breakfast", foodName = "无糖酸奶 + 全麦面包",
                sugarContent = 6.0, calories = 230.0,
                protein = 12.0, fat = 5.0, carbohydrate = 30.0,
                imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=300&fit=crop",
                portionSize = "酸奶200g + 面包2片", createdAt = now()),
            MealRecordEntity(
                userId = 2, mealDate = td, mealTime = "12:15:00",
                mealType = "lunch", foodName = "糖醋排骨盖饭",
                sugarContent = 25.0, calories = 520.0,
                protein = 22.0, fat = 18.0, carbohydrate = 65.0,
                imageUrl = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&h=300&fit=crop",
                portionSize = "一份", createdAt = now()),
            MealRecordEntity(
                userId = 2, mealDate = td, mealTime = "15:30:00",
                mealType = "snack", foodName = "杨枝甘露（全糖）",
                sugarContent = 38.0, calories = 350.0,
                protein = 2.0, fat = 5.0, carbohydrate = 72.0,
                imageUrl = "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&h=400&fit=crop",
                portionSize = "500ml", createdAt = now())
        )
        for (m in meals) {
            db.mealRecordDao().insert(m)
        }
    }

    private suspend fun seedDrinks(db: AppDatabase) {
        val drinks = listOf(
            // 奶茶类
            DrinkEntity(drinkId = 1, drinkName = "珍珠奶茶", brand = "喜茶", category = "奶茶",
                sugarContent = 38f, calories = 420f, volume = 500f, caffeine = 50f,
                fat = 8f, protein = 3f, sodium = 120f, healthScore = 30,
                ingredients = "红茶,牛奶,珍珠,糖浆", allergens = "牛奶",
                imageUrl = "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 2, drinkName = "芋泥波波奶茶", brand = "一点点", category = "奶茶",
                sugarContent = 42f, calories = 480f, volume = 500f, caffeine = 30f,
                fat = 10f, protein = 4f, sodium = 100f, healthScore = 25,
                ingredients = "芋泥,奶茶,波波,炼乳", allergens = "牛奶",
                imageUrl = "https://images.unsplash.com/photo-1627483262112-039e9a0a0c41?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 3, drinkName = "杨枝甘露", brand = "喜茶", category = "果茶",
                sugarContent = 35f, calories = 350f, volume = 500f, caffeine = 10f,
                fat = 5f, protein = 2f, sodium = 50f, healthScore = 40,
                ingredients = "芒果,西柚,椰浆,西米", allergens = "椰子",
                imageUrl = "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 4, drinkName = "多肉葡萄", brand = "喜茶", category = "果茶",
                sugarContent = 30f, calories = 310f, volume = 500f, caffeine = 20f,
                fat = 2f, protein = 1f, sodium = 30f, healthScore = 45,
                ingredients = "葡萄,绿茶,果冻", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 5, drinkName = "烤奶", brand = "茶百道", category = "奶茶",
                sugarContent = 28f, calories = 360f, volume = 500f, caffeine = 40f,
                fat = 9f, protein = 4f, sodium = 110f, healthScore = 35,
                ingredients = "红茶,牛奶,焦糖,奶油", allergens = "牛奶",
                imageUrl = "https://images.unsplash.com/photo-1571934811356-5cc061b6821f?w=400&h=400&fit=crop",
                createdAt = now()),

            // 咖啡类
            DrinkEntity(drinkId = 6, drinkName = "拿铁咖啡", brand = "星巴克", category = "咖啡",
                sugarContent = 12f, calories = 190f, volume = 350f, caffeine = 150f,
                fat = 7f, protein = 8f, sodium = 170f, healthScore = 55,
                ingredients = "浓缩咖啡,牛奶", allergens = "牛奶",
                imageUrl = "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 7, drinkName = "焦糖玛奇朵", brand = "星巴克", category = "咖啡",
                sugarContent = 33f, calories = 250f, volume = 350f, caffeine = 150f,
                fat = 7f, protein = 8f, sodium = 150f, healthScore = 35,
                ingredients = "浓缩咖啡,牛奶,焦糖酱,香草糖浆", allergens = "牛奶",
                imageUrl = "https://images.unsplash.com/photo-1485808191679-5f86510681a2?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 8, drinkName = "美式咖啡", brand = "瑞幸", category = "咖啡",
                sugarContent = 0f, calories = 15f, volume = 350f, caffeine = 200f,
                fat = 0f, protein = 0.5f, sodium = 10f, healthScore = 80,
                ingredients = "浓缩咖啡,水", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 9, drinkName = "生椰拿铁", brand = "瑞幸", category = "咖啡",
                sugarContent = 8f, calories = 180f, volume = 350f, caffeine = 130f,
                fat = 10f, protein = 3f, sodium = 80f, healthScore = 55,
                ingredients = "浓缩咖啡,椰浆,牛奶", allergens = "牛奶,椰子",
                imageUrl = "https://images.unsplash.com/photo-1592663527359-cf6642f54cff?w=400&h=400&fit=crop",
                createdAt = now()),

            // 碳酸饮料类
            DrinkEntity(drinkId = 10, drinkName = "可口可乐", brand = "可口可乐", category = "碳酸饮料",
                sugarContent = 35f, calories = 140f, volume = 330f, caffeine = 34f,
                fat = 0f, protein = 0f, sodium = 45f, healthScore = 20,
                ingredients = "碳酸水,高果糖浆,焦糖色,磷酸,咖啡因", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 11, drinkName = "雪碧", brand = "可口可乐", category = "碳酸饮料",
                sugarContent = 33f, calories = 130f, volume = 330f, caffeine = 0f,
                fat = 0f, protein = 0f, sodium = 65f, healthScore = 20,
                ingredients = "碳酸水,高果糖浆,柠檬酸,柠檬香精", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1625772299848-391b6a87d7b3?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 12, drinkName = "零度可乐", brand = "可口可乐", category = "碳酸饮料",
                sugarContent = 0f, calories = 0f, volume = 330f, caffeine = 34f,
                fat = 0f, protein = 0f, sodium = 40f, healthScore = 50,
                ingredients = "碳酸水,阿斯巴甜,焦糖色,磷酸", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1581636625402-29b2a704ef13?w=400&h=400&fit=crop",
                createdAt = now()),

            // 果汁类
            DrinkEntity(drinkId = 13, drinkName = "鲜榨橙汁", brand = "自制", category = "果汁",
                sugarContent = 21f, calories = 110f, volume = 250f, caffeine = 0f,
                fat = 0.5f, protein = 1.7f, sodium = 2f, healthScore = 70,
                ingredients = "新鲜橙子", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 14, drinkName = "西瓜汁", brand = "自制", category = "果汁",
                sugarContent = 15f, calories = 72f, volume = 250f, caffeine = 0f,
                fat = 0.2f, protein = 0.9f, sodium = 3f, healthScore = 75,
                ingredients = "新鲜西瓜", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1534353473418-4cfa6c56fd38?w=400&h=400&fit=crop",
                createdAt = now()),

            // 茶饮类
            DrinkEntity(drinkId = 15, drinkName = "绿茶", brand = "自泡", category = "茶饮",
                sugarContent = 0f, calories = 2f, volume = 250f, caffeine = 30f,
                fat = 0f, protein = 0f, sodium = 1f, healthScore = 95,
                ingredients = "绿茶叶", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 16, drinkName = "乌龙茶", brand = "自泡", category = "茶饮",
                sugarContent = 0f, calories = 2f, volume = 250f, caffeine = 35f,
                fat = 0f, protein = 0f, sodium = 1f, healthScore = 90,
                ingredients = "乌龙茶叶", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400&h=400&fit=crop",
                createdAt = now()),

            // 酸奶/乳饮
            DrinkEntity(drinkId = 17, drinkName = "酸奶", brand = "蒙牛", category = "乳饮",
                sugarContent = 15f, calories = 150f, volume = 250f, caffeine = 0f,
                fat = 5f, protein = 6f, sodium = 80f, healthScore = 70,
                ingredients = "牛奶,乳酸菌,白砂糖", allergens = "牛奶",
                imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 18, drinkName = "纯牛奶", brand = "蒙牛", category = "乳饮",
                sugarContent = 5f, calories = 130f, volume = 250f, caffeine = 0f,
                fat = 8f, protein = 8f, sodium = 100f, healthScore = 85,
                ingredients = "生牛乳", allergens = "牛奶",
                imageUrl = "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&h=400&fit=crop",
                createdAt = now()),

            // 功能饮料
            DrinkEntity(drinkId = 19, drinkName = "红牛", brand = "红牛", category = "功能饮料",
                sugarContent = 27f, calories = 110f, volume = 250f, caffeine = 80f,
                fat = 0f, protein = 0f, sodium = 200f, healthScore = 30,
                ingredients = "牛磺酸,咖啡因,B族维生素,糖", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1527960471264-932f39eb5846?w=400&h=400&fit=crop",
                createdAt = now()),
            DrinkEntity(drinkId = 20, drinkName = "元气森林气泡水", brand = "元气森林", category = "气泡水",
                sugarContent = 0f, calories = 0f, volume = 480f, caffeine = 0f,
                fat = 0f, protein = 0f, sodium = 5f, healthScore = 75,
                ingredients = "碳酸水,赤藓糖醇,天然香料", allergens = null,
                imageUrl = "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400&h=400&fit=crop",
                createdAt = now())
        )
        db.drinkDao().insertAll(drinks)
    }

    private suspend fun seedFoods(db: AppDatabase) {
        val foods = listOf(
            // 主食类
            FoodEntity(foodId = 1, foodName = "白米饭", category = "主食",
                sugarContent = 0.1f, calories = 116f, protein = 2.6f, fat = 0.3f,
                carbohydrate = 25.9f, fiber = 0.3f, sodium = 2f, servingSize = "100g (约一碗)",
                imageUrl = "https://images.unsplash.com/photo-1516684732162-798a0062be99?w=400&h=300&fit=crop",
                description = "精白米蒸煮而成，是中国主要主食",
                healthTips = "建议搭配蔬菜和蛋白质食用，控制每餐150-200g",
                giValue = 83),
            FoodEntity(foodId = 2, foodName = "全麦面包", category = "主食",
                sugarContent = 4f, calories = 247f, protein = 13f, fat = 3.4f,
                carbohydrate = 43f, fiber = 7f, sodium = 450f, servingSize = "100g (约2片)",
                imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&h=300&fit=crop",
                description = "使用全麦粉制作的面包，保留麸皮和胚芽",
                healthTips = "全麦面包比白面包升糖更慢，适合控糖人群",
                giValue = 51),
            FoodEntity(foodId = 3, foodName = "红薯", category = "主食",
                sugarContent = 4.2f, calories = 86f, protein = 1.6f, fat = 0.1f,
                carbohydrate = 20.1f, fiber = 3f, sodium = 55f, servingSize = "100g (约半个中号)",
                imageUrl = "https://images.unsplash.com/photo-1590165482129-1b8b27698780?w=400&h=300&fit=crop",
                description = "富含膳食纤维和维生素A的薯类食物",
                healthTips = "红薯虽然甜但GI值中等，蒸煮比油炸更健康",
                giValue = 61),
            FoodEntity(foodId = 4, foodName = "燕麦粥", category = "主食",
                sugarContent = 0.5f, calories = 68f, protein = 2.4f, fat = 1.4f,
                carbohydrate = 12f, fiber = 1.7f, sodium = 3f, servingSize = "100g (约一碗)",
                imageUrl = "https://images.unsplash.com/photo-1517673400267-0251440c45dc?w=400&h=300&fit=crop",
                description = "燕麦加水煮成的粥品，营养丰富",
                healthTips = "选择纯燕麦而非即食甜味燕麦，可更好控糖",
                giValue = 55),

            // 水果类
            FoodEntity(foodId = 5, foodName = "苹果", category = "水果",
                sugarContent = 10.4f, calories = 52f, protein = 0.3f, fat = 0.2f,
                carbohydrate = 13.8f, fiber = 2.4f, sodium = 1f, servingSize = "100g (约半个中号)",
                imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&h=300&fit=crop",
                description = "常见水果，富含维生素C和膳食纤维",
                healthTips = "每天一个苹果有益健康，建议连皮食用",
                giValue = 36),
            FoodEntity(foodId = 6, foodName = "香蕉", category = "水果",
                sugarContent = 12.2f, calories = 89f, protein = 1.1f, fat = 0.3f,
                carbohydrate = 22.8f, fiber = 2.6f, sodium = 1f, servingSize = "100g (约一根中号)",
                imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&h=300&fit=crop",
                description = "富含钾的热带水果，能量密度较高",
                healthTips = "香蕉含糖较高，建议选择不太熟的（青色）",
                giValue = 51),
            FoodEntity(foodId = 7, foodName = "蓝莓", category = "水果",
                sugarContent = 10f, calories = 57f, protein = 0.7f, fat = 0.3f,
                carbohydrate = 14.5f, fiber = 2.4f, sodium = 1f, servingSize = "100g (约一小碗)",
                imageUrl = "https://images.unsplash.com/photo-1498557850523-fd3d118b962e?w=400&h=300&fit=crop",
                description = "超级食物，富含花青素和抗氧化剂",
                healthTips = "蓝莓GI值低，是控糖优选水果之一",
                giValue = 25),
            FoodEntity(foodId = 8, foodName = "葡萄", category = "水果",
                sugarContent = 16.3f, calories = 69f, protein = 0.7f, fat = 0.2f,
                carbohydrate = 18.1f, fiber = 0.9f, sodium = 2f, servingSize = "100g (约10颗)",
                imageUrl = "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400&h=300&fit=crop",
                description = "葡萄含有丰富的白藜芦醇",
                healthTips = "葡萄含糖量偏高，建议每次食用量控制在100g以内",
                giValue = 46),
            FoodEntity(foodId = 9, foodName = "草莓", category = "水果",
                sugarContent = 4.9f, calories = 32f, protein = 0.7f, fat = 0.3f,
                carbohydrate = 7.7f, fiber = 2f, sodium = 1f, servingSize = "100g (约5-6颗)",
                imageUrl = "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400&h=300&fit=crop",
                description = "低糖水果，维生素C含量非常高",
                healthTips = "草莓是低GI水果，非常适合控糖人群食用",
                giValue = 25),

            // 蔬菜类
            FoodEntity(foodId = 10, foodName = "西兰花", category = "蔬菜",
                sugarContent = 1.7f, calories = 34f, protein = 2.8f, fat = 0.4f,
                carbohydrate = 6.6f, fiber = 2.6f, sodium = 33f, servingSize = "100g (约3-4朵)",
                imageUrl = "https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=400&h=300&fit=crop",
                description = "十字花科蔬菜，营养价值极高",
                healthTips = "西兰花是超级蔬菜，蒸煮3-5分钟保留最多营养",
                giValue = 10),
            FoodEntity(foodId = 11, foodName = "番茄", category = "蔬菜",
                sugarContent = 2.6f, calories = 18f, protein = 0.9f, fat = 0.2f,
                carbohydrate = 3.9f, fiber = 1.2f, sodium = 5f, servingSize = "100g (约1个中号)",
                imageUrl = "https://images.unsplash.com/photo-1546470427-e26264be0b11?w=400&h=300&fit=crop",
                description = "富含番茄红素的蔬果",
                healthTips = "番茄热量低、糖分少，是控糖好帮手",
                giValue = 15),
            FoodEntity(foodId = 12, foodName = "菠菜", category = "蔬菜",
                sugarContent = 0.4f, calories = 23f, protein = 2.9f, fat = 0.4f,
                carbohydrate = 3.6f, fiber = 2.2f, sodium = 79f, servingSize = "100g",
                imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400&h=300&fit=crop",
                description = "深绿色叶菜，富含铁和维生素K",
                healthTips = "菠菜几乎不含糖，是控糖饮食的理想蔬菜",
                giValue = 6),

            // 蛋白质类
            FoodEntity(foodId = 13, foodName = "鸡胸肉", category = "蛋白质",
                sugarContent = 0f, calories = 165f, protein = 31f, fat = 3.6f,
                carbohydrate = 0f, fiber = 0f, sodium = 74f, servingSize = "100g",
                imageUrl = "https://images.unsplash.com/photo-1604503468506-a8da13d82571?w=400&h=300&fit=crop",
                description = "低脂高蛋白肉类，健身控糖首选",
                healthTips = "鸡胸肉零糖零碳水，是控糖饮食的优质蛋白来源",
                giValue = 0),
            FoodEntity(foodId = 14, foodName = "三文鱼", category = "蛋白质",
                sugarContent = 0f, calories = 208f, protein = 20f, fat = 13f,
                carbohydrate = 0f, fiber = 0f, sodium = 59f, servingSize = "100g",
                imageUrl = "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=400&h=300&fit=crop",
                description = "富含Omega-3脂肪酸的深海鱼",
                healthTips = "三文鱼的Omega-3有助于改善胰岛素敏感性",
                giValue = 0),
            FoodEntity(foodId = 15, foodName = "水煮鸡蛋", category = "蛋白质",
                sugarContent = 0.6f, calories = 155f, protein = 13f, fat = 11f,
                carbohydrate = 1.1f, fiber = 0f, sodium = 124f, servingSize = "100g (约2个)",
                imageUrl = "https://images.unsplash.com/photo-1482049016688-2d3e1b311543?w=400&h=300&fit=crop",
                description = "完整蛋白质来源，营养全面",
                healthTips = "鸡蛋的蛋白质质量最高，每天1-2个有益健康",
                giValue = 0),
            FoodEntity(foodId = 16, foodName = "豆腐", category = "蛋白质",
                sugarContent = 0.7f, calories = 76f, protein = 8f, fat = 4.8f,
                carbohydrate = 1.9f, fiber = 0.3f, sodium = 7f, servingSize = "100g",
                imageUrl = "https://images.unsplash.com/photo-1628689469838-524a4a973b8e?w=400&h=300&fit=crop",
                description = "大豆制品，优质植物蛋白",
                healthTips = "豆腐低糖低卡高蛋白，是控糖饮食的好选择",
                giValue = 15),

            // 零食/甜品类
            FoodEntity(foodId = 17, foodName = "巧克力蛋糕", category = "甜品",
                sugarContent = 35f, calories = 370f, protein = 5f, fat = 15f,
                carbohydrate = 53f, fiber = 2f, sodium = 320f, servingSize = "100g (约1块)",
                imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400&h=300&fit=crop",
                description = "浓郁巧克力味的蛋糕",
                healthTips = "高糖高热量食品，建议偶尔食用，单次不超过50g",
                giValue = 38),
            FoodEntity(foodId = 18, foodName = "提拉米苏", category = "甜品",
                sugarContent = 25f, calories = 283f, protein = 6f, fat = 13f,
                carbohydrate = 35f, fiber = 0.5f, sodium = 100f, servingSize = "100g (约1块)",
                imageUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400&h=300&fit=crop",
                description = "意大利经典甜品，含咖啡和马斯卡彭芝士",
                healthTips = "含糖量高，建议选择小份并在午后食用",
                giValue = 42),
            FoodEntity(foodId = 19, foodName = "冰淇淋", category = "甜品",
                sugarContent = 21f, calories = 207f, protein = 3.5f, fat = 11f,
                carbohydrate = 24f, fiber = 0.7f, sodium = 80f, servingSize = "100g (约1球)",
                imageUrl = "https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=400&h=300&fit=crop",
                description = "冷冻甜品，含牛奶和糖",
                healthTips = "选择低糖或无糖冰淇淋可以减少糖分摄入",
                giValue = 51),

            // 快餐类
            FoodEntity(foodId = 20, foodName = "汉堡", category = "快餐",
                sugarContent = 7f, calories = 295f, protein = 17f, fat = 14f,
                carbohydrate = 24f, fiber = 1f, sodium = 560f, servingSize = "100g (约半个)",
                imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=300&fit=crop",
                description = "西式快餐，含面包、肉饼和蔬菜",
                healthTips = "汉堡隐含糖主要来自酱料和面包，选择全麦面包和少酱",
                giValue = 66),
            FoodEntity(foodId = 21, foodName = "薯条", category = "快餐",
                sugarContent = 0.3f, calories = 312f, protein = 3.4f, fat = 15f,
                carbohydrate = 41f, fiber = 3.8f, sodium = 210f, servingSize = "100g (约中份)",
                imageUrl = "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400&h=300&fit=crop",
                description = "油炸土豆条，高热量高碳水",
                healthTips = "薯条虽含糖不多但GI值极高，升糖速度快",
                giValue = 75),
            FoodEntity(foodId = 22, foodName = "披萨", category = "快餐",
                sugarContent = 3.6f, calories = 266f, protein = 11f, fat = 10f,
                carbohydrate = 33f, fiber = 2.3f, sodium = 640f, servingSize = "100g (约1片)",
                imageUrl = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&h=300&fit=crop",
                description = "意式面饼配芝士和各种配料",
                healthTips = "选择蔬菜配料的薄底披萨可以减少糖分和热量",
                giValue = 60),

            // 坚果类
            FoodEntity(foodId = 23, foodName = "核桃", category = "坚果",
                sugarContent = 2.6f, calories = 654f, protein = 15.2f, fat = 65f,
                carbohydrate = 14f, fiber = 6.7f, sodium = 2f, servingSize = "30g (约3个)",
                imageUrl = "https://images.unsplash.com/photo-1563412885-51bb84deaf70?w=400&h=300&fit=crop",
                description = "富含不饱和脂肪酸的坚果",
                healthTips = "核桃低糖富含好脂肪，每天一小把(30g)有益健康",
                giValue = 15),
            FoodEntity(foodId = 24, foodName = "杏仁", category = "坚果",
                sugarContent = 3.9f, calories = 579f, protein = 21f, fat = 50f,
                carbohydrate = 22f, fiber = 12.5f, sodium = 1f, servingSize = "30g (约15颗)",
                imageUrl = "https://images.unsplash.com/photo-1508061253366-f7da158b6d46?w=400&h=300&fit=crop",
                description = "高蛋白高纤维坚果",
                healthTips = "杏仁富含纤维和蛋白质，有助于稳定血糖",
                giValue = 15),

            // 沙拉/轻食
            FoodEntity(foodId = 25, foodName = "凯撒沙拉", category = "轻食",
                sugarContent = 2f, calories = 127f, protein = 7f, fat = 7f,
                carbohydrate = 9f, fiber = 2f, sodium = 350f, servingSize = "200g",
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&h=300&fit=crop",
                description = "经典沙拉，生菜配帕玛森芝士和面包丁",
                healthTips = "注意沙拉酱的含糖量，选择油醋汁代替千岛酱",
                giValue = 20),

            // 传统中式食物
            FoodEntity(foodId = 26, foodName = "包子", category = "主食",
                sugarContent = 3f, calories = 220f, protein = 7f, fat = 3f,
                carbohydrate = 40f, fiber = 1.5f, sodium = 350f, servingSize = "100g (约1个)",
                imageUrl = "https://images.unsplash.com/photo-1625938145744-e380515399bf?w=400&h=300&fit=crop",
                description = "中式面点，馅料多样",
                healthTips = "选择蔬菜馅包子比肉馅更适合控糖",
                giValue = 60),
            FoodEntity(foodId = 27, foodName = "饺子", category = "主食",
                sugarContent = 1.5f, calories = 210f, protein = 9f, fat = 7f,
                carbohydrate = 28f, fiber = 1f, sodium = 400f, servingSize = "100g (约5个)",
                imageUrl = "https://images.unsplash.com/photo-1496116218417-1a781b1c416c?w=400&h=300&fit=crop",
                description = "中国传统面食，水煮或蒸制",
                healthTips = "蒸饺比煎饺更健康，搭配醋食用更好",
                giValue = 55),
            FoodEntity(foodId = 28, foodName = "米线", category = "主食",
                sugarContent = 0.5f, calories = 113f, protein = 1.5f, fat = 0.3f,
                carbohydrate = 26f, fiber = 0.4f, sodium = 200f, servingSize = "100g",
                imageUrl = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400&h=300&fit=crop",
                description = "米浆制成的细条状食品",
                healthTips = "米线GI值较高，建议搭配蔬菜和蛋白质食用",
                giValue = 70),

            // 甜点/糖果
            FoodEntity(foodId = 29, foodName = "奶茶蛋糕", category = "甜品",
                sugarContent = 30f, calories = 320f, protein = 4f, fat = 18f,
                carbohydrate = 38f, fiber = 0.2f, sodium = 180f, servingSize = "100g",
                imageUrl = "https://images.unsplash.com/photo-1542826438-bd32f43d626f?w=400&h=300&fit=crop",
                description = "奶茶风味的蛋糕，含奶油和茶味",
                healthTips = "高糖高脂食品，每次仅建议食用一小块(50g以内)",
                giValue = 55),
            FoodEntity(foodId = 30, foodName = "酸奶水果捞", category = "轻食",
                sugarContent = 15f, calories = 120f, protein = 4f, fat = 3f,
                carbohydrate = 20f, fiber = 2f, sodium = 40f, servingSize = "200g",
                imageUrl = "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=300&fit=crop",
                description = "酸奶配新鲜水果和坚果",
                healthTips = "选择无糖酸奶和低糖水果（蓝莓、草莓）搭配最佳",
                giValue = 35)
        )
        db.foodDao().insertAll(foods)
    }

    private suspend fun seedDefaultHealthProfile(db: AppDatabase) {
        db.userHealthProfileDao().insert(
            UserHealthProfileEntity(
                userId = 1, age = 22, gender = "male",
                height = 175f, weight = 68f,
                healthConditions = "无特殊健康问题",
                allergies = "无已知过敏",
                medications = "无",
                activityLevel = "moderate",
                sugarLimit = 25f,
                calorieLimit = 2200f,
                waterGoal = 2000f,
                bmi = 22.2f,
                bloodType = "A",
                diabetesType = "无",
                targetWeight = 65f,
                createdAt = now(), updatedAt = now()
            )
        )
    }

    private suspend fun seedSampleHealthRecords(db: AppDatabase) {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val records = mutableListOf<DailyHealthRecordEntity>()

        for (i in 6 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val date = sdf.format(cal.time)
            val moods = listOf("excellent", "good", "good", "normal", "good", "excellent", "good")
            records.add(
                DailyHealthRecordEntity(
                    userId = 1, recordDate = date,
                    totalSugarIntake = (15f + (Math.random() * 20f)).toFloat(),
                    totalCalories = (1600f + (Math.random() * 600f)).toFloat(),
                    waterIntake = (1200f + (Math.random() * 1000f)).toFloat(),
                    exerciseMinutes = (20f + (Math.random() * 40f)).toFloat(),
                    sleepHours = (6f + (Math.random() * 2.5f)).toFloat(),
                    systolicBp = (110f + (Math.random() * 20f)).toFloat(),
                    diastolicBp = (65f + (Math.random() * 15f)).toFloat(),
                    bloodGlucose = (4.5f + (Math.random() * 2f)).toFloat(),
                    weight = (67f + (Math.random() * 2f)).toFloat(),
                    mood = moods[i],
                    notes = null,
                    createdAt = now()
                )
            )
        }
        for (r in records) {
            db.dailyHealthRecordDao().insert(r)
        }
    }

    private suspend fun seedSampleMealRecords(db: AppDatabase) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val meals = listOf(
            MealRecordEntity(
                userId = 1, mealDate = today, mealTime = "07:30:00",
                mealType = "breakfast", foodName = "燕麦粥 + 水煮鸡蛋",
                sugarContent = 3.0, calories = 280.0,
                protein = 18.0, fat = 12.0, carbohydrate = 25.0,
                imageUrl = "https://images.unsplash.com/photo-1517673400267-0251440c45dc?w=400&h=300&fit=crop",
                portionSize = "燕麦粥200g + 鸡蛋2个", createdAt = now()),
            MealRecordEntity(
                userId = 1, mealDate = today, mealTime = "12:00:00",
                mealType = "lunch", foodName = "鸡胸肉沙拉",
                sugarContent = 5.0, calories = 350.0,
                protein = 35.0, fat = 10.0, carbohydrate = 20.0,
                imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&h=300&fit=crop",
                portionSize = "鸡胸肉150g + 蔬菜200g", createdAt = now()),
            MealRecordEntity(
                userId = 1, mealDate = today, mealTime = "15:00:00",
                mealType = "snack", foodName = "蓝莓 + 核桃",
                sugarContent = 12.0, calories = 180.0,
                protein = 5.0, fat = 15.0, carbohydrate = 16.0,
                imageUrl = "https://images.unsplash.com/photo-1498557850523-fd3d118b962e?w=400&h=300&fit=crop",
                portionSize = "蓝莓100g + 核桃30g", createdAt = now())
        )
        for (m in meals) {
            db.mealRecordDao().insert(m)
        }
    }
}
