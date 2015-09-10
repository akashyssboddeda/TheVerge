package com.teinproductions.tein.theverge;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class SearchResultActivity extends AppCompatActivity {
    public static final String QUERY = "query";
    private static final String DATA = "data";

    private RecyclerView recyclerView;
    private BasicArticleListAdapter adapter;
    private TextView errorTextView;

    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        query = getIntent().getExtras().getString(QUERY);
        getSupportActionBar().setTitle(firstCharUpperCase(query));

        errorTextView = (TextView) findViewById(R.id.error_textView);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BasicArticleListAdapter(this, new Elements());
        recyclerView.setAdapter(adapter);

        loadData(savedInstanceState);
    }

    private void loadData(Bundle savedInstanceState) {
        Elements data;
        if (savedInstanceState != null && (data = (Elements) savedInstanceState.getSerializable(DATA)) != null) {
            adapter.setData(data);
        } else {
            refresh();
        }
    }

    private void refresh() {
        new AsyncTask<Void, Void, Elements>() {
            private String errorMessage;

            @Override
            protected Elements doInBackground(Void... params) {
                try {
                    if (ArticleListFragment.checkNotConnected(SearchResultActivity.this)) {
                        errorMessage = getString(R.string.no_connection_established);
                        return null;
                    }

                    Document doc = Jsoup.connect("http://www.theverge.com/search?q=" + query).get();
                    return doc.getElementsByClass("p-basic-article-list__node");
                } catch (IOException e) {
                    e.printStackTrace();
                    errorMessage = getString(R.string.no_connection_established);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Elements elements) {
                if (errorMessage != null) {
                    recyclerView.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setText(errorMessage);
                } else {
                    adapter.setData(elements);
                }
            }
        }.execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DATA, adapter.getData());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    private static String firstCharUpperCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return String.valueOf(chars);
    }
}
