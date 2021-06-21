package com.goverse.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.goverse.provider.adapter.open.OpenDataAdapter;
import com.goverse.provider.proxy.ProviderProxy;

public class OpenProvider extends ContentProvider {

    private String TAG = OpenProvider.class.getSimpleName();

    /**
     * authority
     */
    public static final String AUTHORITY = "com.goverse.provider.openprovider";

    /**
     * ContentProcessor
     */
    private ContentProcessor mContentProcessor;

    /**
     * URL_MATCHER
     */
    private static final UrisMatcher URL_MATCHER =
            new UrisMatcher(UriMatcher.NO_MATCH);


    private static final int MATCH_OPEN =  1;

    static {
        URL_MATCHER.addURI(AUTHORITY, "open/openData", MATCH_OPEN);
    }


    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        ProviderProxy.getInstance().setContentProvider(this);
        ProviderProxy.getInstance().notifyOnAttachInfo(info);
        mContentProcessor.add(MATCH_OPEN, new OpenDataAdapter(this));
        super.attachInfo(context, info);
    }

    @Override
    public boolean onCreate() {
        ProviderProxy.getInstance().notifyOnCreate();
        mContentProcessor = new ContentProcessor(this, URL_MATCHER);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        Log.d(TAG, "query---uri: " + uri + ", ");
        return mContentProcessor.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        Log.d(TAG, "insert---uri: " + uri + ", values: " + values);
        return mContentProcessor.insert(uri, values);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        Log.d(TAG, "update---uri: " + uri + ", values: " + values);
        return mContentProcessor.update(uri, values, selection, selectionArgs);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        Log.d(TAG, "delete---uri: " + uri);
        return mContentProcessor.delete(uri, selection, selectionArgs);
    }

}
