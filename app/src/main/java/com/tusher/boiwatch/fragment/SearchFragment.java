package com.tusher.boiwatch.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.tusher.boiwatch.Constants;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.adapter.MovieAdapter;
import com.tusher.boiwatch.api.RetrofitClient;
import com.tusher.boiwatch.models.Movie;
import com.tusher.boiwatch.models.MovieResponse;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private TabLayout tabLayout;
    private EditText etSearch;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private View llEmptyState;
    private TextView tvNoResults, tvEmptyQuery;
    private ImageView ivClearSearch;
    private Timer timer;
    private String currentTab = "movies";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        etSearch = view.findViewById(R.id.et_search);
        rvResults = view.findViewById(R.id.rv_search_results);
        progressBar = view.findViewById(R.id.search_progress);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        tvNoResults = view.findViewById(R.id.tv_no_results);
        tvEmptyQuery = view.findViewById(R.id.tv_empty_query);
        ivClearSearch = view.findViewById(R.id.iv_clear_search);

        setupTabs();
        setupSearch();

        return view;
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    currentTab = "movies";
                    etSearch.setHint("Search your favorite movies...");
                } else {
                    currentTab = "tv";
                    etSearch.setHint("Search your favorite series...");
                }
                
                String query = etSearch.getText().toString();
                if (query.length() > 1) {
                    performSearch(query);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        ivClearSearch.setOnClickListener(v -> etSearch.setText(""));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (timer != null) timer.cancel();
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 1) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (isAdded()) {
                                getActivity().runOnUiThread(() -> performSearch(s.toString()));
                            }
                        }
                    }, 500);
                } else {
                    rvResults.setAdapter(null);
                    llEmptyState.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);

        String token = "Bearer " + Constants.TMDB_ACCESS_TOKEN;
        Call<MovieResponse> call;
        
        if (currentTab.equals("movies")) {
            call = RetrofitClient.getApi().searchMovies(token, query);
        } else {
            call = RetrofitClient.getApi().searchTV(token, query);
        }

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Movie> movies = response.body().getMovies();
                        if (movies != null) {
                            for (Movie m : movies) {
                                m.setMediaType(currentTab.equals("movies") ? "movie" : "tv");
                            }
                        }
                        
                        MovieAdapter adapter = new MovieAdapter(getContext(), movies);
                        rvResults.setAdapter(adapter);
                        
                        if (movies == null || movies.isEmpty()) {
                            showEmptyState(query);
                        } else {
                            llEmptyState.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showEmptyState(String query) {
        llEmptyState.setVisibility(View.VISIBLE);
        if (currentTab.equals("movies")) {
            tvNoResults.setText("No movies found for '" + query + "'");
            tvEmptyQuery.setText("Try another movie name");
        } else {
            tvNoResults.setText("No series found for '" + query + "'");
            tvEmptyQuery.setText("Try another series name");
        }
    }
}
