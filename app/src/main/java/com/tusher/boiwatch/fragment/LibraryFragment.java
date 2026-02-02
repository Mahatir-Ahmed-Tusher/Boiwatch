package com.tusher.boiwatch.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tusher.boiwatch.Constants;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.adapter.MovieAdapter;
import com.tusher.boiwatch.models.Movie;
import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private RecyclerView rvLibrary;
    private TextView tvEmpty;
    private TabLayout tabLayout;
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        rvLibrary = view.findViewById(R.id.rv_library);
        tvEmpty = view.findViewById(R.id.tv_empty_library);
        tabLayout = view.findViewById(R.id.tab_layout_library);
        prefs = getActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);

        loadData(0); // Default to History

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadData(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    private void loadData(int position) {
        String key = (position == 0) ? Constants.KEY_WATCH_HISTORY : Constants.KEY_WATCHLIST;
        String json = prefs.getString(key, null);
        List<Movie> movies = new ArrayList<>();
        if (json != null) {
            movies = gson.fromJson(json, new TypeToken<List<Movie>>(){}.getType());
        }

        if (movies.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvLibrary.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvLibrary.setVisibility(View.VISIBLE);
            rvLibrary.setAdapter(new MovieAdapter(getContext(), movies));
        }
    }
}
