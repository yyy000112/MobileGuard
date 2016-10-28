package android.ye.mobileguard.engine;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 常用号码数据库
 */
public class CommonNumberDao {
    //数据库路径
    public static String path = "data/data/android.ye.mobileguard/files/commonnum.db";

    //获取数据组
    public List<Group> getGroup(){
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.query("classlist", new String[]{"name", "idx"}, null, null, null, null, null);
        List<Group> groupList = new ArrayList<Group>();
        while (cursor.moveToNext()){
            Group group = new Group();
            group.name = cursor.getString(0);
            group.idx = cursor.getString(1);
            group.children = getChild(group.idx);
            groupList.add(group);
        }
        cursor.close();
        db.close();
        return groupList;
    }

    //获取子节点数据
    public List<Child> getChild(String idx){
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.rawQuery("select * from table" +idx+ ";", null);
        List<Child> childList = new ArrayList<Child>();
        while (cursor.moveToNext()){
            Child child = new Child();
            child._id = cursor.getString(0);
            child.number = cursor.getString(1);
            child.name = cursor.getString(2);
            childList.add(child);
        }
        cursor.close();
        db.close();
        return childList;
    }

   public class Group{
        public String name;
        public String idx;
       public List<Child> children;
    }
   public class Child{
        public String _id;
        public String number;
        public String name;
    }

}
