package com.teinproductions.tein.theverge;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
        //articleURL = "http://www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream";

        ll = (LinearLayout) findViewById(R.id.linearLayout);
        titleTV = (TextView) findViewById(R.id.title);
        authorTV = (TextView) findViewById(R.id.author);
        subTV = (TextView) findViewById(R.id.subtitle);
        mainImg = (ImageView) findViewById(R.id.articleMainImage);

        refresh();
    }

    public static void openArticle(Context context, String link) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(ArticleActivity.ARTICLE_URL, link);
        context.startActivity(intent);
    }

    interface ArticlePiece {
    }

    class Text implements ArticlePiece {
        static final int PARAGRAPH = 0;
        static final int THEVERGE_HEADER = 1;
        static final int H2HEADER = 2;
        static final int SMALL = 3;
        static final int H3HEADER = 4;

        final String text;
        final int type;

        public Text(String text, int type) {
            this.text = text;
            this.type = type;
        }
    }

    class Image implements ArticlePiece {
        final String url;

        public Image(String url) {
            this.url = url;
        }
    }

    class Whitespace implements ArticlePiece {
    }

    private void refresh() {
        new AsyncTask<Void, Void, Void>() {
            String title, author, date, sub, imgSrc;
            ArrayList<ArticlePiece> articlePieces = new ArrayList<>();

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

                    // Whitespace below title and author
                    articlePieces.add(new Whitespace());

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
                    return null;
                }
            }

            private void handleArticle(Element article) {
                loadMainArticleImage(article);
                try {
                    Elements elements = article.getElementsByClass("m-article__entry")
                            .first().children();
                    handleParagraphs(elements);
                } catch (NullPointerException ignored) {/*ignored*/}
            }

            /**
             * Handle pieces of articles with mainly paragraphs in them.
             * For example: main article content, or "m-snippet thin" div of review.
             * @param elements The elements at the "p" level (so including the p-s).
             */
            private void handleParagraphs(Elements elements) {
                for (int i = 0; i < elements.size(); i++) {
                    Element element = elements.get(i);
                    String tagName = element.tagName();
                    if ("p".equals(tagName)) {
                        if (element.children().size() > 0 && "q".equals(element.child(0).tagName())) {
                            // Big red italic heading
                            articlePieces.add(new Whitespace());
                            articlePieces.add(new Text(element.text(), Text.THEVERGE_HEADER));
                            articlePieces.add(new Whitespace());
                        } else if (element.children().size() > 0 && "small".equals(element.child(0).tagName())) {
                            // Small text:
                            // www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                            articlePieces.add(new Text(element.text(), Text.SMALL));
                        } else if (element.children().size() > 0 && "img".equals(element.child(0).tagName())) {
                            addImage(element);
                        } else if (element.html().isEmpty() || element.html().equals(" ")) {
                            // Add nothing.
                        } else {
                            // Regular paragraph
                            articlePieces.add(new Text(element.text(), Text.PARAGRAPH));
                        }
                    } else if ("q".equals(tagName)) {
                        // Big red italic heading
                        articlePieces.add(new Whitespace());
                        articlePieces.add(new Text(element.text(), Text.THEVERGE_HEADER));
                        articlePieces.add(new Whitespace());
                    } else if ("h2".equals(tagName)) {
                        // www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                        articlePieces.add(new Whitespace());
                        articlePieces.add(new Text(element.text(), Text.H2HEADER));
                        articlePieces.add(new Whitespace());
                    } else if ("h3".equals(tagName)) {
                        // www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                        articlePieces.add(new Whitespace());
                        articlePieces.add(new Text(element.text(), Text.H3HEADER));
                        articlePieces.add(new Whitespace());
                    } else if ("figure".equals(tagName)) {
                        // www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                        try {
                            String imgURL = element.getElementsByAttribute("data-original").first().attr("data-original");
                            articlePieces.add(new Image(imgURL));
                        } catch (NullPointerException ignored) {/*ignored*/}
                    } else if ("img".equals(tagName)) {
                        addImage(element);
                    } else if ("aside".equals(tagName)) {
                        addImage(element);
                    }
                    // TODO handle <ul>: www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                }
            }

            private void addImage(Element element) {
                String url;

                Element dataOriginalImg = element.getElementsByAttribute("data-original").first();
                if (dataOriginalImg != null) {
                    url = dataOriginalImg.attr("data-original");
                    articlePieces.add(new Image(url));
                    return;
                }

                Element srcImg = element.getElementsByAttribute("src").first();
                if (srcImg != null) {
                    url = srcImg.attr("src");
                    articlePieces.add(new Image(url));
                }
            }

            private void handleReview(Element article) {
                loadMainArticleImage(article);
                // Example of a Verge review: http://www.theverge.com/2015/7/8/8911731/apple-music-review
                // TODO: 9-7-2015 Surround with try/catch
                handleIntro(article, "m-review__intro");

                Elements body = article.getElementsByAttributeValueContaining(
                        "itemprop", "reviewBody").first().children();
                handleBody(body);
            }

            private void handleFeature(Element article) {
                loadMainArticleImage(article);
                handleIntro(article, "m-feature__intro");

                Elements body = article.getElementsByClass("m-feature__body").first().children();
                handleBody(body);
            }

            private void handleIntro(Element article, String introDivClassName) {
                Elements intro = article.getElementsByClass(introDivClassName).first().children();
                handleParagraphs(intro);

                // Add a whitespace beneath the intro:
                articlePieces.add(new Whitespace());
            }

            private void handleBody(Elements snippetDivs) {
                for (int i = 0; i < snippetDivs.size(); i++) {
                    Element snippet = snippetDivs.get(i);
                    if (snippet.classNames().contains("thin")
                            || snippet.classNames().contains("full-image")
                            || snippet.classNames().contains("e-image")) {
                        /*Elements ps = snippet.getElementsByTag("p");
                        for (int j = 0; j < ps.size(); j++) {
                            // Figure out whether this is a big text
                            Element q = ps.get(j).getElementsByTag("q").first();
                            if (q == null) {
                                // Not a big text
                                articlePieces.add(new Text(ps.get(j).text(), Text.PARAGRAPH));
                            } else {
                                // A big text
                                articlePieces.add(new Text(ps.get(j).text(), Text.THEVERGE_HEADER));
                            }
                        }*/
                        Elements elements = snippet.children();
                        handleParagraphs(elements);
                    }/* else if (snippet.classNames().contains("full-image") || snippet.classNames().contains("e-image")) {
                        // TODO e-images are now also shown when they're inside an m-snippet thin div.
                        *//* Sometimes there is no snippet wrapper div around the an e-image,
                        and the <figure class="e-image"> is then on the same level as the snippets.
                        Example: http://www.theverge.com/2015/7/6/8887691/gopro-hero-4-session-camera-cube-hands-on *//*
                        String imgURL = getFullImage(snippet);
                        if (imgURL != null) articlePieces.add(new Image(imgURL));
                    }*/
                }

                // TODO: 10-7-2015 photo essays! expample: http://www.theverge.com/2015/7/6/8887691/gopro-hero-4-session-camera-cube-hands-on
            }

            private String getFullImage(Element snippet) {
                Element figure = snippet.getElementsByClass("e-image").first();
                if (figure != null) {
                    Element span = figure.getElementsByTag("span").first();
                    return span.getElementsByAttribute("data-original").first().attr("data-original");
                }
                Element img = snippet.getElementsByAttribute("src").first();
                if (img != null) return img.attr("src");

                return null;
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
                // Convert 16dp and 5dp padding to px
                final int px16dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
                final int px5dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                //textViewParams.setMargins(px, 0, px, 0);
                for (int i = 0; i < articlePieces.size(); i++) {
                    ArticlePiece piece = articlePieces.get(i);
                    if (piece instanceof Text) {
                        TextView tv = new TextView(ArticleActivity.this);
                        tv.setText(((Text) piece).text);
                        tv.setPadding(px16dp, 0, px16dp, 0);

                        switch (((Text) piece).type) {
                            case Text.PARAGRAPH:
                                tv.setTextColor(getResources().getColor(android.R.color.black));
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                                break;
                            case Text.H2HEADER:
                                tv.setTextColor(getResources().getColor(android.R.color.black));
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                                tv.setTypeface(null, Typeface.BOLD);
                                break;
                            case Text.H3HEADER:
                                tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                                tv.setTypeface(null, Typeface.BOLD);
                                break;
                            case Text.THEVERGE_HEADER:
                                String text = ((Text) piece).text;
                                if (text.startsWith("\"") && text.endsWith("\"")) {
                                    tv.setText(text.substring(1, text.length() - 1));
                                }

                                tv.setText(tv.getText().toString().toUpperCase());
                                tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                                tv.setTypeface(null, Typeface.ITALIC);
                        }

                        ll.addView(tv, textViewParams);
                    } else if (piece instanceof Whitespace) {
                        ll.addView(new TextView(ArticleActivity.this), textViewParams);
                        // TODO: 2-8-2015 Use android.support.v4.widget.Space instead of empty TextView
                    } else if (piece instanceof Image) {
                        ImageView imageView = new ImageView(ArticleActivity.this);
                        imageView.setPadding(0, px5dp, 0, px5dp);
                        ll.addView(imageView, imageViewParams);
                        // Set 16:9 ratio on imageView  TODO: 10-7-2015 Show full image
                        //imageView.getLayoutParams().height = ll.getWidth() / 16 * 9;
                        //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.requestLayout();
                        Picasso.with(ArticleActivity.this).load(((Image) piece).url)
                                .resize(ll.getWidth(), imageView.getHeight()).into(imageView);
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
}
