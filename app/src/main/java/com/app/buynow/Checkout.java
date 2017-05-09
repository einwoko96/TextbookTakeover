package com.app.buynow;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.textbooktakeover.TextbookTakeoverApplication;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;
import com.app.textbooktakeover.R;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;

/**
 * Created by hitasoft on 13/9/16.
 */

public class Checkout extends AppCompatActivity implements View.OnClickListener{

    ImageView backbtn, itemImage;
    TextView title, itemPrice, itemTitle, sellerName, change, price, shipping, total, paynow,
            nickName, fullName, addressLine, city, country;
    String clientToken="",currencySymbol="", currencyCode="", totalPrice="", shippingId="";
    HashMap<String, String> data = new HashMap<>();
    static final int REQUEST_CODE = 100;
    LinearLayout mainLay;
    AVLoadingIndicatorView progress;
    HashMap<String, String> addressMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        itemImage = (ImageView) findViewById(R.id.itemImage);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        itemTitle = (TextView) findViewById(R.id.itemTitle);
        sellerName = (TextView) findViewById(R.id.sellerName);
        change = (TextView) findViewById(R.id.change);
        price = (TextView) findViewById(R.id.price);
        shipping = (TextView) findViewById(R.id.shipping);
        total = (TextView) findViewById(R.id.total);
        paynow = (TextView) findViewById(R.id.paynow);
        mainLay = (LinearLayout) findViewById(R.id.mainLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nickName = (TextView) findViewById(R.id.nickName);
        fullName = (TextView) findViewById(R.id.fullName);
        addressLine = (TextView) findViewById(R.id.addressLine);
        city = (TextView) findViewById(R.id.city);
        country = (TextView) findViewById(R.id.country);

        data = (HashMap<String, String>)getIntent().getExtras().get("itemData");
        addressMap = (HashMap<String, String>)getIntent().getExtras().get("shippingData");

        title.setText(getString(R.string.checkout));

        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        backbtn.setOnClickListener(this);
        paynow.setOnClickListener(this);
        change.setOnClickListener(this);

        progress.setVisibility(View.VISIBLE);
        mainLay.setVisibility(View.GONE);
        paynow.setVisibility(View.GONE);

        new getClientToken().execute();

        setData();
    }

    private void setData() {
        try {
            Picasso.with(Checkout.this).load(data.get(Constants.TAG_ITEM_URL_350)).into(itemImage);
            itemTitle.setText(data.get(Constants.TAG_TITLE));

            String[] currency = data.get(Constants.TAG_CURRENCY_CODE).split("-");
            currencySymbol=currency[0];
            currencyCode=currency[1];

            if (TextbookTakeoverApplication.isRTL(Checkout.this)){
                itemTitle.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            } else {
                itemTitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            }

            itemPrice.setText(currencySymbol + data.get(Constants.TAG_PRICE));
            sellerName.setText(data.get(Constants.TAG_SELLERNAME));
            price.setText(currencySymbol + data.get(Constants.TAG_PRICE));
            shipping.setText(currencySymbol + data.get(Constants.TAG_SHIPPING_COST));
            int tot = Integer.parseInt(data.get(Constants.TAG_PRICE)) + Integer.parseInt(data.get(Constants.TAG_SHIPPING_COST));
            totalPrice = Integer.toString(tot);
            total.setText(currencySymbol + totalPrice);

            if (addressMap.size() > 0){
                nickName.setText(addressMap.get(Constants.TAG_NICKNAME));
                fullName.setText(addressMap.get(Constants.TAG_NAME));
                addressLine.setText(addressMap.get(Constants.TAG_ADDRESS1)+" , "+ addressMap.get(Constants.TAG_ADDRESS2));
                city.setText(addressMap.get(Constants.TAG_CITY)+" , "+ addressMap.get(Constants.TAG_ZIPCODE));
                country.setText(addressMap.get(Constants.TAG_COUNTRY));
                shippingId = addressMap.get(Constants.TAG_SHIPPINGID);
            }
        } catch (NumberFormatException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /** class for get client token to send braintree **/
    class getClientToken extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_CLIENTTOKEN;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_CLIENTTOKEN);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);

