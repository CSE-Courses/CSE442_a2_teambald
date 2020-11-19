package com.teambald.cse442_project_team_bald.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private SeekBar sBar;
    private TextView recording_length;
    private int pval = 1;
    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor editor;

    private static final String TAG = "SettingsFrag";

    private MainActivity activity;

    public static final String LogInEmail = "LogInEmail";
    public static final String LogInName = "LogInName";
    public static final String LogedInBl = "LogedInBoolean";

    public SettingFragment(MainActivity mainActivity) { activity = mainActivity;}

    private static SignInButton signInButton;
    private static Button signOutButton;
    private static TextView statusText;

    public static final int[] times = new int[]{1,5,10,15,20,25,30};

    private Switch autoRecordSwitch;
    private boolean autoRecordVal = false;

    private Switch authenSwitch;
    private boolean authenVal = false;

    private Switch shakeSwitch;
    private boolean shakeVal = false;

    private Toast toast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize SharedPref and read set recording length from SharedPreference (default: 1).
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        editor = sharedPref.edit();
        pval = sharedPref.getInt(getString(R.string.recording_length_key), 1);
        autoRecordVal = sharedPref.getBoolean(getString(R.string.auto_record),false);
        Log.i(TAG, "PVAL: "+pval);
        Log.i(TAG, "auto record: "+autoRecordVal);
        authenVal = sharedPref.getBoolean(getString(R.string.biometric_authentication),false);
        Log.d(TAG,"authenval: "+authenVal);
        shakeVal = sharedPref.getBoolean(getString(R.string.shake_to_save),false);
        Log.d(TAG,"shakeVal: "+shakeVal);

        toast = Toast.makeText(getContext(),"",Toast.LENGTH_SHORT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }


    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

        sBar = view.findViewById(R.id.recording_seekBar);

        //Set saved value.
        sBar.setProgress(pval);

        recording_length = view.findViewById(R.id.recording_length_tv);
        updatePVALText();

        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Save changed value.
                Log.d(TAG,"Updating pval to :" +progress);
                pval = progress;
                updatePVALText();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //write custom code to on start progress
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"Updating pval sharedpref to :" +pval);
                editor.putInt(getString(R.string.recording_length_key), pval);
                boolean editorCommit = editor.commit();
                int s = sharedPref.getInt(getString(R.string.recording_length_key), 1);
                Log.i("Recording_Length changed to ", String.valueOf(s)+" editorCommit: "+editorCommit);

                toast.setText("Recording length changed to "+getPValString());
                toast.show();
            }
        });

        autoRecordSwitch = view.findViewById(R.id.autoRecSwitch);
        autoRecordSwitch.setChecked(autoRecordVal);
        autoRecordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoRecordVal = isChecked;
                editor.putBoolean(getString(R.string.auto_record),autoRecordVal);
                Log.d(TAG,"Saving auto recording preference: "+isChecked);
                editor.commit();
                if(autoRecordVal)
                {
                    toast.setText("Auto record ON");
                    toast.show();
                }
                else
                {
                    toast.setText("Auto record OFF");
                    toast.show();
                }
            }
        });
        shakeSwitch = view.findViewById(R.id.shakeSwitch);
        shakeSwitch.setChecked(shakeVal);
        shakeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                shakeVal = isChecked;
                editor.putBoolean(getString(R.string.shake_to_save),shakeVal);
                Log.d(TAG,"Saving shake to save recording preference: "+isChecked);
                editor.commit();
                if(shakeVal)
                {
                    toast.setText("Shake to Save ON");
                    toast.show();
                }
                else
                {
                    toast.setText("Shake to Save OFF");
                    toast.show();
                }
            }
        });

        authenSwitch = view.findViewById(R.id.bioSwitch);
        authenSwitch.setChecked(authenVal);
        authenSwitch.setOnCheckedChangeListener(new bioAuthChangeListener());


        signInButton = view.findViewById(R.id.sign_in_button);
        signOutButton = view.findViewById(R.id.sign_out_button);
        signInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        statusText = view.findViewById(R.id.signInTextView);
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
                Log.d(TAG,"Unknown button clicked");
        }
    }
    public String getPValString()
    {
        switch(pval)
        {
            case 0:
                return "1 min";
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return times[pval]+" mins";
            default:
                Log.d(TAG,"Invalid index for pval:"+pval);
                return null;
        }
    }
    public void updatePVALText()
    {
        recording_length.setText(getPValString());
    }
    public void signIn()
    {
        activity.signIn();
    }
    public void signOut()
    {
        activity.signOut();
    }

    public static void updateSignInUI(FirebaseUser account)
    {
        if(null != account)
        {
            editor.putBoolean(LogedInBl,true);
            editor.putString(LogInName,account.getDisplayName());
            editor.putString(LogInEmail,account.getEmail());
            signInButton.setVisibility(View.INVISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
            statusText.setText("Signed In as: "+account.getEmail());
            Log.d(TAG,"Log in successful as:" + account.getEmail());
        }
        else{
            editor.putBoolean(LogedInBl,false);
            editor.putString(LogInName,"null");
            editor.putString(LogInEmail,"null");
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.INVISIBLE);
            statusText.setText("Signed In as: None");
            Log.d(TAG,"Log in unsuccessful as");
        }
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"Setting MenuItems Invisible");
        activity.setMenuItemsVisible(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = null;
        if(activity.getmAuth() != null){
            currentUser = activity.getmAuth().getCurrentUser();
        }
        // Check if the user is already signed in and all required scopes are granted
        if (currentUser != null ) {
            updateSignInUI(currentUser);
        }
        else
        {
            updateSignInUI(null);
        }
    }
    private class bioAuthChangeListener implements CompoundButton.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            int error = BiometricManager.from(getActivity()).canAuthenticate();
            if (error != BiometricManager.BIOMETRIC_SUCCESS) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Can't Use Biometrics")
                        .setMessage("Error " + error)
                        .create()
                        .show();

                toast.setText("Cannot use BIOMETRICS on this device");
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
                        return;
                    }
                    Log.d(TAG,"Authentication error ...");
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Error")
                            .setMessage(errorCode + ": " + errString)
                            .create()
                            .show();

                    toast.setText("Biometric setting change failed");
                    toast.show();
                    buttonView.setChecked(false);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Log.d(TAG,"Authentication Success ...");
                    Toast.makeText(getContext(), "Biometric Setting Updated", Toast.LENGTH_SHORT).show();

                    authenVal = isChecked;
                    editor.putBoolean(getString(R.string.biometric_authentication),authenVal);
                    Log.d(TAG,"Saving authentication preference: "+authenVal);
                    editor.commit();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Log.d(TAG,"Authentication failed ...");
                    toast.setText("Biometric Authentication Failed");
                    toast.show();
                    buttonView.setChecked(false);
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock AudioRecorder.")
                    .setDescription("Scan your biometric")
                    .setDeviceCredentialAllowed(true)
                    .setConfirmationRequired(true)
                    .build();

            biometricPrompt.authenticate(promptInfo);
        }
    }
}
