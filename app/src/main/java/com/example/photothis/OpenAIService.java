package com.example.photothis;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OpenAIService {
    @POST("v1/chat/completions")
    Call<ChatCompletionResponse> getChatCompletion(@Body ChatCompletionRequest request);
}
