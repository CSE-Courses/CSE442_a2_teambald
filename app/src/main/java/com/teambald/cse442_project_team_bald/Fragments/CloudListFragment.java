package com.teambald.cse442_project_team_bald.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.TabsController.CloudListAdapter;
import com.teambald.cse442_project_team_bald.TabsController.SwipeActionHandler;

import java.io.File;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class CloudListFragment extends ListFragment {
    private static final String durationMetaDataConst = "Duration";
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static int mColumnCount = 1;
    // TODO: Customize parameters
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private CloudListAdapter mAdapter;

    private MainActivity activity;
    private String fireBaseFolder = null;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private static final String TAG = "CloudFrag";

    public CloudListFragment(MainActivity mainActivity) {
        activity = mainActivity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cloud_list, container, false);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean logedIn = sharedPref.getBoolean(SettingFragment.LogedInBl,false);
        fireBaseFolder = sharedPref.getString(SettingFragment.LogInEmail,null);
        if(logedIn) {
            RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview_cloud);
            recyclerView.setHasFixedSize(true);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
            recyclerView.setLayoutManager(layoutManager);
            listFiles(fireBaseFolder);
            mAdapter = new CloudListAdapter(itemList, getContext(),this,activity);
            recyclerView.setAdapter(mAdapter);
            ItemTouchHelper itemTouchHelper = new
                    ItemTouchHelper(new SwipeActionHandler((CloudListAdapter) mAdapter,this,"CloudRecording",1, getContext()));
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
        else
        {
            RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview_cloud);
            recyclerView.setHasFixedSize(true);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
            recyclerView.setLayoutManager(layoutManager);
            itemList.clear();
            itemList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
            mAdapter = new CloudListAdapter(itemList, getContext(),this,activity);
            recyclerView.setAdapter(mAdapter);
            //        TapTargetView.showFor(this.activity,                 // `this` is an Activity
//                TapTarget.forView(view.findViewById(R.id.recorder_button), getString(R.string.Home_Recorder_Title),
//                        getString(R.string.Home_Recorder_Description))
//                        // All options below are optional
//                        .outerCircleColor(R.color.ubBlue)      // Specify a color for the outer circle
//                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
//                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
//                        .titleTextSize(40)                  // Specify the size (in sp) of the title text
//                        .titleTextColor(R.color.white)      // Specify the color of the title text
//                        .descriptionTextSize(20)            // Specify the size (in sp) of the description text
//                        .descriptionTextColor(R.color.white)  // Specify the color of the description text
//                        .textColor(R.color.white)            // Specify a color for both the title and description text
//                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
//                        .dimColor(R.color.white)            // If set, will dim behind the view with 30% opacity of the given color
//                        .drawShadow(true)                   // Whether to draw a drop shadow or not
//                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
//                        .tintTarget(true)                   // Whether to tint the target view's color
//                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
//                        .targetRadius(120),                  // Specify the target radius (in dp)
//                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
//                    @Override
//                    public void onTargetClick(TapTargetView view) {
//                        super.onTargetClick(view);      // This call is optional
//                        view.dismiss(true);
//                    }
//                });

        }
    }
    public void listFiles(String fireBaseFolder)
    {
        if( activity.getmAuth().getCurrentUser() != null) {
            StorageReference listRef = storageRef.child(fireBaseFolder);
            CloudSuccListener rstListener = new CloudSuccListener(fireBaseFolder);
            listRef.listAll()
                    .addOnSuccessListener(rstListener)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Uh-oh, an error occurred!
                            Log.d(TAG,"File list error");
                        }
                    });
        }
        else
        {
            itemList.clear();
            itemList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
            updateUI();
        }
    }
    public void updateFiles(ArrayList<String> filenames)
    {
        itemList.clear();
        for (int idx = 0;idx<filenames.size();idx++)
        {
            String filename = filenames.get(idx);
            itemList.add(new RecordingItem(filename, "Duration: ", filename, true, new File(filename)));
        }
        RecyclerView recyclerView = getView().findViewById(R.id.recording_list_recyclerview_cloud);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeActionHandler((CloudListAdapter) mAdapter,this,"CloudRecording",1, getContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        updateUI();
        Log.d(TAG,"There are "+itemList.size()+" items in cloud list");
    }
    private class CloudSuccListener implements OnSuccessListener<ListResult>
    {
        public ArrayList<String> filenames = new ArrayList<>();

        public ArrayList<String> getFilenames() {
            return filenames;
        }

        private String FBfolder = null;

        public CloudSuccListener(String fireBaseFolder)
        {
            FBfolder = fireBaseFolder;
        }
        @Override
        public void onSuccess(ListResult listResult) {
            for (StorageReference item : listResult.getItems()) {
                // All the items under listRef.
                Log.d(TAG,item.getName());
                filenames.add(item.getName());
            }
            updateFiles(filenames);
            updateMetaData(filenames,FBfolder);
        }
    }
    private void updateMetaData(ArrayList<String> filenames,String fireBaseFolder)
    {
        ArrayList<CloudMetaSuccListener> succListeners = new ArrayList<>();
        for(int idx = 0;idx<filenames.size();idx++)
        {
            final String filename = filenames.get(idx);
            CloudMetaSuccListener metaRstListener = new CloudMetaSuccListener(filename,idx);
            succListeners.add(metaRstListener);
            StorageReference listRef = storageRef.child(fireBaseFolder).child(filename);
            listRef.getMetadata()
                    .addOnSuccessListener(metaRstListener)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Log.d(TAG,"MetaData for File: "+filename+" Download Unsuccessful");
                        }
                    });
        }

        updateUI();
        Log.d(TAG,"Update the list for durations");
    }
    private class CloudMetaSuccListener implements OnSuccessListener<StorageMetadata>
    {
        private String filename;
        private String metaDataValue;// = null;
        private int listIdx;
        public CloudMetaSuccListener(String fn,int idx)
        {
            metaDataValue = null;
            filename = fn;
            listIdx = idx;
        }
        public String getMetaDataValue() {
            return metaDataValue;
        }
        @Override
        public void onSuccess(StorageMetadata metaDataRst) {
            metaDataValue = metaDataRst.getCustomMetadata(durationMetaDataConst);
            Log.d(TAG,"MetaData for File: "+filename+" Downloaded successfully");
            itemList.get(listIdx).setDuration(metaDataValue);
            updateUI();
            Log.d(TAG,"Update the list for metadata idx: "+listIdx+" Metadata: "+metaDataValue);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"Showing MenuItems");
        activity.setMenuItemsVisible(this, mAdapter);

        if(storageRef!=null) {
            Log.d(TAG,"OnResume: ");
            //Check if signed in to avoid NullPointerException.
            if(getMainActivity().getmAuth().getCurrentUser() != null) {
                fireBaseFolder = activity.getmAuth().getCurrentUser().getEmail();
                Log.d(TAG,"signed in as:" + fireBaseFolder);
                listFiles(fireBaseFolder);
            }
            else
            {
                itemList.clear();
                itemList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
            }

        }
        else
        {
            itemList.clear();
            itemList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
        }
        updateUI();
        Log.d(TAG,"Update the list");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(storageRef!=null && activity.getmAuth().getCurrentUser() != null){
            Log.d(TAG,"OnAttach: ");
            fireBaseFolder = activity.getmAuth().getCurrentUser().getEmail();
            listFiles(fireBaseFolder);
            updateUI();
        }
        Log.d(TAG,"Showing MenuItems");
        activity.setMenuItemsVisible(this, mAdapter);
    }
    public void updateUI()
    {
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        else
        {Log.d(TAG,"madapter null");}
    }
    public String parseSeconds(int seconds) {
        int min = seconds / 60;
        seconds-=(min * 60);
        return (min < 10 ? "0" + min : String.valueOf(min)) + ":" + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
    }
    public MainActivity getMainActivity()
    {return activity;}
}