package com.teinproductions.tein.theverge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BasicArticleListAdapter extends RecyclerView.Adapter<BasicArticleListAdapter.ViewHolder> {

    Context context;
    Elements data;

    public BasicArticleListAdapter(Context context, Elements data) {
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_search, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, byline, blurb;
        ViewGroup root;

        String articleURL;

        public ViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.title);
            byline = (TextView) itemView.findViewById(R.id.byline);
            blurb = (TextView) itemView.findViewById(R.id.blurb);
            root = (ViewGroup) itemView.findViewById(R.id.root);

            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (articleURL != null) ArticleActivity.openArticle(context, articleURL);
                }
            });
        }

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
}
