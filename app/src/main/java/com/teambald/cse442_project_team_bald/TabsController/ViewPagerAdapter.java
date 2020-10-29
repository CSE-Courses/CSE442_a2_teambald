package com.teambald.cse442_project_team_bald.TabsController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.teambald.cse442_project_team_bald.Fragments.CloudFragment;
import com.teambald.cse442_project_team_bald.Fragments.HomeFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.Fragments.SettingFragment;
import com.teambald.cse442_project_team_bald.MainActivity;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int TABS_SIZE = 4;

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
                HomeFragment homeFragment = HomeFragment.newInstance();
                homeFragment.setActivity(mainActivity);
                return homeFragment;
            case 1:
                RecordingListFragment recordingListFragment = RecordingListFragment.newInstance();
                recordingListFragment.setActivity(mainActivity);
                return  recordingListFragment;
            case 2:
                CloudFragment cloudFragment =  CloudFragment.newInstance();
                cloudFragment.setActivity(mainActivity);
                return cloudFragment;
            case 3:
                SettingFragment settingFragment = SettingFragment.newInstance();
                settingFragment.setActivity(mainActivity);
                return settingFragment;
            default: return HomeFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return TABS_SIZE;
    }
}
