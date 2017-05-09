package com.app.textbooktakeover;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.app.external.ExpandableHeightListView;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 24/6/16.
 **/
public class Promotead extends Fragment implements View.OnClickListener {

    ImageView promote, tick1, tick2, tick3, tick4;
    static RelativeLayout ad, main;
    public static TextView payPromote;
    ExpandableHeightListView promoteList;
    static AVLoadingIndicatorView progress;
    static ScrollView scrollView;
    TextView adText, tagText, adText1, adText2, adText3, adText4, productType;
    public static PromoteAdapter adapter;
    Context context;
    String selectedId = "", selectedPrice = "";
    View tagView;

    public Promotead(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
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
        payPromote = (TextView) getView().findViewById(R.id.promote);
        adText = (TextView) getView().findViewById(R.id.adText);
        promoteList = (ExpandableHeightListView) getView().findViewById(R.id.promoteList);
        scrollView = (ScrollView) getView().findViewById(R.id.scrollView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        main = (RelativeLayout) getView().findViewById(R.id.main);
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

        context = getActivity();

        ad.setVisibility(View.VISIBLE);
        adText.setVisibility(View.VISIBLE);

        promote.setImageResource(R.drawable.promote_bg);
        payPromote.setText(getString(R.string.pay_and_promote));
        adText.setText(getString(R.string.ads_des));

        payPromote.setOnClickListener(this);

        promoteList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        promoteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedId = CreatePromote.promoteItems.get(position).get(Constants.TAG_ID);
                selectedPrice = CreatePromote.promoteItems.get(position).get(Constants.TAG_PRICE);
                adapter.notifyDataSetChanged();
            }
        });

        tick1.setColorFilter(getResources().getColor(R.color.colorPrimary));
        tick2.setColorFilter(getResources().getColor(R.color.colorPrimary));
        tick3.setColorFilter(getResources().getColor(R.color.colorPrimary));
        tick4.setColorFilter(getResources().getColor(R.color.colorPrimary));
        tagText.setText(getString(R.string.promote_tag_features));
        tagText.setTextColor(getResources().getColor(R.color.colorPrimary));
        tagView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        adText1.setText(getString(R.string.promote_feature_list1));
        adText2.setText(getString(R.string.promote_feature_list2));
        adText3.setText(getString(R.string.promote_feature_list3));
        adText4.setText(getString(R.string.promote_feature_list4));
        productType.setText(getString(R.string.ad));
        productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.adbg));
        setAdapter();
    }

    private void setAdapter() {
        adapter = new PromoteAdapter(getActivity(),CreatePromote.promoteItems);
        promoteList.setAdapter(adapter);
        promoteList.setExpanded(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.promote:
                if (CreatePromote.clientToken.equals("")){
                    TextbookTakeoverApplication.dialog(getActivity(), getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                } else if(selectedId.equals("")){
                    TextbookTakeoverApplication.dialog(getActivity(), getResources().getString(R.string.alert), getString(R.string.please_select_promotion));
                }else {
                    Cart cart = Cart.newBuilder()
                            .setCurrencyCode(CreatePromote.currencySymbol)
                            .setTotalPrice(selectedPrice)
                            .addLineItem(LineItem.newBuilder()
                                    .setCurrencyCode(CreatePromote.currencySymbol)
                                    .setDescription("Promotion")
                                    .setQuantity("1")
                                    .setUnitPrice(selectedPrice)
                                    .setTotalPrice(selectedPrice)
                                    .build())
                            .build();
                    PaymentRequest paymentRequest = new PaymentRequest()
                            .clientToken(CreatePromote.clientToken)
                            .amount(CreatePromote.currencyCode+selectedPrice)
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
            req.addProperty("promotion_id", selectedId);
            req.addProperty("currency_code", CreatePromote.currencyCode);
            req.addProperty("pay_nonce", params[0]);

            SOAPParsing soap = new SOAPParsing();
            final String res = soap.getJSONFromUrl(SOAP_ACTION, req);
            try {
                JSONObject json = new JSONObject(res);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                final String message = DefensiveClass.optString(json, Constants.TAG_MESSAGE);

                if (response.equalsIgnoreCase("true")) {
                    ((Activity)getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialog(Promotead.this.getActivity(), getString(R.string.success), getString(R.string.your_promotion_was_activated_successfully));
                        }
                    });
                }else{
                    TextbookTakeoverApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.somethingwrong));
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            return null;
        }

    }

    private void showDialog(final Context context, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent i = new Intent(getActivity(), FragmentMainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                ((Activity)context).finish();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public class PromoteAdapter extends BaseAdapter{
        private Context mContext;
        ArrayList<HashMap<String, String>> data;
        ViewHolder holder = null;

        PromoteAdapter(Context context, ArrayList<HashMap<String, String>> Items){
            mContext = context;
            data = Items;
        }

        @Override
        public int getCount() {
            return data.size();
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
            TextView promotionName, promotionPrice, promotionDays;
            View viewLine;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.create_promote_item,
                        parent, false);
                holder = new ViewHolder();

                holder.promotionName = (TextView) convertView.findViewById(R.id.promotionName);
                holder.promotionPrice = (TextView) convertView.findViewById(R.id.promotionPrice);
                holder.promotionDays = (TextView) convertView.findViewById(R.id.promotionDays);
                holder.viewLine = (View) convertView.findViewById(R.id.view);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            try{
                HashMap<String,String> map = data.get(position);

                if(selectedId.equals(map.get(Constants.TAG_ID))){
                    holder.viewLine.setVisibility(View.VISIBLE);
                    holder.promotionPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else {
                    holder.viewLine.setVisibility(View.INVISIBLE);
                    holder.promotionPrice.setTextColor(getResources().getColor(R.color.primaryText));
                }

                holder.promotionName.setText(map.get(Constants.TAG_NAME));
                holder.promotionDays.setText(map.get(Constants.TAG_DAYS) + " " + getString(R.string.days));
                holder.promotionPrice.setText(CreatePromote.currencySymbol+
                        String.format("%.2f", Float.parseFloat(map.get(Constants.TAG_PRICE))));

            }catch(NullPointerException e){
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }
            return convertView;
        }
    }
}
