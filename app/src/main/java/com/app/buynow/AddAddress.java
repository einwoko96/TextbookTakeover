package com.app.buynow;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
 *
 * This class is to add the shipping addresses.
 *
 */

public class AddAddress extends AppCompatActivity implements View.OnClickListener {

    /** Declare Layout Elements **/
    ImageView back;
    TextView title, save, country;
    EditText addressTitle, name, address, addressLine, city, state, zipcode, phone;

    /** Declare Variables **/
    String from, to, add_title, add_name, add_address, add_addresssLine, add_city, add_state, add_country,
            add_countryId, add_zipcode, add_phone;
    HashMap<String, String> datas = new HashMap<>();
    private ArrayList<String> countryId = new ArrayList<String>();
    private ArrayList<String> countryName = new ArrayList<String>();
    AVLoadingIndicatorView progress;
    LinearLayout mainLay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_address);

        /** Initializing Layout Elements **/
        back = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        save = (TextView) findViewById(R.id.save);
        addressTitle = (EditText) findViewById(R.id.addressTitle);
        name = (EditText) findViewById(R.id.name);
        address  = (EditText) findViewById(R.id.address);
        addressLine  = (EditText) findViewById(R.id.addressLine);
        city = (EditText) findViewById(R.id.city);
        state = (EditText) findViewById(R.id.state);
        country = (TextView) findViewById(R.id.country);
        zipcode = (EditText) findViewById(R.id.zipcode);
        phone  = (EditText) findViewById(R.id.phone);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        mainLay = (LinearLayout) findViewById(R.id.mainLay);

        from = getIntent().getExtras().getString("from");
        to = getIntent().getExtras().getString("to");

        back.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        /** Setting Listeners for layout elements **/
        back.setOnClickListener(this);
        save.setOnClickListener(this);
        country.setOnClickListener(this);

        TextbookTakeoverApplication.setupUI(AddAddress.this, mainLay);

        addressTitle.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(20)});
        name.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        city.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(40)});
        state.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(40)});
        zipcode.setFilters(new InputFilter[]{filterWithoutSpace, new InputFilter.LengthFilter(20)});

        new getCountry().execute();

        if(to.equals("edit")){
            title.setText(getString(R.string.edit_address));
            datas = (HashMap<String, String>) getIntent().getExtras().get("data");
            setData();
            if (from.equals("checkout")){
                country.setOnClickListener(null);
            }
        } else {
            if (from.equals("checkout")){
                country.setOnClickListener(null);
            }
            title.setText(getString(R.string.add_address));
        }

        if (TextbookTakeoverApplication.isRTL(AddAddress.this)){
            addressTitle.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            name.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            address.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            addressLine.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            city.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            state.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            zipcode.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            phone.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            country.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            addressTitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            name.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            address.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            addressLine.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            city.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            state.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            zipcode.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            phone.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            country.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }
    }

    private void setData(){
        Log.v("datas", "datas="+datas);
        addressTitle.setText(datas.get(Constants.TAG_NICKNAME));
        name.setText(datas.get(Constants.TAG_NAME));
        address.setText(datas.get(Constants.TAG_ADDRESS1));
        addressLine.setText(datas.get(Constants.TAG_ADDRESS2));
        city.setText(datas.get(Constants.TAG_CITY));
        state.setText(datas.get(Constants.TAG_STATE));
        zipcode.setText(datas.get(Constants.TAG_ZIPCODE));
        phone.setText(datas.get(Constants.TAG_PHONE));
    }

    class getCountry extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_PRODUCT_BEFORE_ADD;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_PRODUCT_BEFORE_ADD);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("lang_type", getResources().getConfiguration().locale.toString());

            SOAPParsing soap = new SOAPParsing();
            String jsonString = soap.getJSONFromUrl(SOAP_ACTION, req);

            String stats;
            try {
                JSONObject json =new JSONObject(jsonString);
                stats = json.getString(Constants.TAG_STATUS);
                if (stats.equalsIgnoreCase("true")) {
                    JSONObject res = json.getJSONObject("result");

                    JSONArray country = res.getJSONArray(Constants.TAG_COUNTRY);
                    for(int i = 0 ; i < country.length() ; i++){
                        JSONObject jobj = country.getJSONObject(i);

                        String counId = DefensiveClass.optString(jobj, Constants.TAG_COUNTRYID);
                        String counNam = DefensiveClass.optString(jobj, Constants.TAG_COUNTRYNAME);
                        /*Log.v("countryName", "countryName" + counNam);
                        Log.v("countryId", "countryId" + counId);*/
                        if (!counId.equals("0")){
                            countryId.add(counId);
                            countryName.add(counNam);
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
            mainLay.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progress.setVisibility(View.GONE);
            mainLay.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
            try {
                if (to.equals("edit")){
                    for (int i = 0; i < countryId.size(); i++){
                        if (datas.get(Constants.TAG_COUNTRYCODE).equals(countryId.get(i))){
                            add_countryId = countryId.get(i);
                            add_country = countryName.get(i);
                            country.setText(add_country);
                            break;
                        }
                    }
                } else {
                    if (from.equals("profile")){
                        if (countryName.size() > 0){
                            add_countryId = countryId.get(0);
                            add_country = countryName.get(0);
                            country.setText(add_country);
                        }
                    } else if (from.equals("checkout")){
                        HashMap<String, String> map = (HashMap<String, String>)getIntent().getExtras().get("itemData");
                        String counid = map.get(Constants.TAG_COUNTRYID);
                        for (int i = 0; i < countryId.size(); i++){
                            if (countryId.get(i).equals(counid)){
                                add_countryId = countryId.get(i);
                                add_country = countryName.get(i);
                                country.setText(add_country);
                                break;
                            }
                        }
                    }
                }
            } catch (NullPointerException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class addAddress extends AsyncTask<String,Void,String>{

        ProgressDialog dialog = new ProgressDialog(AddAddress.this);
        @Override
        protected String doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_ADD_SHIPPING;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_ADD_SHIPPING);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("nick_name", add_title);
            req.addProperty("full_name", add_name);
            req.addProperty("address1", add_address);
            req.addProperty("address2", add_addresssLine);
            req.addProperty("city", add_city);
            req.addProperty("state", add_state);
            req.addProperty("zip_code", add_zipcode);
            req.addProperty("country_name", add_country);
            req.addProperty("country_id", add_countryId);
            req.addProperty("phone_no", add_phone);

            if(to.equals("edit")){
                req.addProperty("shipping_id", datas.get(Constants.TAG_SHIPPINGID));
                req.addProperty("default", datas.get(Constants.TAG_DEFAULTSHIPPING));
            } else {
                req.addProperty("shipping_id", "0");
                req.addProperty("default", "0");
            }

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
                    HashMap<String, String> map = new HashMap<String, String>();
                    JSONObject temp = jObj.optJSONObject(Constants.TAG_RESULT);
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

                    if (from.equals("profile")){
                        finish();
                        Intent i = new Intent(AddAddress.this, Addresses.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("from", from);
                        startActivity(i);
                    } else if (from.equals("checkout")) {
                        if(to.equals("edit")){
                            finish();
                            Intent i = new Intent(AddAddress.this, Addresses.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("from", from);
                            i.putExtra("shippingId", shippingid);
                            i.putExtra("itemData", (HashMap<String, String>)getIntent().getExtras().get("itemData"));
                            startActivity(i);
                        } else {
                            finish();
                            Intent i = new Intent(AddAddress.this, Checkout.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtra("itemData", (HashMap<String, String>)getIntent().getExtras().get("itemData"));
                            i.putExtra("shippingData", map);
                            startActivity(i);
                        }
                    }
                } else {
                    Toast.makeText(AddAddress.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    InputFilter filterWithoutSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    InputFilter filterWithSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                finish();
                break;

            case R.id.save:
                add_title = addressTitle.getText().toString().trim();
                add_name = name.getText().toString().trim();
                add_address = address.getText().toString().trim();
                add_addresssLine = addressLine.getText().toString().trim();
                add_city = city.getText().toString().trim();
                add_state = state.getText().toString().trim();
                add_country = country.getText().toString().trim();
                add_zipcode = zipcode.getText().toString().trim();
                add_phone = phone.getText().toString().trim();

                if (add_title.length() == 0 || add_name.length() == 0 || add_address.length() == 0 || add_city.length() == 0
                        || add_state.length() == 0 || add_country.length() == 0 || add_phone.length() == 0) {
                    Toast.makeText(AddAddress.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                } else if (add_name.length() < 3 || add_name.length() > 30) {
                    Toast.makeText(AddAddress.this, getString(R.string.fullnameishort), Toast.LENGTH_SHORT).show();
                } else if (add_address.length() < 3) {
                    Toast.makeText(AddAddress.this, getString(R.string.addrsisshort), Toast.LENGTH_SHORT).show();
                } else if (add_city.length() < 2) {
                    Toast.makeText(AddAddress.this, getString(R.string.cityisshort), Toast.LENGTH_SHORT).show();
                } else if (add_state.length() < 2) {
                    Toast.makeText(AddAddress.this, getString(R.string.stateisshort), Toast.LENGTH_SHORT).show();
                } else {
                    new addAddress().execute();
                }
                break;

            case R.id.country:
                ArrayAdapter<String> countryadapter = new ArrayAdapter<String>(AddAddress.this,
                        android.R.layout.simple_list_item_1, countryName);
                final Dialog dialog = new Dialog(AddAddress.this);
                dialog.setContentView(R.layout.share);
                RelativeLayout main = (RelativeLayout) dialog.findViewById(R.id.main);
                final ListView lv = (ListView) dialog.findViewById(R.id.lv);
                lv.setAdapter(countryadapter);
                main.setBackgroundDrawable(null);
                dialog.setCancelable(true);
                dialog.setTitle(getString(R.string.select_country));
                dialog.show();

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        add_countryId = countryId.get(position);
                        add_country = countryName.get(position);
                        country.setText(add_country);
                        dialog.dismiss();
                    }
                });
                break;
        }
    }
}
