package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.db.domain.AppInfo;
import android.ye.mobileguard.engine.AppInfoProvider;
import android.ye.mobileguard.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *软件管理.
 */
public class AppManagerActivity extends Activity implements View.OnClickListener {

    private TextView storageRemain;
    private TextView sdRemain;
    private ListView appList;
    private List<AppInfo> appInfoList;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            myAdapter = new MyAdapter();
            appList.setAdapter(myAdapter);
            if (listDes!=null&& mCustomerList!=null){
                listDes.setText("用户应用（"+mCustomerList.size()+")");
            }
        };
    };
    private MyAdapter.ViewHolder holder;
    private MyAdapter myAdapter;
    private List<AppInfo> mCustomerList;
    private List<AppInfo> mSystemList;
    private MyAdapter.ViewTitleHolder titleHolder;
    private TextView listDes;
    private AppInfo mAppInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_manager_activity);
        initUI();
        iniTitle();
        initList();
    }

    @Override
    protected void onResume() {
       //卸载操作后返回Activity时重新获得新的数据
        getNewData();
        super.onResume();
    }

    private void getNewData() {
        new Thread(){
            @Override
            public void run() {
                appInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
                //用户应用和系统应用分类
                mCustomerList = new ArrayList<AppInfo>();
                mSystemList = new ArrayList<AppInfo>();
                for (AppInfo appInfo : appInfoList){
                    if (appInfo.isSys){
                        //系统应用
                        mSystemList.add(appInfo);
                    }else {
                        //用户应用
                        mCustomerList.add(appInfo);
                    }
                }
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initList() {
        listDes = (TextView) findViewById(R.id.list_des);


        appList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            /**
             *
             * @param view listview 对象
             * @param firstVisibleItem 第一个可见条目索引
             * @param visibleItemCount 当前屏幕可见
             * @param totalItemCount 总共条目数
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //滚动过程中调用方法
                if (mSystemList != null && mCustomerList != null) {
                    if (firstVisibleItem > mCustomerList.size() + 1) {
                        //滚动到了系统条目
                        listDes.setText("系统应用（" + mSystemList.size() + ")");
                    } else {
                        //滚动到了用户条目
                        listDes.setText("用户应用（" + mCustomerList.size() + ")");
                    }
                }

            }
        });

        //设置ListView点击事件
        appList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || position == mCustomerList.size() + 1) {
                    return;
                } else {
                    if (position < mCustomerList.size() + 1) {
                        mAppInfo = mCustomerList.get(position - 1);
                    } else {
                        //返回系统应用的条目对象
                        mAppInfo = mSystemList.get(position - mCustomerList.size() - 2);
                    }
                    showPopupWindow(view);
                }
            }
        });
    }

    private void showPopupWindow(View view) {
       View popuView = View.inflate(this,R.layout.app_list_popuwindow,null);
        TextView unInstall = (TextView) popuView.findViewById(R.id.app_uninstall);
        TextView start = (TextView) popuView.findViewById(R.id.app_start);
        TextView share = (TextView) popuView.findViewById(R.id.app_share);
        unInstall.setOnClickListener(this);
        start.setOnClickListener(this);
        share.setOnClickListener(this);
        //透明→不透明
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(400);
        alphaAnimation.setFillAfter(true);
        //动画缩放
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(400);
        scaleAnimation.setFillAfter(true);
        //添加动画集合
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);


        //创建窗体
        PopupWindow popWin = new PopupWindow(popuView, LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT,true);
        //设置透明背景，使用popupwindow背景必须设置，不然无法点击回退
        popWin.setBackgroundDrawable(new ColorDrawable());
        //设定窗体位置
        popWin.showAsDropDown(view,100,-view.getHeight());
        //窗体视图中添加动画
        popuView.startAnimation(animationSet);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_uninstall:
                if (mAppInfo.isSys){
                    ToastUtil.show(getApplicationContext(),"无法卸载系统应用");
                }else {
                    if (getPackageName().equals(mAppInfo.getPackName())){
                        ToastUtil.show(getApplicationContext(),"无法卸载自身应用");
                    }
                    else {
                        Intent intent = new Intent("android.intent.action.DELETE");
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse("package:" + mAppInfo.getPackName()));
                        startActivity(intent);
                    }
                }

                break;
            case R.id.app_start:
                //获得指定包名应用
                PackageManager pm = getPackageManager();
                //通过launch开启
                Intent launchIntentForPackage = pm.getLaunchIntentForPackage(mAppInfo.getPackName());
                if (launchIntentForPackage!=null){
                    if (getPackageName().equals(mAppInfo.getPackName())){
                        ToastUtil.show(getApplicationContext(),"安全卫士已启动");
                    }else {
                        startActivity(launchIntentForPackage);
                    }

                }else {
                    ToastUtil.show(getApplicationContext(),"无法打开此应用");
                }
                break;
            case R.id.app_share:
                //分享至第三方，待续
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,"分享一个应用，名称为"+mAppInfo.getName());
                intent.setType("text/plain");
                startActivity(intent);
                break;
        }
    }

    private void iniTitle() {
        //获取磁盘可用大小
        String path = Environment.getDataDirectory().getAbsolutePath();
        //获取SD卡可用大小
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //分别获取磁盘和SD卡的可用空间
        String phoneMemory = android.text.format.Formatter.formatFileSize(this, getUsableSpace(path));
        String sdMemory = android.text.format.Formatter.formatFileSize(this,getUsableSpace(sdPath));
        storageRemain.setText("磁盘可用空间:"+phoneMemory);
        sdRemain.setText("SD卡可用空间:"+ sdMemory);
    }

    private long getUsableSpace(String path) {
        //获取可用磁盘大小
        StatFs statFs = new StatFs(path);
        //获取可用区块的个数
        long count = statFs.getAvailableBlocks();
        //获取可用区块的大小
        long size = statFs.getBlockSize();
        //获取可用空间大小
        return count * size;
    }

    private void initUI() {
        storageRemain = (TextView) findViewById(R.id.storage_remain);
        sdRemain = (TextView) findViewById(R.id.sd_remain);
        appList = (ListView) findViewById(R.id.app_list);
    }



    class MyAdapter extends BaseAdapter {
        //获取适配器中条目类型总数，修改为纯文本+图片文字
        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount()+1;
        }

        //指定索引指向的条目类型，设定返回状态码，0为纯文本，1位图片+文字
        @Override
        public int getItemViewType(int position) {
            if (position==0 || position == mCustomerList.size()+1){
                return 0;
            }else {
                return 1;
            }

        }

        @Override
        public int getCount() {
            //ListView 中添加两个描述
            return mSystemList.size()+ mCustomerList.size()+2;
        }

        @Override
        public AppInfo getItem(int position) {
            if (position == 0 || position == mCustomerList.size()+1){
                return null;
            }else {
                if (position< mCustomerList.size()+1){
                    return mCustomerList.get(position-1);
                }else {
                    //返回系统应用的条目对象
                    return mSystemList.get(position-mCustomerList.size()-2);
                }
            }

           // return appInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            titleHolder = new ViewTitleHolder();
            if (type==0){
                //展示纯文本
                if (convertView == null){
                    convertView = View.inflate(getApplicationContext(),R.layout.list_appinfo_title_item,null);
                    titleHolder.appLisTitle = (TextView) convertView.findViewById(R.id.app_list_title);

                    convertView.setTag(titleHolder);
                }else {
                    titleHolder = (ViewTitleHolder) convertView.getTag();
                }
                if (position == 0){
                    titleHolder.appLisTitle.setText("用户应用（"+mCustomerList.size()+")");
                }else {
                    titleHolder.appLisTitle.setText("系统应用（"+mSystemList.size()+")");
                }

                return convertView;

            }else {
                //展示图片+文字
                if (convertView == null){
                    convertView = View.inflate(getApplicationContext(),R.layout.list_appinfo,null);
                    holder = new ViewHolder();
                    holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
                    holder.appName = (TextView) convertView.findViewById(R.id.app_name);
                    holder.appPath = (TextView) convertView.findViewById(R.id.app_path);
                    convertView.setTag(holder);
                }else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.appIcon.setBackgroundDrawable(getItem(position).icon);
                holder.appName.setText(getItem(position).name);
                if (getItem(position).isSD){
                    holder.appPath.setText("SD卡应用");
                }else {
                    holder.appPath.setText("手机应用");
                }
                return convertView;
            }

        }



         class ViewHolder{
            ImageView appIcon;
             TextView appName;
             TextView appPath;
        }

        class ViewTitleHolder{
            TextView appLisTitle;
        }
    }

}
