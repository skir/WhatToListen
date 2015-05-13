package com.afsj.whattolisten.fragments;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afsj.whattolisten.LastFmService;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.Utils;
import com.afsj.whattolisten.activities.PlaylistActivity;
import com.afsj.whattolisten.activities.Tag;
import com.afsj.whattolisten.adapters.TagAdapter;
import com.afsj.whattolisten.data.Contract;

/**
 * Created by ilia on 13.05.15.
 */
public class InfoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TagAdapter adapterInfo;
    private Context mContext;
    private String tag;
    private String artist;
    private int INFO_LOADER = 5;
    private int ARTIST_LOADER = 6;
    private int ALBUM_LOADER = 7;
    private int type = 0;
    private int transition = 0;
    private Drawable toolbarBackground;
    private static int windowWidth = 0;
    private Intent intent;
    private boolean twoPane = false;

    public InfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);

        mContext = getActivity();

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
//        ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);
//        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarBackground = getResources().getDrawable(R.color.material_drawer_primary);
        if(toolbarBackground != null)
            toolbarBackground.setAlpha(0);
        toolbar.setBackgroundDrawable(toolbarBackground);
        transition = 0;

        recyclerView = ((RecyclerView) rootView.findViewById(R.id.cardList));

        return rootView;
    }

    public void setWindowWidth(int w){
        this.windowWidth = w;
    }

    public void setTwoPane(boolean twoPane){
        this.twoPane = twoPane;
    }

    public void setIntent(Intent intent){
        this.intent = intent;

        if(intent.hasExtra(Utils.TYPE))
            type = intent.getIntExtra(Utils.TYPE,0);

        if(intent.hasExtra(LastFmService.ARTIST))
            artist = intent.getStringExtra(LastFmService.ARTIST);

        tag = "";
        toolbar.setTitle("");
        if(intent.hasExtra(LastFmService.QUERY)) {
            tag = intent.getStringExtra(LastFmService.QUERY);
            if(type == Utils.TYPE_INFO)
                toolbar.setTitle(tag);
        }
        ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapterInfo = new TagAdapter(mContext,null,windowWidth,tag,type,twoPane);
        adapterInfo.setTagCardItemClick(new TagAdapter.TagCardItemClick() {
            @Override
            public void tagCardItemClick(String name, int type) {
                Intent i = new Intent(mContext, Tag.class);
                i.putExtra(LastFmService.QUERY, name);
                if (type == TagAdapter.TYPE_ALBUMS)
                    i.putExtra(Utils.TYPE, Utils.TYPE_ALBUM);
                else
                    i.putExtra(Utils.TYPE, Utils.TYPE_ARTIST);
                mContext.startActivity(i);
            }

            @Override
            public void tagCardAlbumItemClick(String name, String artist) {
                Intent i = new Intent(mContext, Tag.class);
                i.putExtra(LastFmService.QUERY, name);
                i.putExtra(LastFmService.ARTIST, artist);
                i.putExtra(Utils.TYPE, Utils.TYPE_ALBUM);
                mContext.startActivity(i);
            }
        });
        adapterInfo.setPlayClick(new TagAdapter.PlayClick() {
            @Override
            public void playClick() {
                Log.e("onclick", tag + " " + String.valueOf(type));
                Intent i = new Intent(mContext, PlaylistActivity.class);
                i.putExtra(LastFmService.QUERY, tag);
                i.putExtra(LastFmService.ARTIST, artist);
                i.putExtra(Utils.TYPE, type);
                mContext.startActivity(i);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
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
        });

        switch (type){
            case Utils.TYPE_INFO:
                getLoaderManager().initLoader(INFO_LOADER, null, this);
                break;
            case Utils.TYPE_ARTIST:
                getLoaderManager().initLoader(ARTIST_LOADER, null, this);
                break;
            case Utils.TYPE_ALBUM:
                getLoaderManager().initLoader(ALBUM_LOADER, null, this);
                break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e("onresume", tag + " " + String.valueOf(type));
        adapterInfo.setPlayClick(new TagAdapter.PlayClick() {
            @Override
            public void playClick() {
                Log.e("onclick",tag+" "+String.valueOf(type));
                Intent i = new Intent(mContext, PlaylistActivity.class);
                i.putExtra(LastFmService.QUERY, tag);
                i.putExtra(LastFmService.ARTIST, artist);
                i.putExtra(Utils.TYPE,type);
                mContext.startActivity(i);
            }
        });
        int header = 2 * windowWidth / 3 - getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
//                Log.e("DY",String.valueOf(header));
        if (transition <= header) {
            int newAlpha = transition * 255 / header;
            toolbarBackground.setAlpha(newAlpha);
        }
        else
            toolbarBackground.setAlpha(255);
    }

    private void getInfo(String tagName){
        Intent intentService = new Intent(mContext,LastFmService.class);

        if(type == Utils.TYPE_INFO)
            intentService.setAction(LastFmService.INFO);
        if(type == Utils.TYPE_ARTIST)
            intentService.setAction(LastFmService.ARTIST);
        if(type == Utils.TYPE_ALBUM) {
            intentService.setAction(LastFmService.ALBUM);
            intentService.putExtra(LastFmService.ARTIST,intent.getStringExtra(LastFmService.ARTIST));
        }

        intentService.putExtra(LastFmService.QUERY,tagName);
        mContext.startService(intentService);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        Log.e("TAG", tag);
        if(type == Utils.TYPE_ARTIST)
            return new CursorLoader(mContext,
                    Contract.ArtistEntry.CONTENT_URI,
                    null,
                    Contract.ArtistEntry.MBID + " = ? COLLATE NOCASE",
                    new String[]{tag},null);

        if(type == Utils.TYPE_ALBUM)
            return new CursorLoader(mContext,
                    Contract.AlbumEntry.CONTENT_URI,
                    null,
                    Contract.AlbumEntry.MBID + " = ?",
                    new String[]{tag},null);

        return new CursorLoader(mContext,
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
                getActivity().onBackPressed();
                break;

        }

        return true;
    }
}
