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
//    public static final String TOP_TAGS = "top_tags";
    public static final String ALBUM = "album";
    public static final String ARTIST = "artist";

    private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
    private static final String RADIO_URL = "http://ext.last.fm/2.0/";

    private static final String API_KEY = "77ce255e53b059752de28ac12846e2f6";
    private static final String API_SECRET = "2b3e2099eeb8608316f1ed8e78f311ba";
    private static final String sk = "be8b675c8a0ff6ed17d61536de6051f4";//"e3ce21a934937f14220bf16ff1386e3b";

    private static final String GET = "GET";
    private static final String POST = "POST";

    private static List<String> queryList;
    private String query;
    private int playlistType = 0;
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
                    playlistType = intent.getIntExtra(Utils.TYPE,0);
                    getPlaylist(query);
                    break;
//                case TOP_TAGS:
//                    getTopTags();
//                    break;
                case ALBUM:
                    getAlbum(query);    //here query is mbid
                    break;
                case ARTIST:
                    getArtist(query);   //here query is mbid
                    break;
            }
        }
    }

    private String httpRequest(Uri builtUri,String method){
        HttpURLConnection urlConnection;
        String json = "";
        try {
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null)
                return "";

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                line += "\n";
                buffer.append(line);
            }

            if (buffer.length() == 0)
                return "";

            json = buffer.toString();
        }catch (MalformedURLException e){
            Log.e("SERVICE",e.toString());
        }catch (ProtocolException e) {
            Log.e("SERVICE", e.toString());
        }catch (IOException e) {
            Log.e("SERVICE", e.toString());
        }
        return json;
    }

    private void search(String query){

        saveData(get(query, "tag.search", null));
    }

    private void getInfo(String tagName){
        String info = get(tagName, "tag.getinfo", null);
        String artists = get(tagName, "tag.gettopartists", "7");
        String albums = get(tagName, "tag.gettopalbums", "7");
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
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("method", method)
                .appendQueryParameter("sk",sk)
                .appendQueryParameter("api_sig",api_sig)
                .appendQueryParameter("format", "json")
                .build();

        savePlaylist(httpRequest(builtUri, GET));
    }

    private String tune(String tag){
        String method = "prototype.tune";
        String station;
        if(playlistType == 0)
            station = "lastfm://globaltags/" + tag.replaceAll(" ", "%20");
        else
            station = "lastfm://artist/" + tag.replaceAll(" ", "%20") + "/similarartists";

        String md5Str = "api_key" + API_KEY + "method" + method + "sk" + sk + "station" + station + API_SECRET;
        String api_sig = md5(md5Str);

        Uri builtUri = Uri.parse(RADIO_URL).buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("method", method)
                .appendQueryParameter("sk",sk)
                .appendQueryParameter("station", station)
                .appendQueryParameter("api_sig",api_sig)
                .appendQueryParameter("format", "json")
                .build();

        return httpRequest(builtUri,POST);
    }

//    private void getTopTags(){
//        saveTopTags(get(null, "tag.getTopTags", null)); //limit is not working here
//    }

    private void getAlbum(String mbid){
        saveAlbum(getByMBID(mbid, "album.getinfo"));
    }

    private void getArtist(String mbid){
        saveArtist(getByMBID(mbid, "artist.getinfo"));
    }

    private String get(String tag, String method, String limit){

        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("method", method)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json");
        if(tag != null)
            builder.appendQueryParameter("tag", tag);
        if(limit != null && !limit.equals(""))
            builder.appendQueryParameter("limit",limit);

        Uri builtUri = builder.build();

        return httpRequest(builtUri, GET);
    }

    private String getByMBID(String mbid, String method){

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("method", method)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("mbid",mbid)
                .build();

        return httpRequest(builtUri,GET);
    }

    private String getByName(String name){

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("method", "artist.getinfo")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("artist",name)
                .build();

        return httpRequest(builtUri,GET);
    }

    private String getSimilarArtists(String mbid){

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("method", "artist.getsimilar")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("mbid",mbid)
                .appendQueryParameter("limit","5")
                .build();

        return httpRequest(builtUri,GET);
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
        String summary = "";
        String albums = "";
        String artists = "";

        //sorry, I'm forced to do that...
        try {
            summary = (new JSONObject(jsonInfo)).getJSONObject("tag").getJSONObject("wiki").getString("summary");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            albums = (new JSONObject(jsonAlbums)).getJSONObject("topalbums").getJSONArray("album").toString();
        }catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            artists = (new JSONObject(jsonArtist)).getJSONObject("topartists").getJSONArray("artist").toString();
        }catch (JSONException e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(Contract.InfoEntry.SUMMARY, summary);
        values.put(Contract.InfoEntry.ALBUMS,albums);
        values.put(Contract.InfoEntry.ARTISTS,artists);
        values.put(Contract.InfoEntry.TAG,query);

        getContentResolver().insert(Contract.InfoEntry.CONTENT_URI, values);
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
                values.put(Contract.PlaylistEntry.IMAGE, "");//(new JSONObject(getByName(track.getString("creator")))).getJSONObject("artist").getJSONArray("image").toString());
                values.put(Contract.PlaylistEntry.DURATION,track.getString("duration"));
                values.put(Contract.PlaylistEntry.LOCATION, track.getString("location"));
                values.put(Contract.PlaylistEntry.TAG,query);

                valuesVector.add(values);
            }

            if(valuesVector.size() > 0){
                ContentValues[] contentValues = new ContentValues[valuesVector.size()];
                valuesVector.toArray(contentValues);

//                getContentResolver().delete(Contract.PlaylistEntry.CONTENT_URI, null, null);
                getContentResolver().bulkInsert(Contract.PlaylistEntry.CONTENT_URI,contentValues);
            }
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        queryList.remove(query);
    }

    /**Moved to SyncAdapter**/
