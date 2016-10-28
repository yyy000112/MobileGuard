package android.ye.mobileguard.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.ye.mobileguard.R;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

/**
 * Created by ye on 2016/7/19.
 */
public class Set3Activity extends BaseSetActivity{

    private Button selectContact;
    private EditText phoneNum;
    private static final int RC_READ_CONTACT_PERM = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set3_activity);
        initUI();
        if (Build.VERSION.SDK_INT>=23){
            int checkReadContactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            if (checkReadContactPermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, RC_READ_CONTACT_PERM);
                return;
            }else {
                initEvent();
            }
        }else {
            initEvent();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case RC_READ_CONTACT_PERM:
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initEvent();
                }else {
                    ToastUtil.show(getApplicationContext(),"获取权限失败");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initEvent() {
        selectContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactListActivity.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data!=null){
            //接收从ContactListActivity中返回的数据
            String phone = data.getStringExtra("phone");
            phone = phone.replace("-","").replace(" ","").trim();
            phoneNum.setText(phone);
            //存储联系人号码
            SpUtil.putString(getApplicationContext(),ConstantValue.PHONE_NUM,phone);
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void initUI() {
        phoneNum = (EditText) findViewById(R.id.phone_num);
        //获取联系人电话的回显

        String phone = SpUtil.getString(getApplication(), ConstantValue.PHONE_NUM, "");
        phoneNum.setText(phone);
        selectContact = (Button) findViewById(R.id.select_contacts);
    }

    @Override
    public void showPrePage() {
        Intent intent = new Intent(getApplicationContext(),Set2Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
    }

    @Override
    public void showNextPage() {
        String phone = phoneNum.getText().toString();
        if (!TextUtils.isEmpty(phone)){
            Intent intent = new Intent(getApplicationContext(),Set4Activity.class);
            startActivity(intent);
            finish();
            //如果现在是输入电话号码,则需要去保存
            SpUtil.putString(this,ConstantValue.PHONE_NUM,phone);
            overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
        }else {
            ToastUtil.show(getApplicationContext(), "请输入号码");
        }
    }

}
