package com.teinproductions.tein.theverge;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.select.Elements;

public class TagActivity extends AppCompatActivity {
    public static final String TAG_NAME = "tag_name";

    private String tagName;
    private SwipeRefreshLayout srLayout;
    private RecyclerView recyclerView;
    private ArticleListAdapter adapter;
    private RelativeLayout errorMessageLayout;
    private TextView errorMessageTV;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tagName = getIntent().getStringExtra(TAG_NAME);
        setTitle(getString(R.string.tag_activity_title, tagName));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        errorMessageLayout = (RelativeLayout) findViewById(R.id.errorMessageContainer);
        errorMessageTV = (TextView) findViewById(R.id.errorMessageTV);
        srLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArticleListAdapter(this, new Elements());
        recyclerView.setAdapter(adapter);

        srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void refresh() {
        srLayout.setRefreshing(true);
        new DownloadAsyncTask(this, new DownloadAsyncTask.OnLoadingFinishedListener() {
            @Override
            public void onCacheLoaded(Elements cache) {
                adapter.setData(cache);
                recyclerView.setVisibility(View.VISIBLE);
                errorMessageLayout.setVisibility(View.GONE);
            }

            @Override
            public void onWebLoaded(Elements result) {
                srLayout.setRefreshing(false);
                adapter.setData(result);
                recyclerView.setVisibility(View.VISIBLE);
                errorMessageLayout.setVisibility(View.GONE);
            }

            @Override
            public void onWebLoadFailed(String errorMessage) {
                srLayout.setRefreshing(false);
                if ("No connection to the server".equals(errorMessage)) {
                    recyclerView.setVisibility(View.GONE);
                    errorMessageLayout.setVisibility(View.VISIBLE);
                    errorMessageTV.setText(getString(R.string.tag_name_error, tagName));
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    errorMessageLayout.setVisibility(View.GONE);
                    Snackbar.make(recyclerView, errorMessage, Snackbar.LENGTH_LONG).show();
                }
            }
        }, "http://www.theverge.com/tag/" + tagName, "p-basic-article-list__node").execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    public void onClickTryAgain(View view) {
        MainActivity.showETDialog(this, R.string.enter_tag_name, R.string.go, new MainActivity.OnETDialogActionClickListener() {
            @Override
            public void onClick(String input) {
                tagName = input;
                setTitle(getString(R.string.tag_activity_title, tagName));
                refresh();
            }
        });
    }

    public void onClickSearch(View view) {
        Intent searchIntent = new Intent(this, SearchResultActivity.class);
        searchIntent.putExtra(SearchResultActivity.QUERY, tagName);
        startActivity(searchIntent);
    }
}