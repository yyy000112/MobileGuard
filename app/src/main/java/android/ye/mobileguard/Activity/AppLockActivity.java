package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.db.dao.APPlockDao;
import android.ye.mobileguard.db.domain.AppInfo;
import android.ye.mobileguard.engine.AppInfoProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 程序锁界面
 */
public class AppLockActivity extends Activity{

    private List<AppInfo> mUnlockList;
    private List<AppInfo> mLockList;
    private List<AppInfo> mappInfoList;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mLockedAdapter = new MyAdapter(true);
            ltLock.setAdapter(mLockedAdapter);

            mUnLockAdapter = new MyAdapter(false);
            ltUnlock.setAdapter(mUnLockAdapter);
        }
    };
    private Button unLock;
    private Button lock;
    private TextView lockApp;
    private TextView unlockedApp;
    private LinearLayout llLock;
    private LinearLayout llUnlock;
    private ListView ltUnlock;
    private ListView ltLock;
    private MyAdapter mLockedAdapter;
    private MyAdapter mUnLockAdapter;
    private APPlockDao mAppDao;
    private TranslateAnimation translateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_lock_activity);

        initUI();
        initData();
        initAnimation();
    }

    private void initAnimation() {
        translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(500);
    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                mappInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
                //用户应用和系统应用分类
                mLockList = new ArrayList<AppInfo>();
                mUnlockList = new ArrayList<AppInfo>();
                //获取数据库已加锁的包名
                mAppDao = APPlockDao.getInstance(getApplicationContext());
                List<String> lockPackageList = mAppDao.findAll();
                for (AppInfo appInfo : mappInfoList){
                    //如果lockPackageList中包含appInfo则说明已经包含在已加锁中
                    if (lockPackageList.contains(appInfo.packName)){
                        mLockList.add(appInfo);
                    }else {
                        mUnlockList.add(appInfo);
                    }
                }
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initUI() {
        unLock = (Button) findViewById(R.id.unlocked);
        lock = (Button) findViewById(R.id.locked);

        lockApp = (TextView) findViewById(R.id.locked_app);
        unlockedApp = (TextView) findViewById(R.id.unlocked_app);

        llLock = (LinearLayout) findViewById(R.id.ll_lock);
        llUnlock = (LinearLayout) findViewById(R.id.ll_unlock);

        ltUnlock = (ListView) findViewById(R.id.lt_unlock);
        ltLock = (ListView) findViewById(R.id.lt_lock);

        //未加锁图片按钮
        unLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示未加锁
                llUnlock.setVisibility(View.VISIBLE);
                //隐藏已加锁
                llLock.setVisibility(View.GONE);
                //点击未加锁时设置图片为深色，设置加锁按钮图片为浅色
                unLock.setBackgroundResource(R.mipmap.tab_left_pressed);
                lock.setBackgroundResource(R.mipmap.tab_right_default);
            }
        });
        //加锁图片按钮
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示已加锁
                llLock.setVisibility(View.VISIBLE);
                //隐藏未加锁
                llUnlock.setVisibility(View.GONE);
                //点击加锁时设置图片为深色，设置未加锁按钮图片为浅色
                lock.setBackgroundResource(R.mipmap.tab_right_pressed);
                unLock.setBackgroundResource(R.mipmap.tab_left_default);
            }
        });
    }

     class MyAdapter extends BaseAdapter{

         private final boolean isLock;
         private ViewHolder holder;

         /**
          *
          * @param isLock 用于区分已加锁和未加锁
          */
         public MyAdapter(boolean isLock){
            this.isLock = isLock;
        }
        @Override
        public int getCount() {
            if (isLock){
               lockApp.setText("已加锁应用:"+mLockList.size());
               return mLockList.size();
            }else {
                unlockedApp.setText("未加锁应用:"+mUnlockList.size());
                return mUnlockList.size();
            }
        }

        @Override
        public AppInfo getItem(int position) {
            if (isLock){
                return mLockList.get(position);
            }else {
                return mUnlockList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            holder = null;
           if (convertView == null){
               convertView = View.inflate(getApplicationContext(),R.layout.app_lock_lsit_item,null);
               holder = new ViewHolder();
               holder.icon= (ImageView) convertView.findViewById(R.id.alk_icon);
               holder.name= (TextView) convertView.findViewById(R.id.alk_name);
               holder.lock = (ImageView) convertView.findViewById(R.id.lock_icon);
               convertView.setTag(holder);
           }else {
               holder = (ViewHolder) convertView.getTag();
           }
            final AppInfo appInfo = getItem(position);
            final View animationView =convertView;
            holder.icon.setBackgroundDrawable(appInfo.icon);
            holder.name.setText(appInfo.name);
            if (isLock){
                holder.lock.setBackgroundResource(R.mipmap.lock);
            }else {
                holder.lock.setBackgroundResource(R.mipmap.unlock);
            }

            holder.lock.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    animationView.startAnimation(translateAnimation);
                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            //动画结束后执行
                            if (isLock){
                                //已加锁中移除一条
                                mLockList.remove(appInfo);
                                //未加锁中添加一条
                                mUnlockList.add(appInfo);
                                //从已加锁数据库中删除一条
                                mAppDao.delete(appInfo.packName);
                                //提示适配器刷新
                                mLockedAdapter.notifyDataSetChanged();
                            }
                            else {
                                //未加锁中移除一条
                                mUnlockList.remove(appInfo);
                                //已加锁中添加一条
                                mLockList.add(appInfo);
                                //加锁数据库中插入一条
                                mAppDao.insert(appInfo.packName);
                                //提示适配器刷新
                                mUnLockAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });


                }
            });

            return convertView;
        }
    }

  static class ViewHolder{
        ImageView icon;
        TextView name;
        ImageView lock;
    }
}
