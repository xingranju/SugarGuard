package com.example.myapplication.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocalFoodRecognizer {

    private val foodKeywordMap = mapOf(
        "Food" to listOf("白米饭", "面条", "包子", "饺子"),
        "Drink" to listOf("绿茶", "纯牛奶", "椰子水"),
        "Fruit" to listOf("苹果", "香蕉", "橙子", "草莓", "蓝莓", "葡萄", "西瓜", "芒果"),
        "Vegetable" to listOf("西兰花", "番茄", "胡萝卜", "菠菜"),
        "Meat" to listOf("鸡胸肉", "牛里脊", "猪里脊", "排骨"),
        "Seafood" to listOf("三文鱼", "虾仁", "鲈鱼", "带鱼"),
        "Dessert" to listOf("巧克力蛋糕", "冰淇淋", "提拉米苏", "蛋挞"),
        "Baked goods" to listOf("全麦面包", "馒头", "烧饼", "蛋黄酥"),
        "Fast food" to listOf("汉堡", "薯条", "披萨", "炸鸡腿"),
        "Salad" to listOf("凯撒沙拉", "鸡肉沙拉", "水果沙拉"),
        "Soup" to listOf("西红柿蛋汤", "酸辣汤", "鸡汤"),
        "Rice" to listOf("白米饭", "糙米饭", "煲仔饭"),
        "Noodle" to listOf("兰州拉面", "意大利面", "炒面"),
        "Bread" to listOf("全麦面包", "吐司配果酱", "牛角包"),
        "Cake" to listOf("巧克力蛋糕", "芝士蛋糕", "奶油蛋糕"),
        "Ice cream" to listOf("冰淇淋"),
        "Pizza" to listOf("披萨"),
        "Sandwich" to listOf("三明治"),
        "Sushi" to listOf("寿司(鲑鱼)", "寿司(金枪鱼)", "刺身拼盘"),
        "Coffee" to listOf("美式咖啡", "拿铁咖啡", "卡布奇诺"),
        "Tea" to listOf("绿茶", "红茶", "乌龙茶", "茉莉花茶"),
        "Juice" to listOf("鲜榨橙汁", "西瓜汁", "苹果汁"),
        "Milk" to listOf("纯牛奶", "酸奶", "豆浆"),
        "Beer" to listOf("啤酒"),
        "Wine" to listOf("红酒"),
        "Chocolate" to listOf("巧克力蛋糕", "巧克力"),
        "Cookie" to listOf("饼干", "夹心饼干"),
        "Nut" to listOf("核桃", "杏仁", "花生", "腰果"),
        "Egg" to listOf("鸡蛋", "水煮鸡蛋", "蒸蛋"),
        "Cheese" to listOf("芝士蛋糕"),
        "Banana" to listOf("香蕉"),
        "Apple" to listOf("苹果"),
        "Orange" to listOf("橙子"),
        "Grape" to listOf("葡萄"),
        "Strawberry" to listOf("草莓"),
        "Watermelon" to listOf("西瓜"),
        "Mango" to listOf("芒果"),
        "Pineapple" to listOf("菠萝"),
        "Cherry" to listOf("樱桃"),
        "Lemon" to listOf("柠檬水"),
        "Tomato" to listOf("番茄"),
        "Broccoli" to listOf("西兰花"),
        "Carrot" to listOf("胡萝卜"),
        "Mushroom" to listOf("蘑菇"),
        "Onion" to listOf("洋葱"),
        "Corn" to listOf("玉米"),
        "Potato" to listOf("土豆"),
        "Sweet potato" to listOf("红薯"),
        "Pumpkin" to listOf("南瓜"),
        "Cucumber" to listOf("黄瓜"),
        "Snack" to listOf("薯片", "饼干", "果冻"),
        "Candy" to listOf("糖果"),
        "Popcorn" to listOf("爆米花"),
        "Pasta" to listOf("意大利面"),
        "Steak" to listOf("牛里脊"),
        "Chicken" to listOf("鸡胸肉", "炸鸡腿", "辣子鸡"),
        "Pork" to listOf("猪里脊", "红烧肉", "回锅肉"),
        "Fish" to listOf("三文鱼", "鲈鱼", "清蒸鲈鱼"),
        "Shrimp" to listOf("虾仁"),
        "Tofu" to listOf("豆腐", "麻婆豆腐"),
        "Dumpling" to listOf("饺子", "小笼包"),
        "Spring roll" to listOf("春卷"),
        "Cabbage" to listOf("卷心菜", "白菜"),
        "head cabbage" to listOf("卷心菜"),
        "green cabbage" to listOf("绿甘蓝"),
        "Chinese cabbage" to listOf("大白菜"),
        "Lettuce" to listOf("生菜"),
        "Spinach" to listOf("菠菜"),
        "Celery" to listOf("芹菜"),
        "Pepper" to listOf("青椒", "辣椒"),
        "Eggplant" to listOf("茄子"),
        "Bean" to listOf("豆角", "四季豆"),
        "Garlic" to listOf("大蒜"),
        "Ginger" to listOf("生姜"),
        "Green onion" to listOf("葱"),
        "Radish" to listOf("萝卜"),
        "Asparagus" to listOf("芦笋"),
        "Avocado" to listOf("牛油果"),
        "Peach" to listOf("桃子"),
        "Pear" to listOf("梨"),
        "Kiwi" to listOf("猕猴桃"),
        "Plum" to listOf("李子"),
        "Coconut" to listOf("椰子"),
        "Fig" to listOf("无花果"),
        "Pomegranate" to listOf("石榴"),
        "Blueberry" to listOf("蓝莓"),
        "Raspberry" to listOf("覆盆子"),
        "Yogurt" to listOf("酸奶"),
        "Butter" to listOf("黄油"),
        "Honey" to listOf("蜂蜜"),
        "Jam" to listOf("果酱"),
        "Donut" to listOf("甜甜圈"),
        "Waffle" to listOf("华夫饼"),
        "Pancake" to listOf("煎饼"),
        "Cereal" to listOf("麦片"),
        "Granola" to listOf("燕麦棒"),
        "Oatmeal" to listOf("燕麦粥"),
        "Taco" to listOf("玉米饼卷"),
        "Burrito" to listOf("墨西哥卷饼"),
        "Curry" to listOf("咖喱"),
        "Dim sum" to listOf("点心"),
        "Wonton" to listOf("馄饨"),
        "Fried rice" to listOf("炒饭"),
        "Hot dog" to listOf("热狗"),
        "French fries" to listOf("薯条"),
        "Hamburger" to listOf("汉堡")
    )

    suspend fun recognizeFood(bitmap: Bitmap): List<String> = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
        val labeler = ImageLabeling.getClient(options)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                val matchedFoods = mutableListOf<String>()
                for (label in labels) {
                    val key = label.text
                    foodKeywordMap[key]?.let { matchedFoods.addAll(it) }
                    foodKeywordMap.entries
                        .filter { it.key.contains(key, ignoreCase = true) || key.contains(it.key, ignoreCase = true) }
                        .forEach { matchedFoods.addAll(it.value) }
                }
                if (matchedFoods.isEmpty()) {
                    matchedFoods.addAll(listOf("白米饭", "鸡胸肉沙拉", "苹果", "绿茶", "酸奶"))
                }
                cont.resume(matchedFoods.distinct().take(10))
            }
            .addOnFailureListener {
                cont.resume(listOf("白米饭", "鸡胸肉沙拉", "苹果", "绿茶", "酸奶"))
            }
    }
}
