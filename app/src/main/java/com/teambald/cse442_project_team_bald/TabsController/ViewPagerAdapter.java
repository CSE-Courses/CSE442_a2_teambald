package com.teambald.cse442_project_team_bald.TabsController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.teambald.cse442_project_team_bald.Fragments.CloudFragment;
import com.teambald.cse442_project_team_bald.Fragments.HomeFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.Fragments.SettingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int TABS_SIZE = 4;


    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0: return HomeFragment.newInstance();
            case 1: return RecordingListFragment.newInstance();
            case 2: return CloudFragment.newInstance();
            case 3: return SettingFragment.newInstance();
            default: return HomeFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return TABS_SIZE;
    }
}
