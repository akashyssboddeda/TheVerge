package com.teinproductions.tein.theverge;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;


public abstract class BasicLoader<D> extends AsyncTaskLoader<D> {
    public BasicLoader(Context context) {
        super(context);
    }

    private D mData;

    @Override
    public void deliverResult(D data) {
        if (isReset()) return;

        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        // Ensure that the Loader has been stopped
        onStopLoading();

        mData = null;
    }
}
