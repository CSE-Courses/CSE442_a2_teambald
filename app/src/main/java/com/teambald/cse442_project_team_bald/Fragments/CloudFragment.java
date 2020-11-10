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
public class CloudFragment extends Fragment {
    private ArrayList<RecordingItem> cloudList = new ArrayList<>();
    private ArrayList<String> durations = new ArrayList<>();

    private static final String durationMetaDataConst = "Duration";
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static int mColumnCount = 1;
    // TODO: Customize parameters
    private static FirebaseStorage storage;
    private static StorageReference storageRef;
    private RecyclerView.Adapter mAdapter;

    private MainActivity activity;
    private String fireBaseFolder = null;
    /*
    private Button down;
    private Button up;
    private Button list;
    */
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    private static final String TAG = "CloudFrag";

    public CloudFragment(MainActivity mainActivity) {
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
        /*View view = inflater.inflate(R.layout.dummy_layout_cloud, container, false);

        up = view.findViewById(R.id.uploadbu);
        down = view.findViewById(R.id.downbu);
        list = view.findViewById(R.id.listbu);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
                uploadFile(path,"Recording_2020_10_21_06_34_00","mp4","baicheng");
            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
                downloadFile(path,"testRec1","mp4","baicheng");
            }
        });

        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listFiles("baicheng");
            }
        });*/
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
            //ArrayList<String> filenames =
            //Log.d(TAG,filenames.toString());
            //Log.d(TAG,filenames.size()+"");
            listFiles(fireBaseFolder);
            mAdapter = new CloudListAdapter(cloudList, getContext(),this,activity);
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
            //ArrayList<String> filenames =
            //Log.d(TAG,filenames.toString());
            //Log.d(TAG,filenames.size()+"");
            cloudList.clear();
            cloudList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
            mAdapter = new CloudListAdapter(cloudList, getContext(),this,activity);
            recyclerView.setAdapter(mAdapter);
        }
    }
    public void listFiles(String fireBaseFolder)
    {
        GoogleSignInAccount gsi = GoogleSignIn.getLastSignedInAccount(getActivity());
        Log.d(TAG,gsi+"");
        if( gsi != null) {
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
            cloudList.clear();
            cloudList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
            if(mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    public void updateFiles(ArrayList<String> filenames)
    {
        cloudList.clear();
        for (int idx = 0;idx<filenames.size();idx++)
        {
            String filename = filenames.get(idx);
            cloudList.add(new RecordingItem(filename, "Duration: ", filename, true, new File(filename)));
        }
        RecyclerView recyclerView = getView().findViewById(R.id.recording_list_recyclerview_cloud);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeActionHandler((CloudListAdapter) mAdapter,this,"CloudRecording",1, getContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            Log.d(TAG,"mAdapter notified");
        }
        Log.d(TAG,"There are "+cloudList.size()+" items in cloud list");
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
        durations = new ArrayList<>();
        ArrayList<CloudMetaSuccListener> succListeners = new ArrayList<>();
        for(int idx = 0;idx<filenames.size();idx++)
        {
            final String filename = filenames.get(idx);
            CloudMetaSuccListener metaRstListener = new CloudMetaSuccListener(filename);
            succListeners.add(metaRstListener);
            StorageReference listRef = storageRef.child(fireBaseFolder).child(filename);
            listRef.getMetadata()
                    .addOnSuccessListener(metaRstListener)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Log.d(TAG,"MetaData for File: "+filename+" Downloaded successfully");
                        }
                    });
        }
        for(int idx = 0;idx<succListeners.size();idx++)
        {
            CloudMetaSuccListener listener = succListeners.get(idx);
            durations.add(listener.getMetaDataValue());
        }

        for(int idx = 0;idx<durations.size();idx++) {
            String duration = durations.get(idx);
            cloudList.get(idx).setDuration(duration);
        }

        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            Log.d(TAG,"Update the list");
        }
    }
    private class CloudMetaSuccListener implements OnSuccessListener<StorageMetadata>
    {
        private String filename;
        private String metaDataValue;// = null;
        public CloudMetaSuccListener(String fn)
        {
            metaDataValue = null;
            filename = fn;
        }
        public String getMetaDataValue() {
            return metaDataValue;
        }
        @Override
        public void onSuccess(StorageMetadata metaDataRst) {
            metaDataValue = metaDataRst.getCustomMetadata(durationMetaDataConst);
            Log.d(TAG,"MetaData for File: "+filename+" Downloaded successfully");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
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
                cloudList.clear();
                cloudList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
            }

        }
        else
        {
            cloudList.clear();
            cloudList.add(new RecordingItem("Please Sign In", "To view Cloud Recordings", true));
        }
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            Log.d(TAG,"Update the list");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(storageRef!=null && activity.getmAuth().getCurrentUser() != null){
            Log.d(TAG,"OnAttach: ");
            fireBaseFolder = activity.getmAuth().getCurrentUser().getEmail();
            listFiles(fireBaseFolder);
            if(mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }
    /*
    public void updateAllFiles(String bucketname)
    {
        ArrayList<String> files = listFiles(bucketname);
        Log.d(TAG,"There are "+files.size()+" items in files list");
        cloudList.clear();
        for (String filename : files)
        {
            File f = new File(filename);
            Uri uri = Uri.parse(f.getAbsolutePath());
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(getContext(), uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int seconds = Integer.parseInt(durationStr) / 1000;
            durationStr = parseSeconds(seconds);
            cloudList.add(new RecordingItem(f.getName(), durationStr , f.getPath(), true, f));
        }
        Log.d(TAG,"There are "+cloudList.size()+" items in cloud list");
    }*/
    public String parseSeconds(int seconds) {
        int min = seconds / 60;
        seconds-=(min * 60);
        return (min < 10 ? "0" + min : String.valueOf(min)) + ":" + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
    }
    public MainActivity getMainActivity()
    {return activity;}
}