package com.teinproductions.tein.theverge;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuthorActivity extends AppCompatActivity {
    private static final String VERGE_USER_URL = "user_url";

    private String url;

    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView imageView;
    private TextView bioTV;
    private LinearLayout contactInfoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        url = getIntent().getStringExtra(VERGE_USER_URL);

        imageView = (ImageView) findViewById(R.id.user_photo);
        contactInfoContainer = (LinearLayout) findViewById(R.id.item_info_container);
        bioTV = (TextView) findViewById(R.id.bio);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);

        refresh();
    }

    private void refresh() {
        new AsyncTask<Void, Void, Void>() {
            private String name = null, imageURL = null, bio = null;
            private List<String> contactInfoNames = new ArrayList<>(), contactInfoContents = new ArrayList<>();

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Document doc = Jsoup.connect(url).get();

                    try {
                        name = doc.getElementsByClass("m-user-page__head").first()
                                .getElementsByTag("h1").first().text();
                    } catch (NullPointerException ignored) {/*ignored*/}

                    Element sideBar = doc.getElementsByClass("m-user-page__sidebar").first();
                    if (sideBar == null) {
                        bio = "Something went wrong.";
                        return null;
                    }

                    try {
                        imageURL = sideBar.getElementsByClass("m-user-page__avatar").first()
                                .attr("src");
                    } catch (NullPointerException ignored) {/*ignored*/}
                    try {
                        bio = sideBar.getElementsByClass("m-user-page__bio").first()
                                .getElementsByTag("p").first().text();
                    } catch (NullPointerException ignored) {/*ignored*/}

                    // Parse contact info pieces
                    Element links = sideBar.getElementsByClass("links").first();
                    int i = 0;
                    while (links != null) {
                        try {
                            String h6 = links.getElementsByTag("h6").get(i).text();
                            if ("email".equalsIgnoreCase(h6)) {
                                // h6 "email" doesn't have an <a> tag after it but an
                                // encoded script, so skip the email.
                                i++;
                                continue;
                            }
                            String a = links.getElementsByTag("a").get(i).outerHtml();
                            contactInfoNames.add(h6);
                            contactInfoContents.add(a);
                            i++;
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                collapsingToolbar.setTitle(name);
                bioTV.setText(bio);
                Picasso.with(AuthorActivity.this).load(imageURL).into(imageView);

                for (int i = 0; i < contactInfoNames.size(); i++) {
                    ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_user_info_item, contactInfoContainer, false);
                    ((TextView) layout.findViewById(R.id.name)).setText(firstCharUppercase(contactInfoNames.get(i)));
                    TextView content = (TextView) layout.findViewById(R.id.content);
                    content.setMovementMethod(LinkMovementMethod.getInstance());
                    content.setText(Html.fromHtml(contactInfoContents.get(i)));

                    contactInfoContainer.addView(layout);
                }
            }
        }.execute();
    }

    public static String firstCharUppercase(String string) {
        if (string.length() == 0) return string;

        char[] chars = string.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return String.valueOf(chars);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    public void onClickViewInBrowser(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean safe = activities.size() > 0;
        if (safe) startActivity(intent);
    }


    public static void openActivity(Activity activity, String userURL) {
        Intent intent = new Intent(activity, AuthorActivity.class);
        intent.putExtra(VERGE_USER_URL, userURL);
        activity.startActivity(intent);
    }
}
