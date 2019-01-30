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

    private static final String STEAMING_START = "streaming";

    private static final int ACTIVITY_CODE_STREAM = 7;

    private CallbackContext callbackContext;

    private static final String TAG = StreamingVideoLive.class.getSimpleName();

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;

        if (STEAMING_START.equals(action)) {
            return this.stream();
        }  else {
            callbackContext.error("streamingVideoLive." + action + " is not a supported method.");
            return false;
        }
    }

    private boolean stream() {
        return run(StreamRTSP.class);
    }

    private boolean run(final Class activityClass) {
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
