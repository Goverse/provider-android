package com.goverse.provider.adapter;

import android.content.ContentProvider;

import com.goverse.provider.ContentAdapter;

/**
 *  Base database Adapter object which is able
 *  to provide data by database.
 */
@SuppressWarnings("JavadocReference")
public abstract class DBContentAdapter extends ContentAdapter {

    public DBContentAdapter(ContentProvider contentProvider) {
        super(contentProvider);
    }
}
