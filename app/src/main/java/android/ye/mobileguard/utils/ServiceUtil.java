package android.ye.mobileguard.utils;


import android.app.ActivityManager;
import android.content.Context;

import java.util.List;


public class ServiceUtil {
    public static boolean isRunning(Context ctx,String serviceName){
        //获取Activitymanager 管理者对象，去获取当前手机运行的服务
        ActivityManager mAm = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        //获取手机中正在运行的服务集合
        List<ActivityManager.RunningServiceInfo> runningServices =mAm.getRunningServices(255);
        //遍历获取所有的服务集合，拿到服务名和传递进来的名称做对比
        for(ActivityManager.RunningServiceInfo runningServiceInfo:runningServices){
            //获取正在运行的服务名
            if (serviceName.equals(runningServiceInfo.service.getClassName())){
                return true;
            }

        }
        return false;
    }
}
