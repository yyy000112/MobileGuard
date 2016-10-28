package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.ye.mobileguard.Permit.PermissionManager;

/**
 * 抽取Set1-4界面中的重复代码，建立父类Activity，
 */
public abstract class BaseSetActivity extends Activity {

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //创建手势识别器
        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getRawX()-e2.getRawX()>100){

                    showNextPage();
                }else if (e2.getRawX()-e1.getRawX()>100){
                    showPrePage();
                }
                return super.onFling(e1,e2,velocityX,velocityY);
            }
        });
    }
    //抽象方法，定义跳转上一页，具体逻辑交由子类实现
    public abstract void showPrePage() ;
    //抽象方法，定义跳转下一页，具体逻辑交由子类实现
    public abstract void showNextPage();

    /**
     * 监听触摸事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    //点击进入上一页
    public void prePage(View view){
        showPrePage();
    }
    //点击进入下一页
    public void nextPage(View view){
        showNextPage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }

}
