package com.tusher.boiwatch.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Movie implements Serializable {
    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("name")
    private String name; // for TV shows
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("backdrop_path")
    private String backdropPath;
    @SerializedName("overview")
    private String overview;
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("first_air_date")
    private String firstAirDate;
    @SerializedName("genre_ids")
    private List<Integer> genreIds;
    @SerializedName("media_type")
    private String mediaType;
    @SerializedName("belongs_to_collection")
    private BelongsToCollection belongsToCollection;

    @SerializedName("original_language")
    private String originalLanguage;

    @SerializedName("production_countries")
    private List<ProductionCountry> productionCountries;

    @SerializedName("origin_country")
    private List<String> originCountry;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title != null ? title : name; }
    public void setTitle(String title) { this.title = title; }
    
    public String getPosterPath() { 
        if (posterPath != null && !posterPath.startsWith("http")) {
            return "https://image.tmdb.org/t/p/w500" + posterPath;
        }
        return posterPath;
    }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    
    public String getBackdropPath() { return "https://image.tmdb.org/t/p/w1280" + backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }
    
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    
    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }
    
    public String getReleaseDate() { return releaseDate != null ? releaseDate : firstAirDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    
    public BelongsToCollection getBelongsToCollection() { return belongsToCollection; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public List<ProductionCountry> getProductionCountries() { return productionCountries; }
    public void setProductionCountries(List<ProductionCountry> productionCountries) { this.productionCountries = productionCountries; }

    public List<String> getOriginCountry() { return originCountry; }
    public void setOriginCountry(List<String> originCountry) { this.originCountry = originCountry; }

    public static class ProductionCountry implements Serializable {
        @SerializedName("iso_3166_1")
        private String iso31661;
        @SerializedName("name")
        private String name;

        public String getIso31661() { return iso31661; }
        public void setIso31661(String iso31661) { this.iso31661 = iso31661; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
