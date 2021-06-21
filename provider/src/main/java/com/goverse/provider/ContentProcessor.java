package com.goverse.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.goverse.provider.permission.PermissionChecker;
import com.goverse.provider.proxy.ProviderProxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *  ContentProcessor is a uri dispatcher for provider,
 *  which is abled to dispatch different uri to kinds of adapters
 *  based on {@link ContentAdapter} .You can have different way
 *  to supply persistence data,such as database, sharedPreference.
 */
public class ContentProcessor {

    private UrisMatcher mUriMatcher;

    private ContentProvider mContentProvider;

    private Map<Integer, ContentAdapter> mContentAdapterMap;

    private final String TAG  = ContentProcessor.class.getSimpleName();

    private PermissionChecker mPermissionChecker;

    public ContentProcessor(ContentProvider contentProvider, UrisMatcher uriMatcher) {
        mContentProvider = contentProvider;
        mUriMatcher = uriMatcher;
        mPermissionChecker = new PermissionChecker(mContentProvider.getContext());
    }

    public void add(int matchCode, ContentAdapter contentAdapter) {
        if (mContentAdapterMap == null) {
            mContentAdapterMap = new HashMap<>();
        }

        mContentAdapterMap.put(matchCode, contentAdapter);
    }

    private ContentAdapter getContentAdapter(Uri uri) {
        Log.d(TAG, "getContentAdapter---uri: " + uri);
        if (mContentAdapterMap == null) return null;
        int code = mUriMatcher.match(uri);
        boolean containsKey = mContentAdapterMap.containsKey(code);
        if (containsKey) {
            return mContentAdapterMap.get(code);
        }
        return null;
    }

    private String getCallingPackage() {
        String callingPackage = null;
        try {
            callingPackage = mContentProvider.getCallingPackage();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return callingPackage;
    }

    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) throws SecurityException{
        Log.d(TAG, "query---uri: " + uri);
        ContentAdapter contentAdapter = getContentAdapter(uri);
        Log.d(TAG, "contentAdapter is null: " + (contentAdapter == null));
        if (contentAdapter != null && mPermissionChecker.checkQuery(getCallingPackage(), contentAdapter.readScope())) {
            ProviderProxy.getInstance().notifyOnRead(uri);
            return contentAdapter.query(projection, selection, selectionArgs, sortOrder);
        }
        return null;
    }

    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.d(TAG, "insert---uri: " + uri);
        ContentAdapter contentAdapter = getContentAdapter(uri);
        if (contentAdapter != null && mPermissionChecker.checkInsert(getCallingPackage(), contentAdapter.writeScope())) {
            ProviderProxy.getInstance().notifyOnWrite(uri);
            if (contentAdapter.insert(values)) {
                notifyContentChange(uri);
                return uri;
            }
        }
        return null;
    }

    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "update---uri: " + uri);
        ContentAdapter contentAdapter = getContentAdapter(uri);
        if (contentAdapter != null && mPermissionChecker.checkUpdate(getCallingPackage(), contentAdapter.writeScope())) {
            ProviderProxy.getInstance().notifyOnWrite(uri);
            int updateCount = contentAdapter.update(values, selection, selectionArgs);
            if (updateCount > 0) {
                notifyContentChange(uri);
                return updateCount;
            }
        }
        return 0;
    }

    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "delete---uri: " + uri);
        ContentAdapter contentAdapter = getContentAdapter(uri);
        if (contentAdapter != null && mPermissionChecker.checkDelete(getCallingPackage(), contentAdapter.writeScope())) {
            ProviderProxy.getInstance().notifyOnWrite(uri);
            int deleteCount = contentAdapter.delete(selection, selectionArgs);
            if (deleteCount > 0) {
                notifyContentChange(uri);
                return deleteCount;
            }
        }
        return 0;
    }

    private void notifyContentChange(Uri uri) {
        int code = mUriMatcher.match(uri);
        List<Uri> uriList = mUriMatcher.getUriListByCode(code);
        if (uriList != null) {
            for (Uri u : uriList) {
                mContentProvider.getContext().getContentResolver().notifyChange(u, null);
            }
        }
    }
}
