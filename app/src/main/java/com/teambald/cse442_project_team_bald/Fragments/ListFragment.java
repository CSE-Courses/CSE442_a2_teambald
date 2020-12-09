package com.teambald.cse442_project_team_bald.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class ListFragment extends Fragment {
    public ArrayList<RecordingItem> itemList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public ArrayList<RecordingItem> getItems()
    {return itemList;}

}