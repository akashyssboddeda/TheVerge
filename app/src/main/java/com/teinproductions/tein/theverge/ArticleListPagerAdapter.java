package com.teinproductions.tein.theverge;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ArticleListPagerAdapter extends FragmentStatePagerAdapter {

    public enum Category {
        HOME(R.array.home_titles, R.array.home_urls),
        LONGFORM(R.array.longform_titles, R.array.longform_urls),
        REVIEWS(R.array.reviews_titles, R.array.reviews_urls),
        VIDEO(R.array.video_titles, R.array.video_urls),
        TECH(R.array.tech_titles, R.array.tech_urls),
        SCIENCE(R.array.science_titles, R.array.science_urls),
        ENTERTAINMENT(R.array.entertainment_titles, R.array.entertainment_urls),
        TRANSPORTATION(R.array.transportation_titles, R.array.transportation_urls),
        DESIGN(R.array.design_titles, R.array.design_urls),
        US_AND_WORLD(R.array.us_and_world_titles, R.array.us_and_world_urls);

        Category(@ArrayRes int titles, @ArrayRes int URLs) {
            this.titles = titles;
            this.URLs = URLs;
        }

        int titles, URLs;
    }

    private Category category;
    private Context context;

    public ArticleListPagerAdapter(Context context, FragmentManager fm, Category category) {
        super(fm);
        this.category = category;
        this.context = context;
    }

    public void setCategory(Category category) {
        this.category = category;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int i) {
        String url = context.getResources().getStringArray(category.URLs)[i];
        return ArticleListFragment.newInstance(url);
    }

    @Override
    public int getCount() {
        return context.getResources().getStringArray(category.titles).length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getResources().getStringArray(category.titles)[position];
    }
}
