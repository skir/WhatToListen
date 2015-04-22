package com.afsj.whattolisten;

import android.app.Application;

import com.afsj.whattolisten.data.Contract;
import com.afsj.whattolisten.sync.SyncAdapter;

/**
 * Created by ilia on 15.04.15.
 */
public class WhatToListen extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        getContentResolver().delete(Contract.ResultsEntry.CONTENT_URI,
                Contract.ResultsEntry.SEARCH_QUERY + " != ?",
                new String[]{SyncAdapter.TOP_TAGS});
        getContentResolver().delete(Contract.InfoEntry.CONTENT_URI, null, null);
        getContentResolver().delete(Contract.PlaylistEntry.CONTENT_URI, null, null);
        getContentResolver().delete(Contract.ArtistEntry.CONTENT_URI, null, null);
        getContentResolver().delete(Contract.AlbumEntry.CONTENT_URI, null, null);
    }
}
