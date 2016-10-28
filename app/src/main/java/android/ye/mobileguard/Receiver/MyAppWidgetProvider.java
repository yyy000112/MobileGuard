package android.ye.mobileguard.Receiver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.style.UpdateAppearance;
import android.ye.mobileguard.Service.UpdateWigetService;

/**
 * 窗口小部件
 */
public class MyAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        //创建第一个窗体小部件的方法
        //开启服务(onCreate)

        super.onEnabled(context);
    }

    @Override
    //"onUpdate 创建多一个窗体小部件调用方法"
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //开启服务
        context.startService(new Intent(context, UpdateWigetService.class));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    //当窗体小部件宽高发生改变的时候调用方法,创建小部件的时候,也调用此方法
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, UpdateWigetService.class));
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    //删除一个窗体小部件调用方法
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }



    @Override
    //删除最后一个窗体小部件调用方法
    public void onDisabled(Context context) {
        context.stopService(new Intent(context, UpdateWigetService.class));
        super.onDisabled(context);
    }


}
