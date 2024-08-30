package com.myname.myapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Post {
    private String userName;
    private String content;
    private String urlImage;
    private String postId;
    private String imageUrl;
    private String userId;
    private long timestamp;
    private int likeCount;
    private HashMap<String, Boolean> likes;
    private List<Comment> comments;

    // Constructor rỗng để Firebase sử dụng
    public Post() {
        this.likes = new HashMap<>();
        this.comments = new ArrayList<>();
    }


    // Constructor cho khi khởi tạo mới
    public Post(String postId, String content, String imageUrl, String userId, long timestamp, int likeCount, List<Comment> comments) {
        this.postId = postId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.timestamp = timestamp;
        this.likeCount = likeCount;
        this.likes = new HashMap<>();
        this.comments = comments != null ? comments : new ArrayList<>();

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
    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public HashMap<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(HashMap<String, Boolean> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
