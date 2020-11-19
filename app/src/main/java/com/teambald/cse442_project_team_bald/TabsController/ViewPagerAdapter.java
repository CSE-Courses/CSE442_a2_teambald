package com.teambald.cse442_project_team_bald.TabsController;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.teambald.cse442_project_team_bald.Fragments.CloudListFragment;
import com.teambald.cse442_project_team_bald.Fragments.HomeFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordSelectFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.Fragments.SettingFragment;
import com.teambald.cse442_project_team_bald.MainActivity;
import com.teambald.cse442_project_team_bald.Objects.RecordingItem;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int TABS_SIZE = 4;
    private static final String TAG = "VPAdapter";

    private MainActivity mainActivity;
    private HomeFragment homeFragment;
    private RecordSelectFragment recordSelectFragment;
    private CloudListFragment cloudListFragment;
    private SettingFragment settingFragment;

    public ViewPagerAdapter(@NonNull MainActivity fragmentActivity) {
        super(fragmentActivity);
        mainActivity = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                homeFragment = new HomeFragment(mainActivity);
                return homeFragment;
            case 1:
                recordSelectFragment = new RecordSelectFragment(mainActivity);
                return  recordSelectFragment;
            case 2:
                cloudListFragment =  new CloudListFragment(mainActivity);
                return cloudListFragment;
            case 3:
                settingFragment = new SettingFragment(mainActivity);
                return settingFragment;
            default:
                Log.d(TAG,"Invalid position in fragment creation");
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return TABS_SIZE;
    }

    public void getAllItems()
    {
        if(cloudListFragment !=null)
        {
            Log.d(TAG,"Cloud Frag !null");
            ArrayList<RecordingItem> cloudItems = cloudListFragment.getItems();
            cloudItems.get(0).setChecked(true);
            cloudListFragment.onResume();
        }
        if(recordSelectFragment!=null)
        {
            RecordingListFragment localFragment = recordSelectFragment.getRecordedFrag();
            RecordingListFragment downloadFragment = recordSelectFragment.getDownloadedFrag();
            if(localFragment!=null)
            {
                Log.d(TAG,"Local Frag !null");
                ArrayList<RecordingItem> localItems = localFragment.getItems();
            }
            if(downloadFragment!=null)
            {
                Log.d(TAG,"Downloaded Frag !null");
                ArrayList<RecordingItem> localItems = downloadFragment.getItems();
            }
        }
        Log.d(TAG,"All items read------------------------");
    }
}
