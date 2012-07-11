package com.boredprogrammers.onetouch.data.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.boredprogrammers.onetouch.data.request.HttpService;
import com.boredprogrammers.onetouch.data.response.CommandResponse;
import com.boredprogrammers.onetouch.data.response.ServiceResponse;

public final class CommandLoader extends AsyncTaskLoader<ServiceResponse<CommandResponse>> {
    private static final String COMMAND_SUFFIX = "/commands";
    private static final String TEAM_LOADER_TAG = "TeamLoader";
    private static final HttpService<CommandResponse> HTTP_SERVICE = new HttpService<CommandResponse>(CommandResponse.class);
    private ServiceResponse<CommandResponse> currentResponse;
    private final String url;
    private final String password;

    public CommandLoader(final Context context, final String url, final String password) {
        super(context);
        this.url = url;
        this.password = password;
    }

    @Override
    public ServiceResponse<CommandResponse> loadInBackground() {
        Log.d(TEAM_LOADER_TAG, "Loading teams from server...");
        return HTTP_SERVICE.call(null, url + COMMAND_SUFFIX, password);
    }

    @Override
    public void deliverResult(final ServiceResponse<CommandResponse> newResponse) {
        currentResponse = newResponse;
        if (isStarted()) {
            super.deliverResult(newResponse);
        }
    }

    @Override
    protected void onStartLoading() {
        if (currentResponse != null) {
            deliverResult(currentResponse);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        currentResponse = null;
    }
}
