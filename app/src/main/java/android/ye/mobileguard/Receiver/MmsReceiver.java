package android.ye.mobileguard.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 拦截短信广播必备
 */
public class MmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
            abortBroadcast();
    }
}
