package com.app.textbooktakeover;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 9/7/16.
 */

public class Profile extends AppCompatActivity implements View.OnClickListener{

    private Toolbar toolbar;
    private ImageView backbtn, settingbtn, optionbtn;
    private CollapsingToolbarLayout collapsingToolbar;
    private TabPagerAdapter tabPagerAdapter;
    CoordinatorLayout main;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    AppBarLayout appbar;
    LinearLayout verificationLay;
    RelativeLayout userLay;
    public static ImageView userImg, mHeaderLogo, fbVerify, mailVerify, mobVerify;
    public static TextView userName, location, userName2, location2, followStatus;
    int headerPosition;
    public static HashMap<String, String> profileMap = new HashMap<String, String>();
    public static ArrayList<String> followingId = new ArrayList<String>();
    String userId="", userNam = "", fullName = "", userImage = "";
    Display display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main_layout);

        backbtn= (ImageView) findViewById(R.id.backbtn);
        settingbtn = (ImageView) findViewById(R.id.settingbtn);
        optionbtn = (ImageView) findViewById(R.id.optionbtn);
        userImg = (ImageView) findViewById(R.id.userImg);
        mHeaderLogo = (ImageView) findViewById(R.id.header_logo);
        userLay = (RelativeLayout) findViewById(R.id.userLay);
        userName = (TextView) findViewById(R.id.userName);
        location = (TextView) findViewById(R.id.location);
        userName2 = (TextView) findViewById(R.id.userName2);
        location2 = (TextView) findViewById(R.id.location2);
        collapsingToolbar=(CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        followStatus = (TextView) findViewById(R.id.followStatus);
        fbVerify = (ImageView) findViewById(R.id.fbverify);
        mailVerify = (ImageView) findViewById(R.id.mailverify);
        mobVerify = (ImageView) findViewById(R.id.mblverify);
        verificationLay = (LinearLayout) findViewById(R.id.verificationLay);
        main = (CoordinatorLayout) findViewById(R.id.main_content);

        setToolbar();

        userId = (String) getIntent().getExtras().get("userId");

        mViewPager= (ViewPager) findViewById(R.id.viewpager);
        mTabLayout= (TabLayout) findViewById(R.id.detail_tabs);
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabPagerAdapter);
        mTabLayout.setTabsFromPagerAdapter(tabPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        display = this.getWindowManager().getDefaultDisplay();

        followingId.clear();
        new getFollowingId().execute();

        verificationLay.setVisibility(View.INVISIBLE);

        if (userId.equals(GetSet.getUserId())){
            settingbtn.setVisibility(View.VISIBLE);
            optionbtn.setVisibility(View.VISIBLE);
            followStatus.setVisibility(View.GONE);
            if (!Constants.BUYNOW && !Constants.EXCHANGE && !Constants.PROMOTION){
                optionbtn.setVisibility(View.GONE);
            }
        } else {
            settingbtn.setVisibility(View.GONE);
            optionbtn.setVisibility(View.GONE);
            followStatus.setVisibility(View.GONE);
        }

        backbtn.setOnClickListener(this);
        optionbtn.setOnClickListener(this);
        settingbtn.setOnClickListener(this);
        followStatus.setOnClickListener(this);

        profileMap.clear();
        new getProfileInformation().execute();

        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //Log.i("offset", "offset=" + verticalOffset);
                //Log.i("dp", "dp="+TextbookTakeoverApplication.pxToDp(Profile.this, verticalOffset));
                float offset = TextbookTakeoverApplication.pxToDp(Profile.this, verticalOffset);
                if (offset > -25) {
                    //opened
                    if (headerPosition != 0 && mHeaderLogo.getVisibility() != View.VISIBLE){
                        Log.i("opened", "opened");
                        userLay.setVisibility(View.GONE);
                        userImg.setVisibility(View.GONE);
                        userName2.setVisibility(View.GONE);
                        location2.setVisibility(View.GONE);
                        mHeaderLogo.setVisibility(View.VISIBLE);
                        userLay.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkout));
                        mHeaderLogo.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkin));
                        userName2.setTextColor(getResources().getColor(R.color.white));
                        location2.setTextColor(getResources().getColor(R.color.white));
                        backbtn.setColorFilter(getResources().getColor(R.color.white));
                        optionbtn.setColorFilter(getResources().getColor(R.color.white));
                        settingbtn.setColorFilter(getResources().getColor(R.color.white));
                    }
                    headerPosition = 0;
                } else if (offset > -130){
                    //semiclosed
                    if (headerPosition != 1 && mHeaderLogo.getVisibility() != View.GONE){
                        Log.i("semiclosed", "semiclosed");
                        userLay.setVisibility(View.VISIBLE);
                        userImg.setVisibility(View.VISIBLE);
                        userName2.setVisibility(View.VISIBLE);
                        location2.setVisibility(View.VISIBLE);
                        mHeaderLogo.setVisibility(View.GONE);
                        userLay.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkin));
                        mHeaderLogo.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkout));
                        userName2.setTextColor(getResources().getColor(R.color.white));
                        location2.setTextColor(getResources().getColor(R.color.white));
                        backbtn.setColorFilter(getResources().getColor(R.color.white));
                        optionbtn.setColorFilter(getResources().getColor(R.color.white));
                        settingbtn.setColorFilter(getResources().getColor(R.color.white));
                    }
                    headerPosition = 1;
                } else {
                    //closed
                    if (headerPosition != 2){
                        Log.i("closed", "closed");
                        userLay.setVisibility(View.VISIBLE);
                        userImg.setVisibility(View.VISIBLE);
                        userName2.setVisibility(View.VISIBLE);
                        location2.setVisibility(View.VISIBLE);
                        mHeaderLogo.setVisibility(View.GONE);
                    //    userLay.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkin));
                    //    mHeaderLogo.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkout));
                        userName2.setTextColor(getResources().getColor(R.color.primaryText));
                        location2.setTextColor(getResources().getColor(R.color.secondaryText));
                        backbtn.setColorFilter(getResources().getColor(R.color.primaryText));
                        optionbtn.setColorFilter(getResources().getColor(R.color.primaryText));
                        settingbtn.setColorFilter(getResources().getColor(R.color.primaryText));
                    }
                    headerPosition = 2;
                }
               // Log.i("vH", "vH="+ (collapsingToolbar.getHeight() + verticalOffset));
               // Log.i("min", "min="+ (2 * ViewCompat.getMinimumHeight(collapsingToolbar)));
            }
        });

    }

    /** class for get the user profile information **/
    private class getProfileInformation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_PROFILE;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_PROFILE);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", userId);

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            try {
                JSONObject obj = new JSONObject(json);
                String response = DefensiveClass.optString(obj, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    JSONObject result = obj.optJSONObject("result");
                    if (!(result == null)) {
                        String userid = DefensiveClass.optString(result, "user_id");
                        String fullname = DefensiveClass.optString(result, "full_name");
                        String username = DefensiveClass.optString(result, "user_name");
                        String userimage = DefensiveClass.optString(result, "user_img");
                        String email = DefensiveClass.optString(result, "email");
                        String facebook_id = DefensiveClass.optString(result, "facebook_id");
                        String mobile_no = DefensiveClass.optString(result, "mobile_no");
                        JSONObject verification = result.optJSONObject("verification");
                        String facebook_ver = DefensiveClass.optString(verification, "facebook");
                        String email_ver = DefensiveClass.optString(verification, "email");
                        String mob_ver = DefensiveClass.optString(verification, "mob_no");

                        profileMap.put("user_id", userid);
                        profileMap.put("user_name", username);
                        profileMap.put("full_name", fullname);
                        profileMap.put("user_img", userimage);
                        profileMap.put("email", email);
                        profileMap.put("facebook_id", facebook_id);
                        profileMap.put("mobile_no", mobile_no);
                        profileMap.put("facebook_ver", facebook_ver);
                        profileMap.put("email_ver", email_ver);
                        profileMap.put("mob_ver", mob_ver);

                        Log.v("userimage", "userimage" + userimage);

                    } else {

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v("profileMap", "profileMap" + profileMap);
            if (profileMap.size() == 0) {
            //    disabledialog(getString(R.string.profile_problem));
            } else {
                setProfileInformation();
            }
        }
    }

    /** for set the user profile information **/
    private void setProfileInformation() {
        try {
            userName.setText(profileMap.get("full_name"));
            userName2.setText(profileMap.get("full_name"));
            location.setText(profileMap.get("user_name"));
            location2.setText(profileMap.get("user_name"));
            Picasso.with(Profile.this).load(profileMap.get("user_img")).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(mHeaderLogo);
            Picasso.with(Profile.this).load(profileMap.get("user_img")).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);

            if(userId.equalsIgnoreCase(GetSet.getUserId())) {
                Constants.pref = getApplicationContext().getSharedPreferences("TBTakeoverPref",
                        MODE_PRIVATE);
                Constants.editor = Constants.pref.edit();
                Constants.editor.putString("photo", profileMap.get("user_img"));
                Constants.editor.putString("userName", profileMap.get("user_name"));
                Constants.editor.putString("fullName", profileMap.get("full_name"));
                Constants.editor.commit();

                GetSet.setImageUrl(Constants.pref.getString("photo", null));
                GetSet.setUserName(Constants.pref.getString("userName", null));
                GetSet.setFullName(Constants.pref.getString("fullName", null));

                if (FragmentMainActivity.userImage != null && FragmentMainActivity.username != null) {
                    Picasso.with(Profile.this).load(GetSet.getImageUrl()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(FragmentMainActivity.userImage);
                    FragmentMainActivity.username.setText(GetSet.getFullName());
                }
            }

            verificationLay.setVisibility(View.VISIBLE);
            if (profileMap.get("facebook_ver").equals("true")){
                fbVerify.setImageResource(R.drawable.fb_veri);
            } else {
                fbVerify.setImageResource(R.drawable.fb_unveri);
            }
            if (profileMap.get("email_ver").equals("true")){
                mailVerify.setImageResource(R.drawable.mail_veri);
            } else {
                mailVerify.setImageResource(R.drawable.mail_unveri);
            }
            if (profileMap.get("mob_ver").equals("true")){
                mobVerify.setImageResource(R.drawable.mob_veri);
            } else {
                mobVerify.setImageResource(R.drawable.mob_unveri);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /** for get the following users id **/
    class getFollowingId extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_FOLLOWER_ID;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_FOLLOWER_ID);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            return json;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONObject json = new JSONObject(res);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    JSONArray result = json.optJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        followingId.add(result.getString(i));
                    }
                }

                Log.v("followingId", "followingId="+ followingId);
                if (!userId.equals(GetSet.getUserId())){
                    followStatus.setVisibility(View.VISIBLE);
                    if (followingId.contains(userId)){
                        followStatus.setText("Unfollow");
                        followStatus.setTextColor(getResources().getColor(R.color.white));
                        followStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_curve_corner));
                    } else {
                        followStatus.setText("Follow");
                        followStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                        followStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_bg_corner));
                    }
                }

            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /** function for set the toolbar as actionbar **/
    private void setToolbar() {
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    /** class for set fragments to each page **/
    class TabPagerAdapter extends FragmentStatePagerAdapter {
        private String[] TITLES;

        public TabPagerAdapter(FragmentManager fm){
            super(fm);
            if(userId.equalsIgnoreCase(GetSet.getUserId())) {
                Log.v("userlogged","userlogged");
                TITLES = new String[]{getString(R.string.my_listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings)};
            }else {
                Log.v("otheruser","otheruser");
                TITLES = new String[]{getString(R.string.listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings)};
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                MyListing fragment = MyListing.newInstance(position, userId);
                return fragment;
            } else if (position == 1) {
                LikedItems fragment = LikedItems.newInstance(position, userId);
                return fragment;
            } else if (position == 2) {
                Followers fragment = Followers.newInstance(position, userId);
                return fragment;
            } else {
                Followings fragment = Followings.newInstance(position, userId);
                return fragment;
            }
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }

    public void disabledialog(final String content){
        final Dialog dialog = new Dialog(Profile.this, R.style.AlertDialog);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(display.getWidth()*80/100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(getString(R.string.error));
        alertMsg.setText(content);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(!dialog.isShowing()){
            dialog.show();
        }
    }

    /** function for showing the popup window **/
    public void viewOptions(View v) {
        String[] values ;
        if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION){
            values = new String[]{getString(R.string.myexchange), getString(R.string.my_promotions), getString(R.string.myorders_sales),
                    getString(R.string.my_address)};
        } else if (Constants.BUYNOW && Constants.EXCHANGE){
            values = new String[]{getString(R.string.myexchange), getString(R.string.myorders_sales),
                    getString(R.string.my_address)};
        } else if (Constants.BUYNOW && Constants.PROMOTION){
            values = new String[]{getString(R.string.my_promotions), getString(R.string.myorders_sales),
                    getString(R.string.my_address)};
        } else if (Constants.EXCHANGE && Constants.PROMOTION){
            values = new String[]{getString(R.string.myexchange), getString(R.string.my_promotions)};
        } else if (Constants.EXCHANGE){
            values = new String[]{getString(R.string.myexchange)};
        } else if (Constants.PROMOTION){
            values = new String[]{getString(R.string.my_promotions)};
        } else if (Constants.BUYNOW){
            values = new String[]{getString(R.string.myorders_sales), getString(R.string.my_address)};
        } else {
            values = new String[]{};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(Profile.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(display.getWidth() * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP|Gravity.RIGHT,0,20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        if (Constants.EXCHANGE){
                            Intent i = new Intent(Profile.this, ExchangeActivity.class);
                            startActivity(i);
                        } else if (Constants.PROMOTION){
                            Intent j = new Intent(Profile.this, MyPromotions.class);
                            startActivity(j);
                        } else {
                            /*Intent k = new Intent(Profile.this, MySalesnOrder.class);
                            startActivity(k);*/
                        }
                        popup.dismiss();
                        break;
                    case 1:
                        if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION){
                            Intent j = new Intent(Profile.this, MyPromotions.class);
                            startActivity(j);
                        } else if ((Constants.BUYNOW && Constants.EXCHANGE) || (Constants.BUYNOW && Constants.PROMOTION)){
                            /*Intent k = new Intent(Profile.this, MySalesnOrder.class);
                            startActivity(k);*/
                        } else if (Constants.EXCHANGE && Constants.PROMOTION){
                            Intent j = new Intent(Profile.this, MyPromotions.class);
                            startActivity(j);
                        } else if (Constants.BUYNOW){
                            /*Intent l = new Intent(Profile.this, Addresses.class);
                            l.putExtra("from", "profile");
                            startActivity(l);*/
                        }
                        popup.dismiss();
                        break;
                    // The below cases only for buy now module, Otherwise comment it.
                    case 2:
                        /*if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION){
                            Intent k = new Intent(Profile.this, MySalesnOrder.class);
                            startActivity(k);
                        } else {
                            Intent l = new Intent(Profile.this, Addresses.class);
                            l.putExtra("from", "profile");
                            startActivity(l);
                        }*/
                        popup.dismiss();
                        break;
                    case 3:
                        /*Intent l = new Intent(Profile.this, Addresses.class);
                        l.putExtra("from", "profile");
                        startActivity(l);
                        popup.dismiss();*/
                        break;
                }
            }
        });
    }

    /** for follow the user **/
    class follow extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_FOLLOW;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_FOLLOW);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("follow_id", params[0]);
            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                followStatus.setOnClickListener(Profile.this);
                JSONObject jobj = new JSONObject(response);
                String status = DefensiveClass.optString(jobj, Constants.TAG_STATUS);
                if (status.equalsIgnoreCase("true")){
                    followStatus.setText("Unfollow");
                    followStatus.setTextColor(getResources().getColor(R.color.white));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        followStatus.setBackground(getResources().getDrawable(R.drawable.primary_curve_corner));
                    } else {
                        followStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_curve_corner));
                    }
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** for unfollow the user **/
    class unfollow extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_UNFOLLOW;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_UNFOLLOW);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("follow_id", params[0]);
            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                followStatus.setOnClickListener(Profile.this);
                JSONObject jobj = new JSONObject(response);
                String status = DefensiveClass.optString(jobj, Constants.TAG_STATUS);
                if (status.equalsIgnoreCase("true")){
                    followStatus.setText("Follow");
                    followStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                    followStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.primary_bg_corner));
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(Profile.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(Profile.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                finish();
                break;
            case R.id.optionbtn:
                viewOptions(v);
                break;
            case R.id.settingbtn:
                Intent i = new Intent(Profile.this, EditProfile.class);
                startActivity(i);
                break;
            case R.id.followStatus:
                if (GetSet.isLogged()){
                    followStatus.setOnClickListener(null);
                    if (followStatus.getText().toString().equals("Follow")){
                        new follow().execute(userId);
                    } else {
                        new unfollow().execute(userId);
                    }
                } else {
                    Intent k = new Intent(Profile.this, WelcomeActivity.class);
                    startActivity(k);
                }

                break;
        }
    }
}

