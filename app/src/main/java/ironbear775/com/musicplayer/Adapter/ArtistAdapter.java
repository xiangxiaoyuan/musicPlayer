package ironbear775.com.musicplayer.Adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.ArtistListFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/1/26.
 */

public class ArtistAdapter extends RecyclerView.Adapter<ArtistViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private final LayoutInflater mInflater;
    private final ArrayList<Music> mList;
    private boolean isClickable = true;
    private final int[] mColor;
    private Context mContext;
    private File dir;

    @NonNull
    @Override
    public String getSectionName(int position) {
        return mList.get(position).getArtist().substring(0, 1).toUpperCase(Locale.ENGLISH);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private ArtistAdapter.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(ArtistAdapter.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setClickable(boolean click) {
        this.isClickable = click;
    }

    public ArtistAdapter(Context context, ArrayList<Music> list) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String newFolder = "MusicPlayer/artist";
        dir = new File(path,newFolder);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        mColor = new int[mList.size()];
        for (int i = 0; i < mColor.length; i++) {
            mColor[i] = generator.getRandomColor();
        }
    }


    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.artist_item_layout, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ArtistViewHolder holder, final int position) {

        holder.setData(position);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isClickable) {
                        mOnItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(holder.itemView, position);
                    return false;
                }
            });
        }

        TextDrawable drawable = TextDrawable.builder()
                .buildRoundRect(mList.get(position).getArtist().substring(0, 1), mColor[position], 5);

        holder.iv.setTag(R.id.artist_url, mList.get(position).getArtist());

        String newKeyWord;
        if (mList.get(position).getArtist().contains("/")) {
            newKeyWord = mList.get(position).getArtist().replace("/", "_");
        }else {
            newKeyWord = mList.get(position).getArtist();
        }

        File file1 = new File(dir,newKeyWord);

        if (MusicUtils.enableDownload) {
            if (file1.exists() && MusicUtils.isImageGood(file1)){
                Glide.with(mContext)
                        .load(file1)
                        .asBitmap()
                        .placeholder(drawable)
                        .into(holder.iv);
            }else {
                holder.iv.setImageDrawable(drawable);
                if (MusicUtils.haveWIFI(mContext)) {
                    MusicUtils.artistImage(holder.iv, mContext, mList.get(position).getArtist(),
                            drawable, (Activity) mContext);
                }
            }
        }else {
            if (file1.exists() && MusicUtils.isImageGood(file1)){
                Glide.with(mContext)
                        .load(file1)
                        .asBitmap()
                        .placeholder(drawable)
                        .into(holder.iv);
            }else {
                holder.iv.setImageDrawable(drawable);
            }
        }
        holder.tv_title.setText(mList.get(position).getArtist());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


}


class ArtistViewHolder extends RecyclerView.ViewHolder {
    final ImageView iv;
    final TextView tv_title;

    ArtistViewHolder(View itemView) {
        super(itemView);
        iv = (ImageView) itemView.findViewById(R.id.artist_iv_image);
        tv_title = (TextView) itemView.findViewById(R.id.artist_tv_title);
    }

    public void setData(int position) {
        Set<Integer> positionSet = ArtistListFragment.positionSet;
        if (MusicUtils.isSelectAll){
            positionSet = MusicList.listPositionSet;
        }
        if (positionSet.contains(position)) {
            itemView.setBackgroundResource(R.color.items_selected_bg_color);
        } else {
            itemView.setBackgroundResource(R.color.listView_bg_color);
        }
    }
}