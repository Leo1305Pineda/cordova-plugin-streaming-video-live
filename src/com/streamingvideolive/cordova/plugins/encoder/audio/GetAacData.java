package com.streamingvideolive.cordova.plugins.encoder.audio;

import android.media.MediaCodec;

import android.media.MediaFormat;
import java.nio.ByteBuffer;

/**
 * Created leonardo pineda on 28/01/19.
 */

public interface GetAacData {

  void getAacData(ByteBuffer aacBuffer, MediaCodec.BufferInfo info);

  void onAudioFormat(MediaFormat mediaFormat);
}
