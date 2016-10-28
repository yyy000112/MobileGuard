package android.ye.mobileguard.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.ye.mobileguard.Permit.PermissionCallBack;
import android.ye.mobileguard.Permit.PermissionManager;
import android.ye.mobileguard.R;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.StreamUtil;
import android.ye.mobileguard.utils.ToastUtil;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.security.Permissions;
import java.util.logging.LogRecord;

public class SplashActivity extends Activity {

    protected static final String tag = "SplashActivity";

    private static final int UPDATE_VERSION = 100;
    private static final int ENTER_HOME = 101;
    private static final int IO_ERROR = 102;
    private static final int JSON_ERROR = 103;
    private static final int URL_ERROR = 104;
    //static final String[] PERMIT = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private TextView version_name;
    //本地版本号
    private  int mLocalVersionCode;
    //布局
    private RelativeLayout spActivity;

    private String mVersionDes;
    private String mDownUrl;

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_VERSION:
                    //弹出对话框
                    showUpdateDialog();
                    break;
                case ENTER_HOME:
                    enterHome();
                    break;
                case IO_ERROR:
                    ToastUtil.show(getApplicationContext(),"读取异常");
                    enterHome();
                    break;
                case JSON_ERROR:
                    ToastUtil.show(getApplicationContext(),"JSON解析异常");
                    enterHome();
                    break;
                case URL_ERROR:
                    ToastUtil.show(getApplicationContext(),"URL异常");
                    enterHome();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //初始化UI
        initUI();

        //初始化动画效果
        initAnimation();
        //初始化数据库
        initDB();
        //桌面快捷方式
        if (!SpUtil.getBoolean(this,ConstantValue.SHORT_CUT,false)){
            iniShortCut();
        }

