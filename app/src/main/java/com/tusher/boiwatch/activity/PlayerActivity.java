package com.tusher.boiwatch.activity;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector;
import android.media.AudioManager;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tusher.boiwatch.Constants;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.adapter.SourceAdapter;
import com.tusher.boiwatch.models.Movie;
import com.tusher.boiwatch.models.PlayerSource;
import com.tusher.boiwatch.utils.AdBlocker;
import com.tusher.boiwatch.utils.PlayerProvider;
import java.io.ByteArrayInputStream;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";
    private WebView webView;
    private ProgressBar progressBar;
    private View playerControls;
    private Movie movie;
    private int season = -1;
    private int episode = -1;
    private SharedPreferences prefs;
    private String currentBaseUrl = "";
    private List<PlayerSource> availableSources;
    private int currentSourceIndex = 0;
    private boolean isErrorOccurred = false;

    private List<com.tusher.boiwatch.models.Episode> episodeList;
    private int currentEpisodeIndex = -1;
    private View btnPrevEpisode;
    private View btnNextEpisode;
    private View llTvControls;

    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable = this::tryNextSource;

    private Handler hideControlsHandler = new Handler(Looper.getMainLooper());
    private Runnable hideControlsRunnable = this::hideControls;
    private static final long CONTROLS_HIDE_DELAY = 3000; // 3 seconds

    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private FrameLayout fullscreenContainer;

    // Gesture controls members
    private GestureDetector gestureDetector;
    private AudioManager audioManager;
    private float currentBrightness = -1f;
    private int maxVolume;
    private View gestureIndicator;
    private ImageView ivGestureIcon;
    private TextView tvGestureText;
    private Handler gestureHandler = new Handler();
    private Runnable hideGestureIndicator = () -> {
        if (gestureIndicator != null) gestureIndicator.setVisibility(View.GONE);
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        movie = (Movie) getIntent().getSerializableExtra("movie");
        if (movie == null) {
            finish();
            return;
        }

        season = getIntent().getIntExtra("season", -1);
        episode = getIntent().getIntExtra("episode", -1);

        if (getIntent().hasExtra("episode_list")) {
            episodeList = (List<com.tusher.boiwatch.models.Episode>) getIntent().getSerializableExtra("episode_list");
            currentEpisodeIndex = getIntent().getIntExtra("current_episode_index", -1);
        }

        prefs = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        
        initViews();
        loadSources();
        startHideControlsTimer();
        
        // Auto-pause background audiobook if playing
        android.content.Intent pauseIntent = new android.content.Intent("ACTION_AUDIOBOOK_CONTROL");
        pauseIntent.putExtra("cmd", "pause");
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(pauseIntent);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        webView = findViewById(R.id.webview_player);
        progressBar = findViewById(R.id.player_progress);
        playerControls = findViewById(R.id.player_controls);
        TextView tvTitle = findViewById(R.id.tv_player_movie_title);
        ImageView ivServerSelect = findViewById(R.id.iv_server_select);

        String title = movie.getTitle();
        if (season != -1 && episode != -1) {
            title += " - S" + season + " E" + episode;
        }
        tvTitle.setText(title);

        Toolbar toolbar = findViewById(R.id.player_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        ivServerSelect.setOnClickListener(v -> {
            resetHideControlsTimer();
            if (availableSources != null) {
                showServerSelectionDialog(availableSources);
            }
        });

        llTvControls = findViewById(R.id.ll_tv_controls);
        btnPrevEpisode = findViewById(R.id.btn_prev_episode);
        btnNextEpisode = findViewById(R.id.btn_next_episode);

        if (season != -1 && episodeList != null) {
            llTvControls.setVisibility(View.VISIBLE);
            updateButtonStates();
            btnPrevEpisode.setOnClickListener(v -> playPrevEpisode());
            btnNextEpisode.setOnClickListener(v -> playNextEpisode());
        } else {
            if (llTvControls != null) llTvControls.setVisibility(View.GONE);
        }

        setupGestureControls();
        initGestureViews();
        setupWebView();
        
        // Handle clicks on controls layout to toggle visibility or reset timer
        playerControls.setOnClickListener(v -> toggleControls());
        
        setFullscreen();
    }

    private void updateTitle() {
        TextView tvTitle = findViewById(R.id.tv_player_movie_title);
        String title = movie.getTitle();
        if (season != -1 && episode != -1) {
            title += " - S" + season + " E" + episode;
        }
        tvTitle.setText(title);
    }

    private void updateButtonStates() {
        if (episodeList == null || btnPrevEpisode == null || btnNextEpisode == null) return;
        btnPrevEpisode.setAlpha(currentEpisodeIndex > 0 ? 1.0f : 0.4f);
        btnPrevEpisode.setEnabled(currentEpisodeIndex > 0);
        
        btnNextEpisode.setAlpha(currentEpisodeIndex < episodeList.size() - 1 ? 1.0f : 0.4f);
        btnNextEpisode.setEnabled(currentEpisodeIndex < episodeList.size() - 1);
    }

    private void playNextEpisode() {
        if (episodeList != null && currentEpisodeIndex < episodeList.size() - 1) {
            currentEpisodeIndex++;
            switchEpisode(episodeList.get(currentEpisodeIndex));
        }
    }

    private void playPrevEpisode() {
        if (episodeList != null && currentEpisodeIndex > 0) {
            currentEpisodeIndex--;
            switchEpisode(episodeList.get(currentEpisodeIndex));
        }
    }

    private void switchEpisode(com.tusher.boiwatch.models.Episode newEpisode) {
        season = newEpisode.getSeasonNumber();
        episode = newEpisode.getEpisodeNumber();
        updateTitle();
        updateButtonStates();
        resetHideControlsTimer();
        Toast.makeText(this, "Playing S" + season + " E" + episode, Toast.LENGTH_SHORT).show();
        loadSources();
    }

    private void toggleControls() {
        if (playerControls.getVisibility() == View.VISIBLE) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        playerControls.setVisibility(View.VISIBLE);
        setFullscreen();
        startHideControlsTimer();
    }

    private void hideControls() {
        playerControls.setVisibility(View.GONE);
        setFullscreen();
    }

    private void startHideControlsTimer() {
        hideControlsHandler.removeCallbacks(hideControlsRunnable);
        hideControlsHandler.postDelayed(hideControlsRunnable, CONTROLS_HIDE_DELAY);
    }

    private void resetHideControlsTimer() {
        if (playerControls.getVisibility() == View.VISIBLE) {
            startHideControlsTimer();
        }
    }

    private void loadSources() {
        progressBar.setVisibility(View.VISIBLE);
        if (season != -1 && episode != -1) {
            availableSources = PlayerProvider.getTVPlayers(movie.getId(), season, episode);
        } else {
            availableSources = PlayerProvider.getMoviePlayers(movie.getId(), 0);
        }
        
        if (!availableSources.isEmpty()) {
            currentSourceIndex = 0;
            loadSource(availableSources.get(currentSourceIndex).getUrl());
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "No sources found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void loadSource(String url) {
        this.currentBaseUrl = url;
        this.isErrorOccurred = false;
        
        // Start fallback timer (15 seconds)
        timeoutHandler.removeCallbacks(timeoutRunnable);
        timeoutHandler.postDelayed(timeoutRunnable, 15000);
        
        webView.loadUrl(url);
    }

    private void tryNextSource() {
        if (availableSources == null || availableSources.isEmpty()) return;

        currentSourceIndex++;
        if (currentSourceIndex < availableSources.size()) {
            String nextUrl = availableSources.get(currentSourceIndex).getUrl();
            String nextName = availableSources.get(currentSourceIndex).getName();
            Toast.makeText(this, "Link unstable. Switching to " + nextName + "...", Toast.LENGTH_SHORT).show();
            loadSource(nextUrl);
        } else {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            Toast.makeText(this, "All servers failed. Try switching servers manually.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void initGestureViews() {
        gestureIndicator = findViewById(R.id.cv_gesture_indicator);
        ivGestureIcon = findViewById(R.id.iv_gesture_icon);
        tvGestureText = findViewById(R.id.tv_gesture_text);
    }

    private void showGestureIndicator(int iconRes, String text) {
        if (gestureIndicator == null) return;
        
        gestureHandler.removeCallbacks(hideGestureIndicator);
        ivGestureIcon.setImageResource(iconRes);
        tvGestureText.setText(text);
        gestureIndicator.setVisibility(View.VISIBLE);
        gestureHandler.postDelayed(hideGestureIndicator, 1500);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupGestureControls() {
        audioManager = (AudioManager) getSystemService(Context.MODE_PRIVATE != 0 ? Context.AUDIO_SERVICE : Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float x = e.getX();
                if (x < webView.getWidth() / 2) {
                    seekBy(-10);
                    showGestureIndicator(R.drawable.ic_fast_rewind, "-10s");
                } else {
                    seekBy(10);
                    showGestureIndicator(R.drawable.ic_fast_forward, "+10s");
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e1 == null || e2 == null) return false;

                float x = e1.getX();
                float deltaY = e1.getY() - e2.getY(); // Positive means sliding UP
                float percent = deltaY / webView.getHeight();

                if (x < webView.getWidth() / 2) {
                    adjustBrightness(percent);
                } else {
                    adjustVolume(percent);
                }
                return true;
            }
        });
    }

    private void adjustVolume(float percent) {
        int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int delta = (int) (percent * maxVolume * 1.5f); // Sensitivity boost
        int newVol = Math.max(0, Math.min(maxVolume, currentVol + delta));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);
        
        int volPercent = (int) ((newVol / (float) maxVolume) * 100);
        showGestureIndicator(R.drawable.ic_volume_on, volPercent + "%");
    }

    private void adjustBrightness(float percent) {
        if (currentBrightness == -1f) {
            currentBrightness = getWindow().getAttributes().screenBrightness;
            if (currentBrightness < 0) currentBrightness = 0.5f; // Default if not set
        }

        currentBrightness = Math.max(0.01f, Math.min(1.0f, currentBrightness + percent * 1.5f));
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = currentBrightness;
        getWindow().setAttributes(layoutParams);

        int briPercent = (int) (currentBrightness * 100);
        showGestureIndicator(R.drawable.ic_brightness, briPercent + "%");
    }

    private void seekBy(int seconds) {
        String js = "javascript:(function() {" +
                "  var videos = document.getElementsByTagName('video');" +
                "  for (var i = 0; i < videos.length; i++) {" +
                "    videos[i].currentTime += " + seconds + ";" +
                "  }" +
                "})()";
        webView.evaluateJavascript(js, null);
    }

    private void showServerSelectionDialog(List<PlayerSource> sources) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.GlassmorphicDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_server_selection, null);
        RecyclerView rvSources = view.findViewById(R.id.rv_sources_dialog);
        
        SourceAdapter adapter = new SourceAdapter(this, sources, (source, position) -> {
            currentSourceIndex = position;
            loadSource(source.getUrl());
            dialog.dismiss();
        });
        rvSources.setAdapter(adapter);
        
        dialog.setContentView(view);
        
        // Fix: Force the BottomSheet to open fully expanded so servers are immediately visible
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;
            View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        
        dialog.show();
    }

    public class WebAppInterface {
        @android.webkit.JavascriptInterface
        public void onVideoEnded() {
            runOnUiThread(() -> {
                if (episodeList != null && currentEpisodeIndex < episodeList.size() - 1) {
                    Toast.makeText(PlayerActivity.this, "Auto-playing next episode...", Toast.LENGTH_SHORT).show();
                    playNextEpisode();
                }
            });
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setSupportMultipleWindows(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // This is key: catch touch events on WebView to show controls and handle gestures
        webView.setOnTouchListener((v, event) -> {
            boolean gestureHandled = gestureDetector.onTouchEvent(event);
            
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                showControls();
            }
            
            // If the gesture detector handled it (scroll/double tap), we still return false
            // to allow the WebView to receive events if needed, BUT we've done our action.
            return gestureHandled; 
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                injectAntiRedirectJS(view);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                timeoutHandler.removeCallbacks(timeoutRunnable); // Stop the timeout timer
                injectAntiRedirectJS(view);
                injectAutoPlayJS(view);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame() && !isErrorOccurred) {
                    isErrorOccurred = true;
                    tryNextSource();
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (request.isForMainFrame() && errorResponse.getStatusCode() >= 400 && !isErrorOccurred) {
                    isErrorOccurred = true;
                    tryNextSource();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (isAllowedUrl(request.getUrl().toString())) return false;
                return true; 
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (AdBlocker.isAd(request.getUrl().toString())) {
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    onHideCustomView();
                    return;
                }
                mCustomView = view;
                mCustomViewCallback = callback;
                
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                
                fullscreenContainer = new FrameLayout(PlayerActivity.this);
                fullscreenContainer.setBackgroundColor(getResources().getColor(android.R.color.black));
                fullscreenContainer.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                
                // Add gesture support to the fullscreen container as well
                fullscreenContainer.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
                
                ((ViewGroup) getWindow().getDecorView()).addView(fullscreenContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                
                // Reparent the gesture indicator to the fullscreen container to keep it on top
                if (gestureIndicator != null && gestureIndicator.getParent() != null) {
                    ((ViewGroup) gestureIndicator.getParent()).removeView(gestureIndicator);
                    fullscreenContainer.addView(gestureIndicator);
                }
                
                playerControls.setVisibility(View.GONE);
                webView.setVisibility(View.GONE);
                
                setFullscreen();
            }

            @Override
            public void onHideCustomView() {
                if (mCustomView == null) return;
                
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                
                ((ViewGroup) getWindow().getDecorView()).removeView(fullscreenContainer);
                
                // Restore the gesture indicator to the main layout
                if (gestureIndicator != null && gestureIndicator.getParent() != null) {
                    ((ViewGroup) gestureIndicator.getParent()).removeView(gestureIndicator);
                    ((ViewGroup) findViewById(R.id.player_controls)).addView(gestureIndicator);
                }
                
                fullscreenContainer = null;
                mCustomView = null;
                mCustomViewCallback.onCustomViewHidden();
                
                playerControls.setVisibility(View.VISIBLE);
                webView.setVisibility(View.VISIBLE);
                
                setFullscreen();
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                return false;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(TAG, "Console: " + consoleMessage.message());
                return true;
            }
        });
    }

    private boolean isAllowedUrl(String url) {
        if (url.startsWith("data:") || url.startsWith("blob:")) return true;
        if (!currentBaseUrl.isEmpty()) {
            try {
                java.net.URI baseUri = new java.net.URI(currentBaseUrl);
                java.net.URI targetUri = new java.net.URI(url);
                if (baseUri.getHost() != null && targetUri.getHost() != null && targetUri.getHost().endsWith(baseUri.getHost())) return true;
            } catch (Exception ignored) {}
        }
        return url.contains(".m3u8") || url.contains(".mp4") || url.contains(".ts") || url.contains("googlevideo.com");
    }

    private void injectAntiRedirectJS(WebView view) {
        String js = "javascript:(function() {" +
                "window.open = function() { return null; };" +
                "window.alert = function() { return null; };" +
                "window.onbeforeunload = null;" +
                "var originalLocation = window.location;" +
                "Object.defineProperty(window, 'location', {" +
                "  configurable: false," +
                "  enumerable: true," +
                "  get: function() { return originalLocation; }," +
                "  set: function(val) { console.log('Blocked location change to: ' + val); }" +
                "});" +
                "})()";
        view.evaluateJavascript(js, null);
    }

    private void injectAutoPlayJS(WebView view) {
        String js = "javascript:(function() {" +
                "  var videos = document.getElementsByTagName('video');" +
                "  if (videos.length > 0) {" +
                "    var v = videos[0];" +
                "    v.removeEventListener('ended', window.boiwatchVideoEndedListener);" +
                "    window.boiwatchVideoEndedListener = function() { Android.onVideoEnded(); };" +
                "    v.addEventListener('ended', window.boiwatchVideoEndedListener);" +
                "  }" +
                "})()";
        view.evaluateJavascript(js, null);
    }

    @Override
    public void onBackPressed() {
        if (mCustomView != null) {
            webView.getWebChromeClient().onHideCustomView();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        webView.pauseTimers();
        timeoutHandler.removeCallbacks(timeoutRunnable);
        hideControlsHandler.removeCallbacks(hideControlsRunnable);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational aspectRatio = new Rational(16, 9);
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
            pipBuilder.setAspectRatio(aspectRatio);
            enterPictureInPictureMode(pipBuilder.build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            // Hide all controls in PiP mode
            if (playerControls != null) playerControls.setVisibility(View.GONE);
            Toolbar toolbar = findViewById(R.id.player_toolbar);
            if (toolbar != null) toolbar.setVisibility(View.GONE);
        } else {
            // Show controls back when exiting PiP mode
            if (playerControls != null) playerControls.setVisibility(View.VISIBLE);
            Toolbar toolbar = findViewById(R.id.player_toolbar);
            if (toolbar != null) toolbar.setVisibility(View.VISIBLE);
            setFullscreen();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        webView.resumeTimers();
        setFullscreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeoutHandler.removeCallbacks(timeoutRunnable);
        hideControlsHandler.removeCallbacks(hideControlsRunnable);
        webView.destroy();
    }
}
