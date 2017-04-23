package ironbear775.com.musicplayer.Fragment;


import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Adapter.MusicAdapter;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/1/24.
 */

public class MusicRecentAddedFragment extends android.app.Fragment {
    public static int count = 0;
    public static final Set<Integer> positionSet = new HashSet<>();
    public static final ArrayList<Music> musicList = new ArrayList<>();
    public static int pos = 0;
    private MusicAdapter musicAdapter;
    private com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView musicView;
    private MusicUtils musicUtils;
    private Toolbar toolbar;
    private RelativeLayout shuffle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shuffle_item_layout, container, false);
        findView(view);

        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        MusicList.toolbar.setVisibility(View.GONE);
        toolbar.setTitle(R.string.toolbar_title_recent_added);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);

        IntentFilter filter = new IntentFilter();
        filter.addAction("SetClickable_False");
        filter.addAction("SetClickable_True");
        filter.addAction("notifyDataSetChanged");
        getActivity().registerReceiver(clickableReceiver, filter);

        musicList.clear();
        readMusic(getActivity());

        musicUtils = new MusicUtils(getActivity());

        initView();

        if (MusicUtils.launchPage == 5) {
            if (MusicService.mediaPlayer.isPlaying()) {
                MusicList.footTitle.setText(MusicService.music.getTitle());
                MusicList.footArtist.setText(MusicService.music.getArtist());
                MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);
                Glide.with(this)
                        .load(MusicService.music.getAlbumArtUri())
                        .asBitmap()
                        .placeholder(R.drawable.default_album_art)
                        .into(MusicList.footAlbumArt);
                Glide.with(this)
                        .load(MusicService.music.getAlbumArtUri())
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.default_album_art)
                        .into(MusicList.accountHeader.getHeaderBackgroundView());
            } else {
                MusicListFragment.readMusic(getActivity());
                MusicList.footTitle.setText(MusicListFragment.musicList.get(MusicUtils.pos).getTitle());
                MusicList.footArtist.setText(MusicListFragment.musicList.get(MusicUtils.pos).getArtist());
                MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
                musicUtils.getFootAlbumArt(MusicUtils.pos, MusicListFragment.musicList);
            }
        }

        return view;
    }

    private void initView() {
        musicView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        musicView.setLayoutManager(layoutManager);

        musicView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .color(Color.parseColor("#22616161"))
                .sizeResId(R.dimen.divider)
                .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                .build());

        musicAdapter = new MusicAdapter(getActivity(), musicList);
        musicView.setAdapter(musicAdapter);

        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (MusicList.actionMode != null) {
                    //多选状态
                    musicUtils.addOrRemoveItem(position, positionSet, musicAdapter);
                } else {
                    setClickAction(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (MusicList.actionMode == null) {
                    Intent intent = new Intent("ActionModeChanged");
                    getActivity().sendBroadcast(intent);
                }
            }
        });

        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtils.shufflePlay(musicList);
            }
        });
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MusicList.slideDrawer.openDrawer();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setClickAction(int position) {

        count = 1;
        MusicListFragment.count = 0;
        AlbumDetailFragment.count = 0;
        ArtistDetailFragment.count = 0;
        PlaylistDetailFragment.count = 0;

        int progress = 0;

        musicUtils.startMusic(position, musicList, progress);

        MusicList.footTitle.setText(musicList.get(position).getTitle());
        MusicList.footArtist.setText(musicList.get(position).getArtist());
        MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);

        musicUtils.getFootAlbumArt(position, musicList);

        pos = position;

    }

    private void findView(View view) {
        shuffle = (RelativeLayout) view.findViewById(R.id.shuffle);
        toolbar = (Toolbar) view.findViewById(R.id.music_toolbar);
        musicView = (FastScrollRecyclerView) view.findViewById(R.id.music_list);
    }

    //获取音乐各种信息
    private void readMusic(Context context) {

        Cursor cursor;
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DATE_ADDED);
        if (cursor != null) {
            if (cursor.moveToLast()) {
                do {
                    if (cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC) == 17) {
                        Music music = new Music();
                        music.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        music.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                        music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart")
                                , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                        music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                        music.setAlbum_id(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                        music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                        music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                        music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));

                        if (!music.getUri().contains(".wmv")) {
                            if (music.getDuration() >= MusicUtils.time[MusicUtils.filterNum])
                                musicList.add(music);
                        }
                    }
                } while (cursor.moveToPrevious());
                cursor.close();
            }
        }
    }

    private final BroadcastReceiver clickableReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "SetClickable_True":
                    musicAdapter.setClickable(true);
                    break;
                case "SetClickable_False":
                    musicAdapter.setClickable(false);
                    break;
                case "notifyDataSetChanged":
                    musicAdapter.notifyDataSetChanged();
                    musicList.clear();
                    readMusic(context);
                    break;
            }
        }
    };

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(clickableReceiver);
        super.onDestroyView();
    }
}
