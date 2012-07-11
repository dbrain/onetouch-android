package com.boredprogrammers.onetouch.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.boredprogrammers.onetouch.R;
import com.boredprogrammers.onetouch.data.table.ServerTable;

public final class AddServerDialog extends SherlockDialogFragment implements OnEditorActionListener {
    private SherlockFragmentActivity activity;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.add_server, null);
        final EditText serverName = (EditText) view.findViewById(R.id.server_name);
        final EditText description = (EditText) view.findViewById(R.id.server_description);

        serverName.requestFocus();
        description.setOnEditorActionListener(this);

        return new AlertDialog.Builder(getSherlockActivity())
            .setTitle(R.string.add_server_title)
            .setView(view)
            .setNegativeButton(R.string.cancel_button, new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    dismiss();
                }
            })
            .setPositiveButton(R.string.save_button, new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    saveServerAndDismiss();
                }
            })
            .create();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        activity = getSherlockActivity();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void saveServerAndDismiss() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... params) {
                final Dialog dialog = getDialog();
                final EditText serverName = (EditText) dialog.findViewById(R.id.server_name);
                final Spinner serverTypeSpinner = (Spinner) dialog.findViewById(R.id.server_type);
                final EditText serverAddress = (EditText) dialog.findViewById(R.id.server_address);
                final EditText serverDescription = (EditText) dialog.findViewById(R.id.server_description);
                final EditText serverPassword = (EditText) dialog.findViewById(R.id.server_password);

                String fullAddress = serverTypeSpinner.getSelectedItem() + serverAddress.getText().toString();
                if (fullAddress.endsWith("/")) {
                    fullAddress = fullAddress.substring(0, fullAddress.length() - 1);
                }

                final ContentValues server = new ContentValues(4);
                server.put(ServerTable.NAME, serverName.getText().toString());
                server.put(ServerTable.ADDRESS, fullAddress);
                server.put(ServerTable.DESCRIPTION, serverDescription.getText().toString());
                server.put(ServerTable.PASSWORD, serverPassword.getText().toString());

                final ContentResolver contentResolver = activity.getContentResolver();
                contentResolver.insert(ServerTable.CONTENT_URI, server);
                return null;
            }

            @Override
            protected void onPostExecute(final Void result) {
                final AddServerDialogListener addServerListener = (AddServerDialogListener) activity;
                addServerListener.onServerAdded();
                dismiss();
            }

        }.execute();
    }

    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            saveServerAndDismiss();
            return true;
        }
        return false;
    }
}
