package com.example.interest;

public class Post {
    private String postId;
    private String userId;
    private String userEmail;
    private String text;
    private long timestamp;
    private int likeCount;

    // Costruttore vuoto richiesto da Firebase
    public Post() {}

    /**
     * Costruttore completo per la creazione di un post.
     * @param postId ID univoco del post.
     * @param userId ID dell'utente che ha creato il post.
     * @param userEmail Email dell'utente autore del post.
     * @param text Contenuto del post.
     * @param timestamp Momento della creazione del post (in millisecondi).
     * @param likeCount Numero di like iniziale del post.
     */
    public Post(String postId, String userId, String userEmail, String text, long timestamp, int likeCount) {
        this.postId = postId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.text = text;
        this.timestamp = timestamp;
        this.likeCount = likeCount;
    }

    // Getter e Setter
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

    /**
     * Metodo per incrementare il numero di like.
     */
    public void incrementLike() {
        this.likeCount++;
    }

    /**
     * Metodo per decrementare il numero di like.
     * (Evita che scenda sotto 0)
     */
    public void decrementLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
