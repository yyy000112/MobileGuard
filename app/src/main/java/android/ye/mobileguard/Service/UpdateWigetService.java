package android.ye.mobileguard.Service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.widget.RemoteViews;
import android.ye.mobileguard.R;
import android.ye.mobileguard.Receiver.MyAppWidgetProvider;
import android.ye.mobileguard.db.domain.ProcessInfo;
import android.ye.mobileguard.engine.ProcessInfoProvider;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 更新小部件服务
 */
public class UpdateWigetService extends Service {

    private InnerReceiver innerReceiver;
    private Timer mTimer;

    @Override
    public void onCreate() {
        //管理进程总数和可用内存更新
        startTimer();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        innerReceiver = new InnerReceiver();
        registerReceiver(innerReceiver,mIntentFilter);
        super.onCreate();
    }

    private class InnerReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                startTimer();
            }else {
                cancelTimer();
            }

        }
    }

    private void cancelTimer() {
        if (mTimer!=null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //ui定时刷新
                updateAppWiget();
            }
        }, 0, 5000);
    }

    private void updateAppWiget() {
        //获取AppWidget对象
        AppWidgetManager aWm= AppWidgetManager.getInstance(this);

        RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.process_widget);
        //给窗体小部件内部内部空间赋值
        remoteViews.setTextViewText(R.id.tv_process_count,"进程总数:"+ ProcessInfoProvider.getProcessCount(this));
        //显示可用内存大小
        String memorySpace = Formatter.formatFileSize(this,ProcessInfoProvider.getAvailableSpace(this));
        remoteViews.setTextViewText(R.id.tv_process_memory, "可用内存:" + memorySpace);

        //点击小部件进入应用
        Intent intent = new Intent("android.intent.action.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        //启用延期意图启动并发送广播，杀死进程
        Intent broadcastIntent = new Intent("android.intent.action.KILL_BACKGROUND_PROCESS");
        PendingIntent broadcast = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_clear,broadcast);
        //窗体小部件对应广播接受者的字节码文件
        ComponentName componentName = new ComponentName(this, MyAppWidgetProvider.class);
        //更新窗体小部件
        aWm.updateAppWidget(componentName, remoteViews);


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (innerReceiver!=null){
            unregisterReceiver(innerReceiver);
        }
        //关闭服务的方法在移除最后一个窗体小部件的时调用,关闭定时任务
        cancelTimer();
        super.onDestroy();
    }
}
