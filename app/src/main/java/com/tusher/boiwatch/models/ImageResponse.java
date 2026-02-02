package com.tusher.boiwatch.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ImageResponse {
    @SerializedName("backdrops")
    private List<ImageData> backdrops;

    public List<ImageData> getBackdrops() {
        return backdrops;
    }

    public static class ImageData {
        @SerializedName("file_path")
        private String filePath;

        public String getFilePath() {
            return "https://image.tmdb.org/t/p/w500" + filePath;
        }
    }
}
