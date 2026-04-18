package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.AddMealRequest
import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.MealRecord
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalMealViewModel : ViewModel() {
    private val TAG = "LocalMealViewModel"
    private val mealApi = RetrofitClient.getMealApiService()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _dailyMeals = MutableLiveData<List<MealRecord>>(emptyList())
    val dailyMeals: LiveData<List<MealRecord>> = _dailyMeals

    private val _selectedDate = MutableLiveData(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    private val _dailySugarTotal = MutableLiveData(0.0)
    val dailySugarTotal: LiveData<Double> = _dailySugarTotal

    private val _dailyCaloriesTotal = MutableLiveData(0.0)
    val dailyCaloriesTotal: LiveData<Double> = _dailyCaloriesTotal

    private val _addMealSuccess = MutableLiveData(false)
    val addMealSuccess: LiveData<Boolean> = _addMealSuccess

    fun resetAddState() {
        _addMealSuccess.value = false
        _errorMessage.value = ""
    }

    fun getDailyMeals(userId: Int, date: LocalDate) {
        _isLoading.value = true
        val dateStr = date.toString()

        mealApi.getDailyMeals(userId, dateStr).enqueue(object : Callback<ApiResponse<Map<String, Any>>> {
            override fun onResponse(
                call: Call<ApiResponse<Map<String, Any>>>,
                response: Response<ApiResponse<Map<String, Any>>>
            ) {
                _isLoading.postValue(false)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val data = response.body()!!.data ?: return
                    val mealsList = parseMealsFromResponse(data, userId)
                    _dailyMeals.postValue(mealsList)

                    _dailySugarTotal.postValue(mealsList.sumOf { it.sugarContent })
                    _dailyCaloriesTotal.postValue(mealsList.sumOf { it.calories })
                    Log.d(TAG, "加载饮食记录成功: $date, 记录数: ${mealsList.size}")
                } else {
                    _dailyMeals.postValue(emptyList())
                    _dailySugarTotal.postValue(0.0)
                    _dailyCaloriesTotal.postValue(0.0)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Map<String, Any>>>, t: Throwable) {
                _isLoading.postValue(false)
                _errorMessage.postValue("网络错误: ${t.message}")
                _dailyMeals.postValue(emptyList())
                Log.e(TAG, "加载饮食记录网络错误", t)
            }
        })
    }

    private val recentAddedKeys = mutableMapOf<String, Long>()

    fun addMeal(
        userId: Int, foodName: String, sugarContent: Double, calories: Double,
        protein: Double?, fat: Double?, carbohydrate: Double?,
        portionSize: String?, notes: String?, mealType: String, imageUrl: String?
    ) {
        val currentMs = System.currentTimeMillis()
        recentAddedKeys.entries.removeIf { currentMs - it.value > 120_000 }

        val dedupKey = "${foodName.trim()}_${mealType}"
        val crossMealKey = "${foodName.trim()}_ANY"
        if (recentAddedKeys.containsKey(dedupKey)) {
            Log.w(TAG, "重复添加拦截(近期已添加): $foodName ($mealType)")
            _errorMessage.postValue("「${foodName}」刚刚已添加到${getMealLabel(mealType)}，请勿重复提交")
            return
        }
        if (recentAddedKeys.containsKey(crossMealKey)) {
            Log.w(TAG, "跨餐次重复拦截(10分钟内同食物): $foodName ($mealType)")
            _errorMessage.postValue("「${foodName}」刚刚已添加，请勿重复提交")
            return
        }

        val existingDup = _dailyMeals.value?.any {
            it.foodName.trim().equals(foodName.trim(), ignoreCase = true) && it.mealType == mealType
        } == true
        if (existingDup) {
            Log.w(TAG, "同名同餐次已存在，拦截: $foodName ($mealType)")
            _errorMessage.postValue("今日${getMealLabel(mealType)}已记录「${foodName}」，不可重复添加")
            return
        }

        recentAddedKeys[dedupKey] = currentMs
        recentAddedKeys[crossMealKey] = currentMs

        val localFile = imageUrl?.let { p ->
            val f = File(p)
            if (f.exists() && f.isFile) f else null
        }
        if (localFile != null) {
            addMealWithImageMultipart(
                userId, foodName, sugarContent, calories, protein, fat, carbohydrate,
                portionSize, notes, mealType, localFile
            )
            return
        }

        _isLoading.value = true
        val now = LocalDate.now()
        val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val request = AddMealRequest(
            userId = userId,
            mealDate = now.toString(),
            mealTime = time,
            mealType = mealType,
            foodName = foodName,
            sugarContent = sugarContent,
            calories = calories,
            protein = protein,
            fat = fat,
            carbohydrate = carbohydrate,
            portionSize = portionSize,
            notes = notes,
            imagePath = imageUrl
        )

        mealApi.addMeal(request).enqueue(object : Callback<ApiResponse<MealRecord>> {
            override fun onResponse(
                call: Call<ApiResponse<MealRecord>>,
                response: Response<ApiResponse<MealRecord>>
            ) {
                _isLoading.postValue(false)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _errorMessage.postValue("添加成功!")
                    _addMealSuccess.postValue(true)
                    getDailyMeals(userId, _selectedDate.value ?: LocalDate.now())
                } else {
                    _addMealSuccess.postValue(false)
                    _errorMessage.postValue("添加失败: ${response.body()?.message ?: "未知错误"}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<MealRecord>>, t: Throwable) {
                _isLoading.postValue(false)
                _addMealSuccess.postValue(false)
                _errorMessage.postValue("网络错误: ${t.message}")
            }
        })
    }

    private fun txt(s: String) = s.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())

    private fun addMealWithImageMultipart(
        userId: Int,
        foodName: String,
        sugarContent: Double,
        calories: Double,
        protein: Double?,
        fat: Double?,
        carbohydrate: Double?,
        portionSize: String?,
        notes: String?,
        mealType: String,
        imageFile: File
    ) {
        _isLoading.value = true
        val pText = portionSize.orEmpty()
        val portionFloat = pText.replace(Regex("[^0-9.]"), "").toFloatOrNull()
            ?: when {
                pText.contains("半", true) || pText.equals("half", true) -> 0.5f
                else -> 1f
            }

        val sugarBody = txt(sugarContent.toString())
        val calBody = txt(calories.toString())
        val proteinBody = protein?.let { txt(it.toString()) }
        val fatBody = fat?.let { txt(it.toString()) }
        val carbBody = carbohydrate?.let { txt(it.toString()) }
        val portionBody = txt(portionFloat.toString())
        val notesBody = txt(notes ?: "")
        val mealTypeBody = txt(mealType)
        val userIdBody = txt(userId.toString())

        val imagePart = MultipartBody.Part.createFormData(
            "image",
            imageFile.name,
            imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        )

        mealApi.addMealWithImage(
            userId = userIdBody,
            foodName = txt(foodName),
            sugarContent = sugarBody,
            calories = calBody,
            protein = proteinBody,
            fat = fatBody,
            carbohydrate = carbBody,
            portionSize = portionBody,
            notes = notesBody,
            mealType = mealTypeBody,
            image = imagePart
        ).enqueue(object : Callback<ApiResponse<Map<String, Any>>> {
            override fun onResponse(
                call: Call<ApiResponse<Map<String, Any>>>,
                response: Response<ApiResponse<Map<String, Any>>>
            ) {
                _isLoading.postValue(false)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _errorMessage.postValue("添加成功!")
                    _addMealSuccess.postValue(true)
                    getDailyMeals(userId, _selectedDate.value ?: LocalDate.now())
                } else {
                    _addMealSuccess.postValue(false)
                    _errorMessage.postValue("添加失败: ${response.body()?.message ?: "未知错误"}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<Map<String, Any>>>, t: Throwable) {
                _isLoading.postValue(false)
                _addMealSuccess.postValue(false)
                _errorMessage.postValue("网络错误: ${t.message}")
            }
        })
    }

    fun deleteMeal(userId: Int, mealId: Int) {
        _isLoading.value = true
        mealApi.deleteMeal(mealId, userId).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(
                call: Call<ApiResponse<String>>,
                response: Response<ApiResponse<String>>
            ) {
                _isLoading.postValue(false)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _errorMessage.postValue("删除成功")
                    getDailyMeals(userId, _selectedDate.value ?: LocalDate.now())
                } else {
                    _errorMessage.postValue("删除失败: ${response.body()?.message ?: "未知错误"}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                _isLoading.postValue(false)
                _errorMessage.postValue("网络错误: ${t.message}")
            }
        })
    }

    fun selectDate(date: LocalDate, userId: Int) {
        _selectedDate.value = date
        getDailyMeals(userId, date)
    }

    private fun getMealLabel(type: String) = when (type) {
        "breakfast" -> "早餐"; "lunch" -> "午餐"; "dinner" -> "晚餐"; "snack" -> "加餐"; else -> "其他"
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseMealsFromResponse(data: Map<String, Any>, userId: Int): List<MealRecord> {
        val mealsObj = data["meals"] as? List<*> ?: return emptyList()
        return mealsObj.mapNotNull { item ->
            val map = item as? Map<String, Any?> ?: return@mapNotNull null
            MealRecord(
                mealId = (map["mealId"] as? Number ?: map["meal_id"] as? Number)?.toInt(),
                userId = (map["userId"] as? Number ?: map["user_id"] as? Number)?.toInt() ?: userId,
                mealDate = map["mealDate"] as? String ?: map["meal_date"] as? String ?: "",
                mealTime = map["mealTime"] as? String ?: map["meal_time"] as? String ?: "",
                mealType = map["mealType"] as? String ?: map["meal_type"] as? String ?: "snack",
                drinkId = (map["drinkId"] as? Number ?: map["drink_id"] as? Number)?.toInt(),
                foodName = map["foodName"] as? String ?: map["food_name"] as? String ?: "unknown",
                foodImagePath = map["imagePath"] as? String ?: map["image_path"] as? String,
                sugarContent = (map["sugarContent"] as? Number ?: map["sugar_content"] as? Number)?.toDouble() ?: 0.0,
                calories = (map["calories"] as? Number)?.toDouble() ?: 0.0,
                protein = (map["protein"] as? Number)?.toDouble(),
                fat = (map["fat"] as? Number)?.toDouble(),
                carbohydrate = (map["carbohydrate"] as? Number)?.toDouble(),
                portionSize = map["portionSize"] as? String ?: map["portion_size"] as? String,
                notes = map["notes"] as? String,
                createdAt = map["createdAt"] as? String ?: map["created_at"] as? String
            )
        }
    }
}
