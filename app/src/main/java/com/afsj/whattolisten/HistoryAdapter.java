package com.afsj.whattolisten;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afsj.whattolisten.data.Contract;

/**
 * Created by ilia on 11.04.15.
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private Cursor data;
    private DataSetObserver mDataSetObserver;
    private final int TYPE_HISTORY_ITEM = 1;
    private final int TYPE_EMPTY = 0;

    public HistoryAdapter(Cursor data){
        this.data = data;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (data != null) {
//            this.data.moveToFirst();
            data.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_EMPTY:
                Log.e("adapter","created empty");
                return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_empty, parent, false));
            case TYPE_HISTORY_ITEM:
                Log.e("adapter","created not empty");
                View history_item = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
                return new HistoryViewHolder(history_item);
        }
//TODO add header
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public int getItemViewType(int position) {
        if(data.getCount() > 0)
            return TYPE_HISTORY_ITEM;
        else
            return TYPE_EMPTY;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.e("onBind", String.valueOf(position));
        if(data.moveToPosition(position)) {
            ((HistoryViewHolder) holder).mTextView.setText(data.getString(data.getColumnIndex(Contract.HistoryEntry.QUERY)));
//            data.moveToNext();
        }
        
    }

    @Override
    public int getItemCount() {
        if(data.getCount() > 0)
            return data.getCount();
        else
            return 1;
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

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public HistoryViewHolder(View v) {
            super(v);
            mTextView = ((TextView) v.findViewById(R.id.query));
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View v) {
            super(v);
        }
    }
}
