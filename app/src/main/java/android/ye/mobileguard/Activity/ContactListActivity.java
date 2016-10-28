package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.ye.mobileguard.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;

/**
 * 建立获取通讯录界面
 */
public class ContactListActivity extends Activity{

    private ListView list_contact;
    private List<HashMap<String, String>> contactlist = new ArrayList<HashMap<String,String>>();
    private MyAdapter mAdapter;

    android.os.Handler mHandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            mAdapter = new MyAdapter();
            list_contact.setAdapter((ListAdapter) mAdapter);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_list);
        initUI();
        initData();
    }

    private void initData() {

        //读取系统联系人,可能是一个耗时操作,放置到子线程中处理

        new Thread(){
            @Override
            public void run() {
                Uri uri = Uri.parse("content://com.android.contacts/contacts");
                //获取内容解析器对象
                ContentResolver contentResolver = getContentResolver();
                //查询系统联系人数据库表过程(读取联系人权限)
                Cursor cursor = contentResolver.query(uri,new String[]{"_id"},null,null,null );
               // Cursor cursor = contentResolver.query(Uri.parse("content://com.android.contacts/raw_contacts"), new String[]{"contact_id"}, null, null, null);
                //先将列表清空
                contactlist.clear();
                //游标循环提取数据
                while (cursor.moveToNext()){
                    String id = cursor.getString(0);
                    //根据id值唯一性,查询data表和mimetype表生成的视图,获取data以及mimetype字段
                    /*int contractID = cursor.getInt(0);
                    StringBuilder sb = new StringBuilder("contractID=");
                    sb.append(contractID);*/
                    uri = Uri.parse("content://com.android.contacts/contacts/" + id + "/data");
                    //data2是邮箱
                    Cursor indexCursor = contentResolver.query(uri,new String[]{"mimetype","data1","data2"},null,null,null);
                   // Cursor indexCursor = contentResolver.query(Uri.parse("content://com.android.contacts/data"),new String[]{"data1", "mimetype"}, "raw_contact_id=?",new String[]{id} , null);


                    //循环获取每一个联系人的电话号码以及姓名,数据类型
                    HashMap<String,String> hashMap = new HashMap<String,String>();
                    while (indexCursor.moveToNext()){
                        String data = indexCursor.getString(1);
                        String type = indexCursor.getString(0);
                        /*String data = indexCursor.getString(0);
                        String type = indexCursor.getString(1);*/
                        //区分类型，填充到hashmap里
                        if (type.equals("vnd.android.cursor.item/phone_v2")){
                            if (!TextUtils.isEmpty(data)){
                                hashMap.put("phone",data);

                            }
                        }else if (type.equals("vnd.android.cursor.item/name")){
                            hashMap.put("name",data);
                        }
                    }
                    indexCursor.close();
                    contactlist.add(hashMap);
                }
                cursor.close();
               // 消息机制,发送一个空的消息,告知主线程可以去使用子线程已经填充好的数据集合
                mHandler.sendEmptyMessage(0);

            };
        }.start();
    }

    private void initUI() {
        list_contact = (ListView) findViewById(R.id.list_contact);
        list_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取点中条目的索引指向集合中的对象
                if (mAdapter!=null){
                    HashMap<String,String> hashMap = mAdapter.getItem(position);
                    //获取当前条目集合中的电话号码
                    String phone = hashMap.get("phone");
                    //在结束此界面回到前一个导航界面的时候,需要将号码返回第三个界面
                    Intent intent = new Intent();
                    intent.putExtra("phone",phone);
                    setResult(0, intent);
                    finish();
                }
            }
        });
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return contactlist.size();
        }

        @Override
        public HashMap<String,String> getItem(int position) {
            return contactlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplication(),R.layout.listview_contact_item,null);
            //不能忘记添加view. 不然报空指针
            TextView contactName = (TextView) view.findViewById(R.id.contact_name);
            TextView contactNum = (TextView) view.findViewById(R.id.contact_num);
            contactName.setText(getItem(position).get("name"));
            contactNum.setText(getItem(position).get("phone"));
            return view;
        }
    }
}
