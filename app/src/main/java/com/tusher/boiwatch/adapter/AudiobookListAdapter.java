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

public class AudiobookListAdapter extends RecyclerView.Adapter<AudiobookListAdapter.ViewHolder> {

    private final Context context;
    private final List<Audiobook> items;
    private final AudiobookAdapter.OnAudiobookClickListener listener;

    public AudiobookListAdapter(Context context, List<Audiobook> items, AudiobookAdapter.OnAudiobookClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_audiobook_list, parent, false);
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

        holder.itemView.setOnClickListener(v -> listener.onAudiobookClick(book));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvAuthor, tvDuration;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_audiobook_thumb);
            tvTitle = itemView.findViewById(R.id.tv_audiobook_title);
            tvAuthor = itemView.findViewById(R.id.tv_audiobook_author);
            tvDuration = itemView.findViewById(R.id.tv_audiobook_duration);
        }
    }
}
