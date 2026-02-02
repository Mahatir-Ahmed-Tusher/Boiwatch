package com.tusher.boiwatch.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tusher.boiwatch.Constants;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.adapter.HeroAdapter;
import com.tusher.boiwatch.adapter.MovieAdapter;
import com.tusher.boiwatch.api.RetrofitClient;
import com.tusher.boiwatch.models.Movie;
import com.tusher.boiwatch.models.MovieResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ViewPager2 heroViewPager;
    private TabLayout heroIndicator;
    private RecyclerView rvContinueWatching;
    private TextView tvContinueWatching;
    private LinearLayout llDynamicSections;
    private FloatingActionButton fabDiscovery;
    private View removeZone;
    private ImageView ivMenu;
    private Handler sliderHandler = new Handler();
    private SharedPreferences prefs;
    private Gson gson = new Gson();
    private String token;

    private float dX, dY;
    private static final int CLICK_ACTION_THRESHOLD = 200;
    private long lastTouchDownTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        heroViewPager = view.findViewById(R.id.hero_viewpager);
        heroIndicator = view.findViewById(R.id.hero_indicator);
        rvContinueWatching = view.findViewById(R.id.rv_continue_watching);
        tvContinueWatching = view.findViewById(R.id.tv_continue_watching);
        llDynamicSections = view.findViewById(R.id.ll_dynamic_sections);
        fabDiscovery = view.findViewById(R.id.fab_discovery);
        removeZone = view.findViewById(R.id.remove_zone);
        ivMenu = view.findViewById(R.id.iv_menu);

        prefs = getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        token = "Bearer " + Constants.TMDB_ACCESS_TOKEN;

        setupDiscoveryButton();
        setupMenu();
        loadContinueWatching();
        fetchTrendingAll(); 
        fetchDynamicSections();

        return view;
    }

    private void setupMenu() {
        ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenuInflater().inflate(R.menu.home_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_settings) {
                    showSettingsDialog();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void showSettingsDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_settings, null);
        MaterialSwitch switchDiscovery = dialogView.findViewById(R.id.switch_discovery_visibility);
        
        boolean isVisible = prefs.getBoolean(Constants.KEY_DISCOVERY_VISIBLE, true);
        switchDiscovery.setChecked(isVisible);
        
        new MaterialAlertDialogBuilder(getContext(), R.style.GlassmorphicDialog)
                .setTitle("Settings")
                .setView(dialogView)
                .setPositiveButton("Done", (dialog, which) -> {
                    boolean newValue = switchDiscovery.isChecked();
                    prefs.edit().putBoolean(Constants.KEY_DISCOVERY_VISIBLE, newValue).apply();
                    fabDiscovery.setVisibility(newValue ? View.VISIBLE : View.GONE);
                })
                .show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupDiscoveryButton() {
        boolean isVisible = prefs.getBoolean(Constants.KEY_DISCOVERY_VISIBLE, true);
        fabDiscovery.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        fabDiscovery.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    lastTouchDownTime = System.currentTimeMillis();
                    removeZone.setVisibility(View.VISIBLE);
                    break;

                case MotionEvent.ACTION_MOVE:
                    view.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                    
                    if (event.getRawY() > removeZone.getTop() + removeZone.getHeight() / 2) {
                        removeZone.setBackgroundColor(0x88FF0000);
                    } else {
                        removeZone.setBackgroundColor(0x44FF0000);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    removeZone.setVisibility(View.GONE);
                    if (System.currentTimeMillis() - lastTouchDownTime < CLICK_ACTION_THRESHOLD) {
                        new DiscoveryChatFragment().show(getChildFragmentManager(), "discovery_chat");
                    } else {
                        int[] location = new int[2];
                        removeZone.getLocationOnScreen(location);
                        if (event.getRawY() > location[1]) {
                            fabDiscovery.setVisibility(View.GONE);
                            prefs.edit().putBoolean(Constants.KEY_DISCOVERY_VISIBLE, false).apply();
                            Toast.makeText(getContext(), "Discovery button removed. You can re-enable it in settings.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                default:
                    return false;
            }
            return true;
        });
    }

    private void fetchDynamicSections() {
        addCategorySection("Popular Movies", RetrofitClient.getApi().getPopularMovies(token), "movie");
        addCategorySection("Top Rated Movies", RetrofitClient.getApi().getTopRatedMovies(token), "movie");
        addCategorySection("Popular TV Shows", RetrofitClient.getApi().getPopularTV(token), "tv");
        addCategorySection("Top Rated Shows", RetrofitClient.getApi().getTopRatedTV(token), "tv");
        addCategorySection("Critically Acclaimed Shows", discoverTV(null, "vote_average.desc"), "tv");
        addCategorySection("Sitcom Picks for You", discoverTV("35", null), "tv"); 
        addCategorySection("Bingeworthy Shows", discoverTV(null, "popularity.desc"), "tv");
        addCategorySection("Horror Classics", discoverMovie("27", null), "movie"); 
        addCategorySection("Thriller Movies", discoverMovie("53", null), "movie");
        addCategorySection("Action Packed", discoverMovie("28", null), "movie");
        addCategorySection("Romance & Comedy", discoverMovie("10749,35", null), "movie"); 
        addCategorySection("Anime", discoverTV("16", "ja"), "tv"); 
        addCategorySection("K-Dramas", discoverTV("18", "ko"), "tv"); 
        addCategorySection("J-Dramas", discoverTV("18", "ja"), "tv"); 
        addCategorySection("Sci-Fi & Fantasy", discoverMovie("878,14", null), "movie");
        addCategorySection("Documentaries", discoverMovie("99", null), "movie");
        addCategorySection("Reality TV", discoverTV("10764", null), "tv");
    }

    private Call<MovieResponse> discoverMovie(String genres, String sortBy) {
        Map<String, String> map = new HashMap<>();
        if (genres != null) map.put("with_genres", genres);
        if (sortBy != null) map.put("sort_by", sortBy);
        return RetrofitClient.getApi().discoverMovie(token, map);
    }

    private Call<MovieResponse> discoverTV(String genres, String lang) {
        Map<String, String> map = new HashMap<>();
        if (genres != null) map.put("with_genres", genres);
        if (lang != null) map.put("with_original_language", lang);
        map.put("sort_by", "popularity.desc");
        return RetrofitClient.getApi().discoverTV(token, map);
    }

    private void addCategorySection(String title, Call<MovieResponse> call, String mediaType) {
        View sectionView = LayoutInflater.from(getContext()).inflate(R.layout.item_home_section, llDynamicSections, false);
        TextView tvTitle = sectionView.findViewById(R.id.tv_section_title);
        RecyclerView rvList = sectionView.findViewById(R.id.rv_section_list);
        
        tvTitle.setText(title);
        rvList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        
        llDynamicSections.addView(sectionView);
        
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getMovies();
                    if (movies != null && !movies.isEmpty()) {
                        for (Movie m : movies) m.setMediaType(mediaType);
                        rvList.setAdapter(new MovieAdapter(getContext(), movies));
                    } else {
                        llDynamicSections.removeView(sectionView);
                    }
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                llDynamicSections.removeView(sectionView);
            }
        });
    }

    private void loadContinueWatching() {
        String json = prefs.getString(Constants.KEY_WATCH_HISTORY, null);
        if (json != null) {
            List<Movie> history = gson.fromJson(json, new TypeToken<List<Movie>>(){}.getType());
            if (history != null && !history.isEmpty()) {
                tvContinueWatching.setVisibility(View.VISIBLE);
                rvContinueWatching.setVisibility(View.VISIBLE);
                
                MovieAdapter adapter = new MovieAdapter(getContext(), history);
                adapter.setOnItemLongClickListener((movie, position) -> {
                    showRemoveFromHistoryDialog(movie, position, history);
                });
                rvContinueWatching.setAdapter(adapter);
            } else {
                tvContinueWatching.setVisibility(View.GONE);
                rvContinueWatching.setVisibility(View.GONE);
            }
        } else {
            tvContinueWatching.setVisibility(View.GONE);
            rvContinueWatching.setVisibility(View.GONE);
        }
    }

    private void showRemoveFromHistoryDialog(Movie movie, int position, List<Movie> history) {
        new MaterialAlertDialogBuilder(getContext(), R.style.GlassmorphicDialog)
                .setTitle("Remove from History")
                .setMessage("Do you want to remove '" + movie.getTitle() + "' from your Continue Watching list?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    history.remove(position);
                    prefs.edit().putString(Constants.KEY_WATCH_HISTORY, gson.toJson(history)).apply();
                    loadContinueWatching();
                    Toast.makeText(getContext(), "Removed from history", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchTrendingAll() {
        RetrofitClient.getApi().getTrendingAll(token).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    setupHero(response.body().getMovies());
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {}
        });
    }

    private void setupHero(List<Movie> movies) {
        if (movies.size() > 5) movies = movies.subList(0, 5); 
        HeroAdapter adapter = new HeroAdapter(getContext(), movies);
        heroViewPager.setAdapter(adapter);
        
        new TabLayoutMediator(heroIndicator, heroViewPager, (tab, position) -> {}).attach();

        heroViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 5000);
            }
        });
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int currentItem = heroViewPager.getCurrentItem();
            int totalItems = heroViewPager.getAdapter() != null ? heroViewPager.getAdapter().getItemCount() : 0;
            if (totalItems > 0) {
                heroViewPager.setCurrentItem((currentItem + 1) % totalItems);
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContinueWatching();
        sliderHandler.postDelayed(sliderRunnable, 5000);
        
        boolean isVisible = prefs.getBoolean(Constants.KEY_DISCOVERY_VISIBLE, true);
        fabDiscovery.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
