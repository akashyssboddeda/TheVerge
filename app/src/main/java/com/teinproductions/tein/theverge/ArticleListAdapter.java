package com.teinproductions.tein.theverge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.teinproductions.tein.theverge.viewholders.ArticleItemViewHolder;
import com.teinproductions.tein.theverge.viewholders.HeroViewHolder;
import com.teinproductions.tein.theverge.viewholders.ReviewViewHolder;
import com.teinproductions.tein.theverge.viewholders.SearchViewHolder;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleItemViewHolder> {
    private static final int HERO_ITEM = 0;
    private static final int REVIEW_ITEM = 1;
    private static final int SEARCH_ITEM = 2;

    Context context;
    Elements data;

    public ArticleListAdapter(Context context, Elements data) {
        this.context = context;
        this.data = data;
    }

    public void setData(Elements data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public Elements getData() {
        return data;
    }

    @Override
    public ArticleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HERO_ITEM:
                return new HeroViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_hero, parent, false));
            case REVIEW_ITEM:
                return new ReviewViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_review, parent, false));
            case SEARCH_ITEM:
                return new SearchViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_search, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ArticleItemViewHolder holder, int position) {
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

}
