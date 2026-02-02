package com.example.newsapp.ui;

import android.Manifest;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.newsapp.R;
import com.example.newsapp.models.NewsArticle;
import com.example.newsapp.networking.FetchNewsTask;
import com.example.newsapp.utils.NotificationHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NewsListFragment.OnArticleSelectedListener, FetchNewsTask.Callback {

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
        fetchNews();
    }

    private void setupViewPager() {
        listFragment = new NewsListFragment();
        detailFragment = new NewsDetailFragment();

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, listFragment, detailFragment);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                // Dynamic title based on default category
                String formatted = currentCategory.substring(0, 1).toUpperCase() + currentCategory.substring(1);
                tab.setText(formatted + " Headlines");
            } else {
                tab.setText("Details");
            }
        }).attach();
    }

    // Interface Implementation
    @Override
    public void onArticleSelected(NewsArticle article) {
        // Pass data to Tab 2
        detailFragment.displayArticle(article);
        // Switch to Tab 2
        viewPager.setCurrentItem(1, true);
    }

    @Override
    public void onRefreshRequested() {
        fetchNews();
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
                break;
            case 5:
                currentCategory = "entertainment";
                break;
            case 6:
                currentCategory = "science";
                break;
            case 7:
                currentCategory = "health";
                break;
        }

        // Fetch the data (removed duplicate call that was here before)
        fetchNews();

        // Update the Tab Text
        updateTabTitle(currentCategory);
        return super.onOptionsItemSelected(item);
    }

    /**
     * FIX: Helper method that shows the progress bar before fetching news.
     * This ensures the loading indicator is visible during network requests.
     */
    private void fetchNews() {
        // Show progress bar BEFORE starting the async task
        progressBar.setVisibility(View.VISIBLE);
        new FetchNewsTask(this).execute(currentCategory);
    }

    @Override
    public void onNewsFetched(List<NewsArticle> articles) {
        // Hide progress bar after fetch completes
        progressBar.setVisibility(View.GONE);

        // Update the list
        listFragment.updateData(articles);

        // Update the detail view if needed
        if (!articles.isEmpty()) {
            detailFragment.displayArticle(articles.get(0));
        }

        // Use the Helper from the previous step
        NotificationHelper.show(this, "News Updated", "Loaded " + articles.size() + " articles.");
    }

    @Override
    public void onError(String message) {
        // Hide progress bar on error too
        progressBar.setVisibility(View.GONE);
        NotificationHelper.show(this, "Error", message);
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

    private void updateTabTitle(String category) {
        // 1. Capitalize the first letter (e.g., "sports" -> "Sports")
        String formattedCategory = category.substring(0, 1).toUpperCase() + category.substring(1);

        // 2. Get the first tab (Index 0)
        TabLayout.Tab tab = tabLayout.getTabAt(0);

        // 3. Set the new text
        if (tab != null) {
            tab.setText(formattedCategory + " Headlines");
        }
    }
}