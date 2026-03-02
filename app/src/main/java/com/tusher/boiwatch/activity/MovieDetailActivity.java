package com.tusher.boiwatch.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.tusher.boiwatch.Constants;
import com.tusher.boiwatch.MainActivity;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.adapter.CastAdapter;
import com.tusher.boiwatch.adapter.EpisodeAdapter;
import com.tusher.boiwatch.adapter.PhotoAdapter;
import com.tusher.boiwatch.api.RetrofitClient;
import com.tusher.boiwatch.models.CreditsResponse;
import com.tusher.boiwatch.models.Crew;
import com.tusher.boiwatch.models.Episode;
import com.tusher.boiwatch.models.EpisodeResponse;
import com.tusher.boiwatch.models.ImageResponse;
import com.tusher.boiwatch.models.Movie;
import com.tusher.boiwatch.models.Season;
import com.tusher.boiwatch.models.TVDetailResponse;
import com.tusher.boiwatch.models.Video;
import com.tusher.boiwatch.models.VideosResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    private Movie movie;
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private RecyclerView rvCast, rvEpisodes, rvPhotos;
    private View btnTrailer, btnPlay; // Changed from MaterialButton to View
    private MaterialButton btnSeasonPicker;
    private TextView tvCrewList, tvYear;
    private RelativeLayout rlDirectorSection;
    private LinearLayout llEpisodesSection;
    private NestedScrollView scrollView;
    private List<Season> seasons = new ArrayList<>();
    private boolean isTVShow = false;
    private int currentSeasonNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        movie = (Movie) getIntent().getSerializableExtra("movie");
        if (movie == null) {
            finish();
            return;
        }

        prefs = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

        initViews();
        fetchCredits();
        fetchVideos();
        fetchPhotos();
        detectMediaType();
        saveToHistory();
    }

    private void initViews() {
        ImageView ivBackdrop = findViewById(R.id.iv_detail_backdrop);
        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvRating = findViewById(R.id.tv_detail_rating);
        TextView tvOverview = findViewById(R.id.tv_detail_overview);
        tvYear = findViewById(R.id.tv_detail_year);
        btnPlay = findViewById(R.id.fab_play);
        
        rvCast = findViewById(R.id.rv_cast);
        rvEpisodes = findViewById(R.id.rv_episodes);
        rvPhotos = findViewById(R.id.rv_photos);
        btnTrailer = findViewById(R.id.btn_play_trailer);
        btnSeasonPicker = findViewById(R.id.btn_season_picker);
        tvCrewList = findViewById(R.id.tv_crew_list);
        rlDirectorSection = findViewById(R.id.rl_director_section);
        llEpisodesSection = findViewById(R.id.ll_episodes_section);
        scrollView = findViewById(R.id.detail_scroll_view);

        tvTitle.setText(movie.getTitle());
        tvRating.setText("⭐ " + String.format("%.1f", movie.getVoteAverage()));
        tvOverview.setText(movie.getOverview());
        
        if (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4) {
            tvYear.setText(movie.getReleaseDate().substring(0, 4));
        }

        Picasso.get().load(movie.getBackdropPath()).into(ivBackdrop);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        btnPlay.setOnClickListener(v -> {
            if (isTVShow) {
                scrollView.smoothScrollTo(0, llEpisodesSection.getTop());
            } else {
                showAdsWarning(null);
            }
        });

        btnTrailer.setEnabled(false); 
        btnSeasonPicker.setOnClickListener(v -> showSeasonPickerDialog());

        MaterialButton btnWatchlist = findViewById(R.id.btn_watchlist);
        updateWatchlistIcon(btnWatchlist);

        btnWatchlist.setOnClickListener(v -> {
            toggleWatchlist();
            updateWatchlistIcon(btnWatchlist);
        });

        setupNavigation();
    }

    private void updateWatchlistIcon(MaterialButton btn) {
        if (isInWatchlist()) {
            btn.setIconTint(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3B82F6")));
        } else {
            btn.setIconTint(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F8FAFC")));
        }
    }

    private boolean isInWatchlist() {
        List<Movie> watchlist = getList(Constants.KEY_WATCHLIST);
        for (Movie m : watchlist) {
            if (m.getId().equals(movie.getId())) return true;
        }
        return false;
    }

    private void toggleWatchlist() {
        List<Movie> watchlist = getList(Constants.KEY_WATCHLIST);
        boolean exists = false;
        
        for (int i = 0; i < watchlist.size(); i++) {
            if (watchlist.get(i).getId().equals(movie.getId())) {
                watchlist.remove(i);
                exists = true;
                break;
            }
        }

        if (!exists) {
            watchlist.add(0, movie);
            Toast.makeText(this, "Added to Watchlist", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Removed from Watchlist", Toast.LENGTH_SHORT).show();
        }

        saveList(Constants.KEY_WATCHLIST, watchlist);
    }

    private void setupNavigation() {
        com.google.android.material.bottomnavigation.BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setSelectedItemId(-1); // Don't select any by default since we're in detail
        
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            
            if (id == R.id.nav_home) {
                intent.putExtra("navigate_to", "home");
            } else if (id == R.id.nav_search) {
                intent.putExtra("navigate_to", "search");
            } else if (id == R.id.nav_reels) {
                intent.putExtra("navigate_to", "reels");
            } else if (id == R.id.nav_library) {
                intent.putExtra("navigate_to", "library");
            }
            
            startActivity(intent);
            finish();
            return true;
        });
    }

    private void detectMediaType() {
        if ("tv".equals(movie.getMediaType())) {
            loadAsTVShow();
            return;
        } else if ("movie".equals(movie.getMediaType())) {
            loadAsMovie();
            return;
        }

        RetrofitClient.getApi().getTVDetails("Bearer " + Constants.TMDB_ACCESS_TOKEN, Integer.parseInt(movie.getId()))
                .enqueue(new Callback<TVDetailResponse>() {
                    @Override
                    public void onResponse(Call<TVDetailResponse> call, Response<TVDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSeasons() != null) {
                            setupTVShowUI(response.body());
                        } else {
                            loadAsMovie();
                        }
                    }
                    @Override
                    public void onFailure(Call<TVDetailResponse> call, Throwable t) {
                        loadAsMovie();
                    }
                });
    }

    private void loadAsTVShow() {
        RetrofitClient.getApi().getTVDetails("Bearer " + Constants.TMDB_ACCESS_TOKEN, Integer.parseInt(movie.getId()))
                .enqueue(new Callback<TVDetailResponse>() {
                    @Override
                    public void onResponse(Call<TVDetailResponse> call, Response<TVDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            setupTVShowUI(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<TVDetailResponse> call, Throwable t) {}
                });
    }

    private void setupTVShowUI(TVDetailResponse details) {
        isTVShow = true;
        if (btnPlay instanceof ImageView) {
            ((ImageView) btnPlay).setImageResource(R.drawable.view_episodes);
            ViewGroup.LayoutParams params = btnPlay.getLayoutParams();
            params.width = (int) (155 * getResources().getDisplayMetrics().density);
            params.height = (int) (50 * getResources().getDisplayMetrics().density);
            btnPlay.setLayoutParams(params);
        }
        seasons = details.getSeasons();
        llEpisodesSection.setVisibility(View.VISIBLE);
        if (seasons != null && !seasons.isEmpty()) {
            // Find Season 1 or the first non-zero season to start with
            Season initialSeason = seasons.get(0);
            for (Season s : seasons) {
                if (s.getSeasonNumber() == 1) {
                    initialSeason = s;
                    break;
                }
            }
            
            // If Season 1 wasn't found but there are other seasons (excluding specials)
            if (initialSeason.getSeasonNumber() == 0 && seasons.size() > 1) {
                for (Season s : seasons) {
                    if (s.getSeasonNumber() > 0) {
                        initialSeason = s;
                        break;
                    }
                }
            }

            currentSeasonNumber = initialSeason.getSeasonNumber();
            fetchEpisodes(currentSeasonNumber);
            btnSeasonPicker.setText(initialSeason.getName());
        }
    }

    private void loadAsMovie() {
        isTVShow = false;
        if (btnPlay instanceof ImageView) {
            ((ImageView) btnPlay).setImageResource(R.drawable.play_now);
            ViewGroup.LayoutParams params = btnPlay.getLayoutParams();
            params.width = (int) (125 * getResources().getDisplayMetrics().density);
            params.height = (int) (44 * getResources().getDisplayMetrics().density);
            btnPlay.setLayoutParams(params);
        }
        llEpisodesSection.setVisibility(View.GONE);
    }

    private void fetchEpisodes(int seasonNumber) {
        RetrofitClient.getApi().getSeasonEpisodes("Bearer " + Constants.TMDB_ACCESS_TOKEN, Integer.parseInt(movie.getId()), seasonNumber)
                .enqueue(new Callback<EpisodeResponse>() {
                    @Override
                    public void onResponse(Call<EpisodeResponse> call, Response<EpisodeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            rvEpisodes.setAdapter(new EpisodeAdapter(MovieDetailActivity.this, response.body().getEpisodes(), episode -> {
                                showAdsWarning(episode);
                            }));
                        }
                    }
                    @Override
                    public void onFailure(Call<EpisodeResponse> call, Throwable t) {}
                });
    }

    private void fetchPhotos() {
        String token = "Bearer " + Constants.TMDB_ACCESS_TOKEN;
        int id = Integer.parseInt(movie.getId());
        Call<ImageResponse> call = isTVShow ? RetrofitClient.getApi().getTVImages(token, id) : RetrofitClient.getApi().getMovieImages(token, id);
        
        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rvPhotos.setAdapter(new PhotoAdapter(MovieDetailActivity.this, response.body().getBackdrops()));
                }
            }
            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {}
        });
    }

    private void showSeasonPickerDialog() {
        if (seasons == null || seasons.isEmpty()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.GlassmorphicDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_season_picker, null);
        RecyclerView rvSeasons = view.findViewById(R.id.rv_season_list);
        
        rvSeasons.setLayoutManager(new LinearLayoutManager(this));
        SeasonAdapter adapter = new SeasonAdapter(this, seasons, currentSeasonNumber, season -> {
            currentSeasonNumber = season.getSeasonNumber();
            btnSeasonPicker.setText(season.getName());
            fetchEpisodes(currentSeasonNumber);
            dialog.dismiss();
        });
        rvSeasons.setAdapter(adapter);
        
        dialog.setContentView(view);
        dialog.show();
    }
    
    private void fetchCredits() {
        String token = "Bearer " + Constants.TMDB_ACCESS_TOKEN;
        int id = Integer.parseInt(movie.getId());
        Call<CreditsResponse> call = isTVShow ? RetrofitClient.getApi().getTVCredits(token, id) : RetrofitClient.getApi().getMovieCredits(token, id);

        call.enqueue(new Callback<CreditsResponse>() {
            @Override
            public void onResponse(Call<CreditsResponse> call, Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rvCast.setAdapter(new CastAdapter(MovieDetailActivity.this, response.body().getCast()));
                    setupDirector(response.body().getCrew());
                }
            }
            @Override
            public void onFailure(Call<CreditsResponse> call, Throwable t) {}
        });
    }

    private void setupDirector(List<Crew> crew) {
        if (crew == null) return;
        StringBuilder directors = new StringBuilder();
        for (Crew member : crew) {
            if ("Director".equals(member.getJob())) {
                if (directors.length() > 0) directors.append(", ");
                directors.append(member.getName());
            }
        }
        if (directors.length() > 0) {
            rlDirectorSection.setVisibility(View.VISIBLE);
            tvCrewList.setVisibility(View.VISIBLE);
            tvCrewList.setText(directors.toString());
        }
    }

    private void fetchVideos() {
        String token = "Bearer " + Constants.TMDB_ACCESS_TOKEN;
        int id = Integer.parseInt(movie.getId());
        
        // Use TV videos endpoint for TV shows, movie videos for movies
        Call<VideosResponse> call;
        if ("tv".equals(movie.getMediaType())) {
            call = RetrofitClient.getApi().getTVVideos(token, id);
        } else {
            call = RetrofitClient.getApi().getMovieVideos(token, id);
        }
        
        call.enqueue(new Callback<VideosResponse>() {
                @Override
                public void onResponse(Call<VideosResponse> call, Response<VideosResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Video> videos = response.body().getResults();
                        if (videos != null) {
                            for (Video video : videos) {
                                if ("Trailer".equals(video.getType()) && "YouTube".equals(video.getSite())) {
                                    setupTrailerButton(video.getKey());
                                    return;
                                }
                            }
                            // If no Trailer found, try Teaser
                            for (Video video : videos) {
                                if ("Teaser".equals(video.getType()) && "YouTube".equals(video.getSite())) {
                                    setupTrailerButton(video.getKey());
                                    return;
                                }
                            }
                        }
                    }
                    
                    // If this was a TV call that failed to find videos, try movie endpoint as fallback
                    if ("tv".equals(movie.getMediaType())) {
                        fetchMovieVideosFallback();
                    }
                }
                @Override
                public void onFailure(Call<VideosResponse> call, Throwable t) {
                    if ("tv".equals(movie.getMediaType())) {
                        fetchMovieVideosFallback();
                    }
                }
            });
    }

    private void fetchMovieVideosFallback() {
        RetrofitClient.getApi().getMovieVideos("Bearer " + Constants.TMDB_ACCESS_TOKEN, Integer.parseInt(movie.getId()))
            .enqueue(new Callback<VideosResponse>() {
                @Override
                public void onResponse(Call<VideosResponse> call, Response<VideosResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Video> videos = response.body().getResults();
                        if (videos != null) {
                            for (Video video : videos) {
                                if (("Trailer".equals(video.getType()) || "Teaser".equals(video.getType())) && "YouTube".equals(video.getSite())) {
                                    setupTrailerButton(video.getKey());
                                    return;
                                }
                            }
                        }
                    }
                }
                @Override
                public void onFailure(Call<VideosResponse> call, Throwable t) {}
            });
    }

    private void setupTrailerButton(String key) {
        btnTrailer.setEnabled(true);
        btnTrailer.setOnClickListener(v -> showTrailerDialog(key));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void showTrailerDialog(String key) {
        View view = getLayoutInflater().inflate(R.layout.dialog_trailer, null);
        WebView webView = view.findViewById(R.id.trailer_webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        
        // Fix for Error 150/153: Set a proper mobile User-Agent
        // Use a desktop-like but compatible UA to avoid the "mobile web" UI (red mark)
        // and get the clean embedded player (green mark)
        String cleanUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36";
        settings.setUserAgentString(cleanUA);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                openYouTubeFallback(key);
            }
        });

        // Clean embed parameters: modestbranding=1, rel=0, showinfo=0
        String iframeHtml = "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>body { margin: 0; background: #000; overflow: hidden; display: flex; align-items: center; justify-content: center; height: 100vh; } .video-container { position: relative; width: 100%; padding-bottom: 56.25%; height: 0; } .video-container iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }</style></head><body><div class=\"video-container\"><iframe id=\"yt_player\" src=\"https://www.youtube.com/embed/" + key + "?autoplay=1&rel=0&modestbranding=1&controls=1&showinfo=0&origin=https://www.youtube.com\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe></div><script>var tag = document.createElement('script'); tag.src = \"https://www.youtube.com/iframe_api\"; var firstScriptTag = document.getElementsByTagName('script')[0]; firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);</script></body></html>";
        webView.loadDataWithBaseURL("https://www.youtube.com", iframeHtml, "text/html", "utf-8", null);

        new MaterialAlertDialogBuilder(this, R.style.GlassmorphicDialog)
                .setView(view)
                .setNeutralButton("Open in YouTube", (dialog, which) -> openYouTubeFallback(key))
                .setOnDismissListener(dialog -> webView.destroy())
                .show();
    }

    private void openYouTubeFallback(String key) {
        try {
            // Try YouTube app first
            Intent ytIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
            startActivity(ytIntent);
        } catch (Exception e) {
            // Fallback to browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + key));
            startActivity(browserIntent);
        }
    }

    private void showAdsWarning(Episode episode) {
        String typeLabel = isTVShow ? "TV series" : "movies";
        String message = "Since we source these " + typeLabel + " from external providers, occasional viewing issues can happen. If that occurs, you can try switching servers by clicking the icon in the top-right corner of the screen.";
        new MaterialAlertDialogBuilder(this, R.style.GlassmorphicDialog)
                .setTitle("External Provider")
                .setMessage(message)
                .setPositiveButton("I Understand", (dialog, which) -> {
                    Intent intent = new Intent(MovieDetailActivity.this, PlayerActivity.class);
                    intent.putExtra("movie", movie);
                    if (episode != null) {
                        intent.putExtra("season", episode.getSeasonNumber());
                        intent.putExtra("episode", episode.getEpisodeNumber());
                    }
                    startActivity(intent);
                })
                .show();
    }

    private void saveToHistory() {
        List<Movie> history = getList(Constants.KEY_WATCH_HISTORY);
        history.removeIf(m -> m.getId().equals(movie.getId()));
        history.add(0, movie);
        if (history.size() > 20) history.remove(history.size() - 1);
        saveList(Constants.KEY_WATCH_HISTORY, history);
    }

    private List<Movie> getList(String key) {
        String json = prefs.getString(key, null);
        if (json == null) return new ArrayList<>();
        return gson.fromJson(json, new TypeToken<List<Movie>>(){}.getType());
    }

    private void saveList(String key, List<Movie> list) {
        prefs.edit().putString(key, gson.toJson(list)).apply();
    }

    private static class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.ViewHolder> {
        private final Context context;
        private final List<Season> seasons;
        private final int currentSeason;
        private final OnSeasonSelectedListener listener;

        public interface OnSeasonSelectedListener {
            void onSeasonSelected(Season season);
        }

        public SeasonAdapter(Context context, List<Season> seasons, int currentSeason, OnSeasonSelectedListener listener) {
            this.context = context;
            this.seasons = seasons;
            this.currentSeason = currentSeason;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_season, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Season season = seasons.get(position);
            holder.tvName.setText(season.getName());
            holder.tvEpisodes.setText(season.getEpisodeCount() + " Episodes");
            Picasso.get().load(season.getPosterPath()).into(holder.ivPoster);

            boolean isSelected = season.getSeasonNumber() == currentSeason;
            holder.ivIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            holder.itemView.setAlpha(isSelected ? 1.0f : 0.7f);

            holder.itemView.setOnClickListener(v -> listener.onSeasonSelected(season));
        }

        @Override
        public int getItemCount() {
            return seasons.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPoster, ivIndicator;
            TextView tvName, tvEpisodes;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPoster = itemView.findViewById(R.id.iv_season_poster);
                ivIndicator = itemView.findViewById(R.id.iv_selected_indicator);
                tvName = itemView.findViewById(R.id.tv_season_name);
                tvEpisodes = itemView.findViewById(R.id.tv_season_episodes);
            }
        }
    }
}
