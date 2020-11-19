package com.teambald.cse442_project_team_bald.Fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;

import java.io.File;
import java.util.ArrayList;

public class RecordSelectFragment extends Fragment {
    private ArrayList<RecordingItem> recordingList = new ArrayList<>();
    private MediaPlayer mediaPlayer = null;
    private File[] allFiles;
    private RecyclerView.Adapter mAdapter;
    private static final String TAG = "RecordingListF";
    private Button HomeButton;
    private Button CloudButton;
    private Button BackButton;
    private String LocalRecord_Directory;
    private String CloudRecord_Directory;
    private MainActivity activity;


    private RecordingListFragment recordedFrag = null;
    private RecordingListFragment downloadedFrag = null;

    public RecordSelectFragment(MainActivity mainActivity)
    {
        activity = mainActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recording_selector, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String rawpath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        LocalRecord_Directory = rawpath+File.separator+"LocalRecording";
        CloudRecord_Directory = rawpath+File.separator+"CloudRecording";

        this.HomeButton = view.findViewById(R.id.Home);
        HomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordedFrag = new RecordingListFragment(activity,LocalRecord_Directory);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.add(((ViewGroup)getView().getParent()).getId() , recordedFrag );
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        this.CloudButton = view.findViewById(R.id.Cloud);
        CloudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadedFrag = new RecordingListFragment(activity,CloudRecord_Directory);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.add(((ViewGroup)getView().getParent()).getId() , downloadedFrag );
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"on resume in selector");
        if(recordedFrag!=null)
        {
            recordedFrag.readAllFiles(LocalRecord_Directory);
            Log.d(TAG,"updating recordedFrag");
        }
        else {
            Log.d(TAG,"recordedFrag null");
        }
        if(downloadedFrag!=null)
        {
            downloadedFrag.readAllFiles(CloudRecord_Directory);
            Log.d(TAG,"updating downloadedFrag");
        }
        else {
            Log.d(TAG,"downloadedFrag null");
        }
        Log.d(TAG,"Setting MenuItems Invisible");
        activity.setMenuItemsVisible(false);
    }
    public RecordingListFragment getRecordedFrag()
    {return recordedFrag;}
    public RecordingListFragment getDownloadedFrag()
    {return downloadedFrag;}
}
