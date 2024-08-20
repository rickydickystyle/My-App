package com.myname.myapp;

public class Post {
    private String userName;
    private String content;
    private String urlImage;
    private String postId;
    private String imageUrl;
    private String userId;
    private long timestamp;

    // Constructor rỗng để Firebase sử dụng
    public Post() {
    }

    // Constructor cho khi khởi tạo mới
    public Post(String postId, String content, String imageUrl, String userId, long timestamp) {
        this.postId = postId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getter và Setter
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
