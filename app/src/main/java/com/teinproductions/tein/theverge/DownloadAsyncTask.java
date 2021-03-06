/*
package com.teinproductions.tein.theverge;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadAsyncTask extends AsyncTask<String, Void, String> {

    private Context context;
    private OnLoadedListener listener;

    interface OnLoadedListener {
        void onLoaded(String s, boolean fromCache);
    }

    public DownloadAsyncTask(Context context) {
        this.context = context;
    }

    public DownloadAsyncTask(Context context, OnLoadedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public String doInBackground(String... params) {
        try {
            //TODO use Jsoup.connect(params[0]).get();
            return fetchWebPage(context, params[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkNotConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnected();
    }

    public static String read(InputStream inputStream) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onLoaded(s, false);
    }

    public void setListener(OnLoadedListener listener) {
        this.listener = listener;
    }


    public static String fetchWebPage(Context context, String URL) throws IOException {
        if (checkNotConnected(context)) return null;

        URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(20000);
        conn.setConnectTimeout(30000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        int response = conn.getResponseCode();
        //Log.d("THEVERGE", "Response: " + response);

        if (response >= 400) return "" + response;

        InputStream is = conn.getInputStream();
        return read(is);
    }
}
*/
