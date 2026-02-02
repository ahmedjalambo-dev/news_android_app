package com.example.newsapp.models; // CHANGE TO YOUR PACKAGE NAME

public class NewsArticle {
    private final String title;
    private final String author;
    private final String description;
    private final String urlToImage;
    private final String publishedAt;
    private final String sourceName;

    public NewsArticle(String title, String author, String description, String urlToImage, String publishedAt, String sourceName) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
        this.sourceName = sourceName;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    // 3. Add Getter for Source
    public String getSourceName() {
        return sourceName;
    }
}