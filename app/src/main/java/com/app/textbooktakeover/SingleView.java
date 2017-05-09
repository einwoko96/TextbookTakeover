package com.app.textbooktakeover;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.textbooktakeover.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hitasoft on 1/7/16.
 **/

public class SingleView extends Activity {

    ImageView back;
    ViewPagerAdapter pagerAdapter;
    ViewPager viewPager;
    ArrayList<String> newary;
    TextView title;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_view);

        viewPager = (ViewPager) findViewById(R.id.pager);
        newary = (ArrayList<String>)getIntent().getExtras().get("mimages");
        back = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);

        back.setVisibility(View.VISIBLE);
        title.setText("Photos");
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SingleView.this.finish();
            }
        });

        pagerAdapter = new ViewPagerAdapter(getBaseContext(), newary);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(getIntent().getExtras().getInt("position"));
        viewPager.setPageMargin(dpToPx(1));
    }

    /** class for swiping the images **/
    class ViewPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        public int getCount() {
            return temp.size();

        }

        public Object instantiateItem(ViewGroup collection, int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.single_image,
                    collection, false);

            final ImageView image = (ImageView) itemView
                    .findViewById(R.id.imgDisplay);
            String imageloadingurl = temp.get(position);
            Log.v("position", "position"+position);

            Picasso.with(SingleView.this).load(imageloadingurl).into(image);
            ((ViewPager) collection).addView(itemView, 0);

            return itemView;

        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = SingleView.this.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(SingleView.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(SingleView.this);
    }
}
