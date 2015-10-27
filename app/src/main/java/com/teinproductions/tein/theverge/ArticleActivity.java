package com.teinproductions.tein.theverge;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArticleActivity extends AppCompatActivity {
    // TODO: 8-9-2015 This-is-my-next-articles: http://www.theverge.com/2014/11/26/7290751/best-action-camera-you-can-buy

    public static final String ARTICLE_URL = "ARTICLE_URL";

    private String articleURL;

    private LinearLayout ll;
    private TextView titleTV, authorTV, subTV;
    private ImageView mainImg;
    private CollapsingToolbarLayout collToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initArticleURL();

        ll = (LinearLayout) findViewById(R.id.linearLayout);
        titleTV = (TextView) findViewById(R.id.title);
        authorTV = (TextView) findViewById(R.id.author);
        subTV = (TextView) findViewById(R.id.subtitle);
        mainImg = (ImageView) findViewById(R.id.articleMainImage);
        collToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);

        refresh();
    }

    private void initArticleURL() {
        articleURL = getIntent().getStringExtra(ARTICLE_URL);
        if (articleURL != null) return;

        Uri data = getIntent().getData();
        if (data != null && "www.theverge.com".equals(data.getHost())) {
            articleURL = data.toString();
        }
    }

    public static void openArticle(Context context, String url) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(ArticleActivity.ARTICLE_URL, url);
        context.startActivity(intent);
    }

    public void onClickViewInBrowser(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleURL));

        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean safe = activities.size() > 0;
        if (safe) startActivity(intent);
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
            private Document doc;
            private String title, date, sub, imgSrc;
            private String authorName, authorTheVergeLink;
            private List<ArticlePiece> articlePieces = new ArrayList<>();
            private String source, relatedItems;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    doc = Jsoup.connect(articleURL).get();

                    parseHeader();

                    // Add whitespace below title and author
                    articlePieces.add(new Whitespace());

                    // Figure out whether it's an article (m-article), review (m-review) or feature (m-feature)
                    Element article = doc.getElementsByTag("article").first();
                    if (article.classNames().contains("m-article")) {
                        handleArticle(article);
                    } else if (article.classNames().contains("m-review")) {
                        handleReview(article);
                    } else if (article.classNames().contains("m-feature")) {
                        handleFeature(article);
                    }

                    parseArticleSource();

                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            private void parseHeader() {
                Element header = doc.getElementsByClass("p-entry-header").first();
                try {
                    title = header.getElementsByClass("instapaper_title").first().text();
                } catch (NullPointerException ignored) {/*ignored*/}
                try {
                    authorName = header.getElementsByClass("author").first().text();
                } catch (NullPointerException ignored) {/*ignored*/}
                try {
                    authorTheVergeLink = header.getElementsByClass("author").first()
                            .getElementsByTag("a").first().attr("href");
                } catch (NullPointerException ignored) {/*ignored*/}
                try {
                    date = header.getElementsByClass("published").first().text();
                } catch (NullPointerException ignored) {/*ignored*/}
                try {
                    sub = header.getElementsByAttributeValueContaining(
                            "data-remote-headline-edit", "summary").first().text();
                } catch (NullPointerException ignored) {/*ignored*/}
            }

            private void parseArticleSource() {
                Element ul = doc.getElementsByClass("m-article__sources").first();
                if (ul == null) return;

                for (Element li : ul.children()) {
                    try {
                        if (!"li".equals(li.tagName())) continue;
                        if (li.classNames().contains("source")) {
                            source = li.getElementsByTag("a").first().outerHtml();
                        } else if (li.classNames().contains("tags")) {
                            Elements as = li.getElementsByTag("a");
                            as.first().attr("href", makeValidURL(as.first().attr("href")));
                            relatedItems = as.first().outerHtml();
                            for (int i = 1; i < as.size(); i++) {
                                as.get(i).attr("href", makeValidURL(as.get(i).attr("href")));
                                relatedItems += ", " + as.get(i).outerHtml();
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }

            /**
             * For example: "/tag/google" becomes "http://www.theverge.com/tag/google"
             * @param urlBefore URL to validate
             * @return Valid URL
             */
            private String makeValidURL(String urlBefore) {
                if (urlBefore.startsWith("/")) return "http://www.theverge.com" + urlBefore;
                else return urlBefore;
            }

            private void handleArticle(Element article) {
                loadMainArticleImage(article);
                try {
                    Elements elements = article.getElementsByClass("m-article__entry")
                            .first().children();
                    handleParagraphs(elements);
                } catch (NullPointerException ignored) {/*ignored*/}
            }

            private void handleParagraphs(Element element) {
                Elements elements = new Elements();
                elements.add(element);
                handleParagraphs(elements);
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
                        if (element.children().size() > 0) {
                            Element child;
                            if ((child = childWithTag(element, "q")) != null) {
                                handleParagraphs(child);
                            } else if ((child = childWithTag(element, "small")) != null) {
                                handleParagraphs(child);
                            } else if ((child = childWithTag(element, "img")) != null) {
                                handleParagraphs(child);
                            } else {
                                // Regular paragraph
                                articlePieces.add(new Text(element.html(), Text.PARAGRAPH));
                            }
                        } else if (!element.html().isEmpty() && !element.html().equals(" ")) {
                            // Regular paragraph
                            articlePieces.add(new Text(element.html(), Text.PARAGRAPH));
                        }
                    } else if ("q".equals(tagName)) {
                        // Big red italic heading
                        articlePieces.add(new Whitespace());
                        articlePieces.add(new Text(element.html(), Text.THEVERGE_HEADER));
                        articlePieces.add(new Whitespace());
                    } else if ("small".equals(tagName)) {
                        // Small text:
                        // www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                        articlePieces.add(new Text(element.html(), Text.SMALL));
                    } else if ("h2".equals(tagName)) {
                        // www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                        articlePieces.add(new Whitespace());
                        articlePieces.add(new Text(element.html(), Text.H2HEADER));
                        articlePieces.add(new Whitespace());
                    } else if ("h3".equals(tagName)) {
                        // www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                        articlePieces.add(new Whitespace());
                        articlePieces.add(new Text(element.html(), Text.H3HEADER));
                        articlePieces.add(new Whitespace());
                    } else if ("figure".equals(tagName)) {
                        // www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                        try {
                            String imgURL = element.getElementsByAttribute("data-original").first().attr("data-original");
                            articlePieces.add(new Image(imgURL));
                        } catch (NullPointerException ignored) {/*ignored*/}
                    } else if ("img".equals(tagName) || "aside".equals(tagName)) {
                        addImage(element);
                    }
                    // TODO handle <ul>: www.theverge.com/2015/7/13/8949959/nasa-new-horizons-pluto-flyby-date-time-livestream
                }
            }

            private Element childWithTag(Element element, String tag) {
                for (Element element1 : element.children()) {
                    if (element1.tagName().equals(tag))
                        return element1;
                }
                return null;
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
                // TODO: 10-9-2015 Better method for extracting main article image
            }

            @Override
            protected void onPostExecute(Void v) {
                if (title == null) titleTV.setVisibility(View.GONE);
                else {
                    titleTV.setText(title);
                    collToolbar.setTitle(title);
                }
                if (sub == null) subTV.setVisibility(View.GONE);
                else subTV.setText(sub);
                authorAndDate();
                sourceAndTags();

                if (imgSrc != null) {
                    Picasso.with(ArticleActivity.this).load(imgSrc).into(mainImg);
                    /*// Set 16:9 ratio on imageView
                    mainImg.getLayoutParams().height = mainImg.getWidth() / 16 * 9;*/
                    //mainImg.requestLayout();
                }

                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                // Convert 16dp and 5dp padding to px
                final int px16dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
                final int px5dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                final int px8dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
                //textViewParams.setMargins(px, 0, px, 0);
                for (int i = 0; i < articlePieces.size(); i++) {
                    ArticlePiece piece = articlePieces.get(i);
                    if (piece instanceof Text) {
                        TextView tv = new TextView(ArticleActivity.this);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());
                        tv.setText(Html.fromHtml(((Text) piece).text));
                        tv.setPadding(px16dp, 0, px16dp, px8dp);

                        switch (((Text) piece).type) {
                            case Text.PARAGRAPH:
                                tv.setTextColor(Color.BLACK);
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
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
                                break;
                            case Text.SMALL:
                                tv.setPadding(px8dp, 0, px8dp, px8dp);
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
                // No author, no date
                if (authorName == null && date == null) {
                    authorTV.setVisibility(View.GONE);
                }

                // Author only
                else if (authorName != null && date == null) {
                    SpannableString sString = new SpannableString(authorName);

                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            AuthorActivity.openActivity(ArticleActivity.this, authorTheVergeLink);
                        }
                    };
                    sString.setSpan(clickableSpan, 3, authorName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                    authorTV.setText(sString);
                    authorTV.setMovementMethod(LinkMovementMethod.getInstance());
                    authorTV.setHighlightColor(Color.TRANSPARENT);
                }

                // Date only
                else if (authorName == null) {
                    // Remove "on "
                    if (date.startsWith("on ")) {
                        date = date.substring(3);
                    }
                    authorTV.setText(date);
                }

                // Author and date
                else {
                    SpannableString sString = new SpannableString(authorName + " " + date);

                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            AuthorActivity.openActivity(ArticleActivity.this, authorTheVergeLink);
                        }

                        /*@Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            // Change spanned text style here
                        }*/
                    };
                    sString.setSpan(clickableSpan, 3, authorName.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                    authorTV.setText(sString);
                    authorTV.setMovementMethod(LinkMovementMethod.getInstance());
                    authorTV.setHighlightColor(Color.TRANSPARENT);
                }
            }

            private void sourceAndTags() {
                TextView articleSourceTitle = (TextView) findViewById(R.id.article_source_title),
                        articleSourceContent = (TextView) findViewById(R.id.article_source_content),
                        relatedItemsTitle = (TextView) findViewById(R.id.related_items_title),
                        relatedItemsContent = (TextView) findViewById(R.id.related_items_content);

                if (source == null) {
                    articleSourceTitle.setVisibility(View.GONE);
                    articleSourceContent.setVisibility(View.GONE);
                } else {
                    articleSourceTitle.setVisibility(View.VISIBLE);
                    articleSourceContent.setVisibility(View.VISIBLE);
                    articleSourceContent.setText(Html.fromHtml(source));
                    articleSourceContent.setMovementMethod(LinkMovementMethod.getInstance());
                }

                if (relatedItems == null) {
                    relatedItemsContent.setVisibility(View.GONE);
                    relatedItemsTitle.setVisibility(View.GONE);
                } else {
                    relatedItemsContent.setVisibility(View.VISIBLE);
                    relatedItemsTitle.setVisibility(View.VISIBLE);
                    relatedItemsContent.setText(Html.fromHtml(relatedItems));
                    relatedItemsContent.setMovementMethod(LinkMovementMethod.getInstance());
                }

                View divider = findViewById(R.id.article_source_divider);
                if (relatedItems == null && source == null) {
                    divider.setVisibility(View.GONE);
                } else {
                    divider.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT >= 11) {
            getMenuInflater().inflate(R.menu.menu_article, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.copy_url:
                if (Build.VERSION.SDK_INT >= 11) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("The Verge URL", articleURL));
                }
        }
        return false;
    }
}
