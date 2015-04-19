package com.afsj.whattolisten.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afsj.whattolisten.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ilia on 14.04.15.
 */
public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private JSONArray data;
    private Context mContext;
    private boolean isAlbums;
    private static AlbumItemClick albumItemClick;
    private static ArtistItemClick artistItemClick;
    private final int TYPE_ALBUM = 0;
    private final int TYPE_ARTIST = 1;

    public CardAdapter(boolean isAlbums, Context context, String array){
        try {
            data = new JSONArray(array);
        }catch (JSONException e){
            Log.e("JSONExeption",e.toString());
        }
        mContext = context;
        this.isAlbums = isAlbums;
    }

    public void setAlbumItemClick(AlbumItemClick c){
        albumItemClick = c;
    }

    public void setArtistItemClick(ArtistItemClick c){
        artistItemClick = c;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_ALBUM:
                return new AlbumViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_item,parent,false));
            case TYPE_ARTIST:
                return new ArtistViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_artist_item,parent,false));
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public int getItemViewType(int position) {
        if(isAlbums) return TYPE_ALBUM;
        else return TYPE_ARTIST;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(data != null) {
            try {
                if(isAlbums) {
                    JSONObject item = data.getJSONObject(position);
                    ((AlbumViewHolder) holder).artist.setText(item.getJSONObject("artist").getString("name"));
                    ((AlbumViewHolder) holder).album.setText(item.getString("name"));
                    Picasso.with(mContext)
                            .load(item.getJSONArray("image").getJSONObject(1).getString("#text"))
                            .into(((AlbumViewHolder) holder).image);

                    ((AlbumViewHolder) holder).mbid = item.getString("mbid");
                }else{
                    JSONObject item = data.getJSONObject(position);
                    ((ArtistViewHolder) holder).artist.setText(item.getString("name"));
                    Picasso.with(mContext)
                            .load(item.getJSONArray("image").getJSONObject(1).getString("#text"))
                            .into(((ArtistViewHolder) holder).image);
                    ((ArtistViewHolder) holder).mbid = item.getString("mbid");
                }
            }catch (JSONException e){
                Log.e("JSONException",e.toString());
            }
        }

    }

    @Override
    public int getItemCount() {
        return data.length();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        public TextView album;
        public TextView artist;
        public ImageView image;
        public String mbid;
        public AlbumViewHolder(View v) {
            super(v);
            album = (TextView) v.findViewById(R.id.album);
            artist = (TextView) v.findViewById(R.id.artist);
            image = (ImageView) v.findViewById(R.id.image);
            ((LinearLayout) v.findViewById(R.id.item)).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(!mbid.equals(""))
                albumItemClick.albumItemClick(mbid);
        }
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView artist;
        public ImageView image;
        public String mbid;
        public ArtistViewHolder(View v){
            super(v);
            artist = (TextView) v.findViewById(R.id.artist);
            image = (ImageView) v.findViewById(R.id.image);
            ((LinearLayout) v.findViewById(R.id.item)).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(!mbid.equals(""))
            artistItemClick.artistItemClick(mbid);
        }
    }

    public interface AlbumItemClick{
        public void albumItemClick(String mbid);
    }

    public interface ArtistItemClick{
        public void artistItemClick(String mbid);
    }
}
