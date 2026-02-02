package com.example.newsapp.utils;

import com.example.newsapp.models.NewsArticle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsJsonParser {

    public static List<NewsArticle> parse(String jsonString) {
        List<NewsArticle> articles = new ArrayList<>();
        if (jsonString == null) return articles;

        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray articlesArray = root.optJSONArray("articles");

            if (articlesArray == null) return articles;

            for (int i = 0; i < articlesArray.length(); i++) {
                JSONObject obj = articlesArray.getJSONObject(i);

                // Extract fields safely
                String title = obj.optString("title");
                String author = obj.optString("author");
                String desc = obj.optString("description");
                String imgUrl = obj.optString("urlToImage");
                String date = obj.optString("publishedAt");

                JSONObject sourceObj = obj.optJSONObject("source");
                String sourceName = (sourceObj != null) ? sourceObj.optString("name") : "Unknown";

                articles.add(new NewsArticle(title, author, desc, imgUrl, date, sourceName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articles;
    }
}