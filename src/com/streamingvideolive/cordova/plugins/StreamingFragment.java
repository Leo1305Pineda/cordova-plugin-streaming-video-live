package com.streamingvideolive.cordova.plugins;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.streamingvideolive.cordova.plugins.encoder.input.video.CameraOpenException;
import com.streamingvideolive.cordova.plugins.rtplibrary.rtsp.RtspCamera1;
import com.streamingvideolive.cordova.plugins.rtsp.utils.ConnectCheckerRtsp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StreamingFragment extends Fragment implements ConnectCheckerRtsp, View.OnClickListener, SurfaceHolder.Callback {

        private static final String TAG = StreamingFragment.class.getSimpleName();

        private View view;
        public FrameLayout frameContainerLayout;
        public FrameLayout mainLayout;
        private Preview mPreview;

        private String appResourcesPackage;
        private RtspCamera1 rtspCamera1;
        private Button button;
        private Button bRecord;
        private EditText etUrl;

        private String currentDateAndTime = "";
        private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/rtmp-rtsp-stream-client-java");

        public int width;
        public int height;
        public int x;
        public int y;

        private FrameLayout.LayoutParams layoutParams;

        private String urlStream = "";

    @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            appResourcesPackage =  getActivity().getPackageName();
            view = inflater.inflate(getResources().getIdentifier("fragment_stream_rtsp", "layout", appResourcesPackage), container, false);
            if(mPreview == null) {
                SurfaceView surfaceView = view.findViewById(view.getResources().getIdentifier("surfaceView", "id", appResourcesPackage));

                button = view.findViewById(view.getResources().getIdentifier("b_start_stop", "id", appResourcesPackage));
                button.setOnClickListener(this);
                bRecord = view.findViewById(view.getResources().getIdentifier("b_record", "id", appResourcesPackage));
                bRecord.setOnClickListener(this);
                Button switchCamera = view.findViewById(getResources().getIdentifier("switch_camera", "id", appResourcesPackage));
                switchCamera.setOnClickListener(this);
                etUrl = view.findViewById(getResources().getIdentifier("et_rtp_url", "id", appResourcesPackage));
                etUrl.setHint("rtsp://yourendpoint");
                if (!urlStream.equals("")) {
                    etUrl.setText(urlStream);
                }
                rtspCamera1 = new RtspCamera1(surfaceView, this);
                surfaceView.getHolder().addCallback(this);

                //set box position and size
                layoutParams = new FrameLayout.LayoutParams(width, height);
                layoutParams.setMargins(x, y, 0, 0);

                frameContainerLayout = (FrameLayout) view.findViewById(getResources().getIdentifier("frame_container", "id", appResourcesPackage));
                frameContainerLayout.setLayoutParams(layoutParams);

                //video view
                mPreview = new Preview(getActivity());
                mainLayout = (FrameLayout) view.findViewById(getResources().getIdentifier("video_view", "id", appResourcesPackage));
                mainLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                mainLayout.addView(mPreview);
                mainLayout.setEnabled(false);

                setupTouchAndBackButton();
            }
            return view;
        }

        public void setRect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
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
                Toast.makeText(getActivity(),"file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(), Toast.LENGTH_SHORT).show();
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Connection success", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onConnectionFailedRtsp(String reason) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Connection failed. " + reason, Toast.LENGTH_SHORT).show();
                    rtspCamera1.stopStream();
                    button.setText("start stream");
                }
            });
        }

        @Override
        public void onDisconnectRtsp() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onAuthErrorRtsp() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Auth error", Toast.LENGTH_SHORT).show();
                    rtspCamera1.stopStream();
                    button.setText("start stream");
                }
            });
        }

        @Override
        public void onAuthSuccessRtsp() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Auth success", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (getResources().getIdentifier("b_start_stop", "id", appResourcesPackage) == view.getId()) {
                if (!rtspCamera1.isStreaming()) {
                    if (rtspCamera1.isRecording()
                            || rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo()) {
                        button.setText("stop stream");
                        rtspCamera1.startStream(etUrl.getText().toString());
                    } else {
                        Toast.makeText(getActivity(), "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    button.setText("start stream");
                    rtspCamera1.stopStream();
                }
            } else if (getResources().getIdentifier("switch_camera", "id", appResourcesPackage) == view.getId()) {
                try {
                    rtspCamera1.switchCamera();
                } catch (CameraOpenException e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(getActivity(), "Recording... ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Error preparing stream, This device cant do it",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                rtspCamera1.startRecord(
                                        folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                bRecord.setText("stop record");
                                Toast.makeText(getActivity(), "Recording... ", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            rtspCamera1.stopRecord();
                            bRecord.setText("stop record");
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        rtspCamera1.stopRecord();
                        bRecord.setText("stop record");
                        Toast.makeText(getActivity(),"file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "You need min JELLY_BEAN_MR2(API 18) for do it...", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void setupTouchAndBackButton() {

            final GestureDetector gestureDetector = new GestureDetector(getActivity().getApplicationContext(), new TapGestureDetector());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    frameContainerLayout.setClickable(false);
                    frameContainerLayout.setOnTouchListener(new View.OnTouchListener() {

                        private int mLastTouchX;
                        private int mLastTouchY;
                        private int mPosX = 0;
                        private int mPosY = 0;

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) frameContainerLayout.getLayoutParams();
                            boolean isSingleTapTouch = gestureDetector.onTouchEvent(event);
                            if (event.getAction() != MotionEvent.ACTION_MOVE && isSingleTapTouch) {
                                return true;
                            } else {
                                int x;
                                int y;
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        if (mLastTouchX == 0 || mLastTouchY == 0) {
                                            mLastTouchX = (int) event.getRawX() - layoutParams.leftMargin;
                                            mLastTouchY = (int) event.getRawY() - layoutParams.topMargin;
                                        } else {
                                            mLastTouchX = (int) event.getRawX();
                                            mLastTouchY = (int) event.getRawY();
                                        }
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        x = (int) event.getRawX();
                                        y = (int) event.getRawY();

                                        final float dx = x - mLastTouchX;
                                        final float dy = y - mLastTouchY;

                                        mPosX += dx;
                                        mPosY += dy;

                                        layoutParams.leftMargin = mPosX;
                                        layoutParams.topMargin = mPosY;

                                        frameContainerLayout.setLayoutParams(layoutParams);

                                        // Remember this touch position for the next move event
                                        mLastTouchX = x;
                                        mLastTouchY = y;

                                        break;
                                    default: break;
                                }
                            }
                            return true;
                        }
                    });
                    frameContainerLayout.setFocusableInTouchMode(true);
                    frameContainerLayout.requestFocus();
                }
            });
        }

    public void setUrlStream(String urlStream) {
            this.urlStream = urlStream;
    }
}
