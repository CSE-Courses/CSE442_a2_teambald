package com.teambald.cse442_project_team_bald.TabsController;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.teambald.cse442_project_team_bald.Fragments.CloudFragment;
import com.teambald.cse442_project_team_bald.Fragments.HomeFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordSelectFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.Fragments.SettingFragment;
import com.teambald.cse442_project_team_bald.MainActivity;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int TABS_SIZE = 4;
    private static final String TAG = "VPAdapter";

    private MainActivity mainActivity;

    public ViewPagerAdapter(@NonNull MainActivity fragmentActivity) {
        super(fragmentActivity);
        mainActivity = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                HomeFragment homeFragment = new HomeFragment(mainActivity);
                return homeFragment;
            case 1:
                RecordSelectFragment recordingListFragment = new RecordSelectFragment(mainActivity);
                return  recordingListFragment;
            case 2:
                CloudFragment cloudFragment =  new CloudFragment(mainActivity);
                return cloudFragment;
            case 3:
                SettingFragment settingFragment = new SettingFragment(mainActivity);
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
}
