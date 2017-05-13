package ironbear775.com.musicplayer.Util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import ironbear775.com.musicplayer.Activity.BaseActivity;
import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ironbear on 2017/3/2.
 */

public class MusicUtils {

    private Activity activity;
    private static String apiKey = "0c26dc3c5612fd63122ccf5bf11f78f9";
    private static String path = "http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=";
    private static String hashPath = "http://songsearch.kugou.com/song_search_v2?callback=jQuery19102275292550172583_1493445518059&keyword=";
    private static String newPath = "http://www.kugou.com/yy/index.php?r=play/getdata&hash=";

    private static Context mContext;
    private static ImageView mImageView;
    private static Drawable mPlaceHolder;
    public static String localPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String folder = "MusicPlayer/artist";
    public static String lyricFolder = "MusicPlayer/lyric";
    private static File appDir;
    private static OkHttpClient client;
    private static Request.Builder requestBuilder;
    public static boolean enableDownload = true;
    public static boolean enableDefaultCover = false;
    public static boolean enableColorNotification = true;
    public static boolean enableLockscreenNotification = true;
    public static boolean keepScreenOn = false;
    public static boolean loadWebLyric = true;
    public static int filterNum = 0; // 0,1,2,3,4,5,6 0,15s,20s,30s,40s,50s,60s
    public static int launchPage = 1;//1,2,3,4,5 music,artist,album,playlist,recent
    public static int pos = 1;
    public static String messageGood = "good";
    public static String messageBad = "error";
    public static String messageNull = "null";
    public static int[] time = {0, 15000, 20000, 30000, 40000, 50000, 60000};
    private MusicService musicService;

    public MusicUtils(Context context) {
        mContext = context;
        client = new OkHttpClient();
        requestBuilder = null;
    }

