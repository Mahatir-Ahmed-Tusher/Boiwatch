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

public class AudiobookAdapter extends RecyclerView.Adapter<AudiobookAdapter.ViewHolder> {

    public interface OnAudiobookClickListener {
        void onAudiobookClick(Audiobook audiobook);
    }
    
    public interface OnAudiobookLongClickListener {
        void onAudiobookLongClick(Audiobook audiobook, int position);
    }

    private final Context context;
    private final List<Audiobook> items;
    private final OnAudiobookClickListener listener;
    private OnAudiobookLongClickListener longClickListener;

    public AudiobookAdapter(Context context, List<Audiobook> items, OnAudiobookClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnAudiobookLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_audiobook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Audiobook book = items.get(position);

        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());

        if (book.getDuration() != null && !book.getDuration().isEmpty()) {
            holder.tvDuration.setText(book.getDuration());
            holder.tvDuration.setVisibility(View.VISIBLE);
        } else {
            holder.tvDuration.setVisibility(View.GONE);
        }

        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            Picasso.get().load(book.getThumbnailUrl()).into(holder.ivThumb);
        }

        holder.itemView.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(80).withEndAction(() ->
                    listener.onAudiobookClick(book)
                ).start()
            ).start();
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onAudiobookLongClick(book, position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb, ivPlay;
        TextView tvTitle, tvAuthor, tvDuration;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_audiobook_thumb);
            ivPlay = itemView.findViewById(R.id.iv_audiobook_play);
            tvTitle = itemView.findViewById(R.id.tv_audiobook_title);
            tvAuthor = itemView.findViewById(R.id.tv_audiobook_author);
            tvDuration = itemView.findViewById(R.id.tv_audiobook_duration);
        }
    }
}
