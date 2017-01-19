package com.paladin.ink;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by jason on 11/5/16.
 */
public class Api {
//    private static final String API_ENDPOINT = "http://localhost:3000/api/";
    private static final String API_ENDPOINT = "https://serene-headland-71291.herokuapp.com/api/";
    private String USER_ID;
    private RequestQueue requestQueue;

    public Api(Context c, String userId) {
        this.USER_ID = userId;
        requestQueue = Volley.newRequestQueue(c);
    }

    public String getUserId() {
        return USER_ID;
    }

    public void sendPicture(String fromId, String toId, String image64, OnPictureSent listener) {
        String url = API_ENDPOINT + "send";
        JSONObject param = new JSONObject();
        try {
            param.put("fromId", fromId);
            param.put("toId", toId);
            param.put("image64", image64);
        } catch (JSONException e) {}

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

    }
    public void getPendingPics(final OnPendingPicsLoaded listener) {
        String url = API_ENDPOINT + "photos/" + USER_ID;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("INKANDROID", "Number of pending pics: " + response.length());
                Iterator<String> keys = response.keys();
                ArrayList<Picture> list = new ArrayList<>();
                while (keys.hasNext()) {
                    try {
                        String key = (String) keys.next();
                        JSONObject pic = response.getJSONObject(key);
                        Picture p = new Picture(key, pic.getString("photoUrl"));
                        list.add(p);
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                }
                listener.onPendingPicsLoaded(list);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void deletePicture(String picId) {
        Log.d("INKLOCK", "removing " + picId);
        String url = API_ENDPOINT + "viewed/" + picId;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, null, null);
        requestQueue.add(req);
    }
    public static void signUpUser(final Context c, String username, String pass, final OnApiResult listener) {
        String url = API_ENDPOINT + "register";
        JSONObject params = new JSONObject();
        try {
            params.put("username", username);
            params.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
            listener.onActionComplete(2);
        }
        RequestQueue queue = Volley.newRequestQueue(c);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(!response.has("errmsg")) {
                    String authKey = null;
                    try {
                        authKey = response.getString("_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences.Editor prefsEditor = c.getSharedPreferences("inklocksharedprefs", MODE_PRIVATE).edit();
                    assert authKey != null;
                    prefsEditor.putString("auth_key", authKey);
                    prefsEditor.commit();
                    listener.onActionComplete(0);
                } else {
                    listener.onActionComplete(1);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onActionComplete(1);
            }
        });
        queue.add(jsonObjectRequest);
    }
    public static void loginUser(final Context c, String username, String pass, final OnApiResult listener) {
        String url = API_ENDPOINT + "login";
        JSONObject params = new JSONObject();
        try {
            params.put("username", username);
            params.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
            listener.onActionComplete(2);
        }
        RequestQueue queue = Volley.newRequestQueue(c);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("INKLOCK", response.toString());
                if(response.length() != 0) {
                    String authKey = null;
                    try {
                        authKey = response.getString("_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences.Editor prefsEditor = c.getSharedPreferences("inklocksharedprefs", MODE_PRIVATE).edit();
                    assert authKey != null;
                    prefsEditor.putString("auth_key", authKey);
                    prefsEditor.commit();
                    listener.onActionComplete(0);
                } else {
                    listener.onActionComplete(1);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onActionComplete(1);
            }
        });
        queue.add(jsonObjectRequest);
    }

    public interface OnApiResult {
        void onActionComplete(int status);
    }
    public interface OnPendingPicsLoaded {
        void onPendingPicsLoaded(ArrayList<Picture> pictures);
    }
    public interface OnPictureSent {
        void onPictureSent(boolean success);
    }

}
