package com.streamingvideolive.cordova.plugins.encoder.audio;

/**
 * Created leonardo pineda on 28/01/19.
 */

public class DataTaken {

  private byte[] pcmBuffer;
  private int size;

  public DataTaken(byte[] pcmBuffer, int size) {
    this.pcmBuffer = pcmBuffer;
    this.size = size;
  }

  public byte[] getPcmBuffer() {
    return pcmBuffer;
  }

  public void setPcmBuffer(byte[] pcmBuffer) {
    this.pcmBuffer = pcmBuffer;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
