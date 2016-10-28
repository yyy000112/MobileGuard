package android.ye.mobileguard.global;

import android.app.Application;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


/**
 * 捕获全局应用的异常
 */
public class Myapplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                ex.printStackTrace();
               String path = Environment.getExternalStorageDirectory().getAbsoluteFile()+ File.separator+"errorMobileGuard.log";
                File file = new File(path);
                try {
                    PrintWriter printWriter = new PrintWriter(file);
                    ex.printStackTrace(printWriter);
                    printWriter.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //上传公司的服务器
                //结束应用
                System.exit(0);
            }
        });

    }
}

