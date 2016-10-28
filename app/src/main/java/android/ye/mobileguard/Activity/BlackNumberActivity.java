package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.db.dao.BlackNumberDao;
import android.ye.mobileguard.db.domain.BlackNumberInfo;
import android.ye.mobileguard.utils.ToastUtil;

import java.util.List;

/**
 * 黑名单功能
 */
public class BlackNumberActivity extends Activity {

    private Button addBlack;
    private ListView blackNumber;
    private BlackNumberDao mDao;
    private List<BlackNumberInfo> mBlackNumberInfoList;
    private MyAdapter mAdapter;
    private int mode;
    public boolean mIsload = false;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (mAdapter == null){
                mAdapter = new MyAdapter();
                blackNumber.setAdapter(mAdapter);
            }else {
                mAdapter.notifyDataSetChanged();
            }

        };
    };
    private int mCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blacknum_activity);
        initUI();
        initData();
    }

    private void initData() {
        new Thread() {
            @Override
            public void run() {
                //获取操作黑名单数据的对象
                mDao = BlackNumberDao.getInstance(getApplicationContext());
                //查询所有数据
                mBlackNumberInfoList = mDao.find(0);
                mCount = mDao.getCount();
                //通过主线程告知要添加的数据
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initUI() {
        addBlack = (Button) findViewById(R.id.add_black);
        blackNumber = (ListView) findViewById(R.id.black_number);

        addBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        //监听滚动状态
        blackNumber.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
               if (mBlackNumberInfoList!=null){
                   /*滚动到停为止，最后一个条目可见，mIsload：防止重复加载的变量
                   如果当前正在加载mIsLoad就会为true,本次加载完毕后,再将mIsLoad改为false
                   如果下一次加载需要去做执行的时候,会判断上诉mIsLoad变量,是否为false,如果为true,就需要等待上一次加载完成,将其值
                   改为false后再去加载*/
                   if (scrollState== AbsListView.OnScrollListener.SCROLL_STATE_IDLE && blackNumber.getLastVisiblePosition()>=mBlackNumberInfoList.size()-1
                           && !mIsload){
                        //条目总数大于集合总数时才可以加载
                       if (mCount>mBlackNumberInfoList.size()){
                           //加载下一页数据
                           new Thread(){
                               @Override
                               public void run() {
                                   //获取操作黑名单数据的对象
                                   mDao = BlackNumberDao.getInstance(getApplicationContext());
                                   //查询部分数据
                                   List<BlackNumberInfo> moreData = mDao.find(mBlackNumberInfoList.size());
                                   //添加下一页数据
                                   mBlackNumberInfoList.addAll(moreData);
                                   //通过主线程告知要添加的数据
                                   mHandler.sendEmptyMessage(0);
                               }
                           }.start();
                       }
                   }
               }

            }
            //滚动过程中调用的方法
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(getApplicationContext(), R.layout.add_blacknumber_dialog, null);
        dialog.setView(view);
        final EditText ed_number = (EditText) view.findViewById(R.id.black_phone_number);
        RadioGroup rg_group = (RadioGroup) view.findViewById(R.id.rg_group);

        Button blackSubmit = (Button) view.findViewById(R.id.black_submit);
        Button blackCancle = (Button) view.findViewById(R.id.black_cancle);
        //监听RadioButton条目的切换过程
        rg_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.sms:
                        //拦截短信
                        mode = 1;
                        break;
                    case R.id.phone:
                        //拦截电话
                        mode = 2;
                        break;
                    case R.id.all:
                        //拦截所有
                        mode = 3;
                        break;
                }
            }
        });

        blackSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = ed_number.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    //数据库插入该条目
                    mDao.insert(phone, String.valueOf(mode));
                    //让数据库和集合中同步刷新
                    BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
                    blackNumberInfo.phone = phone;
                    blackNumberInfo.mode = String.valueOf(mode);
                    //将对象插入到最顶部
                    mBlackNumberInfoList.add(0, blackNumberInfo);
                    //通知适配器数据刷新
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    //隐藏对话框
                    dialog.dismiss();

                } else {
                    ToastUtil.show(getApplicationContext(), "请输入所要拦截的号码");
                }
            }
        });
        blackCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    //Listview适配器
    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mBlackNumberInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mBlackNumberInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            ViewHoler holer = null;
            if (convertView==null){
                convertView =  View.inflate(getApplicationContext(),R.layout.listview_blacknumber_item,null);
                holer = new ViewHoler();
                holer.blackPhone = (TextView) convertView.findViewById(R.id.black_phone);
                holer.blackMode = (TextView) convertView.findViewById(R.id.black_mode);
                holer.deleteBlack = (ImageView) convertView.findViewById(R.id.delete_black);
                convertView.setTag(holer);
            }else {
                holer = (ViewHoler)convertView.getTag();
            }

            holer.deleteBlack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //数据库删除
                    mDao.delete(mBlackNumberInfoList.get(position).getPhone());
                    //集合中删除数据，通知适配器删除
                    mBlackNumberInfoList.remove(position);
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });

            holer.blackPhone.setText(mBlackNumberInfoList.get(position).getPhone());

            int mode = Integer.parseInt(mBlackNumberInfoList.get(position).getMode());

            switch (mode){
                case 1:
                    holer.blackMode.setText("拦截短信");
                    break;
                case 2:
                    holer.blackMode.setText("拦截电话");
                    break;
                case 3:
                    holer.blackMode.setText("拦截所有");
                    break;
            }
            return convertView;
        }

         class ViewHoler {
            TextView blackPhone;
            TextView blackMode;
            ImageView deleteBlack;
        }
    }
}
