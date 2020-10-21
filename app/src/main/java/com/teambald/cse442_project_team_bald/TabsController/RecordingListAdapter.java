package com.teambald.cse442_project_team_bald.TabsController;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import android.widget.Toast;

import java.io.File;
import androidx.recyclerview.widget.RecyclerView;

import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordingListAdapter extends RecyclerView.Adapter<RecordingListAdapter.MyViewHolder> {
    private ArrayList<RecordingItem> mDataset;
    private static final String TAG = "RECORDING_FRAGMENT: ";
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying;
    private View PlayingView;
    private int preint;
    private Context context;



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
    public RecordingListAdapter(ArrayList<RecordingItem> myDataset, Context context) {
        mDataset = myDataset;
        this.context=context;
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
        Switch locker=holder.recordingItemView.findViewById(R.id.locker);

        date.setText(mDataset.get(position).getDate());
        duration.setText(mDataset.get(position).getDuration());
        if(mDataset.get(position).isLocked()) {
            locker.setChecked(true); // if is locked set to true
        }
        //Locker event
        locker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                   mDataset.get(position).Lock(); // lock
                } else {
                    mDataset.get(position).unLock(); // unlock
                }
            }
        });
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
                       playAudio(mDataset.get(position));
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
                        playAudio(mDataset.get(position));
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

    private void playAudio(final RecordingItem recordingItem) {
        File fileToPlay = recordingItem.getAudio_file();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.seekTo(recordingItem.getStartTimeTime());
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Play the audio
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                recordingItem.setStartTime(0);
                stopAudio();
            }
        });
    }
    private void stopAudio() {
        //Stop The Audio
        isPlaying = false;
        mediaPlayer.stop();
        try {
            PlayingView.findViewById(R.id.recording_play_pause_button).setBackgroundResource(R.drawable.ic_play_button);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void pauseAudio(RecordingItem recordingItem){
        mediaPlayer.pause();
        recordingItem.setStartTime(mediaPlayer.getCurrentPosition());
    }

    public void deleteItem(int position) {
        if(!mDataset.get(position).isLocked()) { // if item is unlocked it will be removable.
            mDataset.get(position).getAudio_file().delete();
            mDataset.remove(position);
            CharSequence text = "Recording deleted!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this.context, text, duration);
            toast.show();
        }else{
            CharSequence text = "Please unlock the recording to delete!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this.context, text, duration);
            toast.show();
        }
    }

}
