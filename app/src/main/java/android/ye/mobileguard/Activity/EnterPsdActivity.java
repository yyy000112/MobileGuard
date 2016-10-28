package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.MD5;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

/**
 * 拦截界面
 */
public class EnterPsdActivity extends Activity {

    private TextView packageName;
    private ImageView appIcon;
    private EditText enterPsd;
    private String packName;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_lock_item_enter_psd);
        //获取包名
        packName = getIntent().getStringExtra("packagename");
        initUI();
        initData();
    }

    private void initData() {
        final String psd= SpUtil.getString(getApplicationContext(), ConstantValue.SAFE_VALUE, "");

        PackageManager pM = getPackageManager();
        try {
            ApplicationInfo applicationInfo = pM.getApplicationInfo(packName, 0);
            Drawable icon = applicationInfo.loadIcon(pM);
            appIcon.setBackgroundDrawable(icon);
            packageName.setText(applicationInfo.loadLabel(pM).toString());
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String comfirmPsd = enterPsd.getText().toString();
                    Log.d("TTTTT",comfirmPsd);
                    if (!TextUtils.isEmpty(comfirmPsd)){
                        if (MD5.encoder(comfirmPsd).equals(psd)){
                            //解锁，进入应用,并要解除监听，发送广播

                            Intent intent = new Intent("android.intent.action.SKIP");
                            intent.putExtra("packagename",packName);
                            sendBroadcast(intent);
                            finish();
                        }else {
                            ToastUtil.show(getApplicationContext(),"密码错误");
                        }
                    }else {
                        ToastUtil.show(getApplicationContext(),"请输入密码");
                    }
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        packageName = (TextView) findViewById(R.id.package_name);
        appIcon = (ImageView) findViewById(R.id.app_lock_icon);
        enterPsd = (EditText) findViewById(R.id.enter_psd);
        submit = (Button) findViewById(R.id.submit);

    }

    @Override
    public void onBackPressed() {
        //开启隐式意图跳转到桌面
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivity(intent);
        super.onBackPressed();
    }
}
