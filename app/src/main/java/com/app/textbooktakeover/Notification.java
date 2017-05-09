package com.app.textbooktakeover;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 30/6/16.
 **/

public class Notification extends AppCompatActivity implements AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {

    ListView listView;
    ImageView backbtn;
    TextView title;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    AdapterForHdpi adapter;
    int visibleThreshold=0, previousTotal=0, currentPage=0;
    boolean loading=true, pulldown=false;
    ArrayList<HashMap<String, String>> NotifyAry = new ArrayList<HashMap<String, String>>();
    SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_list);

        listView = (ListView)findViewById(R.id.listView);
        backbtn = (ImageView)findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        progress = (AVLoadingIndicatorView)findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);

        title.setText(getString(R.string.notifications));

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.swipeColor));
        listView.setOnScrollListener(this);
        swipeLayout.setOnRefreshListener(this);

        // For Set Login & Logout State
        Constants.pref = getApplicationContext().getSharedPreferences("TBTakeoverPref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        if (Constants.pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(Constants.pref.getString("userId", null));
            GetSet.setUserName(Constants.pref.getString("userName", null));
            GetSet.setEmail(Constants.pref.getString("Email", null));
            GetSet.setPassword(Constants.pref.getString("Password", null));
            GetSet.setFullName(Constants.pref.getString("fullName", null));
            GetSet.setImageUrl(Constants.pref.getString("photo", null));
        }

        new getNotification().execute(0);

        adapter = new AdapterForHdpi(Notification.this, NotifyAry);
        listView.setAdapter(adapter);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void swipeRefresh(){
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    /** get the notification for user recent activities **/
    class getNotification extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            try {
                int offset = (params[0] * 20);

                String SOAP_ACTION = Constants.NAMESPACE + Constants.API_NOTIFICATIONS;

                SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_NOTIFICATIONS);
                req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                req.addProperty("userId", GetSet.getUserId());
                req.addProperty("offset", Integer.toString(offset));
                req.addProperty("limit", "20");

                SOAPParsing soap = new SOAPParsing();
                final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
                Log.v("res", "res=" + json);
                if (pulldown){
                    NotifyAry.clear();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<HashMap<String,String>> temp=new ArrayList<HashMap<String,String>>();
                        temp.addAll(parsing(json));
                        if (!NotifyAry.contains(temp)){
                            NotifyAry.addAll(temp);
                        }
                        Log.v("NotifyAry","NotifyAry"+NotifyAry);
                    }
                });
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPreExecute() {
            nullLay.setVisibility(View.INVISIBLE);
            if (pulldown) {
                listView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            } else if (NotifyAry.size() > 0) {
                listView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                swipeRefresh();
            } else {
                listView.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(Void unused) {
            if(pulldown){
                pulldown=false;
                loading = true;
            }
            progress.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            swipeLayout.setRefreshing(false);
            adapter.notifyDataSetChanged();
            if (NotifyAry.size() == 0){
                nullLay.setVisibility(View.VISIBLE);
            } else {
                nullLay.setVisibility(View.GONE);
            }
        }
    }

    private ArrayList<HashMap<String, String>> parsing(String json) {
        ArrayList<HashMap<String, String>> NotifyAry = new ArrayList<HashMap<String, String>>();
        try {

            JSONObject jobj = new JSONObject(json);
            String response = jobj.getString(Constants.TAG_STATUS);

            if (response.equalsIgnoreCase("true")) {

                JSONArray result = jobj
                        .optJSONArray(Constants.TAG_RESULT);
                if (result != null) {
                    for (int i = 0; i < result.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject temp = result.getJSONObject(i);
                        String item_title = "", user_id = "", message = "", username = "", daytime = "", userimage = "",
                                item_id = "", itemimage = "";
                        String type = DefensiveClass.optString(temp, Constants.TAG_TYPE);
                        map.put(Constants.TAG_TYPE, type);

                        if (type.equals("add") || type.equals("myoffer") || type.equals("like") || type.equals("exchange")
                                || type.equals("comment") || type.equals("myoffer")) {
                            message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                            daytime = DefensiveClass.optString(temp, Constants.TAG_EVENTTIME);
                            userimage = DefensiveClass.optString(temp, Constants.TAG_USERIMAGE);
                            user_id = DefensiveClass.optString(temp, Constants.TAG_USERID);
                            username = DefensiveClass.optString(temp, Constants.TAG_USERNAME);
                            item_id = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                            item_title = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
                            itemimage = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);

                            map.put(Constants.TAG_MESSAGE, message);
                            map.put(Constants.TAG_EVENTTIME, daytime);
                            map.put(Constants.TAG_USERIMAGE, userimage);
                            map.put(Constants.TAG_USERID, user_id);
                            map.put(Constants.TAG_USERNAME, username);
                            map.put(Constants.TAG_ITEM_ID, item_id);
                            map.put(Constants.TAG_ITEM_TITLE, item_title);
                            map.put(Constants.TAG_ITEM_IMAGE, itemimage);
                        } else if (type.equals("admin") || type.equals("adminpayment")) {
                            message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                            daytime = DefensiveClass.optString(temp, Constants.TAG_EVENTTIME);
                            userimage = DefensiveClass.optString(temp, Constants.TAG_USERIMAGE);

                            map.put(Constants.TAG_MESSAGE, message);
                            map.put(Constants.TAG_EVENTTIME, daytime);
                            map.put(Constants.TAG_USERIMAGE, userimage);
                        } else if (type.equals("follow") || type.equals("order")) {
                            message = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                            daytime = DefensiveClass.optString(temp, Constants.TAG_EVENTTIME);
                            userimage = DefensiveClass.optString(temp, Constants.TAG_USERIMAGE);
                            user_id = DefensiveClass.optString(temp, Constants.TAG_USERID);
                            username = DefensiveClass.optString(temp, Constants.TAG_USERNAME);

                            map.put(Constants.TAG_MESSAGE, message);
                            map.put(Constants.TAG_EVENTTIME, daytime);
                            map.put(Constants.TAG_USERIMAGE, userimage);
                            map.put(Constants.TAG_USERID, user_id);
                            map.put(Constants.TAG_USERNAME, username);
                        }
                        NotifyAry.add(map);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NotifyAry;
    }

    public class AdapterForHdpi extends BaseAdapter {

        Context context;
        ViewHolder holder = null;
        private ArrayList<HashMap<String,String>> dataNotifi;
        public AdapterForHdpi(Context ctx, ArrayList<HashMap<String,String>> data) {
            context = ctx;
            dataNotifi=data;
        }
        @Override
        public int getCount() {
            return dataNotifi.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            ImageView userImage, arrow;
            TextView user_name,time;
            RelativeLayout mainLay;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) context
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.notify_listitem, parent, false);
                holder = new ViewHolder();
               
                holder.user_name = (TextView) convertView
                        .findViewById(R.id.username);
                holder.time = (TextView) convertView.findViewById(R.id.date);
                holder.userImage = (ImageView) convertView
                        .findViewById(R.id.userimg);
                holder.mainLay = (RelativeLayout) convertView
                        .findViewById(R.id.mainLay);
                holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final HashMap<String, String> tempMap = dataNotifi.get(position);
            String type = tempMap.get(Constants.TAG_TYPE);

            holder.time.setText(tempMap.get(Constants.TAG_EVENTTIME));
            Picasso.with(Notification.this).load(tempMap.get(Constants.TAG_USERIMAGE)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);

            if (type.equals("admin") || type.equals("adminpayment")){
                String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + getString(R.string.app_name) + "</font>" + " sent message '" + tempMap.get(Constants.TAG_MESSAGE) + "'";
                holder.user_name.setText(Html.fromHtml(name));
                holder.arrow.setVisibility(View.GONE);
            } else if (type.equals("follow") || type.equals("order")) {
                String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + tempMap.get(Constants.TAG_USERNAME) + "</font>" + " " + tempMap.get(Constants.TAG_MESSAGE);
                holder.user_name.setText(Html.fromHtml(name));
                holder.arrow.setVisibility(View.VISIBLE);
            } else {
                String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + tempMap.get(Constants.TAG_USERNAME) + "</font>" + " " + tempMap.get(Constants.TAG_MESSAGE)
                        + " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + tempMap.get(Constants.TAG_ITEM_TITLE) + "</font>";
                holder.user_name.setText(Html.fromHtml(name));
                holder.arrow.setVisibility(View.VISIBLE);
            }

            holder.userImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!tempMap.get(Constants.TAG_TYPE).equals("admin") && !tempMap.get(Constants.TAG_TYPE).equals("adminpayment")){
                        Intent i = new Intent(Notification.this, Profile.class);
                        i.putExtra("userId", tempMap.get(Constants.TAG_USERID));
                        startActivity(i);
                    }
                }
            });

            holder.mainLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.mainLay.setOnClickListener(null);
                    String type = tempMap.get(Constants.TAG_TYPE);
                    if (type.equals("add") || type.equals("like") || type.equals("comment")) {
                        try {
                            new homeLoadItems().execute(tempMap.get(Constants.TAG_ITEM_ID));
                            holder.mainLay.setOnClickListener(this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (type.equals("follow") || type.equals("order")){
                        Intent i = new Intent(Notification.this, Profile.class);
                        i.putExtra("userId", tempMap.get(Constants.TAG_USERID));
                        startActivity(i);
                    } else if (type.equals("myoffer")){
                        Intent i = new Intent(Notification.this, MessageActivity.class);
                        startActivity(i);
                    } else if (type.equals("exchange")) {
                        Intent i = new Intent(Notification.this, ExchangeActivity.class);
                        startActivity(i);
                    }
                }
            });
            return convertView;
        }
    }

    // home items //
    class homeLoadItems extends AsyncTask<String, Void, Void> {
        ArrayList<HashMap<String,String>> HomeItems=new ArrayList<HashMap<String,String>>();
        private ProgressDialog dialog = new ProgressDialog(Notification.this);
        @Override
        protected Void doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_SEARCH_ITEM;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_SEARCH_ITEM);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("item_id", params[0]);
            req.addProperty("user_id", GetSet.getUserId());
            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            ItemsParsing parse = new ItemsParsing(Notification.this);
            HomeItems.addAll(parse.parsing(json));
            Log.v("HomeItems","HomeItems"+HomeItems);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage(getString(R.string.pleasewait));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if(HomeItems.size() == 0){
                Toast.makeText(Notification.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            }else{
                Intent i = new Intent(Notification.this, DetailActivity.class);
                i.putExtra("data", HomeItems.get(0));
                i.putExtra("position", 0);
                i.putExtra("from", "notification");
                startActivity(i);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }

        if (!loading
                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // I load the next page of thumbnails using a background task,
            if(currentPage != 0){
                new getNotification().execute(currentPage);
                loading = true;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(Notification.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(Notification.this);
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            new getNotification().execute(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

}
