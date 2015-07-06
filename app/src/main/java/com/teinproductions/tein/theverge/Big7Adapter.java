package com.teinproductions.tein.theverge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        Element a = data.get(i);

        viewHolder.image.setImageDrawable(null);
        String imageURL;
        try {
            imageURL = a.getElementsByTag("div").first().attr("data-original");
            if (imageURL != null) {
                Picasso.with(context).load(imageURL).into(viewHolder.image, new Callback() {
                    @Override
                    public void onSuccess() {
                        viewHolder.setRatio();
                    }

                    @Override
                    public void onError() {/*ignored*/}
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        String title = a.getElementsByTag("h2").first().text();
        String subtext = a.getElementsByClass("byline").first().ownText();

        if (title != null) {
            viewHolder.primaryText.setText(title);
            viewHolder.image.setContentDescription(title);
        }
        if (subtext != null) viewHolder.subtext.setText(subtext);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView primaryText, subtext;
        private ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            primaryText = (TextView) itemView.findViewById(R.id.primary_text);
            subtext = (TextView) itemView.findViewById(R.id.subtext);
            image = (ImageView) itemView.findViewById(R.id.imageView);
        }

        void setRatio() {
            // Set 16:9 ratio on imageView, according to the Material Design specs
            image.getLayoutParams().height = image.getWidth() / 16 * 9;
            image.requestLayout();
        }
    }

    static class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {

        private OnLoadedListener listener;

        interface OnLoadedListener {
            void onLoaded(Bitmap bm);
        }

        public ImageDownloaderTask(OnLoadedListener listener) {
            this.listener = listener;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                URLConnection conn = url.openConnection();
                return BitmapFactory.decodeStream(conn.getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (listener != null) {
                listener.onLoaded(bitmap);
            }
        }
    }
}
