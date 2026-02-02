package com.tusher.boiwatch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tusher.boiwatch.fragment.HomeFragment;
import com.tusher.boiwatch.fragment.LibraryFragment;
import com.tusher.boiwatch.fragment.ProfileFragment;
import com.tusher.boiwatch.fragment.SearchFragment;
import com.tusher.boiwatch.models.Genre;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView rvGenres;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        rvGenres = findViewById(R.id.rv_genres);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        setupGenreDrawer();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                fragment = new SearchFragment();
            } else if (itemId == R.id.nav_library) {
                fragment = new LibraryFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }
            return loadFragment(fragment);
        });
    }

    private void setupGenreDrawer() {
        List<Genre> genres = new ArrayList<>();
        // Movie Genres
        genres.add(new Genre(28, "Action", R.drawable.ic_play));
        genres.add(new Genre(12, "Adventure", R.drawable.ic_play));
        genres.add(new Genre(16, "Animation", R.drawable.ic_play));
        genres.add(new Genre(35, "Comedy", R.drawable.ic_play));
        genres.add(new Genre(80, "Crime", R.drawable.ic_play));
        genres.add(new Genre(99, "Documentary", R.drawable.ic_play));
        genres.add(new Genre(18, "Drama", R.drawable.ic_play));
        genres.add(new Genre(10751, "Family", R.drawable.ic_play));
        genres.add(new Genre(14, "Fantasy", R.drawable.ic_play));
        genres.add(new Genre(36, "History", R.drawable.ic_play));
        genres.add(new Genre(27, "Horror", R.drawable.ic_play));
        genres.add(new Genre(10402, "Music", R.drawable.ic_play));
        genres.add(new Genre(9648, "Mystery", R.drawable.ic_play));
        genres.add(new Genre(10749, "Romance", R.drawable.ic_play));
        genres.add(new Genre(878, "Sci-Fi", R.drawable.ic_play));
        genres.add(new Genre(10770, "TV Movie", R.drawable.ic_play));
        genres.add(new Genre(53, "Thriller", R.drawable.ic_play));
        genres.add(new Genre(10752, "War", R.drawable.ic_play));
        genres.add(new Genre(37, "Western", R.drawable.ic_play));

        rvGenres.setLayoutManager(new LinearLayoutManager(this));
        rvGenres.setAdapter(new GenreAdapter(genres));
    }

    public void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.END);
    }

    public boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> {
        private List<Genre> genres;

        GenreAdapter(List<Genre> genres) { this.genres = genres; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre_drawer, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Genre genre = genres.get(position);
            holder.tvName.setText(genre.getName());
            holder.itemView.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.END);
                loadFragment(HomeFragment.newInstance(genre.getId(), genre.getName()));
            });
        }

        @Override
        public int getItemCount() { return genres.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_genre_name);
            }
        }
    }
}
