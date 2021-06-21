package com.goverse.provider;

import android.content.UriMatcher;
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrisMatcher extends UriMatcher {

    private Map<Integer, List<Uri>> uriMatchMap = new HashMap<>();

    /**
     * Creates the root node of the URI tree.
     *
     * @param code the code to match for the root URI
     */
    public UrisMatcher(int code) {
        super(code);
    }

    @Override
    public void addURI(String authority, String path, int code) {
        super.addURI(authority, path, code);

        Uri uri = Uri.parse("content://" + authority + "/" + path);
        List<Uri> uriList = uriMatchMap.get(code);
        if (uriList == null) {
            uriList = new ArrayList<>();
        }
        if (!uriList.contains(uri)) {
            uriList.add(uri);
        }
        uriMatchMap.put(code, uriList);
    }


    public List<Uri> getUriListByCode(int code) {
        return uriMatchMap.get(code);
    }
}
