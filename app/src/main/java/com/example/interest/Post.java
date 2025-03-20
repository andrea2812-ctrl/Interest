package com.example.interest;

public class Post {
    private String postId;
    private String userId;
    private String userEmail;
    private String text;
    private long timestamp;
    private int likeCount; // Nuovo campo

    public Post() {}

    public Post(String postId, String userId, String userEmail, String text, long timestamp, int likeCount) {
        this.postId = postId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.text = text;
        this.timestamp = timestamp;
        this.likeCount = likeCount;
    }

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

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}
