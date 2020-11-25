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

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.teambald.cse442_project_team_bald.Encryption.AudioEncryptionUtils;
import com.teambald.cse442_project_team_bald.Encryption.FileUtils;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LocalListAdapter extends RecordingListAdapter{
    private static final String TAG = "RECORDED_FRAGMENT: ";

    // Provide a suitable constructor (depends on the kind of dataset)
    public LocalListAdapter(ArrayList<RecordingItem> myDataset, Context ct, RecordingListFragment frag, MainActivity ma) {
        mDataset = myDataset;
        context=ct;
        isPlaying=false;
        PlayingView=null;
        preint=-1;
        fragment=frag;
        activity = ma;
        Log.d(TAG,"Init Local List Adap");
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
//        ImageButton button = holder.recordingItemView.findViewById(R.id.recording_play_pause_button);
        CardView item = holder.recordingItemView.findViewById(R.id.audioItem);
        Switch locker=holder.recordingItemView.findViewById(R.id.locker);
        Button rename=holder.recordingItemView.findViewById(R.id.rename_button);
        final TextView text=holder.recordingItemView.findViewById(R.id.renaming_Text);
        final CheckBox checkBox = holder.recordingItemView.findViewById(R.id.checkBox);
        checkBox.setChecked(mDataset.get(position).getChecked());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDataset.get(position).setChecked(isChecked);
                Log.d(TAG,"Local item:"+position+" is checked set to : "+isChecked);
            }
        });

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

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Play audio.");
                activity.playAudio(mDataset.get(position), true);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
