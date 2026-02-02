package com.tusher.boiwatch.models;

import java.util.List;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    private StringBuilder text;
    private int type;
    private List<Movie> suggestions;

    public ChatMessage(String text, int type) {
        this.text = new StringBuilder(text);
        this.type = type;
    }

    public ChatMessage(String text, int type, List<Movie> suggestions) {
        this.text = new StringBuilder(text);
        this.type = type;
        this.suggestions = suggestions;
    }

    public String getText() {
        return text.toString();
    }

    public void appendText(String newText) {
        this.text.append(newText);
    }

    public int getType() {
        return type;
    }

    public List<Movie> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Movie> suggestions) {
        this.suggestions = suggestions;
    }
}
