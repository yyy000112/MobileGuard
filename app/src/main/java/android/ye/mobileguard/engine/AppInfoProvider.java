package android.ye.mobileguard.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.ye.mobileguard.db.domain.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用条目
 */
public class AppInfoProvider {


    /**
     * 返回手机所有应用当前的相关信息（名称，包名，图标，（内存，SD）以及用户，系统
     * @param ctx 获取包管理者的上下文环境
     */
    public static List<AppInfo> getAppInfoList(Context ctx){
        PackageManager mPackageManager = ctx.getPackageManager();
        //获取安装于手机的应用信息集合
        List<PackageInfo> packageInfos = mPackageManager.getInstalledPackages(0);
        List<AppInfo> appInfoList = new ArrayList<AppInfo>();
        //遍历集合
        for (PackageInfo packageInfo:packageInfos){
            AppInfo appInfo = new AppInfo();
            //获取包名
            appInfo.packName = packageInfo.packageName;
            //获取名称
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
           appInfo.name =  applicationInfo.loadLabel(mPackageManager).toString();
            //获取图标
            appInfo.icon = applicationInfo.loadIcon(mPackageManager);
            //判断是用户还是系统
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM){
               //系统应用
                appInfo.isSys = true;
            }else {
                //非系统应用
                appInfo.isSys = false;
            }
            //判断是否为SD卡中的应用
            if ((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE){
                //系统应用
                appInfo.isSD = true;
            }else {
                //非系统应用
                appInfo.isSD = false;
            }
            appInfoList.add(appInfo);
        }
        return appInfoList;
    }
}
