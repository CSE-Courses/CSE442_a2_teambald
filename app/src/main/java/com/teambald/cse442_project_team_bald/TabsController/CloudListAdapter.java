package com.teambald.cse442_project_team_bald.TabsController;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;

import com.teambald.cse442_project_team_bald.Fragments.CloudListFragment;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;

import java.io.File;
import java.util.ArrayList;

public class CloudListAdapter extends RecordingListAdapter{
    private static final String TAG = "CLOUD_FRAGMENT: ";

    // Provide a suitable constructor (depends on the kind of dataset)
    public CloudListAdapter(ArrayList<RecordingItem> myDataset, Context ct, CloudListFragment frag, MainActivity ma) {
        mDataset = myDataset;
        context=ct;
        isPlaying=false;
        PlayingView=null;
        preint=-1;
        cloudListFragment =frag;
        activity = ma;
        Log.d(TAG,"Init Cloud List Adap");
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        return new MyViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cloud_list_item, parent, false)
        );
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final TextView date = holder.recordingItemView.findViewById(R.id.recording_date_tv);
        TextView duration = holder.recordingItemView.findViewById(R.id.recording_duration_tv);
//        ImageButton button = holder.recordingItemView.findViewById(R.id.recording_play_pause_button);
        CardView item = holder.recordingItemView.findViewById(R.id.audioItem);
        final CheckBox checkBox = holder.recordingItemView.findViewById(R.id.checkBox);
        checkBox.setChecked(mDataset.get(position).getChecked());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDataset.get(position).setChecked(isChecked);
                Log.d(TAG,"Cloud item:"+position+" is checked set to : "+isChecked);
            }
        });
        date.setText(mDataset.get(position).getDate());
        duration.setText(mDataset.get(position).getDuration());

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.downloadFileAndPlay(mDataset.get(position));
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
