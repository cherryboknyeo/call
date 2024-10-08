package com.example.photothis;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitInstance(String apiKey) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("Authorization", "Bearer " + "abc")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 연결 타임아웃
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)   // 쓰기 타임아웃
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)    // 읽기 타임아웃
                    .build();


            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.openai.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

}
