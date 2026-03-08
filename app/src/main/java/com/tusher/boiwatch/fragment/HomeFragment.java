package com.tusher.boiwatch.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tusher.boiwatch.Constants;
import com.tusher.boiwatch.MainActivity;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.activity.AboutActivity;
import com.tusher.boiwatch.adapter.HeroAdapter;
import com.tusher.boiwatch.adapter.MovieAdapter;
import com.tusher.boiwatch.adapter.HomeAudiobookAdapter;
import com.tusher.boiwatch.api.RetrofitClient;
import com.tusher.boiwatch.api.YouTubeAudiobookService;
import com.tusher.boiwatch.activity.AudiobookPlayerActivity;
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

    private int selectedGenreId = -1;
    private String selectedGenreName = null;

    private float dX, dY;
    private static final int CLICK_ACTION_THRESHOLD = 200;
    private long lastTouchDownTime;

    public static HomeFragment newInstance(int genreId, String genreName) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt("genre_id", genreId);
        args.putString("genre_name", genreName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedGenreId = getArguments().getInt("genre_id", -1);
            selectedGenreName = getArguments().getString("genre_name");
        }
    }

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
        ImageView ivSearchTop = view.findViewById(R.id.iv_search_top);

        ivSearchTop.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new SearchFragment());
            }
        });

        prefs = getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        token = "Bearer " + Constants.TMDB_ACCESS_TOKEN;

        setupDiscoveryButton();
        setupMenu();
        loadContinueWatching();
        
        if (selectedGenreId != -1) {
            setupGenreMode();
        } else {
            fetchTrendingAll(); 
            fetchDynamicSections();
        }

        return view;
    }

    private void setupGenreMode() {
        // Hide Hero and Continue Watching in Genre Mode
        heroViewPager.setVisibility(View.GONE);
        heroIndicator.setVisibility(View.GONE);
        tvContinueWatching.setVisibility(View.GONE);
        rvContinueWatching.setVisibility(View.GONE);

        // Add a "Exit Genre Mode" header
        View headerView = LayoutInflater.from(getContext()).inflate(R.layout.item_genre_header, llDynamicSections, false);
        TextView tvTitle = headerView.findViewById(R.id.tv_genre_title);
        ImageView ivClose = headerView.findViewById(R.id.iv_close_genre);
        
        tvTitle.setText("Category: " + selectedGenreName);
        ivClose.setOnClickListener(v -> updateGenre(-1, null));
        llDynamicSections.addView(headerView);

        // Fetch contents for this genre
        addCategorySection("Popular in " + selectedGenreName, RetrofitClient.getApi().discoverMovie(token, Map.of("with_genres", String.valueOf(selectedGenreId), "sort_by", "popularity.desc")), "movie");
        addCategorySection("Top Rated " + selectedGenreName, RetrofitClient.getApi().discoverMovie(token, Map.of("with_genres", String.valueOf(selectedGenreId), "sort_by", "vote_average.desc")), "movie");
        addCategorySection("Recent " + selectedGenreName, RetrofitClient.getApi().discoverMovie(token, Map.of("with_genres", String.valueOf(selectedGenreId), "sort_by", "primary_release_date.desc")), "movie");
    }

    public void updateGenre(int genreId, String genreName) {
        this.selectedGenreId = genreId;
        this.selectedGenreName = genreName;
        
        if (llDynamicSections != null) {
            llDynamicSections.removeAllViews();
        }
        
        if (selectedGenreId != -1) {
            setupGenreMode();
        } else {
            heroViewPager.setVisibility(View.VISIBLE);
            heroIndicator.setVisibility(View.VISIBLE);
            tvContinueWatching.setVisibility(View.VISIBLE);
            rvContinueWatching.setVisibility(View.VISIBLE);
            fetchTrendingAll();
            fetchDynamicSections();
        }
    }

    private void setupMenu() {
        ivMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenuInflater().inflate(R.menu.home_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_settings) {
                    showSettingsDialog();
                    return true;
                } else if (item.getItemId() == R.id.menu_about) {
                    startActivity(new Intent(getContext(), AboutActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_explore_genres) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).openDrawer();
                    }
                    return true;
                } else if (item.getItemId() == R.id.menu_audiobooks) {
                    if (getActivity() != null) {
                        com.google.android.material.bottomnavigation.BottomNavigationView navView = getActivity().findViewById(R.id.bottom_navigation);
                        if (navView != null) navView.setSelectedItemId(R.id.nav_audiobooks);
                    }
                    return true;
                } else if (item.getItemId() == R.id.menu_reels) {
                    if (getActivity() != null) {
                        com.google.android.material.bottomnavigation.BottomNavigationView navView = getActivity().findViewById(R.id.bottom_navigation);
                        if (navView != null) navView.setSelectedItemId(R.id.nav_reels);
                    }
                    return true;
                } else if (item.getItemId() == R.id.menu_login) {
                    new LoginBottomSheetFragment().show(getParentFragmentManager(), "LoginSheet");
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
        MaterialButton btnClearHistory = dialogView.findViewById(R.id.btn_clear_history);
        MaterialButton btnClearAll = dialogView.findViewById(R.id.btn_clear_all);
        MaterialButton btnCheckUpdate = dialogView.findViewById(R.id.btn_check_update);
        
        if (btnCheckUpdate != null) {
            btnCheckUpdate.setOnClickListener(v -> 
                com.tusher.boiwatch.utils.UpdateHelper.checkForUpdates(getContext(), true)
            );
        }
        
        MaterialButton btnReorderSections = dialogView.findViewById(R.id.btn_reorder_sections);
        if (btnReorderSections != null) {
            btnReorderSections.setOnClickListener(v -> showSectionToggleDialog());
        }
        
        boolean isVisible = prefs.getBoolean(Constants.KEY_DISCOVERY_VISIBLE, true);
        switchDiscovery.setChecked(isVisible);
        
        android.app.Dialog dialog = new MaterialAlertDialogBuilder(getContext(), R.style.GlassmorphicDialog)
                .setTitle("Settings")
                .setView(dialogView)
                .setPositiveButton("Done", (d, which) -> {
                    boolean newValue = switchDiscovery.isChecked();
                    prefs.edit().putBoolean(Constants.KEY_DISCOVERY_VISIBLE, newValue).apply();
                    fabDiscovery.setVisibility(newValue ? View.VISIBLE : View.GONE);
                })
                .create();

        btnClearHistory.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(getContext(), R.style.GlassmorphicDialog)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to clear your 'Continue Watching' list?")
                    .setPositiveButton("Clear", (d, which) -> {
                        prefs.edit().remove(Constants.KEY_WATCH_HISTORY).apply();
                        loadContinueWatching();
                        Toast.makeText(getContext(), "History cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnClearAll.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(getContext(), R.style.GlassmorphicDialog)
                    .setTitle("Reset App")
                    .setMessage("This will remove all your watch history, watchlist, and custom settings. Proceed?")
                    .setPositiveButton("Clear All", (d, which) -> {
                        prefs.edit().clear().apply();
                        loadContinueWatching();
                        setupDiscoveryButton();
                        Toast.makeText(getContext(), "All data cleared", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        dialog.show();
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
        String json = prefs.getString(Constants.KEY_HOME_SECTIONS, null);
        List<String> activeSections;
        if (json != null) {
            activeSections = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
            boolean modified = false;
            
            if (!activeSections.contains("animation")) {
                int actionIndex = activeSections.indexOf("action");
                if (actionIndex != -1) {
                    activeSections.add(actionIndex + 1, "animation");
                } else {
                    activeSections.add("animation");
                }
                modified = true;
            }
            if (!activeSections.contains("audio_books")) {
                int sitcomIndex = activeSections.indexOf("sitcom");
                if (sitcomIndex != -1) {
                    activeSections.add(sitcomIndex, "audio_books");
                } else {
                    activeSections.add("audio_books");
                }
                modified = true;
            }
            if (activeSections.remove("documentary")) modified = true;
            if (activeSections.remove("reality")) modified = true;
            
            if (modified) {
                prefs.edit().putString(Constants.KEY_HOME_SECTIONS, gson.toJson(activeSections)).apply();
            }
        } else {
            activeSections = getDefaultSections();
        }

        for (String section : activeSections) {
            switch (section) {
                case "popular_movies": addCategorySection("Popular Movies", RetrofitClient.getApi().getPopularMovies(token), "movie"); break;
                case "top_rated_movies": addCategorySection("Top Rated Movies", RetrofitClient.getApi().getTopRatedMovies(token), "movie"); break;
                case "popular_tv": addCategorySection("Popular TV Shows", RetrofitClient.getApi().getPopularTV(token), "tv"); break;
                case "top_rated_tv": addCategorySection("Top Rated Shows", RetrofitClient.getApi().getTopRatedTV(token), "tv"); break;
                case "critically_acclaimed": addCategorySection("Critically Acclaimed Shows", discoverTV(null, "vote_average.desc"), "tv"); break;
                case "audio_books": addAudiobookSection("Audio Books"); break;
                case "sitcom": addCategorySection("Sitcom Picks for You", discoverTV("35", null), "tv"); break;
                case "binge": addCategorySection("Bingeworthy Shows", discoverTV(null, "popularity.desc"), "tv"); break;
                case "horror": addCategorySection("Horror Classics", discoverMovie("27", null), "movie"); break;
                case "thriller": addCategorySection("Thriller Movies", discoverMovie("53", null), "movie"); break;
                case "action": addCategorySection("Action Packed", discoverMovie("28", null), "movie"); break;
                case "animation": addCategorySection("Animations", discoverMovie("16", null), "movie"); break;
                case "romance": addCategorySection("Romance & Comedy", discoverMovie("10749,35", null), "movie"); break;
                case "anime": addCategorySection("Anime", discoverTV("16", "ja"), "tv"); break;
                case "k_drama": addCategorySection("K-Dramas", discoverTV("18", "ko"), "tv"); break;
                case "j_drama": addCategorySection("J-Dramas", discoverTV("18", "ja"), "tv"); break;
                case "sci_fi": addCategorySection("Sci-Fi & Fantasy", discoverMovie("878,14", null), "movie"); break;
            }
        }
    }

    private List<String> getDefaultSections() {
        return List.of("popular_movies", "top_rated_movies", "popular_tv", "top_rated_tv", "critically_acclaimed", "audio_books", "sitcom", "binge", "horror", "thriller", "action", "animation", "romance", "anime", "k_drama", "j_drama", "sci_fi");
    }

    private void showSectionToggleDialog() {
        String[] sectionKeys = {"popular_movies", "top_rated_movies", "popular_tv", "top_rated_tv", "critically_acclaimed", "audio_books", "sitcom", "binge", "horror", "thriller", "action", "animation", "romance", "anime", "k_drama", "j_drama", "sci_fi"};
        String[] sectionNames = {"Popular Movies", "Top Rated Movies", "Popular TV", "Top Rated TV", "Critically Acclaimed", "Audio Books", "Sitcoms", "Bingeworthy", "Horror", "Thriller", "Action", "Animations", "Romance", "Anime", "K-Dramas", "J-Dramas", "Sci-Fi"};
        
        String json = prefs.getString(Constants.KEY_HOME_SECTIONS, null);
        List<String> activeKeys = (json != null) ? gson.fromJson(json, new TypeToken<List<String>>(){}.getType()) : getDefaultSections();
        
        boolean[] checkedItems = new boolean[sectionKeys.length];
        for (int i = 0; i < sectionKeys.length; i++) {
            checkedItems[i] = activeKeys.contains(sectionKeys[i]);
        }

        new MaterialAlertDialogBuilder(getContext(), R.style.GlassmorphicDialog)
                .setTitle("Customize Home")
                .setMultiChoiceItems(sectionNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Save", (dialog, which) -> {
                    List<String> newActiveKeys = new ArrayList<>();
                    for (int i = 0; i < sectionKeys.length; i++) {
                        if (checkedItems[i]) newActiveKeys.add(sectionKeys[i]);
                    }
                    prefs.edit().putString(Constants.KEY_HOME_SECTIONS, gson.toJson(newActiveKeys)).apply();
                    updateGenre(-1, null); // Refresh home
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    private void addAudiobookSection(String title) {
        View sectionView = LayoutInflater.from(getContext()).inflate(R.layout.item_home_section, llDynamicSections, false);
        TextView tvTitle = sectionView.findViewById(R.id.tv_section_title);
        RecyclerView rvList = sectionView.findViewById(R.id.rv_section_list);
        
        tvTitle.setText(title);
        tvTitle.setOnClickListener(v -> {
            if (getActivity() != null) {
                androidx.navigation.Navigation.findNavController(getActivity(), R.id.fragment_container).navigate(R.id.nav_audiobooks);
            }
        });
        
        rvList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        
        llDynamicSections.addView(sectionView);
        
        String query = "Best Sci-Fi Mystery full audiobook english " + (Math.random() > 0.95 ? "bangla" : "");
        YouTubeAudiobookService.getInstance().searchAudiobooks(query, new YouTubeAudiobookService.SearchCallback() {
            @Override
            public void onSuccess(List<com.tusher.boiwatch.models.Audiobook> results) {
                if (isAdded() && results != null && !results.isEmpty()) {
                    HomeAudiobookAdapter adapter = new HomeAudiobookAdapter(getContext(), results, audiobook -> {
                        Intent intent = new Intent(getContext(), AudiobookPlayerActivity.class);
                        intent.putExtra("audiobook", audiobook);
                        startActivity(intent);
                    });
                    rvList.setAdapter(adapter);
                } else {
                    llDynamicSections.removeView(sectionView);
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    llDynamicSections.removeView(sectionView);
                }
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
