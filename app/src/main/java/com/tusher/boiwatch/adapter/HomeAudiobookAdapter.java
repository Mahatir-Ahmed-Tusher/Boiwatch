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

public class HomeAudiobookAdapter extends RecyclerView.Adapter<HomeAudiobookAdapter.ViewHolder> {

    private final Context context;
    private final List<Audiobook> audiobooks;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Audiobook audiobook);
    }

    public HomeAudiobookAdapter(Context context, List<Audiobook> audiobooks, OnItemClickListener listener) {
        this.context = context;
        this.audiobooks = audiobooks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_home_audiobook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Audiobook book = audiobooks.get(position);

        holder.tvTitle.setText(book.getTitle());

        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            Picasso.get().load(book.getThumbnailUrl()).into(holder.ivThumb);
        }

        holder.itemView.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).withEndAction(() -> {
                        if (listener != null) {
                            listener.onItemClick(book);
                        }
                    }).start()
            ).start();
        });
    }

    @Override
    public int getItemCount() {
        return audiobooks != null ? audiobooks.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb, ivPlay;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_audiobook_thumb);
            ivPlay = itemView.findViewById(R.id.iv_audiobook_play);
            tvTitle = itemView.findViewById(R.id.tv_audiobook_title);
        }
    }
}
