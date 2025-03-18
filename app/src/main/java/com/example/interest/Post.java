package com.example.interest;

public class Post {
    private String postId;
    private String userId;
    private String userEmail;
    private String text;
    private long timestamp;

    public Post() {
        // Costruttore vuoto richiesto da Firebase
    }

    public Post(String postId, String userId, String userEmail, String text, long timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
