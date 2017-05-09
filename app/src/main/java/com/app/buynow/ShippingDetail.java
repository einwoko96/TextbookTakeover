package com.app.buynow;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.textbooktakeover.TextbookTakeoverApplication;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by hitasoft on 17/9/16.
 **/

public class ShippingDetail extends AppCompatActivity implements View.OnClickListener {
    ImageView backBtn, settingbtn, titleimage;
    HashMap<String, String> data = new HashMap<>();
    TextView title, save, shippingdate;
    EditText couriername, courierservice, trackingid, otherdetails;
    String sdate, from, Trackid="", Shippingdate="", Otherdetails="", Courierservice="", Couriername="";
    int position;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    public static final String inputFormat = "dd-MM-yyyy";
    SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat);
    int mYear, mMonth, mDay, noOfTimesCalled = 0;
    Date minDate = new Date("Jan-01-01");
    long shippingTimeStamp, toTimeStamp;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.shipping_detail);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        titleimage = (ImageView) findViewById(R.id.userImg);
        settingbtn = (ImageView) findViewById(R.id.settingbtn);
        couriername = (EditText) findViewById(R.id.couriername);
        courierservice = (EditText) findViewById(R.id.shippingservice);
        trackingid = (EditText) findViewById(R.id.trackingid);
        otherdetails = (EditText) findViewById(R.id.otherdetails);
        shippingdate = (TextView) findViewById(R.id.shippingdate);
        title = (TextView) findViewById(R.id.username);
        save = (TextView) findViewById(R.id.save);

        title.setText(getString(R.string.shipping_details));

        data = (HashMap<String, String>) getIntent().getExtras().get("data");
        from = (String) getIntent().getExtras().get("from");
        position = (Integer) getIntent().getExtras().get("position");

        backBtn.setVisibility(View.VISIBLE);
        titleimage.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        save.setOnClickListener(this);
        shippingdate.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        setdata();

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    public void setdata() {

        if (from.equals("order") || ((data.get(Constants.TAG_STATUS).equalsIgnoreCase("claimed") || data.get(Constants.TAG_STATUS).equalsIgnoreCase("delivered")
                || data.get(Constants.TAG_STATUS).equalsIgnoreCase("paid")) && from.equals("sale"))) {
            couriername.setEnabled(false);
            trackingid.setEnabled(false);
            courierservice.setEnabled(false);
            otherdetails.setEnabled(false);
            save.setVisibility(View.GONE);
            shippingdate.setEnabled(false);
        }

        if (TextbookTakeoverApplication.isRTL(ShippingDetail.this)){
            shippingdate.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            couriername.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            trackingid.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            courierservice.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            otherdetails.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            shippingdate.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            couriername.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            trackingid.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            courierservice.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            otherdetails.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }

        Typeface tf;
        tf = Typeface.createFromAsset(this.getAssets(), "font_regular.ttf");
        couriername.setTypeface(tf);
        if (data.size() > 0 && !data.get(Constants.TAG_COURIER_NAME).equals("") ||
                !data.get(Constants.TAG_SHIPPING_DATE).equals("")){
            if ((data.get(Constants.TAG_STATUS).equalsIgnoreCase("shipped") ||
                    data.get(Constants.TAG_STATUS).equalsIgnoreCase("claimed")) && from.equals("enable")) {
                title.setText(getString(R.string.edit_tracking));
            }
            couriername.setText(data.get(Constants.TAG_COURIER_NAME));
            courierservice.setText(data.get(Constants.TAG_COURIER_SERVICE));
            trackingid.setText(data.get(Constants.TAG_TRACK_ID));
            otherdetails.setText(data.get(Constants.TAG_NOTES));
            long ordDate = Long.parseLong(data.get(Constants.TAG_SHIPPING_DATE)) * 1000;
            sdate = getDate(ordDate, "MMM-dd-yyyy");
            shippingdate.setText(sdate);
            Picasso.with(ShippingDetail.this).load(data.get(Constants.TAG_ORDERIMG)).into(titleimage);
        }
    }

    public static String getDate(long timeStamp, String format) {

        try {
            DateFormat sdf = new SimpleDateFormat(format);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                Shippingdate = shippingdate.getText().toString().trim();
                Couriername = couriername.getText().toString().trim();
                Courierservice = courierservice.getText().toString().trim();
                Trackid = trackingid.getText().toString().trim();
                Otherdetails = otherdetails.getText().toString().trim();
                if (Shippingdate.length() == 0 || Couriername.length() == 0 || Courierservice.length() == 0 ||
                        Trackid.length() == 0) {
                    Toast.makeText(ShippingDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                } else {
                    new Addshipping().execute();
                }
                break;
            case R.id.backbtn:
                finish();
                break;
            case R.id.shippingdate:
                Calendar mcurrentDate = Calendar.getInstance();
                mYear = mcurrentDate.get(Calendar.YEAR);
                mMonth = mcurrentDate.get(Calendar.MONTH);
                mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);
                noOfTimesCalled = 0;

                minDate.setYear(Calendar.getInstance().get(Calendar.YEAR) - 100);

                DatePickerDialog mDatePicker = new DatePickerDialog(ShippingDetail.this, new DatePickerDialog.OnDateSetListener() {


                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        Calendar newDate = Calendar.getInstance();
                        newDate.set(selectedyear, selectedmonth, selectedday);
                        if (!compareDates(dateFormatter.format(newDate.getTime()))) {

                            if (noOfTimesCalled % 2 == 0) {
                                noOfTimesCalled = 1;
                                TextbookTakeoverApplication.dialog(ShippingDetail.this, getString(R.string.alert), "Date Error");
                                shippingdate.setText("");
                            }

                        } else {
                            shippingTimeStamp = newDate.getTimeInMillis() / 1000;
                            sdate = getDate(newDate.getTimeInMillis(), "MMM-dd-yyyy");
                           // shippingdate.setText(dateFormatter.format(newDate.getTime()));
                            shippingdate.setText(sdate);
                            Log.v("shippingTimeStamp", "shippingTimeStamp==" + shippingTimeStamp);

                        }


                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Shipping Date");
                mDatePicker.show();
        }
    }

    private boolean compareDates(String compareStringOne) {
        Date date;
        Date dateCompareOne;
        Calendar now = Calendar.getInstance();

        int day = now.get(Calendar.DATE);
        int month = now.get(Calendar.MONTH) + 1;
        int year = now.get(Calendar.YEAR);

        date = parseDate(day + "-" + month + "-" + year);
        Log.e("Curentdate=", date.toString());
        dateCompareOne = parseDate(compareStringOne);

        Log.e("dateCompareOne=", dateCompareOne.toString());

        if (dateCompareOne.before(date)) {
            return false;
        } else return true;
    }

    private Date parseDate(String date) {

        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }

    class Addshipping extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_ORDER_STATUS;
            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_ORDER_STATUS);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("orderid", data.get(Constants.TAG_ORDER_ID));
            req.addProperty("chstatus", "Track");
            req.addProperty("couriername", Couriername);
            req.addProperty("courierservice", Courierservice);
            req.addProperty("trackid", Trackid);
            req.addProperty("notes", Otherdetails);
            req.addProperty("shippingdate", shippingTimeStamp);
            if (data.size() > 0 && !data.get(Constants.TAG_ID).equals("")){
                req.addProperty("id", data.get(Constants.TAG_ID));
            } else {
                req.addProperty("id", "0");
            }
            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            Log.v("ChangeStatus", "response" + json);

            return json;
        }

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (from.equals("sale") && data.get(Constants.TAG_STATUS).equalsIgnoreCase("processing")){
                    new getTrack().execute().get();
                }
                JSONObject json = new JSONObject(res);
                String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (status.equalsIgnoreCase("true")) {
                    Toast.makeText(ShippingDetail.this, getString(R.string.tracking_details_updated), Toast.LENGTH_SHORT).show();
                    if (from.equals("sale") && MySale.Saleary.size() > 0){
                        data.put(Constants.TAG_SHIPPING_DATE, String.valueOf(shippingTimeStamp));
                        data.put(Constants.TAG_COURIER_NAME, Couriername);
                        data.put(Constants.TAG_COURIER_SERVICE, Courierservice);
                        data.put(Constants.TAG_TRACK_ID, Trackid);
                        data.put(Constants.TAG_NOTES, Otherdetails);
                        MySale.Saleary.get(position).put(Constants.TAG_ID, data.get(Constants.TAG_ID));
                        MySale.Saleary.get(position).put(Constants.TAG_SHIPPING_DATE, String.valueOf(shippingTimeStamp));
                        MySale.Saleary.get(position).put(Constants.TAG_COURIER_NAME, Couriername);
                        MySale.Saleary.get(position).put(Constants.TAG_COURIER_SERVICE, Courierservice);
                        MySale.Saleary.get(position).put(Constants.TAG_TRACK_ID, Trackid);
                        MySale.Saleary.get(position).put(Constants.TAG_NOTES, Otherdetails);

                        data.put(Constants.TAG_STATUS, "shipped");
                        MySale.Saleary.get(position).put(Constants.TAG_STATUS, "shipped");
                        MySale.saleAdapter.notifyDataSetChanged();
                    }

                    finish();
                    Intent i = new Intent(ShippingDetail.this, OrderDetail.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("data", data);
                    i.putExtra("from","sale");
                    i.putExtra("position",position);
                    startActivity(i);
                } else {
                    String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);
                    Toast.makeText(ShippingDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    class getTrack extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_TRACK_DETAILS;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_TRACK_DETAILS);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("order_id", data.get(Constants.TAG_ORDER_ID));

            SOAPParsing soap = new SOAPParsing();
            String result = soap.getJSONFromUrl(SOAP_ACTION, req);

            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            try {
                JSONObject jonj = new JSONObject(result);
                if (jonj.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                    JSONObject rest = jonj.getJSONObject(Constants.TAG_RESULT);
                    String id = DefensiveClass.optString(rest, "id");
                    data.put(Constants.TAG_ID, id);
                } else {
                    data.put(Constants.TAG_ID, "0");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }


}
