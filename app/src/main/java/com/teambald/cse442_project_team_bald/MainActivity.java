package com.teambald.cse442_project_team_bald;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.teambald.cse442_project_team_bald.Fragments.SettingFragment;
import com.teambald.cse442_project_team_bald.TabsController.ViewPagerAdapter;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile, which also adds a request dialog to access the user's Google Drive.
 */
public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "MainAct";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;


    private Button proceedButton;

    private TextView mStatusTextView;
    private MainActivity siawd;


    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter vpa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar
        Toolbar tb = findViewById(R.id.my_toolbar);
        setSupportActionBar(tb);
        // Views
        mStatusTextView = findViewById(R.id.status);

        siawd = this;

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleSignInClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END build_client]
        FirebaseApp.initializeApp(getBaseContext());
        mAuth = FirebaseAuth.getInstance();
        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes.

        //SignInButton signInButton = findViewById(R.id.sign_in_button);
        //signInButton.setSize(SignInButton.SIZE_STANDARD);
        // [END customize_button]


        //
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(createTabAdapter());

        tabLayout = findViewById(R.id.tabs);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch(position){
                            case 0:
                                tab.setText("Home");
                                tab.setIcon(R.drawable.ic_home_icon);
                                break;
                            case 1:
                                tab.setText("Recordings");
                                tab.setIcon(R.drawable.ic_recording_list_icon);
                                break;
                            case 2:
                                tab.setText("Cloud");
                                tab.setIcon(R.drawable.white_cloud_icon);
                                break;
                            case 3:
                                tab.setText("Settings");
                                tab.setIcon(R.drawable.ic_settings_icon);
                                break;
                        }

                    }
                }).attach();


    }


    @Override
    public void onStart() {
        super.onStart();
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            SettingFragment.updateSignInUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            SettingFragment.updateSignInUI(null);
                        }
                    }
                });
    }
    // [END auth_with_google]

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                SettingFragment.updateSignInUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onActivityResult]

    // [START signIn]
    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    public void signOut() {
        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // [START_EXCLUDE]
                // [END_EXCLUDE]
                SettingFragment.updateSignInUI(null);
            }
        });
    }
    // [END signOut]



    private ViewPagerAdapter createTabAdapter() {
        vpa = new ViewPagerAdapter(this);
        return vpa;
    }

    public FirebaseAuth getmAuth()
    {return mAuth;}
}
