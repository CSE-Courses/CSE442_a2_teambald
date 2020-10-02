package com.teambald.cse442_project_team_bald.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.TabsController.RecordingListAdapter;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class CloudFragment extends Fragment {
    private ArrayList<RecordingItem> cloudList = new ArrayList<>();

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CloudFragment() {
    }

    public static CloudFragment newInstance() {
        return new CloudFragment();
    }
    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CloudFragment newInstance(int columnCount) {
        CloudFragment fragment = new CloudFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cloud_list, container, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview_cloud);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        //TODO: @Chaoping: This is only for test purpose, replace them with real data later.
        cloudList.add(new RecordingItem("Cloud 9/21/2020 1:25PM", "5 mins", true));
        cloudList.add(new RecordingItem("Cloud 9/21/2020 1:30PM", "5 mins", true));
        cloudList.add(new RecordingItem("Cloud 9/21/2020 1:35PM", "5 mins", true));
        cloudList.add(new RecordingItem("Cloud 9/21/2020 1:40PM", "5 mins", true));

        RecyclerView.Adapter mAdapter = new RecordingListAdapter(cloudList);
        recyclerView.setAdapter(mAdapter);
    }
}