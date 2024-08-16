package com.myname.myapp;

public class Post {
    private String userName;
    private String content;
    private String urlImage;
//    private User author;

    public Post(String userName, String content, String urlImage /*User author*/) {
        this.userName = userName;
        this.content = content;
        this.urlImage = urlImage;
//        this.author = author;
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
//    public User getAuthor() {
//        return author;
//    }
//    public void setAuthor(User author) {
//        this.author = author;
//    }
}
