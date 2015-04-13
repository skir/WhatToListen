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
 * Created by ilia on 12.04.15.
 */
public class ResultsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Cursor data;
    private DataSetObserver mDataSetObserver;
    private final int TYPE_HISTORY_ITEM = 1;
    private final int TYPE_EMPTY = 0;
    private final int TYPE_HEADER = 2;

    public ResultsAdapter(Cursor data){
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
                return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_empty, parent, false));
            case TYPE_HISTORY_ITEM:
                View history_item = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
                return new HistoryViewHolder(history_item);
            case TYPE_HEADER:
                return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false));
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public int getItemViewType(int position) {
        if(position == 0)
            return TYPE_HEADER;
        if(data != null && data.getCount() > 0)
                return TYPE_HISTORY_ITEM;
        return TYPE_EMPTY;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        Log.e("onBind", String.valueOf(position));
        if(position > 0 && data != null && data.moveToPosition(position - 1)) {
            ((HistoryViewHolder) holder).mTextView.setText(data.getString(data.getColumnIndex(Contract.ResultsEntry.NAME)));
//            data.moveToNext();
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
