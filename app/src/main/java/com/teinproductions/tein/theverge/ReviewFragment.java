package com.teinproductions.tein.theverge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jsoup.select.Elements;

public class ReviewFragment extends Fragment {

    SwipeRefreshLayout srLayout;
    RecyclerView recyclerView;
    ArticleListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        srLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ArticleListAdapter(getContext(), new Elements());
        recyclerView.setAdapter(adapter);

        srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();
        return rootView;
    }

    private void refresh() {
        if (!srLayout.isRefreshing()) srLayout.post(new Runnable() {
            @Override
            public void run() {
                srLayout.setRefreshing(true);
            }
        });
        new DownloadAsyncTask(getContext(), new DownloadAsyncTask.OnLoadingFinishedListener() {
            @Override
            public void onLoaded(Elements result, String errorMessage) {
                srLayout.setRefreshing(false);
                adapter.setData(result);
                if (errorMessage != null) {
                    Snackbar.make(recyclerView, errorMessage, Snackbar.LENGTH_LONG).show();
                }
            }
        }, "http://www.theverge.com/reviews", "m-reviews-index__node").execute();
    }
}
