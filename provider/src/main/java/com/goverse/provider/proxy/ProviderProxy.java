package com.goverse.provider.proxy;

import android.content.ContentProvider;
import android.content.pm.PathPermission;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

/**
 * A proxy of contentProcvider used to notify provider's initialization
 * and calling request by multiple applications.
 */
public class ProviderProxy {

    /**
     * Interface definition for a callback to be invoked when initialize ContentProvider.
     */
    public interface OnLifeCycleListener {
        /**
         * Instantiating provider invoked before onCreate().
         * @param info providerInfo
         */
        void onAttachInfo(ProviderInfo info);

        /**
         * Called when the provider is starting.
         */
        void onCreate();
    }

    /**
     * Interface definition for a callback to be invoked when multiple applications call
     * ContentProvider's CURD method.
     */
    public interface OnCallingListener {
        /**
         * Indicating that the query request is calling,
         * @param uri uri
         */
        void onRead(Uri uri);
        /**
         * Indicating that the write request is calling, maybe insert, uodate or delete.
         * @param uri uri
         */
        void onWrite(Uri uri);
    }

    private ProviderProxy() {}

    private List<OnCallingListener> mOnCallingListeners = new ArrayList<>();

    private List<OnLifeCycleListener> mOnLifeCycleListeners = new ArrayList<>();

    private ContentProvider mContentProvider;

    public static ProviderProxy getInstance() {
        return Singleton.mInstance;
    }

    private static class Singleton {
        private static ProviderProxy mInstance = new ProviderProxy();
    }

    public void setContentProvider(ContentProvider contentProvider) {
        mContentProvider = contentProvider;
    }

    /**
     * add OnCallingListener
     * @param onCallingListener onCallingListener
     */
    public void addOnCallingListener(OnCallingListener onCallingListener) {
        if (!mOnCallingListeners.contains(onCallingListener)) {
            mOnCallingListeners.add(onCallingListener);
        }
    }

    /**
     * remove OnCallingListener
     * @param onCallingListener onCallingListener
     */
    public void removeOnCallingListener(OnCallingListener onCallingListener) {
        if (mOnCallingListeners.contains(onCallingListener)) {
            mOnCallingListeners.remove(onCallingListener);
        }
    }

    /**
     * add OnLifeCycleListener
     * Considering the sequence of application's initialization, you should set listener in attachBaseContext(Context).
     * initialize sequence:
     * attachBaseContext in application->attachInfo in provider->onCreate in provider->onCreate in application.
     * @param onLifeCycleListener onLifeCycleListener
     */
    public void addOnLifeCycleListener(OnLifeCycleListener onLifeCycleListener) {
        if (!mOnLifeCycleListeners.contains(onLifeCycleListener)) {
            mOnLifeCycleListeners.add(onLifeCycleListener);
        }
    }

    /**
     * remove OnLifeCycleListener
     * @param onLifeCycleListener onLifeCycleListener
     */
    public void removeOnLifeCycleListener(OnLifeCycleListener onLifeCycleListener) {
        if (mOnLifeCycleListeners.contains(onLifeCycleListener)) {
            mOnLifeCycleListeners.remove(onLifeCycleListener);
        }
    }

    /**
     * Return the package name of the caller that initiated the request being
     * processed on the current thread.{@link ContentProvider#getCallingPackage()}
     * @return package name
     */
    public String getCallingPackage() {
        return mContentProvider.getCallingPackage();
    }

    /**
     * Return the name of the permission required for read-only access to
     * this content provider.{@link ContentProvider#getReadPermission()}
     */
    public final String getReadPermission() {
        return mContentProvider.getReadPermission();
    }

    /**
     * Return the name of the permission required for read/write access to
     * this content provider.{@link ContentProvider#getWritePermission()}
     */
    public final String getWritePermission() {
        return mContentProvider.getWritePermission();
    }

    /**
     * Return the path-based permissions required for read and/or write access to
     * this content provider.{@link ContentProvider#getPathPermissions()}
     */
    public final PathPermission[] getPathPermissions() {
        return mContentProvider.getPathPermissions();

    }

    /**
     * notifyOnAttachInfo
     * @param info providerInfo
     */
    public void notifyOnAttachInfo(ProviderInfo info) {
        for (OnLifeCycleListener onLifeCycleListener : mOnLifeCycleListeners) {
            onLifeCycleListener.onAttachInfo(info);
        }
    }

    /**
     * notifyOnCreate
     */
    public void notifyOnCreate() {
        for (OnLifeCycleListener onLifeCycleListener : mOnLifeCycleListeners) {
            onLifeCycleListener.onCreate();
        }
    }

    /**
     * notifyOnRead
     * @param uri uri
     */
    public void notifyOnRead(Uri uri) {
        for (OnCallingListener onCallingListener : mOnCallingListeners) {
            onCallingListener.onRead(uri);
        }
    }

    /**
     * notifyOnWrite
     * @param uri uri
     */
    public void notifyOnWrite(Uri uri) {
        for (OnCallingListener onCallingListener : mOnCallingListeners) {
            onCallingListener.onWrite(uri);
        }
    }

}
