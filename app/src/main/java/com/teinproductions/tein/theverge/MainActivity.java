package com.teinproductions.tein.theverge;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements DownloadAsyncTask.OnLoadedListener {

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
                refresh();
            }
        });

        // First load from cache
        String cache = getFile();
        if (cache != null) onLoaded(cache, false); // False, otherwise "Offline" message is shown

        refresh();
    }

    private void refresh() {
        srLayout.setRefreshing(true);
        new DownloadAsyncTask(this, this).execute("http://www.theverge.com/");
    }

    @Override
    public void onLoaded(String page, boolean fromCache) {
        try {
            Document doc = Jsoup.parse(page); // Throws IllegalArgumentException if page == null
            Element big7Div = doc.getElementsByClass("big7").first();
            Elements big7 = doc.getElementsByClass("big7").first().getElementsByTag("a");
            //recyclerView.setAdapter(new Big7Adapter(this, big7));
            ((Big7Adapter) recyclerView.getAdapter()).setData(big7);
            recyclerView.getAdapter().notifyDataSetChanged();

            // Everything went right so the file can be cached
            if (!fromCache) saveFile(big7Div.outerHtml());

            if (fromCache) {
                // Cache was loaded successfully, show out of date snackbar
                Toast.makeText(this, "Offline: data may be out of date", Toast.LENGTH_SHORT).show();
            }

            srLayout.setRefreshing(false);
        } catch (NullPointerException | IllegalArgumentException e) {
            e.printStackTrace();

            if (fromCache) {
                // No internet connection and no cache file
                // Show "No connection established" full screen
                Toast.makeText(this, "No connection established", Toast.LENGTH_SHORT).show();
                srLayout.setRefreshing(false);
            } else {
                // No proper internet retrieval but not tried cache yet
                onLoaded(getFile(), true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            refresh();
            return true;
        }

        return false;
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
