package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.db.domain.ProcessInfo;
import android.ye.mobileguard.engine.ProcessInfoProvider;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 进程管理
 */
public class ProcessManagerActivity extends Activity implements View.OnClickListener {

    private TextView processCount;
    private TextView remainAmount;
    private ListView processList;
    private TextView proListDes;
    private List<ProcessInfo> mSystemList;
    private List<ProcessInfo> mCustomerList;
    private ProcessInfo mProcessInfo;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mAdapter = new Madapter();
            processList.setAdapter(mAdapter);
            if(proListDes!=null && mCustomerList!=null){
                proListDes.setText("用户进程("+mCustomerList.size()+")");
            }
        };
    };
    private Button selectAll;
    private Button reverseSelect;
    private Button setting;
    private List<ProcessInfo> processInfoList;
    private Button clear;
    private Madapter mAdapter;
    private int processAmount;
    private long total;
    private String totalSpace;
    private long available;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_manager_activity);

        initUI();
        iniTile();
        initList();
    }

    private void initUI() {
        processCount = (TextView) findViewById(R.id.process_count);
        remainAmount = (TextView) findViewById(R.id.remain_amount);
        selectAll = (Button) findViewById(R.id.select_all);
        reverseSelect = (Button) findViewById(R.id.select_reverse);
        clear = (Button) findViewById(R.id.clear);
        setting = (Button) findViewById(R.id.setting);

        selectAll.setOnClickListener(this);
        reverseSelect.setOnClickListener(this);
        clear.setOnClickListener(this);
        setting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.select_all:
                selectedAll();
                break;
            case R.id.select_reverse:
                selecetReverse();
                break;
            case R.id.clear:
                clearAll();
                break;
            case R.id.setting:
                set();
                break;
        }
    }

    private void set() {
        Intent intent = new Intent(this,SettingProcessActivity.class);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void clearAll() {
        //获取选中进程
        List<ProcessInfo> killProcessInfo =  new ArrayList<ProcessInfo>();
        for(ProcessInfo processInfo:mCustomerList){
            if(processInfo.getPackageName().equals(getPackageName())){
                continue;
            }
            if(processInfo.isCheck){
                //不能在集合循环过程中去移除集合中的对象
//				mCustomerList.remove(processInfo);
                //记录需要杀死的用户进程
                killProcessInfo.add(processInfo);
            }
        }

        for(ProcessInfo processInfo:mSystemList){
            if(processInfo.isCheck){
                //4,记录需要杀死的系统进程
                killProcessInfo.add(processInfo);
            }
        }
        if (killProcessInfo.size()==0){
            ToastUtil.show(this,"请选择需要清除的进程");
        }else {
            long totalReleaseMsize = 0;
            //遍历killProcessInfo
            for (ProcessInfo processInfo:killProcessInfo){
                //首先从列表中将选中的移除
                if(mCustomerList.contains(processInfo)){
                    mCustomerList.remove(processInfo);
                }

                if(mSystemList.contains(processInfo)){
                    mSystemList.remove(processInfo);
                }
                //杀死选中的进程
                ProcessInfoProvider.killProcess(this,processInfo);
                //释放的内存大小
                long memSize = processInfo.memSize;
                totalReleaseMsize +=memSize;

            }
            if (mAdapter!=null){
                mAdapter.notifyDataSetChanged();
            }
            if (killProcessInfo.size()>0){
                //更新进程总数
                int killMount = killProcessInfo.size();
                processAmount -=killMount;
                //更新内存总数
                available +=totalReleaseMsize;
                String availableRemain = Formatter.formatFileSize(this,available);
                //更新title
                processCount.setText("进程总数:"+ processAmount);
                remainAmount.setText("剩余/总共" + availableRemain + "/" + totalSpace);
                //告知用户释放了多少内存
                String totalReleaseSpace = Formatter.formatFileSize(this,totalReleaseMsize);
                ToastUtil.show(this,"清除了"+killMount+"个进程,释放了"+totalReleaseSpace+"的运行内存");
            }

        }

    }

    /**
     * 反选
     */
    private void selecetReverse() {
        //1,将所有的集合中的对象上isCheck字段取反
        for(ProcessInfo processInfo:mCustomerList){
            if(processInfo.getPackageName().equals(getPackageName())){
                continue;
            }
            processInfo.isCheck = !processInfo.isCheck;
        }
        for(ProcessInfo processInfo:mSystemList){
            processInfo.isCheck = !processInfo.isCheck;
        }
        if (mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 全选
     */
    private void selectedAll() {
        //1,将所有的集合中的对象上isCheck字段设置为true,代表全选,排除当前应用
        for(ProcessInfo processInfo:mCustomerList){
            if(processInfo.getPackageName().equals(getPackageName())){
                continue;
            }
            processInfo.isCheck = true;
        }
        for(ProcessInfo processInfo:mSystemList){
            processInfo.isCheck = true;
        }
        if (mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 设置进程数和运行内存大小
     */
    private void iniTile() {
        processAmount = ProcessInfoProvider.getProcessCount(this);
        processCount.setText("进程总数:"+ processAmount);
        //获取可用内存大小,并且格式化
        available = ProcessInfoProvider.getAvailableSpace(this);
        String availableSpace = Formatter.formatFileSize(this, available);
        //获取内存总数,并且格式化
        total = ProcessInfoProvider.getTotalSpace(this);
        totalSpace = Formatter.formatFileSize(this, total);

        remainAmount.setText("剩余/总共" + availableSpace + "/" + totalSpace);
    }

    private void initList() {
        processList = (ListView) findViewById(R.id.process_list);
        proListDes = (TextView) findViewById(R.id.process_list_des);

        new Thread(){
            @Override
            public void run() {
                processInfoList = ProcessInfoProvider.getProcessInfoList(getApplicationContext());
                //用户应用和系统应用分类
                mCustomerList = new ArrayList<ProcessInfo>();
                mSystemList = new ArrayList<ProcessInfo>();
                for (ProcessInfo processInfo : processInfoList){
                    if (processInfo.isSystem){
                        //系统应用
                        mSystemList.add(processInfo);
                    }else {
                        //用户应用
                        mCustomerList.add(processInfo);
                    }
                }

                mHandler.sendEmptyMessage(0);
            }
        }.start();

        processList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            /**
             *
             * @param view listview 对象
             * @param firstVisibleItem 第一个可见条目索引
             * @param visibleItemCount 当前屏幕可见
             * @param totalItemCount 总共条目数
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //滚动过程中调用方法
                if (mSystemList != null && mCustomerList != null) {
                    if (firstVisibleItem > mCustomerList.size() + 1) {
                        //滚动到了系统条目
                        proListDes.setText("系统进程（" + mSystemList.size() + ")");
                    } else {
                        //滚动到了用户条目
                        proListDes.setText("用户进程（" + mCustomerList.size() + ")");
                    }
                }

            }
        });

        //设置ListView点击事件
        processList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || position == mCustomerList.size() + 1) {
                    return;
                } else {
                    if (position < mCustomerList.size() + 1) {
                        mProcessInfo = mCustomerList.get(position - 1);
                    } else {
                        //返回系统应用的条目对象
                        mProcessInfo = mSystemList.get(position - mCustomerList.size() - 2);
                    }
                    if (mProcessInfo != null){
                        //选中条目的进程和本应用进程名不同时，才需要状态取反和选框设置
                        if (!mProcessInfo.packageName.equals(getPackageName())){
                            //状态取反
                            mProcessInfo.isCheck = !mProcessInfo.isCheck;
                           CheckBox cbBox = (CheckBox)view.findViewById(R.id.process_cb_box);
                            cbBox.setChecked(mProcessInfo.isCheck);

                        }
                    }
                }
            }
        });
    }



    class Madapter extends BaseAdapter {
        //获取适配器中条目类型总数，修改为纯文本+图片文字
        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount()+1;
        }

        //指定索引指向的条目类型，设定返回状态码，0为纯文本，1位图片+文字
        @Override
        public int getItemViewType(int position) {
            if (position==0 || position == mCustomerList.size()+1){
                return 0;
            }else {
                return 1;
            }
        }

        @Override
        public int getCount() {
            //ListView 中添加两个描述
            //显示or隐藏系统进程
            if (SpUtil.getBoolean(getApplicationContext(), ConstantValue.SHOW_SYS,false)){
                return  mCustomerList.size()+1;

            }else {
                return mSystemList.size()+ mCustomerList.size()+2;
            }

        }

        @Override
        public ProcessInfo getItem(int position) {
            if (position == 0 || position == mCustomerList.size()+1){
                return null;
            }else {
                if (position< mCustomerList.size()+1){
                    return mCustomerList.get(position-1);
                }else {
                    //返回系统应用的条目对象
                    return mSystemList.get(position-mCustomerList.size()-2);
                }
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            int type = getItemViewType(position);
            ViewTitleHolder titleHolder = new ViewTitleHolder();
            if (type==0){
                //展示纯文本
                if (convertView == null){
                    convertView = View.inflate(getApplicationContext(),R.layout.list_processinfo_title_item,null);
                    titleHolder.processLisTitle = (TextView) convertView.findViewById(R.id.process_list_title);

                    convertView.setTag(titleHolder);
                }else {
                    titleHolder = (ViewTitleHolder) convertView.getTag();
                }
                if (position == 0){
                    titleHolder.processLisTitle.setText("用户进程（"+mCustomerList.size()+")");
                }else {
                    titleHolder.processLisTitle.setText("系统进程（"+mSystemList.size()+")");
                }

                return convertView;

            }else {
                //展示图片+文字
                if (convertView == null){
                    convertView = View.inflate(getApplicationContext(),R.layout.list_processinfo,null);
                    holder = new ViewHolder();
                    holder.processIcon = (ImageView) convertView.findViewById(R.id.process_icon);
                    holder.processName = (TextView) convertView.findViewById(R.id.process_name);
                    holder.processMemoryInfo = (TextView) convertView.findViewById(R.id.process_memory_info);
                    holder.processCbBox = (CheckBox) convertView.findViewById(R.id.process_cb_box);
                    convertView.setTag(holder);
                }else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.processIcon.setBackgroundDrawable(getItem(position).icon);
                holder.processName.setText(getItem(position).name);
                //该活动进程的运行内存
                long memSize = getItem(position).memSize;
                String memeSizeSpace = Formatter.formatFileSize(getApplicationContext(),memSize);
                holder.processMemoryInfo.setText(memeSizeSpace);
                if (getItem(position).packageName.equals(getPackageName())){
                    holder.processCbBox.setVisibility(View.GONE);
                }else {
                    holder.processCbBox.setVisibility(View.VISIBLE);
                }
                holder.processCbBox.setChecked(getItem(position).isCheck);

                return convertView;
            }

        }



        class ViewHolder{
            ImageView processIcon;
            TextView processName;
            TextView processMemoryInfo;
            CheckBox processCbBox;
        }

        class ViewTitleHolder{
            TextView processLisTitle;
        }
    }
}
