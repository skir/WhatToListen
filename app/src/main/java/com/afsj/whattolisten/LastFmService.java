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
    public static final String INFO = "info";

    private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/?";

    private static final String API_KEY = "77ce255e53b059752de28ac12846e2f6";
    private static final String API_SECRET = "2b3e2099eeb8608316f1ed8e78f311ba";

    private static boolean fromHistory = false;

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
                        fromHistory = intent.getBooleanExtra(getString(R.string.from_history),false);
                        search(intent.getStringExtra(QUERY));
                    break;
                case INFO:
                    if(intent.hasExtra(QUERY))
                        getInfo(intent.getStringExtra(QUERY));
            }
        }
    }

    private void search(String query){
        if(!fromHistory){
            ContentValues values = new ContentValues();
            values.put(Contract.HistoryEntry.QUERY, query);
            getContentResolver().insert(Contract.HistoryEntry.CONTENT_URI, values);
        }

        saveData(get(query, "tag.search",null));
    }

    private void getInfo(String tagName){
        String info = get(tagName,"tag.getinfo",null);
        String artists = get(tagName,"tag.gettopartists","4");
        String albums = get(tagName,"tag.gettopalbums","4");
        saveInfo(info,albums,artists);
    }

    private String get(String tag, String method, String limit){
        HttpURLConnection urlConnection = null;
        String json = "";
        try {
            Uri.Builder builder = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("method", method)

                    .appendQueryParameter("tag", tag)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json");
            if(limit != null && limit != "")
                builder.appendQueryParameter("limit",limit);

            Uri builtUri = builder.build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
                return "";

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return"";

            json = buffer.toString();
            Log.e("SERVICE result", json);
        }catch (MalformedURLException e){
            Log.e("SERVICE",e.toString());
        }catch (ProtocolException e) {
            Log.e("SERVICE", e.toString());
        }catch (IOException e) {
            Log.e("SERVICE", e.toString());
        }
        return json;
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

    private void saveInfo(String jsonInfo,String jsonAlbums, String jsonArtist){
        try{
            String summary = (new JSONObject(jsonInfo)).getJSONObject("tag").getJSONObject("wiki").getString("summary");
            String albums = (new JSONObject(jsonAlbums)).getJSONObject("topalbums").getJSONArray("album").toString();
            String artists = (new JSONObject(jsonArtist)).getJSONObject("topartists").getJSONArray("artist").toString();

            ContentValues values = new ContentValues();
            values.put(Contract.InfoEntry.SUMMARY, summary);
            values.put(Contract.InfoEntry.ALBUMS,albums);
            values.put(Contract.InfoEntry.ARTISTS,artists);

            getContentResolver().insert(Contract.InfoEntry.CONTENT_URI, values);
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
