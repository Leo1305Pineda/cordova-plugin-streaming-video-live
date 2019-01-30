package com.streamingvideolive.cordova.plugins.rtsp.rtsp;

import android.media.MediaCodec;
import android.util.Log;

import com.streamingvideolive.cordova.plugins.rtsp.rtcp.BaseSenderReport;
import com.streamingvideolive.cordova.plugins.rtsp.rtp.packets.AacPacket;
import com.streamingvideolive.cordova.plugins.rtsp.rtp.packets.AudioPacketCallback;
import com.streamingvideolive.cordova.plugins.rtsp.rtp.packets.BasePacket;
import com.streamingvideolive.cordova.plugins.rtsp.rtp.packets.H264Packet;
import com.streamingvideolive.cordova.plugins.rtsp.rtp.packets.H265Packet;
import com.streamingvideolive.cordova.plugins.rtsp.rtp.packets.VideoPacketCallback;
import com.streamingvideolive.cordova.plugins.rtsp.rtp.sockets.BaseRtpSocket;
import com.streamingvideolive.cordova.plugins.rtsp.utils.ConnectCheckerRtsp;
import com.streamingvideolive.cordova.plugins.rtsp.utils.RtpConstants;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created leonardo pineda on 29/01/19.
 */

public class RtspSender implements VideoPacketCallback, AudioPacketCallback {

  private final static String TAG = "RtspSender";
  private final BasePacket videoPacket;
  private final AacPacket aacPacket;
  private final BaseRtpSocket rtpSocket;
  private final BaseSenderReport baseSenderReport;
  private BlockingQueue<RtpFrame> rtpFrameBlockingQueue =
      new LinkedBlockingQueue<>(getCacheSize(10));
  private Thread thread;

  public RtspSender(ConnectCheckerRtsp connectCheckerRtsp, Protocol protocol, byte[] sps,
                    byte[] pps, byte[] vps, int sampleRate) {
    videoPacket =
        vps == null ? new H264Packet(sps, pps, this) : new H265Packet(sps, pps, vps, this);
    aacPacket = new AacPacket(sampleRate, this);
    rtpSocket = BaseRtpSocket.getInstance(connectCheckerRtsp, protocol);
    baseSenderReport = BaseSenderReport.getInstance(protocol);
  }

  /**
   * @param size in mb
   * @return number of packets
   */
  private int getCacheSize(int size) {
    return size * 1024 * 1024 / RtpConstants.MTU;
  }

  public void setDataStream(OutputStream outputStream, String host) {
    rtpSocket.setDataStream(outputStream, host);
    baseSenderReport.setDataStream(outputStream, host);
  }

  public void setVideoPorts(int rtpPort, int rtcpPort) {
    videoPacket.setPorts(rtpPort, rtcpPort);
  }

  public void setAudioPorts(int rtpPort, int rtcpPort) {
    aacPacket.setPorts(rtpPort, rtcpPort);
  }

  public void sendVideoFrame(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
    videoPacket.createAndSendPacket(h264Buffer, info);
  }

  public void sendAudioFrame(ByteBuffer aacBuffer, MediaCodec.BufferInfo info) {
    aacPacket.createAndSendPacket(aacBuffer, info);
  }

  @Override
  public void onVideoFrameCreated(RtpFrame rtpFrame) {
    try {
      rtpFrameBlockingQueue.add(rtpFrame);
    } catch (IllegalStateException e) {
      Log.i(TAG, "Video frame discarded");
    }
  }

  @Override
  public void onAudioFrameCreated(RtpFrame rtpFrame) {
    try {
      rtpFrameBlockingQueue.add(rtpFrame);
    } catch (IllegalStateException e) {
      Log.i(TAG, "Audio frame discarded");
    }
  }

  public void start() {
    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (!Thread.interrupted()) {
          try {
            RtpFrame rtpFrame = rtpFrameBlockingQueue.take();
            rtpSocket.sendFrame(rtpFrame);
            baseSenderReport.update(rtpFrame);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    });
    thread.start();
  }

  public void stop() {
    if (thread != null) {
      thread.interrupt();
      try {
        thread.join(100);
      } catch (InterruptedException e) {
        thread.interrupt();
      }
      thread = null;
    }
    rtpFrameBlockingQueue.clear();
    baseSenderReport.reset();
    baseSenderReport.close();
    rtpSocket.close();
    aacPacket.reset();
    videoPacket.reset();
  }
}
