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
import com.tusher.boiwatch.fragment.ReelsFragment;
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
        bottomNavigationView = findViewById(R.id.bottom_navigation); // Fixed: Added assignment

        setupDrawer();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment fragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    fragment = new HomeFragment();
                } else if (itemId == R.id.nav_search) {
                    fragment = new SearchFragment();
                } else if (itemId == R.id.nav_reels) {
                    fragment = new ReelsFragment();
                } else if (itemId == R.id.nav_library) {
                    fragment = new LibraryFragment();
                }
                
                if (fragment != null) {
                    loadFragment(fragment);
                    return true;
                }
                return false;
            });
        }
    }

    private void setupDrawer() {
        List<Genre> items = new ArrayList<>();
        items.add(new Genre(-100, "Reels", R.drawable.ic_play));
        
        items.add(new Genre(28, "Action", R.drawable.ic_play));
        items.add(new Genre(12, "Adventure", R.drawable.ic_play));
        items.add(new Genre(16, "Animation", R.drawable.ic_play));
        items.add(new Genre(35, "Comedy", R.drawable.ic_play));
        items.add(new Genre(80, "Crime", R.drawable.ic_play));
        items.add(new Genre(99, "Documentary", R.drawable.ic_play));
        items.add(new Genre(18, "Drama", R.drawable.ic_play));
        items.add(new Genre(10751, "Family", R.drawable.ic_play));
        items.add(new Genre(14, "Fantasy", R.drawable.ic_play));
        items.add(new Genre(36, "History", R.drawable.ic_play));
        items.add(new Genre(27, "Horror", R.drawable.ic_play));
        items.add(new Genre(10402, "Music", R.drawable.ic_play));
        items.add(new Genre(9648, "Mystery", R.drawable.ic_play));
        items.add(new Genre(10749, "Romance", R.drawable.ic_play));
        items.add(new Genre(878, "Sci-Fi", R.drawable.ic_play));
        items.add(new Genre(10770, "TV Movie", R.drawable.ic_play));
        items.add(new Genre(53, "Thriller", R.drawable.ic_play));
        items.add(new Genre(10752, "War", R.drawable.ic_play));
        items.add(new Genre(37, "Western", R.drawable.ic_play));

        if (rvGenres != null) {
            rvGenres.setLayoutManager(new LinearLayoutManager(this));
            rvGenres.setAdapter(new GenreAdapter(items));
        }
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
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
        private List<Genre> items;

        GenreAdapter(List<Genre> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre_drawer, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Genre item = items.get(position);
            holder.tvName.setText(item.getName());
            
            if (item.getId() == -100) {
                holder.tvName.setTextColor(getResources().getColor(R.color.accent));
                holder.tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                holder.tvName.setTextColor(getResources().getColor(R.color.white));
                holder.tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            holder.itemView.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.END);
                if (item.getId() == -100) {
                    if (bottomNavigationView != null) bottomNavigationView.setSelectedItemId(R.id.nav_reels);
                    loadFragment(new ReelsFragment());
                } else {
                    if (bottomNavigationView != null) bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    loadFragment(HomeFragment.newInstance(item.getId(), item.getName()));
                }
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_genre_name);
            }
        }
    }
}
