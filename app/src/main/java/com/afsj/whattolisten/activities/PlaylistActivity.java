package com.afsj.whattolisten.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.afsj.whattolisten.DividerItemDecoration;
import com.afsj.whattolisten.LastFmService;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.Utils;
import com.afsj.whattolisten.adapters.PlaylistAdapter;
import com.afsj.whattolisten.data.Contract;


public class PlaylistActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private PlaylistAdapter adapterPlaylist;
    private int PLAYLIST_LOADER = 42;
    private int toolbarOffset = 0;
    private String tag;
    private int type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpTag();

        setUpRecyclerView();

        getSupportLoaderManager().initLoader(PLAYLIST_LOADER, null, this);
    }

    private void setUpTag(){
        tag = "";
        Intent intent = getIntent();
        if(intent.hasExtra(Utils.TYPE))
            type = intent.getIntExtra(Utils.TYPE,0);
        if(intent.hasExtra(LastFmService.QUERY)) {
            getSupportActionBar().setTitle(intent.getStringExtra(LastFmService.QUERY));
            tag = intent.getStringExtra(LastFmService.QUERY);
        }
        switch (type){
            case Utils.TYPE_ALBUM: {
                Cursor album = getContentResolver().query(Contract.AlbumEntry.CONTENT_URI,
                        new String[]{Contract.AlbumEntry.ARTIST},
                        Contract.AlbumEntry.MBID + " = ? ",
                        new String[]{tag}, null);
                album.moveToFirst();
                tag = album.getString(album.getColumnIndex(Contract.AlbumEntry.ARTIST));
                getSupportActionBar().setTitle(tag + " and similar");
                break;
            }
            case Utils.TYPE_ARTIST: {
                Cursor album = getContentResolver().query(Contract.ArtistEntry.CONTENT_URI,
                        new String[]{Contract.ArtistEntry.NAME},
                        Contract.ArtistEntry.MBID + " = ? ",
                        new String[]{tag}, null);
                album.moveToFirst();
                tag = album.getString(album.getColumnIndex(Contract.ArtistEntry.NAME));
                getSupportActionBar().setTitle(tag + " and similar");
                break;
            }
            default:
                getSupportActionBar().setTitle(tag);
        }
    }

    private void setUpRecyclerView(){

        adapterPlaylist = new PlaylistAdapter(this,null);
        adapterPlaylist.setListItemClick(new PlaylistAdapter.ListItemClick() {
            @Override
            public void listItemClick(String location) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + location)));
            }
        });

        recyclerView = ((RecyclerView) findViewById(R.id.playlist));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterPlaylist);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && toolbar.getHeight() > toolbarOffset) { // go down
                    if (toolbarOffset + dy <= toolbar.getHeight()) {
                        toolbar.setTranslationY(toolbar.getTranslationY() - dy);
                        toolbarOffset += dy;
                    } else {
                        toolbar.setTranslationY(-toolbar.getHeight());
                        toolbarOffset = toolbar.getHeight();
                    }
                }
                if (dy < 0 && toolbarOffset > 0) {
                    if (toolbarOffset + dy > 0) {
                        toolbar.setTranslationY(toolbar.getTranslationY() - dy);
                        toolbarOffset += dy;
                    } else {
                        toolbar.setTranslationY(0);
                        toolbarOffset = 0;
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
                    if ((double) toolbarOffset > (double) toolbar.getHeight() / 2.0) {
                        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator()).start();
                        toolbarOffset = toolbar.getHeight();
                    } else {
                        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                        toolbarOffset = 0;
                    }
                }
            }
        });
    }

    private void radioTune(){
        Intent intentService = new Intent(this, LastFmService.class);
        intentService.setAction(LastFmService.RADIO_TUNE);
        intentService.putExtra(LastFmService.QUERY, tag);
        intentService.putExtra(Utils.TYPE,type);
        startService(intentService);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        return new CursorLoader(getBaseContext(),
                Contract.PlaylistEntry.CONTENT_URI,
                new String[]{Contract.PlaylistEntry.TITLE,
                        Contract.PlaylistEntry.ARTIST,
                        Contract.PlaylistEntry.ALBUM,
                        Contract.PlaylistEntry.LOCATION,
                        Contract.PlaylistEntry.DURATION},
                Contract.PlaylistEntry.TAG + " = ?",
                new String[]{tag},null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        adapterPlaylist.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount() == 0)
            radioTune();
        adapterPlaylist.swapCursor(data);
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
}
