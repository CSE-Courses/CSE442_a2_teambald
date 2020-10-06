package com.teambald.cse442_project_team_bald;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.teambald.cse442_project_team_bald.Objects.LocalTransfer;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecordingListFragment extends Fragment {
    //TODO: @Chaoping: Create a list of recording object when you are done with it.
    private ArrayList<RecordingItem> recordingList;

    public RecordingListFragment() {
        recordingList=new ArrayList<>();
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
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        //TODO: @Chaoping: This is only for test purpose, replace them with real data later.
        //load(); need path
        recordingList.add(new RecordingItem("9/21/2020 1:25PM", "5 mins","", true));
        recordingList.add(new RecordingItem("9/21/2020 1:30PM", "5 mins", "",true));
        recordingList.add(new RecordingItem("9/21/2020 1:35PM", "5 mins", "",true));
        recordingList.add(new RecordingItem("9/21/2020 1:40PM", "5 mins", "",true));

        RecyclerView.Adapter mAdapter = new RecordingListAdapter(recordingList);
        recyclerView.setAdapter(mAdapter);
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

