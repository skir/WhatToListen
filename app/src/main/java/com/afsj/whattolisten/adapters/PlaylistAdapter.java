package com.afsj.whattolisten.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afsj.whattolisten.R;
import com.afsj.whattolisten.Utils;
import com.afsj.whattolisten.data.Contract;

/**
 * Created by ilia on 14.04.15.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Cursor data;
    private DataSetObserver mDataSetObserver;
    private static Context mContext;
    private static ListItemClick listItemClick;
    private final int TYPE_ITEM = 1;
    private final int TYPE_HEADER = 2;
    private int type = 0;
    private boolean twoPane = false;

    public PlaylistAdapter(Context context,Cursor data,int type,boolean twoPane){
        this.data = data;
        mContext = context;
        this.type = type;
        this.twoPane = twoPane;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (data != null)
            data.registerDataSetObserver(mDataSetObserver);
    }

    public void setListItemClick(ListItemClick l){
        listItemClick = l;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_ITEM:
                return new TrackViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false));
            case TYPE_HEADER:
                if((twoPane && (type == Utils.TYPE_ALBUM || type == Utils.TYPE_ARTIST)) || !twoPane )
                    return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false));
                else
                    return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_small, parent, false));
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public int getItemViewType(int position) {
        if(position == 0)
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position > 0 && data != null && data.moveToPosition(position - 1)) {
            ((TrackViewHolder) holder).title.setText(data.getString(data.getColumnIndex(Contract.PlaylistEntry.TITLE)));
            String aritst = data.getString(data.getColumnIndex(Contract.PlaylistEntry.ARTIST));
            String album = data.getString(data.getColumnIndex(Contract.PlaylistEntry.ALBUM));
            if(!album.equals(""))
                aritst += " - " + album;

            ((TrackViewHolder) holder).artistAlbum.setText(aritst);
            ((TrackViewHolder) holder).location = data.getString(data.getColumnIndex(Contract.PlaylistEntry.LOCATION));
//            int duration = Integer.parseInt(data.getString(data.getColumnIndex(Contract.PlaylistEntry.DURATION)));
//            int minutes = (duration / 1000) / 60;
//            int seconds = (duration / 1000) % 60;
//            String secondsStr = String.valueOf(seconds);
//            if(seconds < 10)
//                secondsStr = "0" + secondsStr;
//
//            ((TrackViewHolder) holder).duration.setText(String.valueOf(minutes) + ":" + secondsStr);
        }

    }

    @Override
    public int getItemCount() {
        if(data!=null && data.getCount() > 0)
            return data.getCount() + 1; //+header
        else
            return 1 + 1;   //header + empty item
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == data) {
            return null;
        }
        final Cursor oldCursor = data;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        data = newCursor;
        if (data != null) {
            if (mDataSetObserver != null) {
                data.registerDataSetObserver(mDataSetObserver);
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

    public static class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public TextView artistAlbum;
        public TextView duration;
        public String location;
        public TrackViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            artistAlbum = (TextView) v.findViewById(R.id.artist_album);
            duration = (TextView) v.findViewById(R.id.duration);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listItemClick.listItemClick(location);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View v) {
            super(v);
        }
    }

    public interface ListItemClick{
        void listItemClick(String location);
    }
}
