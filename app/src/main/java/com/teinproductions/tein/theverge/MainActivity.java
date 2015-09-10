package com.teinproductions.tein.theverge;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CACHE_FILE_NAME = "big7cache";

    ActionBarDrawerToggle drawerToggle;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ArticleListPagerAdapter pagerAdapter;

    ArticleListPagerAdapter.Category category = ArticleListPagerAdapter.Category.HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        refreshContent();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.home);
        tabLayout.setupWithViewPager(viewPager);

        setDrawerToggle();
    }

    private void refreshContent() {
        pagerAdapter = new ArticleListPagerAdapter(this, getSupportFragmentManager(), category);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (pagerAdapter.getCount() == 1) tabLayout.setVisibility(View.GONE);
        else tabLayout.setVisibility(View.VISIBLE);
    }

    private void setDrawerToggle() {
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer_xs, R.string.close_drawer_xs);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.home:
                category = ArticleListPagerAdapter.Category.HOME;
                toolbar.setTitle(R.string.app_name);
                break;
            case R.id.longform:
                category = ArticleListPagerAdapter.Category.LONGFORM;
                toolbar.setTitle(R.string.longform);
                break;
            case R.id.reviews:
                category = ArticleListPagerAdapter.Category.REVIEWS;
                toolbar.setTitle(R.string.reviews);
                break;
            case R.id.video:
                category = ArticleListPagerAdapter.Category.VIDEO;
                toolbar.setTitle(R.string.video);
                break;
            case R.id.tech:
                category = ArticleListPagerAdapter.Category.TECH;
                toolbar.setTitle(R.string.tech);
                break;
            case R.id.science:
                category = ArticleListPagerAdapter.Category.SCIENCE;
                toolbar.setTitle(R.string.science);
                break;
            case R.id.entertainment:
                category = ArticleListPagerAdapter.Category.ENTERTAINMENT;
                toolbar.setTitle(R.string.entertainment);
                break;
            case R.id.transportation:
                category = ArticleListPagerAdapter.Category.TRANSPORTATION;
                toolbar.setTitle(R.string.transportation);
                break;
            case R.id.design:
                category = ArticleListPagerAdapter.Category.DESIGN;
                toolbar.setTitle(R.string.design);
                break;
            case R.id.usAndWorld:
                category = ArticleListPagerAdapter.Category.US_AND_WORLD;
                toolbar.setTitle(R.string.us_and_world);
                break;
            default:
                return false;
        }

        refreshContent();
        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                //refresh(false, false);
                return true;
            case R.id.enterURL:
                return true;
            case R.id.menu_search:
                EditTextDialog.show(getSupportFragmentManager(), getString(R.string.search), new EditTextDialog.OnSearchClickListener() {
                    @Override
                    public void onClickSearch(String query) {
                        Intent searchIntent = new Intent(MainActivity.this, SearchResultActivity.class);
                        searchIntent.putExtra(SearchResultActivity.QUERY, query);
                        startActivity(searchIntent);
                    }
                });
        }
        return false;
    }

    public static String getFile(Context context) {
        StringBuilder sb;

        try {
            FileInputStream fis = context.openFileInput(CACHE_FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader buffReader = new BufferedReader(isr);

            sb = new StringBuilder();
            String line;
            while ((line = buffReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveFile(Context context, String toSave) {
        try {
            FileOutputStream fos = context.openFileOutput(CACHE_FILE_NAME, MODE_PRIVATE);
            fos.write(toSave.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
