package android.ye.mobileguard.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.ye.mobileguard.db.AppLockOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单号码数据库
 */
public class APPlockDao {
    private final Context context;
    //单例模式
    private AppLockOpenHelper applockOpenHelper;

    private APPlockDao(Context context){
        this.context = context;
        applockOpenHelper = new AppLockOpenHelper(context);
    }
    //声明类当前对象
    private static APPlockDao appLockDao = null;
    //创建静态方法
    public static APPlockDao getInstance(Context context){
        if (appLockDao == null){
            appLockDao = new APPlockDao(context);
        }
        return appLockDao;
    }


    /**
     * 插入一个条目
     * @param packageName
     */
    public void insert (String packageName){
        //开启数据库，作写入操作
        SQLiteDatabase db = applockOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("packagename", packageName);
        db.insert("applock", null, values);
        db.close();
        context.getContentResolver().notifyChange(Uri.parse("content://applock/change"), null);
    }

    /**
     * 删除一个条目
     * @param packageName
     */
    public void delete (String packageName){
        SQLiteDatabase db = applockOpenHelper.getWritableDatabase();
        db.delete("applock", "packagename = ?", new String[]{packageName});
        db.close();
        context.getContentResolver().notifyChange(Uri.parse("content://applock/change"), null);
    }

    /**
     * 查询所有
     * @return
     */
    public List<String> findAll(){
        SQLiteDatabase db = applockOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("applock", new String[]{"packagename"}, null, null, null, null, null);
        List<String> lockPackageList = new ArrayList<String>();
        while (cursor.moveToNext()){
            lockPackageList.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return lockPackageList;
    }
}
