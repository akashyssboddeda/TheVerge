package com.teinproductions.tein.theverge;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.teinproductions.tein.theverge.models.Hero;
import com.teinproductions.tein.theverge.viewholders.ArticleItemViewHolder;
import com.teinproductions.tein.theverge.viewholders.HeroViewHolder;
import com.teinproductions.tein.theverge.viewholders.ProductViewHolder;
import com.teinproductions.tein.theverge.viewholders.ReviewViewHolder;
import com.teinproductions.tein.theverge.viewholders.SearchViewHolder;

import org.jsoup.select.Elements;

import java.util.List;

public class ArticleListAdapter extends RecyclerView.Adapter<HeroViewHolder> {
    private static final int HERO_ITEM = 0;
    private static final int REVIEW_ITEM = 1;
    private static final int SEARCH_ITEM = 2;
    private static final int PRODUCT_ITEM = 3;

    private CTActivity activity;
    private List<Hero> data;

    public ArticleListAdapter(CTActivity activity, List<Hero> data) {
        this.activity = activity;
        this.data = data;
    }

    public void setData(List<Hero> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public List<Hero> getData() {
        return data;
    }

    @Override
    public HeroViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HERO_ITEM:
                return new HeroViewHolder(LayoutInflater.from(activity).inflate(R.layout.list_item_hero, parent, false));
            case REVIEW_ITEM:
                //return new ReviewViewHolder(LayoutInflater.from(activity).inflate(R.layout.list_item_review, parent, false));
            case SEARCH_ITEM:
                //return new SearchViewHolder(LayoutInflater.from(activity).inflate(R.layout.list_item_search, parent, false));
            case PRODUCT_ITEM:
                //return new ProductViewHolder(LayoutInflater.from(activity).inflate(R.layout.list_item_product, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(HeroViewHolder holder, int position) {
        holder.bind(activity, data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Hero item = data.get(position);
        // if (item instanceof Hero) etc. etc.
        return HERO_ITEM;
        /*if (element.classNames().contains("m-hero__slot") || element.classNames().contains("m-entry-slot")) {
            return HERO_ITEM;
        } else if (element.classNames().contains("m-reviews-index__node")) {
            return REVIEW_ITEM;
        } else if (element.classNames().contains("p-basic-article-list__node")) {
            return SEARCH_ITEM;
        } else if (element.classNames().contains("m-products-index__grid-item")) {
            return PRODUCT_ITEM;
        } else {
            return super.getItemViewType(position);
        }*/
    }

}
