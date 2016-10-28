package android.ye.mobileguard.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.ye.mobileguard.db.dao.BlackNumberDao;
import android.ye.mobileguard.utils.ToastUtil;


import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.Observer;

/**
 * 黑名单服务
 */
public class BlackNumberService extends Service {


    private SmsMessage sms;
    private BlackNumberDao mDao;
    private MyPhoneStateListener mPhoneStateListener;
    private TelephonyManager mTe;
    private IntentFilter mIntentFilter;
    private MyContentObserve myContentObserve;
    private InnerSmsReceiver innerSmsReceiver;
    //private SMSObserver mSmsObserver;

    @Override
    public void onCreate() {
        //首先要给mDao赋值，不然会报空指针
        mDao = BlackNumberDao.getInstance(getApplicationContext());
        //拦截短信
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_DELIVER");
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        mIntentFilter.setPriority(1000);
        innerSmsReceiver = new InnerSmsReceiver();
        registerReceiver(innerSmsReceiver, mIntentFilter);


        //监听电话
        //电话管理者
        mTe = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //电话监听
        mPhoneStateListener = new MyPhoneStateListener();
        mTe.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        super.onCreate();
    }

    class MyPhoneStateListener extends PhoneStateListener {
        //重写电话状态发生改变会触发的状态
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //摘机状态
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //响铃状态，挂断电话
                    endCall(incomingNumber);
                    break;
            }
        }
    }


    private void endCall(String incomingNumber) {
        // ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        int mode = mDao.getMode(incomingNumber);
        if (mode == 2 || mode == 3) {
            //利用反射去调用ServiceManager类
            try {
                //获取ServiceManager的字节码
                Class<?> clazz = Class.forName("android.os.ServiceManager");
                //获取方法
                Method method = clazz.getMethod("getService", String.class);
                //反射调用此方法
                IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
                //调用aidl文件对象
                ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
                //调动aidl中隐藏的endcall
                iTelephony.endCall();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //注册内容观察者，观察数据库的变化
            myContentObserve = new MyContentObserve(new Handler(), incomingNumber);
            getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls"), true, myContentObserve);

        }
    }


    private class MyContentObserve extends ContentObserver {
        private String phone;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContentObserve(Handler handler, String phone) {
            super(handler);
            this.phone = phone;
        }

        /**
         * 数据发生改变时会去调用
         *
         * @param selfChange
         */
        @Override
        public void onChange(boolean selfChange) {
            getContentResolver().delete((Uri.parse("content://call_log/calls")), "number = ?", new String[]{phone});
            super.onChange(selfChange);
        }
    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //转移到了外部类
    class InnerSmsReceiver extends BroadcastReceiver {

        private String moriginalAddr;

        @Override
        public void onReceive(Context context, Intent intent) {
            //获取短信内容,获取发送短信电话号码,如果此电话号码在黑名单中,并且拦截模式也为1(短信)或者3(所有),拦截短信
            //1,获取短信内容
            Object[] objects = (Object[]) intent.getExtras().get("pdus");
            //2,循环遍历短信过程
            for (Object object : objects) {
                //3,获取短信对象
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) object);
                //4,获取短信对象的基本信息
                String originalAddr = sms.getOriginatingAddress();
                String messageBody = sms.getMessageBody();
                //短信接收后默认号码前+86,需要截取
                if (originalAddr.contains("+86")) {
                    moriginalAddr = originalAddr.substring(3);
                }
                int mode = mDao.getMode(messageBody);
                if (mode ==1 || mode == 3){
                    if (Build.VERSION.SDK_INT<19){
                        abortBroadcast();
                    }/*else {

                        mSmsObserver = new SMSObserver(new Handler(),messageBody);
                        getContentResolver().registerContentObserver(Uri.parse("content://sms"),true, mSmsObserver);
                    }*/
                }
            }
         /*    //获取短信内容,获取发送短信号码，如号码在黑名单中则拦截短信
             Object[] objects = (Object[]) intent.getSerializableExtra("pdus");
             String format = intent.getStringExtra("format");
             //循环遍历短信
             for (Object object : objects) {
                 //获取短信对象
                 sms = SmsMessage.createFromPdu((byte[]) object, format);

                 //获取基本信息
                 String originalAddr = sms.getOriginatingAddress();
                 String messageBody = sms.getMessageBody();
                 //短信接收后默认号码前+86,需要截取
                 if (originalAddr.contains("+86")) {
                     originalAddr = originalAddr.substring(3);
                 }
               //  mDao = BlackNumberDao.getInstance(context);
                int mode = mDao.getMode(originalAddr);

                 if (mode == 1 || mode == 3){
                     if (Build.VERSION.SDK_INT<19){
                         //拦截短信（中断广播）
                         abortBroadcast();
                     }
                     else {

                         deleteSMS(context, messageBody);
                         Log.d("aaaaaa", "拦截我");
                     }
                 }
             }*/
        }
    }


        //删除接收到的短信，4.4版本以上无法删除
        public void deleteSMS(Context context,String smsContent) {
            //myContentObserve = new MyContentObserve(new Handler(), smsContent);
                //短信收件箱的Uri地址
                Uri uri = Uri.parse("content://sms/inbox");
                //查询收件箱里的的所有短信
                Cursor isRead = context.getContentResolver().query(uri,null, "read=?",new String[]{String.valueOf(0)},"date desc");
                while (isRead.moveToNext()){
                    String phone = isRead.getString(isRead.getColumnIndex("address")).trim();
                    //获取信息内容
                    String body = isRead.getString(isRead.getColumnIndex("body")).trim();
                    if (body.contains(smsContent)){
                       int id = isRead.getInt(isRead.getColumnIndex("_id"));
                        context.getContentResolver().delete(Uri.parse("content://sms/"), "_id="+id, null);
                        }
                    }
                isRead.close();
            }


    @Override
    public void onDestroy() {
        if (innerSmsReceiver!=null){
            unregisterReceiver(innerSmsReceiver);
        }
        //注销观察者
        if (myContentObserve != null){
            getContentResolver().unregisterContentObserver(myContentObserve);
        }
        /*if (mSmsObserver!=null){
            getContentResolver().unregisterContentObserver(mSmsObserver);
        }*/

        super.onDestroy();
    }

    /*//短信监听者，似乎也无法删除短信
    class SMSObserver extends ContentObserver{
        private String sms;

    private Cursor mCursor = null;

                public SMSObserver(Handler handler,String smsm) {
                super(handler);
            }
     @Override
        public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                //query inbox
                Uri inSMSUri = Uri.parse("content://sms/inbox");
                mCursor = getContentResolver().query(inSMSUri, null, null, null, "date desc");
                if (mCursor != null) {
                        StringBuilder sb = new StringBuilder();
                        //循环遍历 while(mCursor.moveToNext()) {
                        int isread = mCursor.getInt(mCursor.getColumnIndex("read"));
                        int id = mCursor.getInt(mCursor.getColumnIndex("_id"));
                        if (isread == 0) {
                            Log.d("TTTT","执行");
                                //String strAddress = Integer.toString(mCursor.getInt(mCursor.getColumnIndex("address")));
                                String body = mCursor.getString(mCursor.getColumnIndex("body"));
                                // 把这条记录写成1
                                ContentValues values = new ContentValues();
                              //  values.put("read", 1);
                            if (body.equals(sms))
                                getContentResolver().delete(Uri.parse("content://sms"),"_id=?", new String[]{"" + id});



                                }
                        }
                    mCursor.close();
                }
       }*/


        }
