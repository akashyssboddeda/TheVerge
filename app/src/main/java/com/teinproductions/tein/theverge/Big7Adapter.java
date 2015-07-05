package com.teinproductions.tein.theverge;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Big7Adapter extends RecyclerView.Adapter<Big7Adapter.ViewHolder> {

    private Context context;
    private Elements data;

    public Big7Adapter(Context context, Elements data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Element a = data.get(i);
        String title = a.getElementsByTag("h2").first().text();
        String subtext = a.getElementsByClass("byline").first().ownText();

        if (title != null) viewHolder.primaryText.setText(title);
        if (subtext != null) viewHolder.subtext.setText(subtext);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView primaryText, subtext;

        public ViewHolder(View itemView) {
            super(itemView);
            primaryText = (TextView) itemView.findViewById(R.id.primary_text);
            subtext = (TextView) itemView.findViewById(R.id.subtext);
        }
    }
}
