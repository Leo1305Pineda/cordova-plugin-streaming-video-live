package com.streamingvideolive.cordova.plugins;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import com.zaga.taxy.R;

/**
 * More documentation see:
 * Created leonardo pineda on 29/01/19.
 * {@link com.streamingvideolive.cordova.plugins.rtplibrary.base.Camera1Base}
 * {@link com.streamingvideolive.cordova.plugins.rtplibrary.rtsp.RtspCamera1}
 */
public class StreamRTSP extends Activity
  implements ConnectCheckerRtsp, View.OnClickListener, SurfaceHolder.Callback{

  private RtspCamera1 rtspCamera1;
  private Button button;
  private Button bRecord;
  private EditText etUrl;

  private String currentDateAndTime = "";
  private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
    + "/rtmp-rtsp-stream-client-java");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      setContentView(R.layout.activity_stream_rtsp); //R.layout.activity_example
      SurfaceView surfaceView = findViewById(R.id.surfaceView);
      button = findViewById(R.id.b_start_stop);
      button.setOnClickListener(this);
      bRecord = findViewById(R.id.b_record);
      bRecord.setOnClickListener(this);
      Button switchCamera = findViewById(R.id.switch_camera);
      switchCamera.setOnClickListener(this);
      etUrl = findViewById(R.id.et_rtp_url);
      etUrl.setHint("rtsp://yourendpoint");
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
    switch (view.getId()) {
      case R.id.b_start_stop:
        if (!rtspCamera1.isStreaming()) {
          if (rtspCamera1.isRecording()
            || rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo()) {
            button.setText("stop stream");
            rtspCamera1.startStream(etUrl.getText().toString());
          } else {
            Toast.makeText(this, "Error preparing stream, This device cant do it",
              Toast.LENGTH_SHORT).show();
          }
        } else {
          button.setText("start stream");
          rtspCamera1.stopStream();
        }
        break;
      case R.id.switch_camera:
        try {
          rtspCamera1.switchCamera();
        } catch (CameraOpenException e) {
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.b_record:
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
        break;
      default:
        break;
    }
  }

}
