package com.teambald.cse442_project_team_bald.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseUser;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.R;

public class SettingFragment extends Fragment implements View.OnClickListener {
    private SeekBar sBar;
    private TextView recording_length;
    private int pval = 5;
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

    private boolean autoRecord = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize SharedPref and read set recording length from SharedPreference (default: 1).
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPref.edit();
        pval = sharedPref.getInt(getString(R.string.recording_length_key), 1);
        autoRecord = sharedPref.getBoolean(getString(R.string.auto_record),false);
        Log.i("Recording_Length", String.valueOf(pval));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.setting_fragment, container, false);
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
                pval = progress;
                updatePVALText();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //write custom code to on start progress
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt(getString(R.string.recording_length_key), pval);
                editor.commit();
                int s = sharedPref.getInt(getString(R.string.recording_length_key), 1);
                Log.i("Recording_Length", String.valueOf(s));
            }
        });

        Switch autoSwitch = view.findViewById(R.id.autoRecSwitch);
        autoSwitch.setChecked(autoRecord);
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoRecord = isChecked;
                editor.putBoolean(getString(R.string.auto_record),autoRecord);
                Log.d(TAG,"Saving auto recording preference: "+isChecked);
                editor.commit();
                if(autoRecord)
                {
                    Toast.makeText(getContext(),"Auto record ON",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getContext(),"Auto record OFF",Toast.LENGTH_SHORT).show();
                }
            }
        });

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
    public void updatePVALText()
    {
        switch(pval)
        {
            case 0:
                recording_length.setText("1 min");
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                recording_length.setText(times[pval]+" mins");
                break;
            default:
                Log.d(TAG,"Invalid index for pval");
                break;
        }
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
        else
        {
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
}
