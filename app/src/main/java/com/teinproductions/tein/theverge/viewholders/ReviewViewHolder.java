package com.teinproductions.tein.theverge.viewholders;

import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.teinproductions.tein.theverge.ArticleActivity;
import com.teinproductions.tein.theverge.CTActivity;
import com.teinproductions.tein.theverge.R;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;

public class ReviewViewHolder extends ArticleItemViewHolder {
    TextView vergeScoreTV, titleTV, blurbTV, priceTV;
    ImageView imageView;
    String articleURL;

    public ReviewViewHolder(View itemView) {
        super(itemView);

        vergeScoreTV = (TextView) itemView.findViewById(R.id.verge_score_textView);
        titleTV = (TextView) itemView.findViewById(R.id.title);
        blurbTV = (TextView) itemView.findViewById(R.id.blurb);
        priceTV = (TextView) itemView.findViewById(R.id.price_textView);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);

        itemView.findViewById(R.id.cardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (articleURL != null && URLUtil.isValidUrl(articleURL))
                    ArticleActivity.openArticle((CTActivity) ReviewViewHolder.this.itemView.getContext(), articleURL);
            }
        });
    }

    @Override
    public void bind(CTActivity activity, Element element) {
        parseTitle(element);
        parseBlurb(element);
        parsePrice(element);
        parseVergeScore(element);
        parseImage(element);
        parseArticleURL(element);
    }

    private void parseTitle(Element element) {
        String title = null;
        try {
            Element contentDiv = element.getElementsByClass("content").first();
            Element reviewDiv = contentDiv.getElementsByClass("review").first();
            title = reviewDiv.getElementsByTag("h3").first().text();
        } catch (NullPointerException ignored) {
        }

        if (title == null) titleTV.setVisibility(View.GONE);
        else {
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(title);
            imageView.setContentDescription(title);
        }
    }

    private void parseBlurb(Element element) {
        String blurb = null;
        try {
            Element contentDiv = element.getElementsByClass("content").first();
            Element reviewDiv = contentDiv.getElementsByClass("review").first();
            Element p = reviewDiv.getElementsByTag("p").first();
            Elements children = p.getAllElements();

            // Remove the tags with class="full-review-link" from the children Set
            for (Iterator<Element> iterator = children.iterator(); iterator.hasNext(); ) {
                if (iterator.next().classNames().contains("full-review-link")) {
                    iterator.remove();
                }
            }

            blurb = children.text();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (blurb == null) blurbTV.setVisibility(View.GONE);
        else {
            blurbTV.setVisibility(View.VISIBLE);
            blurbTV.setText(blurb);
        }
    }

    private void parsePrice(Element element) {
        String price = null;
        try {
            Element contentDiv = element.getElementsByClass("content").first();
            Element infoDiv = contentDiv.getElementsByClass("info").first();
            price = infoDiv.getElementsByTag("strong").first().text();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (price == null) priceTV.setVisibility(View.GONE);
        else {
            priceTV.setVisibility(View.VISIBLE);
            priceTV.setText(price);
        }
    }

    private void parseVergeScore(Element element) {
        String vergeScore = null;
        try {
            Element imageDiv = element.getElementsByClass("image").first();
            Element scoreDiv = imageDiv.getElementsByAttributeValue("itemprop", "reviewRating").first();
            vergeScore = scoreDiv.getElementsByTag("strong").first().text();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (vergeScore == null) vergeScoreTV.setVisibility(View.GONE);
        else {
            vergeScoreTV.setVisibility(View.VISIBLE);
            vergeScoreTV.setText(vergeScore);
        }
    }

    private void parseImage(Element element) {
        String imageUrl = null;
        try {
            Element imgDiv = element.getElementsByClass("image").first();
            Element img = imgDiv.getElementsByClass("p-dynamic-image").first();
            imageUrl = img.attr("data-original");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imageUrl == null) imageView.setVisibility(View.GONE);
        else {
            imageView.setVisibility(View.VISIBLE);
            Picasso.with(itemView.getContext()).load(imageUrl).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    setRatio();
                }

                public void onError() {/*ignored*/}
            });
        }
    }

    private void setRatio() {
        // Set 16:9 ratio on imageView, according to the Material Design specs
        imageView.getLayoutParams().height = imageView.getWidth() / 16 * 9;
        imageView.requestLayout();
    }

    private void parseArticleURL(Element element) {
        try {
            Element contentDiv = element.getElementsByClass("content").first();
            Element reviewDiv = contentDiv.getElementsByClass("review").first();
            Element h3 = reviewDiv.getElementsByTag("h3").first();
            articleURL = h3.getElementsByTag("a").first().attr("href");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
