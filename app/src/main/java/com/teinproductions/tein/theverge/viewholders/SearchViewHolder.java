package com.teinproductions.tein.theverge.viewholders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.teinproductions.tein.theverge.ArticleActivity;
import com.teinproductions.tein.theverge.R;
import com.teinproductions.tein.theverge.viewholders.ArticleItemViewHolder;

import org.jsoup.nodes.Element;

public class SearchViewHolder extends ArticleItemViewHolder {
    TextView title, byline, blurb;
    ViewGroup root;

    String articleURL;

    public SearchViewHolder(final View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.title);
        byline = (TextView) itemView.findViewById(R.id.byline);
        blurb = (TextView) itemView.findViewById(R.id.blurb);
        root = (ViewGroup) itemView.findViewById(R.id.root);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (articleURL != null) ArticleActivity.openArticle(itemView.getContext(), articleURL);
            }
        });
    }

    @Override
    public void bind(Element element) {
        title.setText(parseTitle(element));
        byline.setText(parseByline(element));
        blurb.setText(parseBlurb(element));

        articleURL = parseArticleURL(element);
    }

    private String parseTitle(Element element) {
        try {
            return element.getElementsByTag("h3").first().text();
        } catch (NullPointerException e) {
            return "Unknown title";
        }
    }

    private String parseByline(Element element) {
        try {
            return element.getElementsByClass("byline").first().text();
        } catch (NullPointerException e) {
            return "Unknown author";
        }
    }

    private String parseBlurb(Element element) {
        try {
            return element.getElementsByClass("blurb").first().text();
        } catch (NullPointerException e) {
            return "Unknown blurb";
        }
    }

    private String parseArticleURL(Element element) {
        try {
            Element h3 = element.getElementsByTag("h3").first();
            return h3.getElementsByTag("a").attr("href");
        } catch (NullPointerException e) {
            return null;
        }
    }
}

