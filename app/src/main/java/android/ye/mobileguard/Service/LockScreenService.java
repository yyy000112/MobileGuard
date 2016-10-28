package android.ye.mobileguard.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.ye.mobileguard.db.domain.ProcessInfo;
import android.ye.mobileguard.engine.ProcessInfoProvider;

/**
 * 锁屏时清理进程的服务
 */
public class LockScreenService extends Service {

    private InnerReceiver innerReceiver;

    @Override
    public void onCreate() {
        //锁屏action
        IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        innerReceiver = new InnerReceiver();
        registerReceiver(innerReceiver,mIntentFilter);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (innerReceiver!=null){
            unregisterReceiver(innerReceiver);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class InnerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ProcessInfo processInfo = new ProcessInfo();
            ProcessInfoProvider.killAll(getApplicationContext());
        }
    }
}
