package android.ye.mobileguard.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 *
 */
public class SpUtil {

    private static SharedPreferences sp;


    /**
     * 写入存储节点数据
     * @param context
     * @param Key
     * @param value
     */

    public static void putBoolean(Context context,String Key,boolean value){
        if (sp==null) {
            sp = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
        }
        sp.edit().putBoolean(Key,value).commit();
    }

    /**
     * 读取节点存储数据
     * @param context
     * @param Key
     * @param defValue
     */
    public static boolean getBoolean(Context context, String Key, boolean defValue){
        if(sp==null){
            sp = context.getSharedPreferences("Config",Context.MODE_PRIVATE);
        }
       return sp.getBoolean(Key,defValue);
    }

    public static void putString(Context context,String Key,String psd){
        if (sp==null) {
            sp = context.getSharedPreferences("PassWord", Context.MODE_PRIVATE);
        }
        sp.edit().putString(Key, psd).commit();
    }

    /**
     * 读取节点存储数据
     * @param context
     * @param Key
     * @param depsd
     */
    public static String getString(Context context, String Key, String depsd){
        if(sp==null){
            sp = context.getSharedPreferences("PassWord",Context.MODE_PRIVATE);
        }
        return sp.getString(Key,depsd);
    }

    /**
     * 读取节点存储数据
     * @param context
     * @param Key
     * @param depsd
     */
    public static int getInt(Context context, String Key, int depsd){
        if(sp==null){
            sp = context.getSharedPreferences("ToastStytle",Context.MODE_PRIVATE);
        }
        return sp.getInt(Key, depsd);
    }

    public static void putInt(Context context, String Key, int depsd){
        if(sp==null){
            sp = context.getSharedPreferences("ToastStytle",Context.MODE_PRIVATE);
        }
        sp.edit().putInt(Key, depsd).commit();

    }

    /**
     * 取消选中时删除序列号
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        if (sp==null) {
            sp = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
        }
        sp.edit().remove(key).commit();
    }
}
