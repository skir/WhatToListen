package com.afsj.whattolisten.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by ilia on 22.04.15.
 */
public class AuthenticatorService extends Service {
    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
