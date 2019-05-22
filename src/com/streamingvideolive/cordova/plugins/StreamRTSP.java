package com.streamingvideolive.cordova.plugins;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.streamingvideolive.cordova.plugins.encoder.input.video.CameraOpenException;
import com.streamingvideolive.cordova.plugins.rtplibrary.rtsp.RtspCamera1;
import com.streamingvideolive.cordova.plugins.rtsp.utils.ConnectCheckerRtsp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.View.*;

/**
 * More documentation see:
 * Created leonardo pineda on 29/01/19.
 * {@link com.streamingvideolive.cordova.plugins.rtplibrary.base.Camera1Base}
 * {@link com.streamingvideolive.cordova.plugins.rtplibrary.rtsp.RtspCamera1}
 */
public class StreamRTSP extends Activity implements ConnectCheckerRtsp, OnClickListener, SurfaceHolder.Callback{

  private static String TAG = StreamRTSP.class.getSimpleName();
  private String appResourcesPackage;
  private RtspCamera1 rtspCamera1;
  private Button button;
  private Button bRecord;
  private EditText etUrl;
  private String urlStream;
  private String user;
  private String password;

  private String currentDateAndTime = "";
  private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
    + "/rtmp-rtsp-stream-client-java");

  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      appResourcesPackage = this.getPackageName();
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      setContentView(getResources().getIdentifier("activity_stream_rtsp", "layout", appResourcesPackage)); //R.layout.activity_example
      SurfaceView surfaceView = findViewById(getResources().getIdentifier("surfaceView", "id", appResourcesPackage));
      button = findViewById(getResources().getIdentifier("b_start_stop", "id", appResourcesPackage));
      button.setOnClickListener(this);
      bRecord = findViewById(getResources().getIdentifier("b_record", "id", appResourcesPackage));
      bRecord.setOnClickListener(this);
      Button switchCamera = findViewById(getResources().getIdentifier("switch_camera", "id", appResourcesPackage));
      switchCamera.setOnClickListener(this);
      etUrl = findViewById(getResources().getIdentifier("et_rtp_url", "id", appResourcesPackage));
      etUrl.setHint("rtsp://yourendpoint");
      Intent intent = getIntent();
      Bundle extras = intent.getExtras();

      Log.e(TAG, "etUrl visible: " + extras.getString("etUrl"));
      if (extras.getBoolean("etUrl")) {
        etUrl.setVisibility(VISIBLE);
      } else {
        etUrl.setVisibility(INVISIBLE);
      }

      user = extras.getString("user");
      password = extras.getString("password");

      Log.i(TAG, "connected to: " + extras.getString("urlStream"));
      urlStream = extras.getString("urlStream");
      etUrl.setText(urlStream);
      rtspCamera1 = new RtspCamera1(surfaceView, this);
      surfaceView.getHolder().addCallback(this);
    }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    rtspCamera1.startPreview();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtspCamera1.isRecording()) {
      rtspCamera1.stopRecord();
      bRecord.setText("stop record");
      Toast.makeText(this,
        "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
        Toast.LENGTH_SHORT).show();
      currentDateAndTime = "";
    }
    if (rtspCamera1.isStreaming()) {
      rtspCamera1.stopStream();
      button.setText("start stream");
    }
    rtspCamera1.stopPreview();
  }

  @Override
  public void onConnectionSuccessRtsp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(StreamRTSP.this, "Connection success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onConnectionFailedRtsp(String reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Log.e(TAG, "Connection failed. " + reason);
        Toast.makeText(StreamRTSP.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
          .show();
        rtspCamera1.stopStream();
        button.setText("start stream");
      }
    });
  }

  @Override
  public void onDisconnectRtsp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(StreamRTSP.this, "Disconnected", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthErrorRtsp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(StreamRTSP.this, "Auth error", Toast.LENGTH_SHORT).show();
        rtspCamera1.stopStream();
        button.setText("start stream");
      }
    });
  }

  @Override
  public void onAuthSuccessRtsp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(StreamRTSP.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onClick(View view) {
      if (getResources().getIdentifier("b_start_stop", "id", appResourcesPackage) == view.getId()) {
        if (!rtspCamera1.isStreaming()) {
          if (user != null && password != null) {
            rtspCamera1.setAuthorization(user, password);
          }
          if (rtspCamera1.isRecording() || rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo()) {
            button.setText("stop stream");
            rtspCamera1.startStream(etUrl.getText().toString());
          } else {
            Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
          }
        } else {
          button.setText("start stream");
          rtspCamera1.stopStream();
        }
      } else if (getResources().getIdentifier("switch_camera", "id", appResourcesPackage) == view.getId()) {
        try {
          rtspCamera1.switchCamera();
        } catch (CameraOpenException e) {
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
      } else if (getResources().getIdentifier("b_record", "id", appResourcesPackage) == view.getId()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
          if (!rtspCamera1.isRecording()) {
            try {
              if (!folder.exists()) {
                folder.mkdir();
              }
              SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
              currentDateAndTime = sdf.format(new Date());
              if (!rtspCamera1.isStreaming()) {
                if (rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo()) {
                  rtspCamera1.startRecord(
                          folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                  bRecord.setText("stop record");
                  Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                } else {
                  Toast.makeText(this, "Error preparing stream, This device cant do it",
                          Toast.LENGTH_SHORT).show();
                }
              } else {
                rtspCamera1.startRecord(
                        folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                bRecord.setText("stop record");
                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
              }
            } catch (IOException e) {
              rtspCamera1.stopRecord();
              bRecord.setText("stop record");
              Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
          } else {
            rtspCamera1.stopRecord();
            bRecord.setText("stop record");
            Toast.makeText(this,
                    "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
          }
        } else {
          Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
                  Toast.LENGTH_SHORT).show();
        }
      }
  }

}
