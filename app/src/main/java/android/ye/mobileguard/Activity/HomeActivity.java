package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.MD5;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

/**
 *
 */
public class HomeActivity extends Activity {

    private GridView mgridView;
    private String[] mtitles;
    private int[] mImages;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        //初始化控件
        initUI();
        //初始化数据
        initData();
    }

    private void initData() {
        mtitles = new String[]{"手机防盗","通信卫士","软件管理","进程管理","流量统计",
                "手机杀毒","缓存清理","高级工具","设置中心"};
        mImages = new int[]{R.mipmap.home_safe,R.mipmap.home_callmsgsafe,R.mipmap.home_apps,
                R.mipmap.home_taskmanager,R.mipmap.home_netmanager,R.mipmap.home_trojan,
                R.mipmap.home_sysoptimize,R.mipmap.home_tools,R.mipmap.home_settings
        };
        //设置九宫格适配器
        mgridView.setAdapter(new myAdapter());
        //设置点击事件
        mgridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        showDialog();
                        break;
                    case 1:
                        startActivity(new Intent(getApplicationContext(), BlackNumberActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(getApplicationContext(),AppManagerActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(getApplicationContext(),ProcessManagerActivity.class));
                        break;
                    case 5:
                        startActivity(new Intent(getApplicationContext(),AntiVirusActivity.class));
                        break;
                    case 4:
                        startActivity(new Intent(getApplicationContext(),TrafficInfoActivity.class));
                        break;
                    case 6:
                        //startActivity(new Intent(getApplicationContext(),CacheClearActivity.class));
                        startActivity(new Intent(getApplicationContext(),BaseCacheClearActivity.class));
                        break;
                    case 7:
                        startActivity(new Intent(getApplicationContext(),ToolsActivity.class));
                        break;
                    case 8:
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });

    }

    private void showDialog() {
        //判断是否存有密码
        String psd = SpUtil.getString(this, ConstantValue.SAFE_VALUE,"");
        //若密码为空时，显示设置密码对话框
        if(TextUtils.isEmpty(psd)){
            setShowDialog();
        }else {
            showComfirmPsd();
        }
    }

    /**
     * 设置确认密码的对话框
     */
    private void showComfirmPsd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(this,R.layout.comfir_dialog,null);
        dialog.setView(view);
        dialog.show();
        //需要用view.xxx初始化
        final Button comfir = (Button) view.findViewById(R.id.comfirm);
        Button dismiss = (Button) view.findViewById(R.id.dismiss);

        comfir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText comfir_pas = (EditText) view.findViewById(R.id.comfir_pas);
                String comfirm_pas = comfir_pas.getText().toString();
                String sp_psd = SpUtil.getString(getApplicationContext(), ConstantValue.SAFE_VALUE, "");

                if (!TextUtils.isEmpty(comfirm_pas)){
                    if (MD5.encoder(comfirm_pas).equals(sp_psd)){
                        //Intent intent = new Intent(getApplicationContext(), Set1Activity.class);
                        //若设置已经完成，直接跳转到最后一个页面
                        Intent intent = new Intent(getApplicationContext(),SetFinishActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }else {
                        ToastUtil.show(getApplicationContext(),"密码错误，请重新输入");
                    }
                }else {
                    ToastUtil.show(getApplicationContext(),"请输入密码");
                }
            }
        });
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 设置密码对话框
     */
    private void setShowDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //自定义对话框时需调用create
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(this,R.layout.setpsd_dialog,null);
        //显示自定义对话框
        dialog.setView(view);
        dialog.show();
        //需要用view.xx初始化
        Button submit = (Button) view.findViewById(R.id.bt_submit);
        Button cancel = (Button) view.findViewById(R.id.bt_cancel);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText setPsd = (EditText) view.findViewById(R.id.set_psd);
                EditText comfirmPsd = (EditText) view.findViewById(R.id.comfirm_psd);

                String psd = setPsd.getText().toString();
                String comPsd = comfirmPsd.getText().toString();

                if (!TextUtils.isEmpty(psd) && !TextUtils.isEmpty(comPsd)) {
                    if (psd.equals(comPsd)) {
                       Intent intent = new Intent(getApplicationContext(), Set1Activity.class);
                        startActivity(intent);
                        dialog.dismiss();
                        SpUtil.putString(getApplicationContext(), ConstantValue.SAFE_VALUE, MD5.encoder(psd));
                    } else {
                        ToastUtil.show(getApplication(), "确认密码错误");
                    }
                }else{
                    ToastUtil.show(getApplicationContext(), "请输入密码");
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    private void initUI() {
        mgridView = (GridView) findViewById(R.id.Grid_view);
    }

    private class myAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mtitles.length;
        }

        @Override
        public Object getItem(int position) {
            return mtitles[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(), R.layout.item_grid, null);
            TextView gridTitle = (TextView) view.findViewById(R.id.Grid_title);
            ImageView gridIcon = (ImageView) view.findViewById(R.id.Grid_icon);
            gridTitle.setText(mtitles[position]);
            gridIcon.setBackgroundResource(mImages[position]);
            return view;
        }
    }
}
