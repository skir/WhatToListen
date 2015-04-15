package com.afsj.whattolisten;

import android.app.Application;

import com.afsj.whattolisten.data.Contract;

/**
 * Created by ilia on 15.04.15.
 */
public class WhatToListen extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        getContentResolver().delete(Contract.ResultsEntry.CONTENT_URI, null, null);
        getContentResolver().delete(Contract.InfoEntry.CONTENT_URI, null, null);
        getContentResolver().delete(Contract.PlaylistEntry.CONTENT_URI, null, null);
    }
}
