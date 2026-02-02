package com.example.newsapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsapp.R;
import com.example.newsapp.adapters.NewsAdapter;
import com.example.newsapp.models.NewsArticle;

import java.util.ArrayList;
import java.util.List;

public class NewsListFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter adapter;
    private final List<NewsArticle> articles = new ArrayList<>();
    private OnArticleSelectedListener listener;

    // Holds data that arrives before the view is ready
    private List<NewsArticle> pendingArticles = null;

    // Interface for Main Activity to listen to clicks
    public interface OnArticleSelectedListener {
        void onArticleSelected(NewsArticle article);

        void onRefreshRequested();
    }

    // Inside NewsListFragment.java

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnArticleSelectedListener) {
            listener = (OnArticleSelectedListener) context;
        }
        if (context instanceof MainActivity) {
            ((MainActivity) context).setListFragment(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NewsAdapter(getContext(), articles, article -> {
            // Pass click event to MainActivity
            if (listener != null) listener.onArticleSelected(article);
        });
        recyclerView.setAdapter(adapter);

        // Swipe to Refresh requirement
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (listener != null) listener.onRefreshRequested();
            swipeRefreshLayout.setRefreshing(false); // Stop spinner immediately, let AsyncTask handle loading UI
        });

        // CHECKPOINT: If data came in before the view was ready, load it now!
        if (pendingArticles != null) {
            adapter.updateData(pendingArticles);
            pendingArticles = null;
        }

        return view;
    }

    /**
     * FIX: Use the adapter's updateData method which properly updates the adapter's internal list.
     * The previous implementation updated the fragment's list but the adapter had its own copy,
     * so the RecyclerView never showed any items.
     */
    public void updateData(List<NewsArticle> newArticles) {
        // Save data in case the view isn't ready yet
        this.pendingArticles = newArticles;

        // If the adapter is ready, update it directly
        if (adapter != null) {
            adapter.updateData(newArticles);
        }
    }
}