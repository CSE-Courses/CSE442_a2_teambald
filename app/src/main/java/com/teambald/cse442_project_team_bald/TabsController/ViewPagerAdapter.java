package com.teambald.cse442_project_team_bald.TabsController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.teambald.cse442_project_team_bald.Fragments.HomeFragment;
import com.teambald.cse442_project_team_bald.Fragments.RecordingListFragment;
import com.teambald.cse442_project_team_bald.Fragments.SettingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int TABS_SIZE = 3;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? HomeFragment.newInstance() : position == 1 ? RecordingListFragment.newInstance() : SettingFragment.newInstance();
    }

    @Override
    public int getItemCount() {
        return TABS_SIZE;
    }
}
