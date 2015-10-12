package com.teinproductions.tein.theverge;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    /* -- SKETCH --
     * - HeroTabFragment with tabs, hosts:
     * - ArticleListAdapter with several ViewHolders
     * - LongformFragment hosts ArticleListAdapter
     * - DownloadAsyncTask
     * - MainActivity keeps instances of all fragments
     * - ReviewFragment with ArticleListAdapter and additional controls
     * - ProductsFragment with ArticleListAdapter and additional controls
     */

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TabLayout tabLayout;

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

    public TabLayout getTabLayout() {
        return tabLayout;
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
                showETDialog(R.string.enter_a_url, R.string.go, new OnETDialogActionClickListener() {
                    @Override
                    public void onClick(String input) {
                        ArticleActivity.openArticle(MainActivity.this, input);
                    }
                });
                return true;
            case R.id.menu_search:
                showETDialog(R.string.search, R.string.search, new OnETDialogActionClickListener() {
                    @Override
                    public void onClick(String input) {
                        Intent searchIntent = new Intent(MainActivity.this, SearchResultActivity.class);
                        searchIntent.putExtra(SearchResultActivity.QUERY, input);
                        startActivity(searchIntent);
                    }
                });
                return true;
        }
        return false;
    }

    private void showETDialog(@StringRes int title, @StringRes int positiveButton, final OnETDialogActionClickListener listener) {
        final EditText query = new EditText(this);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(query)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onClick(query.getText().toString());
                    }
                })
                .setNeutralButton(R.string.cancel, null)
                .create();

        query.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(query.length() != 0);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /*ignored*/ }

            public void afterTextChanged(Editable s) { /*ignored*/ }
        });
        dialog.show();
    }

    private interface OnETDialogActionClickListener {
        void onClick(String input);
    }
}
