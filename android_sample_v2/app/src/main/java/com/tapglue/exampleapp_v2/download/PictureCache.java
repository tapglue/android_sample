package com.tapglue.exampleapp_v2.download;


import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

public class PictureCache {

    private PictureCache() {

    }

    private static Map<String, Bitmap> cache = new HashMap<>();

    public static boolean isInCache(String url) {
        return cache.containsKey(url);
    }

    public static boolean putInCache(String url, Bitmap image){
        if (!isInCache(url)){
            cache.put(url, image);
            return true;
        } else {
            return false;
        }
    }

    public static Bitmap getFromCache (String url) {
        return cache.get(url);
    }
}
