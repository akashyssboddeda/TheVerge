package com.teinproductions.tein.theverge;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity implements DownloadAsyncTask.OnLoadedListener {

    DownloadAsyncTask asyncTask;
    RecyclerView recyclerView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        asyncTask = new DownloadAsyncTask(this, this);
        //textView = (TextView) findViewById(R.id.textView);
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

        /*for (int i = 0; i < big7.size(); i++) {
            Element a = big7.get(i);
            textView.append(Html.fromHtml(a.getElementsByTag("h2").first().html()) + "\n");
            textView.append(Html.fromHtml(a.getElementsByClass("byline").first().html()) + "\n\n");
        }*/
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
