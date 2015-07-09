package com.teinproductions.tein.theverge;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity {

    public static final String CACHE_FILE_NAME = "big7cache";

    RecyclerView recyclerView;
    SwipeRefreshLayout srLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        srLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Big7Adapter(this, new Elements(0)));

        srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(false, false);
            }
        });

        // First load from cache
        //String cache = getFile();
        //if (cache != null) onLoaded(cache, false); // False, otherwise "Offline" message is shown

        refresh(true, false);
    }

    private void refresh(final boolean firstTryCache, final boolean alreadyTriedCache) {
        srLayout.setRefreshing(true);
        new AsyncTask<Void, Void, Elements>() {
            boolean fromCache = false;
            String errorMessage;
            String toCache;

            @Override
            protected Elements doInBackground(Void... params) {
                try {
                    if (firstTryCache && !alreadyTriedCache) {
                        // First, try with cache:
                        throw new IOException();
                    }


                    if (checkNotConnected(MainActivity.this))
                        throw new NullPointerException();

                    Document doc = Jsoup.connect("http://www.theverge.com/").get();
                    Element big7Div = doc.getElementsByClass("big7").first();
                    toCache = big7Div.outerHtml();

                    return big7Div.getElementsByTag("a");
                } catch (IOException | NullPointerException e) {
                    if (e instanceof HttpStatusException || e instanceof UnsupportedMimeTypeException)
                        errorMessage = "No connection to the server";
                    else if (e instanceof SocketTimeoutException)
                        errorMessage = "Connection timed out";
                    else if (e instanceof IOException) errorMessage = "No connection to the server";
                    else errorMessage = "Please check your connection";

                    // Try with cache
                    fromCache = true;
                    String cache = getFile();
                    if (cache == null) return null;
                    Document doc = Jsoup.parse(cache);
                    return doc.getElementsByClass("big7").first().getElementsByTag("a");
                }
            }

            @Override
            protected void onPostExecute(Elements big7) {
                srLayout.setRefreshing(false);

                if (!fromCache && toCache != null) {
                    // Cache the file
                    saveFile(toCache);
                } else if (fromCache) {
                    if (firstTryCache) {
                        // Load the fetched data into the recyclerView
                        ((Big7Adapter) recyclerView.getAdapter()).setData(big7);
                        recyclerView.getAdapter().notifyDataSetChanged();

                        // Now try from the web:
                        refresh(false, true);
                        return;
                    }

                    // Display the error message in a snackbar
                    if (alreadyTriedCache) errorMessage = "Offline mode: data may be outdated";
                    Snackbar.make(findViewById(R.id.root), errorMessage, Snackbar.LENGTH_LONG).show();
                    if (big7 == null) {
                        // TODO: 9-7-2015 Display error message in card in recyclerView
                        return;
                    }
                }

                // Load the fetched data into the recyclerView
                ((Big7Adapter) recyclerView.getAdapter()).setData(big7);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            refresh(false, false);
            return true;
        }

        return false;
    }

    public static boolean checkNotConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnected();
    }

    private String getFile() {
        StringBuilder sb;

        try {
            FileInputStream fis = openFileInput(CACHE_FILE_NAME);
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

    private void saveFile(String toSave) {
        try {
            FileOutputStream fos = openFileOutput(CACHE_FILE_NAME, MODE_PRIVATE);
            fos.write(toSave.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
