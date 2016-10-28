package android.ye.mobileguard.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询病毒数据库
 */
public class VirusDao {
   public static String path = "data/data/android.ye.mobileguard/files/antivirus.db";

    public static List<String> getVirusList(){
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
        Cursor cursor =db.query("datable", new String[]{"md5"}, null, null, null, null, null);
        List<String> virusList = new ArrayList<String>();
        while (cursor.moveToNext()){
            String md5 = cursor.getString(0);
            virusList.add(md5);
        }
        cursor.close();
        db.close();
        return virusList;
    }



}
