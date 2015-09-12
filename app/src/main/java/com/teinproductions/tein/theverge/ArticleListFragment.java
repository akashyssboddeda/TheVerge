package com.teinproductions.tein.theverge;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Set;

public class ArticleListFragment extends Fragment {
    private static final String URL = "URL";

    SwipeRefreshLayout srLayout;
    RecyclerView recyclerView;
    ArticleListAdapter adapter;
    String url;

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

                    if (checkNotConnected(getActivity()))
                        throw new NullPointerException();

                    Document doc = Jsoup.connect(url).get();
                    Elements elements = doc.getElementsByClass("m-hero__slot");
                    elements.addAll(doc.getElementsByClass("m-entry-slot"));
                    elements.addAll(doc.getElementsByClass("m-reviews-index__node"));
                    filterItems(elements);
                    toCache = elements.outerHtml();

                    return elements;
                } catch (IOException | NullPointerException e) {
                    if (e instanceof HttpStatusException || e instanceof UnsupportedMimeTypeException)
                        errorMessage = "No connection to the server";
                    else if (e instanceof SocketTimeoutException)
                        errorMessage = "Connection timed out";
                    else if (e instanceof IOException) errorMessage = "No connection to the server";
                    else errorMessage = "Please check your connection";

                    // Try with cache
                    fromCache = true;
                    String cache = MainActivity.getFile(getActivity());
                    if (cache == null) return null;
                    Document doc = Jsoup.parse(cache);
                    return doc.getElementsByClass("m-hero__slot-link");
                }
            }

            /**
             * Makes sure all the items in {@code data} are real links
             * to articles and not ads or other things not (yet) supported
             */
            private void filterItems(Elements items) {
                // TODO some articles still leak through this filter and are shown as "Unknown title" card
                for (Iterator<Element> iterator = items.iterator(); iterator.hasNext();) {
                    Element element = iterator.next();
                    Set classNames = element.classNames();
                    if (classNames.contains("-entry-rock") || classNames.contains("-ad")) {
                        iterator.remove();
                    }
                }
            }

            @Override
            protected void onPostExecute(Elements elements) {
                srLayout.setRefreshing(false);
                if (!fromCache && toCache != null) {
                    // Cache the file
                    MainActivity.saveFile(getActivity(), toCache);
                } else if (fromCache) {
                    if (firstTryCache) {
                        // Load the fetched data into the recyclerView
                        if (elements != null) {
                            adapter.setData(elements);
                        }

                        // Now try from the web:
                        refresh(false, true);
                        return;
                    }

                    // Display the error message in a snackbar
                    if (alreadyTriedCache) errorMessage = "Offline mode: data may be outdated";
                    Snackbar.make(recyclerView, errorMessage, Snackbar.LENGTH_LONG).show();
                    if (elements == null) {
                        // TODO: 9-7-2015 Display error message in card in recyclerView
                        return;
                    }
                }

                // Load the fetched data into the recyclerView
                adapter.setData(elements);
            }
        }.execute();
    }

    private void refresh() {
        refresh(true, false);
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
