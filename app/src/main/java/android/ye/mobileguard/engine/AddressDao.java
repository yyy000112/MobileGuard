package android.ye.mobileguard.engine;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;
import android.ye.mobileguard.utils.ToastUtil;

/**
 * 查询归属地数据库
 */
public class AddressDao {
   public static String path = "data/data/android.ye.mobileguard/files/address.db";
    private static String maddress = "未知号码";


    public static String getAddress(String phone){
        maddress ="未知号码";
        //手机号码正则表达式
        String regularExpression = "^1[3-8]\\d{9}";
        //数据库开启只读
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
        if (phone == null){
            maddress = "无法获取号码";
        }
        if (phone.matches(regularExpression)) {
           String phone1 = phone.substring(0, 7);
            Cursor cursor = db.query("data1", new String[]{"outkey"}, "id = ?", new String[]{phone1}, null, null, null);
            if (cursor.moveToNext()) {
                String outkey = cursor.getString(0);
               // Log.d("OUT","outkey"+outkey);
                Cursor inCursor = db.query("data2", new String[]{"location"}, "id = ?", new String[]{outkey}, null, null, null);
                if (inCursor.moveToNext()) {
                    maddress = inCursor.getString(0);
                  //  Log.d("TAG","地址"+maddress);
                }
            } else {
                maddress = "未知号码";
            }
        }else {
            int length = phone.length();
            switch (length){
                case 3:
                    maddress="紧急电话";
                    break;
                case 4:
                    maddress="模拟器";
                    break;
                case 5:
                    maddress="服务电话";
                    break;
                case 11:
                    //区号加座机号码
                    String area = phone.substring(1,3);
                    Cursor cursor = db.query("data2", new String[]{"location"}, "area = ?", new String[]{area}, null, null, null);
                    if (cursor.moveToNext()){
                        maddress = cursor.getString(0);
                    }else {
                        maddress = "未知号码";
                    }
                    break;
                case 12:
                    String area1 = phone.substring(1,4);
                    Cursor cursor1 = db.query("data2", new String[]{"location"}, "area = ?", new String[]{area1}, null, null, null);
                    if (cursor1.moveToNext()){
                        maddress = cursor1.getString(0);
                    }else {
                        maddress = "未知号码";
                    }
                    break;
            }
        }

        return maddress;
    }
}
