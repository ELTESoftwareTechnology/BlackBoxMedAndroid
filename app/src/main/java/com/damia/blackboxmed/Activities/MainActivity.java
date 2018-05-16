package com.damia.blackboxmed.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.damia.blackboxmed.R;
import com.damia.blackboxmed.fragments.FitFragment;
import com.damia.blackboxmed.fragments.ManualFragment;
import com.damia.blackboxmed.Helper.PagerAdapter;

public class MainActivity extends AppCompatActivity {

    FragmentPagerAdapter adapterViewPager;
    SharedPreferences session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);


        ImageButton btnSettings = findViewById(R.id.btnSettings);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.view_pager_tab);

        session = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String check = session.getString("fitPref", "");

        if(check.equals("0")){
            tabLayout.addTab(tabLayout.newTab().setText("Personal Data"));
        } else {
            tabLayout.addTab(tabLayout.newTab().setText("Personal Data"));
            tabLayout.addTab(tabLayout.newTab().setText("Google Fit"));
        }
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //go to settings
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intentSettings);
            }
        });
    }
}



