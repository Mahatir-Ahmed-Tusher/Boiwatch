package com.tusher.boiwatch.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tusher.boiwatch.Constants;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.activity.AudiobookPlayerActivity;
import com.tusher.boiwatch.adapter.AudiobookAdapter;
import com.tusher.boiwatch.adapter.AudiobookHeroAdapter;
import com.tusher.boiwatch.api.YouTubeAudiobookService;
import com.tusher.boiwatch.models.Audiobook;

import java.util.ArrayList;
import java.util.List;

public class AudiobooksFragment extends Fragment {

    private EditText etSearch;

    // Hero Carousel
    private ViewPager2 vpHeroCarousel;
    private TabLayout tabHeroIndicator;
    private AudiobookHeroAdapter heroAdapter;
    private List<Audiobook> heroList = new ArrayList<>();
    private Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;

    // Recently Played
    private LinearLayout llRecentlyPlayed;
    private RecyclerView rvRecentlyPlayed;
    private List<Audiobook> recentlyPlayedList = new ArrayList<>();

    // Dynamic Genres Container
    private LinearLayout llGenreContainer;

    private YouTubeAudiobookService service;
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    private final String[] GENRES = {
            "Sci-Fi", "Philosophy", "History", "Mystery", "Romantic",
            "Comedy", "Adventure", "Classics", "Western", "Thriller"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audiobooks, container, false);

        service = YouTubeAudiobookService.getInstance();
        prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

