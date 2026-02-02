package com.example.newsapp.networking;

import android.os.AsyncTask;
import com.example.newsapp.Config;
import com.example.newsapp.models.NewsArticle;
import com.example.newsapp.utils.NewsJsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FetchNewsTask extends AsyncTask<String, Void, String> {

    // 1. Define the Interface for communication
    public interface Callback {
        void onNewsFetched(List<NewsArticle> articles);
        void onError(String message);
    }

    private final Callback callback;

    // 2. Constructor takes the Listener (Callback)
    public FetchNewsTask(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        String category = params[0];
        // Uses Config from Step 2. If you skipped Step 2, hardcode the URL here.
        String urlString = Config.BASE_URL + "?country=us&category=" + category + "&apiKey=" + Config.API_KEY;
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (s == null) {
            if (callback != null) callback.onError("Failed to fetch news (Network Error)");
            return;
        }

        // 3. Use the Parser (from Step 1)
        List<NewsArticle> parsedArticles = NewsJsonParser.parse(s);

        // 4. Send results back to MainActivity
        if (callback != null) {
            if (parsedArticles.isEmpty()) {
                callback.onError("No articles found.");
            } else {
                callback.onNewsFetched(parsedArticles);
            }
        }
    }
}