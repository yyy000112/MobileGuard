package android.ye.mobileguard.engine;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 短信备份
 */
public class SmsBackup {

    private static int index;
    private static File file;
    private static FileOutputStream fos;

    //短信备份方法
    public static void backUp(Context ctx,String path,CallBack callBack){
       //获取备份短信的文件
        file = new File(path);
        //获取内容解析器，查询获得短信数据库里的数据
        Cursor cursor = ctx.getContentResolver().query(Uri.parse("content://sms/"),new String[]{"address","date","type","body"},null,null,null);
        try {
            //文件输出流
            fos = new FileOutputStream(file);
            //序列化数据库中的数据放置到XML中
            XmlSerializer newSerializer = Xml.newSerializer();
            newSerializer.setOutput(fos, "utf-8");
            //DTD规范
            newSerializer.startDocument("utf-8",true);
            newSerializer.startTag(null,"smss");
            //备份短信总数
            if (callBack!=null){
                callBack.setMax(cursor.getCount());
            }

            //读取数据库中每一条数据些入XML
            while (cursor.moveToNext()){
                newSerializer.startTag(null, "sms");
                newSerializer.startTag(null, "address");
                newSerializer.text(cursor.getString(0));
                newSerializer.endTag(null, "address");
                newSerializer.startTag(null, "date");
                newSerializer.text(cursor.getString(1));
                newSerializer.endTag(null, "date");
                newSerializer.startTag(null, "type");
                newSerializer.text(cursor.getString(2));
                newSerializer.endTag(null, "type");
                newSerializer.startTag(null, "body");
                newSerializer.text(cursor.getString(3));
                newSerializer.endTag(null,"body");
                newSerializer.endTag(null,"sms");
                //每循环一次就让进度条叠加
                index++;
                //ProgressDialog进度条可以在主线程中更新
                Thread.sleep(20);
                callBack.setProgress(index);
            }

            newSerializer.endTag(null,"smss");
            newSerializer.endDocument();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (cursor!=null && fos !=null){
                cursor.close();
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    //回调接口，实现
    public interface CallBack{
        //短信总数设置
        public void setMax(int max);
        //短信备份进度条更新
        public void setProgress(int index);
    }
}
