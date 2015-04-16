package com.afsj.whattolisten;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class LastFmService extends IntentService {

    private final String LOG_TAG = "lastFMService";

    public static final String SEARCH = "search";
    public static final String QUERY = "query";
    public static final String INFO = "info";
    public static final String RADIO_TUNE = "radio_tune";

    private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
    private static final String RADIO_URL = "http://ext.last.fm/2.0/";

    private static final String API_KEY = "77ce255e53b059752de28ac12846e2f6";
    private static final String API_SECRET = "2b3e2099eeb8608316f1ed8e78f311ba";
    private static final String sk = "e3ce21a934937f14220bf16ff1386e3b";

    private static List<String> queryList;
    private String query;
    private Cursor history;

    public LastFmService() {
        super("LastFmService");
        queryList = new ArrayList<>();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(QUERY) && !queryList.contains(intent.getStringExtra(QUERY))) {
            query = intent.getStringExtra(QUERY);
            queryList.add(query);
            Log.e("Service",query);
            final String action = intent.getAction();
            switch (action){
                case SEARCH:
                        search(query);
                    break;
                case INFO:
                        getInfo(query);
                    break;
                case RADIO_TUNE:
                        getPlaylist(query);
            }
        }
    }

    private void search(String query){

        saveData(get(query, "tag.search", null));
    }

    private void getInfo(String tagName){
        String info = get(tagName, "tag.getinfo", null);
        String artists = get(tagName,"tag.gettopartists","5");
        String albums = get(tagName, "tag.gettopalbums", "5");
        saveInfo(info, albums, artists);
    }

    private void getPlaylist(String tag){
        String tune = tune(tag);

        try{
            JSONObject tuneJSON = new JSONObject(tune);
            if(tuneJSON.has("error"))
                return;
        }catch (JSONException e){
            Log.e("JSONException",e.toString());
        }

        String method = "prototype.getPlaylist";
        String md5Str = "api_key" + API_KEY + "method" + method + "sk" + sk + API_SECRET;
        String api_sig = md5(md5Str);
        HttpURLConnection urlConnection = null;
        String json = "";
        try {
            Uri.Builder builder = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("method", method)
                    .appendQueryParameter("sk",sk)
                    .appendQueryParameter("api_sig",api_sig)
                    .appendQueryParameter("format", "json");

            Uri builtUri = builder.build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null)
                return;

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return;

            json = buffer.toString();
            Log.e("SERVICE result", json);
            savePlaylist(json);
        }catch (MalformedURLException e){
            Log.e("SERVICE",e.toString());
        }catch (ProtocolException e) {
            Log.e("SERVICE", e.toString());
        }catch (IOException e) {
            Log.e("SERVICE", e.toString());
        }
    }

    private String tune(String tag){
        HttpURLConnection urlConnection = null;
        String method = "prototype.tune";
        String station = "lastfm://globaltags/" + tag.replaceAll(" ", "%20");
        String md5Str = "api_key" + API_KEY + "method" + method + "sk" + sk + "station" + station + API_SECRET;
        String api_sig = md5(md5Str);
        String json = "";
        try {
            Uri builtUri = Uri.parse(RADIO_URL).buildUpon()
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("method", method)
                    .appendQueryParameter("sk",sk)
                    .appendQueryParameter("station", station)
                    .appendQueryParameter("api_sig",api_sig)
                    .appendQueryParameter("format", "json")
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
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
                return "";

            json = buffer.toString();
            Log.e("SERVICE RADIO result", json);
        }catch (MalformedURLException e){
            Log.e("SERVICE",e.toString());
        }catch (ProtocolException e) {
            Log.e("SERVICE", e.toString());
        }catch (IOException e) {
            Log.e("SERVICE", e.toString());
        }
        return json;
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
                values.put(Contract.ResultsEntry.SEARCH_QUERY,query);

                valuesVector.add(values);
            }

            if(valuesVector.size() > 0){
                ContentValues[] contentValues = new ContentValues[valuesVector.size()];
                valuesVector.toArray(contentValues);

                getContentResolver().bulkInsert(Contract.ResultsEntry.CONTENT_URI,contentValues);
            }
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        saveQuery();
        queryList.remove(query);
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
            values.put(Contract.InfoEntry.TAG,query);

            getContentResolver().insert(Contract.InfoEntry.CONTENT_URI, values);
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        queryList.remove(query);
    }

    private void savePlaylist(String playlist){
        try{
            JSONObject data = new JSONObject(playlist);
            JSONArray trackList = data.getJSONObject("playlist").getJSONObject("trackList").getJSONArray("track");
            Vector<ContentValues> valuesVector = new Vector<>(trackList.length());

            for(int i = 0; i < trackList.length(); i++){
                JSONObject track = trackList.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(Contract.PlaylistEntry.TITLE, track.getString("title"));
                values.put(Contract.PlaylistEntry.ALBUM, track.getString("album"));
                values.put(Contract.PlaylistEntry.ARTIST, track.getString("creator"));
                //TODO get artist image
                values.put(Contract.PlaylistEntry.LOCATION,track.getString("location"));
                values.put(Contract.PlaylistEntry.TAG,query);

                valuesVector.add(values);
            }

            if(valuesVector.size() > 0){
                ContentValues[] contentValues = new ContentValues[valuesVector.size()];
                valuesVector.toArray(contentValues);

                getContentResolver().delete(Contract.PlaylistEntry.CONTENT_URI, null, null);
                getContentResolver().bulkInsert(Contract.PlaylistEntry.CONTENT_URI,contentValues);
            }
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        queryList.remove(query);
    }

    private boolean isHistoryContains(String query){
        history = getContentResolver().query(Contract.HistoryEntry.CONTENT_URI,
                new String[]{Contract.HistoryEntry.QUERY},
                Contract.HistoryEntry.QUERY + " = ? COLLATE NOCASE",
                new String[]{query},null);

        return history != null && history.getCount() > 0;
    }

    private void saveQuery(){
        if(!isHistoryContains(query)){
            ContentValues values = new ContentValues();
            values.put(Contract.HistoryEntry.QUERY, query);
            getContentResolver().insert(Contract.HistoryEntry.CONTENT_URI, values);
        }
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
