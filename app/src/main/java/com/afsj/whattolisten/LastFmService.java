package com.afsj.whattolisten;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

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

public class LastFmService extends IntentService {

    private final String LOG_TAG = "lastFMService";

    public static final String SEARCH = "search";
    public static final String QUERY = "query";

    private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/?";

    private static final String API_KEY = "77ce255e53b059752de28ac12846e2f6";
    private static final String API_SECRET = "2b3e2099eeb8608316f1ed8e78f311ba";

    public LastFmService() {
        super("LastFmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch (action){
                case SEARCH:
                    if(intent.hasExtra(QUERY))
                        search(intent.getStringExtra(QUERY));
            }
        }
    }

    private void search(String query){
        ContentValues values = new ContentValues();
        values.put(Contract.HistoryEntry.QUERY, query);
        getContentResolver().insert(Contract.HistoryEntry.CONTENT_URI, values);

        HttpURLConnection urlConnection = null;
        String json = "";
        try {
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("method", "tag.search")
                    .appendQueryParameter("tag", query)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            json = buffer.toString();
            Log.e("SERVICE result",json);
            saveData(json);
        }catch (MalformedURLException e){
            Log.e("SERVICE",e.toString());
        }catch (ProtocolException e) {
            Log.e("SERVICE", e.toString());
        }catch (IOException e) {
            Log.e("SERVICE", e.toString());
        }
    }

    private void saveData(String json){
        try{
            JSONObject data = new JSONObject(json);
            JSONArray tags = data.getJSONObject("results").getJSONObject("tagmatches").getJSONArray("tag");
            Vector<ContentValues> valuesVector = new Vector<>(tags.length());

            for(int i = 0; i < tags.length(); i++){
                JSONObject tag = tags.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(Contract.ResultsEntry.NAME,tag.getString("name"));
                values.put(Contract.ResultsEntry.URL,tag.getString("url"));
                values.put(Contract.ResultsEntry.TYPE,"tag");

                valuesVector.add(values);
            }

            if(valuesVector.size() > 0){
                ContentValues[] contentValues = new ContentValues[valuesVector.size()];
                valuesVector.toArray(contentValues);

                getContentResolver().delete(Contract.ResultsEntry.CONTENT_URI, null, null);
                getContentResolver().bulkInsert(Contract.ResultsEntry.CONTENT_URI,contentValues);
            }
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
