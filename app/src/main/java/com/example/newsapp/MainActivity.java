package com.example.newsapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NewsListFragment.OnArticleSelectedListener {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private NewsListFragment listFragment;
    private NewsDetailFragment detailFragment;

    private String currentCategory = "general";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.progressBar);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        setupViewPager();
        // If Android version is 13 (API 33) or higher, ask for permission
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        // Initial Load
        new FetchNewsTask().execute(currentCategory);
    }

    private void setupViewPager() {
        listFragment = new NewsListFragment();
        detailFragment = new NewsDetailFragment();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, listFragment, detailFragment);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Headlines" : "Details");
        }).attach();
    }

    // --- Interface Implementation ---
    @Override
    public void onArticleSelected(NewsArticle article) {
        // 1. Pass data to Tab 2
        detailFragment.displayArticle(article);
        // 2. Switch to Tab 2
        viewPager.setCurrentItem(1, true);
    }

    @Override
    public void onRefreshRequested() {
        new FetchNewsTask().execute(currentCategory);
    }

    // --- Menu Handling ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "General");
        menu.add(0, 2, 0, "Technology");
        menu.add(0, 3, 0, "Sports");
        menu.add(0, 4, 0, "Business");
        menu.add(0, 5, 0, "Entertainment");
        menu.add(0, 6, 0, "Science");
        menu.add(0, 7, 0, "Health");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                currentCategory = "general";
                break;
            case 2:
                currentCategory = "technology";
                break;
            case 3:
                currentCategory = "sports";
                break;
            case 4:
                currentCategory = "business";
            case 5:
                currentCategory = "entertainment";
            case 6:
                currentCategory = "science";
            case 7:
                currentCategory = "health";
                break;
        }
        new FetchNewsTask().execute(currentCategory);
        return super.onOptionsItemSelected(item);
    }

    // --- Notifications ---
    private void sendNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "news_channel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "News Updates", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(message).setAutoCancel(true);

        manager.notify(1, builder.build());
    }

    // --- AsyncTask & Networking (The Strict Requirement) ---
    @SuppressWarnings("deprecation") // Suppress warning for academic requirement
    private class FetchNewsTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String category = params[0];
            // REPLACE THIS
            String API_KEY = "1d0edd6ab88f49ea97abb01440082f90";
            String urlString = "https://newsapi.org/v2/top-headlines?country=us&category=" + category + "&apiKey=" + API_KEY;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(5000);

                // Read response
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
            progressBar.setVisibility(View.GONE);

            if (s == null) {
                sendNotification("Error", "Failed to fetch news.");
                return;
            }

            List<NewsArticle> parsedArticles = new ArrayList<>();
            try {
                // Manual JSON Parsing
                JSONObject root = new JSONObject(s);
                JSONArray articlesArray = root.getJSONArray("articles");

                for (int i = 0; i < articlesArray.length(); i++) {
                    JSONObject obj = articlesArray.getJSONObject(i);
                    String title = obj.optString("title");
                    String author = obj.optString("author");
                    String desc = obj.optString("description");
                    String imgUrl = obj.optString("urlToImage");
                    String date = obj.optString("publishedAt");

                    JSONObject sourceObj = obj.getJSONObject("source");
                    String sourceName = sourceObj.optString("name");

                    parsedArticles.add(new NewsArticle(title, author, desc, imgUrl, date, sourceName));
                }

                // Update List Fragment
                listFragment.updateData(parsedArticles);

                // Requirement: Default Tab 2 to first article
                if (!parsedArticles.isEmpty()) {
                    detailFragment.displayArticle(parsedArticles.get(0));
                }

                sendNotification("News Updated", "Loaded " + parsedArticles.size() + " articles.");

            } catch (JSONException e) {
                e.printStackTrace();
                sendNotification("Error", "Data parsing failed.");
            }
        }
    }

    // Simple ViewPager Adapter
    static class ViewPagerAdapter extends FragmentStateAdapter {
        private final Fragment listFrag;
        private final Fragment detailFrag;

        public ViewPagerAdapter(FragmentActivity fa, Fragment l, Fragment d) {
            super(fa);
            this.listFrag = l;
            this.detailFrag = d;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? listFrag : detailFrag;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    public void setListFragment(NewsListFragment fragment) {
        this.listFragment = fragment;
    }

    public void setDetailFragment(NewsDetailFragment fragment) {
        this.detailFragment = fragment;
    }
}