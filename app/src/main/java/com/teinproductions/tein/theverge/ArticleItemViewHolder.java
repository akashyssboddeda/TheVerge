package com.teinproductions.tein.theverge;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import org.jsoup.nodes.Element;


abstract class ArticleItemViewHolder extends RecyclerView.ViewHolder {
    public ArticleItemViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(Element element);

    /**
     * Set 16:9 ratio on {@code imageView}, according to the Material Design specs
     *
     * @param imageView The ImageView to set this ratio on.
     */
    protected void setRatio(ImageView imageView) {
        imageView.getLayoutParams().height = imageView.getWidth() / 16 * 9;
        imageView.requestLayout();
    }
}
