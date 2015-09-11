package com.teinproductions.tein.theverge;


import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jsoup.nodes.Element;

public class HeroViewHolder extends ArticleItemViewHolder {
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

    @Override
    public void bind(Element element) {
        image.setImageDrawable(null);
        String imageURL = parseImageURL(element);
        Picasso.with(itemView.getContext()).load(imageURL).into(image, new Callback() {
            @Override
            public void onSuccess() {
                setRatio(image);
            }

            @Override
            public void onError() {/*ignored*/}
        });

        String title = parseTitle(element);
        primaryText.setText(title);
        image.setContentDescription(title);
        subtext.setText(parseAuthor(element));

        final String articleLink = parseArticleLink(element);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 9-7-2015 Check if href is not a re/code web page
                if (articleLink != null) {
                    ArticleActivity.openArticle(itemView.getContext(), articleLink);
                }
            }
        });
    }

    String parseTitle(Element element) {
        try {
            if (element.classNames().contains("m-hero__slot")) {
                Element a = element.getElementsByClass("m-hero__slot-link").first();
                Element h2 = a.getElementsByTag("h2").first();
                return h2.text();
            } else if (element.classNames().contains("m-entry-slot")) {
                Element h3 = element.getElementsByTag("h3").first();
                return h3.text();
            } else throw new NullPointerException();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return "Unknown title";
        }
    }

    String parseAuthor(Element element) {
        try {
            if (element.classNames().contains("m-hero__slot")) {
                return element.getElementsByClass("byline").first().text();
            } else if (element.classNames().contains("m-entry-slot")) {
                return element.getElementsByClass("author").first().text();
            } else throw new NullPointerException();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return "Unknown author";
        }
    }

    String parseArticleLink(Element element) {
        try {
            if (element.classNames().contains("m-hero__slot")) {
                Element a = element.getElementsByClass("m-hero__slot-link").first();
                return a.attr("href");
            } else if (element.classNames().contains("m-entry-slot")) {
                Element h3 = element.getElementsByTag("h3").first();
                Element a = h3.getElementsByTag("a").first();
                return a.attr("href");
            } else throw new NullPointerException();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    String parseImageURL(Element element) {
        try {
            if (element.classNames().contains("m-hero__slot")) {
                Element a = element.getElementsByClass("m-hero__slot-link").first();
                Element imgDiv = a.getElementsByAttribute("data-original").first();
                return imgDiv.attr("data-original");
            } else if (element.classNames().contains("m-entry-slot")) {
                Element imgDiv = element.getElementsByAttribute("data-original").first();
                return imgDiv.attr("data-original");
            } else throw new NullPointerException();
        } catch (NullPointerException e) {
            e.printStackTrace();
            // Return an image that says "No image"
            //noinspection SpellCheckingInspection
            return "http://best-classic-cars.com/images/no_image_available.png.pagespeed.ce.NRX39FjzIc.png";
        }
    }
}
