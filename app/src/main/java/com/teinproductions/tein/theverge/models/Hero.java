package com.teinproductions.tein.theverge.models;

public class Hero {
    private String title;
    private String url;
    private String imgUrl;
    private String author;
    private int comments;

    public Hero() {
    }

    public Hero(String title, String url, String imgUrl, String author, int comments) {
        this.title = title;
        this.url = url;
        this.imgUrl = imgUrl;
        this.author = author;
        this.comments = comments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }
}
