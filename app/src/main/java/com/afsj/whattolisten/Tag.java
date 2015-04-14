package com.afsj.whattolisten;

import android.content.Intent;
import android.database.Cursor;
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

import com.afsj.whattolisten.data.Contract;


public class Tag extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TagAdapter adapterInfo;
    private int INFO_LOADER = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapterInfo = new TagAdapter(this,null);
        recyclerView = ((RecyclerView) findViewById(R.id.cardList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterInfo);

        Intent intent = getIntent();
        if(intent.hasExtra(LastFmService.QUERY)) {
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
