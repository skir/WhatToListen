package com.afsj.whattolisten.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.afsj.whattolisten.DividerItemDecoration;
import com.afsj.whattolisten.LastFmService;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.adapters.ResultsAdapter;
import com.afsj.whattolisten.data.Contract;
import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuView;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;


public class ResultsActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int RESULTS_LOADER = 1;

    private ResultsAdapter adapterResults;
    private RecyclerView recyclerView;
    private DividerItemDecoration dividerItemDecoration;
    private SearchBox searchBox;
    private MaterialMenuView materialMenuView;
    private int searchBarOffset = 0;
    private int scrollPosition = 0;
    private Context mContext;
    private DrawerLayout drawerLayout;
    private Drawer.Result drawer;
    private boolean isDrawerOpened = false;
    private String tag = "";
    private boolean isResults = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        mContext = this;

        setUpSearchBox();
        materialMenuView = searchBox.getMaterialMenu();

        setUpRecyclerView();

        setUpDrawer();

        searchBox.setLogoText(getString(R.string.what_to_listen));

        getSupportLoaderManager().initLoader(RESULTS_LOADER, null, this);
    }

    private void setUpSearchBox(){
        searchBox = (SearchBox) findViewById(R.id.searchbox);
        searchBox.setMenuListener(new SearchBox.MenuListener() {

            @Override
            public void onMenuClick() {
                //Hamburger has been clicked
                drawer.openDrawer();
            }

        });
        searchBox.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                //Use this to tint the screen
            }

            @Override
            public void onSearchClosed() {
                //Use this to un-tint the screen
            }

            @Override
            public void onSearchTermChanged() {
                //React to the search term changing
                //Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
//                search(searchTerm);
                tag = searchTerm;
                isResults = true;
                searchBox.showLoading(true);
                getSupportLoaderManager().restartLoader(RESULTS_LOADER, null, ResultsActivity.this);
            }

            @Override
            public void onSearchCleared() {

            }

        });
        updateHistory();
    }

    private void setUpRecyclerView(){
        adapterResults = new ResultsAdapter(this,null);
        adapterResults.setListItemClick(new ResultsAdapter.ListItemClick() {
            @Override
            public void listItemClick(String query) {
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
                scrollPosition += dy;
                int toolbarHeight = searchBox.getHeight() + Math.round((float) 16 * getApplicationContext().getResources().getDisplayMetrics().density);
                if (dy > 0 && toolbarHeight > searchBarOffset) { // go down
                    if (searchBarOffset + dy <= toolbarHeight) {
                        searchBox.setTranslationY(searchBox.getTranslationY() - dy);
                        searchBarOffset += dy;
                    } else {
                        searchBox.setTranslationY(-toolbarHeight);
                        searchBarOffset = toolbarHeight;
                    }
                }
                if (dy < 0 && searchBarOffset > 0) {
                    if (searchBarOffset + dy > 0) {
                        searchBox.setTranslationY(searchBox.getTranslationY() - dy);
                        searchBarOffset += dy;
                    } else {
                        searchBox.setTranslationY(0);
                        searchBarOffset = 0;
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int toolbarHeight = searchBox.getHeight() + Math.round((float) 16 * getApplicationContext().getResources().getDisplayMetrics().density);
                if (newState == 0) {
                    if ((double) searchBarOffset > (double) toolbarHeight / 2.0) {
                        if(scrollPosition >= toolbarHeight)
                            searchBarOffset = toolbarHeight;
                        else
                            searchBarOffset = scrollPosition;
                    } else
                        searchBarOffset = 0;

                    searchBox.animate().translationY(-searchBarOffset).setInterpolator(new DecelerateInterpolator()).start();
                }
            }
        });

        dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);

    }

    private void setUpDrawer(){
        drawer = new Drawer()
                .withActivity(this)
                .withHeader(R.layout.drawer_header)
                .withDrawerWidthDp(240)
                .addDrawerItems(
                        new SectionDrawerItem().withName(R.string.drawer_item_settings),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_question).setEnabled(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1)
                ).build();
        drawerLayout = drawer.getDrawerLayout();
        drawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                materialMenuView.setTransformationOffset(
                        MaterialMenuDrawable.AnimationState.BURGER_ARROW,
                        isDrawerOpened ? 2 - slideOffset : slideOffset
                );
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                isDrawerOpened = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                isDrawerOpened = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                if(newState == DrawerLayout.STATE_IDLE) {
                    if(isDrawerOpened) materialMenuView.setState(MaterialMenuDrawable.IconState.ARROW);
                    else materialMenuView.setState(MaterialMenuDrawable.IconState.BURGER);
                }
            }
        });
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
        if(isResults)
            return new CursorLoader(getBaseContext(),
                    Contract.ResultsEntry.CONTENT_URI,
                    new String[]{Contract.ResultsEntry.NAME, Contract.ResultsEntry.SEARCH_QUERY},
                    Contract.ResultsEntry.SEARCH_QUERY + " = ? COLLATE NOCASE",
                    new String[]{tag},null);

        else //TODO suggestions?
            return new CursorLoader(getBaseContext(),
                    Contract.HistoryEntry.CONTENT_URI,
                    new String[]{Contract.HistoryEntry.QUERY, Contract.HistoryEntry._ID}, null, null, Contract.HistoryEntry._ID + " DESC");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        adapterResults.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e("results", String.valueOf(data.getCount()));
        if(data.getCount() == 0) {
            search(tag);
            recyclerView.removeItemDecoration(dividerItemDecoration);
        }else {
            searchBox.showLoading(false);
            updateHistory();
            recyclerView.addItemDecoration(dividerItemDecoration);
        }

        adapterResults.swapCursor(data);
    }

    private void updateHistory(){
        Cursor history = getContentResolver().query(Contract.HistoryEntry.CONTENT_URI,new String[]{Contract.HistoryEntry.QUERY, Contract.HistoryEntry._ID}, null, null, Contract.HistoryEntry._ID + " DESC");

        ArrayList<SearchResult> searchResults = new ArrayList<>();
        for(int i = 0; i < history.getCount(); i++) {
            history.moveToPosition(i);
            SearchResult option = new SearchResult(history.getString(history.getColumnIndex(Contract.HistoryEntry.QUERY)), getResources().getDrawable(R.drawable.search));
            searchResults.add(option);
        }
        searchBox.setSearchables(searchResults);
        history.close();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        ActivityCompat.finishAfterTransition(this);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        isDrawerOpened = drawerLayout.isDrawerOpen(Gravity.START);
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
        Log.e("onRestoreInstanceState", tag);
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void mic(View v) {
        searchBox.micClick(this);
    }
}
