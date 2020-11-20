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
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teambald.cse442_project_team_bald.Encryption.EnDecryptAudio;
import com.teambald.cse442_project_team_bald.Fragments.HomeFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.Fragments.SettingFragment;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    public String recordPath;

    private HandlerThread mRecordingThread;
    private Handler mRecordingHandler;
    //saved recording length.
    private int recordingLength;
    private SharedPreferences prefs;

    private StorageReference mStorageRef;
    private static final String durationMetaDataConst = "Duration";

    @Override
    public void onCreate() {
        //Update and Create Local Recording List and Cloud Recording List
        String rawPath = getApplicationContext().getExternalFilesDir("/").getAbsolutePath();
        recordPath = rawPath+File.separator+"LocalRecording";


        Log.d(TAG, "Service is created");
        mRecordingThread = new HandlerThread("Recording thread", Thread.MAX_PRIORITY);
        mRecordingThread.start();
        mRecordingHandler = new Handler(mRecordingThread.getLooper());

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String signal = intent.getStringExtra("Recording_Service_Signal");
        Log.i(TAG, "Received " + signal);
        if (signal.equals("stop")) {
            Log.i(TAG, "Received Stop Foreground Intent");
            //Stop recording.
            stopRecording();
        }else {
            //Start recording.
            startRecording(this);

            createNotificationChannel();
            Log.i(TAG, "Received start id " + startId + ": " + intent);

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Smart Recorder")
                    .setContentText("Smart Recorder is running.")
                    .setSmallIcon(R.drawable.ic_mic_24)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setWhen(System.currentTimeMillis())
                    .build();
            startForeground(2001, notification);
        }
        return START_STICKY;
    }

    private void startRecording(final Context context) {
        //Read set recording length, default is 5 mins.
        if(!prefs.contains(getString(R.string.recording_length_key)))
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(getString(R.string.recording_length_key),1);
            boolean editorCommit = editor.commit();
            recordingLength = prefs.getInt(getString(R.string.recording_length_key), 1);
            Log.d(TAG,"Setting and obtaining recording length: idx"+recordingLength+" editor commit:"+editorCommit);
        }
        else
        {
            recordingLength = prefs.getInt(getString(R.string.recording_length_key), 1);
            Log.d(TAG,"Obtaining recording length: idx"+recordingLength);
        }
        final int recordingTime = SettingFragment.times[recordingLength];


        String recordingLengthString = "None";
        switch(recordingLength)
        {
            case 0:
                recordingLengthString = "1 min";
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                recordingLengthString = recordingTime+" mins";
                break;
            default:
                Log.d(TAG,"Invalid index for recordingLength "+recordingLength);
                break;
        }
        Log.i(TAG, "Recording will be saved every " + recordingLengthString);
        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.US);
        Date now = new Date();
        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_"+formatter.format(now)+ ".mp4";

        //Path where the file will be stored.
        final String filePath = recordPath + File.separator + recordFile;

        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //Save recording periodically.
        mediaRecorder.setMaxDuration(recordingTime * 60 * 1000);
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

                    autoDelete();

                    //Auto upload to Firebase Storage for signed-in user.
                    final String fireBaseFolder = prefs.getString(SettingFragment.LogInEmail,null);
                    if(fireBaseFolder != null) {
                        final String duration = recordingTime < 10 ? ("0" + recordingTime + ":00") : (recordingTime + ":00");
                        //Encrypt audio.
                        byte[] encoded = EnDecryptAudio.encrypt(recordPath + File.separator + recordFile, context);
                        final String tempFilePath = getApplicationContext().getExternalFilesDir("/").getAbsolutePath()
                                + File.separator + "tmp" + File.separator + recordFile;
                        //Write bytes to a file.
                        EnDecryptAudio.writeByteToFile(encoded, tempFilePath);
                        uploadRecording(tempFilePath, fireBaseFolder, duration);
                    }

                    //Restart the recorder.
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    boolean toRestart = sharedPref.getBoolean(getString(R.string.auto_record),false);
                    if(toRestart)
                    {
                        startRecording(context);
                    }else{
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(getString(R.string.is_recording_key), false);
                        editor.commit();

                        //Interrupt thread.
                        mRecordingThread.quitSafely();
                        stopForeground(true);
                        stopSelf();
                    }
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
        if(mediaRecorder == null){
            return;
        }
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        autoDelete();

        //Auto upload to Firebase Storage for signed-in user.
        final String fireBaseFolder = prefs.getString(SettingFragment.LogInEmail,null);
        Log.i(TAG, "firebaseFolder = " + fireBaseFolder);
        if(fireBaseFolder != null) {
            //Encrypt audio.
            byte[] encoded = EnDecryptAudio.encrypt(recordPath + "/" + recordFile, this);
            final String tempFilePath = getApplicationContext().getExternalFilesDir("/").getAbsolutePath()
                    + File.separator + "tmp" + File.separator + recordFile;
            //Write bytes to a file.
            EnDecryptAudio.writeByteToFile(encoded, tempFilePath);
            uploadRecording(tempFilePath, fireBaseFolder, readRecentRecordingLength());
        }

        //Show toast to notify user that the file has been saved.
        Toast toast = Toast.makeText(getApplicationContext(), "Recording has been saved.", Toast.LENGTH_SHORT);
        toast.show();

        //Interrupt thread.
        mRecordingThread.quitSafely();
        stopForeground(true);
        stopSelf();
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

    public void uploadRecording(String fileUri, final String fireBaseFolder, final String duration){
//        final String fullPath = path + "/" + filename;
//        final String fullFBPath = fireBaseFolder + "/" + filename;
        boolean autoUploadVal = prefs.getBoolean(getString(R.string.auto_upload_key),false);
        if(!autoUploadVal)
        {
            Log.d(TAG,"Not auto uploading");
            return;
        }

        Log.i(TAG, "Trying auto upload Recording, duration = " + duration);

        File f = new File(fileUri);
        Uri file = Uri.fromFile(f);

        final StorageReference storageReference = mStorageRef.child(fireBaseFolder).child(recordFile);
        storageReference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Log.d(TAG, "File auto-upload successful");
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setContentType("audio/mp4")
                                .setCustomMetadata(durationMetaDataConst, duration)
                                .build();
                        // Update metadata properties
                        storageReference.updateMetadata(metadata)
                                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        // Updated metadata is in storageMetadata
                                        Log.d(TAG,"File metadata auto-update successful");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                        Log.d(TAG,"File metadata auto-update unsuccessful");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d(TAG, "File auto-upload unsuccessful");
                    }
                });
        // Create file metadata including the content type

        //Remove temp file.
        f.delete();
    }

    //Once local recordings (unlocked) exceeds 5, the least recent one will be deleted.
    private void autoDelete(){
        boolean autoDeleteVal = prefs.getBoolean(getString(R.string.auto_delete_key),false);
        if(!autoDeleteVal)
        {
            Log.d(TAG,"Not auto deleting");
            return;
        }
        int autoDeleteFreqVal = prefs.getInt(getString(R.string.auto_delete_freq_key),1);
        int autoDeleteThreshold = SettingFragment.fileCounts[autoDeleteFreqVal];
        if(autoDeleteThreshold==-100)
        {
            Log.d(TAG,"Not auto deleting due to unlimited threshold");
            return;
        }
        else if(autoDeleteThreshold <=0)
        {
            Log.d(TAG,"Unknown auto delete threshold in service:"+autoDeleteThreshold);
            return;
        }
        //check storage size after saving the file
        ArrayList<File> filelist= new ArrayList<>();
        File directory = new File(recordPath);
        File[] allFiles = directory.listFiles();
        Arrays.sort(allFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });
        for (File f : allFiles) {
            if(!f.getName().contains("_L")){
                filelist.add(f);
            }
        }
        if(filelist.size()>autoDeleteThreshold){
            filelist.get(0).delete();
        }
        Log.i(TAG, "Auto delete is completed.");
    }

    private String readRecentRecordingLength(){
        Uri uri = Uri.parse(recordPath + "/" + recordFile);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int seconds = Integer.parseInt(durationStr) / 1000;
        int min = seconds / 60;
        seconds-=(min * 60);
        return (min < 10 ? "0" + min : String.valueOf(min)) + ":" + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
    }
}
