package com.boredprogrammers.onetouch.data.request;

import java.io.EOFException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpConnectionParams;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.boredprogrammers.onetouch.data.response.BaseResponse;
import com.boredprogrammers.onetouch.data.response.ServiceError;
import com.boredprogrammers.onetouch.data.response.ServiceResponse;

public final class HttpService<T extends BaseResponse> {
    public static final String USER_AGENT = "Android";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final int STATUS_CODE_MIN_OK = 200;
    public static final int STATUS_CODE_MAX_OK = 300;
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static String HTTP_SERVICE_TAG = "HttpService";
    private static String CONTENT_TYPE = "application/json; charset=utf-8";
    private final Class<T> typeClass;
    private final ObjectMapper objectMapper;

    public HttpService(final Class<T> typeClass) {
        this.typeClass = typeClass;
        this.objectMapper = new ObjectMapper();
        objectMapper.getSerializationConfig().without(Feature.USE_ANNOTATIONS);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ServiceResponse<T> call(final ServiceRequest serviceRequest, final String endPoint, final String password) {
        Log.d(HTTP_SERVICE_TAG, "Calling endpoint " + endPoint);
        final ServiceResponse<T> response = new ServiceResponse<T>();
        AndroidHttpClient httpClient = null;
        try {
            httpClient = AndroidHttpClient.newInstance(USER_AGENT);
            HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 3000);
            HttpConnectionParams.setSoTimeout(httpClient.getParams(), 120000);
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
            final HttpResponse httpResponse = httpClient.execute(request);
            final int statusCode = httpResponse.getStatusLine().getStatusCode();

            if (statusCode >= STATUS_CODE_MIN_OK && statusCode < STATUS_CODE_MAX_OK) {
                final HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    InputStream content = null;
                    try {
                        content = AndroidHttpClient.getUngzippedContent(entity);
                        final T result = objectMapper.readValue(content, typeClass);
                        response.result = result;
                    } finally {
                        if (content != null) {
                            content.close();
                        }
                        entity.consumeContent();
                    }
                }
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
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return response;
    }
}
