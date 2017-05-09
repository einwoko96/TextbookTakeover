package com.app.buynow;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.app.external.FragmentChangeListener;
import com.app.textbooktakeover.TextbookTakeoverApplication;
import com.app.utils.Constants;
import com.app.utils.GetSet;
import com.app.utils.OrderParsing;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.ksoap2.serialization.SoapObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by hitasoft on 14/9/16.
 **/

public class MyOrder extends Fragment implements FragmentChangeListener,AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {

    ListView mListView;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    public static OrderAdapter orderAdapter;
    SwipeRefreshLayout swipeLayout;
    public static ArrayList<HashMap<String, String>> Orderary = new ArrayList<HashMap<String, String>>();
    boolean pulldown = false, loading = true;
    int visibleThreshold=0, previousTotal=0, currentPage=0;
    public static Context context;
    public static int padding, topPadding, leftPadding;

    public MyOrder() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.exchangefragment, container, false);
        Log.v("checkadapter", "order");
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView = (ListView) getView().findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        nullLay = (LinearLayout) getView().findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeLayout);

        context = getActivity();
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.swipeColor));
        swipeLayout.setOnRefreshListener(this);

        padding = TextbookTakeoverApplication.dpToPx(getActivity(), 10);
        topPadding = TextbookTakeoverApplication.dpToPx(getActivity(), 5);
        leftPadding = TextbookTakeoverApplication.dpToPx(getActivity(), 15);
        mListView.setDivider(null);
        mListView.setDividerHeight(padding);

        Orderary.clear();
        new Getorder().execute(0);

        orderAdapter = new OrderAdapter(getActivity(),Orderary);
        mListView.setAdapter(orderAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(),OrderDetail.class);
                i.putExtra("data",Orderary.get(position));
                i.putExtra("from","order");
                i.putExtra("position",position);
                startActivity(i);
            }
        });

        mListView.setOnScrollListener(this);
    }

    @Override
    public void onCentered() {

    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    /**
     * class for get the incoming exchanges
     **/
    class Getorder extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int offset = (params[0] * 20);

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_MYORDER;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_MYORDER);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("offset", offset);
            req.addProperty("limit", "20");
            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            if (pulldown) {
                Orderary.clear();
            }
            ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
            OrderParsing parse = new OrderParsing(context);
            temp.addAll(parse.parsing(json));
            if (!Orderary.contains(temp)) {
                Orderary.addAll(temp);
                Log.v("Orderary", "Orderary==" + Orderary);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            nullLay.setVisibility(View.INVISIBLE);
            if (pulldown) {
                mListView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            } else if (Orderary.size() > 0) {
                mListView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                swipeRefresh();
            } else {
                mListView.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.setVisibility(View.GONE);
            if(pulldown){
                pulldown = false;
                loading = true;
                previousTotal = 0;
            }
            swipeLayout.setRefreshing(false);
            mListView.setVisibility(View.VISIBLE);
            orderAdapter.notifyDataSetChanged();

                if(Orderary.size()==0){
                    nullLay.setVisibility(View.VISIBLE);
                }else{
                    nullLay.setVisibility(View.INVISIBLE);
                }
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


    public class OrderAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        private Context mContext;
        ViewHolder holder = null;
        String date;
        public OrderAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
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
            ImageView myitemImage;
            TextView itemname, orderstatus, time;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.orderitem, parent, false);//layout
                holder = new ViewHolder();

                holder.myitemImage = (ImageView) convertView.findViewById(R.id.myitemImage);
                holder.itemname = (TextView) convertView.findViewById(R.id.itemname);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.orderstatus = (TextView) convertView.findViewById(R.id.orderstatus);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {

                final HashMap<String, String> tempMap = Items.get(position);

                if (tempMap.get(Constants.TAG_STATUS).equalsIgnoreCase("shipped")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.item_shipped));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.orang_round_corner));
                    } else {
                        holder.orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.orang_round_corner));
                    }
                    if(tempMap.get(Constants.TAG_SALEDATE)!=null&&!tempMap.get(Constants.TAG_SALEDATE).equals("")) {
                        long ordDate = Long.parseLong(tempMap.get(Constants.TAG_SALEDATE)) * 1000;
                        date = getDate(ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else if (tempMap.get(Constants.TAG_STATUS).equalsIgnoreCase("delivered") ||
                        tempMap.get(Constants.TAG_STATUS).equalsIgnoreCase("paid")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.item_delivered));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.blue_round_corner));
                    } else {
                        holder.orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_round_corner));
                    }
                    if(tempMap.get(Constants.TAG_SALEDATE)!=null&&!tempMap.get(Constants.TAG_SALEDATE).equals("")) {
                        long ordDate = Long.parseLong(tempMap.get(Constants.TAG_SALEDATE)) * 1000;
                        date = getDate(ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else if (tempMap.get(Constants.TAG_STATUS).equalsIgnoreCase("claimed")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.claimed));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.blue_round_corner));
                    } else {
                        holder.orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_round_corner));
                    }
                    if(tempMap.get(Constants.TAG_SALEDATE)!=null&&!tempMap.get(Constants.TAG_SALEDATE).equals("")) {
                        long ordDate = Long.parseLong(tempMap.get(Constants.TAG_SALEDATE)) * 1000;
                        date = getDate(ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else if (tempMap.get(Constants.TAG_STATUS).equalsIgnoreCase("cancelled")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.order_canceled));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.red_round_corner));
                    } else {
                        holder.orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_round_corner));
                    }
                    if(tempMap.get(Constants.TAG_SALEDATE)!=null&&!tempMap.get(Constants.TAG_SALEDATE).equals("")) {
                        long ordDate = Long.parseLong(tempMap.get(Constants.TAG_SALEDATE)) * 1000;
                        date = getDate(ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else if (tempMap.get(Constants.TAG_STATUS).equalsIgnoreCase("processing")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.under_processing));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.dark_round_corner));
                    } else {
                        holder.orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_round_corner));
                    }
                    if(tempMap.get(Constants.TAG_SALEDATE)!=null&&!tempMap.get(Constants.TAG_SALEDATE).equals("")) {
                        long ordDate = Long.parseLong(tempMap.get(Constants.TAG_SALEDATE)) * 1000;
                        date = getDate(ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else if (tempMap.get(Constants.TAG_STATUS).equalsIgnoreCase("pending")) {
                    holder.orderstatus.setVisibility(View.VISIBLE);
                    holder.orderstatus.setText(getString(R.string.pending));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        holder.orderstatus.setBackground(getResources().getDrawable(R.drawable.dark_round_corner));
                    } else {
                        holder.orderstatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_round_corner));
                    }
                    if(tempMap.get(Constants.TAG_SALEDATE)!=null&&!tempMap.get(Constants.TAG_SALEDATE).equals("")) {
                        long ordDate = Long.parseLong(tempMap.get(Constants.TAG_SALEDATE)) * 1000;
                        date = getDate(ordDate);
                    }
                    holder.time.setText(getString(R.string.order_on) + " " + date);

                } else {
                    holder.orderstatus.setVisibility(View.GONE);
                }

                holder.orderstatus.setPadding(leftPadding, topPadding, leftPadding, topPadding);
                holder.itemname.setText(tempMap.get(Constants.TAG_ITEMNAME));
                Picasso.with(getActivity()).load(tempMap.get(Constants.TAG_ORDERIMG)).into(holder.myitemImage);

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e){
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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

        if (!loading
                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // I load the next page of thumbnails using a background task,
            if(currentPage != 0){
                new Getorder().execute(currentPage);
                loading = true;
            }
        }
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            pulldown = true;
            currentPage = 0;
            if (TextbookTakeoverApplication.isNetworkAvailable(getActivity())) {
                new Getorder().execute(0);
            } else {
                swipeLayout.setRefreshing(false);
            }
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

}