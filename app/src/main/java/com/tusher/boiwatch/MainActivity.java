package com.tusher.boiwatch;

import android.content.Intent;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import com.squareup.picasso.Picasso;
import com.tusher.boiwatch.fragment.HomeFragment;
import com.tusher.boiwatch.fragment.LibraryFragment;
import com.tusher.boiwatch.fragment.AudiobooksFragment;
import com.tusher.boiwatch.fragment.ReelsFragment;
import com.tusher.boiwatch.fragment.SearchFragment;
import com.tusher.boiwatch.models.Genre;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView rvGenres;
    private BottomNavigationView bottomNavigationView;
    private Fragment homeFragment, searchFragment, reelsFragment, libraryFragment, audiobooksFragment;
    private Fragment activeFragment;
    private com.tusher.boiwatch.models.Audiobook currentAudiobook;
    
    // Mini-Player Views
    private View cardMiniPlayer;
    private ImageView ivMiniThumb, btnMiniPlay, btnMiniClose;
    private TextView tvMiniTitle;
    
    private final BroadcastReceiver audiobookStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTION_AUDIOBOOK_STATE".equals(intent.getAction())) {
                boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
                String title = intent.getStringExtra("title");
                String cover = intent.getStringExtra("cover");
                
                currentAudiobook = (com.tusher.boiwatch.models.Audiobook) intent.getSerializableExtra("audiobook");
                
                if (cardMiniPlayer.getVisibility() == View.GONE) {
                    cardMiniPlayer.setVisibility(View.VISIBLE);
                }
                
                tvMiniTitle.setText(title);
                btnMiniPlay.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : R.drawable.ic_play);
                
                if (cover != null && !cover.isEmpty()) {
                    Picasso.get().load(cover).into(ivMiniThumb);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        rvGenres = findViewById(R.id.rv_genres);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // Init Mini-Player
        cardMiniPlayer = findViewById(R.id.card_mini_player);
        ivMiniThumb = findViewById(R.id.iv_mini_thumb);
        tvMiniTitle = findViewById(R.id.tv_mini_title);
        btnMiniPlay = findViewById(R.id.btn_mini_play);
        btnMiniClose = findViewById(R.id.btn_mini_close);
        
        btnMiniPlay.setOnClickListener(v -> {
            Intent intent = new Intent("ACTION_AUDIOBOOK_CONTROL");
            intent.putExtra("cmd", "toggle");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        });
        
        btnMiniClose.setOnClickListener(v -> {
            Intent intent = new Intent("ACTION_AUDIOBOOK_CONTROL");
            intent.putExtra("cmd", "close");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            cardMiniPlayer.setVisibility(View.GONE);
        });
        
        cardMiniPlayer.setOnClickListener(v -> {
            if (currentAudiobook != null) {
                Intent intent = new Intent(this, com.tusher.boiwatch.activity.AudiobookPlayerActivity.class);
                intent.putExtra("audiobook", currentAudiobook);
                startActivity(intent);
            }
        });
        
        LocalBroadcastManager.getInstance(this).registerReceiver(audiobookStateReceiver, new IntentFilter("ACTION_AUDIOBOOK_STATE"));

        setupDrawer();

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            activeFragment = homeFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, homeFragment, "home")
                    .commit();
        }

        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    if (homeFragment == null) homeFragment = new HomeFragment();
                    switchFragment(homeFragment, "home");
                } else if (itemId == R.id.nav_search) {
                    if (searchFragment == null) searchFragment = new SearchFragment();
                    switchFragment(searchFragment, "search");
                } else if (itemId == R.id.nav_reels) {
                    if (reelsFragment == null) reelsFragment = new ReelsFragment();
                    switchFragment(reelsFragment, "reels");
                } else if (itemId == R.id.nav_library) {
                    if (libraryFragment == null) libraryFragment = new LibraryFragment();
                    switchFragment(libraryFragment, "library");
                } else if (itemId == R.id.nav_audiobooks) {
                    if (audiobooksFragment == null) audiobooksFragment = new AudiobooksFragment();
                    switchFragment(audiobooksFragment, "audiobooks");
                }
                return true;
            });
        }

        // Check for updates via Firebase Remote Config
        com.tusher.boiwatch.utils.UpdateHelper.checkForUpdates(this);

        handleIntentExtras();
        adjustBottomNavIconSize();
    }

    private void adjustBottomNavIconSize() {
        // Targets: Search, Audiobooks, Library
        // Home and Reels remain default (26dp from XML)
        int targetSize = (int) (32 * getResources().getDisplayMetrics().density);
        
        ViewGroup menuView = (ViewGroup) bottomNavigationView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            View itemView = menuView.getChildAt(i);
            int id = itemView.getId();
            
            if (id == R.id.nav_search || id == R.id.nav_audiobooks || id == R.id.nav_library) {
                View iconView = itemView.findViewById(com.google.android.material.R.id.navigation_bar_item_icon_view);
                if (iconView != null) {
                    ViewGroup.LayoutParams params = iconView.getLayoutParams();
                    params.width = targetSize;
                    params.height = targetSize;
                    iconView.setLayoutParams(params);
                }
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(audiobookStateReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentExtras();
    }

    private void switchFragment(Fragment fragment, String tag) {
        if (fragment == activeFragment) return;
        
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (activeFragment != null) transaction.hide(activeFragment);
        
        if (!fragment.isAdded()) {
            transaction.add(R.id.fragment_container, fragment, tag);
        } else {
            transaction.show(fragment);
        }
        
        activeFragment = fragment;
        transaction.commit();
    }

    private void handleIntentExtras() {
        if (getIntent() != null && getIntent().hasExtra("navigate_to")) {
            String target = getIntent().getStringExtra("navigate_to");
            if (target != null) {
                if (target.equals("home")) switchNav(R.id.nav_home);
                else if (target.equals("search")) switchNav(R.id.nav_search);
                else if (target.equals("reels")) switchNav(R.id.nav_reels);
                else if (target.equals("library")) switchNav(R.id.nav_library);
                else if (target.equals("audiobooks")) switchNav(R.id.nav_audiobooks);
            }
        }
    }

    private void switchNav(int itemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(itemId);
        }
    }

    public boolean loadFragment(Fragment fragment, int itemId) {
        switchNav(itemId);
        return true;
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
        switchFragment(fragment, "custom");
        return true;
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
                    switchNav(R.id.nav_reels);
                } else {
                    switchNav(R.id.nav_home);
                    if (homeFragment instanceof HomeFragment) {
                        ((HomeFragment) homeFragment).updateGenre(item.getId(), item.getName());
                    }
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
