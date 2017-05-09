package com.app.textbooktakeover;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

/**
 * Created by hitasoft on 24/6/16.
 **/
public class PromoteUrgent extends Fragment implements View.OnClickListener {

    public PromoteUrgent(){}
    ImageView promote, tick1, tick2, tick3, tick4;
    static RelativeLayout ad, main;
    public static TextView pay;
    static AVLoadingIndicatorView progress;
    static ScrollView scrollView;
    static TextView adText;
    TextView tagText, adText1, adText2, adText3, adText4, productType;
    View tagView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.create_promote, container , false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        promote = (ImageView) getView().findViewById(R.id.imageView);
        ad = (RelativeLayout) getView().findViewById(R.id.promotead);
        pay = (TextView) getView().findViewById(R.id.promote);
        scrollView = (ScrollView) getView().findViewById(R.id.scrollView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        main = (RelativeLayout) getView().findViewById(R.id.main);
        adText = (TextView) getView().findViewById(R.id.adText);
        tick1 = (ImageView) getView().findViewById(R.id.tick1);
        tick2 = (ImageView) getView().findViewById(R.id.tick2);
        tick3 = (ImageView) getView().findViewById(R.id.tick3);
        tick4 = (ImageView) getView().findViewById(R.id.tick4);
        tagText = (TextView) getView().findViewById(R.id.tagText);
        adText1 = (TextView) getView().findViewById(R.id.adText1);
        adText2 = (TextView) getView().findViewById(R.id.adText2);
        adText3 = (TextView) getView().findViewById(R.id.adText3);
        adText4 = (TextView) getView().findViewById(R.id.adText4);
        tagView = (View) getView().findViewById(R.id.tagView);
        productType = (TextView) getView().findViewById(R.id.productType);

        ad.setVisibility(View.GONE);

        promote.setImageResource(R.drawable.promote_bg);
        pay.setText(getString(R.string.pay_and_highlight));

        tick1.setColorFilter(getResources().getColor(R.color.red));
        tick2.setColorFilter(getResources().getColor(R.color.red));
        tick3.setColorFilter(getResources().getColor(R.color.red));
        tick4.setColorFilter(getResources().getColor(R.color.red));
        tagText.setText(getString(R.string.urgent_tag_features));
        tagText.setTextColor(getResources().getColor(R.color.red));
        tagView.setBackgroundColor(getResources().getColor(R.color.red));
        adText1.setText(getString(R.string.urgent_feature_list1));
        adText2.setText(getString(R.string.urgent_feature_list2));
        adText3.setText(getString(R.string.urgent_feature_list3));
        adText4.setText(getString(R.string.urgent_feature_list4));
        productType.setText(getString(R.string.urgent));
        productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.urgentbg));
        if(!CreatePromote.urgent.equals("")){
            String price = CreatePromote.currencySymbol+String.format("%.2f",Float.parseFloat(CreatePromote.urgent));
            adText.setText(Html.fromHtml(getString(R.string.urgent_des) + " <font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>"+ price + "</font>"));
        }

        pay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.promote:
                if (CreatePromote.clientToken.equals("")){
                    TextbookTakeoverApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.somethingwrong));
                } else {
                    Cart cart = Cart.newBuilder()
                            .setCurrencyCode(CreatePromote.currencySymbol)
                            .setTotalPrice(CreatePromote.urgent)
                            .addLineItem(LineItem.newBuilder()
                                    .setCurrencyCode(CreatePromote.currencySymbol)
                                    .setDescription("Promotion")
                                    .setQuantity("1")
                                    .setUnitPrice(CreatePromote.urgent)
                                    .setTotalPrice(CreatePromote.urgent)
                                    .build())
                            .build();
                    PaymentRequest paymentRequest = new PaymentRequest()
                            .clientToken(CreatePromote.clientToken)
                            .amount(CreatePromote.currencyCode+CreatePromote.urgent)
                            .primaryDescription("Total Amount")
                            .actionBarTitle(getResources().getString(R.string.app_name))
                            .submitButtonText("Pay")
                            .tokenizationKey(CreatePromote.clientToken)
                            .androidPayCart(cart);

                    startActivityForResult(paymentRequest.getIntent(getActivity()), CreatePromote.REQUEST_CODE);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("resultCode", "resultCode==" + resultCode);
        if (resultCode == BraintreePaymentActivity.RESULT_OK) {
            final PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
            Log.v("nonceeee", "noncee==" + paymentMethodNonce.getNonce());

            new PayForPromotion().execute(paymentMethodNonce.getNonce());

        }else if(resultCode==BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_ERROR || resultCode==BraintreePaymentActivity.BRAINTREE_RESULT_SERVER_UNAVAILABLE){
            TextbookTakeoverApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.payment_error));
        }
    }

    /** send paid promotion details to server **/
    class PayForPromotion extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_PAY_FOR_PROMOTION;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_PAY_FOR_PROMOTION);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("item_id", CreatePromote.itemId);
            req.addProperty("promotion_id", "0");
            req.addProperty("currency_code", CreatePromote.currencyCode);
            req.addProperty("pay_nonce", params[0]);

            SOAPParsing soap = new SOAPParsing();
            final String res = soap.getJSONFromUrl(SOAP_ACTION, req);
            try {
                Log.v("response","responsePayment=="+res);
                JSONObject json = new JSONObject(res);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                final String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);

                if (response.equalsIgnoreCase("true")) {
                    ((Activity)getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialog(getString(R.string.success), getString(R.string.your_promotion_was_activated_successfully));
                        }
                    });
                }else{
                    TextbookTakeoverApplication.dialog(getActivity(), getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            return null;
        }

    }

    private void showDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(PromoteUrgent.this.getActivity()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent i = new Intent(getActivity(), FragmentMainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                ((Activity)getActivity()).finish();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
