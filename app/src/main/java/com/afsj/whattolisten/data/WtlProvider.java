package com.afsj.whattolisten.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by ilia on 26.03.15.
 */
public class WtlProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DbHelper mOpenHelper;

    static final int HISTORY = 100;
    static final int RESULTS = 101;
    static final int INFO = 102;
    static final int PLAYLIST = 103;

    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,Contract.PATH_HISTORY,HISTORY);
        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,Contract.PATH_RESULTS,RESULTS);
        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,Contract.PATH_INFO,INFO);
        uriMatcher.addURI(Contract.CONTENT_AUTHORITY,Contract.PATH_PLAYLIST,PLAYLIST);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case HISTORY:
                return Contract.HistoryEntry.CONTENT_TYPE;
            case RESULTS:
                return Contract.ResultsEntry.CONTENT_TYPE;
            case INFO:
                return Contract.InfoEntry.CONTENT_TYPE;
            case PLAYLIST:
                return Contract.PlaylistEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case HISTORY:
                retCursor = mOpenHelper.getReadableDatabase().query(Contract.HistoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case RESULTS:
                retCursor = mOpenHelper.getReadableDatabase().query(Contract.ResultsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case INFO:
                retCursor = mOpenHelper.getReadableDatabase().query(Contract.InfoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PLAYLIST:
                retCursor = mOpenHelper.getReadableDatabase().query(Contract.PlaylistEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case HISTORY: {
                long _id = db.insert(Contract.HistoryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = Contract.HistoryEntry.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case RESULTS:{
                long _id = db.insert(Contract.ResultsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = Contract.ResultsEntry.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case INFO:{
                long _id = db.insert(Contract.InfoEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = Contract.InfoEntry.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLAYLIST:{
                long _id = db.insert(Contract.PlaylistEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = Contract.PlaylistEntry.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
//        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int id;
        if(selection == null) selection = "1";
        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        switch (match) {
            case HISTORY:
                id = db.delete(Contract.HistoryEntry.TABLE_NAME, selection,selectionArgs);
                break;

            case RESULTS:
                id = db.delete(Contract.ResultsEntry.TABLE_NAME, selection,selectionArgs);
                break;

            case INFO:
                id = db.delete(Contract.InfoEntry.TABLE_NAME, selection,selectionArgs);
                break;

            case PLAYLIST:
                id = db.delete(Contract.PlaylistEntry.TABLE_NAME, selection,selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(id != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int id;

        switch (match) {
            case HISTORY: {
                id = db.update(Contract.HistoryEntry.TABLE_NAME, values,selection,selectionArgs);
                break;
            }
            case RESULTS:{
                id = db.update(Contract.ResultsEntry.TABLE_NAME, values, selection,selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(id != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case RESULTS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(Contract.ResultsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case PLAYLIST: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(Contract.PlaylistEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
