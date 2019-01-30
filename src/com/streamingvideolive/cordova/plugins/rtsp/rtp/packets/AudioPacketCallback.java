package com.streamingvideolive.cordova.plugins.rtsp.rtp.packets;

import com.streamingvideolive.cordova.plugins.rtsp.rtsp.RtpFrame;

/**
 * Created leonardo pineda on 29/01/19.
 */

public interface AudioPacketCallback {
  void onAudioFrameCreated(RtpFrame rtpFrame);
}
