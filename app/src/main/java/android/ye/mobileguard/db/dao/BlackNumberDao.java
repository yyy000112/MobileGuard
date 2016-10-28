package android.ye.mobileguard.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.ye.mobileguard.db.BlackNumberOpenHelper;
import android.ye.mobileguard.db.domain.BlackNumberInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单号码数据库
 */
public class BlackNumberDao {
    //单例模式
    private BlackNumberOpenHelper blackNumberOpenHelper;

    private BlackNumberDao(Context context){
        blackNumberOpenHelper = new BlackNumberOpenHelper(context);
    }
    //声明类当前对象
    private static BlackNumberDao blackNumberDao = null;
    //创建静态方法
    public static BlackNumberDao getInstance(Context context){
        if (blackNumberDao == null){
            blackNumberDao = new BlackNumberDao(context);
        }
        return blackNumberDao;
    }


    /**
     * 增加一个条目
     * @param phone 拦截的电话号码
     * @param mode 拦截类型（1，短信，2，电话，3，电话和短信）
     */
    public void insert (String phone, String mode){
        //开启数据库，作写入操作
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("phone",phone);
        values.put("mode",mode);
        db.insert("blacknumber",null,values);
        db.close();
    }

    /**
     * 删除一个条目
     * @param phone 根据电话号码
     */
    public void delete (String phone){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        db.delete("blacknumber", "phone = ?", new String[]{phone});
        db.close();

    }

    /**
     * 更新一个条目
     * @param phone
     * @param mode
     */
    public void update(String phone, String mode){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("mode",mode);
        db.update("blacknumber", contentValues, "phone = ?", new String[]{phone});
        db.close();
    }

    public List<BlackNumberInfo> findAll(){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("blacknumber", new String[]{"phone", "mode"}, null, null, null, null, "_id desc");
        List<BlackNumberInfo> blackNumberInfoList = new ArrayList<BlackNumberInfo>();
        while (cursor.moveToNext()){
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            blackNumberInfo.phone = cursor.getString(0);
            blackNumberInfo.mode = cursor.getString(1);
            blackNumberInfoList.add(blackNumberInfo);
        }
        cursor.close();
        db.close();
        return blackNumberInfoList;
    }

    /**
     * 分页查询
     * 每次只查询20条
     * @param index 为起始位置的索引值
     */
    public List<BlackNumberInfo> find(int index){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select phone,mode from blacknumber order by _id desc limit ?,20;", new String[]{String.valueOf(index)});
        List<BlackNumberInfo> blackNumberInfoList = new ArrayList<BlackNumberInfo>();
        while (cursor.moveToNext()){
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            blackNumberInfo.phone = cursor.getString(0);
            blackNumberInfo.mode = cursor.getString(1);
            blackNumberInfoList.add(blackNumberInfo);
        }
        cursor.close();
        db.close();
        return blackNumberInfoList;
    }

    /**
     *
     * @return 数据库中数据的总条目个数，返回0代表没有数据或异常
     */
    public int getCount(){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery("select count(*) from blacknumber;", null);
        if (cursor.moveToNext()){
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;

    }

    /**
     *
     * @param phone 作为查询条件的电话号码
     * @return 传入电话号码的拦截模式
     */
    public int getMode(String phone){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        int mode = 0 ;
        Cursor cursor = db.query("blacknumber",new String[]{"mode"},"phone = ?",new String[]{phone},null,null,null,null);
        if (cursor.moveToNext()){
            mode = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return mode;
    }

}
