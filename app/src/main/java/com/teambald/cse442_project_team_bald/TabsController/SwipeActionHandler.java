package com.teambald.cse442_project_team_bald.TabsController;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;
import com.teambald.cse442_project_team_bald.R;
import com.teambald.cse442_project_team_bald.Fragments.CloudFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;

import java.io.File;
import java.util.ArrayList;

public class SwipeActionHandler extends ItemTouchHelper.SimpleCallback {

    /**
     * Creates an ItemTouchHelper that will work with the given Callback.
     * <p>
     * You can attach ItemTouchHelper to a RecyclerView via
     *
     * an onItemTouchListener and a Child attach / detach listener to the RecyclerView.
     *
     * @param callback The Callback which controls the behavior of this touch helper.
     */
    private RecordingListAdapter mAdapter;

    private String readPath;
    private int fragmentId = -1;
    //0->LocalFrag 1->CloudFrag

    private static final String TAG = "SwipeActionHandler";

    private CloudFragment cloudFragment;
    private RecordingListFragment recordingListFragment;

    public SwipeActionHandler(RecordingListAdapter adapter,CloudFragment frag, String path,int fragId) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mAdapter = adapter;
        cloudFragment = frag;
        readPath = path;
        fragmentId = fragId;
    }

    public SwipeActionHandler(RecordingListAdapter adapter,RecordingListFragment frag, String path,int fragId) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mAdapter = adapter;
        recordingListFragment = frag;
        readPath = path;
        fragmentId = fragId;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        ArrayList<RecordingItem> recordingList = mAdapter.getmDataset();
        switch (fragmentId)
        {
            case 0:
                if(direction == ItemTouchHelper.LEFT)
                {
                    //Delete file
                    Log.d(TAG,"Deleting file");
                    mAdapter.deleteItem(position);
                    mAdapter.notifyDataSetChanged();
                }
                else if (direction == ItemTouchHelper.RIGHT)
                {
                    //Upload file
                    mAdapter.notifyDataSetChanged();
                    RecordingItem item = recordingList.get(position);
                    File file = item.getAudio_file();
                    Log.d(TAG,"Uploading file: "+file.getAbsolutePath());
                    String path = readPath+File.separator;

                    FirebaseUser fbuser = recordingListFragment.getMainActivity().getmAuth().getCurrentUser();
                    if(fbuser == null)
                    {
                        Toast.makeText(recordingListFragment.getContext(),"Please Log in",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String fireBaseFolder = fbuser.getEmail();

                    recordingListFragment.getMainActivity().uploadFile(path,"",file.getName(),fireBaseFolder,item.getDuration());
                }
                //
                break;
            case 1:
                if(direction == ItemTouchHelper.LEFT)
                {
                    //Download file
                    mAdapter.notifyDataSetChanged();
                    RecordingItem item = recordingList.get(position);
                    File file = item.getAudio_file();
                    Log.d(TAG,"Downloading file: "+file.getAbsolutePath());
                    String path = cloudFragment.getActivity().getExternalFilesDir("/").getAbsolutePath()+File.separator+readPath+File.separator;
                    FirebaseUser fbuser = cloudFragment.getMainActivity().getmAuth().getCurrentUser();
                    if(fbuser == null)
                    {
                        Toast.makeText(cloudFragment.getContext(),"Please Log in",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String fireBaseFolder = fbuser.getEmail();
                    cloudFragment.getMainActivity().downloadFile(path,"",file.getName(),fireBaseFolder);
                }
                else if (direction == ItemTouchHelper.RIGHT)
                {
                    //Delete file
                    Log.d(TAG,"Deleting file");
                    RecordingItem item = recordingList.get(position);
                    File file = item.getAudio_file();
                    Log.d(TAG,"Deleting cloud file: "+file.getAbsolutePath());

                    FirebaseUser fbuser = cloudFragment.getMainActivity().getmAuth().getCurrentUser();
                    if(fbuser == null)
                    {
                        Toast.makeText(cloudFragment.getContext(),"Please Log in",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String fireBaseFolder = fbuser.getEmail();
                    cloudFragment.getMainActivity().deleteFile(file.getName(),fireBaseFolder);

                    mAdapter.deleteItem(position);
                    mAdapter.notifyDataSetChanged();
                }
                //
                break;
            case -1:
                Log.d(TAG,"swipe error, fail to initialize fragment id number");
                break;
            default:
                Log.d(TAG,"swipe error, unknown case number");

        }
    }
}
