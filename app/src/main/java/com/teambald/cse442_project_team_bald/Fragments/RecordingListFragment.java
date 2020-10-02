package com.teambald.cse442_project_team_bald.Fragments;

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
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.TabsController.RecordingListAdapter;

import java.util.ArrayList;

public class RecordingListFragment extends Fragment {
    //TODO: @Chaoping: Create a list of recording object when you are done with it.
  
    private ArrayList<RecordingItem> recordingList = new ArrayList<>();


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
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        //TODO: @Chaoping: This is only for test purpose, replace them with real data later.
        recordingList.add(new RecordingItem("9/21/2020 1:25PM", "5 mins","", true));
        recordingList.add(new RecordingItem("9/21/2020 1:30PM", "5 mins", "",true));
        recordingList.add(new RecordingItem("9/21/2020 1:35PM", "5 mins", "",true));
        recordingList.add(new RecordingItem("9/21/2020 1:40PM", "5 mins", "",true));

        RecyclerView.Adapter mAdapter = new RecordingListAdapter(recordingList);
        recyclerView.setAdapter(mAdapter);
    }
}

