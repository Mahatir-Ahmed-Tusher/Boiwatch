package com.tusher.boiwatch.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable = this::tryNextSource;

    private Handler hideControlsHandler = new Handler(Looper.getMainLooper());
    private Runnable hideControlsRunnable = this::hideControls;
    private static final long CONTROLS_HIDE_DELAY = 3000; // 3 seconds

    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private FrameLayout fullscreenContainer;

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

        prefs = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        
        initViews();
        loadSources();
        startHideControlsTimer();
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

        setupWebView();
        
        // Handle clicks on controls layout to toggle visibility or reset timer
        playerControls.setOnClickListener(v -> toggleControls());
        
        setFullscreen();
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

        // This is key: catch touch events on WebView to show controls
        webView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                showControls();
            }
            return false; // Don't consume so WebView still works (scrolling, clicking player buttons)
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
                
                ((ViewGroup) getWindow().getDecorView()).addView(fullscreenContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                playerControls.setVisibility(View.GONE);
                webView.setVisibility(View.GONE);
                
                setFullscreen();
            }

            @Override
            public void onHideCustomView() {
                if (mCustomView == null) return;
                
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                
                ((ViewGroup) getWindow().getDecorView()).removeView(fullscreenContainer);
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

    @Override
    public void onBackPressed() {
        if (mCustomView != null) {
            webView.getWebChromeClient().onHideCustomView();
        } else if (webView.canGoBack()) {
            webView.goBack();
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
