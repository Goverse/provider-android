package com.goverse.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.Nullable;

/**
 * Base Adapter object of Provider defines how to provide data source,
 * and it's abled to finish CURD operation after matchering the binding uri
 * dispatched by {@link com.goverse.provider.OpenProvider}.
 * There are two subclass:
 * {@link SPContentAdapter}，data source provide by sharedPreference.
 * Maybe you can implement BaseFileContentAdaper，BaseMemoryContentAdaper.
 */
@SuppressWarnings("JavadocReference")
public abstract class ContentAdapter {

    /**
     * ContentProvider
     */
    private ContentProvider mContentProvider;

    protected final String TAG = this.getClass().getSimpleName();

    public ContentAdapter(ContentProvider contentProvider) {
        mContentProvider = contentProvider;
    }

    public ContentProvider getContentProvider() {
        return mContentProvider;
    }

    /**
     * query {@link ContentProvider#query(Uri, String[], String, String[], String)}
     * @param projection projection
     * @param selection selection
     * @param selectionArgs selectionArgs
     * @param sortOrde sortOrde
     * @return Cursor
     */
    public abstract Cursor query( @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrde);

    /**
     * insert {@link ContentProvider#insert(Uri, ContentValues)}}
     * @param values values
     * @return res
     */
    public abstract boolean insert(@Nullable ContentValues values);

    /**
     * update {@link ContentProvider#update(Uri, ContentValues, String, String[])}
     * @param values values
     * @param selection selection
     * @return update count
     */
    public abstract int update(@Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs);

    /**
     * delete {@link ContentProvider#delete(Uri, String, String[])}`
     * @param selection selection
     * @param selectionArgs selectionArgs
     * @return delete count
     */
    public abstract int delete(@Nullable String selection, @Nullable String[] selectionArgs);

    /**
     * Used to match the type of contentData to read or write.
     * Can not access if scope not settled.
     * 配置read, 例如：READ_SPORT_DATA, 可以获得query查询该数据的能力
     * 配置write, 例如：WRITE_SPORT_DATA，可以获得insert,update,delete写该数据的能力
     * 如果不配置，则该数据不被开放，配置后，配合{@link AuthScope}中三方申请的scopes，会在三方主动requestPermission时进行校验。
     * @return scopes of contentData for third-party app.
     */
    public String readScope() {
        return "";
    }

    public String writeScope() {
        return "";
    }

    /**
     * description for contentData
     * 用于授权页面中显示数据类型
     */
    public String desc() { return "";}
}
