package com.boredprogrammers.onetouch.dialog;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpConnectionParams;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.boredprogrammers.onetouch.R;
import com.boredprogrammers.onetouch.data.model.Command;
import com.boredprogrammers.onetouch.data.model.CommandLine;
import com.boredprogrammers.onetouch.data.model.Server;
import com.boredprogrammers.onetouch.data.request.HttpService;

public final class RunCommandDialog extends SherlockDialogFragment {
    protected static final String TAG = RunCommandDialog.class.getName();
    private final Command command;
    private final Server server;
    private Handler handler;
    private boolean running;

    private static class OutputHandler extends Handler {
        private final TextView outputView;

        public OutputHandler(final TextView outputView) {
            this.outputView = outputView;
        }

        @Override
        public void handleMessage(final Message msg) {
            final String newResponses = (String) msg.obj;
            outputView.append(newResponses);
        }
    }

    public RunCommandDialog(final Server server, final Command command) {
        this.server = server;
        this.command = command;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.run_command, null);
        final TextView outputView = (TextView) view.findViewById(R.id.command_output);
        handler = new OutputHandler(outputView);
        final TextView commandName = (TextView) view.findViewById(R.id.command_name);
        commandName.setText(command.title);
        final TextView commandDescription = (TextView) view.findViewById(R.id.command_description);
        commandDescription.setText(command.description);
        final TextView commandFailOnError = (TextView) view.findViewById(R.id.command_fail);
        commandFailOnError.setText(String.valueOf(command.failOnError));
        final TextView commandExec = (TextView) view.findViewById(R.id.command_exec);
        for (final CommandLine commandLine : command.exec) {
            commandExec.append(commandLine.cmd + " " + TextUtils.join(" ", commandLine.args) + "\n");
        }
        final OnClickListener dismisser = new OnClickListener() {
            @Override
            public void onClick(final View view) {
                dismiss();
            };
        };
        final OnClickListener runner = new OnClickListener() {
            @Override
            public void onClick(final View view) {
                outputView.setText("");
                runCommand();
            }
        };
        final Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(dismisser);
        final Button runButton = (Button) view.findViewById(R.id.run_button);
        runButton.setOnClickListener(runner);
        final Button doneButton = (Button) view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(dismisser);

        return new AlertDialog.Builder(getSherlockActivity())
            .setTitle(R.string.run_command_title)
            .setView(view)
            .create();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void runCommand() {
        if (!running) {
            running = true;
            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(final Void... params) {
                    Log.d(TAG, "Executing command " + command.shortName);
                    AndroidHttpClient httpClient = null;
                    try {
                        httpClient = AndroidHttpClient.newInstance(HttpService.USER_AGENT);
                        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 1000);
                        HttpConnectionParams.setSoTimeout(httpClient.getParams(), 3000);
                        final HttpUriRequest request = new HttpGet(server.address + "/commands/" + command.shortName);
                        request.setHeader(HttpService.AUTHORIZATION_HEADER, server.password);
                        final HttpResponse httpResponse = httpClient.execute(request);
                        final int statusCode = httpResponse.getStatusLine().getStatusCode();

                        if (statusCode >= HttpService.STATUS_CODE_MIN_OK && statusCode < HttpService.STATUS_CODE_MAX_OK) {
                            final HttpEntity entity = httpResponse.getEntity();
                            if (entity != null) {
                                InputStream content = null;
                                try {
                                    content = AndroidHttpClient.getUngzippedContent(entity);
                                    final byte[] readBytes = new byte[8192];
                                    for (int numBytesRead; (numBytesRead = content.read(readBytes)) != -1;) {
                                        final Message msg = new Message();
                                        msg.obj = new String(readBytes, 0, numBytesRead);
                                        handler.sendMessage(msg);
                                    }
                                } finally {
                                    if (content != null) {
                                        try {
                                            content.close();
                                        } catch (final Exception e) {
                                            Log.w(TAG, "An error occurred closing connections", e);
                                        }
                                    }
                                    try {
                                        entity.consumeContent();
                                    } catch (final Exception e) {
                                        Log.w(TAG, "An error occurred closing connections", e);
                                    }
                                }
                            }
                        } else {
                            return "Command " + command.shortName + " failed with status: " + statusCode;
                        }
                    } catch (final Exception e) {
                        return "Command " + command.shortName + " failed with error: " + e;
                    } finally {
                        if (httpClient != null) {
                            httpClient.close();
                        }
                    }
                    return "Command " + command.shortName + " ran successfully.";
                }

                @Override
                protected void onPostExecute(final String result) {
                    final Message resultMsg = new Message();
                    resultMsg.obj = "\n" + result;
                    Log.d("RunCommand", "Dispatching msg " + resultMsg.obj);
                    handler.sendMessage(resultMsg);
                    running = false;
                };

            }.execute();
        }
    }
}
