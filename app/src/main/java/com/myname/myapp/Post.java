package com.myname.myapp;

public class Post {
    private String userName;
    private String content;
    private String urlImage;

    public Post(String userName, String content, String urlImage) {
        this.userName = userName;
        this.content = content;
        this.urlImage = urlImage;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }
    public String getUrlImage() {
        return urlImage;
    }
}
