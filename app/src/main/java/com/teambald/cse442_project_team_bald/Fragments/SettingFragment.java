package com.teambald.cse442_project_team_bald.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.teambald.cse442_project_team_bald.R;

public class SettingFragment extends Fragment {
    private SeekBar sBar;
    private TextView recording_length;
    private int pval = 5;
    private Context context;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    public SettingFragment() { }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize SharedPref and read set recording length from SharedPreference (default: 5).
        context = getActivity();
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        pval = sharedPref.getInt(getString(R.string.recording_length_key), 5);
        Log.i("Recording_Length", String.valueOf(pval));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.setting_fragment, container, false);
    }


    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

        sBar = view.findViewById(R.id.recording_seekBar);
        //Set saved value.
        sBar.setProgress(pval);

        recording_length = view.findViewById(R.id.recording_length_tv);
        recording_length.setText(pval + (pval == 1 ? " min": " mins"));

        sBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Save changed value.
                pval = progress;
                recording_length.setText(pval + (pval == 1 ? " min": " mins"));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //write custom code to on start progress
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt(getString(R.string.recording_length_key), pval);
                editor.commit();
                int s = sharedPref.getInt(getString(R.string.recording_length_key), 5);
                Log.i("Recording_Length", String.valueOf(s));
            }
        });

    }

}
