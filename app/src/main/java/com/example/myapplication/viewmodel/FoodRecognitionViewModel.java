package com.example.myapplication.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapplication.data.model.FoodItem;
import com.example.myapplication.data.repository.FoodRecognitionRepository;

/**
 * 食物识别ViewModel
 * shiWu_shibie_ViewModel
 */
public class FoodRecognitionViewModel extends AndroidViewModel {
    private FoodRecognitionRepository repository;
    
    public FoodRecognitionViewModel(@NonNull Application application) {
        super(application);
        repository = new FoodRecognitionRepository(application);
    }
    
    /**
     * 识别食物
     * shibie_shiWu
     */
    public void recognizeFood(Uri imageUri) {
        repository.recognizeFood(imageUri);
    }
    
    // LiveData Getters
    public LiveData<FoodItem> getRecognitionResult() {
        return repository.getRecognitionResult();
    }
    
    public LiveData<String> getErrorMessage() {
        return repository.getErrorMessage();
    }
    
    public LiveData<Boolean> getIsLoading() {
        return repository.getIsLoading();
    }
}

