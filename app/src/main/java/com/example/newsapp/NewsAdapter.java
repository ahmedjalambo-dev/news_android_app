package com.example.newsapp; // CHANGE THIS TO YOUR PACKAGE NAME

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsArticle> articles;
    private OnItemClickListener listener;

    // Interface to handle clicks
    public interface OnItemClickListener {
        void onItemClick(NewsArticle article);
    }

    // Constructor
    public NewsAdapter(Context context, List<NewsArticle> articles, OnItemClickListener listener) {
        this.context = context;
        this.articles = articles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your updated item_news.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsArticle currentArticle = articles.get(position);

        // 1. Set Source Name
        // Ensure your NewsArticle class has the getSourceName() method we added previously
        holder.sourceTv.setText(currentArticle.getSourceName());

        // 2. Set Title
        holder.titleTv.setText(currentArticle.getTitle());

        // 3. Set Date
        // You can rely on the raw string, or format it if you prefer
        holder.dateTv.setText(currentArticle.getPublishedAt());

        // Takes only the first 10 characters (YYYY-MM-DD)
        String rawDate = currentArticle.getPublishedAt();
        if (rawDate.length() >= 10) {
            holder.dateTv.setText(rawDate.substring(0, 10));
        } else {
            holder.dateTv.setText(rawDate);
        }

        // 4. Set Author
        // Handle cases where author might be null or empty
        String author = currentArticle.getAuthor();
        if (author == null || author.equals("null") || author.isEmpty()) {
            holder.authorTv.setText("Unknown");
        } else {
            holder.authorTv.setText(author);
        }

        // 5. Load Image using Glide
        Glide.with(context)
                .load(currentArticle.getUrlToImage())
                .placeholder(android.R.drawable.ic_menu_gallery) // Default placeholder
                .error(android.R.drawable.ic_delete) // Error image
                .into(holder.thumbnailIv);

        // 6. Handle Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentArticle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    // Updated ViewHolder to match your new XML IDs
    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnailIv;
        TextView sourceTv;
        TextView titleTv;
        TextView dateTv;
        TextView authorTv;


        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);

            // Match these IDs exactly to your new item_news.xml
            thumbnailIv = itemView.findViewById(R.id.img_thumbnail);
            sourceTv = itemView.findViewById(R.id.tv_source_name);
            titleTv = itemView.findViewById(R.id.tv_title);
            dateTv = itemView.findViewById(R.id.tv_date);
            authorTv = itemView.findViewById(R.id.tv_author);
        }
    }
}