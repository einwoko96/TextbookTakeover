package com.app.textbooktakeover;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.buynow.AddAddress;
import com.app.buynow.Addresses;
import com.app.buynow.Checkout;
import com.app.external.HorizontalListView;
import com.app.external.MyTagHandler;
import com.app.external.TimeAgo;
import com.app.external.URLSpanNoUnderline;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
import com.app.utils.SOAPParsing;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.nirhart.parallaxscroll.views.ParallaxScrollView.OnScrollViewListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.viewpagerindicator.CirclePageIndicator;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.Manifest.permission.CALL_PHONE;

public class DetailActivity extends Activity implements OnClickListener, OnScrollViewListener, OnItemClickListener {

    ImageView backBtn, shareBtn, settingBtn, mblVerify, fbVerify, mailVerify;
    TextView title, itemPrice, itemCond, likeCount, userName, description, itemStatus,
            postedTime, viewCount, moreItems, location, chat, offer, titleText, call, ratingCount;
    ImageView image, userImg, map, likeImg, edit;
    Display display;
    int height1, height2, screenWidth2, screenWidth, listHeight, screenheight, position, screenHalf;
    RelativeLayout actionbar, main, reviewLay;
    LinearLayout commentLay, detailLay;
    HorizontalListView listView;
    Target target;
    ItemAdapter itemAdapter;
    ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    ParallaxScrollView sview;
    CirclePageIndicator pageIndicator;
    String chatId = "", from, shopaddress;
    boolean chatClicked = false;
    public static TextView commentCount;
    public static HashMap<String, String> itemMap = new HashMap<String, String>();
    private ArrayList<String> photosAry = new ArrayList<String>();
    private static ArrayList<HashMap<String, String>> MoreItems = new ArrayList<HashMap<String, String>>();
    private static boolean fromStop = false, isSeller = false;
    public static boolean fromEdit = false;
    AVLoadingIndicatorView progress;
    public HashMap<String, String> backupMap = new HashMap<String, String>();
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main_layout);

        title = (TextView) findViewById(R.id.title);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        itemCond = (TextView) findViewById(R.id.itemCond);
        likeCount = (TextView) findViewById(R.id.likesCount);
        commentCount = (TextView) findViewById(R.id.commentCount);
        userName = (TextView) findViewById(R.id.userName);
        description = (TextView) findViewById(R.id.description);
        postedTime = (TextView) findViewById(R.id.postedTime);
        viewCount = (TextView) findViewById(R.id.viewCount);
        chat = (TextView) findViewById(R.id.chat);
        offer = (TextView) findViewById(R.id.offer);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        shareBtn = (ImageView) findViewById(R.id.shareBtn);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        userImg = (ImageView) findViewById(R.id.userImage);
        actionbar = (RelativeLayout) findViewById(R.id.actionbar);
        listView = (HorizontalListView) findViewById(R.id.listView);
        sview = (ParallaxScrollView) findViewById(R.id.scrollView);
        moreItems = (TextView) findViewById(R.id.moretext);
        main = (RelativeLayout) findViewById(R.id.main);
        pageIndicator = (CirclePageIndicator) findViewById(R.id.pagerIndicator);
        location = (TextView) findViewById(R.id.location);
        itemStatus = (TextView) findViewById(R.id.itemStatus);
        map = (ImageView) findViewById(R.id.banner);
        commentLay = (LinearLayout) findViewById(R.id.commentLay);
        settingBtn = (ImageView) findViewById(R.id.settingBtn);
        likeImg = (ImageView) findViewById(R.id.likereditBtn);
        edit = (ImageView) findViewById(R.id.edit);
        mblVerify = (ImageView) findViewById(R.id.mblverify);
        fbVerify = (ImageView) findViewById(R.id.fbverify);
        mailVerify = (ImageView) findViewById(R.id.mailverify);
        detailLay = (LinearLayout) findViewById(R.id.detailLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        titleText = (TextView) findViewById(R.id.title_text);
        call = (TextView) findViewById(R.id.call);
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingCount = (TextView) findViewById(R.id.ratingCount);

        actionbar.bringToFront();

        MoreItems = new ArrayList<HashMap<String, String>>();

        backBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        sview.setOnScrollViewListener(this);
        commentCount.setOnClickListener(this);
        likeCount.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        userImg.setOnClickListener(this);
        commentLay.setOnClickListener(this);
        settingBtn.setOnClickListener(this);
        offer.setOnClickListener(this);
        chat.setOnClickListener(this);
        likeImg.setOnClickListener(this);
        edit.setOnClickListener(this);
        detailLay.setOnClickListener(null);
        call.setOnClickListener(this);

        itemMap = new HashMap<String, String>();
        position = (int) getIntent().getExtras().get("position");
        from = (String) getIntent().getExtras().get("from");
        backupMap = (HashMap<String, String>) getIntent().getExtras().get("data");
        itemMap = backupMap;
        Log.v("itemMap", "itemMap" + itemMap);

        titleText.setText(itemMap.get(Constants.TAG_TITLE));

        display = this.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        screenheight = display.getHeight();
        screenWidth = width * 49 / 100;
        screenWidth2 = display.getWidth();
        listHeight = width * 75 / 100;
        screenHalf = width / 2;
        height1 = screenheight * 55 / 100;
        height2 = screenheight * 65 / 100;
        Log.v("screenheight", "" + screenheight);

        viewPager.getLayoutParams().height = height2;

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);

        setData();

        new moreLoadItems().execute(0);
        itemAdapter = new ItemAdapter(DetailActivity.this, MoreItems);
        Log.v("2ndtime=", "moreitems" + MoreItems);
        listView.setAdapter(itemAdapter);

        getImageAry();

        checkUser();

        new updateView().execute();

        viewPager.setOnPageChangeListener(mOnPageChangeListener);
    }

    /** set uploaded item datas to elements**/
    private void setData() {
        title.setText(itemMap.get(Constants.TAG_TITLE));
        String curren = itemMap.get(Constants.TAG_CURRENCY_CODE);
        if (curren.contains("-")) {
            String cur[] = curren.split("-");
            curren = cur[0];
            if (curren != null) {
                itemPrice.setText(curren + " " + itemMap.get(Constants.TAG_PRICE));
            }
        } else {
            itemPrice.setText(itemMap.get(Constants.TAG_CURRENCY_CODE) + itemMap.get(Constants.TAG_PRICE));
        }
        if (itemMap.get(Constants.TAG_ITEM_CONDITION).equals("") || itemMap.get(Constants.TAG_ITEM_CONDITION).equals("0")){
            itemCond.setVisibility(View.GONE);
        } else {
            itemCond.setText(itemMap.get(Constants.TAG_ITEM_CONDITION));
        }
        likeCount.setText(itemMap.get(Constants.TAG_LIKECOUNT) + " " + getResources().getString(R.string.likes));
        commentCount.setText(itemMap.get(Constants.TAG_COMMENTCOUNT) + " " + getResources().getString(R.string.comments));
        userName.setText(itemMap.get(Constants.TAG_SELLERNAME));
        Spannable spannedText = (Spannable) Html.fromHtml(itemMap.get(Constants.TAG_ITEM_DES), null, new MyTagHandler());
        description.setText(spannedText);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(description);
        viewCount.setText(TextbookTakeoverApplication.format(Double.parseDouble(itemMap.get(Constants.TAG_VIEWCOUNT))) + " " + getResources().getString(R.string.views));
        moreItems.setText(getResources().getString(R.string.more_items_from) + " " + itemMap.get(Constants.TAG_SELLERNAME));
        location.setText(itemMap.get(Constants.TAG_LOCATION));
        shopaddress = itemMap.get(Constants.TAG_LOCATION);

        if (!itemMap.get(Constants.TAG_SELLERID).equals(GetSet.getUserId())){
            new checkItemStatus().execute();
        }

        if (TextbookTakeoverApplication.isRTL(DetailActivity.this)){
            title.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            description.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            description.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }

        try {
            long timestamp = 0;
            String time = itemMap.get(Constants.TAG_POSTED_TIME);
            if(time != null){
                timestamp = Long.parseLong(time) * 1000;
            }
            TimeAgo timeAgo = new TimeAgo(DetailActivity.this);
            postedTime.setText(timeAgo.timeAgo(timestamp));
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (NumberFormatException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        String url = "http://maps.google.com/maps/api/staticmap?center=" + itemMap.get(Constants.TAG_LATITUDE) + "," + itemMap.get(Constants.TAG_LONGITUDE) +
                "&zoom=15&size=" + screenWidth2 + "x" + screenWidth2 / 2 + "&sensor=false";
        Log.v("url","url="+url);
        Picasso.with(DetailActivity.this).load(url).into(map);
        if (!itemMap.get(Constants.TAG_SELLERIMG).equals("")){
            Picasso.with(DetailActivity.this).load(itemMap.get(Constants.TAG_SELLERIMG)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);
        }

        String liked = itemMap.get(Constants.TAG_LIKED);
        if (liked.equalsIgnoreCase("yes")) {
            likeImg.setColorFilter(null);
            likeImg.setImageResource((R.drawable.like_icon));
        } else {
            likeImg.setColorFilter(getResources().getColor(R.color.colorAccent));
            likeImg.setImageResource(R.drawable.unlike_icon);
        }

        if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")){
            itemStatus.setVisibility(View.VISIBLE);
            itemStatus.setText(getString(R.string.sold));
            itemStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.soldbg));
        } else {
            if (Constants.PROMOTION){
                if(itemMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Ad")) {
                    itemStatus.setVisibility(View.VISIBLE);
                    itemStatus.setText(getString(R.string.ad));
                    itemStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.adbg));
                } else if(itemMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Urgent")) {
                    itemStatus.setVisibility(View.VISIBLE);
                    itemStatus.setText(getString(R.string.urgent));
                    itemStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.urgentbg));
                } else {
                    itemStatus.setVisibility(View.GONE);
                }
            } else {
                itemStatus.setVisibility(View.GONE);
            }

        }

        if (Constants.BUYNOW){
            reviewLay.setVisibility(View.VISIBLE);
            try {
                ratingBar.setRating(Float.parseFloat(itemMap.get(Constants.TAG_SELLER_RATING)));
                ratingCount.setText("(" + itemMap.get(Constants.TAG_SELLER_RATING) + ")");
            } catch (NullPointerException e){
                e.printStackTrace();
            } catch (NumberFormatException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            reviewLay.setVisibility(View.GONE);
        }

        if (itemMap.get(Constants.TAG_FACEBOOK_VERIFICATION).equals("true")){
            fbVerify.setImageResource(R.drawable.fb_veri);
        } else {
            fbVerify.setImageResource(R.drawable.fb_unveri);
        }
        if (itemMap.get(Constants.TAG_EMAIL_VERIFICATION).equals("true")){
            mailVerify.setImageResource(R.drawable.mail_veri);
        } else {
            mailVerify.setImageResource(R.drawable.mail_unveri);
        }
        if (itemMap.get(Constants.TAG_MOBILE_VERIFICATION).equals("true")){
            mblVerify.setImageResource(R.drawable.mob_veri);
        } else {
            mblVerify.setImageResource(R.drawable.mob_unveri);
        }

        if (itemMap.get(Constants.TAG_SHOW_SELLER_MOB).equals("true")){
            call.setVisibility(View.VISIBLE);
        } else {
            call.setVisibility(View.GONE);
        }
    }

    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            if (arg0 == 0) {
                try {
                    if (arg0 == viewPager.getCurrentItem()) {
                        //getImageAry();
                        pagerAdapter = new ViewPagerAdapter(DetailActivity.this, photosAry);
                        viewPager.setAdapter(pagerAdapter);
                        viewPager.setCurrentItem(0);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

            try {
                if (arg0 == viewPager.getCurrentItem()) {
                    pageIndicator.setViewPager(viewPager, viewPager.getCurrentItem());
                    int index = viewPager.getCurrentItem();
                    View itemView = viewPager.findViewWithTag("pos" + index);
                    Log.v("itemView", "" + itemView);
                    Log.v("position", "" + index);
                    if (itemView != null) {
                        final ImageView image = (ImageView) itemView.findViewById(R.id.imgDisplay);
                        String imageloadingurl = photosAry.get(index);
                        setImage(image, imageloadingurl);

                    } else {
                        //stubImage.setVisibility(View.VISIBLE);
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPageSelected(int position) {

        }
    };

    /** for change the bottom button by user **/
    private void checkUser() {
        if (itemMap.get(Constants.TAG_SELLERID).equals(GetSet.getUserId())) {
            edit.setVisibility(View.VISIBLE);
            likeImg.setVisibility(View.GONE);
            isSeller = true;
            chat.setVisibility(View.GONE);
            if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")){
                offer.setText(getString(R.string.back_to_sale));
            } else {
                if (Constants.PROMOTION){
                    if (itemMap.get(Constants.TAG_PROMOTION_TYPE).equals("Normal")){
                        offer.setText(getString(R.string.promote_your_product));
                    } else {
                        offer.setText(getString(R.string.promotion_details));
                    }
                } else {
                    offer.setVisibility(View.GONE);
                }
            }
        } else {
            edit.setVisibility(View.GONE);
            likeImg.setVisibility(View.VISIBLE);
            chat.setVisibility(View.VISIBLE);
            isSeller = false;
            if (Constants.BUYNOW){
                offer.setText(getString(R.string.buynow));
                if (itemMap.get(Constants.TAG_INSTANT_BUY).equals("0") || itemMap.get(Constants.TAG_INSTANT_BUY).equals("2")
                        || itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                    offer.setVisibility(View.GONE);
                } else {
                    offer.setVisibility(View.VISIBLE);
                }
            } else {
                offer.setText(getString(R.string.make_an_offer));
                if (itemMap.get(Constants.TAG_MAKE_OFFER).equals("0")){
                    offer.setVisibility(View.VISIBLE);
                } else {
                    offer.setVisibility(View.GONE);
                }
            }
        }
    }

    /** for get the images from json to array **/
    private void getImageAry() {
        if (itemMap.get(Constants.TAG_PHOTOS).equals("") || itemMap.get(Constants.TAG_PHOTOS).equals(null)) {
            Log.v("photosss", "photos emptyyyy");
        } else {
            try {
                JSONArray photos = new JSONArray(itemMap.get(Constants.TAG_PHOTOS));
                for (int i = 0; i < photos.length(); i++) {
                    JSONObject jph = photos.getJSONObject(i);
                    String imageurl = DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_ORG);
                    //photosAry.clear();
                    photosAry.add(imageurl);
                }
                pagerAdapter = new ViewPagerAdapter(DetailActivity.this, photosAry);
                viewPager.setAdapter(pagerAdapter);
                viewPager.setCurrentItem(0);
                //Setting the indicator for pager
                if (photosAry.size() > 1) {
                    if (screenheight > 800) {
                        pageIndicator.setRadius(10);
                    } else {
                        pageIndicator.setRadius(5);
                    }
                    pageIndicator.setViewPager(viewPager);
                } else {
                    pageIndicator.setVisibility(View.GONE);
                }
                Log.v("photosAry", "" + photosAry);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ViewPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        public int getCount() {
            return temp.size();
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.layout_fullscreen,
                    collection, false);

            itemView.setTag("pos" + position);

            image = (ImageView) itemView.findViewById(R.id.imgDisplay);

            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(DetailActivity.this, SingleView.class);
                    i.putExtra("mimages", photosAry);
                    i.putExtra("position", position);
                    startActivity(i);
                }
            });

            ((ViewPager) collection).addView(itemView, 0);

            return itemView;

        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    private void setImage(final ImageView image, String imageloadingurl) {
        Log.v("imageloadingurl", "imageloadingurl=" + imageloadingurl);
        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //stubImage.setVisibility(View.GONE);
                try {
                    float ht = bitmap.getHeight();
                    float scale = (float) display.getWidth() / bitmap.getWidth();
                    int newHeight = (int) Math.round(ht * scale);
                    Log.v("hai" + newHeight, "setimage" + ht);

                    viewPager.getLayoutParams().height = newHeight;
                    viewPager.getLayoutParams().width = display.getWidth();
                    image.getLayoutParams().height = newHeight;
                    image.getLayoutParams().width = display.getWidth();
                    image.setImageBitmap(TextbookTakeoverApplication.getResizedBitmap(bitmap, 1024));

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.v("on failed", "on failed");
                //stubImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(DetailActivity.this).load(imageloadingurl).into(target);
    }


    // more items //
    class moreLoadItems extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            String url = Constants.API_MORE_ITEM;
            int offset = (params[0] * 20);

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_HOME;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_HOME);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("type", "moreitems");
            req.addProperty("seller_id", itemMap.get(Constants.TAG_SELLERID));
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));
            req.addProperty("offset", Integer.toString(offset));
            req.addProperty("limit", "20");
            req.addProperty("user_id", "");

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            MoreItems.clear();
            ItemsParsing parse = new ItemsParsing(DetailActivity.this);
            MoreItems.addAll(parse.parsing(json));
            return null;
        }

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void unused) {
            progress.setVisibility(View.GONE);
            if (MoreItems.size() == 0) {
                moreItems.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
            } else {
                itemAdapter.notifyDataSetChanged();
                listView.getLayoutParams().height = listHeight;
                Log.v("moreheight", "height" + listView.getHeight());
            }
        }

    }

    /** adapter for showing more items **/
    public class ItemAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        private Context mContext;
        ViewHolder holder = null;

        public ItemAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
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
            ImageView singleImage;
            TextView itemPrice, itemName, location, postedTime, productType;
            RelativeLayout imageLay;
            LinearLayout mainLay;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.home_list_items,
                        parent, false);// layout
                holder = new ViewHolder();
                holder.singleImage = (ImageView) convertView.findViewById(R.id.singleImage);
                holder.itemPrice = (TextView) convertView.findViewById(R.id.priceText);
                holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
                holder.productType = (TextView) convertView.findViewById(R.id.productType);
                holder.location = (TextView) convertView.findViewById(R.id.location);
                holder.postedTime = (TextView) convertView.findViewById(R.id.postedTime);
                holder.imageLay = (RelativeLayout) convertView.findViewById(R.id.imageLay);
                holder.mainLay = (LinearLayout) convertView.findViewById(R.id.mainLay);

                holder.singleImage.getLayoutParams().height = screenHalf;
                holder.imageLay.getLayoutParams().height = screenHalf;
                holder.singleImage.getLayoutParams().width = screenHalf;
                holder.imageLay.getLayoutParams().width = screenHalf;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final HashMap<String, String> tempMap = Items.get(position);

                holder.mainLay.setPadding(0, 0, TextbookTakeoverApplication.dpToPx(mContext, 5), 0);

                Picasso.with(DetailActivity.this).load(tempMap.get(Constants.TAG_ITEM_URL_350)).into(holder.singleImage);
                holder.itemName.setText(tempMap.get(Constants.TAG_TITLE));
                holder.itemPrice.setText(tempMap.get(Constants.TAG_CURRENCY_SYM) + " "
                        + tempMap.get(Constants.TAG_PRICE));
                holder.location.setText(tempMap.get(Constants.TAG_LOCATION));

                if (tempMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")){
                    holder.productType.setVisibility(View.VISIBLE);
                    holder.productType.setText(getString(R.string.sold));
                    holder.productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.soldbg));
                } else {
                    if (Constants.PROMOTION){
                        if(tempMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Ad")) {
                            holder.productType.setVisibility(View.VISIBLE);
                            holder.productType.setText(getString(R.string.ad));
                            holder.productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.adbg));
                        } else if(tempMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Urgent")) {
                            holder.productType.setVisibility(View.VISIBLE);
                            holder.productType.setText(getString(R.string.urgent));
                            holder.productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.urgentbg));
                        } else {
                            holder.productType.setVisibility(View.GONE);
                        }
                    } else {
                        holder.productType.setVisibility(View.GONE);
                    }
                }

                long timestamp = 0;
                String time = tempMap.get(Constants.TAG_POSTED_TIME);
                if(time != null){
                    timestamp = Long.parseLong(time) * 1000;
                }
                TimeAgo timeAgo = new TimeAgo(mContext);
                holder.postedTime.setText(timeAgo.timeAgo(timestamp));

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch(NumberFormatException e){
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

    }

    /** class for update the view **/
    class updateView extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_UPDATE_VIEW;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_UPDATE_VIEW);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                String status = json.getString(Constants.TAG_STATUS);
                if (status.equalsIgnoreCase("true")) {
                    String count = itemMap.get(Constants.TAG_VIEWCOUNT);
                    viewCount.setText(TextbookTakeoverApplication.format(Double.parseDouble(count)) + " " + getResources().getString(R.string.views));
                    final int view = (Integer.parseInt(count) + 1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (from){
                                case "home":
                                    FragmentMainActivity.HomeItems.get(position).put(Constants.TAG_VIEWCOUNT, Integer.toString(view));
                                    FragmentMainActivity.homeAdapter.notifyDataSetChanged();
                                    break;
                                case "search":
                                    SearchActivity.HomeItems.get(position).put(Constants.TAG_VIEWCOUNT, Integer.toString(view));
                                    FragmentMainActivity.homeAdapter.notifyDataSetChanged();
                                    break;
                                case "mylisting":
                                    MyListing.AddedItems.get(position).put(Constants.TAG_VIEWCOUNT, Integer.toString(view));
                                    break;
                                case "liked":
                                    LikedItems.likedItems.get(position).put(Constants.TAG_VIEWCOUNT, Integer.toString(view));
                                    break;
                                case "detail":
                                    Log.v("FromDetail","FromDetailUpdated"+MoreItems.get(position).get(Constants.TAG_ID)+" "+MoreItems.get(position).get(Constants.TAG_VIEWCOUNT));
                                    MoreItems.get(position).put(Constants.TAG_VIEWCOUNT, Integer.toString(view));
                                    break;
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void dialog(String name, String imageurl) {
        final Dialog dialog = new Dialog(DetailActivity.this, R.style.DialogSlideAnim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.contactme_dialog);

        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
       // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView contactName = (TextView) dialog.findViewById(R.id.contactName);
        TextView send = (TextView) dialog.findViewById(R.id.send);
        ImageView contactImg = (ImageView) dialog.findViewById(R.id.contactImg);
        final EditText contactMsg = (EditText) dialog.findViewById(R.id.contactMsg);
        final EditText makeOffer = (EditText) dialog.findViewById(R.id.makeOffer);
        LinearLayout offerLay = (LinearLayout) dialog.findViewById(R.id.offerLay);
        LinearLayout emptyLay = (LinearLayout) dialog.findViewById(R.id.emptyLay);
        final RelativeLayout mainLay = (RelativeLayout) dialog.findViewById(R.id.mainLay);

        contactMsg.setFilters(new InputFilter[]{TextbookTakeoverApplication.EMOJI_FILTER, new InputFilter.LengthFilter(500)});

        contactName.setText(name);
        Picasso.with(DetailActivity.this).load(imageurl).into(contactImg);

        if (name.equals(getString(R.string.make_an_offer))){
            offerLay.setVisibility(View.VISIBLE);
        } else {
            offerLay.setVisibility(View.GONE);
        }

        emptyLay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mainLay.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextbookTakeoverApplication.isNetworkAvailable(DetailActivity.this)) {
                    if (contactMsg.getText().toString().trim().length() != 0 && makeOffer.getText().toString().trim().length() != 0) {
                        if (Integer.parseInt(itemMap.get(Constants.TAG_PRICE)) <= Integer.parseInt(makeOffer.getText().toString())){
                            Toast.makeText(DetailActivity.this, getString(R.string.offer_should_not_above), Toast.LENGTH_SHORT).show();
                        } else if (Integer.parseInt(makeOffer.getText().toString()) == 0){
                            Toast.makeText(DetailActivity.this, getString(R.string.offer_should_not_zero), Toast.LENGTH_SHORT).show();
                        } else {
                            new getChatId().execute("offer");
                            new makeOffer().execute(contactMsg.getText().toString().trim(), makeOffer.getText().toString().trim());
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    TextbookTakeoverApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                }

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /** class for get chat id betwwen logined user and the seller **/
    class getChatId extends AsyncTask<String, String, Void> {
        String from = "";
        private ProgressDialog dialog = new ProgressDialog(DetailActivity.this);
        @Override
        protected Void doInBackground(String... params) {
            from = params[0];

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_CHAT_ID;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_CHAT_ID);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("sender_id", GetSet.getUserId());
            req.addProperty("receiver_id", itemMap.get(Constants.TAG_SELLERID));

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            try {
                JSONObject jobj = new JSONObject(json);
                String status = jobj.getString(Constants.TAG_STATUS);

                if (status.equals("true")) {
                    chatId = jobj.getString("chat_id");
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (chatClicked){
                this.dialog.setMessage(getString(R.string.pleasewait));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                this.dialog.show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            chat.setOnClickListener(DetailActivity.this);
            if (from.equals("chat")){
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
                chatClicked = false;
                Intent i = new Intent(DetailActivity.this, ChatActivity.class);
                i.putExtra("userName", itemMap.get(Constants.TAG_SELLER_USERNAME));
                i.putExtra("userId", itemMap.get(Constants.TAG_SELLERID));
                i.putExtra("chatId", chatId);
                i.putExtra("userImage", itemMap.get(Constants.TAG_SELLERIMG));
                i.putExtra("fullName", itemMap.get(Constants.TAG_SELLERNAME));
                i.putExtra("data", itemMap);
                i.putExtra("from", "detail");
                startActivity(i);
            }
        }
    }

    /** class for change the product to sold and back to sale **/
    class changeSoldStatus extends AsyncTask<String, Void, String> {

        private ProgressDialog dialog = new ProgressDialog(DetailActivity.this);

        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_SOLD_ITEM;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_SOLD_ITEM);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("value", params[0]);
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));

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
        protected void onPostExecute(String res) {
            try {
                if (dialog.isShowing()){
                    dialog.dismiss();
                }
                JSONObject json = new JSONObject(res);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    if (DefensiveClass.optString(json, Constants.TAG_MESSAGE).equalsIgnoreCase("Item Status changed to Sold")){
                        Toast.makeText(DetailActivity.this, getString(R.string.item_status_changed_to_sold), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.item_status_changed_to_available), Toast.LENGTH_LONG).show();
                    }
                    String value = "";
                    String promotionType = "";
                    if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")){
                        value = "onsale";
                        offer.setText(getString(R.string.promote_your_product));
                        promotionType = itemMap.get(Constants.TAG_PROMOTION_TYPE);
                    } else {
                        value = "sold";
                        offer.setText(getString(R.string.back_to_sale));
                        itemMap.put(Constants.TAG_PROMOTION_TYPE, "Normal");
                        promotionType = itemMap.get(Constants.TAG_PROMOTION_TYPE);
                    }
                    itemMap.put(Constants.TAG_ITEM_STATUS, value);

                    switch (from){
                        case "home":
                            FragmentMainActivity.HomeItems.get(position).put(Constants.TAG_ITEM_STATUS, value);
                            FragmentMainActivity.HomeItems.get(position).put(Constants.TAG_PROMOTION_TYPE, promotionType);
                            FragmentMainActivity.homeAdapter.notifyDataSetChanged();
                            break;
                        case "search":
                            SearchActivity.HomeItems.get(position).put(Constants.TAG_ITEM_STATUS, value);
                            SearchActivity.HomeItems.get(position).put(Constants.TAG_PROMOTION_TYPE, promotionType);
                            SearchActivity.homeAdapter.notifyDataSetChanged();
                            break;
                        case "mylisting":
                            MyListing.AddedItems.get(position).put(Constants.TAG_ITEM_STATUS, value);
                            MyListing.AddedItems.get(position).put(Constants.TAG_PROMOTION_TYPE, promotionType);
                            MyListing.itemAdapter.notifyDataSetChanged();
                            break;
                        case "liked":
                            LikedItems.likedItems.get(position).put(Constants.TAG_ITEM_STATUS, value);
                            LikedItems.likedItems.get(position).put(Constants.TAG_PROMOTION_TYPE, promotionType);
                            LikedItems.itemAdapter.notifyDataSetChanged();
                            break;
                        case "detail":
                            MoreItems.get(position).put(Constants.TAG_ITEM_STATUS, value);
                            MoreItems.get(position).put(Constants.TAG_PROMOTION_TYPE, promotionType);
                            itemAdapter.notifyDataSetChanged();
                            break;
                    }
                    finish();
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /** class for remove the product from listing **/
    class deleteProduct extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_DELETE_PRODUCT;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_DELETE_PRODUCT);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONObject json = new JSONObject(res);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    Toast.makeText(DetailActivity.this, getString(R.string.product_deleted_duccessfully), Toast.LENGTH_LONG).show();
                    finish();
                    switch (from){
                        case "home":
                            FragmentMainActivity.HomeItems.remove(position);
                            FragmentMainActivity.homeAdapter.notifyDataSetChanged();
                            break;
                        case "search":
                            SearchActivity.HomeItems.remove(position);
                            SearchActivity.homeAdapter.notifyDataSetChanged();
                            break;
                        case "mylisting":
                            MyListing.AddedItems.remove(position);
                            MyListing.itemAdapter.notifyDataSetChanged();
                            break;
                        case "liked":
                            LikedItems.likedItems.remove(position);
                            LikedItems.itemAdapter.notifyDataSetChanged();
                            break;
                        case "detail":
                            MoreItems.remove(position);
                            itemAdapter.notifyDataSetChanged();
                            break;
                    }
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /** class for send the offer request to seller **/
    class makeOffer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            long unixTime = System.currentTimeMillis() / 1000L;

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_SEND_OFFER_REQ;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_SEND_OFFER_REQ);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("sender_id", GetSet.getUserId());
            req.addProperty("chat_id", chatId);
            req.addProperty("source_id", itemMap.get(Constants.TAG_ID));
            req.addProperty("created_date", Long.toString(unixTime));
            req.addProperty("message", params[0]);
            req.addProperty("offer_price", params[1]);

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONObject json = new JSONObject(res);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    Toast.makeText(DetailActivity.this, getString(R.string.message_send_successfully), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /** class for checking item approval or not **/
    class checkItemStatus extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_CHECK_ITEM_STATUS;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_CHECK_ITEM_STATUS);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONObject json = new JSONObject(res);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    if (DefensiveClass.optInt(json, Constants.TAG_ITEM_APPROVE).equals("0")){
                        approveDialog(DetailActivity.this);
                    }
                } else {
                //    Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            try {
                //      sview.setViewsBounds(ParallaxScollListView.ZOOM_X2);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /** for show the popupmenu window **/
    public void shareImage(View v) {
        final ArrayList<String> values = new ArrayList<>();

        if (itemMap.get(Constants.TAG_SELLERID).equals(GetSet.getUserId())) {
            if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")){
                values.add(getString(R.string.delete_product));
                values.add(getString(R.string.back_to_sale));
            } else {
                values.add(getString(R.string.delete_product));
                values.add(getString(R.string.mark_as_sold));
            }

        } else {
            if (Constants.EXCHANGE && itemMap.get(Constants.TAG_EXCHANGE_BUY).equalsIgnoreCase("1")) {
                values.add(getString(R.string.create_exchange));
            }

            if (Constants.BUYNOW && itemMap.get(Constants.TAG_MAKE_OFFER).equals("0")){
                values.add(getString(R.string.make_an_offer));
            }

            if (itemMap.get(Constants.TAG_REPORT).equals("yes")) {
                values.add(getString(R.string.undo_report));
            } else {
                values.add(getString(R.string.report_product));
            }

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(DetailActivity.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(display.getWidth() * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP|Gravity.RIGHT,0,20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                popup.dismiss();
                openAction(values.get(position));
            }
        });
    }

    public void openAction(String from) {
        Log.v("from", "from=" + from);
        if (from.equals(getString(R.string.delete_product))) {
            if (GetSet.isLogged()){
                confirmdialog(getString(R.string.delete_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.back_to_sale))) {
            if (GetSet.isLogged()){
                confirmdialog(getString(R.string.back_sale_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.mark_as_sold))) {
            if (GetSet.isLogged()){
                confirmdialog(getString(R.string.sold_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.create_exchange))) {
            if (GetSet.isLogged()){
                Intent i = new Intent(DetailActivity.this, CreateExchange.class);
                i.putExtra("itemId", itemMap.get(Constants.TAG_ID));
                startActivity(i);
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.make_an_offer))) {
            if (GetSet.isLogged()){
                dialog(getString(R.string.make_an_offer), itemMap.get(Constants.TAG_SELLERIMG));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.undo_report))) {
            if (GetSet.isLogged()){
                confirmdialog(getString(R.string.undoreport_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.report_product))) {
            if (GetSet.isLogged()){
                confirmdialog(getString(R.string.report_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        }
    }

    /** class for like & unlike the product **/
    class itemLike extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_ITEM_LIKE;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_ITEM_LIKE);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            Log.v("like","product"+json);
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            likeCount.setOnClickListener(DetailActivity.this);
            likeImg.setOnClickListener(DetailActivity.this);
            try {
                JSONObject json = new JSONObject(result);
                String status = json.getString(Constants.TAG_STATUS);
                String results = json.getString(Constants.TAG_RESULT);
                String flag = "no";
                if (status.equals("true")) {
                    String count = itemMap.get(Constants.TAG_LIKECOUNT);
                    if (results.equalsIgnoreCase("Item Liked Successfully")) {
                        count = Integer.toString((Integer.parseInt(count) + 1));
                        flag = "yes";
                        likeCount.setText(count + " " + getResources().getString(R.string.likes));
                        likeImg.setColorFilter(null);
                        likeImg.setImageResource(R.drawable.like_icon);
                        itemMap.put(Constants.TAG_LIKECOUNT, count);
                    } else {
                        count = Integer.toString((Integer.parseInt(count) - 1));
                        flag = "no";
                        likeCount.setText(count + " " + getResources().getString(R.string.likes));
                        likeImg.clearColorFilter();
                        likeImg.setImageResource(R.drawable.unlike_icon);
                        itemMap.put(Constants.TAG_LIKECOUNT, count);
                    }
					switch (from){
    				case "home":
    					FragmentMainActivity.HomeItems.get(position).put(Constants.TAG_LIKECOUNT, count);
    					FragmentMainActivity.HomeItems.get(position).put(Constants.TAG_LIKED, flag);
    					FragmentMainActivity.homeAdapter.notifyDataSetChanged();
    					break;
    				case "search":
                        SearchActivity.HomeItems.get(position).put(Constants.TAG_LIKECOUNT, count);
                        SearchActivity.HomeItems.get(position).put(Constants.TAG_LIKED, flag);
                        SearchActivity.homeAdapter.notifyDataSetChanged();
    					break;
    				case "mylisting":
                        MyListing.AddedItems.get(position).put(Constants.TAG_LIKECOUNT, count);
                        MyListing.AddedItems.get(position).put(Constants.TAG_LIKED, flag);
                        MyListing.itemAdapter.notifyDataSetChanged();
                        break;
                    case "liked":
                        LikedItems.likedItems.get(position).put(Constants.TAG_LIKECOUNT, count);
                        LikedItems.likedItems.get(position).put(Constants.TAG_LIKED, flag);
                        LikedItems.itemAdapter.notifyDataSetChanged();
                        break;
    				case "detail":
    					MoreItems.get(position).put(Constants.TAG_LIKECOUNT, count);
    					MoreItems.get(position).put(Constants.TAG_LIKED, flag);
    					itemAdapter.notifyDataSetChanged();
    					break;
    				}
                } else {
                    TextbookTakeoverApplication.dialog(DetailActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /** class for report and undo report **/
    class ReportItem extends AsyncTask<String, Void, String> {
        String value = "";

        @Override
        protected String doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_REPORT_ITEM;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_REPORT_ITEM);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject json = new JSONObject(result);
                String status = json.getString(Constants.TAG_STATUS);
                String message = json.getString(Constants.TAG_MESSAGE);
                if (status.equalsIgnoreCase("true")) {
                    if (message.equalsIgnoreCase("Reported Successfully")){
                        message = getString(R.string.reported_successfully);
                    } else {
                        message = getString(R.string.unreported_successfully);
                    }
                    Toast.makeText(DetailActivity.this, message, Toast.LENGTH_LONG).show();
					if(itemMap.get(Constants.TAG_REPORT).equals("yes")){
                        itemMap.put(Constants.TAG_REPORT, "no");
                        value = "no";
					}else{
                        itemMap.put(Constants.TAG_REPORT, "yes");
                        value = "yes";
					}
					switch (from){
						case "home":
							FragmentMainActivity.HomeItems.get(position).put(Constants.TAG_REPORT, value);
							FragmentMainActivity.homeAdapter.notifyDataSetChanged();
							break;
						case "search":
							SearchActivity.HomeItems.get(position).put(Constants.TAG_REPORT, value);
                            SearchActivity.homeAdapter.notifyDataSetChanged();
							break;
						case "mylisting":
							MyListing.AddedItems.get(position).put(Constants.TAG_REPORT, value);
							MyListing.itemAdapter.notifyDataSetChanged();
							break;
						case "liked":
							LikedItems.likedItems.get(position).put(Constants.TAG_REPORT, value);
                            LikedItems.itemAdapter.notifyDataSetChanged();
							break;
						case "detail":
							MoreItems.get(position).put(Constants.TAG_REPORT, value);
							itemAdapter.notifyDataSetChanged();
							break;
					}
                } else {
                    TextbookTakeoverApplication.dialog(DetailActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                }
                //	reportItem.setEnabled(true);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /** for get the promotional details of the product **/
    class getPromotionDetails extends AsyncTask<Integer, Void, Void> {
        HashMap<String, String> map = new HashMap<String, String>();
        private ProgressDialog dialog = new ProgressDialog(DetailActivity.this);

        @Override
        protected Void doInBackground(Integer... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_CHECK_PROMOTION;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_CHECK_PROMOTION);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));

            SOAPParsing soap = new SOAPParsing();
            final String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            try {
                JSONObject jobj = new JSONObject(json);
                String response = jobj.getString(Constants.TAG_STATUS);

                if (response.equalsIgnoreCase("true")){

                    JSONObject temp = jobj.optJSONObject(Constants.TAG_RESULT);
                    if (temp != null){
                        String id = DefensiveClass.optString(temp, Constants.TAG_ID);
                        String name = DefensiveClass.optString(temp, Constants.TAG_PROMOTION_NAME);
                        String amount = DefensiveClass.optString(temp, Constants.TAG_PAID_AMOUNT);
                        String currencySymbol = DefensiveClass.optString(temp, Constants.TAG_CURRENCY_SYM);
                        String currencyCode = DefensiveClass.optString(temp, Constants.TAG_CURRENCY_CODE);
                        String upto = DefensiveClass.optString(temp, Constants.TAG_UPTO);
                        String transactionId = DefensiveClass.optString(temp, Constants.TAG_TRANSACTION_ID);
                        String status = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                        String itemId = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
                        String itemName = DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME);
                        String itemImage = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);

                        map.put(Constants.TAG_ID, id);
                        map.put(Constants.TAG_PROMOTION_NAME, name);
                        map.put(Constants.TAG_PAID_AMOUNT, amount);
                        map.put(Constants.TAG_CURRENCY_SYM, currencySymbol);
                        map.put(Constants.TAG_CURRENCY_CODE, currencyCode);
                        map.put(Constants.TAG_UPTO, upto);
                        map.put(Constants.TAG_TRANSACTION_ID, transactionId);
                        map.put(Constants.TAG_STATUS, status);
                        map.put(Constants.TAG_ITEM_ID, itemId);
                        map.put(Constants.TAG_ITEM_NAME, itemName);
                        map.put(Constants.TAG_ITEM_IMAGE, itemImage);
                    }
                }

            } catch (Exception e) {
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.v("promotionAry","promotionAry=="+map);
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if(map.size()>0){
                Intent j = new Intent(DetailActivity.this, PromotionDetail.class);
                j.putExtra("data", map);
                startActivity(j);
            }else{
                Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            }
        }
    }

    class homeLoadItems extends AsyncTask<String, Void, Void> {
        ArrayList<HashMap<String,String>> HomeItems=new ArrayList<HashMap<String,String>>();
        private ProgressDialog dialog = new ProgressDialog(DetailActivity.this);
        @Override
        protected Void doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_SEARCH_ITEM;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_SEARCH_ITEM);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));
            req.addProperty("user_id", GetSet.getUserId());
            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            ItemsParsing parse = new ItemsParsing(DetailActivity.this);
            HomeItems.addAll(parse.parsing(json));
            Log.v("HomeItems","HomeItems"+HomeItems);
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

        @Override
        protected void onPostExecute(Void unused) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if(HomeItems.size() == 0){
                Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            }else{
                itemMap.clear();
                itemMap.putAll(HomeItems.get(0));
                setData();
                photosAry.clear();
                getImageAry();
                checkUser();
                viewPager.setOnPageChangeListener(mOnPageChangeListener);
                switch (from){
                    case "home":
                        FragmentMainActivity.HomeItems.get(position).putAll(HomeItems.get(0));
                        FragmentMainActivity.homeAdapter.notifyDataSetChanged();
                        break;
                    case "search":
                        SearchActivity.HomeItems.get(position).putAll(HomeItems.get(0));
                        SearchActivity.homeAdapter.notifyDataSetChanged();
                        break;
                    case "mylisting":
                        MyListing.AddedItems.get(position).putAll(HomeItems.get(0));
                        MyListing.itemAdapter.notifyDataSetChanged();
                        break;
                    case "liked":
                        LikedItems.likedItems.get(position).putAll(HomeItems.get(0));
                        LikedItems.itemAdapter.notifyDataSetChanged();
                        break;
                    case "detail":
                      //  MoreItems.get(position).put(Constants.TAG_REPORT, value);
                      //  itemAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    public void confirmdialog(final String Message) {
        final Dialog dialog = new Dialog(DetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(display.getWidth()*90/100, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(Message);

        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Message.equals(getResources().getString(R.string.report_product_confirmation)) ||
                        Message.equals(getResources().getString(R.string.undoreport_product_confirmation))) {
                    new ReportItem().execute(itemMap.get(Constants.TAG_ID));
                } else if (Message.equals(getResources().getString(R.string.delete_product_confirmation))) {
                    new deleteProduct().execute();
                } else if (Message.equals(getResources().getString(R.string.back_sale_confirmation)) ||
                        Message.equals(getResources().getString(R.string.sold_product_confirmation))) {
                    if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                        new changeSoldStatus().execute("0");
                    } else {
                        new changeSoldStatus().execute("1");
                    }
                }
                dialog.dismiss();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /** class for get saved addresses **/
    class getAddress extends AsyncTask<Integer, Void, Void> {

        ArrayList<HashMap<String, String>> addressAry = new ArrayList<>();
        ProgressDialog dialog = new ProgressDialog(DetailActivity.this);

        @Override
        protected Void doInBackground(Integer... arg0) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_SHIPPING;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_SHIPPING);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("item_id", itemMap.get(Constants.TAG_ID));

            SOAPParsing soap = new SOAPParsing();
            String jobj = soap.getJSONFromUrl(SOAP_ACTION, req);

            HashMap<String, String> map;
            try {
                JSONObject json = new JSONObject(jobj);
                String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    JSONArray result = json.optJSONArray("result");
                    if(!(result==null)){
                        for (int i = 0; i < result.length(); i++) {
                            map = new HashMap<String, String>();
                            JSONObject temp = result.getJSONObject(i);
                            String shippingid = DefensiveClass.optInt(temp, Constants.TAG_SHIPPINGID);
                            String nickname = DefensiveClass.optString(temp, Constants.TAG_NICKNAME);
                            String name = DefensiveClass.optString(temp, Constants.TAG_NAME);
                            String country = DefensiveClass.optString(temp, Constants.TAG_COUNTRY);
                            String state = DefensiveClass.optString(temp, Constants.TAG_STATE);
                            String address1 = DefensiveClass.optString(temp, Constants.TAG_ADDRESS1);
                            String address2 = DefensiveClass.optString(temp, Constants.TAG_ADDRESS2);
                            String city = DefensiveClass.optString(temp, Constants.TAG_CITY);
                            String zipcode = DefensiveClass.optString(temp, Constants.TAG_ZIPCODE);
                            String phone = DefensiveClass.optString(temp, Constants.TAG_PHONE);
                            String countrycode = DefensiveClass.optString(temp, Constants.TAG_COUNTRYCODE);
                            String defaults = DefensiveClass.optString(temp, Constants.TAG_DEFAULTSHIPPING);

                            map.put(Constants.TAG_SHIPPINGID, shippingid);
                            map.put(Constants.TAG_NICKNAME, nickname);
                            map.put(Constants.TAG_NAME, name);
                            map.put(Constants.TAG_COUNTRY, country);
                            map.put(Constants.TAG_STATE, state);
                            map.put(Constants.TAG_ADDRESS1, address1);
                            map.put(Constants.TAG_ADDRESS2, address2);
                            map.put(Constants.TAG_CITY, city);
                            map.put(Constants.TAG_ZIPCODE, zipcode);
                            map.put(Constants.TAG_PHONE, phone);
                            map.put(Constants.TAG_COUNTRYCODE, countrycode);
                            map.put(Constants.TAG_DEFAULTSHIPPING, defaults);

                            addressAry.add(map);
                        }
                    }
                } else if (response.equalsIgnoreCase("error")){
                    TextbookTakeoverApplication.disabledialog(DetailActivity.this, json.optString("message"));
                }
            } catch (Exception e) {
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

        @Override
        protected void onPostExecute(Void unused) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (addressAry.size() > 0){
                int haveDefaultAddress = 0;
                for (int i = 0; i < addressAry.size(); i++){
                    if (addressAry.get(i).get(Constants.TAG_DEFAULTSHIPPING).equals("1")) {
                        haveDefaultAddress = i;
                        break;
                    }
                }
                if (haveDefaultAddress == 0){
                    Intent l = new Intent(DetailActivity.this, Addresses.class);
                    l.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    l.putExtra("from", "checkout");
                    l.putExtra("shippingId", Integer.toString(haveDefaultAddress));
                    l.putExtra("itemData", itemMap);
                    startActivity(l);
                } else {
                    Intent i = new Intent(DetailActivity.this, Checkout.class);
                    i.putExtra("itemData", itemMap);
                    i.putExtra("shippingData", addressAry.get(haveDefaultAddress));
                    startActivity(i);
                }
            } else {
                Intent i = new Intent(DetailActivity.this, AddAddress.class);
                i.putExtra("from", "checkout");
                i.putExtra("to", "add");
                i.putExtra("itemData", itemMap);
                startActivity(i);
            }
        }
    }

    public void approveDialog(final Context ctx) {
        final Dialog dialog = new Dialog(DetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(display.getWidth()*90/100, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(getString(R.string.product_waiting_for_admin_approval));

        cancel.setVisibility(View.GONE);

        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    switch (from){
                        case "home":
                            FragmentMainActivity.HomeItems.remove(position);
                            FragmentMainActivity.homeAdapter.notifyDataSetChanged();
                            break;
                        case "search":
                            SearchActivity.HomeItems.remove(position);
                            SearchActivity.homeAdapter.notifyDataSetChanged();
                            break;
                        case "mylisting":
                            MyListing.AddedItems.remove(position);
                            MyListing.itemAdapter.notifyDataSetChanged();
                            break;
                        case "liked":
                            LikedItems.likedItems.remove(position);
                            LikedItems.itemAdapter.notifyDataSetChanged();
                            break;
                        case "detail":
                            MoreItems.remove(position);
                            itemAdapter.notifyDataSetChanged();
                            break;
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
                dialog.dismiss();
                ((Activity)ctx).finish();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = (Spannable) (textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    @Override
    public void onBackPressed() {
        //MoreItems.clear();
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shareBtn:
                Intent g = new Intent(Intent.ACTION_SEND);
                g.setType("text/plain");
                g.putExtra(Intent.EXTRA_TEXT, itemMap.get(Constants.TAG_PROURL));
                startActivity(Intent.createChooser(g, "Share"));
                break;
            case R.id.backbtn:
                //MoreItems.clear();
                finish();
                break;
            case R.id.edit:
                Intent a = new Intent(DetailActivity.this, AddProductDetail.class);
                a.putExtra("from", "edit");
                a.putExtra("data", itemMap);
                startActivity(a);
                break;
            case R.id.chat:
                if (GetSet.isLogged()) {
                    if (TextbookTakeoverApplication.isNetworkAvailable(DetailActivity.this)) {
                        chat.setOnClickListener(null);
                        chatClicked = true;
                        new getChatId().execute("chat");
                    } else {
                        TextbookTakeoverApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                    }

                } else {
                    Intent i = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.commentCount:
                Intent c = new Intent(DetailActivity.this, CommentsActivity.class);
                c.putExtra("itemId", itemMap.get(Constants.TAG_ID));
                c.putExtra("position", position);
                c.putExtra("from", from);
                c.putExtra("productName", itemMap.get(Constants.TAG_TITLE));
                c.putExtra("productImage", itemMap.get(Constants.TAG_ITEM_URL_350));
                startActivity(c);
                break;
            case R.id.settingBtn:
                shareImage(v);
                break;
            case R.id.offer:
                if (GetSet.isLogged()) {
                    if (isSeller) {
                        if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")){
                            confirmdialog(getString(R.string.back_sale_confirmation));
                        } else {
                            if (itemMap.get(Constants.TAG_PROMOTION_TYPE).equals("Normal")){
                                Intent i = new Intent(DetailActivity.this, CreatePromote.class);
                                i.putExtra("itemId", itemMap.get(Constants.TAG_ID));
                                startActivity(i);
                            } else {
                                new getPromotionDetails().execute();
                            }
                        }
                    } else {
                        if (Constants.BUYNOW){
                            new getAddress().execute();
                        } else {
                            dialog(getString(R.string.make_an_offer), itemMap.get(Constants.TAG_SELLERIMG));
                        }
                    }
                } else {
                    Intent i = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.likesImg:
            case R.id.likereditBtn:
                if (GetSet.isLogged()) {
                    if (TextbookTakeoverApplication.isNetworkAvailable(DetailActivity.this)) {
                        likeImg.setColorFilter(getResources().getColor(R.color.grey));
                        likeCount.setOnClickListener(null);
                        likeImg.setOnClickListener(null);
                        new itemLike().execute();
                    } else {
                        TextbookTakeoverApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                    }

                } else {
                    Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(j);
                }
                break;
            case R.id.userImage:
                Intent u = new Intent(DetailActivity.this, Profile.class);
                u.putExtra("userId", itemMap.get(Constants.TAG_SELLERID));
                startActivity(u);
                break;
            case R.id.call:
                if (GetSet.isLogged()){
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + itemMap.get(Constants.TAG_MOBILE_NO)));
                    if (ActivityCompat.checkSelfPermission(DetailActivity.this,
                            CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{ CALL_PHONE }, 101);
                    } else {
                        startActivity(callIntent);
                    }
                } else {
                    Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(j);
                }
                break;
        }

    }

    @Override
    public void onScrollChanged(ParallaxScrollView v, int l, int t, int oldl,
                                int oldt) {
        //cd.setAlpha(getAlphaforActionBar(v.getScrollY()));

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        //finish();
        try {
            Intent i = new Intent(DetailActivity.this, DetailActivity.class);
            i.putExtra("data", MoreItems.get(position));
            i.putExtra("position", position);
            i.putExtra("from", "detail");
            startActivity(i);
        } catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        Log.v("fromonStop", "from==" + from + " " + MoreItems);
        if (itemMap.equals(backupMap) && !MoreItems.isEmpty()) {
            MoreItems.clear();
            Log.v("MoreItemsCleared", "Cleared");
            fromStop = true;
            itemAdapter.notifyDataSetChanged();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(DetailActivity.this);
    }

    @Override
    protected void onResume() {
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(DetailActivity.this);
        if (fromEdit){
            fromEdit = false;
            new homeLoadItems().execute();
        }
        if ((!itemMap.equals(backupMap)||fromStop)) {
            new moreLoadItems().execute(0);
        }
        itemMap = backupMap;
        super.onResume();
    }
}
