package com.tusher.boiwatch.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.tusher.boiwatch.MainActivity;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.activity.AudiobookPlayerActivity;
import com.tusher.boiwatch.models.Audiobook;
import com.tusher.boiwatch.utils.AdBlocker;

import org.json.JSONObject;

public class AudiobookService extends MediaSessionService {
    private static final String TAG = "AudiobookService";
    private static final String CHANNEL_ID = "audiobook_playback_channel";
    private static final int NOTIFICATION_ID = 1001;

    private MediaSession mediaSession;
    private WebView webView;
    private Audiobook audiobook;
    private boolean isPlaying = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private float currentTime = 0;
    private float duration = 0;

    private final BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra("cmd");
            if ("toggle".equals(cmd)) {
                togglePlayPause();
            } else if ("pause".equals(cmd)) {
                if (isPlaying) togglePlayPause();
            } else if ("seek".equals(cmd)) {
                int seconds = intent.getIntExtra("seconds", 0);
                seekTo(seconds);
            } else if ("seek_relative".equals(cmd)) {
                int offset = intent.getIntExtra("offset", 0);
                seekRelative(offset);
            } else if ("close".equals(cmd)) {
                stopSelf();
            }
        }
    };

    private void seekRelative(int offset) {
        String js = "var v=document.querySelector('video');if(v) v.currentTime += " + offset + ";";
        webView.evaluateJavascript(js, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        
        // Use a dummy ExoPlayer just to satisfy MediaSession requirements for notification controls
        ExoPlayer player = new ExoPlayer.Builder(this).build();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.prepare(); // Preparation keeps the session ready
        
        mediaSession = new MediaSession.Builder(this, player).build();
        
        LocalBroadcastManager.getInstance(this).registerReceiver(controlReceiver, new IntentFilter("ACTION_AUDIOBOOK_CONTROL"));
        
        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 13; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectAdBlockJs(view);
                autoPlayAndStartPolling();
            }

            @Nullable
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, android.webkit.WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (isYouTubeAd(url) || AdBlocker.isAd(url)) {
                    return new android.webkit.WebResourceResponse("text/plain", "utf-8", new java.io.ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Audiobook Playback", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("audiobook")) {
            audiobook = (Audiobook) intent.getSerializableExtra("audiobook");
            
            // Immediately set duration from metadata so UI shows it right away
            duration = audiobook.getDurationSeconds();
            
            loadYouTube(audiobook.getVideoId());
            startForeground(NOTIFICATION_ID, buildNotification());
            
            // Broadcast initial state so the player shows duration immediately
            broadcastState();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, AudiobookPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("audiobook", audiobook);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent toggleIntent = new Intent("ACTION_AUDIOBOOK_CONTROL");
        toggleIntent.putExtra("cmd", "toggle");
        PendingIntent togglePending = PendingIntent.getBroadcast(this, 1, toggleIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(audiobook != null ? audiobook.getTitle() : "Audiobook")
                .setContentText(audiobook != null ? audiobook.getAuthor() : "Playing...")
                .setSmallIcon(R.drawable.ic_audiobook)
                .setOngoing(isPlaying)
                .setContentIntent(pendingIntent)
                .addAction(isPlaying ? android.R.drawable.ic_media_pause : R.drawable.ic_play, 
                        isPlaying ? "Pause" : "Play", togglePending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mediaSession.getSessionCompatToken()));
        
        return builder.build();
    }

    private void loadYouTube(String videoId) {
        String url = "https://m.youtube.com/watch?v=" + videoId;
        webView.loadUrl(url);
    }

    private void autoPlayAndStartPolling() {
        android.content.SharedPreferences prefs = getSharedPreferences("audiobook_prefs", MODE_PRIVATE);
        float savedTime = prefs.getFloat("pos_" + audiobook.getVideoId(), 0);

        String autoPlayJs = "(function(){" +
                "  var v = document.querySelector('video');" +
                "  if(v){" +
                "    v.muted=false; v.volume=1.0;" +
                "    if(" + savedTime + " > 0) v.currentTime = " + savedTime + ";" +
                "    v.play();" +
                "    return 'found';" +
                "  }" +
                "  return 'none';" +
                "})();";

        webView.evaluateJavascript(autoPlayJs, result -> startTimePolling());
    }

    private int adCheckCount = 0;

    private void startTimePolling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (webView == null) return;

                long expectedDuration = audiobook.getDurationSeconds();
                
                // Ensure duration is always set from metadata at minimum
                if (duration <= 0 && expectedDuration > 0) {
                    duration = expectedDuration;
                }

                String pollJs = "(function(){" +
                        "  var v = document.querySelector('video');" +
                        "  if(!v) return 'no_video';" +
                        "  " +
                        "  var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-skip-ad-button, .ytp-ad-skip-button-modern, .ytp-ad-overlay-close-button, button[class*=\"skip\"]');" +
                        "  if(skipBtn) { skipBtn.click(); }" +
                        "  " +
                        "  var d = v.duration;" +
                        "  var ct = v.currentTime;" +
                        "  var paused = v.paused;" +
                        "  if (!d || !isFinite(d) || isNaN(d)) d = 0;" +
                        "  if (!ct || isNaN(ct)) ct = 0;" +
                        "  return JSON.stringify({ct: ct, dur: d, paused: paused});" +
                        "})();";

                webView.evaluateJavascript(pollJs, result -> {
                    try {
                        if (result != null && !result.equals("\"no_video\"") && !result.equals("null")) {
                            String json = result.replace("\\\"", "\"");
                            if (json.startsWith("\"") && json.endsWith("\"")) json = json.substring(1, json.length()-1);
                            JSONObject state = new JSONObject(json);
                            currentTime = (float) state.optDouble("ct", 0);
                            float webDur = (float) state.optDouble("dur", 0);
                            isPlaying = !state.optBoolean("paused", true);

                            if (webDur > 0) {
                                if (webDur < 300 && expectedDuration > 600) {
                                    adCheckCount++;
                                    if (adCheckCount >= 3) {
                                        webView.evaluateJavascript(
                                            "var v=document.querySelector('video'); if(v) v.currentTime=v.duration;", null);
                                        adCheckCount = 0;
                                    }
                                    if (duration <= 0) duration = expectedDuration;
                                } else {
                                    duration = webDur;
                                    adCheckCount = 0;
                                }
                            }

                            // Save progress
                            if (duration > 300 || expectedDuration <= 600) {
                                getSharedPreferences("audiobook_prefs", MODE_PRIVATE).edit()
                                        .putFloat("pos_" + audiobook.getVideoId(), currentTime)
                                        .apply();
                            }
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "Poll error: " + e.getMessage());
                    }
                    
                    // ALWAYS broadcast state so the UI always gets updates
                    broadcastState();
                    handler.postDelayed(this, 1000);
                });
            }
        }, 1000);
    }

    private void broadcastState() {
        Intent intent = new Intent("ACTION_AUDIOBOOK_STATE");
        intent.putExtra("isPlaying", isPlaying);
        intent.putExtra("title", audiobook.getTitle());
        intent.putExtra("cover", audiobook.getThumbnailUrl());
        intent.putExtra("currentTime", (long) currentTime);
        intent.putExtra("duration", (long) duration);
        intent.putExtra("audiobook", audiobook);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        
        // Update notification
        Notification notification = buildNotification();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(NOTIFICATION_ID, notification);
        
        if (isPlaying) {
            startForeground(NOTIFICATION_ID, notification);
            if (mediaSession != null && !mediaSession.getPlayer().getPlayWhenReady()) {
                mediaSession.getPlayer().setPlayWhenReady(true);
            }
        } else {
            if (mediaSession != null && mediaSession.getPlayer().getPlayWhenReady()) {
                mediaSession.getPlayer().setPlayWhenReady(false);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            } else {
                stopForeground(false);
            }
        }
    }

    private void togglePlayPause() {
        String js = "var v=document.querySelector('video');if(v){ if(v.paused) v.play(); else v.pause(); }";
        webView.evaluateJavascript(js, null);
    }

    private void seekTo(int seconds) {
        String js = "var v=document.querySelector('video');if(v) v.currentTime = " + seconds + ";";
        webView.evaluateJavascript(js, null);
    }

    private void injectAdBlockJs(WebView view) {
        String js = "var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-skip-ad-button, .ytp-ad-skip-button-modern, .ytp-ad-overlay-close-button'); if(skipBtn) skipBtn.click();";
        view.evaluateJavascript(js, null);
    }

    private boolean isYouTubeAd(String url) {
        return url != null && (url.contains("googleads") || url.contains("doubleclick.net"));
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }


    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(controlReceiver);
        handler.removeCallbacksAndMessages(null);
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        if (mediaSession != null) {
            mediaSession.getPlayer().release();
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }
}
