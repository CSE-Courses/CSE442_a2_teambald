package com.teambald.cse442_project_team_bald.Fragments;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.teambald.cse442_project_team_bald.Encryption.AudioEncryptionUtils;
import com.teambald.cse442_project_team_bald.Encryption.FileUtils;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.TabsController.RecordingListAdapter;
import com.teambald.cse442_project_team_bald.TabsController.SwipeActionHandler;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecordingListFragment extends Fragment {
    private ArrayList<RecordingItem> recordingList = new ArrayList<>();
    private MediaPlayer mediaPlayer = null;
    private ImageButton BackButton;
    private File[] allFiles;
    private RecyclerView.Adapter mAdapter;
    private static final String TAG = "RecordingListF";
    private String Directory_toRead;


    private MainActivity activity;

    public RecordingListFragment(String path) {
        this.Directory_toRead = path;
    }


    public static RecordingListFragment newInstance() {
        return new RecordingListFragment("");
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.recording_list_fragment, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
        RecyclerView recyclerView = view.findViewById(R.id.recording_list_recyclerview);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);




        //Read local audio files. Will be updated in onResume().
        readAllFiles(Directory_toRead);

        mAdapter = new RecordingListAdapter(recordingList,getContext(),this);
        recyclerView.setAdapter(mAdapter);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeActionHandler( (RecordingListAdapter)mAdapter,0,this));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //setup the Back Button
        this.BackButton = view.findViewById(R.id.Back_Button);
        this.BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment recordingListFG = new RecordSelectFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(((ViewGroup)getView().getParent()).getId() , recordingListFG );
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    //method use to Update the lists in external storage, need to be call on the background daily.
    public void UpToDate() throws ParseException {
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File Data= new File(recordPath);
        String[] pathnames=Data.list();
        SimpleDateFormat formatter = new SimpleDateFormat("MM_dd", Locale.US);
        Date now = new Date();
        int limite_time=7; // set time to delete to 7;
        for(String pathname:pathnames){
            //find the dates and compare with current date
            String date=pathname.substring("Recording_".length()+5,"Recording_".length()+10);
            Date save_date= formatter.parse(date);
            long diff_in_date=now.getTime()-save_date.getTime();
            long diffDays = diff_in_date / (24 * 60 * 60 * 1000); // find the different in day
            if(diffDays>limite_time){
                // do delete if difference greater than limite_time
                File file = new File(pathname);
                boolean deleted = file.delete(); // execute deletes
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Update saved audio file to make sure the recordings are up-to-date.
        readAllFiles(Directory_toRead);
    }

    /*
     * This will be called in onResume().
     */
    public void readAllFiles(String path) {
//        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
//        path = path+File.separator+"LocalRecording";//Local
        File directory = new File(path);
        allFiles = directory.listFiles();
        recordingList.clear();
        for(File f : allFiles){
            try {
                Log.i("File Path", f.getAbsolutePath());
                Uri uri = Uri.parse(f.getAbsolutePath());
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(getContext(), uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int seconds = Integer.parseInt(durationStr) / 1000;
                durationStr = parseSeconds(seconds);
                recordingList.add(new RecordingItem(f.getName(), durationStr, f.getPath(), true, f));
            }catch (Exception e){
                Log.e(TAG, ""+e);
            }
        }
        if(mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /*
     * For Encrypted files only.
     * Update the items in recordingList and notify the mAdapter to display to change.
     * This will be called in onResume().
     */
//    public void readAllEncryptedFiles() {
//        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
//        File directory = new File(path);
//        allFiles = directory.listFiles();
//        recordingList.clear();
//        for(File f : allFiles){
//            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//
//            //Decrypt audio file
//            byte[] decrypt = decrypt(f);
//            FileDescriptor decrypted;
//            try{
//                decrypted = FileUtils.getTempFileDescriptor(getContext(), decrypt);
//            }catch (IOException e){
//                Toast toast = Toast.makeText(getContext(), "Decrypt audio has failed.", Toast.LENGTH_SHORT);
//                toast.show();
//                return;
//            }
//
//            mmr.setDataSource(decrypted);
//
//            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            int seconds = Integer.parseInt(durationStr) / 1000;
//            durationStr = parseSeconds(seconds);
//            recordingList.add(new RecordingItem(f.getName(), durationStr , f.getPath(), true, f));
//        }
//        if(mAdapter != null) {
//            mAdapter.notifyDataSetChanged();
//        }
//    }

    /*
     * Take seconds and parse into the form of 00:00.
     */
    public String parseSeconds(int seconds) {
        int min = seconds / 60;
        seconds-=(min * 60);
        return (min < 10 ? "0" + min : String.valueOf(min)) + ":" + (seconds < 10 ? "0" + seconds : String.valueOf(seconds));
    }

    public void setActivity(MainActivity mainActivity)
    {
        activity = mainActivity;
    }
    public MainActivity getMainActivity()
    {return activity;}

    /**
     * Decrypt and return the decoded bytes
     *
     * @return
     */
    private byte[] decrypt(File file) {
        String filePath = file.getPath();
        try {
            byte[] fileData = FileUtils.readFile(filePath);
            byte[] decryptedBytes = AudioEncryptionUtils.decode(AudioEncryptionUtils.getInstance(getContext()).getSecretKey(), fileData);
            return decryptedBytes;
        } catch (Exception e) {
            Toast toast = Toast.makeText(getContext(), "Decryption failed.", Toast.LENGTH_SHORT);
            toast.show();
        }
        return null;
    }

    private void setpath(String path){
        this.Directory_toRead = path;
    }

}

