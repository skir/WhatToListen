package com.afsj.whattolisten.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.afsj.whattolisten.R;
import com.afsj.whattolisten.data.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Vector;

/**
 * Created by ilia on 22.04.15.
 */
//Just from Sunshine template for fetching top tags
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
    private static final String API_KEY = "77ce255e53b059752de28ac12846e2f6";
    public static final String TOP_TAGS = "top_tags";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult){
        Log.e("SYNC_ADAPTER","performing sync");
        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("method", "tag.getTopTags")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json");

        Uri builtUri = builder.build();

        HttpURLConnection urlConnection;
        String json;
        try {
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null)
                return ;

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                line += "\n";
                buffer.append(line);
            }

            if (buffer.length() == 0)
                return ;

            json = buffer.toString();
            saveTopTags(json);
        } catch (java.net.SocketTimeoutException e){
            Log.e("SocketTimeoutException",e.toString());
        }catch (MalformedURLException e){
            Log.e("MalformedURLException", e.toString());
        }catch (ProtocolException e) {
            Log.e("ProtocolException", e.toString());
        }catch (IOException e) {
            Log.e("IOException", e.toString());
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (accountManager.getPassword(newAccount) == null ) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        SyncAdapter.configurePeriodicSync(context, 60*60*24, 60*60*8);

        ContentResolver.setSyncAutomatically(newAccount, Contract.CONTENT_AUTHORITY, true);

        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void saveTopTags(String json){
        try{
            JSONObject data = new JSONObject(json);
            JSONArray tags = data.getJSONObject("toptags").getJSONArray("tag");
            Vector<ContentValues> valuesVector = new Vector<>(tags.length());

            for(int i = 0; i < tags.length(); i++){
                JSONObject tag = tags.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(Contract.ResultsEntry.NAME,tag.getString("name"));
                values.put(Contract.ResultsEntry.URL,tag.getString("url"));
                values.put(Contract.ResultsEntry.SEARCH_QUERY,TOP_TAGS);

                valuesVector.add(values);
            }

            if(valuesVector.size() > 0){
                ContentValues[] contentValues = new ContentValues[valuesVector.size()];
                valuesVector.toArray(contentValues);

                getContext().getContentResolver().delete(Contract.ResultsEntry.CONTENT_URI,
                        Contract.ResultsEntry.SEARCH_QUERY + " = ?",
                        new String[]{SyncAdapter.TOP_TAGS});
                getContext().getContentResolver().bulkInsert(Contract.ResultsEntry.CONTENT_URI,contentValues);
            }
        }catch (JSONException e) {
            Log.e("SYNC ADAPTER", e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
