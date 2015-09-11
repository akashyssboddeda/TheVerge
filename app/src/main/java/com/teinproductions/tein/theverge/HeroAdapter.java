package com.teinproductions.tein.theverge;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Iterator;
import java.util.Set;

public class HeroAdapter extends RecyclerView.Adapter<HeroAdapter.ViewHolder> {

    private Context context;
    private Elements data;

    public HeroAdapter(Context context, Elements data) {
        this.context = context;
        this.data = data;
    }

    public void setData(Elements data) {
        this.data = data;
        filterData();
    }

    /**
     * Makes sure all the items in {@code data} are real links
     * to articles and not ads or other things not (yet) supported
     */
    private void filterData() {
        // TODO some articles still leak through this filter and are shown as "Unknown title" card
        for (Iterator<Element> iterator = data.iterator(); iterator.hasNext();) {
            Element element = iterator.next();
            Set classNames = element.classNames();
            if (classNames.contains("-entry-rock") || classNames.contains("-ad")) {
                iterator.remove();
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_hero, viewGroup, false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        viewHolder.bind(data.get(i));
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView primaryText, subtext;
        private ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            primaryText = (TextView) itemView.findViewById(R.id.primary_text);
            subtext = (TextView) itemView.findViewById(R.id.subtext);
            image = (ImageView) itemView.findViewById(R.id.imageView);
        }

        void setRatio() {
            // Set 16:9 ratio on imageView, according to the Material Design specs
            image.getLayoutParams().height = image.getWidth() / 16 * 9;
            image.requestLayout();
        }

        void bind(Element element) {
            image.setImageDrawable(null);
            String imageURL = parseImageURL(element);
            Picasso.with(context).load(imageURL).into(image, new Callback() {
                @Override
                public void onSuccess() {
                    setRatio();
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
                        ArticleActivity.openArticle(context, articleLink);
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
                return "http://best-classic-cars.com/images/no_image_available.png.pagespeed.ce.NRX39FjzIc.png";
            }
        }
    }
}