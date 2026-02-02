package com.tusher.boiwatch.api;

import com.tusher.boiwatch.Constants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit tmdbRetrofit = null;
    private static Retrofit groqRetrofit = null;

    public static TMDBApi getApi() {
        if (tmdbRetrofit == null) {
            tmdbRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.TMDB_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return tmdbRetrofit.create(TMDBApi.class);
    }

    public static GroqApiService getGroqApi() {
        if (groqRetrofit == null) {
            groqRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.GROQ_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return groqRetrofit.create(GroqApiService.class);
    }
}
