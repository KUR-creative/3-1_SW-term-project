package com.example.kouram.usetmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ko U Ram on 2017-06-02.
 */

class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    /*
     * Database가 존재하지 않을 때, 딱 한번 실행됨.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exist data( _id INTEGER primary key autoincremet, "
                                                        + "nmae TEXT, age INTEGER, address TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "drop table if exists data";
        db.execSQL(sql);

        onCreate(db);
    }
}
