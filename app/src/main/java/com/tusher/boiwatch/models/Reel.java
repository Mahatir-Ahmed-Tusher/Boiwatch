package com.tusher.boiwatch.models;

import java.util.UUID;

public class Reel {
    private String reelUrl;
    private boolean isFollowed;
    private ReelInfo reelInfo;

    public Reel(String reelUrl, boolean isFollowed, ReelInfo reelInfo) {
        this.reelUrl = reelUrl;
        this.isFollowed = isFollowed;
        this.reelInfo = reelInfo;
    }

    public String getReelUrl() { return reelUrl; }
    public boolean isFollowed() { return isFollowed; }
    public ReelInfo getReelInfo() { return reelInfo; }

    public static class ReelInfo {
        private String username;
        private String profilePicUrl;
        private String description;
        private boolean isLiked;
        private int likes;
        private int comments;
        private String audio;
        private String audioPicUrl;
        private String id;

        public ReelInfo(String username, String profilePicUrl, String description, boolean isLiked, int likes, int comments) {
            this.username = username;
            this.profilePicUrl = profilePicUrl;
            this.description = description;
            this.isLiked = isLiked;
            this.likes = likes;
            this.comments = comments;
            this.audio = username + " • Original Audio";
            this.audioPicUrl = profilePicUrl;
            this.id = UUID.randomUUID().toString();
        }

        public String getUsername() { return username; }
        public String getProfilePicUrl() { return profilePicUrl; }
        public String getDescription() { return description; }
        public boolean isLiked() { return isLiked; }
        public int getLikes() { return likes; }
        public int getComments() { return comments; }
        public String getAudio() { return audio; }
        public String getAudioPicUrl() { return audioPicUrl; }
        public String getId() { return id; }

        public void setLiked(boolean liked) { isLiked = liked; }
    }
}
