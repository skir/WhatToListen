package com.afsj.whattolisten.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afsj.whattolisten.DividerItemDecoration;
import com.afsj.whattolisten.ImageTransformation;
import com.afsj.whattolisten.R;
import com.afsj.whattolisten.Utils;
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
    private static String tag;
    private DataSetObserver mDataSetObserver;
    private static PlayClick playClick;
    private static TagCardItemClick tagCardItemClick;
    private final int TYPE_HEADER_INFO = 0;
    public static final int TYPE_ALBUMS = 1;
    public static final int TYPE_ARTISTS = 2;
    private final int TYPE_HEADER_A = 3;
    private final int TYPE_CARD_TEXT = 4;
    private final int TYPE_TAGS = 5;
    private final int TYPE_TRACK_LIST = 6;
    private int type;

    public TagAdapter(Context context,Cursor info,int windowWidth,String tag,int type){
        this.info = info;
        mContext = context;
        this.windowWidth = windowWidth;
        this.tag = tag;
        this.type = type;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (info != null) {
//            this.data.moveToFirst();
            info.registerDataSetObserver(mDataSetObserver);
        }
    }

    public void setPlayClick(PlayClick playClick){
        this.playClick = playClick;
    }

    public void setTagCardItemClick(TagCardItemClick t){
        this.tagCardItemClick = t;
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
            case TYPE_HEADER_A:
                return new HeaderAViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_artist,parent,false));
            case TYPE_CARD_TEXT:
                return new CardTextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_bio,parent,false));
            case TYPE_TRACK_LIST:
                return new CardTrackListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_card,parent,false));
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public int getItemViewType(int position) {
        if(type == Utils.TYPE_INFO)
            switch (position){
                case 0:
                    return TYPE_HEADER_INFO;
                case 1:
                    return TYPE_ALBUMS;
                case 2:
                    return TYPE_ARTISTS;
            }
        if(type == Utils.TYPE_ARTIST)
            switch (position){
                case 0:
                    return TYPE_HEADER_A;
                case 1:
                    return TYPE_CARD_TEXT;
                case 2:
                    return TYPE_ARTISTS;
            }
        if(type == Utils.TYPE_ALBUM)
            switch (position){
                case 0:
                    return TYPE_HEADER_A;
                case 1:
                    return TYPE_CARD_TEXT;
                case 2:
                    return TYPE_TRACK_LIST;
            }

        return TYPE_HEADER_INFO;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(info != null && info.getCount() > 0) {
            info.moveToFirst();
            switch (getItemViewType(position)) {
                case TYPE_HEADER_INFO:
                    ((InfoViewHolder) holder).info.setText(Html.fromHtml(info.getString(info.getColumnIndex(Contract.InfoEntry.SUMMARY))));
                    try {
                        JSONArray array = new JSONArray(info.getString(info.getColumnIndex(Contract.InfoEntry.ARTISTS)));
                        JSONObject item = array.getJSONObject(0);
                        Picasso.with(mContext)
                                .load(item.getJSONArray("image").getJSONObject(4).getString("#text"))
                                .transform(new ImageTransformation())
                                .into(((InfoViewHolder) holder).image);
                    }catch (JSONException e){
                        Log.e("JSONException",e.toString());
                    }
                    break;
                case TYPE_ALBUMS:
                    CardAdapter adapterAlbum = new CardAdapter(true,mContext,info.getString(info.getColumnIndex(Contract.InfoEntry.ALBUMS)));
                    adapterAlbum.setAlbumItemClick(new CardAdapter.AlbumItemClick() {
                        @Override
                        public void albumItemClick(String mbid) {
                            tagCardItemClick.tagCardItemClick(mbid, TYPE_ALBUMS);
                        }
                    });
                    ((CardViewHolder) holder).recyclerView.setAdapter(adapterAlbum);
                    ((CardViewHolder) holder).title.setText(mContext.getString(R.string.albums));
                    break;
                case TYPE_ARTISTS:
                    CardAdapter adapterArtist;
                    if(type == Utils.TYPE_INFO) {
                        ((CardViewHolder) holder).title.setText(mContext.getString(R.string.artists));
                        adapterArtist = new CardAdapter(false, mContext, info.getString(info.getColumnIndex(Contract.InfoEntry.ARTISTS)));
                    }else {
                        ((CardViewHolder) holder).title.setText(mContext.getString(R.string.similar_artist));
                        adapterArtist = new CardAdapter(false, mContext, info.getString(info.getColumnIndex(Contract.ArtistEntry.SIMILAR)));
                    }
                    adapterArtist.setArtistItemClick(new CardAdapter.ArtistItemClick() {
                        @Override
                        public void artistItemClick(String mbid) {
                            tagCardItemClick.tagCardItemClick(mbid, TYPE_ARTISTS);
                        }
                    });
                    ((CardViewHolder) holder).recyclerView.setAdapter(adapterArtist);
                    break;
                case TYPE_HEADER_A:
                    ((HeaderAViewHolder) holder).name.setText(info.getString(info.getColumnIndex(Contract.ArtistEntry.NAME)));
                    if(type == Utils.TYPE_ALBUM)
                        ((HeaderAViewHolder) holder).name2.setText(info.getString(info.getColumnIndex(Contract.AlbumEntry.ARTIST)));

                    try {
                        JSONArray array = new JSONArray(info.getString(info.getColumnIndex(Contract.ArtistEntry.IMAGE)));
                        JSONObject item = array.getJSONObject(2);
                        Picasso.with(mContext)
                                .load(item.getString("#text"))
                                .into(((HeaderAViewHolder) holder).image);
                    }catch (JSONException e){
                        Log.e("JSONException",e.toString());
                    }
                    break;
                case TYPE_CARD_TEXT:
                    try {
                        if(type == Utils.TYPE_ARTIST) {
                            ((CardTextViewHolder) holder).title.setText(mContext.getString(R.string.bio));
                            JSONObject bio = new JSONObject(info.getString(info.getColumnIndex(Contract.ArtistEntry.BIO)));
                            ((CardTextViewHolder) holder).info.setText(Html.fromHtml(bio.getString("summary")));
                        }else {
                            ((CardTextViewHolder) holder).title.setText(mContext.getString(R.string.wiki));
                            JSONObject wiki = new JSONObject(info.getString(info.getColumnIndex(Contract.AlbumEntry.WIKI)));
                            ((CardTextViewHolder) holder).info.setText(Html.fromHtml(wiki.getString("summary")));
                        }
                    }catch (JSONException e){
                        Log.e("JSONException",e.toString());
                    }
                    break;
                case TYPE_TRACK_LIST:
                    TrackListAdapter adapter = new TrackListAdapter(mContext,info.getString(info.getColumnIndex(Contract.AlbumEntry.TRACK_LIST)));
                    ((CardTrackListViewHolder) holder).recyclerView.setAdapter(adapter);
                    ((CardTrackListViewHolder) holder).title.setText(mContext.getString(R.string.track_list));
                    try {
                        JSONArray data = new JSONArray(info.getString(info.getColumnIndex(Contract.AlbumEntry.TRACK_LIST)));
                        ((CardTrackListViewHolder) holder).cardView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                ((CardTrackListViewHolder) holder).recyclerView.getLayoutParams().height = data.length() * (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 57, mContext.getResources().getDisplayMetrics());
                    }catch(JSONException e){
                        Log.e("JSONException",e.toString());
                    }
                    break;
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
        public TextView info;
        public ImageView image;
        public InfoViewHolder(View v) {
            super(v);
            info = (TextView) v.findViewById(R.id.info);
            info.setMovementMethod(LinkMovementMethod.getInstance());
            image = (ImageView) v.findViewById(R.id.image);
            v.findViewById(R.id.layout).getLayoutParams().height = 2 * windowWidth / 3 + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
            v.findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playClick.playClick();
                }
            });
        }
    }

    public static class HeaderAViewHolder extends RecyclerView.ViewHolder  {
        public TextView name;
        public TextView name2;
        public ImageView image;
        public HeaderAViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.name);
            name2 = (TextView) v.findViewById(R.id.name2);
            image = (ImageView) v.findViewById(R.id.image);
            v.findViewById(R.id.layout).getLayoutParams().height = 2 * windowWidth / 3;
            v.findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playClick.playClick();
                }
            });
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

    public static class CardTextViewHolder extends RecyclerView.ViewHolder{
        public TextView info;
        public TextView title;
        public CardTextViewHolder(View v){
            super(v);
            info = (TextView) v.findViewById(R.id.info);
            info.setMovementMethod(LinkMovementMethod.getInstance());
            title = (TextView) v.findViewById(R.id.title);
        }
    }

    public static class CardTrackListViewHolder extends RecyclerView.ViewHolder{
        public RecyclerView recyclerView;
        public TextView title;
        public CardView cardView;
        public CardTrackListViewHolder(View v){
            super(v);
            recyclerView = (RecyclerView) v.findViewById(R.id.albums);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));

            title = (TextView) v.findViewById(R.id.title);

            cardView = (CardView) v.findViewById(R.id.card_view);
        }
    }

    public interface PlayClick{
        void playClick();
    }

    public interface TagCardItemClick{
        void tagCardItemClick(String mbid, int type);
    }
}
