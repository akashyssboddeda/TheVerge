package com.teinproductions.tein.theverge;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.teinproductions.tein.theverge.viewholders.ArticleItemViewHolder;
import com.teinproductions.tein.theverge.viewholders.HeroViewHolder;
import com.teinproductions.tein.theverge.viewholders.ProductViewHolder;
import com.teinproductions.tein.theverge.viewholders.ReviewViewHolder;
import com.teinproductions.tein.theverge.viewholders.SearchViewHolder;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleItemViewHolder> {
    private static final int HERO_ITEM = 0;
    private static final int REVIEW_ITEM = 1;
    private static final int SEARCH_ITEM = 2;
    private static final int PRODUCT_ITEM = 3;

    CTActivity activity;
    Elements data;

    public ArticleListAdapter(CTActivity activity, Elements data) {
        this.activity = activity;
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
                return new HeroViewHolder(LayoutInflater.from(activity).inflate(R.layout.list_item_hero, parent, false));
            case REVIEW_ITEM:
                return new ReviewViewHolder(LayoutInflater.from(activity).inflate(R.layout.list_item_review, parent, false));
            case SEARCH_ITEM:
                return new SearchViewHolder(LayoutInflater.from(activity).inflate(R.layout.list_item_search, parent, false));
            case PRODUCT_ITEM:
                return new ProductViewHolder(LayoutInflater.from(activity).inflate(R.layout.list_item_product, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ArticleItemViewHolder holder, int position) {
        holder.bind(activity, data.get(position));
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
        } else if (element.classNames().contains("m-products-index__grid-item")) {
            return PRODUCT_ITEM;
        } else {
            return super.getItemViewType(position);
        }
    }

}
