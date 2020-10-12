package com.teambald.cse442_project_team_bald.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.teambald.cse442_project_team_bald.R;


public class HomeFragment extends Fragment{

    private ImageButton recorderButton;
    private boolean isRecording;
    private MediaPlayer mediaPlayer = null;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    private String fileToPlay;
    private MediaRecorder mediaRecorder;
    private String recordFile;

    private Chronometer timer;
    
    
    private static final String TAG = "HOME_FRAGMENT: ";

    private ImageButton recordButton;

    private SignInButton loginButton;

    private TextView accountText;

    private HomeFragment homeFragObj;

    private GoogleSignInClient mGoogleSignInClient;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    public HomeFragment() {
        // Required empty public constructor
    }
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);


        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        if(account != null)
            updateUI(account.getDisplayName());

        Log.d(TAG, "Updated with previous sign in");
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == 0) {
                            // There are no request code
                            Log.d(TAG, "Result OK onActivityResult.");
                            Intent data = result.getData();
                            doSomeOperations(data);
                        }
                        else
                        {
                            Log.d(TAG, "! Result OK onActivityResult.");
                            Log.d(TAG, "! Result OK onActivityResult."+result.getResultCode());
                        }
                    }
                });
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
        loginButton = view.findViewById(R.id.sign_in_button);
        loginButton.setSize(SignInButton.SIZE_STANDARD);
        loginButton.setOnClickListener(new loginClickListener());
        accountText = view.findViewById(R.id.login_account_text);
  
        checkPermissions();
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        recorderButton = view.findViewById(R.id.recorder_button);
        isRecording= false;
    }

    private class recordClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
                if (isRecording) {
                    //Stop Recording
                    recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_recorder_icon_150, null));
                    stopRecording();
                    isRecording = false;

                } else {
                    //Check permission to record audio
                    //Start Recording
                    startRecording();
                    recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button, null));
                    isRecording = true;
            }
        }
    }

    private class loginClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();

            Log.d(TAG,"Launch Sign in Intent");
            someActivityResultLauncher.launch(signInIntent);
        }
    }
    public void doSomeOperations(Intent data)
    {
        Log.d(TAG,"Do Some Operation Start");
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG,account.toString());
            Log.d(TAG,account.getDisplayName());
            // Signed in successfully, show authenticated UI.
            updateUI(account.getDisplayName());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }
    private void updateUI(String name)
    {
        if(name == null)
            name = "Not Signed In";
        accountText.setText(name);
    }

    private void startRecording() {

        //Get app external directory path
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();

        //Get current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.US);
        Date now = new Date();
        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_"+formatter.format(now)+ ".3gp";


        //Setup Media Recorder for recording
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        //Save recording periodically.
        //Read saved recording length (default to 5 mins).
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int max = sharedPref.getInt(getString(R.string.recording_length_key), 5) * 60 * 1000;
        mediaRecorder.setMaxDuration(max);

        //Will be executed when reach max duration.
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                //When reach max duration, stop, save the file and start again.
                if (i == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    //Stop and save the audio.
                    mediaRecorder.stop();
                    mediaRecorder.release();

                    //Show toast to notify user that the file has been saved.
                    Toast toast = Toast.makeText(getContext(), "Recording has been saved.", Toast.LENGTH_LONG);
                    toast.show();

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
    public void onStop() {
        super.onStop();
        if(isRecording){
            stopRecording();
        }
    }

    private void stopRecording() {

        //Change text on page to file saved
        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

    }


}