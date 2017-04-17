package com.hitasoft.app.textbooktakeover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * Created by hitasoft on 24/6/16.
 **/
public class PromotionDetail extends Activity implements View.OnClickListener {

    ImageView backBtn, userImg, itemImage;
    TextView username, itemName, promotionType, paidAmount, transactionId, upto, status, repromote;
    HashMap<String, String> data = new HashMap<>();
    LinearLayout dateLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promotion_detail);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        userImg = (ImageView) findViewById(R.id.userImg);
        username = (TextView) findViewById(R.id.username);
        itemImage = (ImageView) findViewById(R.id.imageView);
        itemName = (TextView) findViewById(R.id.itemtitle);
        promotionType = (TextView) findViewById(R.id.addvr);
        paidAmount = (TextView) findViewById(R.id.amount);
        transactionId = (TextView) findViewById(R.id.transid);
        upto = (TextView) findViewById(R.id.date);
        status = (TextView) findViewById(R.id.status);
        repromote = (TextView) findViewById(R.id.promote);
        dateLay = (LinearLayout) findViewById(R.id.dateLay);

        data = (HashMap<String, String>)getIntent().getExtras().get("data");

        backBtn.setVisibility(View.VISIBLE);
        userImg.setVisibility(View.VISIBLE);
        username.setVisibility(View.VISIBLE);
        repromote.setVisibility(View.GONE);

        repromote.setOnClickListener(this);

        Picasso.with(PromotionDetail.this).load(GetSet.getImageUrl()).into(userImg);
        username.setText(GetSet.getUserName());

        setData();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /** set promotion details to elements **/
    private void setData() {
        Picasso.with(PromotionDetail.this).load(Constants.url + "item/products/resized/350/" + data.get(Constants.TAG_ITEM_ID) + "/" + data.get(Constants.TAG_ITEM_IMAGE)).into(itemImage);
        itemName.setText(data.get(Constants.TAG_ITEM_NAME));
        promotionType.setText(data.get(Constants.TAG_PROMOTION_NAME));
        paidAmount.setText(data.get(Constants.TAG_CURRENCY_SYM)+data.get(Constants.TAG_PAID_AMOUNT));
        transactionId.setText(data.get(Constants.TAG_TRANSACTION_ID));
        upto.setText(data.get(Constants.TAG_UPTO));
        status.setText(data.get(Constants.TAG_STATUS));
        //if(data.get(Constants.TAG_STATUS).equalsIgnoreCase("expired"))
        if (data.get(Constants.TAG_PROMOTION_NAME).equals("urgent")){
            dateLay.setVisibility(View.GONE);
        } else {
            dateLay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(PromotionDetail.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(PromotionDetail.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.promote:
                Intent i = new Intent(PromotionDetail.this, CreatePromote.class);
                i.putExtra("itemId", data.get(Constants.TAG_ITEM_ID));
                startActivity(i);
                break;
        }
    }
}


