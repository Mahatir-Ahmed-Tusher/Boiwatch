package com.tusher.boiwatch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.models.ShortItem;
import android.webkit.JavascriptInterface;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class ShortsAdapter extends RecyclerView.Adapter<ShortsAdapter.ViewHolder> {

    public interface OnVideoEndListener {
        void onVideoEnd(int position);
    }

    private List<ShortItem> shortItems;
    private String htmlTemplate;
    private OnVideoEndListener onVideoEndListener;

    public ShortsAdapter(List<ShortItem> shortItems, OnVideoEndListener listener) {
        this.shortItems = shortItems;
        this.onVideoEndListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (htmlTemplate == null) {
            htmlTemplate = loadTemplateFromAssets(parent.getContext());
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_short, parent, false);
        return new ViewHolder(view, htmlTemplate, onVideoEndListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShortItem item = shortItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return shortItems.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycle();
    }

    private String loadTemplateFromAssets(Context context) {
        if (context == null) return "";
        try (InputStream is = context.getAssets().open("reels_proxy_template.html");
             Scanner s = new Scanner(is).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        WebView webView;
        TextView tvTitle, tvChannel, tvDesc, tvLikes;
        ImageButton btnLike, btnShare;
        ProgressBar progressBar;
        String template;
        boolean isLiked = false;
        boolean isLoaded = false;
        ShortItem currentItem;
        OnVideoEndListener listener;

        public ViewHolder(@NonNull View itemView, String template, OnVideoEndListener listener) {
            super(itemView);
            this.template = template;
            this.listener = listener;
            webView = itemView.findViewById(R.id.short_webview);
            tvTitle = itemView.findViewById(R.id.tv_short_title);
            tvChannel = itemView.findViewById(R.id.tv_short_channel);
            tvDesc = itemView.findViewById(R.id.tv_short_desc);
            tvLikes = itemView.findViewById(R.id.tv_short_like_count);
            btnLike = itemView.findViewById(R.id.btn_short_like);
            btnShare = itemView.findViewById(R.id.btn_short_share);
            progressBar = itemView.findViewById(R.id.short_loading);
            
            setupWebView();
        }

        @SuppressLint("SetJavaScriptEnabled")
        private void setupWebView() {
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setMediaPlaybackRequiresUserGesture(false);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            
            webView.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void onVideoEnd() {
                    webView.post(() -> {
                        if (listener != null) {
                            listener.onVideoEnd(getBindingAdapterPosition());
                        }
                    });
                }
            }, "Android");
            
            // Set real browser User-Agent to satisfy YouTube requirements
            settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Mobile Safari/537.36");
            
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    progressBar.setVisibility(View.GONE);
                }
            });
            webView.setWebChromeClient(new WebChromeClient());
        }

        public void bind(ShortItem item) {
            this.currentItem = item;
            tvTitle.setText(item.getTitle());
            tvChannel.setText(item.getChannel());
            tvDesc.setText(item.getDescription());
            
            // Initial state: don't load the URL yet, wait for 'play()' call when visible
            progressBar.setVisibility(View.GONE);
            
            btnLike.setOnClickListener(v -> {
                isLiked = !isLiked;
                btnLike.setImageResource(R.drawable.ic_check);
                btnLike.setColorFilter(itemView.getContext().getColor(isLiked ? R.color.accent : R.color.white));
                tvLikes.setText(isLiked ? "1" : "Like");
            });

            btnShare.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Check out this movie moment on BoiWatch: https://www.youtube.com/shorts/" + item.getId());
                itemView.getContext().startActivity(Intent.createChooser(intent, "Share via"));
            });
        }

        public void play() {
            if (webView == null || currentItem == null || template == null || template.isEmpty()) return;
            
            try {
                if (!isLoaded) {
                    isLoaded = true;
                    progressBar.setVisibility(View.VISIBLE);
                    String generatedHtml = String.format(template, currentItem.getId());
                    webView.loadDataWithBaseURL("https://boiwatch.app", generatedHtml, "text/html", "UTF-8", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                isLoaded = false;
                progressBar.setVisibility(View.GONE);
            }
        }

        public void recycle() {
            if (webView == null) return;
            try {
                isLoaded = false;
                webView.stopLoading();
                webView.loadUrl("about:blank");
                progressBar.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
