package com.afsj.whattolisten.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.ActivityCompat;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.afsj.whattolisten.DividerItemDecoration;
import com.afsj.whattolisten.LastFmService;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.adapters.ResultsAdapter;
import com.afsj.whattolisten.data.Contract;


public class ResultsActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int RESULTS_LOADER = 1;

    private ResultsAdapter adapterResults;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private int toolbarOffset = 0;
    private Context mContext;
    private String tag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mContext = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapterResults = new ResultsAdapter(this,null);
        adapterResults.setListItemClick(new ResultsAdapter.ListItemClick() {
            @Override
            public void listItemClick(String query) {
                getContentResolver().delete(Contract.InfoEntry.CONTENT_URI, null, null);
                Intent intent = new Intent(mContext,Tag.class);
                intent.putExtra(LastFmService.QUERY,query);
                mContext.startActivity(intent);
            }
        });

        recyclerView = ((RecyclerView) findViewById(R.id.history));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterResults);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int toolbarHeight = toolbar.getHeight() + Math.round((float)16 * getApplicationContext().getResources().getDisplayMetrics().density);
                if (dy > 0 && toolbarHeight > toolbarOffset) { // go down
                    if (toolbarOffset + dy <= toolbarHeight) {
                        toolbar.setTranslationY(toolbar.getTranslationY() - dy);
                        toolbarOffset += dy;
                    } else {
                        toolbar.setTranslationY(-toolbarHeight);
                        toolbarOffset = toolbarHeight;
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
                int toolbarHeight = toolbar.getHeight() + Math.round((float)16 * getApplicationContext().getResources().getDisplayMetrics().density);
                if (newState == 0) {
                    if ((double) toolbarOffset > (double) toolbarHeight / 2.0) {
                        toolbar.animate().translationY(-toolbarHeight).setInterpolator(new AccelerateInterpolator()).start();
                        toolbarOffset = toolbarHeight;
                    } else {
                        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                        toolbarOffset = 0;
                    }
                }
            }
        });

        EditText editText = (EditText) findViewById(R.id.searchQuery);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    getContentResolver().delete(Contract.ResultsEntry.CONTENT_URI, null, null);
                    search(v.getText().toString());
                    ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    handled = true;
                }
                return handled;
            }
        });

        Intent intent = getIntent();
//        tag = "";
        if(intent.hasExtra(LastFmService.QUERY)) {
            tag = intent.getStringExtra(LastFmService.QUERY);
        }
        editText.setText(tag);

        getSupportLoaderManager().initLoader(RESULTS_LOADER, null, this);
    }

    private void search(String query){

        Intent intentService = new Intent(this,LastFmService.class);
        intentService.setAction(LastFmService.SEARCH);
        intentService.putExtra(LastFmService.QUERY,query);
        startService(intentService);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        Log.e("tag", tag);
        return new CursorLoader(getBaseContext(),
                Contract.ResultsEntry.CONTENT_URI,
                new String[]{Contract.ResultsEntry.NAME, Contract.ResultsEntry.SEARCH_QUERY},
                Contract.ResultsEntry.SEARCH_QUERY + " = ? COLLATE NOCASE",
                new String[]{tag},null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        adapterResults.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e("results", String.valueOf(data.getCount()));
        if(data.getCount() == 0)
            search(tag);
        else
            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        adapterResults.swapCursor(data);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
    }

    @Override
    public void onSaveInstanceState(Bundle state){
        Log.e("onSaveInstanceState",tag);
        state.putString("tag", tag);
        super.onSaveInstanceState(state);
    }

    @Override
    public void onRestoreInstanceState(Bundle state){
        tag = state.getString("tag");
        Log.e("onRestoreInstanceState",tag);
        super.onRestoreInstanceState(state);
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
