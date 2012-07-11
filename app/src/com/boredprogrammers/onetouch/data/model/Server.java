package com.boredprogrammers.onetouch.data.model;

import android.database.Cursor;

import com.boredprogrammers.onetouch.data.table.ServerTable;

public final class Server {
    public final String name;
    public final String address;
    public final String description;
    public final String password;

    public Server(final Cursor cursor) {
        if (cursor.moveToFirst()) {
            name = cursor.getString(ServerTable.NAME_INDEX);
            address = cursor.getString(ServerTable.ADDRESS_INDEX);
            description = cursor.getString(ServerTable.DESCRIPTION_INDEX);
            password = cursor.getString(ServerTable.PASSWORD_INDEX);
        } else {
            name = null;
            address = null;
            description = null;
            password = null;
        }
    }
}
