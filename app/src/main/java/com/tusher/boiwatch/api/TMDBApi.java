package com.tusher.boiwatch.api;

import com.tusher.boiwatch.models.Movie;
import com.tusher.boiwatch.models.MovieResponse;
import com.tusher.boiwatch.models.TVDetailResponse;
import com.tusher.boiwatch.models.EpisodeResponse;
import com.tusher.boiwatch.models.ImageResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import java.util.Map;

public interface TMDBApi {
    @GET("trending/all/day")
    Call<MovieResponse> getTrendingAll(@Header("Authorization") String token);

    @GET("trending/movie/day")
    Call<MovieResponse> getTrendingMovies(@Header("Authorization") String token);

    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Header("Authorization") String token);

    @GET("tv/popular")
    Call<MovieResponse> getPopularTV(@Header("Authorization") String token);

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Header("Authorization") String token);

    @GET("tv/top_rated")
    Call<MovieResponse> getTopRatedTV(@Header("Authorization") String token);

    @GET("discover/movie")
    Call<MovieResponse> discoverMovie(
        @Header("Authorization") String token,
        @QueryMap Map<String, String> options
    );
    
    @GET("discover/tv")
    Call<MovieResponse> discoverTV(
        @Header("Authorization") String token,
        @QueryMap Map<String, String> options
    );

    @GET("search/movie")
    Call<MovieResponse> searchMovies(
        @Header("Authorization") String token,
        @Query("query") String query
    );

    @GET("search/tv")
    Call<MovieResponse> searchTV(
        @Header("Authorization") String token,
        @Query("query") String query
    );

    @GET("search/multi")
    Call<MovieResponse> searchMulti(
        @Header("Authorization") String token,
        @Query("query") String query
    );

    @GET("movie/{movie_id}")
    Call<Movie> getMovieDetails(
        @Header("Authorization") String token,
        @Path("movie_id") int movieId
    );
    
    @GET("movie/{movie_id}/credits")
    Call<com.tusher.boiwatch.models.CreditsResponse> getMovieCredits(
        @Header("Authorization") String token,
        @Path("movie_id") int movieId
    );
    
    @GET("movie/{movie_id}/videos")
    Call<com.tusher.boiwatch.models.VideosResponse> getMovieVideos(
        @Header("Authorization") String token,
        @Path("movie_id") int movieId
    );

    @GET("movie/{movie_id}/images")
    Call<ImageResponse> getMovieImages(
        @Header("Authorization") String token,
        @Path("movie_id") int movieId
    );

    @GET("tv/{tv_id}")
    Call<TVDetailResponse> getTVDetails(
        @Header("Authorization") String token,
        @Path("tv_id") int tvId
    );

    @GET("tv/{tv_id}/season/{season_number}")
    Call<EpisodeResponse> getSeasonEpisodes(
        @Header("Authorization") String token,
        @Path("tv_id") int tvId,
        @Path("season_number") int seasonNumber
    );
    
    @GET("tv/{tv_id}/credits")
    Call<com.tusher.boiwatch.models.CreditsResponse> getTVCredits(
        @Header("Authorization") String token,
        @Path("tv_id") int tvId
    );

    @GET("tv/{tv_id}/images")
    Call<ImageResponse> getTVImages(
        @Header("Authorization") String token,
        @Path("tv_id") int tvId
    );

    @GET("collection/{collection_id}")
    Call<com.tusher.boiwatch.models.CollectionResponse> getCollectionDetails(
        @Header("Authorization") String token,
        @Path("collection_id") int collectionId
    );
}
