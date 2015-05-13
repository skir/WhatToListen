package com.afsj.whattolisten.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.afsj.whattolisten.R;
import com.afsj.whattolisten.adapters.PlaylistAdapter;
import com.afsj.whattolisten.fragments.PlaylistFragment;


public class PlaylistActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        PlaylistFragment playlistFragment = ((PlaylistFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playlist));
        playlistFragment.setIntent(getIntent());
        playlistFragment.setTwoPane(false);
    }
}
