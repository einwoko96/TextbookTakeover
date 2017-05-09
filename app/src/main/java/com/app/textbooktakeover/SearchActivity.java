package com.app.textbooktakeover;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.external.TimeAgo;
import com.app.utils.Constants;
import com.app.utils.SOAPParsing;
import com.etsy.android.grid.StaggeredGridView;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 22/6/16.
 */

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, AbsListView.OnScrollListener, TextWatcher, TextView.OnEditorActionListener, AdapterView.OnItemClickListener {

    ImageView backbtn, floatingBtn, resetbtn;
    StaggeredGridView gridView;
    AVLoadingIndicatorView progress;
    LinearLayout nullLay;
    EditText titleEdit;
    boolean loading=true;
    String query = "";
    public static HomeAdapter homeAdapter;
    SwipeRefreshLayout swipeLayout;
    int visibleThreshold=0, previousTotal=0, screenHalf, currentPage=0;
    public static ArrayList<HashMap<String,String>> HomeItems=new ArrayList<HashMap<String,String>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        // For Internet checking
        //TextbookTakeoverApplication.registerReceiver(SearchActivity.this);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        resetbtn = (ImageView) findViewById(R.id.resetbtn);
        gridView = (StaggeredGridView) findViewById(R.id.gridView);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        floatingBtn = (ImageView) findViewById(R.id.floatingBtn);
        titleEdit = (EditText) findViewById(R.id.titleEdit);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);

        backbtn.setVisibility(View.VISIBLE);
        titleEdit.setVisibility(View.VISIBLE);
        resetbtn.setVisibility(View.GONE);

        Display display = this.getWindowManager().getDefaultDisplay();
        int screenWidth= display.getWidth();
        int screenHeight= display.getHeight();
        screenHalf= screenWidth/2;

        backbtn.setOnClickListener(this);
        resetbtn.setOnClickListener(this);
        titleEdit.addTextChangedListener(this);
        gridView.setOnScrollListener(this);
        titleEdit.setOnEditorActionListener(this);
        gridView.setOnItemClickListener(this);
        floatingBtn.setOnClickListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.swipeColor));

        HomeItems.clear();

        new homeLoadItems().execute(0);
        homeAdapter = new HomeAdapter(SearchActivity.this, HomeItems);
        gridView.setAdapter(homeAdapter);

    }

    private void swipeRefresh(){
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    // home items //
    class homeLoadItems extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int offset = (params[0] * 20);
            String sortid = "1";

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_HOME;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_HOME);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("type", "search");
            req.addProperty("sorting_id", sortid);
            req.addProperty("offset", Integer.toString(offset));
            req.addProperty("limit", "20");
            if (GetSet.isLogged()) {
                req.addProperty("user_id", GetSet.getUserId());
            }
            if (!query.equals("")){
                req.addProperty("search_key", query);
            }
            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<HashMap<String,String>> temp=new ArrayList<HashMap<String,String>>();
                    ItemsParsing parse = new ItemsParsing(SearchActivity.this);
                    temp.addAll(parse.parsing(json));
                    if (!HomeItems.contains(temp)){
                        HomeItems.addAll(temp);
                    }
                    Log.v("HomeItems","HomeItems"+HomeItems);
                }
            });
            return null;
        }

        @Override
        protected void onPreExecute() {
            nullLay.setVisibility(View.INVISIBLE);
            if (HomeItems.size() > 0) {
                gridView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                swipeRefresh();
            }  else {
                gridView.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(Void unused) {
            gridView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeLayout.setRefreshing(false);
            homeAdapter.notifyDataSetChanged();
            if(HomeItems.size() == 0){
                nullLay.setVisibility(View.VISIBLE);
            }else{
                nullLay.setVisibility(View.INVISIBLE);
            }
        }
    }

    public class HomeAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        private Context mContext;
        ViewHolder holder = null;

        public HomeAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            Items = data;
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
            ImageView singleImage;
            TextView itemPrice, itemName, location, postedTime, productType;
            RelativeLayout imageLay;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.home_list_items,
                        parent, false);// layout
                holder = new ViewHolder();
                holder.singleImage = (ImageView) convertView.findViewById(R.id.singleImage);
                holder.itemPrice = (TextView) convertView.findViewById(R.id.priceText);
                holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
                holder.productType = (TextView) convertView.findViewById(R.id.productType);
                holder.location = (TextView) convertView.findViewById(R.id.location);
                holder.postedTime = (TextView) convertView.findViewById(R.id.postedTime);
                holder.imageLay = (RelativeLayout) convertView.findViewById(R.id.imageLay);

                holder.singleImage.getLayoutParams().height=screenHalf;
                holder.imageLay.getLayoutParams().height=screenHalf;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {

                final HashMap<String, String> tempMap = Items.get(position);

                //	holder.singleImage.setBackgroundColor(Integer.parseInt(tempMap.get(Constants.TAG_COLOR)));
                Picasso.with(SearchActivity.this).load(tempMap.get(Constants.TAG_ITEM_URL_350)).into(holder.singleImage);
                holder.itemName.setText(tempMap.get(Constants.TAG_TITLE));
                holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY_SYM) + " "
                        + tempMap.get(Constants.TAG_PRICE));
                holder.location.setText(tempMap.get(Constants.TAG_LOCATION));

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

                long timestamp = 0;
                String time = tempMap.get(Constants.TAG_POSTED_TIME);
                if(time != null){
                    timestamp = Long.parseLong(time) * 1000;
                }
                TimeAgo timeAgo = new TimeAgo(mContext);
                holder.postedTime.setText(timeAgo.timeAgo(timestamp));

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch(NumberFormatException e){
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

    }

    /** for click search icon from keyboard **/
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
            Log.i("TAG", "Enter pressed");
            try {
                query = titleEdit.getText().toString().trim();
                currentPage=0;
                previousTotal=0;
                HomeItems.clear();
                homeAdapter.notifyDataSetChanged();
                new homeLoadItems().execute(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length()>0){
            resetbtn.setVisibility(View.VISIBLE);
        } else {
            resetbtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            swipeLayout.setEnabled(false);
        }
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
                new homeLoadItems().execute(currentPage);
                loading = true;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (HomeItems.size() > 0){
            Intent i = new Intent(SearchActivity.this,
                    DetailActivity.class);
            i.putExtra("data", HomeItems.get(position));
            i.putExtra("position", position);
            i.putExtra("from", "search");
            startActivity(i);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(SearchActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(SearchActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                finish();
                break;
            case R.id.resetbtn:
                titleEdit.setText("");
                resetbtn.setVisibility(View.GONE);
                break;
            case R.id.floatingBtn:
                if (GetSet.isLogged()) {
                    Intent m = new Intent(SearchActivity.this, CameraActivity.class);
                    m.putExtra("from", "camera");
                    startActivity(m);
                } else {
                    Intent m = new Intent(SearchActivity.this, WelcomeActivity.class);
                    startActivity(m);
                }
                break;
        }
    }
}
