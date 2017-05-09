package com.app.textbooktakeover;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.external.FragmentChangeListener;
import com.app.textbooktakeover.R;

/**
 * Created by hitasoft on 24/6/16.
 */

public class ExchangeActivity extends FragmentActivity implements View.OnClickListener {

    public static TabLayout slidingTabLayout;
    public static ViewPager mViewPager;
    ViewPagerAdapter mAdapter;
    TextView title;
    FragmentChangeListener listener;
    int mNumFragments = 4;
    ImageView backBtn;
    public static String type = "incoming";
    public static boolean statusChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exchangelay);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);

        backBtn.setOnClickListener(this);

        title.setVisibility(View.VISIBLE);
        backBtn.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.myexchange));

        setupAdapter();
        if (type.equals("outgoing")) {
            mViewPager.setCurrentItem(1, true);
        } else if (type.equals("success")) {
            mViewPager.setCurrentItem(2, true);
        } else if (type.equals("failed")) {
            mViewPager.setCurrentItem(3, true);
        } else {
            mViewPager.setCurrentItem(0, true);
        }
    }

    /** for set viewpager & sliding tab **/
    public void setupAdapter() {
        CharSequence titles[] = {getString(R.string.incoming), getString(R.string.outgoing),
                getString(R.string.success), getString(R.string.failed)};

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, mNumFragments);
        Log.v("checkadapter", "exchng" + mAdapter);
        mViewPager.setAdapter(mAdapter);
        slidingTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                type = "incoming";
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        type = "incoming";
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

            Log.v("checkadapter", "exchng0pos");
            if (position == 0) {
                IncomeExchange lTab = new IncomeExchange();
                return lTab;
            } else if (position == 1) {
                OutgoingExchange mTab = new OutgoingExchange();
                return mTab;
            } else if (position == 2) {
                SuccessExchange lTab = new SuccessExchange();
                return lTab;
            } else if (position == 3) {
                FailedExchange lTab = new FailedExchange();
                return lTab;
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
        TextbookTakeoverApplication.unregisterReceiver(ExchangeActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (statusChanged){
            statusChanged = false;
            if (type.equals("outgoing")) {
                mViewPager.setCurrentItem(1, true);
            } else if (type.equals("success")) {
                mViewPager.setCurrentItem(2, true);
            } else if (type.equals("failed")) {
                mViewPager.setCurrentItem(3, true);
            } else {
                mViewPager.setCurrentItem(0, true);
            }
        }
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(ExchangeActivity.this);
    }
}
