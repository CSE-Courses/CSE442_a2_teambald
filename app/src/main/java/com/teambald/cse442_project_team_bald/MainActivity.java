package com.teambald.cse442_project_team_bald;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.teambald.cse442_project_team_bald.TabsController.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
    ViewPager2 viewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(createTabAdapter());

        tabLayout = findViewById(R.id.tabs);

        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(position == 0 ? "Home" : "Recordings");
                        tab.setIcon(position == 0 ? R.drawable.ic_home_icon : R.drawable.ic_recording_list_icon);
                    }
                }).attach();
    }

    private ViewPagerAdapter createTabAdapter() {
        return new ViewPagerAdapter(this);
    }
}