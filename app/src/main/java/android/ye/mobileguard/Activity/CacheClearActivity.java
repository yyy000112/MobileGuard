package android.ye.mobileguard.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.utils.ToastUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import static android.content.pm.IPackageStatsObserver.*;

/**
 * 缓存清理
 */
public class CacheClearActivity extends Activity {

    private static final int UPDATED_CACHE_UP = 100;
    private static final int CHECK_APP_CACHE = 101;
    private static final int CHECK_FINISH = 102;
    private static final int CLEAR_ALL = 103;
    private Button clearAllCache;
    private ProgressBar pb;
    private TextView tvname;
    private LinearLayout llAdd;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
             switch (msg.what){
                 case UPDATED_CACHE_UP:
                     //在LinearLayout中添加view
                     View view = View.inflate(getApplication(), R.layout.linearlayout_cache_item, null);
                     ImageView ivIcon = (ImageView) view.findViewById(R.id.cache_icon);
                     TextView tvName = (TextView) view.findViewById(R.id.app_name);
                     TextView tvCache = (TextView) view.findViewById(R.id.cache_info);
                     ImageView ivDelete = (ImageView) view.findViewById(R.id.cahe_delete);
                     final CacheInfo cacheInfo = (CacheInfo) msg.obj;
                     ivIcon.setBackgroundDrawable(cacheInfo.icon);
                     tvName.setText(cacheInfo.name+":");
                    tvCache.setText(android.text.format.Formatter.formatFileSize(getApplicationContext(), cacheInfo.cacheSize).toString());
                     llAdd.addView(view, 0);
                     ivDelete.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                             //删除单个应用缓存的权限只供系统使用
                             //通过查看系统日志,获取开启清理缓存activity中action和data
                             Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                             intent.setData(Uri.parse("package:" + cacheInfo.packageName));
                             startActivity(intent);
                         }
                     });
                     break;
                 case CHECK_APP_CACHE:
                     tvname.setText((String)msg.obj);
                     break;
                 case CHECK_FINISH:
                     tvname.setText("扫描完成");
                     break;
                 case CLEAR_ALL:
                     llAdd.removeAllViews();
                     break;
             }

        }
    };
    private PackageManager pm;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cache_clear_activity);
        initUI();
        initData();
    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                pm = getPackageManager();
                //获取已安装包的信息
                List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
                //设置进度条最大值
                pb.setMax(packageInfoList.size());
                //遍历应用，获取缓存信息
                for (PackageInfo packageInfo:packageInfoList){
                    //获取包名作为获取缓存信息的条件
                    String packageName = packageInfo.packageName;
                    getPackageCache(packageName);
                    try {
                        Thread.sleep(10+new Random().nextInt(100));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                        index++;
                    pb.setProgress(index);
                    //每循环一次就将检测应用的名称发送给主线程显示
                    Message msg = Message.obtain();
                    msg.what = CHECK_APP_CACHE;
                    String name = null;
                    try {
                        name = pm.getApplicationInfo(packageName,0).loadLabel(pm).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    msg.obj = name;
                    mHandler.sendMessage(msg);
                }
                //遍历完成后给主线程发送消息
                Message msg = Message.obtain();
                msg.what = CHECK_FINISH;
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 通过包名获取应用缓存信息
     * @param packageName 应用包名
     */
    private void getPackageCache(final String packageName) {
        Stub mIPackageStatsObserver = new Stub() {
            @Override
            public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                //获取指定包名的缓存大小
                long cacheSize = pStats.cacheSize;
                if (cacheSize>0){
                    Message msg = Message.obtain();
                    msg.what = UPDATED_CACHE_UP;
                    CacheInfo cacheInfo = null;
                    cacheInfo = new CacheInfo();
                    try {
                        cacheInfo.cacheSize = cacheSize;
                        String cachaeStr = Formatter.formatFileSize(getApplicationContext(),cacheInfo.cacheSize);
                        cacheInfo.packageName=pStats.packageName;
                        cacheInfo.name = pm.getApplicationInfo(pStats.packageName,0).loadLabel(pm).toString();
                        cacheInfo.icon= pm.getApplicationInfo(pStats.packageName,0).loadIcon(pm);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    msg.obj = cacheInfo;
                    mHandler.sendMessage(msg);
                }
            }
        };
        //获取指定类的字节码文件
        try {
            Class<?> clazz =  Class.forName("android.content.pm.PackageManager");
            //获取调用方法对象
            Method method = clazz.getMethod("getPackageSizeInfo",String.class, IPackageStatsObserver.class);
            //获取对象调用方法
            method.invoke(pm,packageName,mIPackageStatsObserver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private void initUI() {
        clearAllCache = (Button) findViewById(R.id.bt_clear_all);
        pb = (ProgressBar) findViewById(R.id.pb_bar);
        tvname = (TextView) findViewById(R.id.tv_name);
        llAdd = (LinearLayout) findViewById(R.id.ll_add_text);

        clearAllCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkReadContactPermission = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CLEAR_APP_CACHE);
                if (checkReadContactPermission == PackageManager.PERMISSION_GRANTED){
                    Class<?> clazz = null;
                    //获取指定类的字节码文件
                    try {
                        clazz = Class.forName("android.content.pm.PackageManager");
                        //获取调用方法对象
                        Method method = clazz.getMethod("freeStorageAndNotify",long.class, IPackageDataObserver.class);
                        //获取对象调用方法
                        method.invoke(pm,Long.MAX_VALUE, new IPackageDataObserver.Stub() {
                            @Override
                            public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                                //清楚缓存完成后调用方法
                                Message msg = Message.obtain();
                                msg.what=CLEAR_ALL;
                                mHandler.sendMessage(msg);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastUtil.show(getApplicationContext(),"缺少系统签名，无法进行操作");
                }



            }
        });
    }

    public class CacheInfo {
        public String name;
        public String packageName;
        long cacheSize;
        Drawable icon;
    }
}
