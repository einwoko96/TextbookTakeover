package com.app.textbooktakeover;

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

import com.app.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 23/6/16.
 */

public class SubCategory extends AppCompatActivity implements View.OnClickListener{

    ArrayList<HashMap<String, String>> datas = new ArrayList<HashMap<String, String>>();
    ListView listView;
    TextView categoryName, title;
    ImageView backbtn;
    String from = "", categoryname="", categoryId="", subcatId="";
    CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sub_category);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        listView = (ListView) findViewById(R.id.listView);
        categoryName = (TextView) findViewById(R.id.categoryName);
        title = (TextView) findViewById(R.id.title);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);

        from = (String) getIntent().getExtras().get("from");
        datas = (ArrayList<HashMap<String, String>>) getIntent().getExtras().get("data");

        if (from.equals("filter")){
            title.setText(getString(R.string.categories));

            categoryname = (String) getIntent().getExtras().get("categoryName");
            categoryId = (String) getIntent().getExtras().get("categoryId");

            categoryName.setText(categoryname);
            subcatId = SearchAdvance.subcategoryId.get(categoryId);
        } else if (from.equals("add")) {
            title.setText(getString(R.string.itemcondition));

            AddProductDetail.itemCond = "";
            AddProductDetail.itemCond = (String) getIntent().getExtras().get("name");

            categoryName.setVisibility(View.GONE);
        }

        backbtn.setOnClickListener(this);

        Log.v("datas", "datas=" + datas);

        categoryAdapter = new CategoryAdapter(SubCategory.this, datas);
        listView.setAdapter(categoryAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                finish();
                break;
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
            ImageView tick;
            TextView name;
            RelativeLayout mainLay;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.filter_row_selection, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.tick = (ImageView) convertView.findViewById(R.id.tick);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final HashMap<String, String> map = datas.get(position);

                if (from.equals("filter")){
                    holder.tick.setVisibility(View.VISIBLE);
                    holder.name.setText(map.get(Constants.TAG_SUBNAME));
                    holder.tick.setColorFilter(getResources().getColor(R.color.grey));

                    if (map.get(Constants.TAG_SUBID).equals(subcatId)){
                        holder.tick.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    } else {
                        holder.tick.setColorFilter(getResources().getColor(R.color.grey));
                    }
                } else if (from.equals("add")) {
                    holder.tick.setVisibility(View.INVISIBLE);
                    holder.name.setText(map.get(Constants.TAG_NAME));

                    if (map.get(Constants.TAG_NAME).equals(AddProductDetail.itemCond)){
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                    }
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (from.equals("filter")){
                            if (subcatId.equals(map.get(Constants.TAG_SUBID))) {
                                subcatId = "";
                                if (SearchAdvance.categoryId.contains(categoryId)){
                                    SearchAdvance.categoryId.remove(categoryId);
                                    SearchAdvance.categoryName.remove(categoryname);
                                }
                                if (SearchAdvance.subcategoryId.containsKey(categoryId)) {
                                    SearchAdvance.subcategoryId.remove(categoryId);
                                }
                            } else {
                                subcatId = map.get(Constants.TAG_SUBID);
                                if (!SearchAdvance.categoryId.contains(categoryId)) {
                                    SearchAdvance.categoryId.add(categoryId);
                                }
                                if (!SearchAdvance.categoryName.contains(categoryname)){
                                    SearchAdvance.categoryName.add(categoryname);
                                }
                                SearchAdvance.subcategoryId.put(categoryId, subcatId);
                            }
                        } else if (from.equals("add")) {
                            AddProductDetail.itemCond = map.get(Constants.TAG_NAME);
                            if (AddProductDetail.itemCondition != null) {
                                AddProductDetail.itemCondition.setText(AddProductDetail.itemCond);
                                AddProductDetail.itemCondition.setTextColor(getResources().getColor(R.color.primaryText));
                                AddProductDetail.condArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                            }
                        }
                        categoryAdapter.notifyDataSetChanged();
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
        TextbookTakeoverApplication.unregisterReceiver(SubCategory.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(SubCategory.this);
    }
}
