package com.afsj.whattolisten.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.afsj.whattolisten.LastFmService;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.adapters.TagAdapter;
import com.afsj.whattolisten.data.Contract;
import com.afsj.whattolisten.Utils;


public class Tag extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TagAdapter adapterInfo;
    private Context mContext;
    private String tag;
    private int INFO_LOADER = 5;
    private int ARTIST_LOADER = 6;
    private int ALBUM_LOADER = 7;
    private int type = 0;
    private static int transition;
    private Drawable toolbarBackground;
    private static int windowWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        mContext = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if(intent.hasExtra(Utils.TYPE))
            type = intent.getIntExtra(Utils.TYPE,0);

        tag = "";
        if(intent.hasExtra(LastFmService.QUERY)) {
            tag = intent.getStringExtra(LastFmService.QUERY);
            if(type == Utils.TYPE_INFO)
                getSupportActionBar().setTitle(tag);
            else
                getSupportActionBar().setTitle("");
        }

        toolbarBackground = getResources().getDrawable(R.color.material_drawer_primary);
        if(toolbarBackground != null)
            toolbarBackground.setAlpha(0);
        toolbar.setBackgroundDrawable(toolbarBackground);
        transition = 0;
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int heidht = getWindowManager().getDefaultDisplay().getHeight();
        if(width < heidht)
            windowWidth = width;
        else
            windowWidth = heidht;

        adapterInfo = new TagAdapter(this,null,windowWidth,tag,type);
        adapterInfo.setTagCardItemClick(new TagAdapter.TagCardItemClick() {
            @Override
            public void tagCardItemClick(String mbid, int type) {
                Intent i = new Intent(mContext, Tag.class);
                i.putExtra(LastFmService.QUERY, mbid);
                if (type == TagAdapter.TYPE_ALBUMS)
                    i.putExtra(Utils.TYPE, Utils.TYPE_ALBUM);
                else
                    i.putExtra(Utils.TYPE, Utils.TYPE_ARTIST);
                mContext.startActivity(i);
            }
        });
        recyclerView = ((RecyclerView) findViewById(R.id.cardList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterInfo);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                transition += dy;
                int header = 2 * windowWidth / 3 - getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
//                Log.e("DY",String.valueOf(header));
                if (transition <= header) {
                    int newAlpha = transition * 255 / header;
                    toolbarBackground.setAlpha(newAlpha);
                }
                else
                    toolbarBackground.setAlpha(255);
            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if(newState == 0){
//
//                }
//            }
        });

        switch (type){
            case Utils.TYPE_INFO:
                getSupportLoaderManager().initLoader(INFO_LOADER, null, this);
                break;
            case Utils.TYPE_ARTIST:
                getSupportLoaderManager().initLoader(ARTIST_LOADER, null, this);
                break;
            case Utils.TYPE_ALBUM:
                getSupportLoaderManager().initLoader(ALBUM_LOADER, null, this);
                break;
        }
    }

    private void getInfo(String tagName){
        Intent intentService = new Intent(this,LastFmService.class);

        if(type == Utils.TYPE_INFO)
            intentService.setAction(LastFmService.INFO);
        if(type == Utils.TYPE_ARTIST)
            intentService.setAction(LastFmService.ARTIST);
        if(type == Utils.TYPE_ALBUM)
            intentService.setAction(LastFmService.ALBUM);

        intentService.putExtra(LastFmService.QUERY,tagName);
        startService(intentService);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        Log.e("TAG",tag);
        if(type == Utils.TYPE_ARTIST)
            return new CursorLoader(getBaseContext(),
                    Contract.ArtistEntry.CONTENT_URI,
                    null,
                    Contract.ArtistEntry.MBID + " = ? COLLATE NOCASE",
                    new String[]{tag},null);

        if(type == Utils.TYPE_ALBUM)
            return new CursorLoader(getBaseContext(),
                    Contract.AlbumEntry.CONTENT_URI,
                    null,
                    Contract.AlbumEntry.MBID + " = ?",
                    new String[]{tag},null);

        return new CursorLoader(getBaseContext(),
                Contract.InfoEntry.CONTENT_URI,
                new String[]{Contract.InfoEntry.SUMMARY,
                        Contract.InfoEntry.ARTISTS,
                        Contract.InfoEntry.ALBUMS},
                Contract.InfoEntry.TAG + " = ?",
                new String[]{tag},null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        adapterInfo.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e("data",String.valueOf(data.getCount()));
        if(data.getCount() == 0)
            getInfo(tag);
        adapterInfo.swapCursor(data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;

        }

        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e("onresume",tag+" "+String.valueOf(type));
        adapterInfo.setPlayClick(new TagAdapter.PlayClick() {
            @Override
            public void playClick() {
                Log.e("onclick",tag+" "+String.valueOf(type));
                Intent i = new Intent(mContext, PlaylistActivity.class);
                i.putExtra(LastFmService.QUERY, tag);
                i.putExtra(Utils.TYPE,type);
                mContext.startActivity(i);
            }
        });
    }
}
