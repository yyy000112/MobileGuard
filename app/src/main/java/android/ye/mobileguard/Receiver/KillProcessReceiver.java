package android.ye.mobileguard.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.ye.mobileguard.engine.ProcessInfoProvider;

/**
 * 小部件启动杀死进程的广播
 */
public class KillProcessReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ProcessInfoProvider.killAll(context);
    }
}
