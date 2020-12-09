package com.teambald.cse442_project_team_bald.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.Service.RecordingService;
import com.teambald.cse442_project_team_bald.Service.ShakeListener;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;


public class HomeFragment extends Fragment {

    private ImageButton recorderButton;
    private boolean isRecording;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE = 21;
    private ShakeListener mShaker;
    private SharedPreferences sharedPref;

    private static final String TAG = "HOME_FRAGMENT: ";

    private ImageButton recordButton;

    private TextView accountText;

    private TextView recordStatusText;

    private MainActivity activity;

    private MaterialTapTargetSequence mtts;

    public HomeFragment(MainActivity mainActivity) {
        activity = mainActivity;
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

        recordStatusText = view.findViewById(R.id.recordStatus);

        //Initialize My ShakeListener and Vibration feedback
        mShaker = new ShakeListener(this.getContext());
        mShaker.setOnShakeListener(new shakeListener());

        //initilize the Recroding Directory
        initilize_RecordDirctroy();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        //Read isRecording value.
        isRecording = sharedPref.getBoolean(getString(R.string.is_recording_key), false);

        checkPermissions();
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        recorderButton = view.findViewById(R.id.recorder_button);
        if(!isRecording){
            recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_recorder_icon_150, null));
            recordStatusText.setText(getString(R.string.click_to_start));
        }else{
            recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button, null));
            recordStatusText.setText(getString(R.string.click_to_pause));
        }
    }//ON view Created End
    private class shakeListener implements ShakeListener.OnShakeListener
    {

        public void onShake()
        {
            final Vibrator vibe = (Vibrator)activity.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            boolean shakeVal = sharedPref.getBoolean(getString(R.string.shake_to_save),false);
            if (shakeVal) {
                if (isRecording) {
                    vibe.vibrate(50);
                    //Stop Recording
                    recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_recorder_icon_150, null));
                    //stopRecording();
                    System.out.println("Try Stop Recording!!!");
                    try {
                        stopService();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isRecording = false;
                } else {
                    vibe.vibrate(100);
                    //Start service that record audio consistently;
                    System.out.println("Try Start Recording!!!");
                    try{
                        startService();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button, null));
                    isRecording = true;
                }
                //Save isRecording value.
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.is_recording_key), isRecording);
                editor.commit();
            }

        }
    }
    public void showGuidance()
    {
        if(mtts != null)
        {
            mtts.dismiss();
        }
        mtts = new MaterialTapTargetSequence();
        mtts.addPrompt(new MaterialTapTargetPrompt.Builder(getActivity())
                .setTarget(getView().findViewById(R.id.recorder_button))
                .setPrimaryText("Click here to start/pause recording")
                .setFocalRadius(200f)
                .setAutoDismiss(true)
                .setBackButtonDismissEnabled(true)
        );
        mtts.addPrompt(new MaterialTapTargetPrompt.Builder(getActivity())
                .setTarget(getView().findViewById(R.id.login_account_text))
                .setPrimaryText("Here is your log in status")
                .setFocalRadius(200f)
                .setAutoDismiss(true)
                .setBackButtonDismissEnabled(true)
        );
        mtts.show();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(activity.getmAuth()!=null)
            updateUI(activity.getmAuth().getCurrentUser());
        // [END on_start_sign_in]
    }
    @Override
    public void onResume() {
        super.onResume();
        //

        Log.d(TAG,"Setting MenuItems Invisible");
        activity.setMenuItemsVisible(false);

        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        if(activity.getmAuth()!=null)
            updateUI(activity.getmAuth().getCurrentUser());
        // [END on_start_sign_in]

        //Read isRecording value.
        isRecording = sharedPref.getBoolean(getString(R.string.is_recording_key), false);

        if(!isRecording){
            recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_recorder_icon_150, null));
            recordStatusText.setText(getString(R.string.click_to_start));
        }else{
            recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button, null));
            recordStatusText.setText(getString(R.string.click_to_pause));
        }

        boolean showGuidance = sharedPref.getBoolean(getString(R.string.guidance_on_off),false);
        //if(showGuidance)
        if(showGuidance)
        {
            showGuidance();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"On pause called");

        if(mtts != null)
        {
            Log.d(TAG,"MTTS dismissed");
            mtts.dismiss();
            mtts = null;
        }
    }

    private void updateUI(FirebaseUser account) {
        if (account != null) {
            accountText.setText("Signed In as: "+account.getEmail());
        } else {
            accountText.setText("Signed In as: None");
        }
    }

    private class recordClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {

            if (isRecording) {
                //Stop Recording
                recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_recorder_icon_150, null));
                recordStatusText.setText(getString(R.string.click_to_start));

                //stopRecording();
                stopService();
                isRecording = false;

            } else {
                //Start service that record audio consistently;
                startService();
                recorderButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button, null));
                recordStatusText.setText(getString(R.string.click_to_pause));
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
        Intent serviceIntent = new Intent(getContext(), RecordingService.class);
        serviceIntent.putExtra("Recording_Service_Signal", "start");
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

    public void initilize_RecordDirctroy(){
        String rawPath = getContext().getExternalFilesDir("/").getAbsolutePath();
        String recordPath = rawPath+File.separator+"LocalRecording";
        File LocalRecordList = new File(recordPath);
        File CloudRecordList = new File(rawPath+File.separator+"CloudRecording");
        File tmpRecordList = new File(rawPath+File.separator+"tmp");

        LocalRecordList.mkdir();
        CloudRecordList.mkdir();
        tmpRecordList.mkdir();
    }



}