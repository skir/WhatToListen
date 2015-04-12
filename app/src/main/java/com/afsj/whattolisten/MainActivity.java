package com.afsj.whattolisten;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.afsj.whattolisten.data.Contract;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "MainActivity";
    private static final int HISTORY_LOADER = 0;
    private static final int RESULTS_LOADER = 1;
    private Drawer.Result drawer;
    private HistoryAdapter adapterHistory;
    private ResultsAdapter adapterResults;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private int toolbarOffset = 0;
    private boolean isHistory = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapterHistory = new HistoryAdapter(null);

        recyclerView = ((RecyclerView) findViewById(R.id.history));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterHistory);
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

        drawer = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withHeader(R.layout.drawer_header)
                .withDrawerWidthDp(240)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withBadge("99").withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_free_play).withIcon(FontAwesome.Icon.faw_gamepad),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_custom).withIcon(FontAwesome.Icon.faw_eye).withBadge("6").withIdentifier(2),
                        new SectionDrawerItem().withName(R.string.drawer_item_settings),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_question).setEnabled(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1)
                ).build();;


        ((EditText) findViewById(R.id.searchQuery)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(v.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });

        getSupportLoaderManager().initLoader(HISTORY_LOADER, null, this);
    }

    private void search(String searchQuery){
//        ContentValues values = new ContentValues();
//        values.put(Contract.HistoryEntry.QUERY, searchQuery);
//        getContentResolver().insert(Contract.HistoryEntry.CONTENT_URI, values);
//        Intent intent = new Intent(this, SearhResults.class);
//        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,toolbar,"toolbar");
//        ActivityCompat.startActivity(this,intent,options.toBundle());
        isHistory = false;
        adapterResults = new ResultsAdapter(null);
        recyclerView.setAdapter(adapterResults);
        getSupportLoaderManager().destroyLoader(HISTORY_LOADER);
        getSupportLoaderManager().initLoader(RESULTS_LOADER, null, this);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        Log.e("loader","created " + String.valueOf(id));
        if(id == HISTORY_LOADER)
                return new CursorLoader(getBaseContext(),
                        Contract.HistoryEntry.CONTENT_URI,
                        new String[]{Contract.HistoryEntry.QUERY}, null, null, null);
        else
                return new CursorLoader(getBaseContext(),
                        Contract.ResultsEntry.CONTENT_URI,
                        new String[]{Contract.ResultsEntry.NAME},null,null,null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        Log.e("loader","reset");
        if(isHistory) adapterHistory.swapCursor(null);
        else adapterResults.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e("loader","finished");
        if(isHistory) adapterHistory.swapCursor(data);
        else adapterResults.swapCursor(data);
//        if(selectedItem != ListView.INVALID_POSITION && listView != null) listView.smoothScrollToPosition(selectedItem);
    }

    @Override
    public void onBackPressed(){
        if(isHistory)
            super.onBackPressed();
        else{
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
            isHistory = true;
            adapterHistory = new HistoryAdapter(null);
            recyclerView.setAdapter(adapterHistory);
            getSupportLoaderManager().destroyLoader(RESULTS_LOADER);
            getSupportLoaderManager().initLoader(HISTORY_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_main, menu);
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
