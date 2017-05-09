package com.app.buynow;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.textbooktakeover.TextbookTakeoverApplication;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 13/9/16.
 */

public class Addresses extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    /** Declare Layout Elements **/
    ImageView back;
    TextView title, addAddress, continueAddress;
    ListView addressList;
    LinearLayout nullLay;

    /** Declare Variables **/
    AddressAdapter adapter;
    String from = "", addressId = "";
    int selectedPosition;
    ArrayList<HashMap<String, String>> addresses = new ArrayList<>();
    Display display;
    AVLoadingIndicatorView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_layout);

        /** Initializing Layout Elements **/
        back = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        addAddress = (TextView) findViewById(R.id.addAddress);
        continueAddress = (TextView) findViewById(R.id.continueAddress);
        addressList = (ListView) findViewById(R.id.addressList);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);

        /** Get Value from Intent **/
        from = getIntent().getExtras().getString("from");

        display = this.getWindowManager().getDefaultDisplay();

        back.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);

        if(from.equals("profile")) {
            title.setText(getString(R.string.my_address));
        } else {
            title.setText(getString(R.string.select_address));
            addressId = getIntent().getExtras().getString("shippingId");
        }

        /** Setting Listeners for Layout Elements **/
        back.setOnClickListener(this);
        addAddress.setOnClickListener(this);
        continueAddress.setOnClickListener(this);
     //   addressList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        addressList.setOnItemClickListener(this);

        setAdapter();
        new getAddress().execute();

    }

    private void setAdapter() {
        adapter = new AddressAdapter(Addresses.this, addresses);
        addressList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (from.equals("checkout")) {
            Log.v("ItemClicked", "OnItemClicked");
            addressId = addresses.get(position).get(Constants.TAG_SHIPPINGID);
            selectedPosition = position;
            adapter.notifyDataSetChanged();
        }
    }

    /** class for get saved addresses **/
    class getAddress extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... arg0) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_SHIPPING;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_SHIPPING);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            if(from.equals("profile")) {
                req.addProperty("item_id", "0");
            } else {
                HashMap<String, String> map = (HashMap<String, String>)getIntent().getExtras().get("itemData");
                req.addProperty("item_id", map.get(Constants.TAG_ID));
            }

            SOAPParsing soap = new SOAPParsing();
            String jobj = soap.getJSONFromUrl(SOAP_ACTION, req);

            HashMap<String, String> map;
            try {
                JSONObject json = new JSONObject(jobj);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    JSONArray result = json.optJSONArray("result");
                    if(!(result==null)){
                        for (int i = 0; i < result.length(); i++) {
                            map = new HashMap<String, String>();
                            JSONObject temp = result.getJSONObject(i);
                            String shippingid = DefensiveClass.optInt(temp, Constants.TAG_SHIPPINGID);
                            String nickname = DefensiveClass.optString(temp, Constants.TAG_NICKNAME);
                            String name = DefensiveClass.optString(temp, Constants.TAG_NAME);
                            String country = DefensiveClass.optString(temp, Constants.TAG_COUNTRY);
                            String state = DefensiveClass.optString(temp, Constants.TAG_STATE);
                            String address1 = DefensiveClass.optString(temp, Constants.TAG_ADDRESS1);
                            String address2 = DefensiveClass.optString(temp, Constants.TAG_ADDRESS2);
                            String city = DefensiveClass.optString(temp, Constants.TAG_CITY);
                            String zipcode = DefensiveClass.optString(temp, Constants.TAG_ZIPCODE);
                            String phone = DefensiveClass.optString(temp, Constants.TAG_PHONE);
                            String countrycode = DefensiveClass.optString(temp, Constants.TAG_COUNTRYCODE);
                            String defaults = DefensiveClass.optString(temp, Constants.TAG_DEFAULTSHIPPING);

                            map.put(Constants.TAG_SHIPPINGID, shippingid);
                            map.put(Constants.TAG_NICKNAME, nickname);
                            map.put(Constants.TAG_NAME, name);
                            map.put(Constants.TAG_COUNTRY, country);
                            map.put(Constants.TAG_STATE, state);
                            map.put(Constants.TAG_ADDRESS1, address1);
                            map.put(Constants.TAG_ADDRESS2, address2);
                            map.put(Constants.TAG_CITY, city);
                            map.put(Constants.TAG_ZIPCODE, zipcode);
                            map.put(Constants.TAG_PHONE, phone);
                            map.put(Constants.TAG_COUNTRYCODE, countrycode);
                            map.put(Constants.TAG_DEFAULTSHIPPING, defaults);

                            if (defaults.equals("1")){
                                addresses.add(0, map);
                            } else {
                                addresses.add(map);
                            }

                        }
                    }
                } else if (response.equalsIgnoreCase("error")){
                    TextbookTakeoverApplication.disabledialog(Addresses.this, json.optString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            addAddress.setVisibility(View.GONE);
            continueAddress.setVisibility(View.GONE);
            addressList.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void unused) {
            addAddress.setVisibility(View.VISIBLE);
            addressList.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);

            if (addresses.size() == 0){
                nullLay.setVisibility(View.VISIBLE);
            } else {
                nullLay.setVisibility(View.GONE);
            }

            if(from.equals("profile")) {
                continueAddress.setVisibility(View.GONE);
            } else {
                continueAddress.setVisibility(View.VISIBLE);
            }

            adapter.notifyDataSetChanged();
        }
    }

    /** class for get saved addresses **/
    class DefaultOrDelete extends AsyncTask<String, Void, String> {

        ProgressDialog dialog = new ProgressDialog(Addresses.this);
        String from="";
        int position;
        @Override
        protected String doInBackground(String... params) {
            from = params[0];
            position = Integer.parseInt(params[2]);
            String SOAP_ACTION = Constants.NAMESPACE + params[0];

            SoapObject req = new SoapObject(Constants.NAMESPACE, params[0]);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("shipping_id", params[1]);

            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
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
        protected void onPostExecute(String json) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            try {
                JSONObject jObj = new JSONObject(json);
                String response = DefensiveClass.optString(jObj, Constants.TAG_STATUS);
                if(response.equalsIgnoreCase("true")){
                    if (from.equals(Constants.API_DELETE_ADDRESS)){
                        addresses.remove(position);
                    } else if (from.equals(Constants.API_MARK_DEFAULT)) {
                        for (int i = 0; i < addresses.size(); i++){
                            addresses.get(i).put(Constants.TAG_DEFAULTSHIPPING, "0");
                        }
                        addresses.get(position).put(Constants.TAG_DEFAULTSHIPPING, "1");
                        HashMap<String, String> map = new HashMap<>();
                        map.putAll(addresses.get(position));
                        addresses.remove(position);
                        addresses.add(0, map);
                    }
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /** Adapter for Addresses **/
    private class AddressAdapter extends BaseAdapter{
        ArrayList<HashMap<String, String>> Items;
        private Context mContext;
        ViewHolder holder = null;
        public AddressAdapter(Context ctx,ArrayList<HashMap<String, String>> data) {
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
            TextView title, name, addressLine, city, country, defaultText, selectedView;
            ImageView options;
            RelativeLayout main;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.address_item, parent, false);//layout
                holder = new ViewHolder();

                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.addressLine = (TextView) convertView.findViewById(R.id.addressLine);
                holder.city = (TextView) convertView.findViewById(R.id.city);
                holder.country = (TextView) convertView.findViewById(R.id.country);
                holder.options = (ImageView) convertView.findViewById(R.id.options);
                holder.main = (RelativeLayout) convertView.findViewById(R.id.main);
                holder.selectedView = (TextView) convertView.findViewById(R.id.selectedView);
                holder.defaultText = (TextView) convertView.findViewById(R.id.defaultText);

                holder.selectedView.bringToFront();

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try{

                final HashMap<String, String> tempMap=Items.get(position);

                Log.v("addressId","addressId="+addressId+"&"+tempMap.get(Constants.TAG_SHIPPINGID));
                if(addressId.equals(tempMap.get(Constants.TAG_SHIPPINGID))){
                    Log.v("Viewvisible","ViewVisible");
                    holder.selectedView.setVisibility(View.VISIBLE);
                }else{
                    holder.selectedView.setVisibility(View.INVISIBLE);
                }

                if (tempMap.get(Constants.TAG_DEFAULTSHIPPING).equals("1")){
                    holder.defaultText.setVisibility(View.VISIBLE);
                } else {
                    holder.defaultText.setVisibility(View.GONE);
                }

                holder.title.setText(tempMap.get(Constants.TAG_NICKNAME));
                holder.name.setText(tempMap.get(Constants.TAG_NAME));
                holder.addressLine.setText(tempMap.get(Constants.TAG_ADDRESS1)+" , "+ tempMap.get(Constants.TAG_ADDRESS2));
                holder.city.setText(tempMap.get(Constants.TAG_CITY)+" , "+ tempMap.get(Constants.TAG_ZIPCODE));
                holder.country.setText(tempMap.get(Constants.TAG_COUNTRY));

                holder.options.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] values;
                        if (from.equals("profile")){
                            if (tempMap.get(Constants.TAG_DEFAULTSHIPPING).equals("1")){
                                values = new String[]{getString(R.string.edit)};
                            } else {
                                values = new String[]{getString(R.string.edit), getString(R.string.remove), getString(R.string.mark_default)};
                            }
                        } else {
                            values = new String[]{getString(R.string.edit), getString(R.string.remove)};
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Addresses.this,
                                R.layout.share_new, android.R.id.text1, values);
                        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = layoutInflater.inflate(R.layout.share, null);
                        layout.setAnimation(AnimationUtils.loadAnimation(Addresses.this, R.anim.grow_from_topright_to_bottomleft));
                        final PopupWindow popup = new PopupWindow(Addresses.this);
                        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        popup.setContentView(layout);
                        popup.setWidth(display.getWidth() * 50 / 100);
                        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                        popup.setFocusable(true);
                        //popup.showAtLocation(v, Gravity.TOP|Gravity.LEFT,0,v.getHeight());

                        final ListView lv = (ListView) layout.findViewById(R.id.lv);
                        lv.setAdapter(adapter);
                        popup.showAsDropDown(v, -((display.getWidth() * 45 / 100)), -60);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int pos, long id) {
                                switch (pos) {
                                    case 0:
                                        Intent i = new Intent(Addresses.this, AddAddress.class);
                                        i.putExtra("from", from);
                                        i.putExtra("to", "edit");
                                        i.putExtra("data", tempMap);
                                        if (from.equals("checkout")){
                                            i.putExtra("itemData", (HashMap<String, String>)getIntent().getExtras().get("itemData"));
                                        }
                                        startActivity(i);
                                        popup.dismiss();
                                        break;
                                    case 1:
                                        new DefaultOrDelete().execute(Constants.API_DELETE_ADDRESS, tempMap.get(Constants.TAG_SHIPPINGID), String.valueOf(position));
                                        popup.dismiss();
                                        break;
                                    case 2:
                                        new DefaultOrDelete().execute(Constants.API_MARK_DEFAULT, tempMap.get(Constants.TAG_SHIPPINGID), String.valueOf(position));
                                        popup.dismiss();
                                        break;
                                }
                            }
                        });
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

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.backbtn:
                finish();
                break;
            case R.id.addAddress:
                Intent i = new Intent(Addresses.this, AddAddress.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("from", from);
                i.putExtra("to", "add");
                if (from.equals("checkout")){
                    i.putExtra("itemData", (HashMap<String, String>)getIntent().getExtras().get("itemData"));
                }
                startActivity(i);
                break;
            case R.id.continueAddress:
                boolean addressSelected = false;
                for (int n = 0; n < addresses.size(); n++){
                    if(addressId.equals(addresses.get(n).get(Constants.TAG_SHIPPINGID))){
                        selectedPosition = n;
                        addressSelected = true;
                        break;
                    }
                }
                if (addressSelected){
                    finish();
                    Intent j = new Intent(Addresses.this, Checkout.class);
                    j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    j.putExtra("itemData", (HashMap<String, String>)getIntent().getExtras().get("itemData"));
                    j.putExtra("shippingData", addresses.get(selectedPosition));
                    startActivity(j);
                } else {
                    Toast.makeText(Addresses.this, getString(R.string.select_shipping_address), Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }
}