//    private void saveTopTags(String json){
//        try{
//            JSONObject data = new JSONObject(json);
//            JSONArray tags = data.getJSONObject("toptags").getJSONArray("tag");
//            Vector<ContentValues> valuesVector = new Vector<>(tags.length());
//
//            for(int i = 0; i < tags.length(); i++){
//                JSONObject tag = tags.getJSONObject(i);
//                ContentValues values = new ContentValues();
//                values.put(Contract.ResultsEntry.NAME,tag.getString("name"));
//                values.put(Contract.ResultsEntry.URL,tag.getString("url"));
//                values.put(Contract.ResultsEntry.SEARCH_QUERY,TOP_TAGS);
//
//                valuesVector.add(values);
//            }
//
//            if(valuesVector.size() > 0){
//                ContentValues[] contentValues = new ContentValues[valuesVector.size()];
//                valuesVector.toArray(contentValues);
//
//                getContentResolver().bulkInsert(Contract.ResultsEntry.CONTENT_URI,contentValues);
//            }
//        }catch (JSONException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//            e.printStackTrace();
//        }
//        queryList.remove(query);
//    }

    private void saveAlbum(String json){
        String name = "";
        String artist = "";
        String mbid = query;
        String image = "";
        String track_list = "";
        String top_tags = "";
        String wiki = "";
        try{
            JSONObject album = (new JSONObject(json)).getJSONObject("album");
            name = album.getString("name");
            artist = album.getString("artist");
            image = album.getJSONArray("image").toString();
            track_list = album.getJSONObject("tracks").getJSONArray("track").toString();
            top_tags = "";//album.getJSONObject("toptags").getJSONArray("tag").toString();
            wiki = album.getJSONObject("wiki").toString();
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(Contract.AlbumEntry.NAME,name);
        values.put(Contract.AlbumEntry.ARTIST,artist);
        values.put(Contract.AlbumEntry.MBID,mbid);
        values.put(Contract.AlbumEntry.IMAGE,image);
        values.put(Contract.AlbumEntry.TAGS,top_tags);
        values.put(Contract.AlbumEntry.TRACK_LIST,track_list);
        values.put(Contract.AlbumEntry.WIKI,wiki);

        getContentResolver().insert(Contract.AlbumEntry.CONTENT_URI, values);
        queryList.remove(query);
    }

    private void saveArtist(String json){
        String name = "";
        String similar = "";
        String mbid = query;
        String image = "";
        String top_tags = "";
        String bio = "";
        try{
            JSONObject artist = (new JSONObject(json)).getJSONObject("artist");
            name = artist.getString("name");
            similar = (new JSONObject(getSimilarArtists(query))).getJSONObject("similarartists").getJSONArray("artist").toString();
            image = artist.getJSONArray("image").toString();
            top_tags = "";//artist.getJSONObject("tags").getJSONArray("tag").toString();
            bio = artist.getJSONObject("bio").toString();
        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(Contract.ArtistEntry.NAME,name);
        values.put(Contract.ArtistEntry.MBID,mbid);
        values.put(Contract.ArtistEntry.IMAGE,image);
        values.put(Contract.ArtistEntry.TAGS,top_tags);
        values.put(Contract.ArtistEntry.SIMILAR,similar);
        values.put(Contract.ArtistEntry.BIO,bio);

        getContentResolver().insert(Contract.ArtistEntry.CONTENT_URI, values);
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
