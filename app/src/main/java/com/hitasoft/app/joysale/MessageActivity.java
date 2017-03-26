package com.hitasoft.app.joysale;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.ItemsParsing;
import com.hitasoft.app.utils.SOAPParsing;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Hitasoft on 21/6/16.
 */

public class MessageActivity extends AppCompatActivity implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    ImageView backbtn;
    ListView listview;
    TextView title;
    AVLoadingIndicatorView progress;
    LinearLayout nullLay;
    boolean loading=true, pulldown=false;
    public static boolean fromChat = false;
    int visibleThreshold=0, previousTotal=0;
    int currentPage = 0;
    SwipeRefreshLayout swipeLayout;
    public static HomeAdapter homeAdapter;
    public static ArrayList<HashMap<String, String>> Messagepageitems = new ArrayList<HashMap<String, String>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_layout);

        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        listview = (ListView) findViewById(R.id.listView);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);

        progress.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);
        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.messages));

        swipeLayout.setOnRefreshListener(this);
        listview.setOnScrollListener(this);
        listview.setOnItemClickListener(this);
        backbtn.setOnClickListener(this);

        listview.setSmoothScrollbarEnabled(true);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.swipeColor));

        // For Set Login & Logout State
        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
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

        loadData();
    }

    private void loadData() {
        try {

            if(Messagepageitems.size()==0){
                if (JoysaleApplication.isNetworkAvailable(MessageActivity.this)) {
                    new GetNotifications().execute(0);
                }
                homeAdapter = new HomeAdapter(MessageActivity.this, Messagepageitems);
                listview.setAdapter(homeAdapter);
            }else{
                homeAdapter = new HomeAdapter(MessageActivity.this, Messagepageitems);
                listview.setAdapter(homeAdapter);
                currentPage = 0;
                previousTotal = 0;
                pulldown = true;
                swipeRefresh();
                new GetNotifications().execute(0);
            }
        } catch(NullPointerException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void swipeRefresh(){
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                finish();
                break;
        }
    }

    /** get the messages for user recent activties **/
    class GetNotifications extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            try {
                int offset= (params[0]*20);

                String SOAP_ACTION = Constants.NAMESPACE + Constants.API_MESSAGE;

                SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_MESSAGE);
                req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                req.addProperty("user_id", GetSet.getUserId());
                req.addProperty("offset", Integer.toString(offset));
                req.addProperty("limit", "20");

                SOAPParsing soap = new SOAPParsing();
                final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
                if (pulldown){
                    Messagepageitems.clear();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Messagepageitems.addAll(parsing(json));
                        Log.v("res","res="+Messagepageitems);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("error", e.toString());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            nullLay.setVisibility(View.GONE);
            if (pulldown) {
                listview.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            } else if (Messagepageitems.size() > 0) {
                listview.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                swipeRefresh();
            } else {
                listview.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected void onPostExecute(Void unused) {
            try {
                if(pulldown){
                    pulldown=false;
                    loading = true;
                    previousTotal = 0;
                }
                listview.setVisibility(View.VISIBLE);
                swipeLayout.setRefreshing(false);
                progress.setVisibility(View.GONE);
                homeAdapter.notifyDataSetChanged();
                if (Messagepageitems.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else{
                    nullLay.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<HashMap<String,String>> parsing(String url) {
        ArrayList<HashMap<String, String>> Messagepageitems = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject json = new JSONObject(url);
            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
            if(response.equalsIgnoreCase("true")){
                JSONArray result = json.optJSONArray(Constants.TAG_RESULT);
                if(result != null){

                    for(int i=0 ; i<result.length() ; i++){
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject temp = result.getJSONObject(i);
                        String id = DefensiveClass.optInt(temp, Constants.TAG_MESSAGEID);
                        String image = DefensiveClass.optString(temp, Constants.TAG_USERIMAGE);
                        String name = DefensiveClass.optString(temp, Constants.TAG_USERNAME);
                        String fullname = DefensiveClass.optString(temp, Constants.TAG_FULLNAME);
                        String userid = DefensiveClass.optString(temp, Constants.TAG_USERID);
                        String msg = DefensiveClass.optString(temp, Constants.TAG_MESSAGE);
                        String lastreplied = DefensiveClass.optString(temp, Constants.TAG_LAST_REPLIED);
                        String msgTime = DefensiveClass.optString(temp, Constants.TAG_MESSAGETIME);

                        map.put(Constants.TAG_MESSAGEID, id);
                        map.put(Constants.TAG_USERIMAGE, image);
                        map.put(Constants.TAG_USERNAME, name);
                        map.put(Constants.TAG_FULLNAME, fullname);
                        map.put(Constants.TAG_MESSAGE, msg);
                        map.put(Constants.TAG_USERID, userid);
                        map.put(Constants.TAG_LAST_REPLIED, lastreplied);
                        map.put(Constants.TAG_MESSAGETIME, msgTime);

                        Messagepageitems.add(map);
                    }

                }
            } else if (response.equalsIgnoreCase("error")){
                JoysaleApplication.disabledialog(MessageActivity.this, json.optString("message"));
            } else{

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Messagepageitems;
    }


    public class HomeAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        private Context mContext;
        ViewHolder holder = null;

        public HomeAdapter(Context ctx,ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            Items=data;
        }

        @Override
        public int getCount() {

            return Items.size();
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
            ImageView userimg, readDot;
            TextView username;
            TextView time;
            TextView comment;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.message_item, parent, false);//layout
                holder = new ViewHolder();

                holder.userimg = (ImageView) convertView.findViewById(R.id.user_image);
                holder.username = (TextView) convertView.findViewById(R.id.user_name);
                holder.comment = (TextView) convertView.findViewById(R.id.comment);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.readDot = (ImageView) convertView.findViewById(R.id.read_dot);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try{

                final HashMap<String, String> tempMap=Items.get(position);

                Picasso.with(mContext).load(tempMap.get(Constants.TAG_USERIMAGE)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userimg);
                long timestamp = 0;
                String time = tempMap.get(Constants.TAG_MESSAGETIME);
                if(time != null){
                    timestamp = Long.parseLong(time) * 1000;
                }
                if (tempMap.get(Constants.TAG_LAST_REPLIED).equals(tempMap.get(Constants.TAG_USERID)) || tempMap.get(Constants.TAG_LAST_REPLIED).equals("0")){
                    holder.readDot.setVisibility(View.GONE);
                } else {
                    holder.readDot.setVisibility(View.VISIBLE);
                }

                holder.username.setText(tempMap.get(Constants.TAG_FULLNAME));
                holder.comment.setText(tempMap.get(Constants.TAG_MESSAGE));
                holder.time.setText(JoysaleApplication.getTimeAgo(timestamp, mContext));

            }catch(NullPointerException e){
                e.printStackTrace();
            } catch(NumberFormatException e){
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }

            return convertView;
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            swipeLayout.setEnabled(true);
        } else {
            swipeLayout.setEnabled(false);
        }

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // I load the next page of thumbnails using a background task,
            if(currentPage != 0 ){
                new GetNotifications().execute(currentPage);
                loading = true;
            }

        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            if (Messagepageitems.size() > 0 && !pulldown){
                Intent i = new Intent(MessageActivity.this, ChatActivity.class);
                i.putExtra("userName", Messagepageitems.get(arg2).get(Constants.TAG_USERNAME));
                i.putExtra("userId", Messagepageitems.get(arg2).get(Constants.TAG_USERID));
                i.putExtra("chatId", Messagepageitems.get(arg2).get(Constants.TAG_MESSAGEID));
                i.putExtra("userImage", Messagepageitems.get(arg2).get(Constants.TAG_USERIMAGE));
                i.putExtra("fullName", Messagepageitems.get(arg2).get(Constants.TAG_FULLNAME));
                i.putExtra("position", arg2);
                i.putExtra("from", "message");
                startActivity(i);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        JoysaleApplication.unregisterReceiver(MessageActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JoysaleApplication.registerReceiver(MessageActivity.this);
        if (fromChat){
            currentPage = 0;
            pulldown = true;
            fromChat = false;
            listview.smoothScrollToPosition(0);
            swipeRefresh();
            if (JoysaleApplication.isNetworkAvailable(MessageActivity.this)) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new GetNotifications().execute(0);
                    }
                }, 2000);
            } else {
                swipeLayout.setRefreshing(false);
            }
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            pulldown = true;
            if (JoysaleApplication.isNetworkAvailable(MessageActivity.this)) {
                new GetNotifications().execute(0);
            } else {
                swipeLayout.setRefreshing(false);
            }
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

}
