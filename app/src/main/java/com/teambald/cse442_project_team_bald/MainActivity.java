package com.teambald.cse442_project_team_bald;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.teambald.cse442_project_team_bald.TabsController.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter vpa;
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
                        switch(position){
                            case 0:
                                tab.setText("Home");
                                tab.setIcon(R.drawable.ic_home_icon);
                                break;
                            case 1:
                                tab.setText("Recordings");
                                tab.setIcon(R.drawable.ic_recording_list_icon);
                                break;
                            case 2:
                                tab.setText("Cloud");
                                tab.setIcon(R.drawable.ic_cloud_icon);
                                break;
                            default:
                                tab.setText("Home");
                                tab.setIcon(R.drawable.ic_home_icon);
                        }

                    }
                }).attach();
    }
    private ViewPagerAdapter createTabAdapter() {
        vpa = new ViewPagerAdapter(this);
        return vpa;
    }
}