package com.afsj.whattolisten.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afsj.whattolisten.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ilia on 14.04.15.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private JSONArray data;
    private Context mContext;
    private final int TYPE_ALBUM = 0;

    public AlbumsAdapter(Context context,String array){
        try {
            data = new JSONArray(array);
        }catch (JSONException e){
            Log.e("JSONExeption",e.toString());
        }
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_ALBUM:
                return new AlbumViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_item,parent,false));
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public int getItemViewType(int position) {
        return TYPE_ALBUM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(data != null) {
            try {
                JSONObject item = data.getJSONObject(position);
                ((AlbumViewHolder) holder).artist.setText(item.getJSONObject("artist").getString("name"));
                ((AlbumViewHolder) holder).album.setText(item.getString("name"));
                Picasso.with(mContext)
                        .load(item.getJSONArray("image").getJSONObject(0).getString("#text"))
                        .into(((AlbumViewHolder) holder).image);

            }catch (JSONException e){
                Log.e("JSONException",e.toString());
            }
        }

    }

    @Override
    public int getItemCount() {
        return data.length();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder  {
        public TextView album;
        public TextView artist;
        public ImageView image;
        public AlbumViewHolder(View v) {
            super(v);
            album = (TextView) v.findViewById(R.id.album);
            artist = (TextView) v.findViewById(R.id.artist);
            image = (ImageView) v.findViewById(R.id.image);
        }
    }
}