            SOAPParsing soap = new SOAPParsing();
            final String res = soap.getJSONFromUrl(SOAP_ACTION, req);

            try {
                JSONObject json = new JSONObject(res);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);

                if (response.equalsIgnoreCase("true")) {
                    clientToken = DefensiveClass.optString(json, Constants.TAG_TOKEN);
                }else{
                    clientToken="";
                }
            }catch (JSONException e){
                e.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (clientToken.equals("")) {
                Toast.makeText(Checkout.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            } else {
                progress.setVisibility(View.GONE);
                mainLay.setVisibility(View.VISIBLE);
                paynow.setVisibility(View.VISIBLE);
            }
        }

    }

    /** send paid item details to server **/
    class PayNow extends AsyncTask<String, Void, Void> {

        private ProgressDialog dialog = new ProgressDialog(Checkout.this);
        @Override
        protected Void doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_BUYNOW_PAYMENT;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_BUYNOW_PAYMENT);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("merchant_id", data.get(Constants.TAG_SELLERID));
            req.addProperty("shipping_id", shippingId);
            req.addProperty("item_id", data.get(Constants.TAG_ID));
            req.addProperty("item_price",data.get(Constants.TAG_PRICE));
            req.addProperty("shipping_fee", data.get(Constants.TAG_SHIPPING_COST));
            req.addProperty("order_total", totalPrice);
            req.addProperty("currency_code", currencyCode);
            req.addProperty("nonce", params[0]);

            SOAPParsing soap = new SOAPParsing();
            final String res = soap.getJSONFromUrl(SOAP_ACTION, req);
            try {
                Log.v("response","responsePayment=="+res);
                JSONObject json = new JSONObject(res);
                final String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                final String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.equalsIgnoreCase("true")) {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            showDialog(getString(R.string.success), getString(R.string.ordered_successfully));
                        }else{
                            TextbookTakeoverApplication.dialog(Checkout.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                        }
                    }
                });

            }catch (JSONException e){
                e.printStackTrace();
            }catch (NullPointerException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage(getString(R.string.pleasewait));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }
    }

    private void showDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(Checkout.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Checkout.this, MySalesnOrder.class);
                i.putExtra("from", "buynow");
                startActivity(i);
                finish();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("resultCode", "resultCode==" + resultCode);
        if (resultCode == BraintreePaymentActivity.RESULT_OK) {
            final PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
            Log.v("nonceeee", "noncee==" + paymentMethodNonce.getNonce());

            new PayNow().execute(paymentMethodNonce.getNonce());

        }else if(resultCode==BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR || resultCode==BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_UNAVAILABLE){
            TextbookTakeoverApplication.dialog(Checkout.this, getString(R.string.alert), getString(R.string.payment_error));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(Checkout.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(Checkout.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                finish();
                break;
            case R.id.paynow:
                if (clientToken.equals("")){
                    TextbookTakeoverApplication.dialog(Checkout.this, getString(R.string.alert), getString(R.string.somethingwrong));
                } else {
                    Cart cart = Cart.newBuilder()
                            .setCurrencyCode(currencySymbol)
                            .setTotalPrice(totalPrice)
                            .addLineItem(LineItem.newBuilder()
                                    .setCurrencyCode(currencySymbol)
                                    .setDescription("Buy Now")
                                    .setQuantity("1")
                                    .setUnitPrice(totalPrice)
                                    .setTotalPrice(totalPrice)
                                    .build())
                            .build();
                    PaymentRequest paymentRequest = new PaymentRequest()
                            .clientToken(clientToken)
                            .amount(currencyCode + totalPrice)
                            .primaryDescription("Total Amount")
                            .actionBarTitle(getResources().getString(R.string.app_name))
                            .submitButtonText("Pay")
                            .tokenizationKey(clientToken)
                            .androidPayCart(cart);

                    startActivityForResult(paymentRequest.getIntent(Checkout.this), REQUEST_CODE);
                }
                break;
            case R.id.change:
                Intent l = new Intent(Checkout.this, Addresses.class);
                l.putExtra("from", "checkout");
                l.putExtra("shippingId", shippingId);
                l.putExtra("itemData", data);
                startActivity(l);
                break;
        }
    }
}
