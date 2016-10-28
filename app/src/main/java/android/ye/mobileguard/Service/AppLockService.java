package android.ye.mobileguard.Service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.ye.mobileguard.Activity.EnterPsdActivity;
import android.ye.mobileguard.db.dao.APPlockDao;
import android.ye.mobileguard.utils.ToastUtil;

import java.util.List;
import java.util.Random;

/**
 * 程序锁服务
 */
public class AppLockService extends Service {

    private boolean isWatch;
    private APPlockDao mAppLockDao;
    private String skipPackageName;
    private List<String> mPackageNameList;
    private InnerReceiver mInnerReceiver;
    private MyContentObserve myContentObserve;

    @Override
    public void onCreate() {
        mAppLockDao = APPlockDao.getInstance(this);
        isWatch = true;
        watch();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SKIP");
        mInnerReceiver = new InnerReceiver();
        registerReceiver(mInnerReceiver,intentFilter);
        //注册一个内容观察者，观察数据库的变化，即时让mPackageNameList更新
        myContentObserve = new MyContentObserve(new Handler());
        getContentResolver().registerContentObserver(Uri.parse("content://applock/change"), true, myContentObserve);
        super.onCreate();
    }

    private void watch() {
        new Thread(){
            //监测现在正在开启的Activity以及任务栈,开启可控死循环
            @Override
            public void run() {
                mPackageNameList = mAppLockDao.findAll();

                while (isWatch){
                   if (Build.VERSION.SDK_INT<21){
                       //获取activity管理者对象
                       ActivityManager aM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                       //获取正在开启的任务栈
                       List<ActivityManager.RunningTaskInfo> runningTasks =aM.getRunningTasks(1);

                       ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
                       //获取栈顶Activity包名
                       String packageName = runningTaskInfo.topActivity.getPackageName();

                       //此包名与已加锁中的包名一致，则开启拦截界面
                       if(mPackageNameList.contains(packageName)){
                           //如果现在检测的程序,以及解锁了,则不需要去弹出拦截界面
                           if(!packageName.equals(skipPackageName)){
                           //7,弹出拦截界面

                           Intent intent = new Intent(getApplicationContext(),EnterPsdActivity.class);
                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                           intent.putExtra("packagename", packageName);
                           startActivity(intent);
                             }
                       }

                       try {
                           Thread.sleep(500);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   } else {
                       String packageName = getRunningApp();
                       if (packageName==null||skipPackageName == null){
                           return;
                       }else {
                           //如果现在检测的程序,以及解锁了,则不需要去弹出拦截界面
                           if(!packageName.equals(skipPackageName)){
                               //7,弹出拦截界面
                                if (!skipPackageName.equals(getPackageName())){
                                    Intent intent = new Intent(getApplicationContext(),EnterPsdActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("packagename", packageName);
                                    startActivity(intent);
                                }

                           }
                       }

                   }

                }

            };
        }.start();
    }


    public String getRunningApp() {
        long ts = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,ts-2000, ts);
            if (queryUsageStats == null || queryUsageStats.isEmpty()) {
                return null;
            }
            UsageStats recentStats = null;
            for (UsageStats usageStats : queryUsageStats) {
                if (recentStats == null ||
                        recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                    recentStats = usageStats;
                }
            }
            return recentStats.getPackageName();
        }else
            return null;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //广播接受者
     class InnerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取发送广播过程中传递过来的包名,跳过次包名检测过程
            skipPackageName = intent.getStringExtra("packagename");
        }
    }
    //内容观察者
    private class MyContentObserve extends ContentObserver{
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContentObserve(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            //更新数据库
            new Thread(){
                @Override
                public void run() {
                    mPackageNameList = mAppLockDao.findAll();
                }
            }.start();
            super.onChange(selfChange);
        }
    }

    @Override
    public void onDestroy() {
        isWatch =false;
        //注销广播接收者
        if (mInnerReceiver!=null){
            unregisterReceiver(mInnerReceiver);
        }
        //注销内容观察者
        if (myContentObserve!=null){
            getContentResolver().unregisterContentObserver(myContentObserve);
        }
        super.onDestroy();
    }
}
