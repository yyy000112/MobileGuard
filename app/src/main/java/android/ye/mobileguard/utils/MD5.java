package android.ye.mobileguard.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 */
public class MD5 {
    /**
     * 给定字符串按照MD5加密
     * @param psd 将密码加盐处理
     * @return
     */
    public static String encoder(String psd){
        try {
            //加盐处理
            psd = psd+"mobilessfe";
            //1,指定加密算法类型
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //2,将需要加密的字符串中转换成byte类型的数组,然后进行随机哈希过程
            byte[] bs = digest.digest(psd.getBytes());
//			System.out.println(bs.length);
            //3,循环遍历bs,然后让其生成32位字符串,固定写法
            //4,拼接字符串过程
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : bs) {
                int i = b & 0xff;
                //int类型的i需要转换成16机制字符
                String hexString = Integer.toHexString(i);
//				System.out.println(hexString);
                if(hexString.length()<2){
                    hexString = "0"+hexString;
                }
                stringBuffer.append(hexString);
            }
            //5,打印测试

            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
