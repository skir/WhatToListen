package com.afsj.whattolisten.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by ilia on 22.04.15.
 */
public class SyncService  extends Service {
    private static final Object mSyncAdapterLock = new Object();
    private static SyncAdapter mSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (mSyncAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
