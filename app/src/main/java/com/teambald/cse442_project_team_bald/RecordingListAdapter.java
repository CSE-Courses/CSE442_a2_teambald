package com.teambald.cse442_project_team_bald;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.teambald.cse442_project_team_bald.Objects.RecordingItem;

import java.util.ArrayList;

public class RecordingListAdapter extends RecyclerView.Adapter<RecordingListAdapter.MyViewHolder> {
    private ArrayList<RecordingItem> mDataset;
    private static final String TAG = "RECORDING_FRAGMENT: ";

    private boolean isPlaying;
    private View PlayingView;
    private int preint;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View recordingItemView;
        public MyViewHolder(View v) {
            super(v);
            recordingItemView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecordingListAdapter(ArrayList<RecordingItem> myDataset) {
        mDataset = myDataset;

        isPlaying=false;
        PlayingView=null;
        preint=-1;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        return new MyViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recording_list_item, parent, false)
        );
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        TextView date = holder.recordingItemView.findViewById(R.id.recording_date_tv);
        TextView duration = holder.recordingItemView.findViewById(R.id.recording_duration_tv);
        ImageButton button = holder.recordingItemView.findViewById(R.id.recording_play_pause_button);

        date.setText(mDataset.get(position).getDate());
        duration.setText(mDataset.get(position).getDuration());

        button.setBackgroundResource(mDataset.get(position).isPlay() ? R.drawable.ic_play_button : R.drawable.ic_pause_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: @Chaoping: Add listener that play/pause the audio. Make sure to pause other playing audio if there is any.
                Log.i(TAG, "Audio No. "+ (position + 1) + " is clicked");

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
                    }else{ // if playing != current click
                        PlayingView.findViewById(R.id.recording_play_pause_button).setBackgroundResource(R.drawable.ic_play_button); // have the previous view change the icon to pause status
                        mDataset.get(preint).setPlay(true); // have the previous data set to true
                        mDataset.get(position).setPlay(false); // have the current data set to false
                        view.findViewById(R.id.recording_play_pause_button).setBackgroundResource(R.drawable.ic_pause_button); // change the current background to play status
                        isPlaying=true;// set playing to true
                        preint=position; // track index
                        PlayingView=view; // track view
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
