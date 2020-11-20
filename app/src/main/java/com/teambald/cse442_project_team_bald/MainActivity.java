package com.teambald.cse442_project_team_bald;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.teambald.cse442_project_team_bald.Encryption.EnDecryptAudio;
import com.teambald.cse442_project_team_bald.Fragments.CloudListFragment;
import com.teambald.cse442_project_team_bald.Fragments.ListFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.Fragments.SettingFragment;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.TabsController.CloudListAdapter;
import com.teambald.cse442_project_team_bald.TabsController.LocalListAdapter;
import com.teambald.cse442_project_team_bald.TabsController.RecordingListAdapter;
import com.teambald.cse442_project_team_bald.TabsController.ViewPagerAdapter;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;
/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile, which also adds a request dialog to access the user's Google Drive.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainAct";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private static FirebaseStorage storage;
    private static StorageReference storageRef;

    private static final String durationMetaDataConst = "Duration";

    private Button proceedButton;

    private MainActivity siawd;


    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter vpa;

    private MenuItem uploadItem,downloadItem,deleteItem,selectAll,deselectAll;

    private CloudListFragment clf;
    private RecordingListFragment rlf_local;
    private RecordingListFragment rlf_downloaded;
    private CloudListAdapter cla;
    private LocalListAdapter rla_local;
    private LocalListAdapter rla_downloaded;
    /*
    * 0-Cloud
    * 1-Local Recorded
    * 2-Local Downloaded*/
    private int fragmentIndicator = -1;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar
        Toolbar tb = findViewById(R.id.my_toolbar);
        setSupportActionBar(tb);

        // Views

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
        Log.d(TAG, "mAuth null is:" + (null == mAuth));
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
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
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

        toast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        uploadItem = menu.findItem(R.id.upload);
        downloadItem = menu.findItem(R.id.download);
        deleteItem = menu.findItem(R.id.delete);
        selectAll = menu.findItem(R.id.selectall);
        deselectAll = menu.findItem(R.id.deselectall);
        setMenuItemsVisible(false);
        uploadItem.setOnMenuItemClickListener(new uploadItemListener());
        downloadItem.setOnMenuItemClickListener(new downloadItemListener());
        deleteItem.setOnMenuItemClickListener(new deleteItemListener());
        selectAll.setOnMenuItemClickListener(new selectAllItemListener());
        deselectAll.setOnMenuItemClickListener(new deselectAllItemListener());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
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
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        SettingFragment.updateSignInUI(null);
                    }
                });
    }
    // [END signOut]


    private ViewPagerAdapter createTabAdapter() {
        vpa = new ViewPagerAdapter(this);
        return vpa;
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public boolean downloadFile(String path, String localFolder, String filenamePref, String filenameSuf, String fireBaseFolder) {
        return downloadFile(path, localFolder, filenamePref + "." + filenameSuf, fireBaseFolder);
    }

    public boolean downloadFile(String path, String localFolder, String filename, String fireBaseFolder) {
        try {
            Log.d(TAG, "Downloading from");
            Log.d(TAG, fireBaseFolder);
            Log.d(TAG, filename);
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filename);
            final String fullPath = path + "/" + localFolder + "/" + filename;
            final File tempFile = new File(fullPath);
            storageReference.getFile(tempFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            //Decrypt and overwrite the file.
                            byte[] decrpted = EnDecryptAudio.decrypt(tempFile, getApplicationContext());
                            EnDecryptAudio.writeByteToFile(decrpted, tempFile.getPath());

                            Log.d(TAG, "File download Successful");
                            toast.setText("File download Successful");
                            toast.show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle failed download
                            // ...
                            Log.d(TAG, "Read file error on Failure");
                            toast.setText("File download Unsuccessful");
                            toast.show();
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, "Read file error");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean downloadFileToPlay(String path, String localFolder, String filename, String fireBaseFolder, final RecordingListAdapter rla, final RecordingItem recordingItem) {
        try {
            Log.d(TAG, "Downloading from");
            Log.d(TAG, fireBaseFolder);
            Log.d(TAG, filename);
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filename);
            final String fullPath = path + "/" + localFolder + "/" + filename;
            final File tempFile = new File(fullPath);
            storageReference.getFile(tempFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Log.d(TAG, "File Download Successful");
                            Log.d(TAG, "Start playing now");

                            //Decrypt and overwrite the file.
                            byte[] decrpted = EnDecryptAudio.decrypt(tempFile, getApplicationContext());
                            EnDecryptAudio.writeByteToFile(decrpted, tempFile.getPath());

                            toast.setText("Starting Audio Now");
                            toast.show();
                            rla.playAudio(tempFile, recordingItem);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle failed download
                            // ...
                            Log.d(TAG, "Read file error on Failure");
                            toast.setText("Audio Read Unsuccessful");
                            toast.show();
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, "Read file error");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void uploadRecording(final String fileUri, final String fireBaseFolder, final String duration) {
//        final String fullPath = path + "/" + filename;
//        final String fullFBPath = fireBaseFolder + "/" + filename;

        Log.i(TAG, "Trying uploadRecording, duration = " + duration);

        File f = new File(fileUri);
        final Uri file = Uri.fromFile(f);

        final StorageReference storageReference = storageRef.child(fireBaseFolder).child(f.getName());
        storageReference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Log.d(TAG, "File upload successful");
                        toast.setText("File Upload Successful");
                        toast.show();
                        Log.d(TAG, "Uploading metadata for: " + fileUri + " Metadata: " + duration);
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
                                        Log.d(TAG, "File metadata update successful");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                        Log.d(TAG, "File metadata update unsuccessful");
                                        Log.d(TAG, "Errors:");
                                        exception.printStackTrace();
                                        Log.d(TAG, exception.getLocalizedMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.d(TAG, "File upload unsuccessful");

                        toast.setText("File upload Unsuccessful");
                        toast.show();
                    }
                });
        //Remove temp file.
        f.delete();
    }

    public void deleteFile(final String filenamePref, final String filenameSuf, final String fireBaseFolder) {
        deleteFile(filenamePref + "." + filenameSuf, fireBaseFolder);
    }

    public void deleteFile(final String filename, final String fireBaseFolder) {
        if (null != filename && null != fireBaseFolder) {
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filename);
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d(TAG, "File deleted from cloud successfully");
                    Log.d(TAG, "From:" + fireBaseFolder + "/" + filename);

                    toast.setText("File deleted from cloud successfully");
                    toast.show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(TAG, "File not deleted from cloud!");
                    Log.d(TAG, "From:" + fireBaseFolder + "/" + filename);

                    toast.setText("File not deleted from cloud!");
                    toast.show();
                }
            });
        }
    }
    public void deleteFile(final String filename, final String fireBaseFolder, final CloudListFragment clf, final String firebaseFolder, final boolean updateUI) {
        if (null != filename && null != fireBaseFolder) {
            StorageReference storageReference = storageRef.child(fireBaseFolder).child(filename);
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d(TAG, "File deleted from cloud successfully");
                    Log.d(TAG, "From:" + fireBaseFolder + "/" + filename);
                    if(updateUI)
                        clf.listFiles(firebaseFolder);
                    toast.setText("File deleted from cloud successfully");
                    toast.show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(TAG, "File not deleted from cloud!");
                    Log.d(TAG, "From:" + fireBaseFolder + "/" + filename);
                    if(updateUI)
                        clf.listFiles(firebaseFolder);
                    toast.setText("File not deleted from cloud!");
                    toast.show();
                }
            });
        }
    }

    /*
     * 0-Cloud
     * 1-Local Recorded
     * 2-Local Downloaded*/
    private class uploadItemListener implements MenuItem.OnMenuItemClickListener
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuitem) {
            Log.d(TAG, "Upload button pressed in menu");
            FirebaseUser fbuser = getmAuth().getCurrentUser();
            if (fbuser == null) {
                Toast.makeText(getApplicationContext(), "Please Log in", Toast.LENGTH_SHORT).show();
                return false;
            }
            String fireBaseFolder = fbuser.getEmail();
            if(fragmentIndicator == 1 && rlf_local != null && rla_local !=null)
            {
                Log.d(TAG, "Uploading local items");
                ArrayList<RecordingItem> allItems = rla_local.getmDataset();
                ArrayList<RecordingItem> checkedItems = getCheckedItems(allItems);
                Log.d(TAG, "Uploading "+checkedItems.size()+" items from :"+allItems.size() +" items");
                for(RecordingItem item:checkedItems) {
                    File file = item.getAudio_file();
                    Log.d(TAG, "Uploading file: " + file.getAbsolutePath());
                    String path = rlf_local.getDirectory_toRead() + File.separator;

                    byte[] encoded = EnDecryptAudio.encrypt(file.getPath(), getApplicationContext());
                    final String tempFilePath = getApplicationContext().getExternalFilesDir("/").getAbsolutePath()
                            + File.separator + "tmp" + File.separator + file.getName();
                    //Write bytes to a file.
                    EnDecryptAudio.writeByteToFile(encoded, tempFilePath);
                    uploadRecording(tempFilePath, fireBaseFolder, item.getDuration());
                }
                Log.d(TAG,"Uploaded All Selected Items");
            }
            else if(fragmentIndicator == 2 && rlf_downloaded != null && rla_downloaded !=null)
            {
                Log.d(TAG, "Uploading downloaded items");
                ArrayList<RecordingItem> allItems = rla_downloaded.getmDataset();
                ArrayList<RecordingItem> checkedItems = getCheckedItems(allItems);
                Log.d(TAG, "Uploading "+checkedItems.size()+" items from :"+allItems.size() +" items");
                for(RecordingItem item:checkedItems) {
                    File file = item.getAudio_file();
                    Log.d(TAG, "Uploading file: " + file.getAbsolutePath());
                    String path = rlf_downloaded.getDirectory_toRead() + File.separator;

                    byte[] encoded = EnDecryptAudio.encrypt(file.getPath(), getApplicationContext());
                    final String tempFilePath = getApplicationContext().getExternalFilesDir("/").getAbsolutePath()
                            + File.separator + "tmp" + File.separator + file.getName();
                    //Write bytes to a file.
                    EnDecryptAudio.writeByteToFile(encoded, tempFilePath);
                    uploadRecording(tempFilePath, fireBaseFolder, item.getDuration());
                }
                Log.d(TAG,"Uploaded All Selected Items");
            }
            else
            {
                Log.d(TAG,"unknown fragment indicator or fragment/adapters null");
                return false;
            }
            return true;
        }
    }
    private class downloadItemListener implements MenuItem.OnMenuItemClickListener
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuitem) {
            Log.d(TAG, "Download button pressed in menu");
            FirebaseUser fbuser = getmAuth().getCurrentUser();
            if (fbuser == null) {
                Toast.makeText(getApplicationContext(), "Please Log in", Toast.LENGTH_SHORT).show();
                return false;
            }
            String fireBaseFolder = fbuser.getEmail();
            if(fragmentIndicator == 0 && clf != null && cla !=null)
            {
                Log.d(TAG, "Downloading cloud items");
                ArrayList<RecordingItem> allItems = cla.getmDataset();
                ArrayList<RecordingItem> checkedItems = getCheckedItems(allItems);
                Log.d(TAG,"Downloading "+checkedItems.size() +" items out of "+allItems.size() + " items");
                for(RecordingItem item:checkedItems) {
                    File file = item.getAudio_file();
                    Log.d(TAG, "Downloading file: " + file.getAbsolutePath());
                    String path = getExternalFilesDir("/").getAbsolutePath() + File.separator + "CloudRecording" + File.separator;
                    downloadFile(path, "", file.getName(), fireBaseFolder);
                }
                Log.d(TAG,"All items downloaded");
            }
            else if(fragmentIndicator == 2 && rlf_downloaded != null && rla_downloaded !=null)
            {
                Log.d(TAG, "Uploading downloaded items");
                setDataSetCheck(rlf_downloaded.getItems(),true);
                rla_downloaded.notifyDataSetChanged();
                Log.d(TAG,"Checked all items in rla_downloaded");
            }
            else
            {
                Log.d(TAG,"unknown fragment indicator or fragment/adapters null");
                return false;
            }
            return true;
        }
    }
    private class deleteItemListener implements MenuItem.OnMenuItemClickListener
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuitem) {
            Log.d(TAG, "Delete button pressed in menu");
            if(fragmentIndicator == 0 && clf!=null && cla!=null)
            {
                Log.d(TAG, "Deleting checked items in cloud fragment");
                FirebaseUser fbuser = getmAuth().getCurrentUser();
                if (fbuser == null) {
                    Toast.makeText(getApplicationContext(), "Please Log in", Toast.LENGTH_SHORT).show();
                    return false;
                }
                String fireBaseFolder = fbuser.getEmail();
                Log.d(TAG, "Deleting cloud items");
                ArrayList<RecordingItem> allItems = cla.getmDataset();
                ArrayList<RecordingItem> checkedItems = getCheckedItems(allItems);
                for(RecordingItem item : checkedItems) {
                    File file = item.getAudio_file();
                    Log.d(TAG, "Deleting cloud file: " + file.getAbsolutePath());
                    deleteFile(file.getName(), fireBaseFolder,clf,fireBaseFolder,item.equals(checkedItems.get(checkedItems.size()-1)));
                }
            }
            else if(fragmentIndicator == 1 && rlf_local != null && rla_local !=null)
            {
                Log.d(TAG, "Deleting checked items in local fragment");
                ArrayList<RecordingItem> allItems = rla_local.getmDataset();
                ArrayList<Integer> checkedItems = getCheckedItemsPosition(allItems);
                for(int idx = checkedItems.size()-1;idx>=0;idx--)
                {
                    int pos = checkedItems.get(idx);
                    rla_local.deleteItem(idx);
                    rla_local.notifyDataSetChanged();
                    Log.d(TAG, "Deleting local file at position: " + pos);
                }
                Log.d(TAG, "Deleted all checked local files");
            }
            else if(fragmentIndicator == 2 && rlf_downloaded != null && rla_downloaded !=null)
            {
                Log.d(TAG, "Deleting checked items in downloaded fragment");
                ArrayList<RecordingItem> allItems = rla_downloaded.getmDataset();
                ArrayList<Integer> checkedItems = getCheckedItemsPosition(allItems);
                for(int idx = checkedItems.size()-1;idx>=0;idx--)
                {
                    int pos = checkedItems.get(idx);
                    rla_downloaded.deleteItem(idx);
                    rla_downloaded.notifyDataSetChanged();
                    Log.d(TAG, "Deleting downloaded file at position: " + pos);
                }
                Log.d(TAG, "Deleted all checked downloaded files");
            }
            else
            {
                Log.d(TAG,"unknown fragment indicator or fragment/adapters null");
                return false;
            }
            return true;
        }
    }
    public ArrayList<RecordingItem> getCheckedItems(ArrayList<RecordingItem> items)
    {
        Log.d(TAG,"Getting checked items");
        ArrayList<RecordingItem> checkedItems = new ArrayList<>();
        if(items == null)
        {
            Log.d(TAG,"items object null");
            return checkedItems;
        }
        for(RecordingItem item : items)
        {
            if(item.getChecked())
            {
                checkedItems.add(item);
                Log.d(TAG,"An item is checked");
            }
            else
            {
                Log.d(TAG,"An item is not checked");
            }
        }
        return checkedItems;
    }
    public ArrayList<Integer> getCheckedItemsPosition(ArrayList<RecordingItem> items)
    {
        Log.d(TAG,"Getting checked items position");
        ArrayList<Integer> checkedItems = new ArrayList<>();
        if(items == null)
        {
            Log.d(TAG,"items object null");
            return checkedItems;
        }
        for(int idx = 0; idx<items.size();idx++)
        {
            RecordingItem item = items.get(idx);
            if(item.getChecked())
            {
                checkedItems.add(idx);
                Log.d(TAG,"An item is checked");
            }
            else
            {
                Log.d(TAG,"An item is not checked");
            }
        }
        return checkedItems;
    }
    /*
     * 0-Cloud
     * 1-Local Recorded
     * 2-Local Downloaded*/
    private class selectAllItemListener implements MenuItem.OnMenuItemClickListener
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuitem) {
            Log.d(TAG, "Select All button pressed in menu");
            //Toast.makeText(MainActivity.this, "Selecting all items", Toast.LENGTH_SHORT).show();
            if(fragmentIndicator == 0 && clf!=null && cla!=null)
            {
                setDataSetCheck(cla.getmDataset(),true);
                cla.notifyDataSetChanged();
            }
            else if(fragmentIndicator == 1 && rlf_local != null && rla_local !=null)
            {
                setDataSetCheck(rla_local.getmDataset(),true);
                rla_local.notifyDataSetChanged();
            }
            else if(fragmentIndicator == 2 && rlf_downloaded != null && rla_downloaded !=null)
            {
                setDataSetCheck(rlf_downloaded.getItems(),true);
                rla_downloaded.notifyDataSetChanged();
            }
            else
            {
                Log.d(TAG,"unknown fragment indicator or fragment/adapters null");
                return false;
            }
            return true;
        }
    }
    private class deselectAllItemListener implements MenuItem.OnMenuItemClickListener
    {
        @Override
        public boolean onMenuItemClick(MenuItem menuitem) {
            Log.d(TAG, "Deselect all button pressed in menu");
            if(fragmentIndicator == 0 && clf!=null && cla!=null)
            {
                setDataSetCheck(cla.getmDataset(),false);
                cla.notifyDataSetChanged();
                Log.d(TAG,"Unchecked all items in cla");
            }
            else if(fragmentIndicator == 1 && rlf_local != null && rla_local !=null)
            {
                setDataSetCheck(rla_local.getmDataset(),false);
                rla_local.notifyDataSetChanged();
                Log.d(TAG,"Unchecked all items in rla_local");
            }
            else if(fragmentIndicator == 2 && rlf_downloaded != null && rla_downloaded !=null)
            {
                setDataSetCheck(rlf_downloaded.getItems(),false);
                rla_downloaded.notifyDataSetChanged();
                Log.d(TAG,"Unchecked all items in rla_downloaded");
            }
            else
            {
                Log.d(TAG,"unknown fragment indicator or fragment/adapters null");
                return false;
            }
            return true;
        }
    }
    public void setDataSetCheck(ArrayList<RecordingItem> items,boolean check)
    {
        for(RecordingItem item1 : items)
        {
            item1.setChecked(check);
        }
    }

    /*
     * 0-Cloud
     * 1-Local Recorded
     * 2-Local Downloaded*/
    public void setMenuItemsVisible(boolean visible)
    {
        if(!visible)
        {fragmentIndicator = -1;}
        if(uploadItem!=null){uploadItem.setVisible(visible);}
        if(downloadItem!=null){downloadItem.setVisible(visible);}
        if(deleteItem!=null){deleteItem.setVisible(visible);}
        if(selectAll!=null){selectAll.setVisible(visible);}
        if(deselectAll!=null){deselectAll.setVisible(visible);}
    }
    public void setMenuItemsVisible(CloudListFragment cloudListFragment, CloudListAdapter cloudListAdapter)
    {
        fragmentIndicator = 0;
        clf = cloudListFragment;
        cla = cloudListAdapter;
        Log.d(TAG,"Cloud fragment registered");
        if(uploadItem!=null){uploadItem.setVisible(false);}
        if(downloadItem!=null){downloadItem.setVisible(true);}
        if(deleteItem!=null){deleteItem.setVisible(true);}
        if(selectAll!=null){selectAll.setVisible(true);}
        if(deselectAll!=null){deselectAll.setVisible(true);}
    }
    public void setMenuItemsVisible(RecordingListFragment recordingListFragment, LocalListAdapter recordingListAdapter,int ind)
    {
        fragmentIndicator = ind;
        if(ind == 1)
        {
            rlf_local = recordingListFragment;
            rla_local = recordingListAdapter;
            Log.d(TAG,"Local fragment registered");
        }
        else if(ind == 2)
        {
            rlf_downloaded = recordingListFragment;
            rla_downloaded = recordingListAdapter;
            Log.d(TAG,"Downloaded fragment registered");
        }
        else
        {
            Log.d(TAG,"Invalid index");
        }
        if(uploadItem!=null){uploadItem.setVisible(true);}
        if(downloadItem!=null){downloadItem.setVisible(false);}
        if(deleteItem!=null){deleteItem.setVisible(true);}
        if(selectAll!=null){selectAll.setVisible(true);}
        if(deselectAll!=null){deselectAll.setVisible(true);}
    }
}
