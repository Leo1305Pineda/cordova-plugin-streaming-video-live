package com.streamingvideolive.cordova.plugins.rtplibrary.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Surface;
import com.streamingvideolive.cordova.plugins.encoder.input.gl.SurfaceManager;
import com.streamingvideolive.cordova.plugins.encoder.input.gl.render.ManagerRender;
import com.streamingvideolive.cordova.plugins.encoder.input.gl.render.filters.BaseFilterRender;
import com.streamingvideolive.cordova.plugins.encoder.utils.gl.GlUtil;

/**
 * Created leonardo pineda on 28/01/19.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class OpenGlView extends OpenGlViewBase {

  private ManagerRender managerRender = null;
  private boolean loadAA = false;

  private boolean AAEnabled = false;
  private boolean keepAspectRatio = false;
  private boolean isFlipHorizontal = false, isFlipVertical = false;

  public OpenGlView(Context context) {
    super(context);
  }

  public OpenGlView(Context context, AttributeSet attrs) {
    super(context, attrs);
    int id = context.getResources().getIdentifier("OpenGlView","styleable",context.getPackageName());
    TypedArray typedArray = context.obtainStyledAttributes(attrs, context.getResources().getIntArray(id));
    try {
      keepAspectRatio = typedArray.getBoolean(context.getResources().getIdentifier("OpenGlView_keepAspectRatio","styleable",context.getPackageName()), false);
      AAEnabled = typedArray.getBoolean(context.getResources().getIdentifier("OpenGlView_AAEnabled","styleable",context.getPackageName()), false);
      ManagerRender.numFilters = typedArray.getInt(context.getResources().getIdentifier("OpenGlView_numFilters","styleable",context.getPackageName()), 1);
      isFlipHorizontal = typedArray.getBoolean(context.getResources().getIdentifier("OpenGlView_isFlipHorizontal","styleable",context.getPackageName()), false);
      isFlipVertical = typedArray.getBoolean(context.getResources().getIdentifier("OpenGlView_isFlipVertical","styleable",context.getPackageName()), false);
    } finally {
      typedArray.recycle();
    }
  }

  @Override
  public void init() {
    if (!initialized) managerRender = new ManagerRender();
    managerRender.setCameraFlip(isFlipHorizontal, isFlipVertical);
    initialized = true;
  }

  @Override
  public SurfaceTexture getSurfaceTexture() {
    return managerRender.getSurfaceTexture();
  }

  @Override
  public Surface getSurface() {
    return managerRender.getSurface();
  }

  @Override
  public void setFilter(int filterPosition, BaseFilterRender baseFilterRender) {
    filterQueue.add(new Filter(filterPosition, baseFilterRender));
  }

  @Override
  public void setFilter(BaseFilterRender baseFilterRender) {
    setFilter(0, baseFilterRender);
  }

  @Override
  public void enableAA(boolean AAEnabled) {
    this.AAEnabled = AAEnabled;
    loadAA = true;
  }

  @Override
  public void setRotation(int rotation) {
    managerRender.setCameraRotation(rotation);
  }

  public boolean isKeepAspectRatio() {
    return keepAspectRatio;
  }

  public void setKeepAspectRatio(boolean keepAspectRatio) {
    this.keepAspectRatio = keepAspectRatio;
  }

  public void setCameraFlip(boolean isFlipHorizontal, boolean isFlipVertical) {
    managerRender.setCameraFlip(isFlipHorizontal, isFlipVertical);
  }

  @Override
  public boolean isAAEnabled() {
    return managerRender != null && managerRender.isAAEnabled();
  }

  @Override
  public void run() {
    releaseSurfaceManager();
    surfaceManager = new SurfaceManager(getHolder().getSurface());
    surfaceManager.makeCurrent();
    managerRender.setStreamSize(encoderWidth, encoderHeight);
    managerRender.initGl(previewWidth, previewHeight, getContext());
    managerRender.getSurfaceTexture().setOnFrameAvailableListener(this);
    semaphore.release();
    try {
      while (running) {
          if (frameAvailable) {
            frameAvailable = false;
            surfaceManager.makeCurrent();
            managerRender.updateFrame();
            managerRender.drawOffScreen();
            managerRender.drawScreen(previewWidth, previewHeight, keepAspectRatio);
            surfaceManager.swapBuffer();
            if (takePhotoCallback != null) {
              takePhotoCallback.onTakePhoto(
                  GlUtil.getBitmap(previewWidth, previewHeight, encoderWidth, encoderHeight));
              takePhotoCallback = null;
            }
            synchronized (sync) {
              if (surfaceManagerEncoder != null) {
                surfaceManagerEncoder.makeCurrent();
                managerRender.drawScreen(encoderWidth, encoderHeight, false);
                long ts = managerRender.getSurfaceTexture().getTimestamp();
                surfaceManagerEncoder.setPresentationTime(ts);
                surfaceManagerEncoder.swapBuffer();
              }
            }
          }
          if (!filterQueue.isEmpty()) {
            Filter filter = filterQueue.take();
            managerRender.setFilter(filter.getPosition(), filter.getBaseFilterRender());
          } else if (loadAA) {
            managerRender.enableAA(AAEnabled);
            loadAA = false;
          }
      }
    } catch (InterruptedException ignore) {
      Thread.currentThread().interrupt();
    } finally {
      managerRender.release();
      releaseSurfaceManager();
    }
  }
}