package com.netmania.checklod.general.data;

import android.content.Context;

import com.squareup.picasso.LruCache;

/**
 * Created by kevinhan on 2016. 6. 22..
 */
public class CustomLruCache extends LruCache {

    public CustomLruCache(Context context) {
        super(context);
    }

    public CustomLruCache(int maxSize) {
        super(maxSize);
    }
}
