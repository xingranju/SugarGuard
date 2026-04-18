package com.example.myapplication.api;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Retrofit客户端单例
 * 用于HTTP网络请求
 */
public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // Android模拟器访问本机

    /** 供图片 URL 拼接等使用（与 BASE_URL 一致） */
    public static String getBaseUrl() {
        return BASE_URL;
    }
    // 真机测试时使用: "http://你的电脑IP:8080/"
    
    private static RetrofitClient instance = null;
    private final Retrofit retrofit;
    private final AuthApiService authApiService;
    private final AIApiService aiApiService;
    private final MealApiService mealApiService;
    private final UserProfileApiService userProfileApiService;
    private final DrinkPreferenceApiService drinkPreferenceApiService;
    private final DailyHealthRecordApiService dailyHealthRecordApiService;
    private final UserInfoApiService userInfoApiService;
    private final DrinkApiService drinkApiService;
    private final ConversationHistoryApiService conversationHistoryApiService;
    private final FoodRecognitionApiService foodRecognitionApiService;
    private final NotificationApiService notificationApiService;
    private final NotificationSettingsApiService notificationSettingsApiService;
    private final ReportApiService reportApiService;
    private static Context appContext;
    
    private RetrofitClient() {
        // 创建OkHttpClient
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);
        
        // 添加JWT拦截器
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                
                // 从SharedPreferences获取token
                String token = getToken();
                
                Request.Builder requestBuilder = original.newBuilder();
                
                // 如果有token,添加到请求头
                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }
                
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        
        // 添加日志拦截器(便于调试)
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);
        
        // 创建Retrofit实例
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        
        // 创建API服务
        authApiService = retrofit.create(AuthApiService.class);
        aiApiService = retrofit.create(AIApiService.class);
        mealApiService = retrofit.create(MealApiService.class);
        userProfileApiService = retrofit.create(UserProfileApiService.class);
        drinkPreferenceApiService = retrofit.create(DrinkPreferenceApiService.class);
        dailyHealthRecordApiService = retrofit.create(DailyHealthRecordApiService.class);
        userInfoApiService = retrofit.create(UserInfoApiService.class);
        drinkApiService = retrofit.create(DrinkApiService.class);
        conversationHistoryApiService = retrofit.create(ConversationHistoryApiService.class);
        foodRecognitionApiService = retrofit.create(FoodRecognitionApiService.class);
        notificationApiService = retrofit.create(NotificationApiService.class);
        notificationSettingsApiService = retrofit.create(NotificationSettingsApiService.class);
        reportApiService = retrofit.create(ReportApiService.class);
    }
    
    /**
     * 初始化应用上下文(在Application或第一个Activity中调用)
     * @param context 应用上下文
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }
    
    /**
     * 获取RetrofitClient单例
     * @return RetrofitClient实例
     */
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }
    
    public AuthApiService getAuthApi() {
        return authApiService;
    }

    public static AuthApiService getAuthApiService() {
        return getInstance().getAuthApi();
    }

    /**
     * 获取AI API服务(实例方法)
     * @return AIApiService实例
     */
    public AIApiService getAIApi() {
        return aiApiService;
    }
    
    /**
     * 获取AI API服务(静态方法)
     * @return AIApiService实例
     */
    public static AIApiService getAIApiService() {
        return getInstance().getAIApi();
    }
    
    /**
     * 获取Meal API服务(实例方法)
     * @return MealApiService实例
     */
    public MealApiService getMealApi() {
        return mealApiService;
    }
    
    /**
     * 获取Meal API服务(静态方法)
     * @return MealApiService实例
     */
    public static MealApiService getMealApiService() {
        return getInstance().getMealApi();
    }
    
    /**
     * 获取UserProfile API服务(实例方法)
     * @return UserProfileApiService实例
     */
    public UserProfileApiService getUserProfileApi() {
        return userProfileApiService;
    }
    
    /**
     * 获取UserProfile API服务(静态方法)
     * @return UserProfileApiService实例
     */
    public static UserProfileApiService getUserProfileApiService() {
        return getInstance().getUserProfileApi();
    }
    
    /**
     * 获取DrinkPreference API服务(实例方法)
     * @return DrinkPreferenceApiService实例
     */
    public DrinkPreferenceApiService getDrinkPreferenceApi() {
        return drinkPreferenceApiService;
    }
    
    /**
     * 获取DrinkPreference API服务(静态方法)
     * @return DrinkPreferenceApiService实例
     */
    public static DrinkPreferenceApiService getDrinkPreferenceApiService() {
        return getInstance().getDrinkPreferenceApi();
    }
    
    /**
     * 获取DailyHealthRecord API服务(实例方法)
     * @return DailyHealthRecordApiService实例
     */
    public DailyHealthRecordApiService getDailyHealthRecordApi() {
        return dailyHealthRecordApiService;
    }
    
    /**
     * 获取DailyHealthRecord API服务(静态方法)
     * @return DailyHealthRecordApiService实例
     */
    public static DailyHealthRecordApiService getDailyHealthRecordApiService() {
        return getInstance().getDailyHealthRecordApi();
    }
    
    /**
     * 获取UserInfo API服务(实例方法)
     * @return UserInfoApiService实例
     */
    public UserInfoApiService getUserInfoApi() {
        return userInfoApiService;
    }
    
    /**
     * 获取UserInfo API服务(静态方法)
     * @return UserInfoApiService实例
     */
    public static UserInfoApiService getUserInfoApiService() {
        return getInstance().getUserInfoApi();
    }
    
    /**
     * 获取Drink API服务(实例方法)
     * @return DrinkApiService实例
     */
    public DrinkApiService getDrinkApi() {
        return drinkApiService;
    }
    
    /**
     * 获取Drink API服务(静态方法)
     * @return DrinkApiService实例
     */
    public static DrinkApiService getDrinkApiService() {
        return getInstance().getDrinkApi();
    }
    
    /**
     * 获取ConversationHistory API服务(实例方法)
     * @return ConversationHistoryApiService实例
     */
    public ConversationHistoryApiService getConversationHistoryApi() {
        return conversationHistoryApiService;
    }
    
    /**
     * 获取ConversationHistory API服务(静态方法)
     * @return ConversationHistoryApiService实例
     */
    public static ConversationHistoryApiService getConversationHistoryApiService() {
        return getInstance().getConversationHistoryApi();
    }

    public FoodRecognitionApiService getFoodRecognitionApi() {
        return foodRecognitionApiService;
    }

    public static FoodRecognitionApiService getFoodRecognitionApiService() {
        return getInstance().getFoodRecognitionApi();
    }

    public NotificationApiService getNotificationApi() {
        return notificationApiService;
    }

    public static NotificationApiService getNotificationApiService() {
        return getInstance().getNotificationApi();
    }

    public NotificationSettingsApiService getNotificationSettingsApi() {
        return notificationSettingsApiService;
    }

    public static NotificationSettingsApiService getNotificationSettingsApiService() {
        return getInstance().getNotificationSettingsApi();
    }

    public ReportApiService getReportApi() {
        return reportApiService;
    }

    public static ReportApiService getReportApiService() {
        return getInstance().getReportApi();
    }
    
    /**
     * 从SharedPreferences获取JWT token
     * @return JWT token
     */
    private String getToken() {
        if (appContext == null) {
            return null;
        }
        
        // 使用与AuthRepository相同的SharedPreferences名称和键
        SharedPreferences prefs = appContext.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        return prefs.getString("access_token", null);
    }
}