    public void startMusic(int position, ArrayList<Music> musicList, int progress) {

        Intent serviceIntent = new Intent(mContext, MusicService.class);

        serviceIntent.setAction("musiclist");
        serviceIntent.putParcelableArrayListExtra("musicList", musicList);
        serviceIntent.putExtra("musicPosition", position);
        serviceIntent.putExtra("musicProgress", progress);

        mContext.startService(serviceIntent);
    }
    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MusicBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    //设置底部栏专辑封面
    public void getFootAlbumArt(int pos1, ArrayList<Music> musicList) {
        String AlbumUri = musicList.get(pos1).getAlbumArtUri();

        Glide.with(mContext)
                .load(AlbumUri)
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.default_album_art_land)
                .into(MusicList.accountHeader.getHeaderBackgroundView());
        Glide.with(mContext)
                .load(AlbumUri)
                .placeholder(R.drawable.default_album_art)
                .into(MusicList.footAlbumArt);
    }

    //生成随机数
    public int createRandom(ArrayList<Music> musicList) {
        Random random = new Random();
        int randomInt;
        if (musicList.size() == 1) {
            randomInt = 0;
        } else {
            randomInt = random.nextInt(musicList.size());
        }
        return randomInt;
    }

    public void shufflePlay(ArrayList<Music> musicList) {
        if (musicList.size() >= 1) {
            int pos = createRandom(musicList);

            startMusic(pos, musicList, 0);
            MusicService.isRandom = true;
            MusicList.footTitle.setText(musicList.get(pos).getTitle());
            MusicList.footArtist.setText(musicList.get(pos).getArtist());
            MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);
            getFootAlbumArt(pos, musicList);

        }
    }

    public void playAll(ArrayList<Music> musicList) {
        startMusic(0, musicList, 0);
        MusicService.isRandom = false;
        MusicList.footTitle.setText(musicList.get(0).getTitle());
        MusicList.footArtist.setText(musicList.get(0).getArtist());
        MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
        getFootAlbumArt(0, musicList);
    }

    public void addOrRemoveItem(int position, Set<Integer> positionSet,
                                RecyclerView.Adapter adapter) {
        if (positionSet.contains(position)) {
            positionSet.remove(position);
        } else {
            positionSet.add(position);
        }
        if (positionSet.size() == 0) {
            MusicList.actionMode.finish();
            adapter.notifyItemChanged(position);
        } else {
            String locale = Locale.getDefault().toString();
            if (locale.equals("zh_CN")) {
                MusicList.actionMode.setTitle(mContext.getResources().getString(R.string.selected) +
                        " " + positionSet.size());
            } else {
                MusicList.actionMode.setTitle(positionSet.size() +
                        " " + mContext.getResources().getString(R.string.selected));
            }
            //更新列表界面，否则无法显示已选的item
            adapter.notifyItemChanged(position);
        }
    }

    public static void saveInfoService(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
        String musicUri;
        musicUri = MusicService.music.getUri();
        for (int i = 0; i < MusicListFragment.musicList.size(); i++) {
            if (MusicListFragment.musicList.get(i).getUri().equals(musicUri)) {
                editor.putInt("position", i);
                break;
            }
        }

        int progress = MusicService.mediaPlayer.getCurrentPosition();
        editor.putInt("progress", progress);
        editor.putBoolean("isRandom", MusicService.isRandom);
        editor.putInt("isSingleOrCycle", MusicService.isSingleOrCycle);

        editor.putInt("flag", 0);
        editor.putBoolean("enable", enableDownload);

        Gson gson = new Gson();
        String json = gson.toJson(MusicListFragment.musicList);
        editor.putString("json",json);

        editor.apply();
        editor.commit();
    }


    public static void artistImage(ImageView imageView, final Context context,
                                   final String keyWord, final Drawable placeHolder,
                                   final Activity uiactivity) {
        if (enableDownload) {
            mImageView = imageView;
            mContext = context;
            mPlaceHolder = placeHolder;

            appDir = new File(localPath, folder);

            String newKeyWord;
            if (keyWord.contains("/")) {
                newKeyWord = keyWord.replace("/", "_");
            } else {
                newKeyWord = keyWord;
            }

            final File file = new File(appDir, newKeyWord);

            if (file.exists() && !isImageGood(file)) {
                file.delete();
            }
            URL url = null;
            try {
                url = new URL(path + keyWord + "&api_key=" + apiKey + "&format=json");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (url != null && !file.exists()) {
                requestBuilder = new Request.Builder()
                        .url(url);
            }
            Call call = null;
            if (requestBuilder != null) {
                call = client.newCall(requestBuilder.build());
            }
            if (call != null) {
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("Fail", "request failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String result = response.body().string();

                            final String ImageUrl = parseJson(result);

                            if (!appDir.exists()) {
                                appDir.mkdirs();
                            }

                            if (ImageUrl != null && !ImageUrl.equals("")) {
                                InputStream is;

                                URL url = new URL(ImageUrl);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                is = new BufferedInputStream(connection.getInputStream());

                                BitmapFactory.Options opt = new BitmapFactory.Options();

                                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                                Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);

                                is.close();

                                if (bitmap != null) {

                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(file);
                                        if (bitmap.getByteCount() > 3000000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fos);
                                        } else if (bitmap.getByteCount() > 2500000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fos);
                                        } else if (bitmap.getByteCount() > 2000000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                                        } else if (bitmap.getByteCount() < 1500000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                                        } else if (bitmap.getByteCount() < 1000000) {
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                                        }
                                        fos.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            if (fos != null) {
                                                fos.close();
                                            }
                                            if (!bitmap.isRecycled()) {
                                                bitmap.recycle();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            uiactivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mImageView.getTag(R.id.artist_url).equals(keyWord)) {
                                        if (file.exists()) {
                                            Glide.with(mContext)
                                                    .load(file)
                                                    .asBitmap()
                                                    .centerCrop()
                                                    .placeholder(mPlaceHolder)
                                                    .into(mImageView);
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } else {
            closeDownloadArtistImage();
        }
    }

    public static void updateArtist(ImageView imageView, final Context context,
                                    final String keyWord, final Drawable placeHolder,
                                    final Activity uiactivity) {
        appDir = new File(localPath, folder);

        String newKeyWord;
        if (keyWord.contains("/")) {
            newKeyWord = keyWord.replace("/", "_");
        } else {
            newKeyWord = keyWord;
        }

        final File file = new File(appDir, newKeyWord);

        if (file.exists()) {
            file.delete();
        }
        artistImage(imageView, context, keyWord, placeHolder, uiactivity);
    }

    public static void closeDownloadArtistImage() {
        client.dispatcher().cancelAll();
    }

    public static void deleteDownloadImage(File root) {
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory()) { // 判断是否为文件夹
                    if (f.exists()) { // 判断是否存在
                        deleteDownloadImage(f);
                        try {
                            f.delete();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    private static String parseJson(String json) {
        String imageUrl = null;
        try {
            JSONObject main = new JSONObject(json);
            if (main.has("artist")) {
                JSONObject artist = main.getJSONObject("artist");

                JSONArray image = artist.getJSONArray("image");
                if (image.length() > 0) {
                    JSONObject finalImage = (JSONObject) image.get(4);
                    imageUrl = finalImage.getString("#text");

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return imageUrl;
    }

    private static List<String> parseLyricJson(String json) {
        List<String> key = new ArrayList<>();
        String j = json.substring(41, json.length() - 1);

        Log.d("json", j + "");

        try {
            JSONObject main = new JSONObject(j);
            if (main.has("data")) {
                JSONObject data = main.getJSONObject("data");

                if (data.has("lists") && !data.get("lists").toString().equals("{}")) {
                    JSONArray lists = data.getJSONArray("lists");
                    if (lists.length() > 0) {

                        JSONObject list = (JSONObject) lists.get(0);

                        if (list.has("Grp") && !list.get("Grp").toString().equals("{}")) {

                            JSONArray grp = list.getJSONArray("Grp");

                            JSONObject object = grp.getJSONObject(0);

                            String album = null;
                            if (object.getString("AlbumID").equals("")) {
                                album = " ";
                            } else {
                                album = list.getString("AlbumID");
                            }
                            key.add(0, album);
                            key.add(1, object.getString("FileHash"));
                        } else if (list.has("AlbumID") && list.has("FileHash")) {
                            String album = null;
                            if (list.getString("AlbumID").equals("")) {
                                album = " ";
                            } else {
                                album = list.getString("AlbumID");
                            }
                            key.add(0, album);
                            key.add(1, list.getString("FileHash"));
                        }
                    }
                }else {
                    key.add(0, "");
                    key.add(1, "");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return key;
    }

    public static boolean haveWIFI(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isImageGood(File file) {
        BitmapFactory.Options options;
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getPath(), options); //filePath代表图片路径
        if (options.mCancel || options.outWidth == -1
                || options.outHeight == -1) {
            //表示图片已损毁
            return false;
        }
        return true;
    }


    public static void getWebLyric(String songTitle, String singer, int duration) {
        OkHttpClient client = new OkHttpClient();

        try {

            URL u = new URL(hashPath + songTitle + " " + singer
                    + "&page=1&pagesize=30&userid=-1&clientver=&platform=WebFilter&tag=em&filter=2&iscorrection=1&privilege_filter=0&_=1493445518061");
            Request.Builder builder = new Request.Builder().url(u);
            Call hashcall = client.newCall(builder.build());
            hashcall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    MusicList.lyricView.setLabel(BaseActivity.myContext.getResources()
                            .getString(R.string.no_lyric));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();

                    String hashkey = null;
                    String album_id = null;

                    List<String> key = parseLyricJson(result);

                    if (key.size() > 0) {
                        album_id = key.get(0);
                        hashkey = key.get(1);

                        if (album_id!=null && hashkey !=null) {

                            URL url = new URL(newPath + hashkey + "&album_id=" + album_id);
                            Request.Builder builder = new Request.Builder().url(url);
                            Call call2 = client.newCall(builder.build());
                            call2.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    MusicList.lyricView.setLabel(BaseActivity.myContext.getResources()
                                            .getString(R.string.no_lyric));
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        String webLyric = response.body().string();

                                        String lyric = null;
                                        try {
                                            JSONObject main = new JSONObject(webLyric);
                                            if (main.has("data") && !main.get("data").toString().equals("[]")) {
                                                JSONObject data = main.getJSONObject("data");

                                                if (data.has("lyrics") && !data.get("lyrics").toString().equals("")) {
                                                    lyric = data.getString("lyrics");

                                                    if (lyric != null) {

                                                        MusicList.lyricView.loadLrc(lyric);
                                                        MusicList.handler.post(MusicList.runnable1);

                                                        File dir = new File(localPath, lyricFolder);

                                                        if (!dir.exists()) {
                                                            dir.mkdirs();
                                                        }

                                                        String newSongTitle,newSinger;
                                                        if (songTitle.contains("/")) {
                                                            newSongTitle = songTitle.replace("/", "_");
                                                        } else {
                                                            newSongTitle = songTitle;
                                                        }
                                                        if (singer.contains("/")) {
                                                            newSinger = singer.replace("/", "_");
                                                        } else {
                                                            newSinger = singer;
                                                        }

                                                        File file = new File(dir, newSongTitle+"_"+newSinger+".lrc");
                                                        FileOutputStream fos = new FileOutputStream(file);
                                                        fos.write(lyric.getBytes());
                                                        fos.close();

                                                    } else {
                                                        MusicList.lyricView.loadLrc("");
                                                        MusicList.lyricView.setLabel(
                                                                BaseActivity.myContext.getResources()
                                                                        .getString(R.string.no_lyric));
                                                    }
                                                } else {
                                                    MusicList.lyricView.loadLrc("");
                                                    MusicList.lyricView.setLabel(
                                                            BaseActivity.myContext.getResources()
                                                                    .getString(R.string.no_lyric));
                                                }

                                            } else {
                                                MusicList.lyricView.loadLrc("");
                                                MusicList.lyricView.setLabel(
                                                        BaseActivity.myContext.getResources()
                                                                .getString(R.string.no_lyric));
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            });
                        }else {
                            MusicList.lyricView.loadLrc("");
                            MusicList.lyricView.setLabel(
                                    BaseActivity.myContext.getResources()
                                            .getString(R.string.no_lyric));
                        }
                    }
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


}
