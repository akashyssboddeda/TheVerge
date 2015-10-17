package com.teinproductions.tein.theverge;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class ArticleListFragment extends Fragment {
    private static final String URL = "URL";
    private static final String DATA = "data";

    private SwipeRefreshLayout srLayout;
    private RecyclerView recyclerView;
    private ArticleListAdapter adapter;
    private String url;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.url = getArguments().getString(URL);

        srLayout = new SwipeRefreshLayout(getActivity());
        recyclerView = new RecyclerView(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ArticleListAdapter(getActivity(), new Elements());
        recyclerView.setAdapter(adapter);
        srLayout.addView(recyclerView);
        srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();
        return srLayout;
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
            public void onCacheLoaded(Elements cache) {
                adapter.setData(cache);
            }

            @Override
            public void onWebLoaded(Elements result) {
                srLayout.setRefreshing(false);
                adapter.setData(result);
            }

            @Override
            public void onWebLoadFailed(String errorMessage) {
                srLayout.setRefreshing(false);
                if (errorMessage != null) {
                    Snackbar.make(recyclerView, errorMessage, Snackbar.LENGTH_LONG).show();
                }
            }
        }, url, "m-hero__slot", "m-entry-slot").execute();
    }

    public static boolean checkNotConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnected();
    }


    public static ArticleListFragment newInstance(String url) {
        ArticleListFragment fragment = new ArticleListFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        fragment.setArguments(args);
        return fragment;
    }
}
