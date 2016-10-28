package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.engine.AddressDao;



/**
 * 创建查询页面
 */
public class QueryAdrActivity extends Activity {

    private EditText pNum;
    private TextView result;
    private String address;

   private Handler mHandler = new Handler(){
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
            result.setText(address);
       }
   };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_phone_addr);
        initUI();
    }

    private void initUI() {
        pNum = (EditText) findViewById(R.id.q_phone_num);
        final Button query = (Button) findViewById(R.id.query_numaddr);
        result = (TextView) findViewById(R.id.status_result);
        //查询按钮点击事件
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = pNum.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    //查询是耗时操作，开启子线程
                    query(phone);
                } else {
                    Animation shake = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake);
                    pNum.startAnimation(shake);
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(500);

                }


            }
        });
        //输入框即时监听
       pNum.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {

           }

           @Override
           public void afterTextChanged(Editable s) {
               String phone = pNum.getText().toString();
               query(phone);
           }
       });
    }

    private void query(final String phone) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                address = AddressDao.getAddress(phone);
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }


}
