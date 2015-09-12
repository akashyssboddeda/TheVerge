package com.teinproductions.tein.theverge.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.teinproductions.tein.theverge.R;

import org.jsoup.nodes.Element;


public class ProductViewHolder extends ArticleItemViewHolder {
    TextView productName, productBrand;
    ImageView imageView;

    public ProductViewHolder(View itemView) {
        super(itemView);

        productName = (TextView) itemView.findViewById(R.id.productName);
        productBrand = (TextView) itemView.findViewById(R.id.productBrand);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
    }

    @Override
    public void bind(Element element) {
        parseProductName(element);
        parseProductBrand(element);
        parseImage(element);
    }

    private void parseProductName(Element element) {
        String name = null;
        try {
            name = element.getElementsByTag("h4").first().text();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (name == null) productName.setVisibility(View.GONE);
        else {
            productName.setVisibility(View.VISIBLE);
            productName.setText(name);
        }
    }

    private void parseProductBrand(Element element) {
        String brand = null;
        try {
            brand = element.getElementsByTag("p").first().text();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (brand == null) productBrand.setVisibility(View.GONE);
        else {
            productBrand.setVisibility(View.VISIBLE);
            productBrand.setText(brand);
        }
    }

    private void parseImage(Element element) {
        String imageURL = null;
        try {
            imageURL = element.getElementsByClass("m-products-index__grid-item-image").first().attr("data-original");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imageURL == null) imageView.setVisibility(View.GONE);
        else {
            imageView.setVisibility(View.VISIBLE);
            Picasso.with(itemView.getContext()).load(imageURL).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    setRatio();
                }

                public void onError() {/*ignored*/}
            });
        }
    }

    private void setRatio() {
        // Set 16:9 ratio on imageView, according to the Material Design specs
        imageView.getLayoutParams().height = imageView.getWidth() / 16 * 9;
        imageView.requestLayout();
    }
}

