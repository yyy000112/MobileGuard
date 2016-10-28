package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.db.domain.AppInfo;
import android.ye.mobileguard.engine.VirusDao;
import android.ye.mobileguard.utils.MD5;

import java.security.Signature;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Handler;

/**
 * 反病毒界面
 */
public class AntiVirusActivity extends Activity {

    private static final int SCAN_FINISH = 101;
    private static final int SCANNING = 102;
    private ImageView ivScanner;
    private TextView scanning;
    private ProgressBar pB;
    private LinearLayout llAddText;
    private RotateAnimation mRotateAnimation;
    private List<ScannerInfo> mScannerInfoList;
    private int index;

    private android.os.Handler mHandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SCANNING:
                    //显示正在扫描的名称
                    ScannerInfo scannerInfo = (ScannerInfo) msg.obj;
                    scanning.setText(scannerInfo.name);
                    //在LinearLayout中动态添加TextView
                    TextView textView = new TextView(getApplicationContext());
                    if (scannerInfo.isVirus){
                        textView.setTextColor(Color.RED);
                        textView.setText("发现病毒:"+scannerInfo.name);
                    }else {
                        textView.setTextColor(Color.BLACK);
                        textView.setText("扫描安全:"+scannerInfo.name);
                    }
                    llAddText.addView(textView);
                    break;
                case SCAN_FINISH:
                    scanning.setText("扫描完成");
                    //停止动画
                    ivScanner.clearAnimation();
                    //卸载删除病毒
                    unInstallVirus();
                    break;
            }
        }
    };

    private void unInstallVirus() {
        for (ScannerInfo info:mScannerInfoList){
            String packageName = info.packageName;
            Intent intent = new Intent("android.intent.action.DELETE");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse("packaename:"+ packageName));
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.antivirus_activity);
        initUI();
        initAnnotation();
        checkVirus();
    }

    private void checkVirus() {
        new Thread(){
            @Override
            public void run() {
                //获取数据库中的所有病毒数据
                List<String> virusList = VirusDao.getVirusList();
                PackageManager pm = getPackageManager();
                //获取所有应用程序签名文件(PackageManager.GET_SIGNATURES 已安装应用的签名文件+卸载完了的应用残余的文件)
                List<PackageInfo> packageInfoList = pm.getInstalledPackages(PackageManager.GET_SIGNATURES + PackageManager.GET_UNINSTALLED_PACKAGES);
                //创建记录病毒的集合
                mScannerInfoList = new ArrayList<ScannerInfo>();
                //记录所有应用集合
                List<ScannerInfo> scannerInfos = new ArrayList<ScannerInfo>();
                //设置进度条最大值
                pB.setMax(packageInfoList.size());

               for (PackageInfo packageInfo:packageInfoList){
                  ScannerInfo scannerInfo = new ScannerInfo();
                   //获取签名文件组的数组
                   android.content.pm.Signature[] signatures = packageInfo.signatures;
                   //获取签名文件数组的第一位,然后进行md5,将此md5和数据库中的md5比对
                   android.content.pm.Signature signature = signatures[0];
                   String string = signature.toCharsString();
                   String encode = MD5.encoder(string);
                   //判断此应用是否为病毒
                   if (virusList.contains(encode)){
                       //记录病毒
                       scannerInfo.isVirus=true;
                       mScannerInfoList.add(scannerInfo);
                   }else {
                       scannerInfo.isVirus = false;
                   }
                   //维护包名和应用名
                   scannerInfo.packageName = packageInfo.packageName;
                   scannerInfo.name = packageInfo.applicationInfo.loadLabel(pm).toString();
                   scannerInfos.add(scannerInfo);
                   index++;
                   pB.setProgress(index);
                   try {
                       //设置随机睡眠时间
                       Thread.sleep(30+new Random().nextInt(200));
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
                   //在子线程中发送消息,告知主线程更新UI(1:顶部扫描应用的名称2:扫描过程中往线性布局中添加view)
                   Message msg = Message.obtain();
                   msg.what = SCANNING;
                   msg.obj = scannerInfo;
                   mHandler.sendMessage(msg);
               }
                Message msg = Message.obtain();
                msg.what = SCAN_FINISH;
                mHandler.sendMessage(msg);
            }
        }.start();




    }

    private void initAnnotation() {
        mRotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        mRotateAnimation.setDuration(500);
        //指定动画无限循环
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        //保持动画执行结束后的状态
        mRotateAnimation.setFillAfter(true);
        ivScanner.startAnimation(mRotateAnimation);
    }

    private void initUI() {
        ivScanner = (ImageView) findViewById(R.id.iv_scanner);
        scanning = (TextView) findViewById(R.id.tv_finish_scanner);
        pB = (ProgressBar) findViewById(R.id.pgb_bar);
        llAddText = (LinearLayout) findViewById(R.id.ll_add_text);
    }

    public class ScannerInfo {
        boolean isVirus;
        String packageName;
        String name;
    }
}
