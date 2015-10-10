package com.teinproductions.tein.theverge;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Set;

public class DownloadAsyncTask extends AsyncTask<Void, Elements, Elements> {

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
        Uri uri =  Uri.parse(url);
        String urlWithoutScheme = (uri.getHost() + uri.getPath()).replace("/", "");

        // First try cache
        try {
            String cache = getFile(urlWithoutScheme);
            Document doc = Jsoup.parse(cache);
            Elements elements = new Elements();
            for (String divClass : classes) {
                elements.addAll(doc.getElementsByClass(divClass));
            }
            filterItems(elements);
            publishProgress(elements);
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }

        // Now try web
        try {
            if (checkNotConnected())
                throw new NullPointerException();

            Document doc = Jsoup.connect(url).get();
            Elements elements = new Elements();
            for (String divClass : classes) {
                elements.addAll(doc.getElementsByClass(divClass));
            }
//            Elements elements = doc.getElementsByClass("m-hero__slot");
//            elements.addAll(doc.getElementsByClass("m-entry-slot"));
//            elements.addAll(doc.getElementsByClass("m-reviews-index__node"));
//            elements.addAll(doc.getElementsByClass("m-products-index__grid-item"));
            filterItems(elements);

            // Save the cache
            saveFile(elements.outerHtml(), urlWithoutScheme);

            return elements;
        } catch (IOException | NullPointerException e) {
            if (e instanceof HttpStatusException || e instanceof UnsupportedMimeTypeException)
                errorMessage = "No connection to the server";
            else if (e instanceof SocketTimeoutException)
                errorMessage = "Connection timed out";
            else if (e instanceof IOException) errorMessage = "No connection to the server";
            else errorMessage = "Please check your connection";

            return null;
        }
    }

    @Override
    protected void onProgressUpdate(Elements... values) {
        Toast.makeText(context, "cache loaded", Toast.LENGTH_SHORT).show();
        if (loadListener != null) loadListener.onCacheLoaded(values[0]);
    }

    @Override
    protected void onPostExecute(Elements elements) {
        Toast.makeText(context, "web loaded", Toast.LENGTH_SHORT).show();
        if (loadListener == null) return;

        if (elements == null) {
            loadListener.onWebLoadFailed(errorMessage);
        } else {
            loadListener.onWebLoaded(elements);
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


    public String getFile(String fileName) {
        StringBuilder sb;

        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader buffReader = new BufferedReader(isr);

            sb = new StringBuilder();
            String line;
            while ((line = buffReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveFile(String toSave, String fileName) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(toSave.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public interface OnLoadingFinishedListener {
        void onCacheLoaded(Elements cache);

        void onWebLoaded(Elements result);

        void onWebLoadFailed(String errorMessage);
    }
}
