package com.teambald.cse442_project_team_bald.TabsController;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import android.widget.Toast;

import java.io.File;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.teambald.cse442_project_team_bald.Encryption.AudioEncryptionUtils;
import com.teambald.cse442_project_team_bald.Encryption.FileUtils;
import com.teambald.cse442_project_team_bald.Fragments.CloudFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

public class RecordingListAdapter extends RecyclerView.Adapter<RecordingListAdapter.MyViewHolder> {
    public ArrayList<RecordingItem> mDataset;
    public static final String TAG = "RecordingListAda: ";
    public MediaPlayer mediaPlayer = null;
    public boolean isPlaying;
    public View PlayingView;
    public int preint;
    public Context context;
    public CloudFragment cloudFragment;
    public RecordingListFragment fragment;
    public MainActivity activity;

    public RecordingListAdapter()
    {}

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
        final TextView date = holder.recordingItemView.findViewById(R.id.recording_date_tv);
        TextView duration = holder.recordingItemView.findViewById(R.id.recording_duration_tv);
        ImageButton button = holder.recordingItemView.findViewById(R.id.recording_play_pause_button);
        Switch locker=holder.recordingItemView.findViewById(R.id.locker);
        Button rename=holder.recordingItemView.findViewById(R.id.rename_button);
        final TextView text=holder.recordingItemView.findViewById(R.id.renaming_Text);

        date.setText(mDataset.get(position).getDate());
        duration.setText(mDataset.get(position).getDuration());
        //Renaming event
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(text.getText()!=""){
                    date.setText(text.getText());
                    String name= mDataset.get(position).getAudio_file().getName();
                    String path=mDataset.get(position).getAudio_file().getPath();
                    String subName= name.substring(name.indexOf("Recording"));
                    String SubPath=path.substring(0,path.indexOf(name));
                    mDataset.get(position).getAudio_file().renameTo(new File(SubPath+text.getText()+"_"+subName ));
                    text.setText("");
                    notifyDataSetChanged();
                    FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
                    ft.detach(fragment);
                    ft.attach(fragment);
                    ft.commit();
                }
            }
        });
        //Locker event
        locker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {// lock
                    String path= mDataset.get(position).getAudio_file().getPath();
                    if(path.charAt(path.length()-5) !='L') {
                        String name = path.substring(0, path.length() - 4);
                        mDataset.get(position).getAudio_file().renameTo(new File(name + "_L.mp4"));
                        FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
                        ft.detach(fragment);
                        ft.attach(fragment);
                        ft.commit();

                    }
                } else {
                    mDataset.get(position).unLock(); // unlock
                    String path= mDataset.get(position).getAudio_file().getPath();
                    String name= path.substring(0,path.length()-6);
                    if(path.charAt(path.length()-5)== 'L') {
                        mDataset.get(position).getAudio_file().renameTo(new File(name + ".mp4"));
                        FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
                        ft.detach(fragment);
                        ft.attach(fragment);
                        ft.commit();
                    }
                }
            }
        });
        if(!mDataset.get(position).isLocked()) {
            locker.setChecked(false); // if is locked set to true
        }else{
            locker.setChecked(true);
        }
        button.setBackgroundResource(mDataset.get(position).isPlay() ? R.drawable.ic_play_button : R.drawable.ic_pause_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        if(mDataset==null)
        {
            Log.d(TAG,"mDataSet null");
            return 0;
        }
        return mDataset.size();
    }

    public void playAudio(final RecordingItem recordingItem) {
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
    public void playAudio(final File fileToPlay,final RecordingItem recordingItem)
    {
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
    public void downloadAndPlayAudio(final RecordingItem recordingItem)
    {
        String filename = recordingItem.getDate();
        String rawPath = activity.getExternalFilesDir("/").getAbsolutePath();
        String fullPath = rawPath+File.separator+"tmp"+File.separator+filename;
        File audioFile = new File(fullPath);
        Log.d(TAG,"Downloading file first for cloud play.");
        activity.downloadFileToPlay(rawPath,"tmp",filename,activity.getmAuth().getCurrentUser().getEmail(),this,recordingItem);
        Log.d(TAG,"Downloaded file for cloud play");
    }

    public void stopAudio() {
        //Stop The Audio
        isPlaying = false;
        mediaPlayer.stop();
        try {
            PlayingView.findViewById(R.id.recording_play_pause_button).setBackgroundResource(R.drawable.ic_play_button);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void pauseAudio(RecordingItem recordingItem){
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
    public ArrayList<RecordingItem> getmDataset() {
        return mDataset;
    }

    public void setmDataset(ArrayList<RecordingItem> mDataset) {
        this.mDataset = mDataset;
    }

    /**
     * Decrypt and return the decoded bytes
     *
     * @return
     */
    public byte[] decrypt(File file) {
        String filePath = file.getPath();
        try {
            byte[] fileData = FileUtils.readFile(filePath);
            byte[] decryptedBytes = AudioEncryptionUtils.decode(AudioEncryptionUtils.getInstance(context).getSecretKey(), fileData);
            return decryptedBytes;
        } catch (Exception e) {
            Toast toast = Toast.makeText(context, "Decryption failed.", Toast.LENGTH_SHORT);
            toast.show();
        }
        return null;
    }
}
