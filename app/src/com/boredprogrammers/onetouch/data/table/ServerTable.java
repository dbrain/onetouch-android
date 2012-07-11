package com.boredprogrammers.onetouch.data.table;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.boredprogrammers.onetouch.data.provider.ServerProvider;

public class ServerTable implements BaseColumns {
    public static final String TABLE_NAME = "servers";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.boredprogrammers.server";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.boredprogrammers.server";

    public static final Uri CONTENT_URI = Uri.withAppendedPath(ServerProvider.CONTENT_URI, TABLE_NAME);

    // Column names
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String ADDRESS = "address";
    public static final String PASSWORD = "password";

    public static final String DEFAULT_SORT_ORDER = NAME + " ASC";
    public static final String[] COLUMNS = { _ID, NAME, DESCRIPTION, ADDRESS, PASSWORD };

    // Indexes for columns when using default select (COLUMNS)
    public static final int _ID_INDEX = 0;
    public static final int NAME_INDEX = 1;
    public static final int DESCRIPTION_INDEX = 2;
    public static final int ADDRESS_INDEX = 3;
    public static final int PASSWORD_INDEX = 4;

    public static final String getCreateStatement() {
        return "CREATE TABLE " + TABLE_NAME
            + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NAME + " TEXT, "
            + DESCRIPTION + " TEXT, "
            + ADDRESS + " TEXT, "
            + PASSWORD + " TEXT);";
    }

    public static final void upgradeTable(final SQLiteDatabase db, final int currentVersion, final int newVersion) {
    }
}
