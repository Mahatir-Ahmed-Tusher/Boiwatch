package com.tusher.boiwatch.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.activity.MovieDetailActivity;
import com.tusher.boiwatch.models.Movie;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private List<Movie> movies;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Movie movie, int position);
    }

    public MovieAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        
        // Handle horizontal layout width
        if (parent instanceof RecyclerView) {
            RecyclerView.LayoutManager lm = ((RecyclerView) parent).getLayoutManager();
            if (lm instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) lm;
                if (layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    params.width = context.getResources().getDimensionPixelSize(R.dimen.movie_item_width_horizontal);
                    view.setLayoutParams(params);
                }
            }
        }
        
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.tvTitle.setText(movie.getTitle());
        
        // Extract year from release_date (YYYY-MM-DD)
        String year = "";
        String fullDate = movie.getReleaseDate();
        if (fullDate != null && fullDate.length() >= 4) {
            year = fullDate.substring(0, 4);
        }
        holder.tvYear.setText(year);
        
        // Format rating to one decimal
        holder.tvRating.setText(String.format("%.1f", movie.getVoteAverage()));

        Picasso.get()
                .load(movie.getPosterPath())
                .placeholder(R.drawable.placeholder_poster)
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("movie", movie);
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(movie, position);
                return true;
            }
            return false;
        });
        
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.scale_up));
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvYear, tvRating;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvYear = itemView.findViewById(R.id.tv_year);
            tvRating = itemView.findViewById(R.id.tv_rating);
        }
    }
}
