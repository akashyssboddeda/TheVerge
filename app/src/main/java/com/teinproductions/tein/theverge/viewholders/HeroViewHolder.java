package com.teinproductions.tein.theverge.viewholders;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.teinproductions.tein.theverge.ArticleActivity;
import com.teinproductions.tein.theverge.CTActivity;
import com.teinproductions.tein.theverge.R;
import com.teinproductions.tein.theverge.models.Hero;

import org.jsoup.nodes.Element;

public class HeroViewHolder extends RecyclerView.ViewHolder {
    private CardView cardView;
    private TextView primaryText, subtext;
    private ImageView image;

    public HeroViewHolder(View itemView) {
        super(itemView);

        cardView = (CardView) itemView.findViewById(R.id.cardView);
        primaryText = (TextView) itemView.findViewById(R.id.primary_text);
        subtext = (TextView) itemView.findViewById(R.id.subtext);
        image = (ImageView) itemView.findViewById(R.id.imageView);
    }

    public void bind(final CTActivity activity, final Hero hero) {
        image.setImageDrawable(null);
        Picasso.with(itemView.getContext()).load(hero.getImgUrl()).into(image, new Callback() {
            @Override
            public void onSuccess() {
                setRatio(image);
            }

            @Override
            public void onError() {}
        });

        primaryText.setText(hero.getTitle());
        image.setContentDescription(hero.getTitle());
        subtext.setText(hero.getAuthor());

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hero.getUrl() != null) {
                    ArticleActivity.openArticle(activity, hero.getUrl());
                }
            }
        });
    }

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
