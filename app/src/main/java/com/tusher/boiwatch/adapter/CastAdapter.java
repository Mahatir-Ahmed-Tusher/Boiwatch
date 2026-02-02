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
import com.tusher.boiwatch.models.Cast;
import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private Context context;
    private List<Cast> castList;

    public CastAdapter(Context context, List<Cast> castList) {
        this.context = context;
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast cast = castList.get(position);
        holder.tvName.setText(cast.getName());
        holder.tvCharacter.setText(cast.getCharacter());
        Picasso.get().load(cast.getProfilePath()).placeholder(R.drawable.placeholder_poster).into(holder.ivProfile);
    }

    @Override
    public int getItemCount() {
        return castList.size();
    }

    static class CastViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvCharacter;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_cast_profile);
            tvName = itemView.findViewById(R.id.tv_cast_name);
            tvCharacter = itemView.findViewById(R.id.tv_cast_character);
        }
    }
}
