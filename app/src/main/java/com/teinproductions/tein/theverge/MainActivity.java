package com.teinproductions.tein.theverge;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity implements DownloadAsyncTask.OnLoadedListener {

    DownloadAsyncTask asyncTask;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        asyncTask = new DownloadAsyncTask(this, this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        asyncTask.execute("http://www.theverge.com/");
    }

    @Override
    public void onLoaded(String s) {
        if (check400Number(s) || getString(R.string.no_network).equals(s)) {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        }

        Document doc = Jsoup.parse(s);
        Elements big7 = doc.getElementsByClass("big7").first().getElementsByTag("a");

        recyclerView.setAdapter(new Big7Adapter(this, big7));
    }

    private boolean check400Number(String s) {
        try {
            int check = Integer.parseInt(s);
            return check >= 400;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
