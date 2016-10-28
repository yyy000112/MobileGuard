package android.ye.mobileguard.engine;



import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Debug;
import android.ye.mobileguard.R;
import android.ye.mobileguard.db.domain.ProcessInfo;


import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;


import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.ActivityManager.*;

/**
 * 进程条目
 */
public class ProcessInfoProvider {


    private static ActivityManager am;
    private static ProcessInfo processInfo;

    /**
     * 返回手机所有进程当前的相关信息（名称，包名，图标，（内存）以及用户，系统
     * @param ctx 获取包管理者的上下文环境
     */
    public static List<ProcessInfo> getProcessInfoList(Context ctx){
        if (Build.VERSION.SDK_INT>=21){
            List<ProcessInfo> processInfoList = new ArrayList<ProcessInfo>();
            ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            PackageManager pm = ctx.getPackageManager();
            List<AndroidAppProcess> listInfo = ProcessManager.getRunningAppProcesses();
            if(listInfo.isEmpty() || listInfo.size() == 0){
                return null;
            }
            for (AndroidAppProcess info : listInfo) {

                processInfo = new ProcessInfo();
                //获取包名
                processInfo.packageName = info.getPackageName();
                //获取进程占用的内存大小
                Debug.MemoryInfo[] processMemInfo = am.getProcessMemoryInfo(new int[]{info.pid});
                Debug.MemoryInfo memoryInfo = processMemInfo[0];
                //获取已经使用的大小
                processInfo.memSize = memoryInfo.getTotalPrivateDirty()*1024;
                //获取名称

                try {
                    ApplicationInfo applicationInfo = pm.getApplicationInfo(processInfo.getPackageName(),0);
                    processInfo.name =  applicationInfo.loadLabel(pm).toString();
                    //获取图标
                    processInfo.icon = applicationInfo.loadIcon(pm);
                    //判断是用户还是系统
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM){
                        //系统应用
                        processInfo.isSystem = true;
                    }else {
                        //非系统应用
                        processInfo.isSystem = false;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    processInfo.name = info.getPackageName();
                    processInfo.icon = ctx.getResources().getDrawable(R.mipmap.ic_launcher);
                    processInfo.isSystem=true;
                    e.printStackTrace();
                }
                processInfoList.add(processInfo);
            }
            return processInfoList;
        }

        else {
            PackageManager mPackageManager = ctx.getPackageManager();
            am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            //进程相关信息
            List<ProcessInfo> processInfoList = new ArrayList<ProcessInfo>();
            //获取正在运行的进程集合
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = am.getRunningAppProcesses();
            //遍历集合
            for (RunningAppProcessInfo info:runningAppProcessInfos){

                processInfo = new ProcessInfo();
                //获取包名
                processInfo.packageName = info.processName;
                //获取进程占用的内存大小
                Debug.MemoryInfo[] processMemInfo = am.getProcessMemoryInfo(new int[]{info.pid});
                Debug.MemoryInfo memoryInfo = processMemInfo[0];
                //获取已经使用的大小
                processInfo.memSize = memoryInfo.getTotalPrivateDirty()*1024;
                //获取名称

                try {
                    ApplicationInfo applicationInfo = mPackageManager.getApplicationInfo(processInfo.getPackageName(),0);
                    processInfo.name =  applicationInfo.loadLabel(mPackageManager).toString();
                    //获取图标
                    processInfo.icon = applicationInfo.loadIcon(mPackageManager);
                    //判断是用户还是系统
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM){
                        //系统应用
                        processInfo.isSystem = true;
                    }else {
                        //非系统应用
                        processInfo.isSystem = false;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    processInfo.name = info.processName;
                    processInfo.icon = ctx.getResources().getDrawable(R.mipmap.ic_launcher);
                    processInfo.isSystem=true;
                    e.printStackTrace();
                }
                processInfoList.add(processInfo);
            }
            return processInfoList;
        }


    }

    /**
     * 获取活动的进程数
     * @param ctx
     * @return
     */
    public static int getProcessCount(Context ctx){
        am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT>=21){
            List<AndroidAppProcess> listInfo = ProcessManager.getRunningAppProcesses();
            int count = listInfo.size();
            return count;
        }else {
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = am.getRunningAppProcesses();
            int count = runningAppProcessInfos.size();
            return count;
        }

    }


    public static long getAvailableSpace(Context ctx){
        am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo memoryInfo = new MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        long memoryAvailableSize = memoryInfo.availMem;
        return memoryAvailableSize;
    }
    /**
     * 获得活动进程的内存大小
     * @param ctx
     * @return 返回可用内存数 bytes
     */
    public static long getTotalSpace(Context ctx){
        am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
       MemoryInfo memoryInfo = new MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        long memoryTotalSize = memoryInfo.totalMem;//低于16的版本不可使用
        return memoryTotalSize;

        /*//兼容低版本
        //内存大小写入文件中,读取proc/meminfo文件,读取第一行,获取数字字符,转换成bytes返回
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader("proc/meminfo"));
            String oneLine = bufferedReader.readLine();
            //字符串转换成字符数组
            char[] chars = oneLine.toCharArray();
           //循环遍历每一个字符,如果此字符的ASCII码在0到9的区域内,说明此字符有效
            StringBuffer stringBuffer = new StringBuffer();
            for (char c : chars){
                if (c>'0'&& c<'9'){
                    stringBuffer.append(c);
                }
            }
            return Long.parseLong(stringBuffer.toString())*1024;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (bufferedReader!=null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;*/

    }

    /**
     * 杀死相应进程
     * @param ctx
     */
    public static void killProcess(Context ctx, ProcessInfo processInfo){
        am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        //杀死指定包名的进程
        am.killBackgroundProcesses(processInfo.getPackageName());
    }

    public static void killAll(Context context) {
        am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT>=21){
            List<AndroidAppProcess> listInfo = ProcessManager.getRunningAppProcesses();
            for (AndroidAppProcess info : listInfo){
                if (info.getPackageName().equals(context.getPackageName())){
                    //跳过自身应用
                    continue;
                }
                am.killBackgroundProcesses(info.getPackageName());
            }
        }else {
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = am.getRunningAppProcesses();
            for (RunningAppProcessInfo info:runningAppProcessInfos){
                if (info.processName.equals(context.getPackageName())){
                    continue;
                }
                am.killBackgroundProcesses(info.processName);
            }
        }
    }
}
