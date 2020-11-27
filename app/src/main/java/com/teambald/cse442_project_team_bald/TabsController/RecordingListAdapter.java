package com.teambald.cse442_project_team_bald.TabsController;

import android.content.Context;
import android.media.MediaPlayer;
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

import android.widget.Toast;

import java.io.File;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.teambald.cse442_project_team_bald.Encryption.AudioEncryptionUtils;
import com.teambald.cse442_project_team_bald.Encryption.FileUtils;
import com.teambald.cse442_project_team_bald.Fragments.CloudListFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;

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
    public CloudListFragment cloudListFragment;
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
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

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
