package com.tusher.boiwatch.activity;

import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.squareup.picasso.Picasso;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.adapter.AudiobookChapterAdapter;
import com.tusher.boiwatch.api.YouTubeAudiobookService;
import com.tusher.boiwatch.models.Audiobook;
import com.tusher.boiwatch.models.AudiobookChapter;

import java.util.List;

public class AudiobookPlayerActivity extends AppCompatActivity {

    private static final String TAG = "AudiobookPlayer";

    private Audiobook audiobook;
    private ImageView ivCover, btnPlayPause, btnRewind, btnForward;
    private TextView tvTitle, tvAuthor, tvCurrentTime, tvTotalTime, tvLoadingStatus;
    private SeekBar seekBar;
    private ProgressBar progressLoading;
    private RecyclerView rvChapters;
    private View rlChaptersHeader;

    private AudiobookChapterAdapter chapterAdapter;
    private List<AudiobookChapter> chapters;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isSeekBarTracking = false;
    private boolean isPlaying = false;
    
    // Receives playback state updates from AudiobookService every second
    private final BroadcastReceiver stateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTION_AUDIOBOOK_STATE".equals(intent.getAction())) {
                boolean playing = intent.getBooleanExtra("isPlaying", false);
                long ct = intent.getLongExtra("currentTime", 0);
                long dur = intent.getLongExtra("duration", 0);
                updateUI(playing, ct, dur);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiobook_player);

        audiobook = (Audiobook) getIntent().getSerializableExtra("audiobook");
        if (audiobook == null) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        displayMetadata();
        
        // Start the background audio service
        Intent serviceIntent = new Intent(this, com.tusher.boiwatch.service.AudiobookService.class);
        serviceIntent.putExtra("audiobook", audiobook);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        // Listen for playback state broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(
                stateReceiver, new IntentFilter("ACTION_AUDIOBOOK_STATE"));
        
        // Fetch chapter info from video description
        fetchDescription(audiobook.getVideoId());
    }

    private void initViews() {
        ivCover = findViewById(R.id.iv_player_cover);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnRewind = findViewById(R.id.btn_rewind);
        btnForward = findViewById(R.id.btn_forward);
        tvTitle = findViewById(R.id.tv_player_title);
        tvAuthor = findViewById(R.id.tv_player_author);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        tvLoadingStatus = findViewById(R.id.tv_loading_status);
        seekBar = findViewById(R.id.seekbar_player);
        progressLoading = findViewById(R.id.progress_loading);
        rvChapters = findViewById(R.id.rv_chapters);
        rlChaptersHeader = findViewById(R.id.rl_chapters_header);

        progressLoading.setVisibility(View.GONE);
        tvLoadingStatus.setVisibility(View.GONE);
        
        // Play/Pause toggle
        btnPlayPause.setOnClickListener(v -> sendControl("toggle"));
        // Rewind 10 seconds
        btnRewind.setOnClickListener(v -> sendControl("seek_relative", -10));
        // Forward 10 seconds
        btnForward.setOnClickListener(v -> sendControl("seek_relative", 10));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime((long) progress * 1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                isSeekBarTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                isSeekBarTracking = false;
                sendControl("seek", sb.getProgress());
            }
        });
    }

    // ── Service Communication ──────────────────────────────────

    private void sendControl(String cmd) {
        Intent intent = new Intent("ACTION_AUDIOBOOK_CONTROL");
        intent.putExtra("cmd", cmd);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendControl(String cmd, int value) {
        Intent intent = new Intent("ACTION_AUDIOBOOK_CONTROL");
        intent.putExtra("cmd", cmd);
        if ("seek".equals(cmd)) {
            intent.putExtra("seconds", value);
        } else if ("seek_relative".equals(cmd)) {
            intent.putExtra("offset", value);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // ── UI Updates ─────────────────────────────────────────────

    /**
     * Called every second via broadcast from AudiobookService.
     * Updates play/pause icon, seekbar position, and time labels.
     */
    private void updateUI(boolean playing, long currentTimeSec, long durationSec) {
        this.isPlaying = playing;
        
        // Dynamic play/pause icon
        btnPlayPause.setImageResource(playing 
                ? android.R.drawable.ic_media_pause 
                : R.drawable.ic_play);
        
        // Use audiobook metadata as fallback if service hasn't loaded duration yet
        long totalDur = (durationSec > 0) ? durationSec : audiobook.getDurationSeconds();
        
        if (totalDur > 0) {
            seekBar.setMax((int) totalDur);
            tvTotalTime.setText(formatTime(totalDur * 1000));
            
            // Only update seekbar if user is NOT dragging it
            if (!isSeekBarTracking) {
                seekBar.setProgress((int) currentTimeSec);
                tvCurrentTime.setText(formatTime(currentTimeSec * 1000));
            }
            
            updateCurrentChapter(currentTimeSec);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_audiobook);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void displayMetadata() {
        tvTitle.setText(audiobook.getTitle());
        tvAuthor.setText(audiobook.getAuthor());
        if (audiobook.getThumbnailUrl() != null && !audiobook.getThumbnailUrl().isEmpty()) {
            Picasso.get().load(audiobook.getThumbnailUrl()).into(ivCover);
        }
        
        // Show duration from metadata immediately (before service broadcasts)
        long metaDuration = audiobook.getDurationSeconds();
        if (metaDuration > 0) {
            seekBar.setMax((int) metaDuration);
            tvTotalTime.setText(formatTime(metaDuration * 1000));
        }
    }

    // ── Helpers ────────────────────────────────────────────────

    private String formatTime(long ms) {
        long totalSeconds = ms / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", (int) hours, (int) minutes, (int) seconds);
        }
        return String.format("%d:%02d", (int) minutes, (int) seconds);
    }

    // ── Chapters ───────────────────────────────────────────────

    private void fetchDescription(String videoId) {
        YouTubeAudiobookService.getInstance().fetchVideoDescription(videoId, 
                new YouTubeAudiobookService.DescriptionCallback() {
            @Override
            public void onSuccess(String description) { parseChapters(description); }
            @Override
            public void onError(String message) { 
                Log.d(TAG, "Could not fetch description for chapters (OK)"); 
            }
        });
    }

    private void parseChapters(String description) {
        chapters = YouTubeAudiobookService.extractChapters(description);
        if (chapters != null && !chapters.isEmpty()) {
            rlChaptersHeader.setVisibility(View.VISIBLE);
            rvChapters.setVisibility(View.VISIBLE);
            chapterAdapter = new AudiobookChapterAdapter(this, chapters, (chapter, position) -> {
                sendControl("seek", (int) chapter.getStartTimeSeconds());
                chapterAdapter.setCurrentChapter(position);
            });
            rvChapters.setAdapter(chapterAdapter);
        }
    }

    private void updateCurrentChapter(long positionSec) {
        if (chapters == null || chapters.isEmpty() || chapterAdapter == null) return;
        int currentIdx = -1;
        for (int i = chapters.size() - 1; i >= 0; i--) {
            if (positionSec >= chapters.get(i).getStartTimeSeconds()) {
                currentIdx = i;
                break;
            }
        }
        if (currentIdx >= 0) chapterAdapter.setCurrentChapter(currentIdx);
    }

    // ── Lifecycle ──────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        // Simply go back — audio continues via the Foreground Service
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stateReceiver);
        handler.removeCallbacksAndMessages(null);
    }
}
