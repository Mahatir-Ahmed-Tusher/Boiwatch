package com.tusher.boiwatch.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.activity.MovieDetailActivity;
import com.tusher.boiwatch.models.Movie;
import java.util.List;

public class HeroAdapter extends RecyclerView.Adapter<HeroAdapter.HeroViewHolder> {
    private Context context;
    private List<Movie> movies;

    public HeroAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public HeroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hero, parent, false);
        return new HeroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeroViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.tvTitle.setText(movie.getTitle());
        Picasso.get()
                .load(movie.getBackdropPath())
                .placeholder(R.drawable.placeholder_backdrop)
                .into(holder.ivBackdrop);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("movie", movie);
            context.startActivity(intent);
        });
        
        holder.btnPlay.setOnClickListener(v -> {
             Intent intent = new Intent(context, MovieDetailActivity.class);
             intent.putExtra("movie", movie);
             context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(movies.size(), 5); // Show top 5 trending
    }

    public static class HeroViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBackdrop;
        TextView tvTitle;
        View btnPlay;

        public HeroViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackdrop = itemView.findViewById(R.id.iv_hero_backdrop);
            tvTitle = itemView.findViewById(R.id.tv_hero_title);
            btnPlay = itemView.findViewById(R.id.btn_play_hero);
        }
    }
}
