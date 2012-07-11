package com.boredprogrammers.onetouch;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.boredprogrammers.onetouch.data.loader.CommandLoader;
import com.boredprogrammers.onetouch.data.model.Command;
import com.boredprogrammers.onetouch.data.model.Server;
import com.boredprogrammers.onetouch.data.request.HttpService;
import com.boredprogrammers.onetouch.data.response.BaseResponse;
import com.boredprogrammers.onetouch.data.response.CommandResponse;
import com.boredprogrammers.onetouch.data.response.ServiceResponse;

public class CommandListFragment extends SherlockListFragment implements LoaderCallbacks<ServiceResponse<CommandResponse>> {
    private static final int DEFAULT_LOADER = -1;
    private CommandAdapter adapter;
    private Server server;

    private static class CommandAdapter extends BaseAdapter {
        private List<Command> commands;
        private final LayoutInflater inflater;

        public CommandAdapter(final Context ctx) {
            inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (commands != null) {
                return commands.size();
            } else {
                return 0;
            }
        }

        @Override
        public Command getItem(final int position) {
            if (position < commands.size()) {
                return commands.get(position);
            } else {
                return null;
            }
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, final ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.command_list_item, null);
            }
            final Command command = getItem(position);
            final TextView commandName = (TextView) view.findViewById(R.id.name);
            final TextView commandDescription = (TextView) view.findViewById(R.id.description);
            commandName.setText(command.title);
            commandDescription.setText(command.description);
            return view;
        }

        public void switchResponse(final ServiceResponse<CommandResponse> response) {
            if (response != null) {
                this.commands = response.result.commands;
            }
            notifyDataSetChanged();
        }

    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
        setListShown(false);
        adapter = new CommandAdapter(getSherlockActivity());
        setListAdapter(adapter);
    }

    @Override
    public Loader<ServiceResponse<CommandResponse>> onCreateLoader(final int id, final Bundle args) {
        return new CommandLoader(getSherlockActivity(), server.address, server.password);
    }

    @Override
    public void onLoadFinished(final Loader<ServiceResponse<CommandResponse>> loader, final ServiceResponse<CommandResponse> response) {
        if (response.error != null || response.result == null) {
            // TODO Show error, allow for rerun
        } else {
            adapter.switchResponse(response);
        }
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(final Loader<ServiceResponse<CommandResponse>> loader) {
        adapter.switchResponse(null);
    }

    @Override
    public void onListItemClick(final ListView list, final View view, final int position, final long id) {
        final Command command = adapter.getItem(position);
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(final Void... params) {
                final HttpService<BaseResponse> service = new HttpService<BaseResponse>(BaseResponse.class);
                HttpResponse response = null;
                try {
                    response = service.callForResponse(null, server.address + "/commands/" + command.shortName, server.password);
                    final int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        return "Command " + command.shortName + " failed with status: " + statusCode;
                    }
                } catch (final Exception e) {
                    return "Command " + command.shortName + " failed with error: " + e.getMessage();
                } finally {
                    if (response != null) {
                        try {
                            response.getEntity().consumeContent();
                        } catch (final IOException e) {
                            return "Failed to close command " + command.shortName + ": " + e.getMessage();
                        }
                    }
                }
                return "Command " + command.shortName + " ran successfully.";
            }

            @Override
            protected void onPostExecute(final String result) {
                Toast.makeText(getSherlockActivity(), result, Toast.LENGTH_SHORT).show();
            };

        }.execute();
    }

    public void setServer(final Server server) {
        this.server = server;
        getLoaderManager().initLoader(DEFAULT_LOADER, null, this);
    }
}
