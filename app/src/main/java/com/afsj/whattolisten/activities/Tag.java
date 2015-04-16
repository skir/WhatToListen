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
import android.view.MenuItem;

import com.afsj.whattolisten.LastFmService;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.activities.PlaylistActivity;
import com.afsj.whattolisten.adapters.TagAdapter;
import com.afsj.whattolisten.data.Contract;


public class Tag extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TagAdapter adapterInfo;
    private Context mContext;
    private String tag;
    private int INFO_LOADER = 5;
    private static int transition;
    private Drawable toolbarBackground;
    private static int windowWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        mContext = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tag = "";
        Intent intent = getIntent();
        if(intent.hasExtra(LastFmService.QUERY)) {
            getSupportActionBar().setTitle(intent.getStringExtra(LastFmService.QUERY));
            tag = intent.getStringExtra(LastFmService.QUERY);
        }

        toolbarBackground = getResources().getDrawable(R.color.material_drawer_primary);
        toolbarBackground.setAlpha(0);
        toolbar.setBackgroundDrawable(toolbarBackground);
        transition = 0;
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int heidht = getWindowManager().getDefaultDisplay().getHeight();
        if(width < heidht)
            windowWidth = width;
        else
            windowWidth = heidht;

        adapterInfo = new TagAdapter(this,null,windowWidth,tag);
        adapterInfo.setPlayClick(new TagAdapter.PlayClick() {
            @Override
            public void playClick(String tag) {
                getContentResolver().delete(Contract.PlaylistEntry.CONTENT_URI, null, null);
                Intent i = new Intent(mContext,PlaylistActivity.class);
                i.putExtra(LastFmService.QUERY,tag);
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
                if (transition <= header + 1) {
                    int newAlpha = transition * 255 / header;
                    toolbarBackground.setAlpha(newAlpha);
                }
            }
        });


        getSupportLoaderManager().initLoader(INFO_LOADER, null, this);
//        getInfo(tag);
    }

    private void getInfo(String tagName){
        Intent intentService = new Intent(this,LastFmService.class);
        intentService.setAction(LastFmService.INFO);
        intentService.putExtra(LastFmService.QUERY,tagName);
        startService(intentService);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        return new CursorLoader(getBaseContext(),
                Contract.InfoEntry.CONTENT_URI,
                new String[]{Contract.InfoEntry.SUMMARY,
                        Contract.InfoEntry.ARTISTS,
                        Contract.InfoEntry.ALBUMS},
                null,null,null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        adapterInfo.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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
}
