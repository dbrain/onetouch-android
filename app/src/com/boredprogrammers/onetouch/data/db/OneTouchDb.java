package com.boredprogrammers.onetouch.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.boredprogrammers.onetouch.data.table.ServerTable;

public class OneTouchDb extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "onetouch.db";
    public static final int DATABASE_VERSION = 1;

    public OneTouchDb(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(ServerTable.getCreateStatement());
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int currentVersion, final int newVersion) {
        ServerTable.upgradeTable(db, currentVersion, newVersion);
    }

}
