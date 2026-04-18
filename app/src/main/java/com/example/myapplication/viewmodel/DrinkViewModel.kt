package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.AddDrinkRecordRequest
import com.example.myapplication.model.ApiResponse
import com.example.myapplication.model.Drink
import com.example.myapplication.model.MealRecord
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DrinkViewModel : ViewModel() {

    private val TAG = "DrinkViewModel"
    private val drinkApi = RetrofitClient.getDrinkApiService()

    private val _drinks = MutableLiveData<List<Drink>>()
    val drinks: LiveData<List<Drink>> = _drinks

    private val _brands = MutableLiveData<List<String>>()
    val brands: LiveData<List<String>> = _brands

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _selectedDrink = MutableLiveData<Drink?>()
    val selectedDrink: LiveData<Drink?> = _selectedDrink

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun getAllDrinks() {
        _isLoading.value = true
        drinkApi.getAllDrinks().enqueue(object : Callback<ApiResponse<List<Drink>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Drink>>>,
                response: Response<ApiResponse<List<Drink>>>
            ) {
                _isLoading.postValue(false)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _drinks.postValue(response.body()!!.data ?: emptyList())
                    Log.d(TAG, "获取饮品列表成功: ${response.body()!!.data?.size ?: 0} 条")
                } else {
                    _errorMessage.postValue("加载饮品失败")
                    _drinks.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Drink>>>, t: Throwable) {
                _isLoading.postValue(false)
                _errorMessage.postValue("网络错误: ${t.message}")
                _drinks.postValue(emptyList())
            }
        })
    }

    fun searchDrinks(keyword: String? = null, brand: String? = null, category: String? = null) {
        _isLoading.value = true
        val hasFilter = !keyword.isNullOrBlank() || !brand.isNullOrBlank() || !category.isNullOrBlank()

        if (!hasFilter) {
            getAllDrinks()
            return
        }

        drinkApi.searchDrinks(keyword, brand, category)
            .enqueue(object : Callback<ApiResponse<List<Drink>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Drink>>>,
                    response: Response<ApiResponse<List<Drink>>>
                ) {
                    _isLoading.postValue(false)
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        _drinks.postValue(response.body()!!.data ?: emptyList())
                    } else {
                        _errorMessage.postValue("搜索失败")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<Drink>>>, t: Throwable) {
                    _isLoading.postValue(false)
                    _errorMessage.postValue("网络错误: ${t.message}")
                }
            })
    }

    fun getAllBrands() {
        drinkApi.getAllBrands().enqueue(object : Callback<ApiResponse<List<String>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<String>>>,
                response: Response<ApiResponse<List<String>>>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _brands.postValue(response.body()!!.data ?: emptyList())
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<String>>>, t: Throwable) {
                Log.e(TAG, "获取品牌列表失败", t)
            }
        })
    }

    fun getAllCategories() {
        drinkApi.getAllCategories().enqueue(object : Callback<ApiResponse<List<String>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<String>>>,
                response: Response<ApiResponse<List<String>>>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _categories.postValue(response.body()!!.data ?: emptyList())
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<String>>>, t: Throwable) {
                Log.e(TAG, "获取类别列表失败", t)
            }
        })
    }

    fun addDrinkRecord(
        userId: Long, drinkId: Int, mealType: String,
        portionSize: Float? = null, notes: String? = null
    ) {
        _isLoading.value = true
        val request = AddDrinkRecordRequest(
            userId = userId,
            drinkId = drinkId,
            mealType = mealType,
            portionSize = portionSize,
            notes = notes
        )
        drinkApi.addDrinkRecord(request).enqueue(object : Callback<ApiResponse<MealRecord>> {
            override fun onResponse(
                call: Call<ApiResponse<MealRecord>>,
                response: Response<ApiResponse<MealRecord>>
            ) {
                _isLoading.postValue(false)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _successMessage.postValue("添加饮品记录成功")
                } else {
                    _errorMessage.postValue("添加失败: ${response.body()?.message ?: "未知错误"}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<MealRecord>>, t: Throwable) {
                _isLoading.postValue(false)
                _errorMessage.postValue("网络错误: ${t.message}")
            }
        })
    }

    fun selectDrink(drink: Drink?) {
        _selectedDrink.value = drink
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
