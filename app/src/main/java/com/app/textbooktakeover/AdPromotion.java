package com.app.textbooktakeover;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 8/7/16.
 */
public class AdPromotion extends Fragment {
    ListView mListView;
    AdAdapter adapter;
    AVLoadingIndicatorView progress;
    ArrayList<HashMap<String,String>> adAry=new ArrayList<HashMap<String,String>>();
    LinearLayout nullLay;
    public AdPromotion(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.urgent, container , false);

        return v;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView = (ListView) getView().findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        nullLay = (LinearLayout) getView().findViewById(R.id.nullLay);
        nullLay.setVisibility(View.GONE);
        new getAdlist().execute(0);

        adapter = new AdAdapter(getActivity(),adAry);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent j = new Intent(getActivity(), PromotionDetail.class);
                j.putExtra("data",adAry.get(position));
                startActivity(j);
            }
        });


    }

    /** class for getting admin defined advertisement **/
    class getAdlist extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_MY_PROMOTIONS;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_MY_PROMOTIONS);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("type", "ad");

            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            try {
                JSONObject jobj = new JSONObject(json);
                String response = jobj.getString(Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    JSONArray result = jobj.optJSONArray(Constants.TAG_RESULT);

                    for (int i = 0; i < result.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject temp = result.getJSONObject(i);
                        String id = DefensiveClass.optString(temp, Constants.TAG_ID);
                        String name = DefensiveClass.optString(temp, Constants.TAG_PROMOTION_NAME);
                        String amount = DefensiveClass.optString(temp, Constants.TAG_PAID_AMOUNT);
                        String currencySymbol = DefensiveClass.optString(temp, Constants.TAG_CURRENCY_SYM);
                        String currencyCode = DefensiveClass.optString(temp, Constants.TAG_CURRENCY_CODE);
                        String upto = DefensiveClass.optString(temp, Constants.TAG_UPTO);
                        String transactionId = DefensiveClass.optString(temp, Constants.TAG_TRANSACTION_ID);
                        String status = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                        String itemId = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                        String itemName = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                        String itemImage = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);

                        map.put(Constants.TAG_ID, id);
                        map.put(Constants.TAG_PROMOTION_NAME, name);
                        map.put(Constants.TAG_PAID_AMOUNT, amount);
                        map.put(Constants.TAG_CURRENCY_SYM, currencySymbol);
                        map.put(Constants.TAG_CURRENCY_CODE, currencyCode);
                        map.put(Constants.TAG_UPTO, upto);
                        map.put(Constants.TAG_TRANSACTION_ID, transactionId);
                        map.put(Constants.TAG_STATUS, status);
                        map.put(Constants.TAG_ITEM_ID, itemId);
                        map.put(Constants.TAG_ITEM_NAME, itemName);
                        map.put(Constants.TAG_ITEM_IMAGE, itemImage);

                        adAry.add(map);

                    }

                }
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.v("adAry","adAry=="+adAry);
            progress.setVisibility(View.GONE);
            if(adAry.size()>0){
                adapter.notifyDataSetChanged();
                nullLay.setVisibility(View.GONE);
            }
            else{
                nullLay.setVisibility(View.VISIBLE);
            }
        }
    }

    public class AdAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        private Context mContext;
        ViewHolder holder = null;
        public AdAdapter(Context ctx,ArrayList<HashMap<String, String>> data) {
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

            TextView itemtitle,date;
            ImageView view;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.urgentlist_item, parent, false);//layout
                holder = new ViewHolder();

                holder.itemtitle = (TextView) convertView.findViewById(R.id.itemtitle);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.view = (ImageView) convertView.findViewById(R.id.lnext);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try{

                final HashMap<String, String> tempMap=Items.get(position);

                holder.itemtitle.setText(tempMap.get(Constants.TAG_ITEM_NAME));
                holder.date.setText(tempMap.get(Constants.TAG_UPTO));

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent j = new Intent(getActivity(), PromotionDetail.class);
                        j.putExtra("data",tempMap);
                        startActivity(j);
                    }
                });



            }catch(NullPointerException e){
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }
            return convertView;
        }

    }

}
