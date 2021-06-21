package com.goverse.provider.auth;
import android.util.Log;

import com.goverse.provider.adapter.open.OpenDataAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthScope {

    public static class Configuration {
        public String packageName;
        public List<String> sha1;
        public List<String> scopes;

        public Configuration(String packageName, String[] sha1, String[] scopes) {
            this.packageName = packageName;
            this.sha1 = Arrays.asList(sha1);
            if (scopes != null) {
                this.scopes = new ArrayList<>(Arrays.asList(scopes));
            }
        }
    }

    private static Map<String, Configuration> callerListConfigMap = new HashMap<>();
    private static Map<String, Configuration> callerWhiteListConfigMap = new HashMap<>();

    static {

        callerListConfigMap.put("com.eg.android.AlipayGphone", new Configuration("com.eg.android.AlipayGphone", new String[] {"84:0F:34:3A:0E:FC:32:5B:A0:BF:75:DA:C8:35:E4:D5:87:03:34:35"}, new String[]{OpenDataAdapter.READ_SCOPE})); // 支付宝

    }

    /**
     * save granted scopes
     * @param callerPackageName callerPackageName
     * @param scopeSet scopeSet
     */
    public static void saveCallerGrantScope(String callerPackageName, Set<String> scopeSet) {
        Log.d("ContentScope", "saveCallerGrantScope: " + callerPackageName);
        // TODO: 2021/6/21  saveCallerGrantScope
    }

    /**
     * get all scopes has granted
     * @param callerPackageName callerPackageName
     * @return all scopes
     */
    public static Set<String> getCallerGrantScope(String callerPackageName) {
        Log.d("ContentScope", "getCallerGrantScope: " + callerPackageName);
        // TODO: 2021/6/21  getCallerGrantScope
        return null;
    }

    public static Map<String, Configuration> getCallerListConfigMap() {
        return callerListConfigMap;
    }

    public static Map<String, Configuration> getCallerWhiteListConfigMap() {
        return callerWhiteListConfigMap;
    }

}
