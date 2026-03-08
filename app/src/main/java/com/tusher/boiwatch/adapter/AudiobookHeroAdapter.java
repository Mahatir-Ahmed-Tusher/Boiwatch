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
import com.tusher.boiwatch.models.Audiobook;

import java.util.List;

public class AudiobookHeroAdapter extends RecyclerView.Adapter<AudiobookHeroAdapter.HeroViewHolder> {

    private final Context context;
    private final List<Audiobook> audiobooks;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Audiobook audiobook);
    }

    public AudiobookHeroAdapter(Context context, List<Audiobook> audiobooks, OnItemClickListener listener) {
        this.context = context;
        this.audiobooks = audiobooks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HeroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_audiobook_hero, parent, false);
        return new HeroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeroViewHolder holder, int position) {
        Audiobook audiobook = audiobooks.get(position);

        holder.tvTitle.setText(audiobook.getTitle());
        holder.tvAuthor.setText(audiobook.getAuthor());

        if (audiobook.getThumbnailUrl() != null && !audiobook.getThumbnailUrl().isEmpty()) {
            Picasso.get()
                    .load(audiobook.getThumbnailUrl())
                    .into(holder.ivThumb);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(audiobook);
            }
        });
    }

    @Override
    public int getItemCount() {
        return audiobooks != null ? audiobooks.size() : 0;
    }

    public static class HeroViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvAuthor;

        public HeroViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_hero_thumb);
            tvTitle = itemView.findViewById(R.id.tv_hero_title);
            tvAuthor = itemView.findViewById(R.id.tv_hero_author);
        }
    }
}
