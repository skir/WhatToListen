package com.afsj.whattolisten.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.afsj.whattolisten.DividerItemDecoration;
import com.afsj.whattolisten.LastFmService;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.adapters.HistoryAdapter;
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
    private Drawer.Result drawer;
    private HistoryAdapter adapterHistory;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private Cursor history;
    private int toolbarOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText editText = (EditText) findViewById(R.id.searchQuery);

        adapterHistory = new HistoryAdapter(null);
        adapterHistory.setListItemClick(new HistoryAdapter.ListItemClick() {
            @Override
            public void listItemClick(String query) {
                editText.setText(query);
                search(query);
            }
        });

        recyclerView = ((RecyclerView) findViewById(R.id.history));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapterHistory);
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

        drawer = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withHeader(R.layout.drawer_header)
                .withDrawerWidthDp(240)
                .addDrawerItems(
                        new SectionDrawerItem().withName(R.string.drawer_item_settings),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_question).setEnabled(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1)
                ).build();


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
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(LastFmService.QUERY, searchQuery);
//        getWindow().setSharedElementEnterTransition(new ChangeImageTransform());
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, toolbar, "toolbar");
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        return new CursorLoader(getBaseContext(),
                Contract.HistoryEntry.CONTENT_URI,
                new String[]{Contract.HistoryEntry.QUERY, Contract.HistoryEntry._ID}, null, null, Contract.HistoryEntry._ID + " DESC");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        adapterHistory.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        history = data;
        if(data.getCount() > 0)
            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        adapterHistory.swapCursor(data);
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        MenuInflater inflater = new MenuInflater(this);
//        inflater.inflate(R.menu.menu_main, menu);
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
