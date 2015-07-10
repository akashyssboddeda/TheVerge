package com.teinproductions.tein.theverge;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class ArticleActivity extends AppCompatActivity {

    public static final String ARTICLE_URL = "ARTICLE_URL";

    private String articleURL;

    LinearLayout ll;
    TextView titleTV, authorTV, subTV;
    ImageView mainImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        articleURL = getIntent().getStringExtra(ARTICLE_URL);

        ll = (LinearLayout) findViewById(R.id.linearLayout);
        titleTV = (TextView) findViewById(R.id.title);
        authorTV = (TextView) findViewById(R.id.author);
        subTV = (TextView) findViewById(R.id.subtitle);
        mainImg = (ImageView) findViewById(R.id.articleMainImage);

        refresh();
    }

    private void refresh() {
        new AsyncTask<Void, Void, Void>() {
            String title, author, date, sub, imgSrc;
            ArrayList<CharSequence> paragraphs = new ArrayList<>();

            class ImageURL implements CharSequence {
                final String url;
                public ImageURL(String url) {
                    this.url = url;
                }
                public int length() {
                    return url.length();
                }
                public char charAt(int index) {
                    return url.charAt(index);
                }
                public CharSequence subSequence(int start, int end) {
                    return url.subSequence(start, end);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Document document = Jsoup.connect(articleURL).get();

                    try {
                        // Parse the title TODO: 9-7-2015 Or should I call document.getElementsById("stream-title")?
                        title = document.getElementsByClass("instapaper_title").first().text();
                    } catch (NullPointerException ignored) {/*ignored*/}
                    try {
                        author = document.getElementsByClass("author").first().text();
                    } catch (NullPointerException ignored) {/*ignored*/}
                    try {
                        date = document.getElementsByClass("published").first().text();
                    } catch (NullPointerException ignored) {/*ignored*/}
                    try {
                        sub = document.getElementsByAttributeValueContaining(
                                "data-remote-headline-edit", "summary").first().text();
                    } catch (NullPointerException ignored) {/*ignored*/}

                    // Figure out whether it's an article (m-article), review (m-review) or feature (m-feature)
                    Element article = document.getElementsByTag("article").first();
                    if (article.classNames().contains("m-article")) {
                        handleArticle(article);
                    } else if (article.classNames().contains("m-review")) {
                        handleReview(article);
                    } else if (article.classNames().contains("m-feature")) {
                        handleFeature(article);
                    }

                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ArticleActivity.this, "IOException", Toast.LENGTH_SHORT).show();
                    return null;
                }
            }

            private void handleArticle(Element article) {
                loadMainArticleImage(article);
                try {
                    Elements text = article.getElementsByClass("m-article__entry")
                            .first().getElementsByTag("p");
                    for (int i = 0; i < text.size(); i++) {
                        paragraphs.add(text.get(i).text());
                    }
                } catch (NullPointerException ignored) {/*ignored*/}
            }

            private void handleReview(Element article) {
                loadMainArticleImage(article);
                // Example of a Verge review: http://www.theverge.com/2015/7/8/8911731/apple-music-review
                // TODO: 9-7-2015 Surround with try/catch
                handleIntro(article, "m-review__intro");

                Elements body = article.getElementsByAttributeValueContaining(
                        "itemprop", "reviewBody").first().getElementsByTag("div");
                handleBody(body);
            }

            private void handleFeature(Element article) {
                loadMainArticleImage(article);
                handleIntro(article, "m-feature__intro");

                Elements body = article.getElementsByClass("m-feature__body").first().getElementsByTag("div");
                handleBody(body);
            }

            private void handleIntro(Element article, String introDivClassName) {
                Elements intro = article.getElementsByClass(introDivClassName).first().getElementsByTag("p");
                for (int i = 0; i < intro.size(); i++) {
                    paragraphs.add(intro.get(i).text());
                }
                // Add a whitespace beneath the intro:
                paragraphs.add("\n");
            }

            private void handleBody(Elements snippetDivs) {
                for (int i = 0; i < snippetDivs.size(); i++) {
                    Element current = snippetDivs.get(i);
                    if (current.classNames().contains("thin")) {
                        Elements ps = current.getElementsByTag("p");
                        for (int j = 0; j < ps.size(); j++) {
                            // Figure out whether this is a big text
                            Element q = ps.get(j).getElementsByTag("q").first();
                            if (q == null) {
                                // Not a big text
                                paragraphs.add(ps.get(j).text());
                            } else {
                                // A big text
                                // TODO: 9-7-2015 Add big texts
                            }
                        }
                    } else if (current.classNames().contains("full-image")) {
                        String imgURL = current.getElementsByAttribute("src").first().attr("src");
                        paragraphs.add(new ImageURL(imgURL));
                    }
                }
            }

            private void loadMainArticleImage(Element article) {
                imgSrc = article.getElementsByClass("p-dynamic-image").first().attr("data-original");
            }

            @Override
            protected void onPostExecute(Void v) {
                if (title == null) titleTV.setVisibility(View.GONE);
                else titleTV.setText(title);
                if (sub == null) subTV.setVisibility(View.GONE);
                else subTV.setText(sub);
                authorAndDate();

                if (imgSrc != null) {
                    Picasso.with(ArticleActivity.this).load(imgSrc).into(mainImg);
                    // Set 16:9 ratio on imageView
                    mainImg.getLayoutParams().height = mainImg.getWidth() / 16 * 9;
                    mainImg.requestLayout();
                }

                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                // Convert 16dp padding to px
                final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
                textViewParams.setMargins(px, 0, px, 0);
                for (int i = 0; i < paragraphs.size(); i++) {
                    if (paragraphs.get(i) instanceof String) {
                        TextView tv = new TextView(ArticleActivity.this);
                        tv.setText(paragraphs.get(i));
                        ll.addView(tv, textViewParams);
                    } else if (paragraphs.get(i) instanceof ImageURL) {
                        ImageView imageView = new ImageView(ArticleActivity.this);
                        ll.addView(imageView, imageViewParams);
                        Picasso.with(ArticleActivity.this).load(((ImageURL) paragraphs.get(i)).url).into(imageView);
                    }
                }
            }

            private void authorAndDate() {
                if (author == null && date == null) authorTV.setVisibility(View.GONE);
                else if (author != null && date == null) {
                    authorTV.setText(author);
                } else if (author == null) {
                    // Display only date, without "on"
                    if (date.startsWith("on ")) {
                        date = date.substring(3);
                    }
                    authorTV.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    authorTV.setTextSize(getResources().getDimension(R.dimen.article_date_text_size));
                    authorTV.setText(date);
                } else {
                    // Display both author and date
                    SpannableString sString = new SpannableString(author + " " + date);
                    RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.8f);
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(android.R.color.darker_gray));
                    sString.setSpan(sizeSpan, author.length(), sString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    sString.setSpan(colorSpan, author.length(), sString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                    authorTV.setText(sString);
                }
            }

        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }
}
