package com.streamingvideolive.cordova.plugins;

import android.content.Intent;

import android.app.Activity;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;

public class StreamingVideoLive extends CordovaPlugin {

    private static final String TAG = StreamingVideoLive.class.getSimpleName();

    private static final String STEAMING_START = "streaming";

    private static final int ACTIVITY_CODE_STREAM = 7;

    private static final String CAMERA          = Manifest.permission.CAMERA;
    private static final String FLASHLIGHT      = Manifest.permission.FLASHLIGHT;
    private static final String RECORD_AUDIO    = Manifest.permission.RECORD_AUDIO;
    private static final int REQ_CODE = 500;

    private String[] permissions = {
        CAMERA, 
        FLASHLIGHT, 
        RECORD_AUDIO
    };

    private CallbackContext callbackContext;
    private String _action;
    private JSONArray _args;

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        return runSwitch(action, args);
/*
        if (STEAMING_START.equals(action)) {
            return this.stream();
        }  else {
            callbackContext.error("streamingVideoLive." + action + " is not a supported method.");
            return false;
        }
        */
    }

    public boolean runSwitch(String action, JSONArray args) {
        _action = action;
        _args = args;
        if(hasPermission()) {
            switch (action) {
                case STEAMING_START: streamRTSP(StreamRTSP.class);
                return true;
            }
        } else {
            readPermission(REQ_CODE);
            return true;
        }
        callbackContext.error("streamingVideoLive." + action + " is not a supported method.");
        return false; 
    }

    private boolean streamRTSP(final Class activityClass) {
        final CordovaInterface cordovaObj = cordova;
        final CordovaPlugin plugin = this;

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                final Intent streamIntent = new Intent(cordovaObj.getActivity().getApplicationContext(), activityClass);

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
        return true;
    }

    public boolean hasPermission() {
        return ( cordova.hasPermission(CAMERA) &&
            cordova.hasPermission(FLASHLIGHT) &&
            cordova.hasPermission(RECORD_AUDIO))
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