package com.app.textbooktakeover;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.external.GPSTracker;
import com.app.external.HorizontalListView;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AddProductDetail extends AppCompatActivity implements View.OnClickListener {

    ImageView backBtn, cancelIcon;
    TextView title, cancel, post, promote, alert_title, uploadStatus;
    EditText productName, productDes, price, paypalId, shippingFee;
    Spinner currency;
    SwitchCompat chatSwitch, exchangeSwitch, buySwitch;
    ProgressBar loadingProgress;
    HorizontalListView imageList;
    LinearLayout parentLay, saveLay, categoryLay, locationLay;
    RelativeLayout uploadSuccessLay, imageLoadingLay;
    AVLoadingIndicatorView loadingView, postProgress;
    String from = "", currencyid = "", productUrl = "", postPrice = "", posteditemId = "";
    String bookTitle, author, isbn, description, date;
    Integer stars;
    Bitmap img;
    public static HashMap<String, String> itemMap = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> categAry = new ArrayList<HashMap<String, String>>();
    ArrayList<ArrayList<HashMap<String, String>>> subcategAry = new ArrayList<ArrayList<HashMap<String, String>>>();
    ArrayList<HashMap<String, String>> conditionAry = new ArrayList<HashMap<String, String>>();
    private ArrayList<String> currencyID = new ArrayList<String>();
    private ArrayList<String> currencyspin = new ArrayList<String>();
    ArrayList<String> uploadedImage = new ArrayList<String>();
    ImagesAdapter imagesAdapter;
    ArrayAdapter currencyadapter;
    Dialog dialog;
    int count;
    public static LinearLayout bottomLay, conditionLay, instantLay, buynowLay;
    public static RelativeLayout exchangeLay, offerLay;
    public static String itemCond = "", loc = "", categId = "", subcategId = "", mcountryId = "";
    public static TextView itemCondition, location, category;
    public static ImageView condArrow, locArrow, catArrow;
    public static double lat, lon;
    public static ArrayList<HashMap<String, Object>> images = new ArrayList<HashMap<String, Object>>();
    ArrayList<String> pathsAry = new ArrayList<String>();
    ArrayList<String> removeAry = new ArrayList<String>();
    private ArrayList<String> countryId = new ArrayList<String>();
    private ArrayList<String> countryName = new ArrayList<String>();
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addproduct_detail);

        // Elements declaration
        backBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        cancel = (TextView) findViewById(R.id.cancel);
        post = (TextView) findViewById(R.id.post);
        productName = (EditText) findViewById(R.id.productName);
        productDes = (EditText) findViewById(R.id.productDes);
        price = (EditText) findViewById(R.id.price);
        currency = (Spinner) findViewById(R.id.currency);
        chatSwitch = (SwitchCompat) findViewById(R.id.chatSwitch);
        exchangeSwitch = (SwitchCompat) findViewById(R.id.exchangeSwitch);
        imageList = (HorizontalListView) findViewById(R.id.imageList);
        parentLay = (LinearLayout) findViewById(R.id.parentLay);
        saveLay = (LinearLayout) findViewById(R.id.saveLay);
        loadingView = (AVLoadingIndicatorView) findViewById(R.id.progress);
        catArrow = (ImageView) findViewById(R.id.catArrow);
        condArrow = (ImageView) findViewById(R.id.condArrow);
        categoryLay = (LinearLayout) findViewById(R.id.categoryLay);
        conditionLay = (LinearLayout) findViewById(R.id.conditionLay);
        locationLay = (LinearLayout) findViewById(R.id.locationLay);
        exchangeLay = (RelativeLayout) findViewById(R.id.exchangeLay);
        offerLay = (RelativeLayout) findViewById(R.id.offerLay);
        location = (TextView) findViewById(R.id.location);
        itemCondition = (TextView) findViewById(R.id.itemCondition);
        category = (TextView) findViewById(R.id.category);
        locArrow = (ImageView) findViewById(R.id.locArrow);
        bottomLay = (LinearLayout) findViewById(R.id.bottomLay);
        instantLay = (LinearLayout) findViewById(R.id.instantLay);
        buySwitch = (SwitchCompat) findViewById(R.id.buySwitch);
        paypalId = (EditText) findViewById(R.id.paypalId);
        shippingFee = (EditText) findViewById(R.id.shippingFee);
        buynowLay = (LinearLayout) findViewById(R.id.buynowLay);

        title.setText(getString(R.string.add_your_stuff));
        Bundle i = getIntent().getExtras();
        from = i.getString("from");
