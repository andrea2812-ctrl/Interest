package com.example.interest;

public class Post {
    private String postId;
    private String userId;
    private String userEmail;
    private String text;
    private long timestamp;

    // Costruttore vuoto (obbligatorio per Firebase)
    public Post() {}

    // Costruttore completo
    public Post(String postId, String userId, String userEmail, String text, long timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getter e Setter (aggiunti per flessibilità)
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}