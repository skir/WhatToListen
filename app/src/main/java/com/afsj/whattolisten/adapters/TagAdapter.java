package com.afsj.whattolisten.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afsj.whattolisten.DividerItemDecoration;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.data.Contract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ilia on 13.04.15.
 */
public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private Cursor info;
    private static Context mContext;
    private static int windowWidth;
    private DataSetObserver mDataSetObserver;
    private final int TYPE_HEADER_INFO = 0;
    private final int TYPE_ALBUMS = 1;
    private final int TYPE_ARTISTS = 2;

    public TagAdapter(Context context,Cursor info,int windowWidth){
        this.info = info;
        mContext = context;
        this.windowWidth = windowWidth;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (info != null) {
//            this.data.moveToFirst();
            info.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_HEADER_INFO:
                return new InfoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_summary, parent, false));
            case TYPE_ALBUMS:
                return new CardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_card,parent,false));
            case TYPE_ARTISTS:
                return new CardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_card,parent,false));
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public int getItemViewType(int position) {
        switch (position){
            case 0:
                return TYPE_HEADER_INFO;
            case 1:
                return TYPE_ALBUMS;
            case 2:
                return TYPE_ARTISTS;
        }

        return TYPE_HEADER_INFO;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(info != null && info.getCount() > 0) {
            info.moveToFirst();
            switch (position) {
                case 0:
                    ((InfoViewHolder) holder).mTextView.setText(Html.fromHtml(info.getString(info.getColumnIndex(Contract.InfoEntry.SUMMARY))));
                    try {
                        JSONArray array = new JSONArray(info.getString(info.getColumnIndex(Contract.InfoEntry.ARTISTS)));
                        JSONObject item = array.getJSONObject(1);
                        Picasso.with(mContext)
                                .load(item.getJSONArray("image").getJSONObject(4).getString("#text"))
                                .into(((InfoViewHolder) holder).image);
                    }catch (JSONException e){
                        Log.e("JSONException",e.toString());
                    }
                    break;
                case 1:
                    ((CardViewHolder) holder).recyclerView.setAdapter(new CardAdapter(true,mContext,info.getString(info.getColumnIndex(Contract.InfoEntry.ALBUMS))));
                    ((CardViewHolder) holder).title.setText(mContext.getString(R.string.albums));
                    break;
                case 2:
                    ((CardViewHolder) holder).recyclerView.setAdapter(new CardAdapter(false,mContext,info.getString(info.getColumnIndex(Contract.InfoEntry.ARTISTS))));
                    ((CardViewHolder) holder).title.setText(mContext.getString(R.string.artists));
            }
        }

    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == info) {
            return null;
        }
        final Cursor oldCursor = info;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        info = newCursor;
        if (info != null) {
            if (mDataSetObserver != null) {
                info.registerDataSetObserver(mDataSetObserver);
            }
            notifyDataSetChanged();
        } else {
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            notifyDataSetChanged();
        }
    }

    public static class InfoViewHolder extends RecyclerView.ViewHolder  {
        public TextView mTextView;
        public ImageView image;
        public InfoViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.info);
            image = (ImageView) v.findViewById(R.id.image);
            ((RelativeLayout) v.findViewById(R.id.layout)).getLayoutParams().height = 2 * windowWidth / 3;
        }
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder{
        public RecyclerView recyclerView;
        public TextView title;
        public CardViewHolder(View v){
            super(v);
            recyclerView = (RecyclerView) v.findViewById(R.id.albums);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL_LIST));

            title = (TextView) v.findViewById(R.id.title);
        }
    }
}
