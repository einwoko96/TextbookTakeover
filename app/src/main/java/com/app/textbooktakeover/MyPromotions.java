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

import com.app.textbooktakeover.R;

/**
 * Created by hitasoft on 24/6/16.
 **/
public class MyPromotions extends FragmentActivity {

    public static TabLayout slidingTabLayout;
    ViewPager mViewPager;
    TextView title;
    int mNumFragments = 3;
    ImageView backBtn;
    ViewPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypromotion);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.my_promotions));

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setupAdapter();
    }

    public void setupAdapter() {
        CharSequence titles[] = {getString(R.string.urgent), getString(R.string.advertisement), getString(R.string.expired)};

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, mNumFragments);
        Log.v("checkadapter", "Urgent" + mAdapter);
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
                UrgentPromotion lTab = new UrgentPromotion();
                return lTab;
            } else if (position == 1) {
                AdPromotion mTab = new AdPromotion();
                return mTab;
            } else if (position == 2){
                ExpiredPromotion eTab = new ExpiredPromotion();
                return eTab;
            }else {
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
        TextbookTakeoverApplication.unregisterReceiver(MyPromotions.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(MyPromotions.this);
    }
}
