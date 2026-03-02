package com.tusher.boiwatch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.models.Episode;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.ViewHolder> {

    private Context context;
    private List<Episode> episodes;
    private OnEpisodeClickListener listener;

    public interface OnEpisodeClickListener {
        void onEpisodeClick(Episode episode);
    }

    public EpisodeAdapter(Context context, List<Episode> episodes, OnEpisodeClickListener listener) {
        this.context = context;
        this.episodes = episodes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_episode, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Episode episode = episodes.get(position);
        holder.tvTitle.setText(episode.getName());
        holder.tvNumber.setText(String.valueOf(episode.getEpisodeNumber()));
        holder.tvOverview.setText(episode.getOverview());

        if (episode.getAirDate() != null) {
            holder.tvDate.setText(episode.getAirDate());
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }

        if (episode.getRuntime() != null && episode.getRuntime() > 0) {
            holder.tvDuration.setText(episode.getRuntime() + "m");
            holder.tvDuration.setVisibility(View.VISIBLE);
        } else {
            holder.tvDuration.setVisibility(View.GONE);
        }

        Picasso.get()
                .load(episode.getStillPath())
                .placeholder(R.drawable.hero_gradient)
                .into(holder.ivStill);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEpisodeClick(episode);
            }
        });
    }

    @Override
    public int getItemCount() {
        return episodes != null ? episodes.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStill;
        TextView tvTitle, tvNumber, tvOverview, tvDate, tvDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivStill = itemView.findViewById(R.id.iv_episode_still);
            tvTitle = itemView.findViewById(R.id.tv_episode_title);
            tvNumber = itemView.findViewById(R.id.tv_episode_number);
            tvOverview = itemView.findViewById(R.id.tv_episode_overview);
            tvDate = itemView.findViewById(R.id.tv_episode_date);
            tvDuration = itemView.findViewById(R.id.tv_episode_duration);
        }
    }
}
