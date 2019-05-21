package com.streamingvideolive.cordova.plugins.rtplibrary.rtsp;

import android.content.Context;
import android.media.MediaCodec;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.SurfaceView;
import android.view.TextureView;
import com.streamingvideolive.cordova.plugins.encoder.utils.CodecUtil;
import com.streamingvideolive.cordova.plugins.rtplibrary.base.Camera1Base;
import com.streamingvideolive.cordova.plugins.rtplibrary.view.LightOpenGlView;
import com.streamingvideolive.cordova.plugins.rtplibrary.view.OpenGlView;
import com.streamingvideolive.cordova.plugins.rtsp.rtsp.Protocol;
import com.streamingvideolive.cordova.plugins.rtsp.rtsp.RtspClient;
import com.streamingvideolive.cordova.plugins.rtsp.rtsp.VideoCodec;
import com.streamingvideolive.cordova.plugins.rtsp.utils.ConnectCheckerRtsp;
import java.nio.ByteBuffer;

/**
 * More documentation see:
 * {@link com.streamingvideolive.cordova.plugins.rtplibrary.base.Camera1Base}
 *
 * Created leonardo pineda on 29/01/19.
 */

public class RtspCamera1 extends Camera1Base {

  private RtspClient rtspClient;

  public RtspCamera1(SurfaceView surfaceView, ConnectCheckerRtsp connectCheckerRtsp) {
    super(surfaceView);
    rtspClient = new RtspClient(connectCheckerRtsp);
  }

  public RtspCamera1(TextureView textureView, ConnectCheckerRtsp connectCheckerRtsp) {
    super(textureView);
    rtspClient = new RtspClient(connectCheckerRtsp);
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  public RtspCamera1(OpenGlView openGlView, ConnectCheckerRtsp connectCheckerRtsp) {
    super(openGlView);
    rtspClient = new RtspClient(connectCheckerRtsp);
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  public RtspCamera1(LightOpenGlView lightOpenGlView, ConnectCheckerRtsp connectCheckerRtsp) {
    super(lightOpenGlView);
    rtspClient = new RtspClient(connectCheckerRtsp);
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  public RtspCamera1(Context context, ConnectCheckerRtsp connectCheckerRtsp) {
    super(context);
    rtspClient = new RtspClient(connectCheckerRtsp);
  }

  /**
   * Internet protocol used.
   *
   * @param protocol Could be Protocol.TCP or Protocol.UDP.
   */
  public void setProtocol(Protocol protocol) {
    rtspClient.setProtocol(protocol);
  }

  public void setVideoCodec(VideoCodec videoCodec) {
    videoEncoder.setType(videoCodec == VideoCodec.H265 ? CodecUtil.H265_MIME : CodecUtil.H264_MIME);
  }

  @Override
  public void setAuthorization(String user, String password) {
    rtspClient.setAuthorization(user, password);
  }

  @Override
  public void setToken(String token) {
    rtspClient.setToken(token);
  }

  @Override
  protected void prepareAudioRtp(boolean isStereo, int sampleRate) {
    rtspClient.setIsStereo(isStereo);
    rtspClient.setSampleRate(sampleRate);
  }

  @Override
  protected void startStreamRtp(String url) {
    rtspClient.setUrl(url);
  }

  @Override
  protected void stopStreamRtp() {
    rtspClient.disconnect();
  }

  @Override
  protected void getAacDataRtp(ByteBuffer aacBuffer, MediaCodec.BufferInfo info) {
    rtspClient.sendAudio(aacBuffer, info);
  }

  @Override
  protected void onSpsPpsVpsRtp(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {
    ByteBuffer newSps = sps.duplicate();
    ByteBuffer newPps = pps.duplicate();
    ByteBuffer newVps = vps != null ? vps.duplicate() : null;
    rtspClient.setSPSandPPS(newSps, newPps, newVps);
    rtspClient.connect();
  }

  @Override
  protected void getH264DataRtp(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
    rtspClient.sendVideo(h264Buffer, info);
  }
}
