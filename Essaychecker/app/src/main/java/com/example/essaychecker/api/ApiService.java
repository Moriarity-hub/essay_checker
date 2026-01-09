package com.example.essaychecker.api;

import com.example.essaychecker.model.CorrectionResponse;
import com.example.essaychecker.model.EssayRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers({"Content-Type: application/json"})
    @POST("correct_essay")
    Call<CorrectionResponse> correctEssay(@Body EssayRequest request);
}