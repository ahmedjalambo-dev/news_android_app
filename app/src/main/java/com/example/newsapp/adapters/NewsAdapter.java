package com.example.newsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsapp.models.NewsArticle;
import com.example.newsapp.R;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final Context context;
    private final List<NewsArticle> articles;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NewsArticle article);
    }

    public NewsAdapter(Context context, List<NewsArticle> articles, OnItemClickListener listener) {
        this.context = context;
        // Create a copy of the list to ensure data safety
        this.articles = new ArrayList<>(articles);
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsArticle currentArticle = articles.get(position);
        holder.bind(currentArticle, context, listener);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    /**
     * Clean Code Update: Uses DiffUtil to calculate changes efficiently
     * instead of redrawing the entire list with notifyDataSetChanged().
     */
    public void updateData(List<NewsArticle> newArticles) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new NewsDiffCallback(this.articles, newArticles));

        this.articles.clear();
        this.articles.addAll(newArticles);

        diffResult.dispatchUpdatesTo(this);
    }

    // --- ViewHolder Class ---
    static class NewsViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnailIv;
        final TextView sourceTv;
        final TextView titleTv;
        final TextView dateTv;
        final TextView authorTv;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailIv = itemView.findViewById(R.id.img_thumbnail);
            sourceTv = itemView.findViewById(R.id.tv_source_name);
            titleTv = itemView.findViewById(R.id.tv_title);
            dateTv = itemView.findViewById(R.id.tv_date);
            authorTv = itemView.findViewById(R.id.tv_author);
        }

        // Helper method to move binding logic out of the adapter's onBindViewHolder
        void bind(NewsArticle article, Context context, OnItemClickListener listener) {
            sourceTv.setText(article.getSourceName());
            titleTv.setText(article.getTitle());

            // Format Date safely
            String rawDate = article.getPublishedAt();
            if (rawDate != null && rawDate.length() >= 10) {
                dateTv.setText(rawDate.substring(0, 10));
            } else {
                dateTv.setText(rawDate);
            }

            // Handle Author safely
            String author = article.getAuthor();
            if (author == null || author.equals("null") || author.isEmpty()) {
                authorTv.setText("Unknown");
            } else {
                authorTv.setText(author);
            }

            // Load Image
            Glide.with(context).load(article.getUrlToImage()).placeholder(R.drawable.ic_image_placeholder).error(R.drawable.ic_broken_image).into(thumbnailIv);

            // Click Listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(article);
                }
            });
        }
    }

    // --- DiffUtil Callback for Performance ---
    static class NewsDiffCallback extends DiffUtil.Callback {
        private final List<NewsArticle> oldList;
        private final List<NewsArticle> newList;

        public NewsDiffCallback(List<NewsArticle> oldList, List<NewsArticle> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // Compare unique IDs (or URL/Title if no ID exists)
            // Assuming URL is unique for a news article
            String oldUrl = oldList.get(oldItemPosition).getUrlToImage(); // Ideally use a unique Article URL or ID
            String newUrl = newList.get(newItemPosition).getUrlToImage();

            // Handle potential nulls safely
            if (oldUrl == null && newUrl == null) return true;
            if (oldUrl == null || newUrl == null) return false;

            return oldUrl.equals(newUrl);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            NewsArticle oldItem = oldList.get(oldItemPosition);
            NewsArticle newItem = newList.get(newItemPosition);

            // Check if visual content has changed
            return oldItem.getTitle().equals(newItem.getTitle()) && oldItem.getPublishedAt().equals(newItem.getPublishedAt());
        }
    }
}