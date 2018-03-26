package com.example.xpl.map;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by xpl on 17-11-26.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper{

    public static final String CREATE_USER = "create table User("
            +"id integer primary key autoincrement,"
            +"firstname text, "
            +"lastname text,"
            +"phonenum text,"
            +"email text,"
            +"password text)";

    private Context mcontext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mcontext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
//        Toast.makeText(mcontext, "数据库创建成功！", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
