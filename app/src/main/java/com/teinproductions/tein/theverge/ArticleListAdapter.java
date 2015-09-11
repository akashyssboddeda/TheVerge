package com.teinproductions.tein.theverge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {
    private static final int HERO_ITEM = 0;
    private static final int REVIEW_ITEM = 1;
    private static final int SEARCH_ITEM = 2;

    Context context;
    Elements data;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HERO_ITEM:
                return new HeroViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_hero), parent, false);
            case REVIEW_ITEM:
                return new ReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_review), parent, false);
            case SEARCH_ITEM:
                return new SearchViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_search), parent, false);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        Element element = data.get(position);
        if (element.classNames().contains("m-hero__slot") || element.classNames().contains("m-entry-slot")) {
            return HERO_ITEM;
        } else if (element.classNames().contains("m-reviews-index__node")) {
            return REVIEW_ITEM;
        } else if (element.classNames().contains("p-basic-article-list__node")) {
            return SEARCH_ITEM;
        } else {
            return super.getItemViewType(position);
        }
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        protected abstract void init(View itemView);

        public abstract void bind(Element element);
    }
}
