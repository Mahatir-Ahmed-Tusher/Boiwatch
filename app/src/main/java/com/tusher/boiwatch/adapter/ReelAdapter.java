package com.tusher.boiwatch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.models.Reel;
import com.tusher.boiwatch.utils.ReelPlayerManager;
import androidx.media3.ui.PlayerView;
import java.util.List;

public class ReelAdapter extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {

    private List<Reel> reels;

    public ReelAdapter(List<Reel> reels) {
        this.reels = reels;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reel, parent, false);
        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {
        Reel reel = reels.get(position);
        holder.bind(reel);
    }

    @Override
    public int getItemCount() {
        return reels.size();
    }

    public static class ReelViewHolder extends RecyclerView.ViewHolder {
        public PlayerView playerView;
        public TextView tvUsername, tvDescription, tvAudio, tvLikes, tvComments, btnFollow;
        public ImageView ivProfile, ivLike, ivHeartAnim, ivVolumeAnim;
        public View btnLike, btnComment, btnShare;

        public ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.player_view);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvAudio = itemView.findViewById(R.id.tv_audio);
            tvLikes = itemView.findViewById(R.id.tv_likes);
            tvComments = itemView.findViewById(R.id.tv_comments);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivHeartAnim = itemView.findViewById(R.id.iv_heart_anim);
            ivVolumeAnim = itemView.findViewById(R.id.iv_volume_anim);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnComment = itemView.findViewById(R.id.btn_comment);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnFollow = itemView.findViewById(R.id.btn_follow);
        }

        public void bind(Reel reel) {
            tvUsername.setText(reel.getReelInfo().getUsername());
            tvDescription.setText(reel.getReelInfo().getDescription());
            tvAudio.setText(reel.getReelInfo().getAudio());
            tvLikes.setText(String.valueOf(reel.getReelInfo().getLikes()));
            tvComments.setText(String.valueOf(reel.getReelInfo().getComments()));

            if (reel.getReelInfo().getProfilePicUrl() != null && !reel.getReelInfo().getProfilePicUrl().isEmpty()) {
                Picasso.get().load(reel.getReelInfo().getProfilePicUrl()).placeholder(R.drawable.ic_profile).into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.ic_profile);
            }
            
            ivLike.setImageResource(reel.getReelInfo().isLiked() ? R.drawable.ic_play : R.drawable.ic_play); // Replace with heart icons if available
            // Note: Since I don't have ic_heart, I'll keep using ic_play or similar for now, 
            // but the logic remains the same.
            
            tvAudio.setSelected(true); // For marquee effect

            // Initial state: clear player from view until selected
            playerView.setPlayer(null);
            ivHeartAnim.setVisibility(View.GONE);
            ivVolumeAnim.setVisibility(View.GONE);
        }
    }
}
