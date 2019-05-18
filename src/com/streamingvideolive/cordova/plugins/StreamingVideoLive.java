package com.streamingvideolive.cordova.plugins;

import android.Manifest;
import android.content.Intent;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import android.widget.FrameLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StreamingVideoLive extends CordovaPlugin {

    private static final String TAG = StreamingVideoLive.class.getSimpleName();

    private StreamingFragment fragment = null;
    private int containerViewId = 20;
    private String urlStream;

    private static final String STEAMING_START = "streaming";
    private static final String STEAMING_POWER_OFF = "powerOff";

    private static final int ACTIVITY_CODE_STREAM = 7;

    private static final String CAMERA = Manifest.permission.CAMERA;
    private static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_DENIED_ERROR = "Permissions denied.";
    private static final int REQ_CODE = 500;

    private String[] permissions = {
            CAMERA,
            RECORD_AUDIO,
            WRITE_EXTERNAL_STORAGE
    };

    private CallbackContext callbackContext;
    private String _action;
    private JSONArray _args;

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        return runSwitch(action, args);
    }

    private boolean runSwitch(String action, JSONArray args) {
        _action = action;
        _args = args;
        if(hasPermission()) {
            switch (action) {
                case STEAMING_START:
                    try {
                        JSONObject options = args.getJSONObject(1);
                        urlStream = args.getString(0);
                        if (options.getString("mode").equals("fragment")) {
                            streamPreview(options.getJSONObject("preview"));
                        } else {
                            streamRTSP();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        callbackContext.error(e.toString());
                    }
                    break;
                case STEAMING_POWER_OFF: stopPreview(callbackContext);
                    break;
                default: callbackContext.error("streamingVideoLive." + action + " is not a supported method.");
                    break;
            }
        } else {
            readPermission(REQ_CODE);
            return true;
        }
        callbackContext.error("streamingVideoLive." + action + " is not permission");
        return false;
    }

    private void streamRTSP() {
        final CordovaInterface cordovaObj = cordova;
        final CordovaPlugin plugin = this;

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                final Intent streamIntent = new Intent(cordovaObj.getActivity().getApplicationContext(), StreamRTSP.class);

                /*
                Bundle extras = new Bundle();
                extras.putString("mediaUrl", url);

                if (options != null) {
                    Iterator<String> optKeys = options.keys();
                    while (optKeys.hasNext()) {
                        try {
                            final String optKey = (String)optKeys.next();
                            if (options.get(optKey).getClass().equals(String.class)) {
                                extras.putString(optKey, (String)options.get(optKey));
                                Log.v(TAG, "Added option: " + optKey + " -> " + String.valueOf(options.get(optKey)));
                            } else if (options.get(optKey).getClass().equals(Boolean.class)) {
                                extras.putBoolean(optKey, (Boolean)options.get(optKey));
                                Log.v(TAG, "Added option: " + optKey + " -> " + String.valueOf(options.get(optKey)));
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException while trying to read options. Skipping option.");
                        }
                    }
                    streamIntent.putExtras(extras);
                }*/

                cordovaObj.startActivityForResult(plugin, streamIntent, ACTIVITY_CODE_STREAM);
            }
        });
    }

    private void streamPreview(JSONObject preview) {
        Log.d(TAG, "streamPreview: " + preview.toString());
        if (fragment != null) {
            callbackContext.error("stream already started in " + urlStream );
        }

        final float opacity = Float.parseFloat("1");

        fragment = new StreamingFragment();

        DisplayMetrics metrics = cordova.getActivity().getResources().getDisplayMetrics();
        // offset
        float x = 0;
        float y = 0;
        float width = 500;
        float height = 500;
        try {
            x = (float) preview.getDouble("x");
            y = (float) preview.getDouble("y");
            width = (float) preview.getDouble("width");
            height = (float) preview.getDouble("height");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int computedX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, metrics);
        int computedY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, y, metrics);

        // size
        int computedWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, metrics);
        int computedHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, metrics);

        fragment.setRect(computedX, computedY, computedWidth, computedHeight);

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //create or update the layout params for the container view
                FrameLayout containerView = (FrameLayout) cordova.getActivity().findViewById(containerViewId);
                if(containerView == null){
                    containerView = new FrameLayout(cordova.getActivity().getApplicationContext());
                    containerView.setId(containerViewId);

                    FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    cordova.getActivity().addContentView(containerView, containerLayoutParams);
                }
                //display camera bellow the webview
                containerView.setAlpha(opacity);
                containerView.bringToFront();

                //add the fragment to the container
                FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(containerView.getId(), fragment);
                fragmentTransaction.commit();
            }
        });
    }

    private void stopPreview(CallbackContext callbackContext) {
        if (fragment != null) {
            FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
            fragment = null;
            callbackContext.success();
        } else {
            callbackContext.error("streaming is not initialized");
        }
    }

    public boolean hasPermission() {
        return ( cordova.hasPermission(CAMERA) &&
                cordova.hasPermission(RECORD_AUDIO) &&
                cordova.hasPermission(WRITE_EXTERNAL_STORAGE)
        );
    }

    private void readPermission(int requestCode) {
        cordova.requestPermissions(this, requestCode, permissions);
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        int i = 0;
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR +
                        " Request Code: " + REQ_CODE + ", Actions: " + _action + ", Permission: " + permissions[i]));
                callbackContext.error("streamingVideoLive: " +  permissions[i] + " permissions denied.");
                return;
            }
            i++;
        }

        if (requestCode == REQ_CODE) {
            this.runSwitch(_action, _args);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v(TAG, "onActivityResult: " + requestCode + " " + resultCode);
        super.onActivityResult(requestCode, resultCode, intent);
        if (ACTIVITY_CODE_STREAM == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Iniciada la actividad"));
            } else if (Activity.RESULT_CANCELED == resultCode) {
                String errMsg = "Error";
                if (intent != null && intent.hasExtra("message")) {
                    errMsg = intent.getStringExtra("message");
                }
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            }
        }
    }

}