package android.ye.mobileguard.utils;


import android.content.Context;
import android.widget.Toast; 

public class ToastUtil  {
    /**
     *
     * @param context 上下文
     * @param msg 文本内容
     */
    public static void show(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
}