    if(PermissionManager.getInstance().hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
        //初始化数据
        initData();
    }else {
        if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ToastUtil.show(this, "需要激活权限,进入设置-应用界面开启相应权限");
        }else {
            PermissionManager.getInstance().requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallBack() {
                @Override
                public void onGranted(String[] permissions, int[] grantResults) {
                    //初始化数据
                    initData();
                }

                @Override
                public void onFailed(String[] permissions, int[] grantResults) {
                    ToastUtil.show(getApplicationContext(),"权限获取失败，进入设置-应用界面开启相应权限");
                }
            });
        }
    }

    }

    private void iniShortCut() {
        //开启桌面快捷方式
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //配置图标
        intent.putExtra(intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(),R.mipmap.app_icon));
        //配置名称
        intent.putExtra(intent.EXTRA_SHORTCUT_NAME,"手机卫士");
        //创建快捷方式后跳转的对象
        Intent shortCut = new Intent("android.intent.action.HOME");
        shortCut.addCategory("android.intent.category.DEFAULT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortCut);
        //发送广播
        sendBroadcast(intent);
        SpUtil.putBoolean(this,ConstantValue.SHORT_CUT,true);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.getInstance().onRequestPermissionResult(requestCode,permissions,grantResults);
    }

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置坐上角图标
        builder.setIcon(R.mipmap.alert_launcher);
        builder.setTitle("版本更新");
        //设置内容
        builder.setMessage(mVersionDes);
        //设置积极按钮，立马更新
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadAPK();
            }
        });
        //设置不选择安装的点击事件
        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enterHome();
            }
        });
        //设置取消点击事件
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                enterHome();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 进入主页面
     */
    private void enterHome() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
        //结束当前页面
        finish();
    }

    /**
     * 安装对应APK
     */
    private void downloadAPK() {
        //判断SD卡路径是否可用
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //获取SD路径
            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator
                    +"mobileguard.apk";
            HttpUtils httpUtils = new HttpUtils();

            httpUtils.download(mDownUrl, path, new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    File file = responseInfo.result;
                    //提示用户安装
                    installApk(file);
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    Log.i(tag,"下载失败");

                }
                @Override
                public void onStart(){
                    super.onStart();
                    Log.i(tag,"下载开始");
                }
                @Override
            public void onLoading(long total,long current,boolean isUploading){
                    Log.i(tag, "下载中........");
                    Log.i(tag, "total = "+total);
                    Log.i(tag, "current = "+current);
                    super.onLoading(total,current,isUploading);
                }
            });
        }
    }

    private void installApk(File file) {
        //系统界面入口，安装
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        //设置数据源和安装类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivityForResult(intent, 0);
    }

    /**
     * 开启一个Activity,返回结果
     * @param requestcode
     * @param resultcode
     * @param data
     */
    protected void onActivityResult(int requestcode,int resultcode,Intent data){
        enterHome();
        super.onActivityResult(requestcode, requestcode, data);
    }



    private void initDB() {
        //归属地数据库拷贝
        initAddrDB("address.db");
        //常用号码数据库拷贝
        initAddrDB("commonnum.db");
        //拷贝病毒数据库
        initAddrDB("antivirus.db");

    }

    /**
     *数据库拷贝到file文件夹下
     * @param DBname 数据库名称
     */
    private void initAddrDB(String DBname) {
        //在files文件夹下创建同名数据库
        File files = getFilesDir();
        File file = new File(files,DBname);
        //运行时文件若存在则返回
        if (file.exists()){
            return;
        }

        InputStream in = null;
        OutputStream os = null;
        try {
            //读取assets目录下的数据
             in = getAssets().open(DBname);
            //将读取的内容写入到指定文件夹的文件中去
             os = new FileOutputStream(file);

            byte[] bs = new byte[1024];
            int temp = -1;
            while ((temp = in.read(bs)) != -1){
                os.write(bs,0,temp);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (in!=null && os!=null){
                try {
                    in.close();
                    os.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(3*1000);
        spActivity.startAnimation(alphaAnimation);
    }

    private void checkVersion() {
        new Thread(){
            @Override
            public void run() {
                //发送数据请求
                Message msg = Message.obtain();
                long startTime = System.currentTimeMillis();
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://192.168.10.147:8080/update1.json");
                    //开启链接
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    //请求超时
                    //connection.setReadTimeout(2*1000);
                    //读取超时
                    //connection.setReadTimeout(2*1000);
                    if (connection.getResponseCode()==200){
                        InputStream is = connection.getInputStream();
                        String json = StreamUtil.streamToString(is);
                        Log.d(tag,json);
                        //解析json
                        JSONObject jsonObject = new JSONObject(json);
                        String versionName = jsonObject.getString("versionName");

                        mVersionDes = jsonObject.getString("versionDes");
                        mDownUrl = jsonObject.getString("downloadUrl");
                        String versionCode= jsonObject.getString("versionCode");
                        //比对版本号(服务器版本号>本地版本号,提示用户更新)
                        if (mLocalVersionCode<Integer.parseInt(versionCode)){
                            //提示用户更新，弹出对话框
                            msg.what= UPDATE_VERSION;
                        }else {
                            msg.what = ENTER_HOME;
                        }

                    }
                } catch (MalformedURLException e) {
                    msg.what = URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what=IO_ERROR;
                } catch (JSONException e) {
                    e.printStackTrace();
                    msg.what=JSON_ERROR;
                }
                finally {
                    //请求网络的时长小于4秒,强制让其睡眠满4秒钟
                    long endTime = System.currentTimeMillis();
                   /* if (endTime-startTime<2*1000){
                        try {
                            Thread.sleep(2*1000-(endTime-startTime));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (connection!=null){
                        connection.disconnect();
                    }*/
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * 获取初始化数据
     */
    private void initData() {
        //应用版本名称
        version_name.setText("版本名称" + getVersionName());
        //获得本地版本号
        mLocalVersionCode = getVersionCode();
        //检查版本
        if (SpUtil.getBoolean(this, ConstantValue.AUTO_UPDATE,false)){
            //
            checkVersion();
        }else {
            //消息发送后延时3秒进入
            mHandler.sendEmptyMessageDelayed(ENTER_HOME,2*1000);
        }

    }

    /**
     * 初始化控件
     */
    private void initUI() {
        version_name = (TextView) findViewById(R.id.version_name);
        spActivity = (RelativeLayout) findViewById(R.id.sp_acti);
    }

    /**
     * 返回版本号
     * @return 非0代表成功
     */
    private int getVersionCode() {
        PackageManager pm = getPackageManager();
        //获取版本号，传0代表获取基本信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(),0);
            //获取版本号
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取版本名称
     * @return 应用版本名称，返回null表示异常
     */
    private String getVersionName() {
        PackageManager pm = getPackageManager();
        //从包的管理者对象中,获取指定包名的版本名称,传0代表获取基本信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(),0);
            //获取版本名称
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