        initViews(view);
        setupSearch();
        setupHeroCarousel();
        loadRecentlyPlayed();
        loadDynamicGenres();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentlyPlayed();
        startAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_audiobook_search);
        vpHeroCarousel = view.findViewById(R.id.vp_hero_carousel);
        tabHeroIndicator = view.findViewById(R.id.tab_hero_indicator);
        llRecentlyPlayed = view.findViewById(R.id.ll_recently_played);
        rvRecentlyPlayed = view.findViewById(R.id.rv_recently_played);
        llGenreContainer = view.findViewById(R.id.ll_genre_container);
    }

    // ── Search & Bottom Sheet ──────────────────────────────────

    private void setupSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    hideKeyboard();
                    openSearchBottomSheet(query);
                }
                return true;
            }
            return false;
        });

        // Also allow clicking the icon to trigger search (though it's visually inside ET here)
        // If user taps enter, it triggers IME action.
    }

    private void openSearchBottomSheet(String query) {
        AudiobookSearchBottomSheet bottomSheet = AudiobookSearchBottomSheet.newInstance(query);
        bottomSheet.setOnAudiobookSelectedListener(audiobook -> openPlayer(audiobook));
        bottomSheet.show(getChildFragmentManager(), "AudiobookSearch");
    }

    // ── Hero Carousel ──────────────────────────────────────────

    private void setupHeroCarousel() {
        heroAdapter = new AudiobookHeroAdapter(requireContext(), heroList, this::openPlayer);
        vpHeroCarousel.setAdapter(heroAdapter);

        // Link TabLayout with ViewPager2 for dots
        new TabLayoutMediator(tabHeroIndicator, vpHeroCarousel,
                (tab, position) -> {
                    // empty, just for dots
                }).attach();

        // Setup auto-scroll logic
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (heroAdapter.getItemCount() > 0) {
                    int currentItem = vpHeroCarousel.getCurrentItem();
                    int nextItem = (currentItem + 1) % heroAdapter.getItemCount();
                    vpHeroCarousel.setCurrentItem(nextItem, true);
                }
                autoScrollHandler.postDelayed(this, 5000); // 5 seconds
            }
        };

        // Fetch top global audiobooks for the hero
        service.searchAudiobooks("Trending best full audiobooks 2026", new YouTubeAudiobookService.SearchCallback() {
            @Override
            public void onSuccess(List<Audiobook> results) {
                if (getActivity() == null || !isAdded()) return;
                heroList.clear();
                // Take top 5 for hero
                if (results.size() > 5) {
                    heroList.addAll(results.subList(0, 5));
                } else {
                    heroList.addAll(results);
                }
                heroAdapter.notifyDataSetChanged();
                startAutoScroll();
            }

            @Override
            public void onError(String message) {
                // Ignore silent failure
            }
        });
    }

    private void startAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        if (heroList.size() > 1) {
            autoScrollHandler.postDelayed(autoScrollRunnable, 5000);
        }
    }

    private void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    // ── Recently Played ────────────────────────────────────────

    private void loadRecentlyPlayed() {
        String json = prefs.getString(Constants.KEY_RECENTLY_PLAYED_AUDIOBOOKS, null);
        recentlyPlayedList.clear();
        if (json != null) {
            List<Audiobook> saved = gson.fromJson(json, new TypeToken<List<Audiobook>>() {
            }.getType());
            if (saved != null && !saved.isEmpty()) {
                recentlyPlayedList.addAll(saved);
            }
        }

        if (recentlyPlayedList.isEmpty()) {
            llRecentlyPlayed.setVisibility(View.GONE);
        } else {
            llRecentlyPlayed.setVisibility(View.VISIBLE);
            AudiobookAdapter adapter = new AudiobookAdapter(requireContext(), recentlyPlayedList, this::openPlayer);
            adapter.setOnItemLongClickListener((audiobook, position) -> {
                showRemoveFromRecentlyPlayedDialog(audiobook, position);
            });
            rvRecentlyPlayed.setAdapter(adapter);

            rvRecentlyPlayed.setOnFlingListener(null);
            LinearSnapHelper snapHelper = new LinearSnapHelper();
            try { snapHelper.attachToRecyclerView(rvRecentlyPlayed); } catch (Exception ignored) {}
        }
    }

    // ── Dynamic Genre Sections ─────────────────────────────────

    private void loadDynamicGenres() {
        llGenreContainer.removeAllViews(); // Clear any existing

        for (String genre : GENRES) {
            // Inflate empty section layout
            View genreView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_audiobook_genre_section, llGenreContainer, false);
            
            TextView tvTitle = genreView.findViewById(R.id.tv_genre_title);
            ShimmerFrameLayout shimmer = genreView.findViewById(R.id.shimmer_genre);
            RecyclerView rvGenre = genreView.findViewById(R.id.rv_genre);

            tvTitle.setText(genre);
            
            // Add to container immediately
            llGenreContainer.addView(genreView);

            // Fetch data for this genre
            String query = genre.equals("Classics") ? "Classic Literature full audiobook english" : "Best " + genre + " full audiobook english";
            
            // Adding a small mix of Bengali audiobooks conceptually handled by query
            if (Math.random() > 0.85) { // ~15% chance to feature Bengali in a genre search
                query += " bangla"; 
            }

            service.searchAudiobooks(query, new YouTubeAudiobookService.SearchCallback() {
                @Override
                public void onSuccess(List<Audiobook> results) {
                    if (getActivity() == null || !isAdded()) return;

                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);

                    if (results != null && !results.isEmpty()) {
                        rvGenre.setVisibility(View.VISIBLE);
                        AudiobookAdapter adapter = new AudiobookAdapter(requireContext(), results, AudiobooksFragment.this::openPlayer);
                        rvGenre.setAdapter(adapter);

                        rvGenre.setOnFlingListener(null);
                        LinearSnapHelper snapHelper = new LinearSnapHelper();
                        try { snapHelper.attachToRecyclerView(rvGenre); } catch (Exception ignored) {}
                    } else {
                        // Hide section if no results
                        genreView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String message) {
                    if (getActivity() == null || !isAdded()) return;
                    shimmer.stopShimmer();
                    shimmer.setVisibility(View.GONE);
                    genreView.setVisibility(View.GONE);
                }
            });
        }
    }

    // ── Navigation & Utils ─────────────────────────────────────

    private void openPlayer(Audiobook audiobook) {
        saveToRecentlyPlayed(audiobook);
        Intent intent = new Intent(requireContext(), AudiobookPlayerActivity.class);
        intent.putExtra("audiobook", audiobook);
        startActivity(intent);
    }

    private void saveToRecentlyPlayed(Audiobook audiobook) {
        String json = prefs.getString(Constants.KEY_RECENTLY_PLAYED_AUDIOBOOKS, null);
        List<Audiobook> list = new ArrayList<>();
        if (json != null) {
            List<Audiobook> saved = gson.fromJson(json, new TypeToken<List<Audiobook>>() {
            }.getType());
            if (saved != null) list.addAll(saved);
        }

        list.removeIf(a -> a.getVideoId().equals(audiobook.getVideoId()));
        list.add(0, audiobook);

        if (list.size() > 10) list = list.subList(0, 10);

        prefs.edit().putString(Constants.KEY_RECENTLY_PLAYED_AUDIOBOOKS, gson.toJson(list)).apply();
    }

    private void showRemoveFromRecentlyPlayedDialog(Audiobook audiobook, int position) {
        new MaterialAlertDialogBuilder(requireContext(), R.style.GlassmorphicDialog)
                .setTitle("Remove from History")
                .setMessage("Do you want to remove '" + audiobook.getTitle() + "' from your Recently Played list?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    recentlyPlayedList.remove(position);
                    prefs.edit().putString(Constants.KEY_RECENTLY_PLAYED_AUDIOBOOKS, gson.toJson(recentlyPlayedList)).apply();
                    loadRecentlyPlayed();
                    Toast.makeText(getContext(), "Removed from recently played", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void hideKeyboard() {
        if (getActivity() != null && etSearch != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        }
    }
}
