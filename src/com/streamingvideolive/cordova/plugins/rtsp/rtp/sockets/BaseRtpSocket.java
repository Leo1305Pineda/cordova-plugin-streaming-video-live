package com.streamingvideolive.cordova.plugins.rtsp.rtp.sockets;

import com.streamingvideolive.cordova.plugins.rtsp.rtsp.Protocol;
import com.streamingvideolive.cordova.plugins.rtsp.rtsp.RtpFrame;
import com.streamingvideolive.cordova.plugins.rtsp.utils.ConnectCheckerRtsp;

import java.io.OutputStream;

/**
 * Created leonardo pineda on 29/01/19.
 */

public abstract class BaseRtpSocket {

  protected final static String TAG = "BaseRtpSocket";
  protected ConnectCheckerRtsp connectCheckerRtsp;

  BaseRtpSocket(ConnectCheckerRtsp connectCheckerRtsp) {
    this.connectCheckerRtsp = connectCheckerRtsp;
  }

  public static BaseRtpSocket getInstance(ConnectCheckerRtsp connectCheckerRtsp,
      Protocol protocol) {
    return protocol == Protocol.TCP ? new RtpSocketTcp(connectCheckerRtsp)
        : new RtpSocketUdp(connectCheckerRtsp);
  }

  public abstract void setDataStream(OutputStream outputStream, String host);

  public abstract void sendFrame(RtpFrame rtpFrame);

  public abstract void close();
}
