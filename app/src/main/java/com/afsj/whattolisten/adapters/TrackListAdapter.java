package com.afsj.whattolisten.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afsj.whattolisten.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ilia on 19.04.15.
 */
public class TrackListAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private JSONArray data;
    private static Context mContext;
    private final int TYPE_TRACK = 0;

    public TrackListAdapter(Context context, String array){
        try {
            data = new JSONArray(array);
        }catch (JSONException e){
            Log.e("JSONExeption", e.toString());
        }
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_TRACK)
                return new TrackViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.track,parent,false));

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public int getItemViewType(int position) {
        return TYPE_TRACK;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(data != null) {
            try {
                JSONObject item = data.getJSONObject(position);
                ((TrackViewHolder) holder).title.setText(item.getString("name"));
                int duration = Integer.parseInt(item.getString("duration"));
                int minutes = duration / 60;
                int seconds = duration % 60;
                String secondsStr = String.valueOf(seconds);
                if(seconds < 10)
                    secondsStr = "0" + secondsStr;

                ((TrackViewHolder) holder).duration.setText(String.valueOf(minutes) + ":" + secondsStr);
                ((TrackViewHolder) holder).number.setText(String.valueOf(position + 1));
            }catch (JSONException e){
                Log.e("JSONException",e.toString());
            }
        }

    }

    @Override
    public int getItemCount() {
        return data.length();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView duration;
        public TextView number;
        public TrackViewHolder(View v){
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            duration = (TextView) v.findViewById(R.id.duration);
            number = (TextView) v.findViewById(R.id.number);
        }
    }
}