//        i.getString("isbn", tag);
        bookTitle = (String) i.getString("title");
        author = (String) i.getString("author");
        description = (String) i.getString("description");
        date = (String) i.getString("date");
        stars = (Integer) i.getInt("stars");
        img = (Bitmap) i.getParcelable("image");
        if (from != null && from.equals("scanner")) {
            loadData();
        } else if (from.equals("AddProduct")) {
            productDes.setText((String) i.getString("productDes"));
            productName.setText((String) i.getString("productName"));
        }
        //Elements visibility
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        //Elements set listener
        backBtn.setOnClickListener(this);
        cancel.setOnClickListener(this);
        post.setOnClickListener(this);
        conditionLay.setOnClickListener(this);
        locationLay.setOnClickListener(this);
        categoryLay.setOnClickListener(this);
        productName.addTextChangedListener(new addListenerOnTextChange(this, productName));
        productDes.addTextChangedListener(new addListenerForDes(this, productDes));

        new GetCategories().execute();

        if (!from.equals("edit")) {
            setLocationTxt();
            setImageAdapter();
        }

        TextbookTakeoverApplication.setupUI(AddProductDetail.this, parentLay);

        // Init Dialog
        dialog = new Dialog(AddProductDetail.this, R.style.PostDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.product_upload_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        promote = (TextView) dialog.findViewById(R.id.promote);
        alert_title = (TextView) dialog.findViewById(R.id.alert_title);
        uploadStatus = (TextView) dialog.findViewById(R.id.uploadStatus);
        cancelIcon = (ImageView) dialog.findViewById(R.id.cancelIcon);
        loadingProgress = (ProgressBar) dialog.findViewById(R.id.loadingProgress);
        postProgress = (AVLoadingIndicatorView) dialog.findViewById(R.id.postProgress);
        uploadSuccessLay = (RelativeLayout) dialog.findViewById(R.id.uploadSuccessLay);
        imageLoadingLay = (RelativeLayout) dialog.findViewById(R.id.imageLoadingLay);

        //Dialog elements listener
        promote.setOnClickListener(this);
        cancelIcon.setOnClickListener(this);

        buySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buynowLay.setVisibility(View.VISIBLE);
                } else {
                    buynowLay.setVisibility(View.GONE);
                }
            }
        });

        if (savedInstanceState != null) {
            loadData();
        }
    }

    private void loadData() {
        String desc = "";
        if (bookTitle != null) {
            productName.setText(bookTitle.substring(7));
        }
        if (author != null) {
            desc += (author + "\n");
        }
//        if(description != null){
//            desc += (description + "\n");
//        }
        if (date != null) {
            desc += (date + "\n");
        }
        if (stars != null) {
            if (stars != 0)
                desc += (stars + " Stars" + "\n");
            else
                desc += "No Rating";
        }
        if (img != null) {
//            desc += (stars + " Stars" + "\n");
        }
        productDes.setText(desc);
    }

    /**
     * Function for set current location from Gps
     **/
    private void setLocationTxt() {
        GPSTracker gps = new GPSTracker(AddProductDetail.this);
        if (lat == 0 && lon == 0) {
            if (gps.canGetLocation()) {
                if (TextbookTakeoverApplication.isNetworkAvailable(AddProductDetail.this)) {
                    lat = gps.getLatitude();
                    lon = gps.getLongitude();
                    Log.v("lati", "lat" + lat);
                    Log.v("longi", "longi" + lon);
                    new GetLocationAsync(lat, lon).execute();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedBundle) {
        savedBundle.putString("title", "" + bookTitle);
        savedBundle.putString("author", "" + author);
        savedBundle.putString("description", "" + description);
        savedBundle.putString("date", "" + date);
        savedBundle.putString("stars", "" + stars.toString());
        savedBundle.putParcelable("thumbPic", img);
        super.onSaveInstanceState(savedBundle);
    }

    class GetCategories extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_PRODUCT_BEFORE_ADD;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_PRODUCT_BEFORE_ADD);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);

            SOAPParsing soap = new SOAPParsing();
            String jsonString = soap.getJSONFromUrl(SOAP_ACTION, req);

            String stats;
            try {
                JSONObject json = new JSONObject(jsonString);
                stats = json.getString(Constants.TAG_STATUS);
                if (stats.equalsIgnoreCase("true")) {
                    JSONObject res = json.getJSONObject("result");

                    JSONArray category = res.getJSONArray("category");
                    for (int i = 0; i < category.length(); i++) {
                        JSONObject jcat = category.getJSONObject(i);
                        String id = DefensiveClass.optString(jcat, "category_id");
                        String name = DefensiveClass.optString(jcat, "category_name");
                        String image = DefensiveClass.optString(jcat, "category_img");
                        String product_condition = DefensiveClass.optString(jcat, "product_condition");
                        String exchange_buy = DefensiveClass.optString(jcat, "exchange_buy");
                        String make_offer = DefensiveClass.optString(jcat, "make_offer");
                        String instant_buy = DefensiveClass.optString(jcat, "instant_buy");

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("name", name);
                        map.put("id", id);
                        map.put("image", image);
                        map.put("product_condition", product_condition);
                        map.put("exchange_buy", exchange_buy);
                        map.put("make_offer", make_offer);
                        map.put("instant_buy", instant_buy);
                        categAry.add(map);

                        ArrayList<HashMap<String, String>> subTemp = new ArrayList<HashMap<String, String>>();
                        JSONArray subcat = jcat.getJSONArray("subcategory");
                        for (int x = 0; x < subcat.length(); x++) {
                            JSONObject scat = subcat.getJSONObject(x);
                            String subid = scat.getString("sub_id");
                            String subname = scat.getString("sub_name");

                            HashMap<String, String> smap = new HashMap<String, String>();
                            smap.put("name", subname);
                            smap.put("id", subid);

                            subTemp.add(smap);
                        }
                        subcategAry.add(subTemp);
                    }

                    JSONArray currency = res.getJSONArray("currency");
                    currencyID.add("dollar");
                    currencyspin.add("USD $");
//                    for (int i = 0; i < currency.length(); i++) {
//                        JSONObject jcur = currency.getJSONObject(i);
//
//                        currencyID.add(jcur.getString("id"));
//                        currencyspin.add(jcur.getString("symbol"));
//                    }

                    JSONArray productCondition = res.getJSONArray("product_condition");
                    for (int i = 0; i < productCondition.length(); i++) {
                        JSONObject jcur = productCondition.getJSONObject(i);

                        String name = DefensiveClass.optString(jcur, "name");
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("name", name);
                        conditionAry.add(map);
                    }

                    JSONArray country = res.getJSONArray(Constants.TAG_COUNTRY);
                    for (int i = 0; i < country.length(); i++) {
                        JSONObject jobj = country.getJSONObject(i);

                        String counId = DefensiveClass.optString(jobj, Constants.TAG_COUNTRYID);
                        if (!counId.equals("0")) {
                            countryId.add(counId);
                            countryName.add(DefensiveClass.optString(jobj, Constants.TAG_COUNTRYNAME));
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
            super.onPreExecute();

            parentLay.setVisibility(View.GONE);
            saveLay.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            currencyadapter = new ArrayAdapter<String>(AddProductDetail.this, R.layout.spinner_item, currencyspin);
            currencyadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currency.setAdapter(currencyadapter);

            currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    //   currencyid = currencyID.get(position);
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorPrimary));

                    String selectedCurrency = currencyspin.get(position);
                    if (selectedCurrency.contains("-")) {
                        String cur[] = selectedCurrency.split("-");
                        currencyid = cur[1] + "-" + cur[0];
                    } else {
                        currencyid = "";
                    }
                    Log.v("currencyid", "" + currencyid);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            try {
                Log.v("from", "from=" + from);
                if (from.equals("edit")) {
                    setEditProducts();
                    setImageAdapter();
                }
                setCategoryConditions();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            parentLay.setVisibility(View.VISIBLE);
            saveLay.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);
        }
    }

    /**
     * Function for set already edited datas
     **/
    private void setEditProducts() {
        try {
            itemMap.clear();
            itemMap = (HashMap<String, String>) getIntent().getExtras().get("data");
            Log.v("itemMap", "itemMap" + itemMap);
            productName.setText(itemMap.get(Constants.TAG_TITLE));
            //    productName.setSelection(itemMap.get(Constants.TAG_TITLE).length() - 1);
            productDes.setText(itemMap.get(Constants.TAG_ITEM_DES));
            price.setText(itemMap.get(Constants.TAG_PRICE));
            category.setText(itemMap.get(Constants.TAG_CATEGORYNAME));
            category.setTextColor(getResources().getColor(R.color.primaryText));
            catArrow.setColorFilter(getResources().getColor(R.color.primaryText));
            categId = itemMap.get(Constants.TAG_CATEGORYID);
            Log.v("categId", "categId=" + categId);
            subcategId = itemMap.get(Constants.TAG_SUBCATEGORYID);
            location.setText(itemMap.get(Constants.TAG_LOCATION));
            location.setTextColor(getResources().getColor(R.color.primaryText));
            locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
            loc = itemMap.get(Constants.TAG_LOCATION);
            try {
                lat = Double.parseDouble(itemMap.get(Constants.TAG_LATITUDE));
                lon = Double.parseDouble(itemMap.get(Constants.TAG_LONGITUDE));
            } catch (NullPointerException | NumberFormatException e) {
                e.printStackTrace();
                lat = 0;
                lon = 0;
            } catch (Exception e) {
                e.printStackTrace();
                lat = 0;
                lon = 0;
            }
            if (itemMap.get(Constants.TAG_EXCHANGE_BUY).equals("1")) {
                exchangeSwitch.setChecked(true);
            } else {
                exchangeSwitch.setChecked(false);
            }
            if (itemMap.get(Constants.TAG_MAKE_OFFER).equals("1")) {
                chatSwitch.setChecked(true);
            } else {
                chatSwitch.setChecked(false);
            }
            if (itemMap.get(Constants.TAG_INSTANT_BUY).equals("1")) {
                buySwitch.setChecked(true);
            } else {
                buySwitch.setChecked(false);
            }

            itemCond = itemMap.get(Constants.TAG_ITEM_CONDITION);
            if (itemMap.get(Constants.TAG_ITEM_CONDITION).equals("0")) {
                itemCond = "";
            }
            itemCondition.setText(itemCond);
            itemCondition.setTextColor(getResources().getColor(R.color.primaryText));
            condArrow.setColorFilter(getResources().getColor(R.color.primaryText));
            paypalId.setText(itemMap.get(Constants.TAG_PAYPALID));
            shippingFee.setText(itemMap.get(Constants.TAG_SHIPPING_COST));
            currency.setSelection(getIndex(currency, itemMap.get(Constants.TAG_CURRENCY_CODE)));
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * For setting  product condition depends on category
     **/
    private void setCategoryConditions() {
        bottomLay.setVisibility(View.GONE);
        instantLay.setVisibility(View.GONE);
        Log.v("categoryId", "categoryId=" + categId);
        if (categAry.size() > 0) {
            for (int i = 0; i < categAry.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map = categAry.get(i);
                if (map.get("id").equals(categId)) {
                    if (map.get("product_condition").equals("disable") && map.get("exchange_buy").equals("disable")
                            && map.get("make_offer").equals("disable")) {
                        bottomLay.setVisibility(View.GONE);
                        conditionLay.setVisibility(View.GONE);
                        exchangeLay.setVisibility(View.GONE);
                        offerLay.setVisibility(View.GONE);
                    } else {
                        bottomLay.setVisibility(View.VISIBLE);
                        if (map.get("product_condition").equals("enable")) {
                            conditionLay.setVisibility(View.VISIBLE);
                        } else {
                            conditionLay.setVisibility(View.GONE);
                        }

                        if (map.get("exchange_buy").equals("enable")) {
                            if (Constants.EXCHANGE) {
                                exchangeLay.setVisibility(View.VISIBLE);
                            } else {
                                exchangeLay.setVisibility(View.GONE);
                            }
                        } else {
                            exchangeLay.setVisibility(View.GONE);
                        }

                        if (map.get("make_offer").equals("enable")) {
                            offerLay.setVisibility(View.VISIBLE);
                        } else {
                            offerLay.setVisibility(View.GONE);
                        }
                    }

                    if (map.get("instant_buy").equals("disable")) {
                        instantLay.setVisibility(View.GONE);
                    } else {
                        if (Constants.BUYNOW) {
                            instantLay.setVisibility(View.VISIBLE);
                            if (buySwitch.isChecked()) {
                                buynowLay.setVisibility(View.VISIBLE);
                            } else {
                                buynowLay.setVisibility(View.GONE);
                            }
                        } else {
                            instantLay.setVisibility(View.GONE);
                        }
                    }
                    break;
                }
            }
        }
    }

    private int getIndex(Spinner spinner, String myString) {

        int index = 0;
        if (myString.contains("-")) {
            String cur[] = myString.split("-");
            myString = cur[1] + "-" + cur[0];
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        Log.v("index spin=" + spinner.getCount(), "index=" + myString + "==" + index);
        return index;
    }

    /**
     * For set images to listview
     **/
    private void setImageAdapter() {
        try {
            if (from.equals("edit")) {
                if (itemMap.get(Constants.TAG_PHOTOS).equals("") || itemMap.get(Constants.TAG_PHOTOS).equals(null)) {
                    Log.v("photosss", "photos emptyyyy");
                } else {
                    JSONArray photos = new JSONArray(itemMap.get(Constants.TAG_PHOTOS));
                    for (int i = 0; i < photos.length(); i++) {
                        JSONObject jph = photos.getJSONObject(i);
                        String imageurl = DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_350);
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("type", "url");
                        map.put("image", imageurl);
                        if (!images.contains(map)) {
                            images.add(map);
                        }
                    }
                    images.addAll(CameraActivity.images);
                    CameraActivity.images.clear();
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("type", "add");
                    if (images.contains(map)) {
                        images.remove(map);
                    }
                    images.add(map);
                    imagesAdapter = new ImagesAdapter(AddProductDetail.this, images);
                    imageList.setAdapter(imagesAdapter);
                }
            } else {
                images.addAll(CameraActivity.images);
                CameraActivity.images.clear();
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("type", "add");
                if (images.contains(map)) {
                    images.remove(map);
                }
                images.add(map);
                imagesAdapter = new ImagesAdapter(AddProductDetail.this, images);
                imageList.setAdapter(imagesAdapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class ImagesAdapter extends BaseAdapter {

        private Context mContext;
        ArrayList<HashMap<String, Object>> imgAry;

        public ImagesAdapter(Context ctx2, ArrayList<HashMap<String, Object>> data) {
            mContext = ctx2;
            imgAry = data;
        }

        @Override
        public int getCount() {
            return imgAry.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.addproduct_image, parent, false);//layout
            } else {
                view = convertView;
                view.forceLayout();
            }
            Log.v("position", "" + position);
            try {
                ImageView singleImage = (ImageView) view.findViewById(R.id.imageView);
                ImageView delete = (ImageView) view.findViewById(R.id.delete);

                final HashMap<String, Object> tempMap = imgAry.get(position);

                if (tempMap.get("type").equals("add")) {
                    delete.setVisibility(View.GONE);
                    singleImage.setImageResource(R.drawable.plus_sign);
                    singleImage.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            CameraActivity.fromedit = true;
//                            finish();
                            Intent i = new Intent(AddProductDetail.this, CameraActivity.class);
                            i.putExtra("from", "AddProduct");
                            i.putExtra("productName", productName.getText().toString());
                            i.putExtra("productDes", productDes.getText().toString());
                            finish();
                            startActivity(i);
                        }
                    });
                } else if (tempMap.get("type").equals("path")) {
                    delete.setVisibility(View.VISIBLE);
                    singleImage.setImageBitmap(TextbookTakeoverApplication.getResizedBitmap((Bitmap) tempMap.get("image"), 70));
                } else {
                    delete.setVisibility(View.VISIBLE);
                    Picasso.with(AddProductDetail.this).load(tempMap.get("image").toString()).into(singleImage);
                }

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (images.size() > 0) {
                            if (tempMap.get("type").equals("url")) {
                                String imageurl = tempMap.get("image").toString();
                                imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                                removeAry.add(imageurl);
                            }
                            images.remove(tempMap);
                        }
                        imagesAdapter.notifyDataSetChanged();
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return view;
        }

    }

    /**
     * function for update the captured image
     **/
    public File saveBitmapToFile(File file) {
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 2;
            o.inPreferredConfig = Bitmap.Config.RGB_565;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 1024;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPreferredConfig = Bitmap.Config.RGB_565;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file

            //file.createNewFile();
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/" + getString(R.string.app_name));
            dir.mkdirs();
            file = new File(dir, String.valueOf(System.currentTimeMillis()) + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            galleryAddPic(file.toString());
            outputStream.flush();
            outputStream.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void galleryAddPic(String file) {
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        sendBroadcast(mediaScanIntent);
    }

    /**
     * class for uploading images to server
     **/
    class moreLoadItems extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "";
        String status;

        @Override
        protected Integer doInBackground(String... imgpath) {
            for (int i = 0; i < count; i++) {
                Log.v("i", "" + i);
                publishProgress(Math.min(i, count));

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                DataInputStream inStream = null;
                StringBuilder builder = new StringBuilder();
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                String urlString = Constants.API_UPLOAD_ITEM;
                try {
                    String exsistingFileName = imgpath[i];
                    Log.v(" exsistingFileName", exsistingFileName);
                    FileInputStream fileInputStream = new FileInputStream(saveBitmapToFile(new File(exsistingFileName)));
                    URL url = new URL(urlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);
                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                            + exsistingFileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    Log.e("MediaPlayer", "Headers are written");
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    Log.v("buffer", "buffer" + buffer);

                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        Log.v("bytesRead", "bytesRead" + bytesRead);
                    }
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String inputLine;
                    Log.v("in", "" + in);
                    while ((inputLine = in.readLine()) != null)
                        builder.append(inputLine);

                    Log.e("MediaPlayer", "File is written");
                    fileInputStream.close();
                    Json = builder.toString();
                    dos.flush();
                    dos.close();

                } catch (MalformedURLException ex) {
                    Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
                } catch (IOException ioe) {
                    Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
                }
                try {
                    inStream = new DataInputStream(conn.getInputStream());
                    String str;
                    while ((str = inStream.readLine()) != null) {
                        Log.e("MediaPlayer", "Server Response" + str);
                    }
                    inStream.close();
                } catch (IOException ioex) {
                    Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
                }
                try {
                    jsonobject = new JSONObject(Json);
                    Log.v("json", "json" + Json);
                    status = jsonobject.getString("status");
                    if (status.equals("true")) {
                        JSONObject image = jsonobject.getJSONObject("Image");
                        String msg = image.getString("Message");
                        String uploadedimgname = image.getString("Name");
                        uploadedImage.add(uploadedimgname);
                    }

                } catch (JSONException e) {
                    status = "false";
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    status = "false";
                    e.printStackTrace();
                } catch (Exception e) {
                    status = "false";
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            loadingProgress.setProgress(0);

        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.v("values[0]", "" + values[0]);
            loadingProgress.setProgress(values[0]);
            uploadStatus.setText(values[0] + " " + getString(R.string.of) + " " + count + getString(R.string.image_uploaded));
        }

        @Override
        protected void onPostExecute(Integer unused) {
            if (status.equals("true")) {
                loadingProgress.setProgress(count);
                uploadStatus.setText(count + " " + getString(R.string.of) + " " + count + getString(R.string.image_uploaded));
                alert_title.setText(getString(R.string.posting_list));
                loadingProgress.setVisibility(View.GONE);
                postProgress.setVisibility(View.VISIBLE);
                uploadStatus.setVisibility(View.GONE);
                if (from.equals("edit") && uploadedImage.size() > 0) {
                    ArrayList<String> tempAry = new ArrayList<String>();
                    tempAry.addAll(uploadedImage);
                    uploadedImage.clear();
                    int index = 0;
                    for (int i = 0; i < images.size(); i++) {
                        if (images.get(i).get("type").equals("url")) {
                            String imageurl = images.get(i).get("image").toString();
                            imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                            uploadedImage.add(imageurl);
                        } else if (images.get(i).get("type").equals("path")) {
                            String imageurl = tempAry.get(index);
                            uploadedImage.add(imageurl);
                            index++;
                        }
                    }
                }
                new SendProducts().execute();
            } else {
                TextbookTakeoverApplication.dialog(AddProductDetail.this, getString(R.string.alert), getString(R.string.image_cannot));
            }
        }
    }

    /**
     * class for add or edit a product
     **/
    class SendProducts extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... arg0) {
            String result = postData();
            return result;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jonj = new JSONObject(result);
                if (jonj.getString(Constants.TAG_STATUS).equalsIgnoreCase(
                        "true")) {
                    productUrl = DefensiveClass.optString(jonj, "product_url");
                    posteditemId = DefensiveClass.optString(jonj, "item_id");
                    String promotionType = DefensiveClass.optString(jonj, "promotion_type");
                    uploadSuccessLay.setVisibility(View.VISIBLE);
                    imageLoadingLay.setVisibility(View.INVISIBLE);
                    CameraActivity.images.clear();
                    images.clear();
                    if (promotionType.equalsIgnoreCase("Normal") && itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("onsale")) {
                        if (Constants.PROMOTION) {
                            promote.setVisibility(View.VISIBLE);
                        } else {
                            promote.setVisibility(View.GONE);
                        }
                    } else {
                        promote.setVisibility(View.GONE);
                    }
                    if (from.equals("edit")) {
                        DetailActivity.fromEdit = true;
                    }
                } else {
                    TextbookTakeoverApplication.dialog(AddProductDetail.this, getString(R.string.alert), getString(R.string.your_product_not));
                }
            } catch (JSONException e) {
                TextbookTakeoverApplication.dialog(AddProductDetail.this, getString(R.string.alert), getString(R.string.your_product_not));
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String postData() {
        String result = null;

        try {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_POST_PRODUCT;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_POST_PRODUCT);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);

            // Add your data
            if (from.equals("edit")) {
                req.addProperty("item_id", itemMap.get(Constants.TAG_ID));
                req.addProperty("sold", "false");
                req.addProperty("remove_img", removeAry.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
            }
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("item_name", productName.getText().toString().trim());
            req.addProperty("item_des", productDes.getText().toString().trim());
            req.addProperty("quantity", "1");
            req.addProperty("price", postPrice);
            req.addProperty("size", "");
            req.addProperty("category", categId);
            req.addProperty("subcategory", subcategId);
            req.addProperty("product_img", uploadedImage.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
            req.addProperty("shipping_detail", "");
            req.addProperty("shipping_time", "");
            req.addProperty("address", location.getText().toString().trim());
            req.addProperty("lat", Double.toString(lat));
            req.addProperty("lon", Double.toString(lon));
            req.addProperty("currency", currencyid);
            req.addProperty("paypal_id", paypalId.getText().toString().trim());
            req.addProperty("country_id", mcountryId);
            req.addProperty("shipping_cost", shippingFee.getText().toString().trim());
            if (exchangeLay.getVisibility() == View.VISIBLE) {
                if (exchangeSwitch.isChecked()) {
                    req.addProperty("exchange_to_buy", "1");
                } else {
                    req.addProperty("exchange_to_buy", "0");
                }
            } else {
                req.addProperty("exchange_to_buy", "2");
            }

            if (offerLay.getVisibility() == View.VISIBLE) {
                if (chatSwitch.isChecked()) {
                    req.addProperty("make_offer", "1");
                } else {
                    req.addProperty("make_offer", "0");
                }
            } else {
                req.addProperty("make_offer", "2");
            }

            if (conditionLay.getVisibility() == View.VISIBLE) {
                req.addProperty("item_condition", itemCond);
            } else {
                req.addProperty("item_condition", "0");
            }

            if (instantLay.getVisibility() == View.VISIBLE) {
                if (buySwitch.isChecked()) {
                    req.addProperty("instant_buy", "1");
                } else {
                    req.addProperty("instant_buy", "0");
                }
            } else {
                req.addProperty("instant_buy", "2");
            }

            SOAPParsing soap = new SOAPParsing();
            result = soap.getJSONFromUrl(SOAP_ACTION, req);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * for converting lat, lon to address
     **/
    private class GetLocationAsync extends AsyncTask<String, Void, String> {

        // boolean duplicateResponse;
        double x, y;
        StringBuilder str;
        private List<Address> addresses;

        public GetLocationAsync(double latitude, double longitude) {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                Geocoder geocoder = new Geocoder(AddProductDetail.this, Locale.ENGLISH);
                addresses = geocoder.getFromLocation(x, y, 1);
                str = new StringBuilder();
                if (geocoder.isPresent() && addresses.size() > 0) {

                    Address returnAddress = addresses.get(0);

                    String localityString = returnAddress.getLocality();
                    String city = returnAddress.getCountryName();
                    String region_code = returnAddress.getCountryCode();
                    //    String zipcode = returnAddress.getPostalCode();

                    str.append(localityString + "");
                    str.append(city + "" + region_code + "");
                    //    str.append(zipcode + "");

                } else {
                }
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (addresses != null && !addresses.isEmpty()) {
                    loc = addresses.get(0).getAddressLine(0) + ", "
                            + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                    location.setText(loc);
                    location.setTextColor(getResources().getColor(R.color.primaryText));
                    locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    /**
     * class for restrict space and spl characters
     **/
    public static class addListenerOnTextChange implements TextWatcher {
        private Context mContext;
        EditText mEdittextview;

        public addListenerOnTextChange(Context context, EditText edittextview) {
            super();
            this.mContext = context;
            this.mEdittextview = edittextview;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEdittextview.getText().length() > 0) {
                mEdittextview.setError(null);
            }

            String result = s.toString().replaceAll("  ", " ");
            String specialChar = s.toString().replaceAll("[^\\s\\w]*", "");
            //for numbers
            //specialChar = specialChar.replaceAll("[0-9]", "");

            Log.v("special char", "=" + specialChar);
            if (!s.toString().equals(result)) {
                mEdittextview.setText(result);
                mEdittextview.setSelection(result.length());
                // alert the user
            }
            if (!s.toString().equals(specialChar)) {
                mEdittextview.setText(specialChar);
                mEdittextview.setSelection(specialChar.length());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    /**
     * Class for filter the multiple spaces
     **/
    public static class addListenerForDes implements TextWatcher {
        private Context mContext;
        EditText mEdittextview;

        public addListenerForDes(Context context, EditText edittextview) {
            super();
            this.mContext = context;
            this.mEdittextview = edittextview;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEdittextview.getText().length() > 0) {
                mEdittextview.setError(null);
            }

            String result = s.toString().replaceAll("  ", " ");

            if (!s.toString().equals(result)) {
                mEdittextview.setText(result);
                mEdittextview.setSelection(result.length());
                // alert the user
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(AddProductDetail.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(AddProductDetail.this);
        if (ContextCompat.checkSelfPermission(AddProductDetail.this, WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddProductDetail.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("requestCode", "requestCode=" + requestCode);
        if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(AddProductDetail.this, getString(R.string.storage_permission_access), Toast.LENGTH_SHORT).show();
            //finish();
        } else {
            Toast.makeText(AddProductDetail.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void reset() {
        itemCond = "";
        categId = "";
        subcategId = "";
        loc = "";
        lat = 0;
        lon = 0;
        itemMap.clear();
        images.clear();
        CameraActivity.images.clear();
    }

    private String getCountryId() {
        mcountryId = "";
        try {
            String countryname = location.getText().toString();
            Log.v("location", "location=" + countryname);
            if (countryname.contains(",")) {
                countryname = countryname.substring(countryname.lastIndexOf(',') + 1).trim();
            }
            countryname = countryname.trim();
            Log.v("countryname", "countryname=" + countryname);
            if (countryId.size() > 0) {
                int index = countryName.indexOf(countryname);
                Log.v("index", "index=" + index);
                Log.v("countryId", "countryId=" + countryId);
                mcountryId = countryId.get(index);
            }
        } catch (NullPointerException e) {
            mcountryId = "";
            e.printStackTrace();
        } catch (Exception e) {
            mcountryId = "";
            e.printStackTrace();
        }
        return mcountryId;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        reset();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                TextbookTakeoverApplication.hideSoftKeyboard(AddProductDetail.this);
                reset();
                finish();
                break;
            case R.id.cancelIcon:
                dialog.dismiss();
                finish();
                reset();
                break;
            case R.id.cancel:
                finish();
                reset();
                break;
            case R.id.post:
                try {
                    postPrice = price.getText().toString().trim();
                    if (productName.getText().toString().trim().equals("")) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (productDes.getText().toString().trim().equals("")) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (postPrice == null || postPrice.equals("")) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (Integer.parseInt(postPrice) == 0) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.price_should), Toast.LENGTH_SHORT).show();
                    } else if (location.getText().toString().equals(getString(R.string.set_your_location)) ||
                            location.getText().toString().equals("")) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (getCountryId().equals("")) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.problem_location), Toast.LENGTH_SHORT).show();
                    } else if (category.getText().toString().equals(getString(R.string.select_your_category))) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.choose_category), Toast.LENGTH_SHORT).show();
                    } else if (itemCond.equals("") && conditionLay.getVisibility() == View.VISIBLE) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.choose_condition), Toast.LENGTH_SHORT).show();
                    } else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked() && paypalId.getText().toString().trim().length() == 0) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked() && !paypalId.getText().toString().matches(emailPattern)) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_verify_paypalid), Toast.LENGTH_SHORT).show();
                    } else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked() && shippingFee.getText().toString().trim().length() == 0) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (images.size() <= 1) {
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_upload_image), Toast.LENGTH_SHORT).show();
                    } else {
                        for (int i = 0; i < images.size(); i++) {
                            if (images.get(i).get("type").equals("path")) {
                                pathsAry.add(images.get(i).get("path").toString());
                            }
                        }
                        count = pathsAry.size();
                        loadingProgress.setMax(count);
                        dialog.show();
                        String paths = pathsAry.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", "");
                        if (paths.contains(",")) {
                            String path[] = paths.split(",");
                            Log.v("path", "path" + path);
                            new moreLoadItems().execute(path);
                        } else {
                            if (pathsAry.size() > 0) {
                                new moreLoadItems().execute(paths);
                            } else {
                                alert_title.setText(getString(R.string.posting_list));
                                loadingProgress.setVisibility(View.GONE);
                                postProgress.setVisibility(View.VISIBLE);
                                uploadStatus.setVisibility(View.GONE);
                                for (int i = 0; i < images.size(); i++) {
                                    if (images.get(i).get("type").equals("url")) {
                                        String imageurl = images.get(i).get("image").toString();
                                        imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                                        uploadedImage.add(imageurl);
                                    }
                                }
                                new SendProducts().execute();
                            }
                        }
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.promote:
                if (!posteditemId.equals("")) {
                    reset();
                    finish();
                    Intent u = new Intent(AddProductDetail.this, CreatePromote.class);
                    u.putExtra("itemId", posteditemId);
                    startActivity(u);
                } else {
                    Toast.makeText(AddProductDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.conditionLay:
                Intent i = new Intent(AddProductDetail.this, SubCategory.class);
                i.putExtra("data", conditionAry);
                i.putExtra("from", "add");
                i.putExtra("name", itemCond);
                startActivity(i);
                break;
            case R.id.locationLay:
                Intent j = new Intent(AddProductDetail.this, LocationActivity.class);
                j.putExtra("from", "add");
                startActivity(j);
                break;
            case R.id.categoryLay:
                Intent k = new Intent(AddProductDetail.this, SelectCategory.class);
                k.putExtra("from", "add");
                k.putExtra("categAry", categAry);
                k.putExtra("subcategAry", subcategAry);
                startActivity(k);
                break;
        }
    }
}
