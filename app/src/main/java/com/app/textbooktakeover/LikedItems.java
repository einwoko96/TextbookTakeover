package com.app.textbooktakeover;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
import com.app.utils.SOAPParsing;
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

public class LikedItems extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static String userId = "";
    private static final String ARG_POSITION = "position";
    public static RecyclerViewAdapter itemAdapter;
    int screenWidth, screenHeight, mPosition;
    int currentPage = 0;
    TextView userName;
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;
    public static ArrayList<HashMap<String, String>> likedItems = new ArrayList<HashMap<String, String>>();
    public static Context context;
    private int previousTotal = 0;
    private boolean loading = true, pulldown=false;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    GridLayoutManager LayoutManager;
    ArrayList<String> likedId = new ArrayList<String>();
    public static boolean flag =  true;

    public static LikedItems newInstance(int position, String userrId) {
        LikedItems fragment = new LikedItems();
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
        View v = inflater.inflate(R.layout.my_listing, container, false);

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
        int width = display.getWidth();
        screenWidth = width / 2;
        screenHeight = width * 35 / 100;

        context = getActivity();
        likedItems.clear();
        new getLikedId().execute();
        loadData();
    }

    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        LayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(LayoutManager);

        itemAdapter = new RecyclerViewAdapter(likedItems);
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

            if (likedItems.size() == 0) {
                try {
                    if (TextbookTakeoverApplication.isNetworkAvailable(context)) {
                        if(flag) {
                            new likedItems().execute(0);
                            flag = false;
                        }
                    }
                    setAdapter();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                setAdapter();
                nullLay.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** class for get the list of liked items **/
    class likedItems extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int offset = (params[0] * 20);
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_HOME;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_HOME);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("type", "liked");
            req.addProperty("user_id", userId);
            req.addProperty("offset", Integer.toString(offset));
            req.addProperty("limit", "20");

            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            Log.v("likedjson", "jsonee" + json);
            if (pulldown) {
                likedItems.clear();
            }
            ArrayList<HashMap<String,String>> temp=new ArrayList<HashMap<String,String>>();
            ItemsParsing parse = new ItemsParsing("liked", context);
            temp.addAll(parse.parsing(json));
            if (!likedItems.contains(temp)){
                likedItems.addAll(temp);
            }
            Log.v("likedItems", "likedItems" + likedItems);
            return null;
        }

        @Override
        protected void onPreExecute() {
            nullLay.setVisibility(View.GONE);
            swipeRefresh();
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (pulldown) {
                pulldown = false;
                loading = true;
            }
            recyclerView.setVisibility(View.VISIBLE);
            swipeLayout.setRefreshing(false);
            itemAdapter.notifyDataSetChanged();
            if (likedItems.size() == 0) {
                nullLay.setVisibility(View.VISIBLE);
            } else {
                nullLay.setVisibility(View.GONE);
            }
            flag = true;
        }
    }

    /** class for the liked item ids **/
    class getLikedId extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_LIKED_ID;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_LIKED_ID);
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
                    likedId.clear();
                    for (int i = 0; i < result.length(); i++){
                        likedId.add(result.getString(i));
                    }
                    Log.v("likedId", "likedId="+ likedId);
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

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            ImageView singleImage;
            TextView productType;

            public MyViewHolder(View view) {
                super(view);
                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                productType = (TextView) view.findViewById(R.id.productType);

                singleImage.getLayoutParams().width = screenWidth;
                singleImage.getLayoutParams().height = screenHeight;

                singleImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.singleImage:
                        if (likedId.contains(likedItems.get(getAdapterPosition()).get(Constants.TAG_ID))) {
                            likedItems.get(getAdapterPosition()).put(Constants.TAG_LIKED, "yes");
                        } else {
                            likedItems.get(getAdapterPosition()).put(Constants.TAG_LIKED, "no");
                        }
                        Intent i = new Intent(context,
                                DetailActivity.class);
                        i.putExtra("data", likedItems.get(getAdapterPosition()));
                        i.putExtra("position", getAdapterPosition());
                        i.putExtra("from", "liked");
                        startActivity(i);
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
                    .inflate(R.layout.mylisting_list_items, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            HashMap<String, String> tempMap=Items.get(position);
            Picasso.with(context).load(tempMap.get(Constants.TAG_ITEM_URL_350)).into(holder.singleImage);
            if (tempMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")){
                holder.productType.setVisibility(View.VISIBLE);
                holder.productType.setText(getString(R.string.sold));
                holder.productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.soldbg));
            } else {
                if (Constants.PROMOTION){
                    if(tempMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Ad")) {
                        holder.productType.setVisibility(View.VISIBLE);
                        holder.productType.setText(getString(R.string.ad));
                        holder.productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.adbg));
                    } else if(tempMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Urgent")) {
                        holder.productType.setVisibility(View.VISIBLE);
                        holder.productType.setText(getString(R.string.urgent));
                        holder.productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.urgentbg));
                    } else {
                        holder.productType.setVisibility(View.GONE);
                    }
                } else {
                    holder.productType.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            new getLikedId().execute();
            new likedItems().execute(0);
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
                new likedItems().execute(currentPage);
                loading = true;
            }
        }
    };
}
