package com.teambald.cse442_project_team_bald.Service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.teambald.cse442_project_team_bald.Fragments.HomeFragment;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
 * Use Service and thread to run long time lasting recording.
 */
public class RecordingService extends Service {
    public static final String CHANNEL_ID = "Recording_Service_Channel_01";
    private static final String TAG = "RecordingServiceTAG";
    private MediaRecorder mediaRecorder;
    private String recordFile;

    private HandlerThread mRecordingThread;
    private Handler mRecordingHandler;
    //saved recording length.
    private int recordingLength;

    @Override
    public void onCreate() {
        Log.d(TAG, "Service is created");
        mRecordingThread = new HandlerThread("Recording thread", Thread.MAX_PRIORITY);
        mRecordingThread.start();
        mRecordingHandler = new Handler(mRecordingThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String signal = intent.getStringExtra("Recording_Service_Signal");
        Log.i(TAG, "Received " + signal);
        if (signal.equals("stop")) {
            Log.i(TAG, "Received Stop Foreground Intent");
            //Stop recording.
            stopRecording();
            //Interrupt thread.
            mRecordingThread.quitSafely();
            stopForeground(true);
            stopSelf();
        }else {
            //Read saved recording length (default to 5 mins).
            recordingLength = intent.getIntExtra("RECORDING_LENGTH", 300000);
//            recordingLength = 5000;          //DEBUG Use.
            //Start recording.
            startRecording();

            createNotificationChannel();
            Log.i(TAG, "Received start id " + startId + ": " + intent);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("SmartRecorder")
                    .setContentText("SmartRecorder is recording audio.")
                    .setSmallIcon(R.drawable.ic_mic_24)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setWhen(System.currentTimeMillis())
                    .build();
            startForeground(2001, notification);
        }
        return START_STICKY;
    }

    private void startRecording() {
        //Get app external directory path
        String recordPath = getApplicationContext().getExternalFilesDir("/").getAbsolutePath();

        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.US);
        Date now = new Date();
        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_"+formatter.format(now)+ ".mp4";

        //Path used for encryption.
        final String filePath = recordPath + "/" + recordFile;

        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //Save recording periodically.
        mediaRecorder.setMaxDuration(recordingLength);
        //Will be executed when reach max duration.
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                //When reach max duration, stop, save the file and start again.
                if (i == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    //Stop and save the audio.
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();

                    //Show toast to notify user that the file has been saved.
//                    Toast toast = Toast.makeText(getApplicationContext(), "Recording has been saved.", Toast.LENGTH_SHORT);
//                    toast.show();

                    //Restart the recorder.
                    startRecording();
                }
            }
        });

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Start Recording
        mediaRecorder.start();
    }

    private void stopRecording() {
        Log.i(TAG, "Stop recording");
        //Change text on page to file saved
        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        //Show toast to notify user that the file has been saved.
        Toast toast = Toast.makeText(getApplicationContext(), "Recording has been saved.", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service has been destroyed");
        mRecordingThread.quitSafely();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sensor Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.enableVibration(false);
            serviceChannel.setSound(null,null);
            serviceChannel.enableLights(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
