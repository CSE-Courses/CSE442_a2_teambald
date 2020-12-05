package com.teambald.cse442_project_team_bald.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseUser;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.R;

import java.util.concurrent.Executor;

public class SettingFragment extends Fragment implements View.OnClickListener {
    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor editor;

    private static final String TAG = "SettingsFrag";

    private MainActivity activity;

    public static final String LogInEmail = "LogInEmail";
    public static final String LogInName = "LogInName";
    public static final String LogedInBl = "LogedInBoolean";


    //Account Section
    private static SignInButton signInButton;
    private static Button signOutButton;
    private static TextView statusText;


    //Recording Section
    private Switch autoRecordSwitch;
    private boolean autoRecordVal = false;
    public static final int[] times = new int[]{1, 5, 10, 15, 20, 25, 30};
    private SeekBar recordingLengthSeekBar;
    private int recordingLengthSeekBarVal = 1;
    private TextView recording_length;

    //Biometric Section
    private Switch authenSwitch;
    private boolean authenVal = false;


    //Saving Section
    private Switch shakeSwitch;
    private boolean shakeVal = false;
    private Switch autoUploadSwitch;
    private boolean autoUpload = false;
    private Switch autoDeleteSwitch;
    private boolean autoDelete = false;
    private SeekBar autoDeleteFreqSeekBar;
    public static final int[] fileCounts = new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, -100};
    private TextView autoDeletefreqText;
    private int autodeleteFreqVal = 1;


    private Toast toast;

    public SettingFragment(MainActivity mainActivity) {
        activity = mainActivity;
    }

    //Account Section
    //Recording Section
    //Saving Section
    //Biometric Section
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize SharedPref and read set recording length from SharedPreference (default: 1).
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        editor = sharedPref.edit();

        //Account Section
        //Recording Section
        recordingLengthSeekBarVal = sharedPref.getInt(getString(R.string.recording_length_key), 1);
        autoRecordVal = sharedPref.getBoolean(getString(R.string.auto_record), false);
        Log.d(TAG, "recordingLengthSeekBarVal: " + recordingLengthSeekBarVal);
        Log.d(TAG, "auto record: " + autoRecordVal);
        //Saving Section
        shakeVal = sharedPref.getBoolean(getString(R.string.shake_to_save), false);
        Log.d(TAG, "shake val: " + shakeVal);
        autoUpload = sharedPref.getBoolean(getString(R.string.auto_upload_key), false);
        Log.d(TAG, "auto upload val: " + autoUpload);
        autoDelete = sharedPref.getBoolean(getString(R.string.auto_delete_key), false);
        Log.d(TAG, "auto delete val: " + autoDelete);
        autodeleteFreqVal = sharedPref.getInt(getString(R.string.auto_delete_freq_key), 1);
        Log.d(TAG, "auto delete freq val: " + autodeleteFreqVal);
        //Biometric Section
        authenVal = sharedPref.getBoolean(getString(R.string.biometric_authentication), false);
        Log.d(TAG, "bioauthen val: " + authenVal);

        toast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

        //Account Section
        signInButton = view.findViewById(R.id.sign_in_button);
        signOutButton = view.findViewById(R.id.sign_out_button);
        signInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        statusText = view.findViewById(R.id.signInTextView);

        //Recording Section
        recordingLengthSeekBar = view.findViewById(R.id.recording_seekBar);
        recordingLengthSeekBar.setProgress(recordingLengthSeekBarVal);
        recording_length = view.findViewById(R.id.recording_length_tv);
        updateRecordingLengthText();
        recordingLengthSeekBar.setOnSeekBarChangeListener(new recordingLengthSeekBarListener());

        autoRecordSwitch = view.findViewById(R.id.autoRecSwitch);
        autoRecordSwitch.setChecked(autoRecordVal);
        autoRecordSwitch.setOnCheckedChangeListener(new autoRecordChangeListener());

        //Saving Section
        shakeSwitch = view.findViewById(R.id.shakeSwitch);
        shakeSwitch.setChecked(shakeVal);
        shakeSwitch.setOnCheckedChangeListener(new shakeChangeListener());

        autoUploadSwitch = view.findViewById(R.id.autoUploadSwitch);
        autoUploadSwitch.setChecked(autoUpload);
        autoUploadSwitch.setOnCheckedChangeListener(new autoUploadChangeListener());

        autoDeleteSwitch = view.findViewById(R.id.autoDeleteSwitch);
        autoDeleteSwitch.setChecked(autoDelete);
        autoDeleteSwitch.setOnCheckedChangeListener(new autoDeleteChangeListener());
        autoDeletefreqText = view.findViewById(R.id.autodelete_freq_text);
        updateAutoDeleteText();
        autoDeleteFreqSeekBar = view.findViewById(R.id.autoDelete_seekbar);
        autoDeleteFreqSeekBar.setProgress(autodeleteFreqVal);
        autoDeleteFreqSeekBar.setOnSeekBarChangeListener(new autoDeleteSeekbarChangeListener());
        //Biometric Section
        authenSwitch = view.findViewById(R.id.bioSwitch);
        authenSwitch.setChecked(authenVal);
        authenSwitch.setOnCheckedChangeListener(new bioAuthChangeListener());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            default:
                Log.d(TAG, "Unknown button clicked");
        }
    }

    public void signIn() {
        activity.signIn();
    }

    public void signOut() {
        activity.signOut();
    }

    public static void updateSignInUI(FirebaseUser account) {
        if (null != account) {
            editor.putBoolean(LogedInBl, true);
            editor.putString(LogInName, account.getDisplayName());
            editor.putString(LogInEmail, account.getEmail());
            signInButton.setVisibility(View.INVISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
            statusText.setText("Signed In as: " + account.getEmail());
            Log.d(TAG, "Log in successful as:" + account.getEmail());
        } else {
            editor.putBoolean(LogedInBl, false);
            editor.putString(LogInName, "null");
            editor.putString(LogInEmail, "null");
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.INVISIBLE);
            statusText.setText("Signed In as: None");
            Log.d(TAG, "Log in unsuccessful as");
        }
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Setting MenuItems Invisible");
        activity.setMenuItemsVisible(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = null;
        if (activity.getmAuth() != null) {
            currentUser = activity.getmAuth().getCurrentUser();
        }
        // Check if the user is already signed in and all required scopes are granted
        if (currentUser != null) {
            updateSignInUI(currentUser);
        } else {
            updateSignInUI(null);
        }
    }

    private class bioAuthChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            int error = BiometricManager.from(getActivity()).canAuthenticate();
            if (error != BiometricManager.BIOMETRIC_SUCCESS) {
                toast.setText("Cannot use BIOMETRICS on this device right now");
                toast.show();
                buttonView.setChecked(false);
                return;
            }

            Executor executor;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                executor = getActivity().getMainExecutor();
            } else {
                final Handler handler = new Handler(Looper.getMainLooper());
                executor = new Executor() {
                    @Override
                    public void execute(@NonNull Runnable command) {
                        handler.post(command);
                    }
                };
            }


            BiometricPrompt biometricPrompt = new BiometricPrompt(getActivity(), executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        // Just negative button tap
                        final Vibrator vibe = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
                        vibe.vibrate(VibrationEffect.createOneShot(1000,255));
                        return;
                    }
                    Log.d(TAG, "Authentication error ...");
                    toast.setText("Authentication error");
                    toast.show();
                    buttonView.setChecked(false);
                    final Vibrator vibe = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(VibrationEffect.createOneShot(1000,255));
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Log.d(TAG, "Authentication Success ...");
                    Toast.makeText(getContext(), "Biometric Setting Updated", Toast.LENGTH_SHORT).show();

                    authenVal = isChecked;
                    editor.putBoolean(getString(R.string.biometric_authentication), authenVal);
                    Log.d(TAG, "Saving authentication preference: " + authenVal);
                    editor.commit();
                    final Vibrator vibe = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(VibrationEffect.createOneShot(500,255));
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Log.d(TAG, "Authentication failed ...");
                    toast.setText("Biometric Authentication Failed");
                    toast.show();
                    buttonView.setChecked(false);
                    final Vibrator vibe = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(VibrationEffect.createOneShot(1000,255));
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Smart Recorder")
                    .setDescription("Scan your biometric")
                    .setDeviceCredentialAllowed(true)
                    .setConfirmationRequired(true)
                    .build();

            biometricPrompt.authenticate(promptInfo);
        }
    }

    private class recordingLengthSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        public recordingLengthSeekBarListener() {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Save changed value.
            Log.d(TAG, "Updating recordingLengthSeekBarVal to :" + progress);
            recordingLengthSeekBarVal = progress;
            updateRecordingLengthText();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //write custom code to on start progress
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "Updating recordingLengthSeekBarVal sharedpref to :" + recordingLengthSeekBarVal);
            editor.putInt(getString(R.string.recording_length_key), recordingLengthSeekBarVal);
            boolean editorCommit = editor.commit();
            int s = sharedPref.getInt(getString(R.string.recording_length_key), 1);
            Log.i("Recording_Length changed to ", String.valueOf(s) + " editorCommit: " + editorCommit);

            toast.setText("Recording length changed to " + getRecordingLengthString());
            toast.show();
        }
    }

    public String getRecordingLengthString() {
        switch (recordingLengthSeekBarVal) {
            case 0:
                return "1 min";
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return times[recordingLengthSeekBarVal] + " mins";
            default:
                Log.d(TAG, "Invalid index for recordingLengthSeekBarVal:" + recordingLengthSeekBarVal);
                return null;
        }
    }

    public void updateRecordingLengthText() {
        recording_length.setText(getRecordingLengthString());
    }

    private class autoRecordChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            autoRecordVal = isChecked;
            editor.putBoolean(getString(R.string.auto_record), autoRecordVal);
            Log.d(TAG, "Saving auto recording preference: " + isChecked);
            editor.commit();
            if (autoRecordVal) {
                toast.setText("Auto record ON");
                toast.show();
            } else {
                toast.setText("Auto record OFF");
                toast.show();
            }
        }
    }

    private class shakeChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            shakeVal = isChecked;
            editor.putBoolean(getString(R.string.shake_to_save), shakeVal);
            Log.d(TAG, "Saving shake to save recording preference: " + isChecked);
            editor.commit();
            if (shakeVal) {
                toast.setText("Shake to Save ON");
                toast.show();
            } else {
                toast.setText("Shake to Save OFF");
                toast.show();
            }
        }
    }

    private class autoUploadChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            autoUpload = isChecked;
            editor.putBoolean(getString(R.string.auto_upload_key), autoUpload);
            Log.d(TAG, "Saving auto upload preference: " + isChecked);
            editor.commit();
            if (autoUpload) {
                toast.setText("Auto Upload ON");
                toast.show();
            } else {
                toast.setText("Auto Upload OFF");
                toast.show();
            }
        }
    }

    private class autoDeleteChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            autoDelete = isChecked;
            editor.putBoolean(getString(R.string.auto_delete_key), autoDelete);
            Log.d(TAG, "Saving auto delete preference: " + isChecked);
            editor.commit();
            if (autoDelete) {
                toast.setText("Auto Delete ON");
                toast.show();
            } else {
                toast.setText("Auto Delete OFF");
                toast.show();
            }
        }
    }

    private class autoDeleteSeekbarChangeListener implements SeekBar.OnSeekBarChangeListener {
        public autoDeleteSeekbarChangeListener() {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Save changed value.
            Log.d(TAG, "Updating auto delete freq to :" + progress);
            autodeleteFreqVal = progress;
            updateAutoDeleteText();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //write custom code to on start progress
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "Updating auto delete freq sharedpref to :" + autodeleteFreqVal);
            editor.putInt(getString(R.string.auto_delete_freq_key), autodeleteFreqVal);
            editor.commit();
            toast.setText("Auto-delete Threshold is now " + getAutoDeleteFreqString());
            toast.show();
        }
    }

    public String getAutoDeleteFreqString() {
        switch (autodeleteFreqVal) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return fileCounts[autodeleteFreqVal] + " files";
            case 10:
                return "Unlimited";
            default:
                Log.d(TAG, "Invalid index for auto delete freq string:" + autodeleteFreqVal);
                return null;
        }
    }

    public void updateAutoDeleteText() {
        autoDeletefreqText.setText(getAutoDeleteFreqString());
    }
}