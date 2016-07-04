package com.jcolladosp.pruebastripe;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Collado on 4/7/16.
 */
public class Utils
{
    public static JsonObjectRequest makeStringRequest(int method, String url, JSONObject body, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, final Context context) {
        return new JsonObjectRequest(method, url, body, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<>();
                }

                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response.statusCode == HttpURLConnection.HTTP_OK) {
                    try {
                        String value = new String(response.data);
                        return Response.success(new JSONObject("{\"OK\":\"" + value + "\"}"), HttpHeaderParser.parseCacheHeaders(response));
                    } catch (JSONException e) {
                        Log.e("tag", e.toString());
                        return null;
                    }
                } else {
                    return super.parseNetworkResponse(response);
                }
            }
        };
    }
}
