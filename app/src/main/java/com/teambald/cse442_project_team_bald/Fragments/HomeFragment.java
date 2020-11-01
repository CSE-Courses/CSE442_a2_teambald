package com.teambald.cse442_project_team_bald.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.teambald.cse442_project_team_bald.Encryption.AudioEncryptionUtils;
import com.teambald.cse442_project_team_bald.Encryption.FileUtils;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.Service.RecordingService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;


public class HomeFragment extends Fragment {

    private ImageButton recorderButton;
    private boolean isRecording;
    private MediaPlayer mediaPlayer = null;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    private String fileToPlay;
    private MediaRecorder mediaRecorder;
    private String recordFile;
    //Path of new recording.
    private String filePath;
    //SharedPreference
    private SharedPreferences sharedPref;

    private Chronometer timer;
    
    private static final String TAG = "HOME_FRAGMENT: ";

    private ImageButton recordButton;

    private TextView accountText;

    private HomeFragment homeFragObj;

    public HomeFragment() {
        // Required empty public constructor
    }
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
  
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_fragment, container, false);
    }
  
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        recordButton = view.findViewById(R.id.recorder_button);
        recordButton.setOnClickListener(new recordClickListener());
        accountText = view.findViewById(R.id.login_account_text);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        checkPermissions();
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        recorderButton = view.findViewById(R.id.recorder_button);
        if(!isRecording){
            recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_recorder_icon_150, null));
        }else{
            recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button, null));
        }
        isRecording= false;
    }
    @Override
    public void onStart() {
        super.onStart();

        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        updateUI(account);
        // [END on_start_sign_in]
    }
    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            accountText.setText("Signed In as: "+account.getDisplayName());
        } else {
            accountText.setText("None");
        }
    }

    private class recordClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            //Read isRecording value.
            isRecording = sharedPref.getBoolean(getString(R.string.is_recording_key), false);

            if (isRecording) {
                //Stop Recording
                recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_recorder_icon_150, null));
                //stopRecording();
                stopService();
                isRecording = false;

            } else {
                //Start service that record audio consistently;
                startService();
                recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button, null));
                isRecording = true;
            }
            //Save isRecording value.
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.is_recording_key), isRecording);
            editor.commit();
        }
    }

    //Start service that can record audio even if the app is not visible to user.
    public void startService() {
        if(!checkPermissions()){
            Toast toast = Toast.makeText(getContext(), "Please check the permission before recording.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        int recordingLength = sharedPref.getInt(getString(R.string.recording_length_key), 5) * 60 * 1000;
        Intent serviceIntent = new Intent(getContext(), RecordingService.class);
        serviceIntent.putExtra("Recording_Service_Signal", "start");
        serviceIntent.putExtra("RECORDING_LENGTH", recordingLength);
        ContextCompat.startForegroundService(getContext(), serviceIntent);
        Log.d("mTAG", "Service start!");
    }

    //Stop service
    public void stopService() {
        Intent serviceIntent = new Intent(getContext(), RecordingService.class);
        serviceIntent.putExtra("Recording_Service_Signal", "stop");
        ContextCompat.startForegroundService(getContext(), serviceIntent);
        Log.d("mTAG", "Service Stop!");
    }

    private boolean checkPermissions() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            return true;
        } else {
            //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    /**
     * Encrypt and save to disk
     *
     * @return
     */
    private boolean encrypt(String filePath) {
        try {
            byte[] fileData = FileUtils.readFile(filePath);
            byte[] encodedBytes = AudioEncryptionUtils.encode(AudioEncryptionUtils.getInstance(getContext()).getSecretKey(), fileData);
            FileUtils.saveFile(encodedBytes, filePath);
            return true;
        } catch (Exception e) {
            Log.e("Encryption", e.toString());
            Toast toast = Toast.makeText(getContext(), "Encryption failed.", Toast.LENGTH_SHORT);
            toast.show();
        }
        return false;
    }

    /**
     * Decrypt and return the decoded bytes
     *
     * @return
     */
    private byte[] decrypt(String filePath) {
        try {
            byte[] fileData = FileUtils.readFile(filePath);
            byte[] decryptedBytes = AudioEncryptionUtils.decode(AudioEncryptionUtils.getInstance(getContext()).getSecretKey(), fileData);
            return decryptedBytes;
        } catch (Exception e) {
            Toast toast = Toast.makeText(getContext(), "Decryption failed.", Toast.LENGTH_SHORT);
            toast.show();
        }
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("mTAG", "APP stopped!");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("mTAG", "APP resumed!");
        Log.d("mTAG", "Recording = " + isRecording);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("mTAG", "APP paused!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("mTAG", "APP destroyed!");
    }
}