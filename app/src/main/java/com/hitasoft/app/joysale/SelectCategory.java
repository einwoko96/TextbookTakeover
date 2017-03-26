package com.hitasoft.app.joysale;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hitasoft.app.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 2/7/16.
 */

public class SelectCategory extends AppCompatActivity implements View.OnClickListener{

    TextView title;
    ImageView backbtn;
    ArrayList<HashMap<String, String>> categAry = new ArrayList<HashMap<String, String>>();
    ArrayList<ArrayList<HashMap<String, String>>> subcategAry = new ArrayList<ArrayList<HashMap<String, String>>>();
    ListView categoryList, subList;
    String from = "";
    CategoryAdapter categoryAdapter;
    SubAdapter subAdapter;
    int padding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_category);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        categoryList = (ListView) findViewById(R.id.categoryList);
        subList = (ListView) findViewById(R.id.subList);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.categories));

        from = (String) getIntent().getExtras().get("from");
        categAry = (ArrayList<HashMap<String, String>>) getIntent().getExtras().get("categAry");
        subcategAry = (ArrayList<ArrayList<HashMap<String, String>>>) getIntent().getExtras().get("subcategAry");

        padding = JoysaleApplication.dpToPx(SelectCategory.this, 15);

        categoryAdapter = new CategoryAdapter(SelectCategory.this, categAry);
        categoryList.setAdapter(categoryAdapter);

        Log.v("subcategAry", "subcategAry" + subcategAry);

        if (AddProductDetail.categId.equals("")){
            if (categAry.size()>0 && subcategAry.size() > 0) {
                Log.v("if", "if");
                AddProductDetail.categId = categAry.get(0).get("id");
                if(AddProductDetail.category != null){
                    AddProductDetail.category.setText(categAry.get(0).get("name"));
                    AddProductDetail.category.setTextColor(getResources().getColor(R.color.primaryText));
                    AddProductDetail.catArrow.setColorFilter(getResources().getColor(R.color.primaryText));

                    setCategoryConditions(categAry.get(0));
                }

                categoryAdapter.notifyDataSetChanged();

                subAdapter = new SubAdapter(SelectCategory.this, subcategAry.get(0));
                subList.setAdapter(subAdapter);
            }
        } else{
            categoryAdapter.notifyDataSetChanged();
            if (categAry.size()>0 && subcategAry.size() > 0) {
                for (int i = 0; i < categAry.size(); i++){
                    if (categAry.get(i).get("id").equals(AddProductDetail.categId)){
                        Log.v("else", "else");
                        if(AddProductDetail.category != null){
                            AddProductDetail.category.setText(categAry.get(i).get("name"));
                            AddProductDetail.category.setTextColor(getResources().getColor(R.color.primaryText));
                            AddProductDetail.catArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                        }
                        categoryAdapter.notifyDataSetChanged();

                        subAdapter = new SubAdapter(SelectCategory.this, subcategAry.get(i));
                        subList.setAdapter(subAdapter);
                        break;
                    }
                }
            }
        }
        backbtn.setOnClickListener(this);
    }

    /** set product condition by choosing the category **/
    private void setCategoryConditions(HashMap<String, String> map){
        if (map.get("id").equals(AddProductDetail.categId)){
            if (map.get("product_condition").equals("disable") && map.get("exchange_buy").equals("disable")
                    && map.get("make_offer").equals("disable")) {
                AddProductDetail.bottomLay.setVisibility(View.GONE);
                AddProductDetail.conditionLay.setVisibility(View.GONE);
                AddProductDetail.exchangeLay.setVisibility(View.GONE);
                AddProductDetail.offerLay.setVisibility(View.GONE);
            } else {
                AddProductDetail.bottomLay.setVisibility(View.VISIBLE);
                AddProductDetail.conditionLay.setVisibility(View.VISIBLE);
                AddProductDetail.exchangeLay.setVisibility(View.VISIBLE);
                AddProductDetail.offerLay.setVisibility(View.VISIBLE);
                if (map.get("product_condition").equals("enable")){
                    AddProductDetail.conditionLay.setVisibility(View.VISIBLE);
                } else {
                    AddProductDetail.conditionLay.setVisibility(View.GONE);
                }

                if (map.get("exchange_buy").equals("enable")){
                    if (Constants.EXCHANGE){
                        AddProductDetail.exchangeLay.setVisibility(View.VISIBLE);
                    } else {
                        AddProductDetail.exchangeLay.setVisibility(View.GONE);
                    }
                } else {
                    AddProductDetail.exchangeLay.setVisibility(View.GONE);
                }

                if (map.get("make_offer").equals("enable")){
                    AddProductDetail.offerLay.setVisibility(View.VISIBLE);
                } else {
                    AddProductDetail.offerLay.setVisibility(View.GONE);
                }
            }

            if (map.get("instant_buy").equals("disable")){
                AddProductDetail.instantLay.setVisibility(View.GONE);
            } else {
                if (Constants.BUYNOW){
                    AddProductDetail.instantLay.setVisibility(View.VISIBLE);
                } else {
                    AddProductDetail.instantLay.setVisibility(View.GONE);
                }
            }
        }
    }

    public class CategoryAdapter extends BaseAdapter {

        private Context mContext;
        ArrayList<HashMap<String, String>> datas;
        ViewHolder holder = null;

        public CategoryAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            datas = data;
        }

        @Override
        public int getCount() {
            return datas.size();
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
            ImageView icon, selectIcon;
            TextView name;
            View selectView;
            RelativeLayout mainLay;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.category_list_item, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.selectIcon = (ImageView) convertView.findViewById(R.id.selectIcon);
                holder.selectView = (View) convertView.findViewById(R.id.selectView);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final HashMap<String, String> map = datas.get(position);

                holder.name.setText(map.get("name"));
                Picasso.with(mContext).load(map.get("image")).into(holder.icon);
                if (map.get("id").equals(AddProductDetail.categId)){
                    holder.selectIcon.setVisibility(View.VISIBLE);
                    holder.selectView.setVisibility(View.VISIBLE);
                } else {
                    holder.selectIcon.setVisibility(View.GONE);
                    holder.selectView.setVisibility(View.GONE);
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddProductDetail.categId = map.get("id");
                        categoryAdapter.notifyDataSetChanged();

                        if(AddProductDetail.category != null){
                            AddProductDetail.category.setText(map.get("name"));
                            AddProductDetail.category.setTextColor(getResources().getColor(R.color.primaryText));
                            AddProductDetail.catArrow.setColorFilter(getResources().getColor(R.color.primaryText));

                            setCategoryConditions(map);
                        }

                        if (subcategAry.get(position).size() > 0){
                            subAdapter = new SubAdapter(mContext, subcategAry.get(position));
                            subList.setAdapter(subAdapter);
                        } else {
                            finish();
                        }
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }

    public class SubAdapter extends BaseAdapter {

        private Context mContext;
        ArrayList<HashMap<String, String>> datas;
        ViewHolder holder = null;

        public SubAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            datas = data;
        }

        @Override
        public int getCount() {
            return datas.size();
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
            ImageView tick;
            TextView name;
            RelativeLayout mainLay;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_row_selection, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.tick = (ImageView) convertView.findViewById(R.id.tick);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                holder.mainLay.setPadding(0, padding, padding, padding);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final HashMap<String, String> map = datas.get(position);

                holder.name.setText(map.get("name"));
                holder.tick.setVisibility(View.INVISIBLE);
                if (map.get("id").equals(AddProductDetail.subcategId)){
                    holder.tick.setVisibility(View.VISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.tick.setVisibility(View.INVISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddProductDetail.subcategId = map.get("id");
                        subAdapter.notifyDataSetChanged();
                        finish();
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(SelectCategory.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(SelectCategory.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                finish();
                break;
        }
    }
}
