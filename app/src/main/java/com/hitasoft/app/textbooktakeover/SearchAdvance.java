package com.hitasoft.app.textbooktakeover;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hitasoft.app.external.ExpandableHeightListView;
import com.hitasoft.app.external.MySeekBar;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.SOAPParsing;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchAdvance extends AppCompatActivity implements OnClickListener, SeekBar.OnSeekBarChangeListener {

    TextView title, last24Txt, last7Txt, last30Txt, allproductTxt, popularTxt, urgentTxt, highTxt, lowTxt, reset, apply, seektext;
    InputMethodManager imm;
    ImageView backbtn, home, road, last24Next, last7Next, last30Next, allproductNext, popularNext, urgentNext, highNext, lowNext;
    MySeekBar conditionBar;
    RelativeLayout locationLay, mainLay;
    LinearLayout saveLay;
    ExpandableHeightListView category;
    AVLoadingIndicatorView progress;
    ArrayList<HashMap<String,String>> categoryAry = new ArrayList<HashMap<String,String>>();
    ArrayList<ArrayList<HashMap<String, String>>> subcategAry = new ArrayList<ArrayList<HashMap<String, String>>>();
    CategoryAdapter categoryAdapter;
    public static ArrayList<String> categoryId = new ArrayList<String>();
    public static ArrayList<String> categoryName = new ArrayList<String>();
    public static HashMap<String,String> subcategoryId = new HashMap<String,String>();
    public static String postedWithin = "", sortBy = "1", distance="0", postedTxt="", sortTxt="";
    public static float distanceX;
    public static boolean applyFilter = false;
    int primaryText, colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_advance);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        conditionBar = (MySeekBar) findViewById(R.id.conditionBar);
        seektext = (TextView) findViewById(R.id.seektext);
        title = (TextView) findViewById(R.id.title);
        home = (ImageView) findViewById(R.id.home);
        road = (ImageView) findViewById(R.id.road);
        locationLay = (RelativeLayout) findViewById(R.id.locationLay);
        category = (ExpandableHeightListView) findViewById(R.id.category);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        mainLay = (RelativeLayout) findViewById(R.id.mainLay);
        saveLay= (LinearLayout) findViewById(R.id.saveLay);
        last24Txt = (TextView) findViewById(R.id.last24Txt);
        last7Txt = (TextView) findViewById(R.id.last7Txt);
        last30Txt = (TextView) findViewById(R.id.last30Txt);
        allproductTxt = (TextView) findViewById(R.id.allproductTxt);
        popularTxt = (TextView) findViewById(R.id.popularTxt);
        urgentTxt = (TextView) findViewById(R.id.urgentTxt);
        highTxt = (TextView) findViewById(R.id.highTxt);
        lowTxt = (TextView) findViewById(R.id.lowTxt);
        reset = (TextView) findViewById(R.id.reset);
        apply = (TextView) findViewById(R.id.apply);
        last24Next = (ImageView) findViewById(R.id.last24Next);
        last7Next = (ImageView) findViewById(R.id.last7Next);
        last30Next = (ImageView) findViewById(R.id.last30Next);
        allproductNext = (ImageView) findViewById(R.id.allproductNext);
        popularNext = (ImageView) findViewById(R.id.popularNext);
        urgentNext = (ImageView) findViewById(R.id.urgentNext);
        highNext = (ImageView) findViewById(R.id.highNext);
        lowNext = (ImageView) findViewById(R.id.lowNext);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        saveLay.setVisibility(View.GONE);
        mainLay.setVisibility(View.GONE);

        title.setText(getString(R.string.filter));

        backbtn.setOnClickListener(this);
        locationLay.setOnClickListener(this);
        conditionBar.setOnSeekBarChangeListener(this);
        last24Txt.setOnClickListener(this);
        last7Txt.setOnClickListener(this);
        last30Txt.setOnClickListener(this);
        allproductTxt.setOnClickListener(this);
        popularTxt.setOnClickListener(this);
        urgentTxt.setOnClickListener(this);
        highTxt.setOnClickListener(this);
        lowTxt.setOnClickListener(this);
        last24Next.setOnClickListener(this);
        last7Next.setOnClickListener(this);
        last30Next.setOnClickListener(this);
        allproductNext.setOnClickListener(this);
        popularNext.setOnClickListener(this);
        urgentNext.setOnClickListener(this);
        highNext.setOnClickListener(this);
        lowNext.setOnClickListener(this);
        reset.setOnClickListener(this);
        apply.setOnClickListener(this);
        category.setExpanded(true);

        primaryText = getResources().getColor(R.color.primaryText);
        colorPrimary = getResources().getColor(R.color.colorPrimary);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        setSortBy(sortBy);
        setPostedWithin(postedWithin);
        conditionBar.setProgress(Integer.parseInt(distance));

        new getCategory().execute();
        categoryAdapter = new CategoryAdapter(SearchAdvance.this, categoryAry, subcategAry);
        category.setAdapter(categoryAdapter);

        if(FragmentMainActivity.locationTxt.getText().toString().equals(getString(R.string.world_wide))) {
            conditionBar.setEnabled(false);
        }else{
            conditionBar.setEnabled(true);
        }

        ViewTreeObserver observer = conditionBar.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.v("conditionBar.getRight()", "conditionBar.getRight()==" + conditionBar.getRight());
                if (conditionBar.getRight() != 0) {
                    seekText(Integer.parseInt(distance));
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        conditionBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        conditionBar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });

    }

    private void setPostedWithin(String type){
        last24Txt.setTextColor(primaryText);
        last7Txt.setTextColor(primaryText);
        last30Txt.setTextColor(primaryText);
        allproductTxt.setTextColor(primaryText);

        last24Next.setVisibility(View.GONE);
        last7Next.setVisibility(View.GONE);
        last30Next.setVisibility(View.GONE);
        allproductNext.setVisibility(View.GONE);

        last24Next.setColorFilter(primaryText);
        last7Next.setColorFilter(primaryText);
        last30Next.setColorFilter(primaryText);
        allproductNext.setColorFilter(primaryText);
        postedTxt = "";
        switch (type){
            case "last24h":
                postedTxt = getString(R.string.last24h);
                last24Txt.setTextColor(colorPrimary);
                last24Next.setVisibility(View.VISIBLE);
                last24Next.setColorFilter(colorPrimary);
                break;
            case "last7d":
                postedTxt = getString(R.string.last7d);
                last7Txt.setTextColor(colorPrimary);
                last7Next.setVisibility(View.VISIBLE);
                last7Next.setColorFilter(colorPrimary);
                break;
            case "last30d":
                postedTxt = getString(R.string.last30d);
                last30Txt.setTextColor(colorPrimary);
                last30Next.setVisibility(View.VISIBLE);
                last30Next.setColorFilter(colorPrimary);
                break;
            case "all":
                postedTxt = getString(R.string.all);
                allproductTxt.setTextColor(colorPrimary);
                allproductNext.setVisibility(View.VISIBLE);
                allproductNext.setColorFilter(colorPrimary);
                break;
        }
    }

    /** for change the seekbar progreess **/
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.v("onprogreschange", "onprogresschange");
        conditionBar.setProgress(progress);
        if (progress == 0) {
            //  conditionBar.setThumb(getResources().getDrawable(R.drawable.seekthumb));
            home.setBackgroundResource(R.drawable.f_hme);
            road.setBackgroundResource(R.drawable.f_road);
            seektext.setVisibility(View.GONE);
        } else {
            conditionBar.setThumb(getResources().getDrawable(R.drawable.green_seek));
            home.setBackgroundResource(R.drawable.f_hme_select);
            road.setBackgroundResource(R.drawable.f_road_select);
            seektext.setVisibility(View.VISIBLE);
        }
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        p.addRule(RelativeLayout.ABOVE, seekBar.getId());
        Rect thumbRect = conditionBar.getSeekBarThumb().getBounds();
        p.setMargins(
                thumbRect.centerX(), 0, 0, 0);
       /* textView.setLayoutParams(p);
        textView.setText(String.valueOf(progress) + " ft.");*/

        seektext.bringToFront();
        seekText(progress);
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        distance = String.valueOf(seekBar.getProgress());
    }

    /** for change the seekbar progress text **/
    private void seekText(int how_many) {
        String what_to_say = String.valueOf(how_many);
        seektext.setText(what_to_say + " MI");

        int seek_label_pos = (((conditionBar.getRight() - conditionBar.getLeft()) * conditionBar.getProgress()) / conditionBar.getMax()) + conditionBar.getLeft();
        Log.v("xvalue", "xvalue=" + seek_label_pos);
        if (seek_label_pos == 0) {
            float xvalue = distanceX - seektext.getWidth() / 2;
            seektext.setX(xvalue);
        } else {
            float xvalue = seek_label_pos - seektext.getWidth() / 2;
            distanceX = seek_label_pos;
            seektext.setX(xvalue);
        }


    }

    /** class for get the catergory from admin **/
    class getCategory extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            try{
                String SOAP_ACTION = Constants.NAMESPACE + Constants.API_CATEGORY;

                SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_CATEGORY);
                req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);

                SOAPParsing soap = new SOAPParsing();
                String json = soap.getJSONFromUrl(SOAP_ACTION, req);
                Log.v("res", "res=" + json);
                JSONObject jobj =new JSONObject(json);
                String response = jobj.getString(Constants.TAG_STATUS);

                if (response.equalsIgnoreCase("true")) {

                    JSONObject result = jobj
                            .optJSONObject(Constants.TAG_RESULT);
                    if(!(result==null)){
                        JSONArray category = result.optJSONArray(Constants.TAG_CATEGORY);
                        if(category != null){
                            for(int i=0 ; i<category.length() ; i++){
                                HashMap<String, String> map = new HashMap<String, String>();
                                JSONObject temp = category.getJSONObject(i);
                                String name = DefensiveClass.optString(temp, Constants.TAG_CATEGORYNAME);
                                String id = DefensiveClass.optString(temp, Constants.TAG_CATEGORYID);
                                String image = DefensiveClass.optString(temp, Constants.TAG_CATEGORYIMG);

                                map.put(Constants.TAG_CATEGORYNAME, name);
                                map.put(Constants.TAG_CATEGORYID, id);
                                map.put(Constants.TAG_CATEGORYIMG, image);

                                categoryAry.add(map);

                                ArrayList<HashMap<String,String>> tempAry = new ArrayList<HashMap<String,String>>();
                                JSONArray subcategory = temp.optJSONArray(Constants.TAG_SUBCATEGORY);
                                for (int j = 0; j<subcategory.length() ; j++){
                                    HashMap<String, String> smap = new HashMap<String, String>();
                                    JSONObject stemp = subcategory.getJSONObject(j);

                                    String subid = DefensiveClass.optString(stemp, Constants.TAG_SUBID);
                                    String subname = DefensiveClass.optString(stemp, Constants.TAG_SUBNAME);

                                    smap.put(Constants.TAG_SUBID, subid);
                                    smap.put(Constants.TAG_SUBNAME, subname);

                                    tempAry.add(smap);
                                }
                                HashMap<String, String> tmap = new HashMap<String, String>();
                                tmap.put(Constants.TAG_SUBID, "all");
                                tmap.put(Constants.TAG_SUBNAME, getString(R.string.all));
                                tempAry.add(0, tmap);

                                subcategAry.add(tempAry);
                            }
                        }
                    }
                }
            } catch(JSONException e){
                e.printStackTrace();
            } catch(NullPointerException e){
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            saveLay.setVisibility(View.GONE);
            mainLay.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.setVisibility(View.GONE);
            saveLay.setVisibility(View.VISIBLE);
            mainLay.setVisibility(View.VISIBLE);
            if (categoryAry.size() == 0) {
              Toast.makeText(SearchAdvance.this, getString(R.string.category_problem), Toast.LENGTH_SHORT).show();
            }
            Log.v("categoryAry", "categoryAry="+ categoryAry);
            Log.v("subcategAry", "subcategAry="+ subcategAry);
        //    categoryAdapter.notifyDataSetChanged();
        }

    }

    public class CategoryAdapter extends BaseAdapter {

        private Context mContext;
        ArrayList<HashMap<String, String>> datas;
        ArrayList<ArrayList<HashMap<String, String>>> subcateg = new ArrayList<ArrayList<HashMap<String, String>>>();
        ViewHolder holder = null;

        public CategoryAdapter(Context ctx, ArrayList<HashMap<String, String>> data, ArrayList<ArrayList<HashMap<String, String>>> subcat) {
            mContext = ctx;
            datas = data;
            subcateg = subcat;
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
            ImageView tick, next;
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
                holder.next = (ImageView) convertView.findViewById(R.id.next);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final HashMap<String, String> map = datas.get(position);

                holder.name.setText(map.get(Constants.TAG_CATEGORYNAME));
                holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                holder.tick.setVisibility(View.INVISIBLE);
                holder.next.setVisibility(View.INVISIBLE);

                if (subcateg.get(position).size() == 1){
                    if (categoryId.contains(map.get(Constants.TAG_CATEGORYID))){
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.next.setVisibility(View.INVISIBLE);
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.next.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (categoryId.contains(map.get(Constants.TAG_CATEGORYID))){
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.next.setVisibility(View.INVISIBLE);
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.next.setVisibility(View.VISIBLE);
                    }
                }

                holder.mainLay.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (subcateg.get(position).size() == 1) {
                            if (categoryId.contains(map.get(Constants.TAG_CATEGORYID))) {
                                categoryId.remove(map.get(Constants.TAG_CATEGORYID));
                                categoryName.remove(map.get(Constants.TAG_CATEGORYNAME));
                            } else {
                                categoryId.add(map.get(Constants.TAG_CATEGORYID));
                                categoryName.add(map.get(Constants.TAG_CATEGORYNAME));
                            }
                            categoryAdapter.notifyDataSetChanged();
                        } else {
                            if (subcategoryId.get(map.get(Constants.TAG_CATEGORYID)) == null){
                                subcategoryId.put(map.get(Constants.TAG_CATEGORYID), "");
                            }
                            Intent i = new Intent(SearchAdvance.this, SubCategory.class);
                            i.putExtra("from" , "filter");
                            i.putExtra("categoryName", map.get(Constants.TAG_CATEGORYNAME));
                            i.putExtra("categoryId", map.get(Constants.TAG_CATEGORYID));
                            i.putExtra("data", subcateg.get(position));
                            startActivity(i);
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

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(SearchAdvance.this);

        if (categoryAdapter != null){
            categoryAdapter.notifyDataSetChanged();
        }
    }

    /** set selected sort by **/
    private void setSortBy(String type){
        popularTxt.setTextColor(primaryText);
        urgentTxt.setTextColor(primaryText);
        highTxt.setTextColor(primaryText);
        lowTxt.setTextColor(primaryText);

        popularNext.setVisibility(View.GONE);
        urgentNext.setVisibility(View.GONE);
        highNext.setVisibility(View.GONE);
        lowNext.setVisibility(View.GONE);

        popularNext.setColorFilter(primaryText);
        urgentNext.setColorFilter(primaryText);
        highNext.setColorFilter(primaryText);
        lowNext.setColorFilter(primaryText);
        sortTxt = "";
        switch (type){
            case "2":
                sortTxt = getString(R.string.popular);
                popularTxt.setTextColor(colorPrimary);
                popularNext.setVisibility(View.VISIBLE);
                popularNext.setColorFilter(colorPrimary);
                break;
            case "3":
                sortTxt = getString(R.string.hightlow);
                highTxt.setTextColor(colorPrimary);
                highNext.setVisibility(View.VISIBLE);
                highNext.setColorFilter(colorPrimary);
                break;
            case "4":
                sortTxt = getString(R.string.lowthigh);
                lowTxt.setTextColor(colorPrimary);
                lowNext.setVisibility(View.VISIBLE);
                lowNext.setColorFilter(colorPrimary);
                break;
            case "5":
                sortTxt = getString(R.string.urgent);
                urgentTxt.setTextColor(colorPrimary);
                urgentNext.setVisibility(View.VISIBLE);
                urgentNext.setColorFilter(colorPrimary);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(SearchAdvance.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.locationLay:
                Intent i = new Intent(SearchAdvance.this, LocationActivity.class);
                i.putExtra("from", "home");
                startActivity(i);
                break;
            case R.id.last24Txt:
            case R.id.last24Next:
                postedWithin="last24h";
                setPostedWithin(postedWithin);
                break;
            case R.id.last7Txt:
            case R.id.last7Next:
                postedWithin="last7d";
                setPostedWithin(postedWithin);
                break;
            case R.id.last30Txt:
            case R.id.last30Next:
                postedWithin="last30d";
                setPostedWithin(postedWithin);
                break;
            case R.id.allproductTxt:
            case R.id.allproductNext:
                postedWithin="all";
                setPostedWithin(postedWithin);
                break;
            case R.id.popularTxt:
            case R.id.popularNext:
                sortBy = "2";
                setSortBy(sortBy);
                break;
            case R.id.urgentTxt:
            case R.id.urgentNext:
                sortBy = "5";
                setSortBy(sortBy);
                break;
            case R.id.highTxt:
            case R.id.highNext:
                sortBy = "3";
                setSortBy(sortBy);
                break;
            case R.id.lowTxt:
            case R.id.lowNext:
                sortBy = "4";
                setSortBy(sortBy);
                break;
            case R.id.reset:
                distance = "0";
                distanceX = 0;
                categoryId.clear();
                categoryName.clear();
                subcategoryId.clear();
                postedWithin = "";
                sortBy = "1";
                categoryAdapter.notifyDataSetChanged();
                setPostedWithin(postedWithin);
                setSortBy(sortBy);
                conditionBar.setProgress(Integer.parseInt(distance));
                FragmentMainActivity.filterAry.clear();
                if(FragmentMainActivity.filterAdapter!=null)
                    FragmentMainActivity.filterAdapter.notifyDataSetChanged();
                applyFilter = true;
                break;
            case R.id.apply:
                //categoryName.clear();
                FragmentMainActivity.filterAry.clear();
                /*for (int n = 0; n < categoryAry.size(); n++){
                    for (int m = 0; m < categoryId.size(); m++){
                        if (categoryId.get(m).equals(categoryAry.get(n).get(Constants.TAG_CATEGORYID))){
                            String name = categoryAry.get(n).get(Constants.TAG_CATEGORYNAME);
                            categoryName.add(name);
                        }
                    }
                }*/
                applyFilter = true;
                finish();
                Intent k = new Intent(SearchAdvance.this, FragmentMainActivity.class);
                startActivity(k);
                break;

        }

    }

}
