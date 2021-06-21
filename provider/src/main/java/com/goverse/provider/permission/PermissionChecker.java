package com.goverse.provider.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.util.Preconditions;
import com.goverse.provider.auth.AuthScope;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PermissionChecker {

    private Context mContext;

    private final String TAG = PermissionChecker.class.getSimpleName();

    public PermissionChecker(Context context) {
        mContext = context;
    }

    /**
     * Flag indicating that caller is abled to query data in contentProvider.
     */
    static final byte FLAG_CONTENT_QUERY = 0x01;

    /**
     * Flag indicating that caller is abled to insert data in contentProvider.
     */
    static final byte FLAG_CONTENT_INSERT = 0x02;

    /**
     * Flag indicating that caller is abled to update data in contentProvider.
     */
    static final byte FLAG_CONTENT_UPDATE = 0x04;

    /**
     * Flag indicating that caller is abled to delete data in contentProvider.
     */
    static final byte FLAG_CONTENT_DELETE = 0x08;

    /**
     * get Caller's SHA1
     * @param callerPackageName callerPackageName
     * @return Caller's SHA1
     */
    private String getCallerSHA1ByPackageName(String callerPackageName) {
        Log.d(TAG, "getCallerSHA1ByPackageName: " + callerPackageName);
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(
                    callerPackageName, PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("RestrictedApi")
    private boolean checkCallerSha1Valid(String callerPackageName, List<String> sha1) {
        Log.d(TAG, "checkCallerSha1Valid---callerPackageName: " + callerPackageName + ",sha1: " + sha1);
        callerPackageName = Preconditions.checkNotNull(callerPackageName);
        sha1 = Preconditions.checkNotNull(sha1);
        String callerSha1 = getCallerSHA1ByPackageName(callerPackageName);
        boolean res = sha1.contains(callerSha1);
        Log.d(TAG, callerPackageName + " sha1 is valid: " + res);
        return res;
    }

    private boolean checkScopeGrantedByUser(String callerPackageName, String scope) {

        Set<String> callerGrantScope = AuthScope.getCallerGrantScope(callerPackageName);
        if (callerGrantScope == null || !callerGrantScope.contains(scope)) return false;
        return true;
    }

    private boolean check(byte flag, String callerPackageName, String scope) {
        Log.d(TAG, "check---flag: " + flag + ",callerPackageName: " + callerPackageName);
        String appPackageName = mContext.getApplicationInfo().packageName;

        // allow to access all scopes for self.
        if (appPackageName.equalsIgnoreCase(callerPackageName)) return true;
        if (TextUtils.isEmpty(callerPackageName) || TextUtils.isEmpty(scope)) return false;

        try {
            // permission denied if scope not settled
            if (TextUtils.isEmpty(scope)) {
                Log.d(TAG, "scope is null ...");
                return false;
            }

            // check validation in caller white list for packageName, sha1 and can access to the scope.
            Map<String, AuthScope.Configuration> callerWhiteListConfigMap = AuthScope.getCallerWhiteListConfigMap();

            if (callerWhiteListConfigMap != null) {
                AuthScope.Configuration configuration = callerWhiteListConfigMap.get(callerPackageName);
                if (configuration != null) {
                    Log.d(TAG, callerPackageName + " is in white list ...");
                    List<String> sha1 = configuration.sha1;
                    List<String> scopes = configuration.scopes;
                    if (checkCallerSha1Valid(callerPackageName, sha1)) {
                        if (scopes != null) {
                            if (scopes.contains(scope)) {
                                return true;
                            }
                        }
                    }
                }
            }
            Log.d(TAG, callerPackageName + " is not in white list ...");
            // check validation in caller list, first to check caller's validation, then check if it has been granted permission and can access to this data.
            Map<String, AuthScope.Configuration> callerListConfigMap = AuthScope.getCallerListConfigMap();

            if (callerListConfigMap != null) {
                AuthScope.Configuration configuration = callerListConfigMap.get(callerPackageName);
                if (configuration == null) {
                    Log.d(TAG, callerPackageName + " is not in caller list ...");
                    return false;
                }
                List<String> sha1 = configuration.sha1;
                List<String> scopes = configuration.scopes;

                if (!checkCallerSha1Valid(callerPackageName, sha1) || !checkScopeGrantedByUser(callerPackageName, scope)) return false;
                if (scopes != null && scopes.contains(scope)) return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * check Query permission
     * @param callerPackageName callerPackageName
     * @return isValid
     */
    public boolean checkQuery(String callerPackageName, String scope) {

        return check(FLAG_CONTENT_QUERY, callerPackageName, scope);
    }

    /**
     * check Insert permission
     * @param callerPackageName callerPackageName
     * @param scopes scopes
     * @return isValid
     */
    public boolean checkInsert(String callerPackageName, String scopes) {

        return check(FLAG_CONTENT_INSERT, callerPackageName, scopes);
    }

    /**
     * check Update permission
     * @param callerPackageName callerPackageName
     * @param scopes scopes
     * @return isValid
     */
    public boolean checkUpdate(String callerPackageName, String scopes) {

        return check(FLAG_CONTENT_UPDATE, callerPackageName, scopes);
    }

    /**
     * check Delete permission
     * @param callerPackageName callerPackageName
     * @param scopes scopes
     * @return isValid
     */
    public boolean checkDelete(String callerPackageName, String scopes) {

        return check(FLAG_CONTENT_DELETE, callerPackageName, scopes);
    }
}
