package com.app.buynow;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.app.textbooktakeover.Profile;
import com.app.textbooktakeover.TextbookTakeoverApplication;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by hitasoft on 17/1/17.
 */

public class Review extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public static String userId = "";
    private static final String ARG_POSITION = "position";
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;
    public static RecyclerViewAdapter itemAdapter;
    int mPosition, currentPage=0;
    ArrayList<HashMap<String,String>> reviews = new ArrayList<HashMap<String,String>>();
    public static Context context;
    private int previousTotal = 0;
    private boolean loading = true, pulldown=false;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    LinearLayoutManager LayoutManager;
    public static boolean flag =  true;

    public static Review newInstance(int position, String userrId) {
        Review fragment = new Review();
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
        View v = inflater.inflate(R.layout.review, container , false);

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

        context = getActivity();

        loadData();

    }

    /** for set adapter to recycler view **/
    private void setAdapter(){
        recyclerView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(LayoutManager);

        itemAdapter = new RecyclerViewAdapter(reviews);
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
            if(reviews.size()==0){
                try {
                    if (TextbookTakeoverApplication.isNetworkAvailable(context)) {
                        if(flag) {
                            new getReviews().execute(0);
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

    /** for get reviews list of user **/
    class getReviews extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            //final String json = TextbookTakeoverApplication.loadJSONFromAsset(context, "review.json");
            int offset= (params[0]*20);
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_REVIEW;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_REVIEW);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", userId);
            req.addProperty("offset", Integer.toString(offset));
            req.addProperty("limit", "20");

            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            if (pulldown){
                reviews.clear();
            }
            ArrayList<HashMap<String,String>> temp=new ArrayList<HashMap<String,String>>();
            temp.addAll(parsing(json));
            if (!reviews.contains(temp)){
                reviews.addAll(temp);
            }
            Log.v("reviews","reviews"+reviews);
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
            if(reviews.size() == 0){
                nullLay.setVisibility(View.VISIBLE);
            }else{
                nullLay.setVisibility(View.GONE);
            }
            flag = true;
        }
    }

    private ArrayList<HashMap<String,String>> parsing (String json){
        ArrayList<HashMap<String,String>> reviews = new ArrayList<HashMap<String,String>>();
        try {
            JSONObject jobj = new JSONObject(json);
            String status = DefensiveClass.optString(jobj, Constants.TAG_STATUS);

            if (status.equalsIgnoreCase("true")){
                JSONArray result = jobj.getJSONArray(Constants.TAG_RESULT);
                for (int i = 0; i < result.length(); i++){
                    JSONObject temp = result.getJSONObject(i);
                    HashMap<String,String> map = new HashMap<String,String>();

                    String reviewId = DefensiveClass.optString(temp, Constants.TAG_REVIEW_ID);
                    String rating = DefensiveClass.optString(temp, Constants.TAG_RATING);
                    String reviewTitle = DefensiveClass.optString(temp, Constants.TAG_REVIEW_TITLE);
                    String reviewDes = DefensiveClass.optString(temp, Constants.TAG_REVIEW_DES);
                    String userId = DefensiveClass.optString(temp, Constants.TAG_USERID);
                    String fullName = DefensiveClass.optString(temp, Constants.TAG_FULLNAME);
                    String userImage = DefensiveClass.optString(temp, Constants.TAG_USERIMAGE);
                    String itemId = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                    String itemName = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                    String createdDate = DefensiveClass.optString(temp, Constants.TAG_CREATED_DATE);

                    map.put(Constants.TAG_REVIEW_ID, reviewId);
                    map.put(Constants.TAG_RATING, rating);
                    map.put(Constants.TAG_REVIEW_TITLE, reviewTitle);
                    map.put(Constants.TAG_REVIEW_DES, reviewDes);
                    map.put(Constants.TAG_USERID, userId);
                    map.put(Constants.TAG_FULLNAME, fullName);
                    map.put(Constants.TAG_USERIMAGE, userImage);
                    map.put(Constants.TAG_ITEM_ID, itemId);
                    map.put(Constants.TAG_ITEM_NAME, itemName);
                    map.put(Constants.TAG_CREATED_DATE, createdDate);

                    reviews.add(map);
                }
            }

        } catch (JSONException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return reviews;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            ImageView userImage;
            TextView userName, itemName, title, review;
            RatingBar ratingBar;

            public MyViewHolder(View view) {
                super(view);
                userImage = (ImageView) view.findViewById(R.id.userImage);
                ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
                userName = (TextView) view.findViewById(R.id.userName);
                itemName = (TextView) view.findViewById(R.id.itemName);
                title = (TextView) view.findViewById(R.id.title);
                review = (TextView) view.findViewById(R.id.review);

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
                }
            }
        }

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_list_items, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final HashMap<String, String> tempMap=Items.get(position);

            Picasso.with(context).load(tempMap.get(Constants.TAG_USERIMAGE)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);
            holder.userName.setText(tempMap.get(Constants.TAG_FULLNAME));
            holder.title.setText(tempMap.get(Constants.TAG_REVIEW_TITLE));
            holder.review.setText(tempMap.get(Constants.TAG_REVIEW_DES));

            try {
                holder.ratingBar.setRating(Float.parseFloat(tempMap.get(Constants.TAG_RATING)));
                long Date = Long.parseLong(tempMap.get(Constants.TAG_CREATED_DATE)) * 1000;
                holder.itemName.setText(tempMap.get(Constants.TAG_ITEM_NAME) + " " + getString(R.string.on) + " " + getDate(Date));
            } catch (NullPointerException e){
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

            LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable().getCurrent();
            stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

    /**
     * To convert timestamp to Date
     **/
    private String getDate(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("MMM d, yyyy", getResources().getConfiguration().locale);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            new getReviews().execute(0);
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
                new getReviews().execute(currentPage);
                loading = true;
            }
        }
    };
}
