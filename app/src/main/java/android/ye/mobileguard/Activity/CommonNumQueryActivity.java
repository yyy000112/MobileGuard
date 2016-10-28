package android.ye.mobileguard.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.ye.mobileguard.R;
import android.ye.mobileguard.engine.CommonNumberDao;

import java.util.List;

/**
 * 常用号码查询
 */
public class CommonNumQueryActivity extends Activity {

    private List<CommonNumberDao.Group> groupList;
    private ExpandableListView expandableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_num_query);
        initUI();
        initData();
    }

    /**
     * 给可扩展的ListView准备和填充数据
     */
    private void initData() {
        CommonNumberDao commonNumberDao = new CommonNumberDao();
        groupList = commonNumberDao.getGroup();
        final MyAdapter mAdapter = new MyAdapter();
        expandableListView.setAdapter(mAdapter);
        //设置子类的点击事件
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                startCall(mAdapter.getChild(groupPosition, childPosition).number);
                return false;
            }
        });
    }

    private void startCall(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+number));
        startActivity(intent);
    }

    private void initUI() {
        expandableListView = (ExpandableListView) findViewById(R.id.ex_list);

    }

    class MyAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return groupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return groupList.get(groupPosition).children.size();
        }

        @Override
        public CommonNumberDao.Group getGroup(int groupPosition) {
            return groupList.get(groupPosition);
        }

        @Override
        public CommonNumberDao.Child getChild(int groupPosition, int childPosition) {
            return groupList.get(groupPosition).children.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText("         "+getGroup(groupPosition).name);
            textView.setTextColor(Color.BLUE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
            return textView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
          View view = View.inflate(getApplication(), R.layout.common_number_child_list, null);
            TextView numName = (TextView) view.findViewById(R.id.common_num_name);
            TextView number = (TextView) view.findViewById(R.id.common_nun);
            numName.setText(getChild(groupPosition,childPosition).name);
            number.setText(getChild(groupPosition,childPosition).number);
            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
