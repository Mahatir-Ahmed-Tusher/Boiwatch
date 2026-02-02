package com.tusher.boiwatch.models;

import java.util.List;

public class ChatRequest {
    public String model;
    public List<Message> messages;
    public double temperature;
    public int max_completion_tokens;
    public double top_p;
    public boolean stream;
    public String reasoning_effort;
    public Object stop;

    public ChatRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
        this.temperature = 0.6;
        this.top_p = 0.95;
        this.max_completion_tokens = 4096;
        this.stream = true;
        this.reasoning_effort = "default";
        this.stop = null;
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
