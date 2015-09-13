package com.teinproductions.tein.theverge;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ArticleListPagerAdapter extends FragmentStatePagerAdapter {

    private String[] urls, titles;

    public ArticleListPagerAdapter(FragmentManager fm, String[] urls, String[] titles) {
        super(fm);
        this.urls = urls;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int i) {
        return ArticleListFragment.newInstance(urls[i]);
    }

    @Override
    public int getCount() {
        return urls.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
