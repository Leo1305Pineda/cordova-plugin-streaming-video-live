package com.streamingvideolive.cordova.plugins.rtsp.utils;

/**
 * Created leonardo pineda on 29/01/19.
 */

public interface ConnectCheckerRtsp {

  void onConnectionSuccessRtsp();

  void onConnectionFailedRtsp(String reason);

  void onDisconnectRtsp();

  void onAuthErrorRtsp();

  void onAuthSuccessRtsp();
}
