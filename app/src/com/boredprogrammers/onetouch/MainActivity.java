package com.boredprogrammers.onetouch;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.boredprogrammers.onetouch.dialog.AddServerDialog;
import com.boredprogrammers.onetouch.dialog.AddServerDialogListener;

public class MainActivity extends SherlockFragmentActivity implements AddServerDialogListener {
    private static final String ADD_SERVER_TAG = "add_server_frag";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_server:
                final FragmentManager fragMan = getSupportFragmentManager();
                final AddServerDialog addServerDlg = new AddServerDialog();
                addServerDlg.show(fragMan, ADD_SERVER_TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServerAdded() {
        Toast.makeText(this, R.string.server_added, Toast.LENGTH_LONG).show();
    }
}
