package com.teinproductions.tein.theverge;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String CACHE_FILE_NAME = "big7cache";

    /* - HeroTabFragment with tabs, hosts:
     * - ArticleListAdapter with several ViewHolders
     * - LongformFragment hosts ArticleListAdapter
     * - DownloadAsyncTask
     * - MainActivity keeps instances of all fragments
     * - ReviewFragment with ArticleListAdapter and additional controls
     * - ProductsFragment with ArticleListAdapter and additional controls
     */

    ActionBarDrawerToggle drawerToggle;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.home);

        swapFragment(HeroTabFragment.newInstance(
                getResources().getStringArray(R.array.home_urls), getResources().getStringArray(R.array.home_titles)));

        setDrawerToggle();
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
        String[] heroUrls, heroTitles;
        switch (menuItem.getItemId()) {
            case R.id.home:
                heroUrls = getResources().getStringArray(R.array.home_urls);
                heroTitles = getResources().getStringArray(R.array.home_titles);
                toolbar.setTitle(R.string.app_name);
                break;
            case R.id.longform:
                toolbar.setTitle(R.string.longform);
                return false;
            case R.id.reviews:
                swapFragment(new ReviewFragment());
                toolbar.setTitle(R.string.reviews);
                drawerLayout.closeDrawer(navigationView);
                return true;
            case R.id.video:
                toolbar.setTitle(R.string.video);
                return false;
            case R.id.tech:
                heroUrls = getResources().getStringArray(R.array.tech_urls);
                heroTitles = getResources().getStringArray(R.array.tech_titles);
                toolbar.setTitle(R.string.tech);
                break;
            case R.id.science:
                heroUrls = getResources().getStringArray(R.array.science_urls);
                heroTitles = getResources().getStringArray(R.array.science_titles);
                toolbar.setTitle(R.string.science);
                break;
            case R.id.entertainment:
                heroUrls = getResources().getStringArray(R.array.entertainment_urls);
                heroTitles = getResources().getStringArray(R.array.entertainment_titles);
                toolbar.setTitle(R.string.entertainment);
                break;
            case R.id.transportation:
                heroUrls = getResources().getStringArray(R.array.transportation_urls);
                heroTitles = getResources().getStringArray(R.array.transportation_titles);
                toolbar.setTitle(R.string.transportation);
                break;
            case R.id.design:
                heroUrls = getResources().getStringArray(R.array.design_urls);
                heroTitles = getResources().getStringArray(R.array.design_titles);
                toolbar.setTitle(R.string.design);
                break;
            case R.id.usAndWorld:
                heroUrls = getResources().getStringArray(R.array.us_and_world_urls);
                heroTitles = getResources().getStringArray(R.array.us_and_world_titles);
                toolbar.setTitle(R.string.us_and_world);
                break;
            default:
                return false;
        }

        if (heroUrls != null && heroTitles != null) {
            swapFragment(HeroTabFragment.newInstance(
                    heroUrls, heroTitles));
        }

        drawerLayout.closeDrawer(navigationView);
        return true;
    }

    private void swapFragment(Fragment newFragment) {
        if (newFragment == null) return;
        if (newFragment.equals(getSupportFragmentManager().findFragmentById(R.id.container)))
            return;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, newFragment)
                .commit();
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
                EditTextDialog.show(getSupportFragmentManager(), getString(R.string.enter_a_url),
                        new EditTextDialog.OnSearchClickListener() {
                            @Override
                            public void onClickSearch(String query) {
                                ArticleActivity.openArticle(MainActivity.this, query);
                            }
                        });
                return true;
            case R.id.menu_search:
                EditTextDialog.show(getSupportFragmentManager(), getString(R.string.search),
                        new EditTextDialog.OnSearchClickListener() {
                            @Override
                            public void onClickSearch(String query) {
                                Intent searchIntent = new Intent(MainActivity.this, SearchResultActivity.class);
                                searchIntent.putExtra(SearchResultActivity.QUERY, query);
                                startActivity(searchIntent);
                            }
                        });
                return true;
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
