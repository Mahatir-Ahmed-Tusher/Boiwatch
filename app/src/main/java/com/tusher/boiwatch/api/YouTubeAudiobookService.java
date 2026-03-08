package com.tusher.boiwatch.api;

import android.util.Log;

import com.tusher.boiwatch.models.Audiobook;
import com.tusher.boiwatch.models.AudiobookChapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class YouTubeAudiobookService {

    private static final String TAG = "YTAudiobookService";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String YOUTUBE_API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
    private static final long MIN_DURATION_SECONDS = 600; // 10 min

    private static final String[] AUDIOBOOK_KEYWORDS = {
            "audiobook", "audio book", "full audiobook", "complete audiobook",
            "chapter", "novel", "full book", "narrated"
    };

    private final OkHttpClient client;
    private final ExecutorService executor;
    private static YouTubeAudiobookService instance;

    public static synchronized YouTubeAudiobookService getInstance() {
        if (instance == null) {
            instance = new YouTubeAudiobookService();
        }
        return instance;
    }

    private YouTubeAudiobookService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
        executor = Executors.newFixedThreadPool(3);
    }

    // ── Callbacks ──────────────────────────────────────────────

    public interface SearchCallback {
        void onSuccess(List<Audiobook> results);
        void onError(String message);
    }

    public interface StreamCallback {
        void onSuccess(String audioUrl, String description);
        void onError(String message);
    }

    public interface DescriptionCallback {
        void onSuccess(String description);
        void onError(String message);
    }

    // ── Search (InnerTube — this was working before) ──────────

    public void searchAudiobooks(String query, SearchCallback callback) {
        executor.execute(() -> {
            try {
                String fullQuery = query + " audiobook";
                List<Audiobook> results = innerTubeSearch(fullQuery);

                if (results.isEmpty()) {
                    // Retry without "audiobook" suffix in case query already has it
                    results = innerTubeSearch(query);
                }

                List<Audiobook> finalResults = results;
                postOnMain(() -> callback.onSuccess(finalResults));
            } catch (Exception e) {
                Log.e(TAG, "Search error", e);
                postOnMain(() -> callback.onError("Search failed: " + e.getMessage()));
            }
        });
    }

    /**
     * Fetch just the video description (for chapter extraction).
     * Uses a lightweight InnerTube player call — no stream extraction.
     */
    public void fetchVideoDescription(String videoId, DescriptionCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject clientObj = new JSONObject();
                clientObj.put("clientName", "WEB");
                clientObj.put("clientVersion", "2.20241126.01.00");
                clientObj.put("gl", "US");
                clientObj.put("hl", "en");

                JSONObject context = new JSONObject();
                context.put("client", clientObj);

                JSONObject body = new JSONObject();
                body.put("videoId", videoId);
                body.put("context", context);

                Request request = new Request.Builder()
                        .url("https://www.youtube.com/youtubei/v1/player?key=" + YOUTUBE_API_KEY)
                        .post(RequestBody.create(body.toString(), JSON_TYPE))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONObject videoDetails = json.optJSONObject("videoDetails");
                    String description = videoDetails != null ?
                            videoDetails.optString("shortDescription", "") : "";
                    postOnMain(() -> callback.onSuccess(description));
                } else {
                    postOnMain(() -> callback.onError("Failed to fetch description"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Description fetch error", e);
                postOnMain(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private List<Audiobook> innerTubeSearch(String query) {
        List<Audiobook> results = new ArrayList<>();
        try {
            JSONObject clientObj = new JSONObject();
            clientObj.put("clientName", "WEB");
            clientObj.put("clientVersion", "2.20241126.01.00");
            clientObj.put("gl", "US");
            clientObj.put("hl", "en");

            JSONObject context = new JSONObject();
            context.put("client", clientObj);

            JSONObject body = new JSONObject();
            body.put("query", query);
            body.put("context", context);

            Request request = new Request.Builder()
                    .url("https://www.youtube.com/youtubei/v1/search?key=" + YOUTUBE_API_KEY)
                    .post(RequestBody.create(body.toString(), JSON_TYPE))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful() || response.body() == null) {
                Log.e(TAG, "Search HTTP error: " + response.code());
                return results;
            }

            String responseBody = response.body().string();
            JSONObject root = new JSONObject(responseBody);

            JSONObject contents = root.optJSONObject("contents");
            if (contents == null) return results;

            JSONObject twoColumn = contents.optJSONObject("twoColumnSearchResultsRenderer");
            if (twoColumn == null) return results;

            JSONArray sections = twoColumn.getJSONObject("primaryContents")
                    .getJSONObject("sectionListRenderer")
                    .getJSONArray("contents");

            for (int i = 0; i < sections.length(); i++) {
                JSONObject section = sections.getJSONObject(i);
                JSONObject itemSection = section.optJSONObject("itemSectionRenderer");
                if (itemSection == null) continue;

                JSONArray items = itemSection.getJSONArray("contents");
                for (int j = 0; j < items.length(); j++) {
                    JSONObject item = items.getJSONObject(j);
                    JSONObject vr = item.optJSONObject("videoRenderer");
                    if (vr == null) continue;

                    Audiobook book = parseVideoRenderer(vr);
                    if (book != null && isLikelyAudiobook(book)) {
                        results.add(book);
                    }
                }
            }

            Log.d(TAG, "InnerTube search returned " + results.size() + " audiobook results");
        } catch (Exception e) {
            Log.e(TAG, "InnerTube search error", e);
        }
        return results;
    }

    private boolean isLikelyAudiobook(Audiobook book) {
        // Accept if duration is long enough
        if (book.getDurationSeconds() >= MIN_DURATION_SECONDS) return true;

        // Accept if title contains audiobook keywords regardless of duration
        String titleLower = book.getTitle().toLowerCase();
        for (String kw : AUDIOBOOK_KEYWORDS) {
            if (titleLower.contains(kw)) return true;
        }
        return false;
    }

    // ── Stream extraction ──────────────────────────────────────

    public void getAudioStream(String videoId, StreamCallback callback) {
        executor.execute(() -> {
            Log.d(TAG, "=== Getting audio stream for videoId: " + videoId + " ===");

            // Strategy 1: Piped API (public YouTube proxy — returns pre-decoded URLs)
            String[] pipedResult = tryPipedApi(videoId);
            if (pipedResult != null) {
                Log.d(TAG, "SUCCESS via Piped API");
                postOnMain(() -> callback.onSuccess(pipedResult[0], pipedResult[1]));
                return;
            }

            // Strategy 2: Invidious API (another public YouTube proxy)
            String[] invidiousResult = tryInvidiousApi(videoId);
            if (invidiousResult != null) {
                Log.d(TAG, "SUCCESS via Invidious API");
                postOnMain(() -> callback.onSuccess(invidiousResult[0], invidiousResult[1]));
                return;
            }

            // Strategy 3: InnerTube player API direct
            String[] innertubeResult = tryInnerTubePlayer(videoId);
            if (innertubeResult != null) {
                Log.d(TAG, "SUCCESS via InnerTube player");
                postOnMain(() -> callback.onSuccess(innertubeResult[0], innertubeResult[1]));
                return;
            }

            Log.e(TAG, "All extraction strategies failed for " + videoId);
            postOnMain(() -> callback.onError("Could not load audio. Please try another audiobook."));
        });
    }

    /**
     * Piped API — free, open-source YouTube proxy.
     * Returns fully decoded stream URLs ready for ExoPlayer.
     */
    private String[] tryPipedApi(String videoId) {
        String[] instances = {
                "https://pipedapi.kavin.rocks",
                "https://pipedapi.r4fo.com",
                "https://pipedapi.adminforge.de",
                "https://api.piped.projectsegfault.com"
        };

        for (String instance : instances) {
            try {
                Log.d(TAG, "Trying Piped: " + instance);
                String url = instance + "/streams/" + videoId;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "BoiWatch/2.0")
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    Log.w(TAG, "Piped " + instance + " HTTP " + response.code());
                    continue;
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                // Check for errors
                if (json.has("error")) {
                    Log.w(TAG, "Piped error: " + json.getString("error"));
                    continue;
                }

                // Get audio streams
                JSONArray audioStreams = json.optJSONArray("audioStreams");
                if (audioStreams == null || audioStreams.length() == 0) {
                    Log.w(TAG, "Piped: no audio streams");
                    continue;
                }

                // Find best audio stream (prefer m4a/mp4 for ExoPlayer compatibility)
                String bestUrl = null;
                int bestBitrate = 0;
                String bestMp4Url = null;
                int bestMp4Bitrate = 0;

                for (int i = 0; i < audioStreams.length(); i++) {
                    JSONObject stream = audioStreams.getJSONObject(i);
                    String streamUrl = stream.optString("url", "");
                    int bitrate = stream.optInt("bitrate", 0);
                    String mimeType = stream.optString("mimeType", "");
                    String codec = stream.optString("codec", "");

                    if (streamUrl.isEmpty()) continue;

                    // Prefer m4a/mp4 for better ExoPlayer compatibility
                    if (mimeType.contains("mp4") || mimeType.contains("m4a") || codec.contains("mp4a")) {
                        if (bitrate > bestMp4Bitrate) {
                            bestMp4Bitrate = bitrate;
                            bestMp4Url = streamUrl;
                        }
                    }

                    if (bitrate > bestBitrate) {
                        bestBitrate = bitrate;
                        bestUrl = streamUrl;
                    }
                }

                // Prefer mp4 audio, fallback to any audio
                String selectedUrl = bestMp4Url != null ? bestMp4Url : bestUrl;
                if (selectedUrl == null) continue;

                String description = json.optString("description", "");
                Log.d(TAG, "Piped: found audio stream, bitrate=" + Math.max(bestMp4Bitrate, bestBitrate));
                return new String[]{selectedUrl, description};

            } catch (Exception e) {
                Log.w(TAG, "Piped " + instance + " error: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Invidious API — another YouTube proxy with decoded streams.
     */
    private String[] tryInvidiousApi(String videoId) {
        String[] instances = {
                "https://inv.nadeko.net",
                "https://invidious.privacyredirect.com",
                "https://vid.puffyan.us"
        };

        for (String instance : instances) {
            try {
                Log.d(TAG, "Trying Invidious: " + instance);
                String url = instance + "/api/v1/videos/" + videoId;

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "BoiWatch/2.0")
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful() || response.body() == null) continue;

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                // Get adaptive formats
                JSONArray adaptiveFormats = json.optJSONArray("adaptiveFormats");
                if (adaptiveFormats == null || adaptiveFormats.length() == 0) continue;

                String bestUrl = null;
                int bestBitrate = 0;

                for (int i = 0; i < adaptiveFormats.length(); i++) {
                    JSONObject fmt = adaptiveFormats.getJSONObject(i);
                    String type = fmt.optString("type", "");
                    if (!type.startsWith("audio/")) continue;

                    String streamUrl = fmt.optString("url", "");
                    int bitrate = fmt.optInt("bitrate", 0);

                    if (!streamUrl.isEmpty() && bitrate > bestBitrate) {
                        bestBitrate = bitrate;
                        bestUrl = streamUrl;
                    }
                }

                if (bestUrl != null) {
                    String description = json.optString("description", "");
                    Log.d(TAG, "Invidious: found audio stream");
                    return new String[]{bestUrl, description};
                }
            } catch (Exception e) {
                Log.w(TAG, "Invidious " + instance + " error: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * InnerTube player API — direct YouTube API (last resort).
     * Uses ANDROID client which sometimes returns direct URLs.
     */
    private String[] tryInnerTubePlayer(String videoId) {
        String[][] clients = {
                {"ANDROID", "19.09.37", "3",
                 "com.google.android.youtube/19.09.37 (Linux; U; Android 11) gzip",
                 "Android", "11", null, null, "30"},
                {"IOS", "19.09.3", "5",
                 "com.google.ios.youtube/19.09.3 (iPhone14,3; U; CPU iOS 15_6 like Mac OS X)",
                 null, "15.6", null, null, null},
        };

        for (String[] clientDef : clients) {
            try {
                Log.d(TAG, "Trying InnerTube client: " + clientDef[0]);

                JSONObject clientObj = new JSONObject();
                clientObj.put("clientName", clientDef[0]);
                clientObj.put("clientVersion", clientDef[1]);
                clientObj.put("gl", "US");
                clientObj.put("hl", "en");
                if (clientDef[4] != null) clientObj.put("osName", clientDef[4]);
                if (clientDef[5] != null) clientObj.put("osVersion", clientDef[5]);
                if (clientDef[8] != null) clientObj.put("androidSdkVersion", clientDef[8]);

                JSONObject context = new JSONObject();
                context.put("client", clientObj);

                JSONObject body = new JSONObject();
                body.put("videoId", videoId);
                body.put("context", context);
                body.put("contentCheckOk", true);
                body.put("racyCheckOk", true);

                Request request = new Request.Builder()
                        .url("https://www.youtube.com/youtubei/v1/player?key=" + YOUTUBE_API_KEY)
                        .post(RequestBody.create(body.toString(), JSON_TYPE))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("User-Agent", clientDef[3])
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful() || response.body() == null) continue;

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                JSONObject playability = json.optJSONObject("playabilityStatus");
                if (playability == null || !"OK".equals(playability.optString("status"))) {
                    Log.w(TAG, clientDef[0] + ": " + (playability != null ? playability.optString("reason", "not OK") : "no playability"));
                    continue;
                }

                // Find audio URL
                JSONObject streamingData = json.optJSONObject("streamingData");
                if (streamingData == null) continue;

                JSONArray formats = streamingData.optJSONArray("adaptiveFormats");
                if (formats == null) continue;

                String bestUrl = null;
                int bestBitrate = 0;

                for (int i = 0; i < formats.length(); i++) {
                    JSONObject fmt = formats.getJSONObject(i);
                    String mimeType = fmt.optString("mimeType", "");
                    if (!mimeType.startsWith("audio/")) continue;
                    if (!fmt.has("url")) continue; // Skip cipher-only

                    int bitrate = fmt.optInt("bitrate", 0);
                    if (bitrate > bestBitrate) {
                        bestBitrate = bitrate;
                        bestUrl = fmt.getString("url");
                    }
                }

                if (bestUrl != null) {
                    String description = "";
                    try {
                        description = json.getJSONObject("videoDetails").optString("shortDescription", "");
                    } catch (Exception ignored) {}
                    Log.d(TAG, clientDef[0] + ": found direct audio URL");
                    return new String[]{bestUrl, description};
                }
            } catch (Exception e) {
                Log.e(TAG, clientDef[0] + " error: " + e.getMessage());
            }
        }
        return null;
    }

    // ── Chapter extraction ─────────────────────────────────────

    public static List<AudiobookChapter> extractChapters(String description) {
        List<AudiobookChapter> chapters = new ArrayList<>();
        if (description == null || description.isEmpty()) return chapters;

        // Match "1:23:45 Title" or "1:23:45 - Title"
        Pattern pattern = Pattern.compile("(?m)^\\s*(\\d{1,2}:\\d{2}:\\d{2})\\s*[-–—]?\\s*(.+)$");
        Matcher matcher = pattern.matcher(description);
        while (matcher.find()) {
            String ts = matcher.group(1);
            String title = matcher.group(2);
            if (ts != null && title != null) {
                chapters.add(new AudiobookChapter(title.trim(), parseDuration(ts.trim())));
            }
        }

        // Fallback: match "12:34 Title"
        if (chapters.isEmpty()) {
            Pattern p2 = Pattern.compile("(?m)^\\s*(\\d{1,2}:\\d{2})\\s*[-–—]?\\s*(.+)$");
            Matcher m2 = p2.matcher(description);
            while (m2.find()) {
                String ts = m2.group(1);
                String title = m2.group(2);
                if (ts != null && title != null) {
                    chapters.add(new AudiobookChapter(title.trim(), parseDuration(ts.trim())));
                }
            }
        }
        return chapters;
    }

    // ── Helpers ────────────────────────────────────────────────

    private Audiobook parseVideoRenderer(JSONObject vr) {
        try {
            String videoId = vr.getString("videoId");

            String title = "";
            JSONObject titleObj = vr.optJSONObject("title");
            if (titleObj != null) {
                JSONArray runs = titleObj.optJSONArray("runs");
                if (runs != null && runs.length() > 0) {
                    title = runs.getJSONObject(0).optString("text", "");
                } else {
                    title = titleObj.optString("simpleText", "");
                }
            }

            String author = "";
            try {
                JSONObject ownerText = vr.optJSONObject("ownerText");
                if (ownerText == null) ownerText = vr.optJSONObject("longBylineText");
                if (ownerText != null) {
                    JSONArray runs = ownerText.optJSONArray("runs");
                    if (runs != null && runs.length() > 0) {
                        author = runs.getJSONObject(0).optString("text", "");
                    }
                }
            } catch (Exception ignored) {}

            String thumbnail = "";
            try {
                JSONArray thumbs = vr.getJSONObject("thumbnail").getJSONArray("thumbnails");
                thumbnail = thumbs.getJSONObject(thumbs.length() - 1).getString("url");
            } catch (Exception ignored) {}

            String duration = "";
            long durationSeconds = 0;
            try {
                JSONObject lt = vr.optJSONObject("lengthText");
                if (lt != null) {
                    duration = lt.optString("simpleText", "");
                    durationSeconds = parseDuration(duration);
                }
            } catch (Exception ignored) {}

            return new Audiobook(videoId, title, author, thumbnail, duration, durationSeconds);
        } catch (Exception e) {
            return null;
        }
    }

    private static long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) return 0;
        String[] parts = duration.split(":");
        try {
            if (parts.length == 3) {
                return Long.parseLong(parts[0]) * 3600 + Long.parseLong(parts[1]) * 60 + Long.parseLong(parts[2]);
            } else if (parts.length == 2) {
                return Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
            }
        } catch (NumberFormatException ignored) {}
        return 0;
    }

    private void postOnMain(Runnable action) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(action);
    }
}
