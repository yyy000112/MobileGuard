package android.ye.mobileguard.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 转换成字符串
 *
 */
public class StreamUtil {


    public static String streamToString(InputStream is) {
        //在读取的过程中,将读取的内容存储值缓存中,然后一次性的转换成字符串返回
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //读流操作
        byte[] buffer = new byte[1024];
        //记录读取内容的临时变量
        int temp = -1;
        try {
            while((temp = is.read(buffer))!=-1){
                bos.write(buffer, 0, temp);
            }
            //返回读取数据
            return bos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                is.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    /*public static String streamToString(InputStream is){
        StringBuilder sbf = new StringBuilder();
        BufferedReader reader = null;
        try {

            reader = new BufferedReader(new InputStreamReader(is));
            String temp;
            while ((temp=reader.readLine())!=null){
                sbf.append(temp);
                return sbf.toString();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }*/
}
