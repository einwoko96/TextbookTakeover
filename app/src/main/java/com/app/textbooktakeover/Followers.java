package com.app.textbooktakeover;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.utils.SOAPParsing;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 29/6/16.
 */

public class Followers extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static String userId = "";
    private static final String ARG_POSITION = "position";
    public static RecyclerViewAdapter itemAdapter;
    int screenWidth, screenHeight, mPosition, padding;
    int currentPage=0;
    TextView userName;
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;
    ArrayList<HashMap<String,String>> followers = new ArrayList<HashMap<String,String>>();
    public static Context context;
    private int previousTotal = 0;
    private boolean loading = true, pulldown=false;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    LinearLayoutManager LayoutManager;
    public static boolean flag =  true;

    public static Followers newInstance(int position, String userrId) {
        Followers fragment = new Followers();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        userId = userrId;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.followers, container , false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        nullLay = (LinearLayout) v.findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);

        return v;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nullLay.setVisibility(View.GONE);

        recyclerView.addOnScrollListener(onScrollListener);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.swipeColor));

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int width= display.getWidth();
        screenWidth = width / 2;
        screenHeight = width * 35/100;

        padding = TextbookTakeoverApplication.dpToPx(getActivity(), 10);
        context = getActivity();

        loadData();

    }

    /** for set adapter to recycler view **/
    private void setAdapter(){
        recyclerView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(LayoutManager);

        itemAdapter = new RecyclerViewAdapter(followers);
        recyclerView.setAdapter(itemAdapter);
    }

    private void swipeRefresh(){
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    private void loadData() {
        try {

            if(followers.size()==0){
                try {
                    if (TextbookTakeoverApplication.isNetworkAvailable(context)) {
                        if(flag) {
                            new getFollowers().execute(0);
                            flag = false;
                        }
                    }
                    setAdapter();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else{
                setAdapter();
                nullLay.setVisibility(View.GONE);
            }
        } catch(NullPointerException e){
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** for get followers list of user **/
    class getFollowers extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            //final String json = TextbookTakeoverApplication.loadJSONFromAsset(context, "followers.json");
            int offset= (params[0]*20);
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_FOLLOWERS;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_FOLLOWERS);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", userId);
            req.addProperty("offset", Integer.toString(offset));
            req.addProperty("limit", "20");

            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            Log.v("json","jsonee"+json);
            if (pulldown){
                followers.clear();
            }
            ArrayList<HashMap<String,String>> temp=new ArrayList<HashMap<String,String>>();
            temp.addAll(parsing(json));
            if (!followers.contains(temp)){
                followers.addAll(temp);
            }
            Log.v("followers","followers"+followers);
            return null;
        }

        @Override
        protected void onPreExecute() {
            nullLay.setVisibility(View.GONE);
            swipeRefresh();
        }

        @Override
        protected void onPostExecute(Void unused) {
            if(pulldown){
                pulldown=false;
                loading = true;
            }
            recyclerView.setVisibility(View.VISIBLE);
            swipeLayout.setRefreshing(false);
            itemAdapter.notifyDataSetChanged();
            if(followers.size() == 0){
                nullLay.setVisibility(View.VISIBLE);
            }else{
                nullLay.setVisibility(View.GONE);
            }
            flag = true;
        }
    }

    private ArrayList<HashMap<String,String>> parsing (String json){
        ArrayList<HashMap<String,String>> followers = new ArrayList<HashMap<String,String>>();
        try {
            JSONObject jobj = new JSONObject(json);
            String status = DefensiveClass.optString(jobj, Constants.TAG_STATUS);
            JSONArray result = jobj.getJSONArray(Constants.TAG_RESULT);

            if (status.equalsIgnoreCase("true")){
                for (int i = 0; i < result.length(); i++){
                    JSONObject temp = result.getJSONObject(i);
                    HashMap<String,String> map = new HashMap<String,String>();

                    String userId = DefensiveClass.optString(temp, Constants.TAG_USERID);
                    String userName = DefensiveClass.optString(temp, Constants.TAG_USERNAME);
                    String fullName = DefensiveClass.optString(temp, Constants.TAG_FULLNAME);
                    String followStatus = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                    String userImage = DefensiveClass.optString(temp, Constants.TAG_USERIMAGE);

                    map.put(Constants.TAG_USERID, userId);
                    map.put(Constants.TAG_USERNAME, userName);
                    map.put(Constants.TAG_FULLNAME, fullName);
                    map.put(Constants.TAG_STATUS, followStatus);
                    map.put(Constants.TAG_USERIMAGE, userImage);

                    followers.add(map);
                }
            }

        } catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return followers;
    }

    /** class for getting following userid's **/
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
                    Profile.followingId.clear();
                    for (int i = 0; i < result.length(); i++) {
                        Profile.followingId.add(result.getString(i));
                    }
                }

                Log.v("followingId", "followingId="+ Profile.followingId);
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            ImageView userImage, followStatus;
            TextView userName, location;

            public MyViewHolder(View view) {
                super(view);
                userImage = (ImageView) view.findViewById(R.id.userImage);
                followStatus = (ImageView) view.findViewById(R.id.followStatus);
                userName = (TextView) view.findViewById(R.id.userName);
                location = (TextView) view.findViewById(R.id.location);

                followStatus.setOnClickListener(this);
                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.userImage:
                        Intent u = new Intent(context, Profile.class);
                        u.putExtra("userId", Items.get(getAdapterPosition()).get(Constants.TAG_USERID));
                        startActivity(u);
                        break;
                    case R.id.followStatus:
                        if (GetSet.isLogged()){
                            String userId = Items.get(getAdapterPosition()).get(Constants.TAG_USERID);
                            if (Profile.followingId.contains(userId)){
                                new unfollow().execute(userId);
                            } else {
                                new follow().execute(userId);
                            }
                        } else {
                            Intent k = new Intent(context, WelcomeActivity.class);
                            startActivity(k);
                        }
                        break;
                }
            }
        }

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.followers_list, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final HashMap<String, String> tempMap=Items.get(position);

            Picasso.with(context).load(tempMap.get(Constants.TAG_USERIMAGE)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);
            holder.userName.setText(tempMap.get(Constants.TAG_FULLNAME));
            holder.location.setText(tempMap.get(Constants.TAG_USERNAME));

            if (tempMap.get(Constants.TAG_USERID).equals(GetSet.getUserId())){
                holder.followStatus.setVisibility(View.GONE);
            } else {
                holder.followStatus.setVisibility(View.VISIBLE);
                if (Profile.followingId.contains(tempMap.get(Constants.TAG_USERID))){
                    holder.followStatus.setImageResource(R.drawable.unfollow);
                    holder.followStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.unfollow_bg));
                } else {
                    holder.followStatus.setImageResource(R.drawable.follow);
                    holder.followStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.follow_bg));
                }
            }

            holder.followStatus.setPadding(padding,padding,padding,padding);
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

    /** for follow the user **/
    class follow extends AsyncTask<String, Void, String> {
        String userId = "";
        @Override
        protected String doInBackground(String... params) {
            userId = params[0];
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
                JSONObject jobj = new JSONObject(response);
                String status = DefensiveClass.optString(jobj, Constants.TAG_STATUS);
                if (status.equalsIgnoreCase("true")){
                    Profile.followingId.add(userId);
                    itemAdapter.notifyDataSetChanged();
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
        String userId = "";
        @Override
        protected String doInBackground(String... params) {
            userId = params[0];
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
                JSONObject jobj = new JSONObject(response);
                String status = DefensiveClass.optString(jobj, Constants.TAG_STATUS);
                if (status.equalsIgnoreCase("true")){
                    Profile.followingId.remove(userId);
                    itemAdapter.notifyDataSetChanged();
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
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            new getFollowingId().execute();
            new getFollowers().execute(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
            // code
        }

        @Override
        public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = LayoutManager.getItemCount();
            firstVisibleItem = LayoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached
                new getFollowers().execute(currentPage);
                loading = true;
            }
        }
    };

}

