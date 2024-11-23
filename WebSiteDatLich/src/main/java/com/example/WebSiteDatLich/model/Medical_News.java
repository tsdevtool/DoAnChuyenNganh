package com.example.WebSiteDatLich.model;

public class Medical_News {
    public Integer getMedical_news_id() {
        return medical_news_id;
    }

    public void setMedical_news_id(Integer medical_news_id) {
        this.medical_news_id = medical_news_id;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    private Integer medical_news_id;
    private String date_time;
    private String image;  // URL of the image
    private String title;
    private String content;
    private Integer user_id;
}
