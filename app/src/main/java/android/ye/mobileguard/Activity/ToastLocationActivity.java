package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.ye.mobileguard.R;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

/**
 * .
 */
public class ToastLocationActivity extends Activity {


    private ImageView drag;
    private Button bt_top;
    private Button bt_bottom;

    private int mScreenHeight;
    private int mScreenWidth;
    private long startTime = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toast_location_activity);
        initUI();
    }

    private void initUI() {
        drag = (ImageView) findViewById(R.id.location_drag);
        bt_top = (Button) findViewById(R.id.bt_top);
        bt_bottom = (Button) findViewById(R.id.bt_bottom);

        //获取屏幕宽度
       WindowManager mWm = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenHeight = dm.heightPixels;
        mScreenWidth = dm.widthPixels;
        mScreenHeight = mWm.getDefaultDisplay().getHeight();
        mScreenWidth = mWm.getDefaultDisplay().getWidth();
        //获取左上角坐标
        int locationX = SpUtil.getInt(getApplication(), ConstantValue.LOCATION_X,0);
        int locationY = SpUtil.getInt(getApplication(), ConstantValue.LOCATION_Y,0);
        //左上角坐标作用在ivew上，view在相对布局中,所以其所在位置的规则需要由相对布局提供,宽高都为wrapcontent
        final RelativeLayout.LayoutParams layoutParams =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        //将左，上坐标作用在drag对应的规则参数上
        layoutParams.leftMargin = locationX;
        layoutParams.topMargin = locationY;
        //将以上规则作用在drag上
        drag.setLayoutParams(layoutParams);
        //初始位置
        if (locationY>mScreenHeight/2){
            bt_top.setVisibility(View.VISIBLE);
            bt_bottom.setVisibility(View.INVISIBLE);
        }else {
            bt_top.setVisibility(View.INVISIBLE);
            bt_bottom.setVisibility(View.VISIBLE);
        }

        drag.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;
            //对不同事件作出处理
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    //抬起
                    case MotionEvent.ACTION_UP:
                        //存储移动的位置
                        SpUtil.putInt(getApplication(), ConstantValue.LOCATION_X,drag.getLeft());
                        SpUtil.putInt(getApplication(), ConstantValue.LOCATION_Y,drag.getTop());

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
                        //获取当前控件（左，上）点的坐标位置
                        int left = drag.getLeft()+disX;//左侧坐标
                        int top = drag.getTop()+disY;//上侧坐标
                        int right = drag.getRight()+disX;//右侧坐标
                        int bottom = drag.getBottom()+disY;//底部坐标

                        //做容错处理(drag不能拖拽出手机屏幕）
                        //左边边缘不能超出屏幕

                        if (left<0 || right>mScreenWidth ||top<0 ||bottom>mScreenHeight-22){
                            return true;
                        }

                        //右边边缘不能超出屏幕
                        if(right>mScreenWidth){
                            return true;
                        }
                        //左边不能超出屏幕
                        if(left<0){
                        return true;
                        }
                        //上边缘不能超出屏幕
                        if(top<0){
                            return true;
                        }
                        //下边缘(屏幕高度-22）=底边显示最大值，不能超出屏幕
                        if(bottom>mScreenHeight-22){
                            return true;
                        }
                        //拖动到中间位置时，上下按钮的显示调换
                        if (top>mScreenHeight/2){
                            bt_top.setVisibility(View.VISIBLE);
                            bt_bottom.setVisibility(View.INVISIBLE);
                        }else {
                            bt_top.setVisibility(View.INVISIBLE);
                            bt_bottom.setVisibility(View.VISIBLE);
                        }
                        //告知移动控件，按计算出来的坐标移动
                        drag.layout(left,top,right,bottom);
                        //触发一次，重置一次坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();


                        break;

                }
                //在当前的情况下返回false不响应事件,返回true才会响应事件
                //return true
                //既要响应点击事件,又要响应拖拽过程,则此返回值结果需要修改为false
                return false;
            }
        });
        //双击居中事件
        drag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startTime != 0 ){
                    long endTime = System.currentTimeMillis();
                    if (endTime-startTime<500){

                        int left = mScreenWidth/2 - drag.getWidth()/2;
                        int top = mScreenHeight/2 - drag.getHeight()/2;
                        int right = mScreenWidth/2 + drag.getWidth()/2;
                        int bottom = mScreenHeight/2 + drag.getHeight()/2;
                        drag.layout(left,top,right,bottom);
                        //存储最终位置
                        SpUtil.putInt(getApplication(), ConstantValue.LOCATION_X,drag.getLeft());
                        SpUtil.putInt(getApplication(), ConstantValue.LOCATION_Y, drag.getTop());
                    }
                }
                startTime = System.currentTimeMillis();
            }
        });
    }
}
