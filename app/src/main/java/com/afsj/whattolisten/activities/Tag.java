package com.afsj.whattolisten.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.afsj.whattolisten.R;
import com.afsj.whattolisten.adapters.TagAdapter;
import com.afsj.whattolisten.fragments.InfoFragment;
import com.afsj.whattolisten.fragments.PlaylistFragment;


public class Tag extends ActionBarActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TagAdapter adapterInfo;
    private Context mContext;
    private String tag;
    private int INFO_LOADER = 5;
    private int ARTIST_LOADER = 6;
    private int ALBUM_LOADER = 7;
    private int type = 0;
    private int transition = 0;
    private Drawable toolbarBackground;
    private static int windowWidth = 0;

    private InfoFragment infoFragment;
    private PlaylistFragment playlistFragment;
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        infoFragment = ((InfoFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_info));
        if (findViewById(R.id.fragment_playlist) != null) {
            mTwoPane = true;

            int width = (int)(getWindowManager().getDefaultDisplay().getWidth() * 4.0/7.0 * 2.0/3.0);
            int heidht = getWindowManager().getDefaultDisplay().getHeight();
            if(width < heidht)
                windowWidth = width;
            else
                windowWidth = heidht;

            infoFragment.setWindowWidth(windowWidth);
            infoFragment.setTwoPane(mTwoPane);
            infoFragment.setIntent(getIntent());

            playlistFragment = ((PlaylistFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playlist));
            playlistFragment.setTwoPane(true);
            playlistFragment.setIntent(getIntent());

        } else {
            mTwoPane = false;

            int width = getWindowManager().getDefaultDisplay().getWidth();
            int heidht = getWindowManager().getDefaultDisplay().getHeight();
            if(width < heidht)
                windowWidth = width;
            else
                windowWidth = heidht;

            infoFragment.setWindowWidth(windowWidth);
            infoFragment.setTwoPane(mTwoPane);
            infoFragment.setIntent(getIntent());

        }
    }
}
