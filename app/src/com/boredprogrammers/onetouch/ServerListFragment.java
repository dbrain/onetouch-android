package com.boredprogrammers.onetouch;

import static com.boredprogrammers.onetouch.data.table.ServerTable.ADDRESS;
import static com.boredprogrammers.onetouch.data.table.ServerTable.ADDRESS_INDEX;
import static com.boredprogrammers.onetouch.data.table.ServerTable.CONTENT_URI;
import static com.boredprogrammers.onetouch.data.table.ServerTable.DEFAULT_SORT_ORDER;
import static com.boredprogrammers.onetouch.data.table.ServerTable.DESCRIPTION;
import static com.boredprogrammers.onetouch.data.table.ServerTable.NAME;
import static com.boredprogrammers.onetouch.data.table.ServerTable.PASSWORD_INDEX;
import static com.boredprogrammers.onetouch.data.table.ServerTable._ID_INDEX;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.boredprogrammers.onetouch.data.request.HttpService;
import com.boredprogrammers.onetouch.data.response.BaseResponse;

public class ServerListFragment extends SherlockListFragment implements LoaderCallbacks<Cursor>, OnItemLongClickListener {
    private static final String TAG = "ServerList";
    private static final int DEFAULT_LOADER = -1;
    private ServerCursorAdapter adapter;
    protected ActionMode actionMode;
    private final SparseBooleanArray serverStatuses = new SparseBooleanArray(10);

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
            mode.setTitle("1 selected");
            mode.getMenuInflater().inflate(R.menu.server_cab_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            if (item.getItemId() == R.id.delete) {
                adapter.deleteSelectedItems();
                getListView().scrollTo(0, 0);
                mode.setTitle("0 selected");
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(final ActionMode mode) {
            actionMode = null;
            adapter.setSelectionMode(false, -1);
        }
    };

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListShown(false);
        adapter = new ServerCursorAdapter(getSherlockActivity(), R.layout.server_list_item, null,
            new String[] { NAME, DESCRIPTION, ADDRESS },
            new int[] { R.id.name, R.id.description, R.id.address });
        setListAdapter(adapter);
        getListView().setOnItemLongClickListener(this);
        getLoaderManager().initLoader(DEFAULT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new CursorLoader(getSherlockActivity(), CONTENT_URI, null, null, null, DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        adapter.changeCursor(cursor);
        checkNetworkStatuses();
        setListShown(true);
    }

    private void checkNetworkStatuses() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... params) {
                Cursor servers = null;
                try {
                    final ContentResolver contentResolver = getSherlockActivity().getContentResolver();
                    servers = contentResolver.query(CONTENT_URI, null, null, null, DEFAULT_SORT_ORDER);
                    while (servers.moveToNext()) {
                        checkNetworkStatus(servers.getLong(_ID_INDEX), servers.getString(ADDRESS_INDEX), servers.getString(PASSWORD_INDEX));
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "An error occurred checking server status: " + e.getMessage());
                } finally {
                    if (servers != null) {
                        servers.close();
                    }
                }
                return null;
            }

        }.execute();
    }

    private void checkNetworkStatus(final Long serverId, final String address, final String password) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(final Void... params) {
                final HttpService<BaseResponse> httpService = new HttpService<BaseResponse>(BaseResponse.class);
                try {
                    final HttpResponse response = httpService.callForResponse(null, address + "/info", password);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        return true;
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "An error occurred checking server status: " + e.getMessage());
                }
                Log.d(TAG, "Could not contact " + address);
                return false;
            }

            @Override
            protected void onPostExecute(final Boolean result) {
                serverStatuses.put(serverId.intValue(), result);
                adapter.notifyDataSetChanged();
            };

        }.execute();
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }

    protected static class OnServerSelectedListener implements OnCheckedChangeListener {
        private final List<Long> selectedItems;
        private final Long itemId;
        private final ActionMode actionMode;

        public OnServerSelectedListener(final ActionMode actionMode, final Long itemId, final List<Long> selectedItems) {
            this.actionMode = actionMode;
            this.itemId = itemId;
            this.selectedItems = selectedItems;
        }

        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            if (isChecked && !selectedItems.contains(itemId)) {
                selectedItems.add(itemId);
            } else if (!isChecked) {
                selectedItems.remove(itemId);
            }
            actionMode.setTitle(selectedItems.size() + " selected");
        }
    }

    private final class ServerCursorAdapter extends SimpleCursorAdapter {
        protected boolean selectMode;
        protected final List<Long> selectedItems = new ArrayList<Long>();

        @SuppressWarnings("deprecation")
        public ServerCursorAdapter(final Context context, final int layout, final Cursor c, final String[] from, final int[] to) {
            super(context, layout, c, from, to);
        }

        public void setSelectionMode(final boolean selectMode, final long id) {
            selectedItems.clear();
            if (id != -1) {
                selectedItems.add(id);
            }
            this.selectMode = selectMode;
            adapter.notifyDataSetChanged();
        }

        public void deleteSelectedItems() {
            new AsyncTask<Void, Void, Integer>() {
                private Long[] itemsToDelete;

                @Override
                protected void onPreExecute() {
                    itemsToDelete = selectedItems.toArray(new Long[selectedItems.size()]);
                    getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
                };

                @Override
                protected Integer doInBackground(final Void... params) {
                    final ContentResolver contentResolver = getSherlockActivity().getContentResolver();
                    return contentResolver.delete(CONTENT_URI, "_id IN (" + TextUtils.join(",", itemsToDelete) + ")", null);
                }

                @Override
                protected void onPostExecute(final Integer result) {
                    selectedItems.clear();
                    Toast.makeText(getSherlockActivity(), getString(R.string.servers_deleted, result), Toast.LENGTH_LONG).show();
                    getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                }

            }.execute();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final Long itemId = ((Cursor) getItem(position)).getLong(_ID_INDEX);
            final View view = super.getView(position, convertView, parent);
            final View serverStatus = view.findViewById(R.id.server_status);
            final CheckBox deleteCheckbox = (CheckBox) view.findViewById(R.id.delete);
            deleteCheckbox.setOnCheckedChangeListener(new OnServerSelectedListener(actionMode, itemId, selectedItems));
            if (selectMode) {
                deleteCheckbox.setChecked(selectedItems.contains(itemId));
                deleteCheckbox.setVisibility(View.VISIBLE);
            } else {
                deleteCheckbox.setVisibility(View.GONE);
            }
            if (serverStatuses.indexOfKey(itemId.intValue()) < 0) {
                serverStatus.setBackgroundResource(R.drawable.checking_icon);
            } else {
                if (serverStatuses.get(itemId.intValue())) {
                    serverStatus.setBackgroundResource(R.drawable.online_icon);
                } else {
                    serverStatus.setBackgroundResource(R.drawable.offline_icon);
                }
            }
            return view;
        }
    }

    @Override
    public void onListItemClick(final ListView list, final View view, final int position, final long id) {
        final Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> list, final View view, final int position, final long id) {
        actionMode = getSherlockActivity().startActionMode(actionModeCallback);
        adapter.setSelectionMode(true, id);
        return true;
    }

}
