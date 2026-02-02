package com.tusher.boiwatch.api;

import com.tusher.boiwatch.models.ChatRequest;
import com.tusher.boiwatch.models.ChatResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface GroqApiService {
    @POST("chat/completions")
    Call<ChatResponse> getChatCompletion(
            @Header("Authorization") String authHeader,
            @Body ChatRequest request
    );

    @Streaming
    @POST("chat/completions")
    Call<ResponseBody> streamChatCompletion(
            @Header("Authorization") String authorization,
            @Body ChatRequest request
    );
}
