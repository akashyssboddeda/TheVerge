package com.teinproductions.tein.theverge;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Set;

public class DownloadAsyncTask extends AsyncTask<Void, Void, Elements> {

    private String url;
    private Context context;
    private String[] classes;
    private OnLoadingFinishedListener loadListener;

    public DownloadAsyncTask(Context context, OnLoadingFinishedListener loadListener, String url, String... classes) {
        this.url = url;
        this.context = context;
        this.classes = classes;
        this.loadListener = loadListener;
    }

    private String errorMessage;

    @Override
    protected Elements doInBackground(Void... params) {
        try {
            if (checkNotConnected())
                throw new NullPointerException();

            Document doc = Jsoup.connect(url).get();
            Elements elements = new Elements();
            for (String divClass : classes) {
                elements.addAll(doc.getElementsByClass(divClass));
            }
            /*Elements elements = doc.getElementsByClass("m-hero__slot");
            elements.addAll(doc.getElementsByClass("m-entry-slot"));
            elements.addAll(doc.getElementsByClass("m-reviews-index__node"));
            elements.addAll(doc.getElementsByClass("m-products-index__grid-item"));*/
            filterItems(elements);

            return elements;
        } catch (IOException | NullPointerException e) {
            if (e instanceof HttpStatusException || e instanceof UnsupportedMimeTypeException)
                errorMessage = "No connection to the server";
            else if (e instanceof SocketTimeoutException)
                errorMessage = "Connection timed out";
            else if (e instanceof IOException) errorMessage = "No connection to the server";
            else errorMessage = "Please check your connection";

            return new Elements();
        }
    }

    private boolean checkNotConnected() {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnected();
    }

    /**
     * Makes sure all the items in {@code data} are real links
     * to articles and not for example ads
     */
    private void filterItems(Elements items) {
        // TODO some articles still leak through this filter and are shown as "Unknown title" card
        for (Iterator<Element> iterator = items.iterator(); iterator.hasNext(); ) {
            Element element = iterator.next();
            Set classNames = element.classNames();
            if (classNames.contains("-entry-rock") || classNames.contains("-ad")) {
                iterator.remove();
            }
        }
    }

    @Override
    protected void onPostExecute(Elements elements) {
        if (loadListener != null) loadListener.onLoaded(elements, errorMessage);
    }


    public interface OnLoadingFinishedListener {
        void onLoaded(Elements result, String errorMessage);
    }
}
