package com.afsj.whattolisten.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.afsj.whattolisten.DividerItemDecoration;
import com.afsj.whattolisten.LastFmService;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.Utils;
import com.afsj.whattolisten.adapters.PlaylistAdapter;
import com.afsj.whattolisten.data.Contract;

/**
 * Created by ilia on 13.05.15.
 */
public class PlaylistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private PlaylistAdapter adapterPlaylist;
    private int PLAYLIST_LOADER = 42;
    private int toolbarOffset = 0;
    private String tag;
    private String artist;
    private int type = 0;
    private Intent intent;
    private boolean twoPane = false;

    public PlaylistFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(!twoPane);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
//        ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);

//        setUpTag();

        recyclerView = ((RecyclerView) rootView.findViewById(R.id.playlist));

        return rootView;
    }

    public void setTwoPane(boolean twoPane){
        this.twoPane = twoPane;
        if(!twoPane) ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setIntent(Intent intent){
        this.intent = intent;
        setUpTag();
        setUpRecyclerView();
    }

    private void setUpTag(){
        tag = "";
        if (intent != null){
            if(intent.hasExtra(Utils.TYPE))
                type = intent.getIntExtra(Utils.TYPE,0);
            if(intent.hasExtra(LastFmService.QUERY)) {
                tag = intent.getStringExtra(LastFmService.QUERY);
            }
            if(intent.hasExtra(LastFmService.ARTIST))
                artist = intent.getStringExtra(LastFmService.ARTIST);
        }
        toolbar.setTitle("");

        switch (type){
            case Utils.TYPE_ALBUM: {
                toolbar.setTitle(artist + " and similar");
                tag = artist;
                break;
            }
            case Utils.TYPE_ARTIST: {
                Log.e("title", tag);
                toolbar.setTitle(tag + " and similar");
                break;
            }
            default:
                if(!twoPane) toolbar.setTitle(tag);
                else toolbar.setVisibility(View.GONE);
        }
        ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);

        getLoaderManager().initLoader(PLAYLIST_LOADER, null, this);
    }

    private void setUpRecyclerView(){

        adapterPlaylist = new PlaylistAdapter(getActivity(),null,type,twoPane);
        adapterPlaylist.setListItemClick(new PlaylistAdapter.ListItemClick() {
            @Override
            public void listItemClick(String location) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + location)));
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterPlaylist);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
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
        Intent intentService = new Intent(getActivity(), LastFmService.class);
        intentService.setAction(LastFmService.RADIO_TUNE);
        intentService.putExtra(LastFmService.QUERY, tag);
        intentService.putExtra(Utils.TYPE,type);
        getActivity().startService(intentService);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        return new CursorLoader(getActivity(),
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
                getActivity().onBackPressed();
                break;

        }

        return true;
    }
}
