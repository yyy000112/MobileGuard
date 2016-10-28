package android.ye.mobileguard.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.location.GpsStatus;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.ye.mobileguard.R;
import android.ye.mobileguard.engine.AddressDao;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;

import java.util.logging.Handler;


public class AddressService extends Service {

    private TelephonyManager mTe;
    private MyPhoneStateListener mPhoneStateListener;
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    private View mViewToast;
    private WindowManager mWm;
    private String mAddress;
    private android.os.Handler mHandler = new android.os.Handler(){
        public void handleMessage(Message msg){
            toast_item.setText(mAddress);
        };
    };
    private TextView toast_item;
    private int[] stytleId;
    private int toastStytleIndex;

    private int mScreenHeight;
    private int mScreenWidth;
    private InnerOutCallReceiver mInnerOutCallReceiver;

    @Override
    public void onCreate() {
        //来时时，管理吐司的显示

        //电话管理者
        mTe = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //电话监听
        mPhoneStateListener = new MyPhoneStateListener();
        mTe.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        //获取窗体对象
        mWm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenHeight = mWm.getDefaultDisplay().getHeight();
        mScreenWidth = mWm.getDefaultDisplay().getWidth();
        //播出电话的广播
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        //创建广播接受者
        mInnerOutCallReceiver = new InnerOutCallReceiver();
        //注册广播接收者
        registerReceiver(mInnerOutCallReceiver,mIntentFilter);
        //

        super.onCreate();
    }

    class InnerOutCallReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //接收号码后要显示自定义吐司，显示播出号码归属地
            String phone = getResultData(); //获取拨出号码
            toastShow(phone);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void toastShow(String incomingNumber){
        final WindowManager.LayoutParams params = mParams;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
       // params.windowAnimations = com.android.internal.R.style.Animation_Toast;
        //在响铃的时候显示吐司
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.setTitle("Toast");
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                //| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;默认能够被触摸
       // 将吐司指定在左上角
        params.gravity = Gravity.LEFT+Gravity.TOP;
            //吐司显示效果,xml→view，将吐司挂在窗体上
            mViewToast = View.inflate(this, R.layout.toast_view, null);
            toast_item = (TextView) mViewToast.findViewById(R.id.tost_item);


            mViewToast.setOnTouchListener(new View.OnTouchListener() {
                private int startX;
                private int startY;
                @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    //抬起
                    case MotionEvent.ACTION_UP:
                        //存储移动的位置
                        SpUtil.putInt(getApplication(), ConstantValue.LOCATION_X,params.x);
                        SpUtil.putInt(getApplication(), ConstantValue.LOCATION_Y,params.y);

                        break;
                    //按下
                    case MotionEvent.ACTION_DOWN:
                        //相对于原点的距离
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        //移动的坐标
                        int disX = moveX - startX;
                        int disY = moveY - startY;

                        params.x = params.x +disX;
                        params.y = params.y + disY;

                        //做容错处理(drag不能拖拽出手机屏幕）
                        //左边边缘不能超出屏幕
                        if (params.x < 0){
                            params.x = 0;
                        }
                        if (params.y < 0){
                            params.y = 0;
                        }
                        if (params.x > mScreenWidth - mViewToast.getWidth() ){
                            params.x = mScreenWidth - mViewToast.getWidth();
                        }
                        if (params.y > mScreenHeight-22-mViewToast.getHeight()){
                            params.y = mScreenHeight-22-mViewToast.getHeight();
                        }
                        //告知吐司需要按照手势的移动,去做位置的更新
                        mWm.updateViewLayout(mViewToast,params);

                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();


                        break;
                }
                    //相应拖拽事件
                return true;
            }
        });

        //读取SP中存储吐司位置的x,y
        //x为坐上角的值，y为左上角的值
       params.x =  SpUtil.getInt(getApplicationContext(),ConstantValue.LOCATION_X,0);
        params.y = SpUtil.getInt(getApplicationContext(),ConstantValue.LOCATION_Y,0);
        //从SP中获取索引，匹配图片展示
        stytleId = new int[]{R.mipmap.call_locate_white,R.mipmap.call_locate_orange,R.mipmap.call_locate_blue,
        R.mipmap.call_locate_green,R.mipmap.call_locate_gray};
        toastStytleIndex = SpUtil.getInt(getApplicationContext(), ConstantValue.TOAST_STYTLE, 0);
       toast_item.setBackgroundResource(stytleId[toastStytleIndex]);

        //在窗体上挂载view
        mWm.addView(mViewToast,mParams);
        //查询号码归属
        query(incomingNumber);
    }

    private void query(final String incomingNumber) {
        new Thread(){
            public void run(){
                mAddress = AddressDao.getAddress(incomingNumber);
                mHandler.sendEmptyMessage(0);
            };
        }.start();
    }

    @Override
    public void onDestroy() {
        //销毁吐司
        if (mTe != null && mPhoneStateListener != null) {
            mTe.listen(mPhoneStateListener,PhoneStateListener.LISTEN_NONE);
        }
        if(mInnerOutCallReceiver!= null){
            unregisterReceiver(mInnerOutCallReceiver);
        }
        super.onDestroy();
    }

    class MyPhoneStateListener extends PhoneStateListener{
        //重写电话状态发生改变会触发的状态
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state){
                case TelephonyManager.CALL_STATE_IDLE:
                    //空闲状态
                    //挂断电话时一处
                    if (mWm!=null&&mViewToast!=null){
                        mWm.removeView(mViewToast);
                    }

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //摘机状态
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //响铃状态
                    toastShow(incomingNumber);
                    break;
            }
        }
    }
}
