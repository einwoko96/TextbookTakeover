package com.hitasoft.app.textbooktakeover;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.SOAPParsing;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 23/6/16.
 */

public class CategoryActivity extends AppCompatActivity implements View.OnClickListener {

    ListView listView;
    ImageView backbtn;
    TextView title;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    CategoryAdapter categoryAdapter;
    ArrayList<HashMap<String, String>> categoryAry = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.category_layout);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        listView = (ListView) findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        title = (TextView) findViewById(R.id.title);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);

        title.setText(getString(R.string.categories));
        backbtn.setOnClickListener(this);

        try {
            if (TextbookTakeoverApplication.isNetworkAvailable(CategoryActivity.this)) {
                new getCategory().execute(0);
            }
            categoryAdapter = new CategoryAdapter(CategoryActivity.this, categoryAry);
            listView.setAdapter(categoryAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * for getting admin defined categories
     **/
    class getCategory extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            try {
                String SOAP_ACTION = Constants.NAMESPACE + Constants.API_CATEGORY;

                SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_CATEGORY);
                req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);

                SOAPParsing soap = new SOAPParsing();
                String json = soap.getJSONFromUrl(SOAP_ACTION, req);
                Log.v("res", "res=" + json);
                JSONObject jobj = new JSONObject(json);
                String response = jobj.getString(Constants.TAG_STATUS);

                if (response.equalsIgnoreCase("true")) {

                    JSONObject result = jobj
                            .optJSONObject(Constants.TAG_RESULT);
                    if (!(result == null)) {
                        JSONArray category = result.optJSONArray(Constants.TAG_CATEGORY);
                        if (category != null) {
                            for (int i = 0; i < category.length(); i++) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                JSONObject temp = category.getJSONObject(i);
                                String name = DefensiveClass.optString(temp, Constants.TAG_CATEGORYNAME);
                                String id = DefensiveClass.optString(temp, Constants.TAG_CATEGORYID);
                                String image = DefensiveClass.optString(temp, Constants.TAG_CATEGORYIMG).replace("/40/", "/200/");

                                map.put(Constants.TAG_CATEGORYNAME, name);
                                map.put(Constants.TAG_CATEGORYID, id);
                                map.put(Constants.TAG_CATEGORYIMG, image);

                                categoryAry.add(map);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            listView.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            categoryAdapter.notifyDataSetChanged();
            if (categoryAry.size() == 0) {
                nullLay.setVisibility(View.VISIBLE);
            } else {
                nullLay.setVisibility(View.GONE);
            }
        }
    }

    /**
     * adapter for list the categorys in listview
     **/
    public class CategoryAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        private Context mContext;
        ViewHolder holder = null;

        public CategoryAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
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
            TextView Txt1, Txt2, Txt3;
            ImageView Img1, Img2, Img3;
            LinearLayout l1, l2, l3;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.category_item, parent, false);//layout
                holder = new ViewHolder();

                holder.Txt1 = (TextView) convertView.findViewById(R.id.txt1);
                holder.Txt2 = (TextView) convertView.findViewById(R.id.txt2);
                holder.Txt3 = (TextView) convertView.findViewById(R.id.txt21);
                holder.Img1 = (ImageView) convertView.findViewById(R.id.img1);
                holder.Img2 = (ImageView) convertView.findViewById(R.id.img2);
                holder.Img3 = (ImageView) convertView.findViewById(R.id.img21);
                holder.l1 = (LinearLayout) convertView.findViewById(R.id.lay1);
                holder.l2 = (LinearLayout) convertView.findViewById(R.id.lay2);
                holder.l3 = (LinearLayout) convertView.findViewById(R.id.lay3);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                holder.l1.setVisibility(View.GONE);
                holder.l2.setVisibility(View.GONE);
                holder.l3.setVisibility(View.GONE);
                final HashMap<String, String> tempMap = Items.get(position);
                switch (position % 2) {
                    case 0:
                        if (tempMap.size() % 2 != 0 && (position == getCount())) {

                            holder.l1.setVisibility(View.GONE);
                            holder.l2.setVisibility(View.GONE);
                            holder.l3.setVisibility(View.VISIBLE);
                            holder.Txt3.setText(Items.get(position).get(Constants.TAG_CATEGORYNAME));

                            Picasso.with(CategoryActivity.this).load(Items.get(position).get(Constants.TAG_CATEGORYIMG)).into(holder.Img3);

                        } else {
                            holder.l1.setVisibility(View.VISIBLE);
                            holder.l2.setVisibility(View.VISIBLE);
                            holder.l3.setVisibility(View.GONE);

                            holder.Txt1.setText(Items.get(position).get(Constants.TAG_CATEGORYNAME));

                            Picasso.with(CategoryActivity.this).load(Items.get(position).get(Constants.TAG_CATEGORYIMG)).into(holder.Img1);

                            holder.Txt2.setText(Items.get(position + 1).get(Constants.TAG_CATEGORYNAME));
                            Picasso.with(CategoryActivity.this).load(Items.get(position + 1).get(Constants.TAG_CATEGORYIMG)).into(holder.Img2);
                        }
                        break;
                    case 2:
                        break;
                }

                holder.l1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentMainActivity.filterAry.clear();
                        SearchAdvance.applyFilter = true;
                        SearchAdvance.distance = "0";
                        SearchAdvance.categoryId.clear();
                        SearchAdvance.categoryName.clear();
                        SearchAdvance.subcategoryId.clear();
                        SearchAdvance.postedWithin = "";
                        SearchAdvance.sortBy = "1";
                        SearchAdvance.categoryId.add(Items.get(position).get(Constants.TAG_CATEGORYID));
                        SearchAdvance.categoryName.add(Items.get(position).get(Constants.TAG_CATEGORYNAME));
                        finish();
                        Intent i = new Intent(CategoryActivity.this, FragmentMainActivity.class);
                        startActivity(i);
                    }
                });
                holder.l2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentMainActivity.filterAry.clear();
                        SearchAdvance.applyFilter = true;
                        SearchAdvance.distance = "0";
                        SearchAdvance.categoryId.clear();
                        SearchAdvance.categoryName.clear();
                        SearchAdvance.subcategoryId.clear();
                        SearchAdvance.postedWithin = "";
                        SearchAdvance.sortBy = "1";
                        SearchAdvance.categoryId.add(Items.get(position + 1).get(Constants.TAG_CATEGORYID));
                        SearchAdvance.categoryName.add(Items.get(position + 1).get(Constants.TAG_CATEGORYNAME));
                        Intent i = new Intent(CategoryActivity.this, FragmentMainActivity.class);
                        startActivity(i);
                    }
                });
                holder.l3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentMainActivity.filterAry.clear();
                        SearchAdvance.applyFilter = true;
                        SearchAdvance.distance = "0";
                        SearchAdvance.categoryId.clear();
                        SearchAdvance.categoryName.clear();
                        SearchAdvance.subcategoryId.clear();
                        SearchAdvance.postedWithin = "";
                        SearchAdvance.sortBy = "1";
                        SearchAdvance.categoryId.add(Items.get(Items.size() - 1).get(Constants.TAG_CATEGORYID));
                        SearchAdvance.categoryName.add(Items.get(Items.size() - 1).get(Constants.TAG_CATEGORYNAME));
                        Intent i = new Intent(CategoryActivity.this, FragmentMainActivity.class);
                        startActivity(i);
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                holder.l1.setVisibility(View.GONE);
                holder.l2.setVisibility(View.GONE);
                holder.l3.setVisibility(View.VISIBLE);
                holder.Txt3.setText(Items.get(position).get(Constants.TAG_CATEGORYNAME));
                Picasso.with(CategoryActivity.this).load(Items.get(position).get(Constants.TAG_CATEGORYIMG)).into(holder.Img3);
                holder.l3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentMainActivity.filterAry.clear();
                        SearchAdvance.applyFilter = true;
                        SearchAdvance.distance = "0";
                        SearchAdvance.categoryId.clear();
                        SearchAdvance.categoryName.clear();
                        SearchAdvance.subcategoryId.clear();
                        SearchAdvance.postedWithin = "";
                        SearchAdvance.sortBy = "1";
                        SearchAdvance.categoryId.add(Items.get(Items.size() - 1).get(Constants.TAG_CATEGORYID));
                        SearchAdvance.categoryName.add(Items.get(Items.size() - 1).get(Constants.TAG_CATEGORYNAME));
                        Intent i = new Intent(CategoryActivity.this, FragmentMainActivity.class);
                        startActivity(i);
                    }
                });
                e.printStackTrace();
            }
            return convertView;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(CategoryActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(CategoryActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
        }
    }
}
