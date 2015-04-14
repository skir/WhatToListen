package com.afsj.whattolisten.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.afsj.whattolisten.data.Contract.HistoryEntry;
import com.afsj.whattolisten.data.Contract.ResultsEntry;

/**
 * Created by ilia on 26.03.15.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "whattolisten.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_HISTORY_TABLE = "CREATE TABLE " + HistoryEntry.TABLE_NAME + " (" +
                HistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HistoryEntry.QUERY + " TEXT NOT NULL );";

        final String SQL_CREATE_RESULTS_TABLE = "CREATE TABLE " + ResultsEntry.TABLE_NAME + " (" +
                ResultsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ResultsEntry.NAME + " TEXT NOT NULL, " +
                ResultsEntry.TYPE + " TEXT NOT NULL, " +
                ResultsEntry.URL + " TEXT NOT NULL );";

        final String SQL_CREATE_INFO_TABLE = "CREATE TABLE " + Contract.InfoEntry.TABLE_NAME + " (" +
                Contract.InfoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Contract.InfoEntry.ALBUMS + " TEXT NOT NULL, " +
                Contract.InfoEntry.ARTISTS + " TEXT NOT NULL, " +
                Contract.InfoEntry.SUMMARY + " TEXT NOT NULL );";

        sqLiteDatabase.execSQL(SQL_CREATE_HISTORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RESULTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HistoryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ResultsEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Contract.InfoEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
