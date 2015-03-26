package com.afsj.whattolisten;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


public class MainFragment extends Fragment {

    private static final String LOG_TAG = "MainFragment";

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ((EditText) rootView.findViewById(R.id.search_edittext)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    search(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
        return rootView;
    }


    public void search(String query){
        Log.e(LOG_TAG, query);
        Intent intent = new Intent(getActivity(),LastFmService.class);
        intent.setAction(LastFmService.SEARCH);
        intent.putExtra(LastFmService.QUERY,query);
        getActivity().startService(intent);
    }
}
