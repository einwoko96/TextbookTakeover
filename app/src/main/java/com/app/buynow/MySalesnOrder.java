package com.app.buynow;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.textbooktakeover.FragmentMainActivity;
import com.app.textbooktakeover.TextbookTakeoverApplication;
import com.app.textbooktakeover.R;

/**
 * Created by hitasoft on 14/9/16.
 **/

public class MySalesnOrder extends AppCompatActivity {

    public static TabLayout slidingTabLayout;
    ViewPager mViewPager;
    TextView title;
    int mNumFragments = 2;
    ImageView backBtn;
    ViewPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salesnorder);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.myorders_sales));

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getExtras() != null){
                    FragmentMainActivity.HomeItems.clear();
                    FragmentMainActivity.currentPage = 0;
                    if (FragmentMainActivity.homeAdapter != null){
                        FragmentMainActivity.homeAdapter.notifyDataSetChanged();
                    }
                    Intent i = new Intent(MySalesnOrder.this, FragmentMainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else {
                    finish();
                }

            }
        });
        setupAdapter();
    }

    public void setupAdapter() {
        CharSequence titles[] = {getString(R.string.myorders), getString(R.string.mysales)};

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, mNumFragments);
        Log.v("checkadapter", "Orders" + mAdapter);
        mViewPager.setAdapter(mAdapter);
        slidingTabLayout.setupWithViewPager(mViewPager);
    }

    public static class ViewPagerAdapter extends FragmentStatePagerAdapter {

        CharSequence titles[];
        int numbOfTabs;

        public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int noOfTabs) {
            super(fm);
            this.titles = titles;
            this.numbOfTabs = noOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0) {
                MyOrder lTab = new MyOrder();
                return lTab;
            } else if (position == 1) {
                MySale mTab = new MySale();
                return mTab;
            } else {
                return null;
            }

        }

        @Override
        public int getCount() {
            return numbOfTabs;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(MySalesnOrder.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(MySalesnOrder.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().getExtras() != null){
            FragmentMainActivity.HomeItems.clear();
            FragmentMainActivity.currentPage = 0;
            if (FragmentMainActivity.homeAdapter != null){
                FragmentMainActivity.homeAdapter.notifyDataSetChanged();
            }
            Intent i = new Intent(MySalesnOrder.this, FragmentMainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else {
            finish();
        }
    }
}

