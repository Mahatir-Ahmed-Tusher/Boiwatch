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

public class FranchiseAdapter extends RecyclerView.Adapter<FranchiseAdapter.ViewHolder> {

    private Context context;
    private List<Movie> parts;

    public FranchiseAdapter(Context context, List<Movie> parts) {
        this.context = context;
        this.parts = parts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_episode, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = parts.get(position);
        holder.tvTitle.setText(movie.getTitle());
        
        String year = "";
        if (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4) {
            year = movie.getReleaseDate().substring(0, 4);
        }
        holder.tvNumber.setText("Part " + (position + 1) + " (" + year + ")");
        holder.tvOverview.setText(movie.getOverview());

        Picasso.get()
                .load(movie.getBackdropPath())
                .placeholder(R.drawable.hero_gradient)
                .into(holder.ivStill);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("movie", movie);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return parts != null ? parts.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStill;
        TextView tvTitle, tvNumber, tvOverview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStill = itemView.findViewById(R.id.iv_episode_still);
            tvTitle = itemView.findViewById(R.id.tv_episode_title);
            tvNumber = itemView.findViewById(R.id.tv_episode_number);
            tvOverview = itemView.findViewById(R.id.tv_episode_overview);
        }
    }
}
