package com.afsj.whattolisten;

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
import android.view.Menu;
import android.view.MenuItem;

import com.afsj.whattolisten.adapters.TagAdapter;
import com.afsj.whattolisten.data.Contract;


public class Tag extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TagAdapter adapterInfo;
    private int INFO_LOADER = 5;
    private static int transition;
    private Drawable toolbarBackground;
    private static int windowWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarBackground = getResources().getDrawable(R.color.material_drawer_primary);
        toolbarBackground.setAlpha(0);
        toolbar.setBackgroundDrawable(toolbarBackground);
        transition = 0;
        windowWidth = getWindowManager().getDefaultDisplay().getWidth();

        adapterInfo = new TagAdapter(this,null,windowWidth);
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

        Intent intent = getIntent();
        if(intent.hasExtra(LastFmService.QUERY)) {
            getSupportActionBar().setTitle(intent.getStringExtra(LastFmService.QUERY));
            getInfo(intent.getStringExtra(LastFmService.QUERY));
        }

        getSupportLoaderManager().initLoader(INFO_LOADER, null, this);
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
        adapterInfo.swapCursor(data);
    }
}
