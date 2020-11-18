package com.teambald.cse442_project_team_bald;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executor;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile, which also adds a request dialog to access the user's Google Drive.
 */
public class AuthenActivity extends AppCompatActivity
{

    private static final String TAG = "AuthenActivity";


    private static SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Decide whether to use biometric

        Intent intent;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean authenVal = sharedPref.getBoolean(getString(R.string.biometric_authentication),false);
        Log.d(TAG,"Reading authentication preference:"+authenVal);
        if(!authenVal)
        {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else
        {
            setContentView(R.layout.activity_authen);

            //Toolbar
            Button button = findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int error = BiometricManager.from(AuthenActivity.this).canAuthenticate();
                    if (error != BiometricManager.BIOMETRIC_SUCCESS) {
                        new AlertDialog.Builder(AuthenActivity.this)
                                .setTitle("Can't Use Biometrics")
                                .setMessage("Error " + error)
                                .create()
                                .show();
                        return;
                    }


                    Executor executor;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        executor = AuthenActivity.this.getMainExecutor();
                    } else {
                        final Handler handler = new Handler(Looper.getMainLooper());
                        executor = new Executor() {
                            @Override
                            public void execute(@NonNull Runnable command) {
                                handler.post(command);
                            }
                        };
                    }


                    BiometricPrompt biometricPrompt = new BiometricPrompt(AuthenActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                                // Just negative button tap
                                return;
                            }
                            Log.d(TAG,"Authentication error ...");
                            new AlertDialog.Builder(AuthenActivity.this)
                                    .setTitle("Error")
                                    .setMessage(errorCode + ": " + errString)
                                    .create()
                                    .show();

                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            Log.d(TAG,"Authentication Success ...");
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Log.d(TAG,"Authentication failed ...");
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

            });
            button.performClick();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

}
