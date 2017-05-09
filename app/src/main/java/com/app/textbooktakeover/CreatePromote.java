package com.app.textbooktakeover;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 24/6/16.
 **/
public class CreatePromote extends AppCompatActivity {

    public static TabLayout slidingTabLayout;
    ViewPager mViewPager;
    TextView title;
    int mNumFragments = 2;
    ImageView backBtn;
    ViewPagerAdapter mAdapter;
    static ArrayList<HashMap<String,String>> promoteItems = new ArrayList<>();
    static String itemId = "", currencySymbol = "", currencyCode = "", urgent = "";
    static final int REQUEST_CODE = 100;

    //static String clientToken = "eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiJjMzRmNTViZDdiZGZmMDAwMmQxZTE4YTFjNTNkMzliN2NhYjkxMjFmNzFlMjBmZTYxN2Y5MThmMDA2ZDExOGU0fGNyZWF0ZWRfYXQ9MjAxNi0wNS0zMVQxMDo0NToxNC43MDU2MjcwNjgrMDAwMFx1MDAyNm1lcmNoYW50X2lkPTM0OHBrOWNnZjNiZ3l3MmJcdTAwMjZwdWJsaWNfa2V5PTJuMjQ3ZHY4OWJxOXZtcHIiLCJjb25maWdVcmwiOiJodHRwczovL2FwaS5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tOjQ0My9tZXJjaGFudHMvMzQ4cGs5Y2dmM2JneXcyYi9jbGllbnRfYXBpL3YxL2NvbmZpZ3VyYXRpb24iLCJjaGFsbGVuZ2VzIjpbXSwiZW52aXJvbm1lbnQiOiJzYW5kYm94IiwiY2xpZW50QXBpVXJsIjoiaHR0cHM6Ly9hcGkuc2FuZGJveC5icmFpbnRyZWVnYXRld2F5LmNvbTo0NDMvbWVyY2hhbnRzLzM0OHBrOWNnZjNiZ3l3MmIvY2xpZW50X2FwaSIsImFzc2V0c1VybCI6Imh0dHBzOi8vYXNzZXRzLmJyYWludHJlZWdhdGV3YXkuY29tIiwiYXV0aFVybCI6Imh0dHBzOi8vYXV0aC52ZW5tby5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tIiwiYW5hbHl0aWNzIjp7InVybCI6Imh0dHBzOi8vY2xpZW50LWFuYWx5dGljcy5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tLzM0OHBrOWNnZjNiZ3l3MmIifSwidGhyZWVEU2VjdXJlRW5hYmxlZCI6dHJ1ZSwicGF5cGFsRW5hYmxlZCI6dHJ1ZSwicGF5cGFsIjp7ImRpc3BsYXlOYW1lIjoiQWNtZSBXaWRnZXRzLCBMdGQuIChTYW5kYm94KSIsImNsaWVudElkIjpudWxsLCJwcml2YWN5VXJsIjoiaHR0cDovL2V4YW1wbGUuY29tL3BwIiwidXNlckFncmVlbWVudFVybCI6Imh0dHA6Ly9leGFtcGxlLmNvbS90b3MiLCJiYXNlVXJsIjoiaHR0cHM6Ly9hc3NldHMuYnJhaW50cmVlZ2F0ZXdheS5jb20iLCJhc3NldHNVcmwiOiJodHRwczovL2NoZWNrb3V0LnBheXBhbC5jb20iLCJkaXJlY3RCYXNlVXJsIjpudWxsLCJhbGxvd0h0dHAiOnRydWUsImVudmlyb25tZW50Tm9OZXR3b3JrIjp0cnVlLCJlbnZpcm9ubWVudCI6Im9mZmxpbmUiLCJ1bnZldHRlZE1lcmNoYW50IjpmYWxzZSwiYnJhaW50cmVlQ2xpZW50SWQiOiJtYXN0ZXJjbGllbnQzIiwiYmlsbGluZ0FncmVlbWVudHNFbmFibGVkIjp0cnVlLCJtZXJjaGFudEFjY291bnRJZCI6ImFjbWV3aWRnZXRzbHRkc2FuZGJveCIsImN1cnJlbmN5SXNvQ29kZSI6IlVTRCJ9LCJjb2luYmFzZUVuYWJsZWQiOmZhbHNlLCJtZXJjaGFudElkIjoiMzQ4cGs5Y2dmM2JneXcyYiIsInZlbm1vIjoib2ZmIn0=";
    static String clientToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promote);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);
        promoteItems = new ArrayList<>();

        itemId = getIntent().getExtras().getString("itemId");

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.create_promotion));
        setupAdapter();

        new getClientToken().execute();
        new LoadPromotion().execute();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /** set viewpager and sliding tab **/
    public void setupAdapter() {
        CharSequence titles[] = {getString(R.string.urgent), getString(R.string.advertisement)};

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, mNumFragments);
        Log.v("checkadapter", "Urgent" + mAdapter);
        mViewPager.setAdapter(mAdapter);
        slidingTabLayout.setupWithViewPager(mViewPager);
    }

    /** class for get client token to send braintree **/
    class getClientToken extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_CLIENTTOKEN;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_CLIENTTOKEN);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);

            SOAPParsing soap = new SOAPParsing();
            final String res = soap.getJSONFromUrl(SOAP_ACTION, req);

            Log.v("response","response=="+res);
            try {
                JSONObject json = new JSONObject(res);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);

                if (response.equalsIgnoreCase("true")) {
                    clientToken = DefensiveClass.optString(json, Constants.TAG_TOKEN);
                }else{

                }
            }catch (JSONException e){
                e.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

    }

    /** class for get promotion datas form admin  **/
    class LoadPromotion extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_PROMOTION;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_PROMOTION);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);


            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            try {
                JSONObject jObj = new JSONObject(json);
                String response = DefensiveClass.optString(jObj, Constants.TAG_STATUS);
                if(response.equalsIgnoreCase("true")){
                    JSONObject result = jObj.getJSONObject(Constants.TAG_RESULT);
                    urgent = DefensiveClass.optString(result, Constants.TAG_URGENT);
                    currencySymbol = DefensiveClass.optString(result, Constants.TAG_CURRENCY_SYM).trim();
                    currencyCode = DefensiveClass.optString(result, Constants.TAG_CURRENCY_CODE);
                    JSONArray otherPromo = result.optJSONArray("other_promotions");
                    for(int i=0; i<otherPromo.length(); i++){
                        HashMap<String, String> map = new HashMap<>();

                        JSONObject promo = otherPromo.getJSONObject(i);
                        String id = DefensiveClass.optString(promo, Constants.TAG_ID);
                        String name = DefensiveClass.optString(promo, Constants.TAG_NAME);
                        String price = DefensiveClass.optString(promo, Constants.TAG_PRICE);
                        String days = DefensiveClass.optString(promo, Constants.TAG_DAYS);

                        map.put(Constants.TAG_ID, id);
                        map.put(Constants.TAG_NAME, name);
                        map.put(Constants.TAG_PRICE, price);
                        map.put(Constants.TAG_DAYS, days);

                        promoteItems.add(map);
                    }
                }

                Log.v("promoteItems", "promoteItems==" + promoteItems);
            }catch (JSONException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            if(PromoteUrgent.progress!=null)
                PromoteUrgent.progress.setVisibility(View.VISIBLE);
            if(PromoteUrgent.main!=null)
                PromoteUrgent.main.setVisibility(View.GONE);
            if(Promotead.progress!=null)
                Promotead.progress.setVisibility(View.VISIBLE);
            if(Promotead.main!=null)
                Promotead.main.setVisibility(View.GONE);
            if(PromoteUrgent.pay!=null)
                PromoteUrgent.pay.setVisibility(View.GONE);
            if(Promotead.payPromote!=null)
                Promotead.payPromote.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(Void unused) {
            if(PromoteUrgent.progress!=null)
                PromoteUrgent.progress.setVisibility(View.GONE);
            if(PromoteUrgent.main!=null)
                PromoteUrgent.main.setVisibility(View.VISIBLE);
            if(Promotead.progress!=null)
                Promotead.progress.setVisibility(View.GONE);
            if(Promotead.main!=null)
                Promotead.main.setVisibility(View.VISIBLE);
            if(PromoteUrgent.pay!=null)
                PromoteUrgent.pay.setVisibility(View.VISIBLE);
            if(Promotead.payPromote!=null)
                Promotead.payPromote.setVisibility(View.VISIBLE);
            if(!CreatePromote.urgent.equals("")){
                String price = CreatePromote.currencySymbol+String.format("%.2f",Float.parseFloat(CreatePromote.urgent));
                PromoteUrgent.adText.setText(Html.fromHtml(getString(R.string.urgent_des) + " <font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>"+ price + "</font>"));
            }
            Promotead.adapter.notifyDataSetChanged();
        }
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

            Log.v("checkadapter", "Urgentget");
            if (position == 0) {
                PromoteUrgent lTab = new PromoteUrgent();
                return lTab;
            } else if (position == 1) {
                Promotead mTab = new Promotead();
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
        TextbookTakeoverApplication.unregisterReceiver(CreatePromote.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(CreatePromote.this);
    }
}
