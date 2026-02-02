package com.example.newsapp; // CHANGE THIS TO YOUR PACKAGE NAME

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class NewsDetailFragment extends Fragment {

    // UI Components
    private ImageView imageView;
    private TextView sourceTv; // NEW: Source Name
    private TextView titleTv, authorTv, descTv, dateTv;
    private Button readMoreBtn;

    // Holding variable for the data
    private NewsArticle pendingArticle;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            ((MainActivity) context).setDetailFragment(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_detail, container, false);

        // Bind Views
        imageView = view.findViewById(R.id.detail_image);
        sourceTv = view.findViewById(R.id.detail_source_name); // 1. Bind new Source TextView
        titleTv = view.findViewById(R.id.detail_title);
        authorTv = view.findViewById(R.id.detail_author);
        descTv = view.findViewById(R.id.detail_desc);
        dateTv = view.findViewById(R.id.detail_date);
        readMoreBtn = view.findViewById(R.id.btn_read_more);

        // Setup Read More Button (Optional: Open URL in Browser)
        readMoreBtn.setOnClickListener(v -> {
            // Note: Since you haven't added the URL to your NewsArticle class yet,
            // this button currently does nothing.
            // If you added 'url' to NewsArticle, you would launch the Intent here.
        });

        // CHECKPOINT: If data came in before the view was ready, load it now!
        if (pendingArticle != null) {
            updateUI(pendingArticle);
        }

        return view;
    }

    // Public method called by MainActivity
    public void displayArticle(NewsArticle article) {
        this.pendingArticle = article; // Save the data

        // If the view is already ready (screen is visible), update immediately
        if (titleTv != null) {
            updateUI(article);
        }
    }

    // Helper method to actually set the text
    private void updateUI(NewsArticle article) {
        // 2. Set the Source Name text
        sourceTv.setText(article.getSourceName());

        titleTv.setText(article.getTitle());

        // Handle potential null author
        String authorText = article.getAuthor();
        if (authorText == null || authorText.equals("null") || authorText.isEmpty()) {
            authorTv.setText("Author: Unknown");
        } else {
            authorTv.setText("Author: " + authorText);
        }

        descTv.setText(article.getDescription());
        dateTv.setText(article.getPublishedAt());

        if (getContext() != null) {
            Glide.with(this)
                    .load(article.getUrlToImage())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imageView);
        }

        String rawDate = article.getPublishedAt();
        if (rawDate != null && rawDate.length() >= 10) {
            dateTv.setText(rawDate.substring(0, 10)); // Shows "2026-02-01"
        } else {
            dateTv.setText(rawDate); // Fallback if data is weird
        }
    }
}