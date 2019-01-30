package com.streamingvideolive.cordova.plugins.rtplibrary.view;

import com.streamingvideolive.cordova.plugins.encoder.input.gl.render.filters.BaseFilterRender;

/**
 * Created leonardo pineda on 30/01/19.
 */

public class Filter {

  private int position;
  private BaseFilterRender baseFilterRender;

  public Filter() {
  }

  public Filter(int position, BaseFilterRender baseFilterRender) {
    this.position = position;
    this.baseFilterRender = baseFilterRender;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public BaseFilterRender getBaseFilterRender() {
    return baseFilterRender;
  }

  public void setBaseFilterRender(BaseFilterRender baseFilterRender) {
    this.baseFilterRender = baseFilterRender;
  }
}
