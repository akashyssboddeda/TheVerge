package com.teinproductions.tein.theverge;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teinproductions.tein.theverge.models.Hero;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ArticleListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Hero>> {
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
        adapter = new ArticleListAdapter((CTActivity) getActivity(), new ArrayList<Hero>());
        recyclerView.setAdapter(adapter);
        srLayout.addView(recyclerView);
        srLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(0, null, ArticleListFragment.this);
            }
        });

        return srLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<Hero>> onCreateLoader(int id, Bundle args) {
        return new HeroLoader(getContext(), url, new HeroLoader.OnForceLoadListener() {
            @Override
            public void onForceLoad() {
                srLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        srLayout.setRefreshing(true);
                    }
                });
            }
        });
    }

    @Override
    public void onLoadFinished(Loader<List<Hero>> loader, List<Hero> data) {
        if (data == null) {
            Snackbar.make(recyclerView, "Something went horribly wrong", Snackbar.LENGTH_LONG).show();
        } else {
            adapter.setData(data);
            srLayout.setRefreshing(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Hero>> loader) {
        adapter.setData(new ArrayList<Hero>());
    }

    private static class HeroLoader extends BasicLoader<List<Hero>> {
        private String url;
        private OnForceLoadListener onForceLoadListener;

        private interface OnForceLoadListener {
            void onForceLoad();
        }

        public HeroLoader(Context context, String url, OnForceLoadListener listener) {
            super(context);
            this.url = url;
            this.onForceLoadListener = listener;
        }

        @Override
        public List<Hero> loadInBackground() {
            try {
                if (!IOUtils.checkNotConnected(getContext())) {
                    Document doc = Jsoup.connect(url).get();
                    Elements heroElements = doc.getElementsByClass("m-hero__slot");
                    heroElements.addAll(doc.getElementsByClass("m-entry-slot"));
                    filterItems(heroElements);

                    List<Hero> result = new ArrayList<>();
                    for (Element element : heroElements) {
                        result.add(new Hero(
                                parseTitle(element),
                                parseArticleLink(element),
                                parseImageURL(element),
                                parseAuthor(element), 0));
                    }
                    return result;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onForceLoad() {
            super.onForceLoad();
            if (onForceLoadListener != null) onForceLoadListener.onForceLoad();
        }

        /**
         * Makes sure all the items in {@code mData} are real links
         * to articles and not for example ads
         */
        private void filterItems(Elements items) {
            // TODO some articles still leak through this filter and are shown as "Unknown title" card
            for (Iterator<Element> iterator = items.iterator(); iterator.hasNext(); ) {
                Element element = iterator.next();
                Set classNames = element.classNames();
                if (classNames.contains("-entry-rock") || classNames.contains("-ad")) {
                    iterator.remove();
                }
            }
        }

        private String parseImageURL(Element element) {
            try {
                if (element.classNames().contains("m-hero__slot")) {
                    Element a = element.getElementsByClass("m-hero__slot-link").first();
                    Element imgDiv = a.getElementsByAttribute("data-original").first();
                    return imgDiv.attr("data-original");
                } else if (element.classNames().contains("m-entry-slot")) {
                    Element imgDiv = element.getElementsByAttribute("data-original").first();
                    return imgDiv.attr("data-original");
                } else throw new NullPointerException();
            } catch (NullPointerException e) {
                e.printStackTrace();
                // Return an image that says "No image"
                //noinspection SpellCheckingInspection
                return "http://best-classic-cars.com/images/no_image_available.png.pagespeed.ce.NRX39FjzIc.png";
            }
        }

        private String parseTitle(Element element) {
            try {
                if (element.classNames().contains("m-hero__slot")) {
                    Element a = element.getElementsByClass("m-hero__slot-link").first();
                    Element h2 = a.getElementsByTag("h2").first();
                    return h2.text();
                } else if (element.classNames().contains("m-entry-slot")) {
                    Element h3 = element.getElementsByTag("h3").first();
                    return h3.text();
                } else throw new NullPointerException();
            } catch (NullPointerException e) {
                e.printStackTrace();
                return "Unknown title";
            }
        }

        private String parseAuthor(Element element) {
            try {
                if (element.classNames().contains("m-hero__slot")) {
                    return element.getElementsByClass("byline").first().text();
                } else if (element.classNames().contains("m-entry-slot")) {
                    return element.getElementsByClass("author").first().text();
                } else throw new NullPointerException();
            } catch (NullPointerException e) {
                e.printStackTrace();
                return "Unknown author";
            }
        }

        private String parseArticleLink(Element element) {
            try {
                if (element.classNames().contains("m-hero__slot")) {
                    Element a = element.getElementsByClass("m-hero__slot-link").first();
                    return a.attr("href");
                } else if (element.classNames().contains("m-entry-slot")) {
                    Element h3 = element.getElementsByTag("h3").first();
                    Element a = h3.getElementsByTag("a").first();
                    return a.attr("href");
                } else throw new NullPointerException();
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static ArticleListFragment newInstance(String url) {
        ArticleListFragment fragment = new ArticleListFragment();
        Bundle args = new Bundle();
        args.putString(URL, url);
        fragment.setArguments(args);
        return fragment;
    }
}
