package ironbear775.com.musicplayer;

import android.app.Activity;
import android.app.Application;

import com.umeng.commonsdk.UMConfigure;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ironbear on 2017/7/8.
 */

public class MyApplication extends Application {
    private List<Activity> oList;//用于存放所有启动的Activity的集合

    @Override
    public void onCreate() {
        super.onCreate();
        oList = new ArrayList<>();

        UMConfigure.init(this, "5af7b675a40fa375ab0000de", "CoolApk", UMConfigure.DEVICE_TYPE_PHONE, null);
        UMConfigure.setLogEnabled(true);
        UMConfigure.setEncryptEnabled(true);
    }

    /**
     * 添加Activity
     */
    public void addActivity(Activity activity) {
    // 判断当前集合中不存在该Activity
        if (!oList.contains(activity)) {
            oList.add(activity);//把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    public void removeActivity(Activity activity) {
    //判断当前集合中存在该Activity
        if (oList.contains(activity)) {
            oList.remove(activity);//从集合中移除
            activity.finish();//销毁当前Activity
        }
    }

    /**
     * 销毁所有的Activity
     */
    public void removeALLActivity() {
        //通过循环，把集合中的所有Activity销毁
        for (Activity activity : oList) {
            activity.finish();
        }
    }

}

