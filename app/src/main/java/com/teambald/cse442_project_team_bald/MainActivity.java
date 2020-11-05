package com.teambald.cse442_project_team_bald;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teambald.cse442_project_team_bald.Fragments.SettingFragment;
import com.teambald.cse442_project_team_bald.TabsController.ViewPagerAdapter;

import java.io.File;

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
    private static FirebaseStorage storage;
    private static StorageReference storageRef;

    private static final String durationMetaDataConst = "Duration";

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
        Log.d(TAG,"mAuth null is:"+ (null==mAuth));
        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes.

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
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

    public boolean downloadFile(String path,String localFolder,String filenamePref,String filenameSuf,String fireBaseFolder)
    {
        return downloadFile(path,localFolder,filenamePref+"."+filenameSuf,fireBaseFolder);
    }
    public boolean downloadFile(String path,String localFolder,String filename,String fireBaseFolder)
    {
        try
        {
            Log.d(TAG,"Downloading from");
            Log.d(TAG,fireBaseFolder);
            Log.d(TAG,filename);
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filename);
            final String fullPath = path + "/" +localFolder+ "/" + filename;
            File tempFile = new File(fullPath);
            storageReference.getFile(tempFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Log.d(TAG,"File download Successful");
                            Toast tst = Toast.makeText(getApplicationContext(),"File download Successful", Toast.LENGTH_SHORT);
                            tst.show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle failed download
                            // ...
                            Log.d(TAG,"Read file error on Failure");
                            Toast tst = Toast.makeText(getApplicationContext(),"File download Unsuccessful", Toast.LENGTH_SHORT);
                            tst.show();
                        }
                    });
        }
        catch (Exception e)
        {
            Log.d(TAG,"Read file error");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public void uploadFile(String path, String localFolder, final String filenamePref, final String filenameSuf, final String fireBaseFolder, String duration)
    {uploadFile(path,localFolder,filenamePref + "." + filenameSuf,fireBaseFolder,duration);}
    public void uploadFile(String path, String localFolder, final String filename, final String fireBaseFolder, String duration)
    {
        if(null!=path && null!=filename && null!=fireBaseFolder)
        {
            final String fullPath = path + "/" +localFolder+ "/" + filename;
            final String fullFBPath = fireBaseFolder + "/" + filename;

            Uri file = Uri.fromFile(new File(fullPath));
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filename);
            storageReference.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Log.d(TAG, "File upload successful");
                            Log.d(TAG, "From:" + fullPath);
                            Log.d(TAG, "To:" + fullFBPath);
                            Toast tst = Toast.makeText(getApplicationContext(),"File upload Successful", Toast.LENGTH_SHORT);
                            tst.show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Log.d(TAG, "File upload unsuccessful");
                            Log.d(TAG, "From:" + fullPath);
                            Log.d(TAG, "To:" + fullFBPath);
                            Toast tst = Toast.makeText(getApplicationContext(),"File upload Unsuccessful", Toast.LENGTH_SHORT);
                            tst.show();
                        }
                    });
            // Create file metadata including the content type
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
                            Log.d(TAG,"File metadata update successful");
                            Log.d(TAG,"For file: "+fireBaseFolder+"//"+filename);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Log.d(TAG,"File metadata update unsuccessful");
                        }
                    });
        }
        else
        {
            return;
        }
    }
    public void deleteFile(final String filenamePref, final String filenameSuf, final String fireBaseFolder)
    {deleteFile(filenamePref + "." + filenameSuf,fireBaseFolder);}
    public void deleteFile(final String filename, final String fireBaseFolder)
    {
        if(null!=filename && null!=fireBaseFolder)
        {
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filename);
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d(TAG,"File deleted from cloud successfully");
                    Log.d(TAG, "From:" + fireBaseFolder+"/"+filename);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(TAG,"File not deleted from cloud!");
                    Log.d(TAG, "From:" + fireBaseFolder+"/"+filename);
                }
            });
        }
    }
}
