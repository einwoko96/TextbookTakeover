package com.app.buynow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.textbooktakeover.TextbookTakeoverApplication;
import com.app.utils.Constants;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;

/**
 * Created by hitasoft on 17/1/17.
 */

public class WriteReview extends AppCompatActivity implements View.OnClickListener {

    ImageView backbtn;
    RatingBar ratingBar;
    EditText reviewTitle, review;
    TextView submit;
    String from;
    int position;
    HashMap<String, String> data = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_review);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        reviewTitle = (EditText) findViewById(R.id.reviewTitle);
        review = (EditText) findViewById(R.id.review);
        submit = (TextView) findViewById(R.id.submit);

        backbtn.setOnClickListener(this);
        submit.setOnClickListener(this);

        from = (String) getIntent().getExtras().get("from");
        data = (HashMap<String, String>) getIntent().getExtras().get("data");
        position = (int) getIntent().getExtras().get("position");

        if (from.equals("edit")){
            ratingBar.setRating(Float.parseFloat(data.get(Constants.TAG_RATING)));
            reviewTitle.setText(data.get(Constants.TAG_REVIEW_TITLE));
            review.setText(data.get(Constants.TAG_REVIEW_DES));
        }

        if (TextbookTakeoverApplication.isRTL(WriteReview.this)){
            reviewTitle.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            review.setGravity(Gravity.RIGHT | Gravity.TOP);
        } else {
            reviewTitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            review.setGravity(Gravity.LEFT | Gravity.TOP);
        }

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
    }

    class writeReview extends AsyncTask<String, String, String> {
        private ProgressDialog dialog = new ProgressDialog(WriteReview.this);

        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_UPDATE_REVIEW;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_UPDATE_REVIEW);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("seller_id", data.get(Constants.TAG_SELLERID));
            req.addProperty("rating", params[0]);
            req.addProperty("review_title", params[1]);
            req.addProperty("review_des", params[2]);
            req.addProperty("order_id", data.get(Constants.TAG_ORDER_ID));
            if (from.equals("edit")){
                req.addProperty("review_id", data.get(Constants.TAG_REVIEW_ID));
            } else {
                req.addProperty("review_id", "0");
            }

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

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
            submit.setOnClickListener(WriteReview.this);
            if (this.dialog.isShowing() && this.dialog != null) {
                this.dialog.dismiss();
            }
            try {
                JSONObject jobj = new JSONObject(json);
                String status = jobj.getString(Constants.TAG_STATUS);

                if (status.equalsIgnoreCase("true")) {
                    finish();
                    Intent i = new Intent(WriteReview.this, MySalesnOrder.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    Toast.makeText(WriteReview.this, getString(R.string.rated_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WriteReview.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.submit:
                if (ratingBar.getRating() == 0){
                    Toast.makeText(WriteReview.this, getString(R.string.please_give_your_rating), Toast.LENGTH_SHORT).show();
                } else if (reviewTitle.getText().toString().trim().length() == 0 || review.getText().toString().trim().length() == 0) {
                    Toast.makeText(WriteReview.this, getString(R.string.please_fill), Toast.LENGTH_SHORT).show();
                } else {
                    submit.setOnClickListener(null);
                    new writeReview().execute(String.valueOf((int) ratingBar.getRating()), reviewTitle.getText().toString().trim(), review.getText().toString().trim());
                }
                break;
        }
    }
}
