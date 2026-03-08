package com.tusher.boiwatch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tusher.boiwatch.R;
import com.tusher.boiwatch.models.AudiobookChapter;

import java.util.List;

public class AudiobookChapterAdapter extends RecyclerView.Adapter<AudiobookChapterAdapter.ViewHolder> {

    private final Context context;
    private final List<AudiobookChapter> chapters;
    private final OnChapterClickListener listener;
    private int currentChapterIndex = -1;

    public interface OnChapterClickListener {
        void onChapterClick(AudiobookChapter chapter, int position);
    }

    public AudiobookChapterAdapter(Context context, List<AudiobookChapter> chapters, OnChapterClickListener listener) {
        this.context = context;
        this.chapters = chapters;
        this.listener = listener;
    }

    public void setCurrentChapter(int index) {
        int old = currentChapterIndex;
        currentChapterIndex = index;
        if (old >= 0 && old < chapters.size()) notifyItemChanged(old);
        if (index >= 0 && index < chapters.size()) notifyItemChanged(index);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_audiobook_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudiobookChapter chapter = chapters.get(position);
        holder.tvTime.setText(chapter.getFormattedTime());
        holder.tvTitle.setText(chapter.getTitle());

        // Highlight current chapter
        if (position == currentChapterIndex) {
            holder.tvTitle.setTextColor(context.getResources().getColor(R.color.accent));
            holder.tvTime.setAlpha(1f);
        } else {
            holder.tvTitle.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvTime.setAlpha(0.7f);
        }

        holder.itemView.setOnClickListener(v -> listener.onChapterClick(chapter, position));
    }

    @Override
    public int getItemCount() { return chapters.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTitle;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_chapter_time);
            tvTitle = itemView.findViewById(R.id.tv_chapter_title);
        }
    }
}
