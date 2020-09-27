package com.teambald.cse442_project_team_bald;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class HomeFragment extends Fragment {
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
    }
    private class recordClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View view) {
            //TODO: @Xuanhua: Implement record function here.
            Log.d(TAG, "Record button is clicked.");
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

}