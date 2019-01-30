package com.streamingvideolive.cordova.plugins.encoder.input.gl.render;

/**
 * Created leonardo pineda on 28/01/19.
 */

public class RenderHandler {

  private int[] fboId = new int[] { 0 };
  private int[] rboId = new int[] { 0 };
  private int[] texId = new int[] { 0 };

  public int[] getTexId() {
    return texId;
  }

  public int[] getFboId() {
    return fboId;
  }

  public int[] getRboId() {
    return rboId;
  }

  public void setFboId(int[] fboId) {
    this.fboId = fboId;
  }

  public void setRboId(int[] rboId) {
    this.rboId = rboId;
  }

  public void setTexId(int[] texId) {
    this.texId = texId;
  }
}
