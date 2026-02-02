package com.tusher.boiwatch.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.models.PlayerSource;
import java.util.List;

public class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.SourceViewHolder> {
    private Context context;
    private List<PlayerSource> sources;
    private int selectedPosition = 0;
    private OnSourceClickListener listener;

    public interface OnSourceClickListener {
        void onSourceClick(PlayerSource source, int position);
    }

    public SourceAdapter(Context context, List<PlayerSource> sources, OnSourceClickListener listener) {
        this.context = context;
        this.sources = sources;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_source, parent, false);
        return new SourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SourceViewHolder holder, int position) {
        PlayerSource source = sources.get(position);
        holder.tvName.setText(source.getName());

        if (selectedPosition == position) {
            holder.cardSource.setStrokeColor(context.getColor(R.color.accent));
            holder.cardSource.setCardBackgroundColor(context.getColor(R.color.primary));
            holder.tvName.setTextColor(Color.WHITE);
        } else {
            holder.cardSource.setStrokeColor(context.getColor(R.color.surface));
            holder.cardSource.setCardBackgroundColor(context.getColor(R.color.card_background));
            holder.tvName.setTextColor(context.getColor(R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            listener.onSourceClick(source, selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return sources.size();
    }

    public static class SourceViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardSource;
        TextView tvName;

        public SourceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardSource = itemView.findViewById(R.id.card_source);
            tvName = itemView.findViewById(R.id.tv_source_name);
        }
    }
}
