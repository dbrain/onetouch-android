package com.boredprogrammers.onetouch.data.provider;

import static android.provider.BaseColumns._ID;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.boredprogrammers.onetouch.data.db.OneTouchDb;
import com.boredprogrammers.onetouch.data.table.ServerTable;

public final class ServerProvider extends ContentProvider {
    private OneTouchDb dbOpenHelper;
    public static final String AUTHORITY = "com.boredprogrammers.onetouch.data.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/");

    private static final int SERVERS = 1;
    private static final int SERVER_ID = 2;

    private static final UriMatcher URI_MATCHER;
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, ServerTable.TABLE_NAME, SERVERS);
        URI_MATCHER.addURI(AUTHORITY, ServerTable.TABLE_NAME + "/#", SERVER_ID);
    }

    @Override
    public boolean onCreate() {
        dbOpenHelper = new OneTouchDb(getContext());
        return true;
    }

    @Override
    public String getType(final Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case SERVERS:
                return ServerTable.CONTENT_TYPE;
            case SERVER_ID:
                return ServerTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }
    }

    @Override
    public Cursor query(final Uri uri, String[] projection, final String selection, final String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        // Defaults to use if not specified
        String defaultSortOrder;
        String[] defaultColumns;

        switch (URI_MATCHER.match(uri)) {
            case SERVERS:
                qb.setTables(ServerTable.TABLE_NAME);
                defaultSortOrder = ServerTable.DEFAULT_SORT_ORDER;
                defaultColumns = ServerTable.COLUMNS;
                break;
            case SERVER_ID:
                qb.setTables(ServerTable.TABLE_NAME);
                qb.appendWhere(appendRowId(selection, uri.getLastPathSegment()));
                defaultSortOrder = ServerTable.DEFAULT_SORT_ORDER;
                defaultColumns = ServerTable.COLUMNS;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = defaultSortOrder;
        }

        if (projection == null) {
            projection = defaultColumns;
        }

        // Execute the query
        final SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        final Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Watch the data for changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        String table;

        switch (URI_MATCHER.match(uri)) {
            case SERVERS:
                table = ServerTable.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        final SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        final long rowId = db.insertOrThrow(table, null, values);

        // Return the new uri and notify of the change
        Uri itemUri = Uri.withAppendedPath(CONTENT_URI, table);
        itemUri = ContentUris.withAppendedId(itemUri, rowId);
        getContext().getContentResolver().notifyChange(itemUri, null);
        return itemUri;
    }

    @Override
    public int update(final Uri uri, final ContentValues values, String selection, final String[] selectionArgs) {
        String table;

        switch (URI_MATCHER.match(uri)) {
            case SERVERS:
                table = ServerTable.TABLE_NAME;
                break;
            case SERVER_ID:
                table = ServerTable.TABLE_NAME;
                selection = appendRowId(selection, uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        // Execute the update
        final SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        final int updatedCount = db.update(table, values, selection, selectionArgs);

        // Notify that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);
        return updatedCount;
    }

    @Override
    public int delete(final Uri uri, String selection, final String[] selectionArgs) {
        String table;

        switch (URI_MATCHER.match(uri)) {
            case SERVERS:
                table = ServerTable.TABLE_NAME;
                break;
            case SERVER_ID:
                table = ServerTable.TABLE_NAME;
                selection = appendRowId(selection, uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        // Execute the deletion
        final SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        final int deletedCount = db.delete(table, selection, selectionArgs);

        // Notify the resolver of the changes
        getContext().getContentResolver().notifyChange(uri, null);
        return deletedCount;
    }

    private String appendRowId(final String selection, final String id) {
        return appendRowId(selection, Long.parseLong(id));
    }

    private String appendRowId(final String selection, final long id) {
        return _ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
    }

}
