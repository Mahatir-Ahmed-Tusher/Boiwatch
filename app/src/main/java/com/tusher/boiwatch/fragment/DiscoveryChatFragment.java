package com.tusher.boiwatch.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tusher.boiwatch.Constants;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.adapter.ChatAdapter;
import com.tusher.boiwatch.api.RetrofitClient;
import com.tusher.boiwatch.models.ChatRequest;
import com.tusher.boiwatch.models.ChatMessage;
import com.tusher.boiwatch.models.Movie;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscoveryChatFragment extends BottomSheetDialogFragment {

    private static final String TAG = "DiscoveryChatFragment";
    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private List<ChatRequest.Message> chatHistory = new ArrayList<>();
    private EditText etMessage;
    private ImageButton btnSend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discovery_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChat = view.findViewById(R.id.rv_chat);
        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send);

        adapter = new ChatAdapter(getContext(), messages);
        rvChat.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
        
        if (messages.isEmpty()) {
            String welcome = "Hey... ki obostha? I'm Cinemawala, your personal movie guru. 🎬\n\nI can suggest movies based on your mood, write reviews, share mind-blowing trivia, or just chat about cinema! What's on your mind today?";
            messages.add(new ChatMessage(welcome, ChatMessage.TYPE_AI));
            adapter.notifyDataSetChanged();
            chatHistory.add(new ChatRequest.Message("assistant", welcome));
        }
    }

    private void sendMessage() {
        String userText = etMessage.getText().toString().trim();
        if (userText.isEmpty()) return;

        messages.add(new ChatMessage(userText, ChatMessage.TYPE_USER));
        chatHistory.add(new ChatRequest.Message("user", userText));
        
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
        etMessage.setText("");

        fetchAIResponse();
    }

    private void fetchAIResponse() {
        List<ChatRequest.Message> fullMessages = new ArrayList<>();
        
        String systemPrompt = "You are 'Cinemawala', an expert movie guide for the BoiWatch app. " +
                "You are talkative, responsive, curious, and sincere. You have a friendly, local vibe. " +
                "Welcome message MUST start with 'Hey... ki obostha?'. " +
                "Language Policy: Always respond in English unless the user speaks in Bengali. " +
                "Recommendation Content: Prioritize recommending English (mostly), Hindi, Korean (K-Dramas), and Japanese Anime. " +
                "Recommendation Rule: When suggesting titles, DO NOT write the movie or show names in your text. " +
                "Simply explain why you chose them and provide their valid TMDB IDs in brackets like [ID1, ID2]. " +
                "Example: 'I think you will love these because of their plot twists: [27205, 299536]'. " +
                "Always suggest real titles. Stay in character.";
        
        fullMessages.add(new ChatRequest.Message("system", systemPrompt));
        fullMessages.addAll(chatHistory);

        ChatRequest request = new ChatRequest("qwen/qwen3-32b", fullMessages);
        
        ChatMessage aiMessage = new ChatMessage("...", ChatMessage.TYPE_AI);
        messages.add(aiMessage);
        int position = messages.size() - 1;
        adapter.notifyItemInserted(position);
        rvChat.scrollToPosition(position);

        RetrofitClient.getGroqApi().streamChatCompletion("Bearer " + Constants.GROQ_API_KEY, request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            handleStreamError(response, position);
                            return;
                        }

                        new Thread(() -> {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                                String line;
                                StringBuilder fullContent = new StringBuilder();
                                boolean isFirstToken = true;
                                boolean isThinking = false;

                                while ((line = reader.readLine()) != null) {
                                    if (!line.startsWith("data: ")) continue;

                                    String json = line.substring(6).trim();
                                    if (json.equals("[DONE]")) break;

                                    JSONObject chunk = new JSONObject(json);
                                    JSONArray choices = chunk.getJSONArray("choices");
                                    JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");

                                    if (delta.has("content")) {
                                        String token = delta.getString("content");
                                        
                                        if (token.contains("<thought>") || token.contains("<think>")) {
                                            isThinking = true;
                                            continue;
                                        }
                                        if (token.contains("</thought>") || token.contains("</think>")) {
                                            isThinking = false;
                                            continue;
                                        }
                                        
                                        if (isThinking) continue;

                                        if (isFirstToken && !token.trim().isEmpty()) {
                                            clearUI(position);
                                            isFirstToken = false;
                                        }
                                        
                                        if (!isFirstToken) {
                                            fullContent.append(token);
                                            updateUI(position, token);
                                        }
                                    }
                                }
                                String finalResponse = fullContent.toString();
                                chatHistory.add(new ChatRequest.Message("assistant", finalResponse));
                                processFinalContent(position, finalResponse);
                            } catch (Exception e) {
                                Log.e(TAG, "Stream processing error", e);
                            }
                        }).start();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        clearUI(position);
                        updateUI(position, "Connection failed: " + t.getMessage());
                    }
                });
    }

    private void clearUI(int position) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            messages.set(position, new ChatMessage("", ChatMessage.TYPE_AI));
            adapter.notifyItemChanged(position);
        });
    }

    private void updateUI(int position, String token) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            messages.get(position).appendText(token);
            adapter.notifyItemChanged(position);
            rvChat.scrollToPosition(messages.size() - 1);
        });
    }

    private void handleStreamError(Response<ResponseBody> response, int position) {
        clearUI(position);
        String errorMsg = "Error: " + response.code();
        try {
            if (response.errorBody() != null) {
                errorMsg += " - " + response.errorBody().string();
            }
        } catch (Exception ignored) {}
        final String finalError = errorMsg;
        getActivity().runOnUiThread(() -> {
            messages.get(position).appendText("Oops! My film reel got stuck. Let's try again? (" + finalError + ")");
            adapter.notifyItemChanged(position);
        });
    }

    private void processFinalContent(int position, String content) {
        List<String> ids = new ArrayList<>();
        // Better extraction: find all digit sequences inside any brackets
        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String match = matcher.group(1);
            Pattern idPattern = Pattern.compile("\\d+");
            Matcher idMatcher = idPattern.matcher(match);
            while (idMatcher.find()) {
                String id = idMatcher.group();
                if (!ids.contains(id)) {
                    ids.add(id);
                }
            }
        }

        if (!ids.isEmpty()) {
            fetchMediaDetails(position, ids);
        }
    }

    private void fetchMediaDetails(int position, List<String> ids) {
        List<Movie> suggestions = new ArrayList<>();
        final int[] count = {0};
        final int total = ids.size();
        
        for (String idStr : ids) {
            int id = Integer.parseInt(idStr);
            // Step 1: Try as Movie
            RetrofitClient.getApi().getMovieDetails("Bearer " + Constants.TMDB_ACCESS_TOKEN, id)
                    .enqueue(new Callback<Movie>() {
                        @Override
                        public void onResponse(Call<Movie> call, Response<Movie> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Movie m = response.body();
                                m.setMediaType("movie");
                                synchronized (suggestions) { suggestions.add(m); }
                                checkCompletion();
                            } else {
                                // Step 2: Fallback to TV if not a movie
                                fetchTVAsFallback(id, suggestions, () -> checkCompletion());
                            }
                        }

                        @Override
                        public void onFailure(Call<Movie> call, Throwable t) {
                            fetchTVAsFallback(id, suggestions, () -> checkCompletion());
                        }

                        private void checkCompletion() {
                            count[0]++;
                            if (count[0] == total && isAdded()) {
                                getActivity().runOnUiThread(() -> {
                                    messages.get(position).setSuggestions(new ArrayList<>(suggestions));
                                    adapter.notifyItemChanged(position);
                                });
                            }
                        }
                    });
        }
    }

    private void fetchTVAsFallback(int id, List<Movie> suggestions, Runnable onDone) {
        RetrofitClient.getApi().getTVDetails("Bearer " + Constants.TMDB_ACCESS_TOKEN, id)
                .enqueue(new Callback<com.tusher.boiwatch.models.TVDetailResponse>() {
                    @Override
                    public void onResponse(Call<com.tusher.boiwatch.models.TVDetailResponse> call, Response<com.tusher.boiwatch.models.TVDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.tusher.boiwatch.models.TVDetailResponse tv = response.body();
                            Movie m = new Movie(); 
                            m.setId(String.valueOf(tv.getId()));
                            m.setTitle(tv.getName());
                            m.setPosterPath(tv.getPosterPath());
                            m.setVoteAverage(tv.getVoteAverage());
                            m.setReleaseDate(tv.getFirstAirDate());
                            m.setMediaType("tv");
                            synchronized (suggestions) { suggestions.add(m); }
                        }
                        onDone.run();
                    }
                    @Override
                    public void onFailure(Call<com.tusher.boiwatch.models.TVDetailResponse> call, Throwable t) {
                        onDone.run();
                    }
                });
    }
}
