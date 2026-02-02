package com.tusher.boiwatch.utils;

import android.content.Context;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class ReelPlayerManager {
    private static ReelPlayerManager instance;
    private ExoPlayer exoPlayer;

    private ReelPlayerManager(Context context) {
        exoPlayer = new ExoPlayer.Builder(context).build();
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
    }

    public static ReelPlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReelPlayerManager(context.getApplicationContext());
        }
        return instance;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void play(String url, PlayerView playerView) {
        playerView.setPlayer(exoPlayer);
        MediaItem mediaItem = MediaItem.fromUri(url);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
    }

    public void pause() {
        exoPlayer.pause();
    }

    public void resume() {
        exoPlayer.play();
    }

    public void release() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
            instance = null;
        }
    }

    public ExoPlayer getPlayer() {
        return exoPlayer;
    }
}
