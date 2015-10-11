package com.teinproductions.tein.theverge;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HeroTabFragment extends Fragment {
    private static final String URLS = "URLS";
    private static final String TITLES = "TITLES";

    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hero_tab, container, false);

        viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        tabLayout = ((MainActivity) getActivity()).getTabLayout();

        String[] urls = getArguments().getStringArray(URLS);
        String[] titles = getArguments().getStringArray(TITLES);
        loadData(urls, titles);

        return rootView;
    }

    public void loadData(String[] urls, String[] titles) {
        viewPager.setAdapter(new ArticleListPagerAdapter(
                getActivity().getSupportFragmentManager(), urls, titles));
        if (urls.length == 1) tabLayout.setVisibility(View.GONE);
        else {
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    public static HeroTabFragment newInstance(String[] urls, String[] titles) {
        HeroTabFragment fragment = new HeroTabFragment();

        Bundle args = new Bundle();
        args.putStringArray(URLS, urls);
        args.putStringArray(TITLES, titles);
        fragment.setArguments(args);

        return fragment;
    }
}
