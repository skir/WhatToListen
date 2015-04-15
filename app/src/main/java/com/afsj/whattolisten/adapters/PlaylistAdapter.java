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

    public PlaylistAdapter(Context context,Cursor data){
        this.data = data;
        mContext = context;
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
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public int getItemViewType(int position) {
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
        public ImageView cover;
        public String location;
        public TrackViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            artistAlbum = (TextView) v.findViewById(R.id.artist_album);
            cover = (ImageView) v.findViewById(R.id.album_cover);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listItemClick.listItemClick(location);
        }
    }

    public interface ListItemClick{
        public void listItemClick(String location);
    }
}
