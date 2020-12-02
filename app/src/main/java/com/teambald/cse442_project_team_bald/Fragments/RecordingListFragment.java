package com.teambald.cse442_project_team_bald.Fragments;

import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.teambald.cse442_project_team_bald.Encryption.AudioEncryptionUtils;
import com.teambald.cse442_project_team_bald.Encryption.FileUtils;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.TabsController.LocalListAdapter;
import com.teambald.cse442_project_team_bald.TabsController.SwipeActionHandler;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class RecordingListFragment extends ListFragment {
    private MediaPlayer mediaPlayer = null;
    private ImageButton BackButton;
    private File[] allFiles;
    private LocalListAdapter mAdapter;
    private static final String TAG = "RecordingListF";
    private String Directory_toRead;


    private MainActivity activity;

    public RecordingListFragment(MainActivity mainActivity,String path) {
        this.Directory_toRead = path;
        activity = mainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.recording_list_fragment, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new LocalListAdapter(itemList,getContext(),this,activity);
        recyclerView.setAdapter(mAdapter);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeActionHandler( (LocalListAdapter)mAdapter,this,Directory_toRead,0, getContext()));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        readAllFiles(Directory_toRead);
        //setup the Back Button
        this.BackButton = view.findViewById(R.id.Back_Button);
        this.BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment recordingListFG = new RecordSelectFragment(activity);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(((ViewGroup)getView().getParent()).getId() , recordingListFG );
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        if(activity!=null)
            showMenu();

        TapTargetSequence sequence= new TapTargetSequence(this.activity)
                .targets(
                        TapTarget.forView(BackButton, "BackButton",
                                getString(R.string.Home_Recorder_Description))
                                .outerCircleColor(R.color.ubBlue)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                                .targetCircleColor(R.color.white)   // Specify a color for the target circle
                                .titleTextSize(40)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(20)            // Specify the size (in sp) of the description text
                                .descriptionTextColor(R.color.white)  // Specify the color of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.white)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(40)               // Specify the target radius (in dp)

                );
        sequence.start();
    }// On view created end

    /*
     * 0-Cloud
     * 1-Local Recorded
     * 2-Local Downloaded*/
    @Override
    public void onResume() {
        super.onResume();
        //Update saved audio file to make sure the recordings are up-to-date.
        Log.d(TAG,"On Resume function");
        readAllFiles(Directory_toRead);
        if(activity!=null)
            showMenu();
        else
        {
            Log.d(TAG,"Activity null, menu not shown");
        }
    }
    public String getDirectory_toRead(){return Directory_toRead;}
    public void showMenu()
    {
        if(Directory_toRead !=null && Directory_toRead.contains("CloudRecording")) {
            activity.setMenuItemsVisible(this, mAdapter, 2);
            Log.d(TAG,"Showing MenuItems in recording list fragment: idx: "+2);
        }
        else if(Directory_toRead !=null && Directory_toRead.contains("LocalRecording")) {
            activity.setMenuItemsVisible(this, mAdapter, 1);
            Log.d(TAG,"Showing MenuItems in recording list fragment: idx: "+1);
        }
        else {
            Log.d(TAG, "Unknown directory to read or null");
        }
    }
    /*
     * This will be called in onResume().
     */
    public void readAllFiles(String path) {
//        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
//        path = path+File.separator+"LocalRecording";//Local
        File directory = new File(path);
        allFiles = directory.listFiles();
        itemList.clear();
        ArrayList<RecordingItem> unlocked=new ArrayList<>();

        if(allFiles == null)
        {
            Log.d(TAG, "all files array null");
        }
        else {
            Arrays.sort(allFiles, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.compare(f1.lastModified(), f2.lastModified());
                }
            });
            Log.d(TAG, "reading " + allFiles.length + " items");
            for (File f : allFiles) {
                try {
                    Log.i("File Path", f.getAbsolutePath());
                    Uri uri = Uri.parse(f.getAbsolutePath());
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(getContext(), uri);
                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    int seconds = Integer.parseInt(durationStr) / 1000;
                    durationStr = parseSeconds(seconds);
                    String name = "";
                    for (int i = 0; i < f.getName().length(); i++) {
                        if (f.getName().charAt(i) != '_') {
                            name += f.getName().charAt(i);
                        } else {
                            break;
                        }
                    }
                    if (f.getName().indexOf("Record") == 0) {
                        name = f.getName();
                    }
                    if(!f.getName().contains("_L")){
                        unlocked.add(new RecordingItem(name, durationStr, f.getPath(), true, f));
                    }
                    itemList.add(new RecordingItem(name, durationStr, f.getPath(), true, f));
                } catch (Exception e) {
                    Log.e(TAG, "" + e);
                }
            }
        }
        while(unlocked.size()>5){
            File file_delete =unlocked.get(0).getAudio_file();
            unlocked.remove(0);
            itemList.remove(file_delete);
            file_delete.delete();
        }

        Log.d(TAG,"mAdapter notified, size of list "+ itemList.size());
        updateUI();
    }

    public void updateUI()
    {
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        else
        {
            Log.d(TAG,"mAdapter null");
        }
    }

    public String parseSeconds(int seconds) {
        int min = seconds / 60;
        seconds-=(min * 60);
        return (min < 10 ? "0" + min : String.valueOf(min)) + ":" + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
    }

    public MainActivity getMainActivity()
    {return activity;}

    private void setpath(String path){
        this.Directory_toRead = path;
    }
}

