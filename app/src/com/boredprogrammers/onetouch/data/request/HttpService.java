package com.boredprogrammers.onetouch.data.request;

import java.io.EOFException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import android.util.Log;

import com.boredprogrammers.onetouch.data.response.BaseResponse;
import com.boredprogrammers.onetouch.data.response.ServiceError;
import com.boredprogrammers.onetouch.data.response.ServiceResponse;

public final class HttpService<T extends BaseResponse> {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final int STATUS_CODE_MIN_OK = 200;
    private static final int STATUS_CODE_MAX_OK = 300;
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static String HTTP_SERVICE_TAG = "HttpService";
    private static String CONTENT_TYPE = "application/json; charset=utf-8";
    private final Class<T> typeClass;
    private final ObjectMapper objectMapper;
    private static final HttpParams HTTP_PARAMS;
    static {
        HTTP_PARAMS = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(HTTP_PARAMS, 2000);
        HttpConnectionParams.setSoTimeout(HTTP_PARAMS, 4000);
    }

    public HttpService(final Class<T> typeClass) {
        this.typeClass = typeClass;
        this.objectMapper = new ObjectMapper();
        objectMapper.getSerializationConfig().without(Feature.USE_ANNOTATIONS);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public HttpResponse callForResponse(final ServiceRequest serviceRequest, final String endPoint, final String password) throws Exception {
        Log.d(HTTP_SERVICE_TAG, "Calling end point " + endPoint);
        try {
            final DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpUriRequest request;
            if (serviceRequest != null) {
                // We have request parameters, POST
                final HttpPost httpPost = new HttpPost(endPoint);
                httpPost.setHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE);
                final String json = objectMapper.writeValueAsString(serviceRequest);
                Log.d(HTTP_SERVICE_TAG, "Sending request " + json);
                httpPost.setEntity(new StringEntity(json));
                request = httpPost;
            } else {
                // No request parameters, GET
                request = new HttpGet(endPoint);
            }
            request.setHeader(AUTHORIZATION_HEADER, password);
            request.setParams(HTTP_PARAMS);
            return httpClient.execute(request);
        } catch (final EOFException e) {
            throw e;
        } catch (final Exception e) {
            throw e;
        }
    }

    public ServiceResponse<T> call(final ServiceRequest serviceRequest, final String endPoint, final String password) {
        final ServiceResponse<T> response = new ServiceResponse<T>();
        try {
            final HttpResponse httpResponse = callForResponse(serviceRequest, endPoint, password);
            final int statusCode = httpResponse.getStatusLine().getStatusCode();

            if (statusCode >= STATUS_CODE_MIN_OK && statusCode < STATUS_CODE_MAX_OK) {
                final T result = objectMapper.readValue(httpResponse.getEntity().getContent(), typeClass);
                response.result = result;
            } else {
                Log.e(HTTP_SERVICE_TAG, "Bad response from server, code: " + statusCode);
                response.error = new ServiceError(statusCode, "Bad response from server.");
            }
            Log.d(HTTP_SERVICE_TAG, "Got response for end point " + endPoint);
        } catch (final EOFException e) {
            Log.w(HTTP_SERVICE_TAG, "No response from end point " + endPoint);
            response.error = new ServiceError(e);
        } catch (final Exception e) {
            Log.e(HTTP_SERVICE_TAG, "An error occurred calling end point " + endPoint, e);
            response.error = new ServiceError(e);
        }
        return response;
    }
}
