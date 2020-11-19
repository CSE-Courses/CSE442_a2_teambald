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
        ImageButton button = holder.recordingItemView.findViewById(R.id.recording_play_pause_button);
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
        button.setBackgroundResource(mDataset.get(position).isPlay() ? R.drawable.ic_play_button : R.drawable.ic_pause_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Audio No. "+ (position + 1) + " is clicked");
                //Download first;
                //Swap the play/pause icon.
                mDataset.get(position).setPlay(!mDataset.get(position).isPlay());
                view.findViewById(R.id.recording_play_pause_button)
                        .setBackgroundResource(mDataset.get(position).isPlay() ? R.drawable.ic_play_button : R.drawable.ic_pause_button);

                //Swap the play/pause icon.
                if(!isPlaying) { // if there is no other audio playing
                    mDataset.get(position).setPlay(false); // set false in item
                    view.findViewById(R.id.recording_play_pause_button)
                            .setBackgroundResource(R.drawable.ic_pause_button); // change the background icon
                    isPlaying = true; // set playing to true
                    downloadAndPlayAudio(mDataset.get(position));
                    preint = position; // track the index
                    PlayingView = view; // track the view


                }else{ // if there exists a playing audio
                    if(PlayingView==view){  // if playing = current click
                        mDataset.get(position).setPlay(true); // set true in data
                        view.findViewById(R.id.recording_play_pause_button)
                                .setBackgroundResource( R.drawable.ic_play_button); // set the icon back to pause status
                        isPlaying=false; // set playing to false
                        preint=-1; // stop tracking index
                        PlayingView=null; // stop tracking view
                        pauseAudio( mDataset.get(position));

                    }else{ // if playing != current click
                        PlayingView.findViewById(R.id.recording_play_pause_button).setBackgroundResource(R.drawable.ic_play_button); // have the previous view change the icon to pause status
                        mDataset.get(preint).setPlay(true); // have the previous data set to true
                        mDataset.get(position).setPlay(false); // have the current data set to false
                        pauseAudio(mDataset.get(position));
                        view.findViewById(R.id.recording_play_pause_button).setBackgroundResource(R.drawable.ic_pause_button); // change the current background to play status
                        isPlaying=true;// set playing to true
                        preint=position; // track index
                        PlayingView=view; // track view
                        downloadAndPlayAudio(mDataset.get(position));
                    }
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
