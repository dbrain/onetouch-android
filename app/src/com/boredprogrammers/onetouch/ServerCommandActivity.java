package com.boredprogrammers.onetouch;

import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.boredprogrammers.onetouch.data.model.Server;
import com.boredprogrammers.onetouch.data.table.ServerTable;

public final class ServerCommandActivity extends SherlockFragmentActivity implements LoaderCallbacks<Cursor> {
    private static final int SERVER_LOADER = -1;
    private CommandListFragment commandList;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.server);
        commandList = (CommandListFragment) getSupportFragmentManager().findFragmentById(R.id.command_list);
        getSupportLoaderManager().initLoader(SERVER_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final long serverId = ContentUris.parseId(getIntent().getData());
        return new CursorLoader(this, ServerTable.CONTENT_URI, null, "_id = " + serverId, null, ServerTable.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        final Server server = new Server(cursor);
        getSupportActionBar().setTitle("Server: " + server.name);
        commandList.setServer(server);
        loader.abandon();
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
    }
}
