package com.teambald.cse442_project_team_bald.Fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.teambald.cse442_project_team_bald.Objects.LocalTransfer;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.TabsController.RecordingListAdapter;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecordingListFragment extends Fragment {
    //TODO: @Chaoping: Create a list of recording object when you are done with it.
  
    private ArrayList<RecordingItem> recordingList = new ArrayList<>();
    private MediaPlayer mediaPlayer = null;
    private File[] allFiles;
    public RecordingListFragment() {

    }
    public void load(){
        LocalTransfer transfer=new LocalTransfer();
        // transfer.loadData(recordingList); // need path to implement the function
    }

    public static RecordingListFragment newInstance() {
        return new RecordingListFragment();
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
        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path);
        allFiles = directory.listFiles();


        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        //TODO: @Xuanhua: implement getRecordingList from Local Storage
        File TestFile =  allFiles[1];
        recordingList.add(new RecordingItem(TestFile.getName(), "5 mins",TestFile.getPath(), true,TestFile));

        RecyclerView.Adapter mAdapter = new RecordingListAdapter(recordingList);
        recyclerView.setAdapter(mAdapter);
    }

    //TODO: @Xuanhua: to get all AudioFiles
    public void getAudioFiles(){

    }



    //method use to Update the lists in external storage, need to be call on the background daily.
    public void UpToDate() throws ParseException {
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File Data= new File(recordPath);
        String[] pathnames=Data.list();
        SimpleDateFormat formatter = new SimpleDateFormat("MM_dd", Locale.US);
        Date now = new Date();
        int limite_time=7; // set time to delete to 7;
        for(String pathname:pathnames){
            //find the dates and compare with current date
            String date=pathname.substring("Recording_".length()+5,"Recording_".length()+10);
            Date save_date= formatter.parse(date);
            long diff_in_date=now.getTime()-save_date.getTime();
            long diffDays = diff_in_date / (24 * 60 * 60 * 1000); // find the different in day
            if(diffDays>limite_time){
                // do delete if difference greater than limite_time
                File file = new File(pathname);
                boolean deleted = file.delete(); // execute deletes
            }

        }
    }



}

