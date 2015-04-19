package com.afsj.whattolisten.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ilia on 26.03.15.
 */
public class Contract {

    public static final String CONTENT_AUTHORITY = "com.afsj.whattolisten";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_HISTORY = "history";
    public static final String PATH_RESULTS = "results";
    public static final String PATH_INFO = "info";
    public static final String PATH_PLAYLIST = "playlist";
    public static final String PATH_ALBUM = "album";
    public static final String PATH_ARTIST = "artist";

    public static final class HistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = PATH_HISTORY;

        public static final String QUERY = "query";
        //TODO add user

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HISTORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HISTORY;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ResultsEntry implements BaseColumns {
        public static final String TABLE_NAME = PATH_RESULTS;

        public static final String NAME = "name";
        public static final String URL = "url";
        public static final String SEARCH_QUERY = "search_query";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RESULTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RESULTS;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class InfoEntry implements BaseColumns{
        public static final String TABLE_NAME = PATH_INFO;

        public static final String SUMMARY = "summary";
        public static final String ALBUMS = "albums";
        public static final String ARTISTS = "artists";
        public static final String TAG = "tag";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INFO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INFO;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class PlaylistEntry implements BaseColumns{
        public static final String TABLE_NAME = PATH_PLAYLIST;

        public static final String TITLE = "title";
        public static final String LOCATION = "location";
        public static final String ALBUM = "album";
        public static final String ARTIST = "artist";
        public static final String TAG = "tag";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAYLIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLAYLIST;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class AlbumEntry implements BaseColumns{
        public static final String TABLE_NAME = PATH_ALBUM;

        public static final String NAME = "name";
        public static final String ARTIST = "artist";
        public static final String MBID = "mbid";
        public static final String TRACK_LIST = "track_list";
        public static final String TAGS = "tags";
        public static final String WIKI = "wiki";
        public static final String IMAGE = "image";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ALBUM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALBUM;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ArtistEntry implements BaseColumns{
        public static final String TABLE_NAME = PATH_ARTIST;

        public static final String NAME = "name";
        public static final String MBID = "mbid";
        public static final String SIMILAR = "similar";
        public static final String TAGS = "tags";
        public static final String BIO = "bio";
        public static final String IMAGE = "image";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


}
