package com.app.textbooktakeover;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.external.BadgeView;
import com.app.external.GPSTracker;
import com.app.external.HorizontalListView;
import com.app.external.TimeAgo;
import com.app.scanner.ScannerActivity;
import com.app.utils.Constants;
import com.app.utils.SOAPParsing;
import com.etsy.android.grid.StaggeredGridView;
import com.etsy.android.grid.util.GridRefreshListener;
import com.app.buynow.MySalesnOrder;
import com.app.external.AutoScrollViewPager;
import com.app.helper.ItemAdapter;
import com.app.helper.Model;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.LinePageIndicator;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class FragmentMainActivity extends AppCompatActivity implements OnClickListener, AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, GridRefreshListener {

	public static ListView listView;
	ImageView titleImage, menu_btn, filter_btn, search_btn, floatingBtn, notifybtn;
	public static ItemAdapter adapter;
	public static int screenWidth, screenHeight, screenHalf, currentPage=0;
	int visibleThreshold=0, previousTotal=0, mDrawerPosition=0;
	boolean loading=true, pulldown=false, mDrawerItemClicked = false;
	boolean networkEnable = false;
    public static String chatCount = "", homeBanner="";
	DrawerLayout drawer;
	ActionBarDrawerToggle toggle;
	LinearLayout profheader, proflogin, nullLay;
	RelativeLayout locationLay, headerLay, reviewLay;
	TextView login, userid, ratingCount;
	public static TextView username, locationTxt;
	public static ImageView userImage;
	Toolbar toolbar;
	StaggeredGridView gridView;
	SwipeRefreshLayout swipeLayout;
	AlertDialog alertDialog;
	public static HomeAdapter homeAdapter;
	AVLoadingIndicatorView progress;
	HorizontalListView filterList;
	public static FilterAdapter filterAdapter;
	View filterView;
	GPSTracker gps;
	Display display;
	View header;
	AutoScrollViewPager viewPager;
    LinePageIndicator pageIndicator;
	BannerPagerAdapter pagerAdapter;
    BadgeView notifyBadge;
	private Geocoder geocoder;
	private List<Address> addresses;
	public static ArrayList<HashMap<String,String>> filterAry = new ArrayList<HashMap<String,String>>();
	public static ArrayList<HashMap<String,String>> HomeItems=new ArrayList<HashMap<String,String>>();
	public static ArrayList<HashMap<String,String>> bannerAry = new ArrayList<HashMap<String,String>>();
	RatingBar ratingBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_frame);

		// For Getting Side Menu Items
		Model.LoadModel(FragmentMainActivity.this);
		final String[] ids = new String[Model.Items.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = Integer.toString(i + 1);
		}

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Elements initialisation
		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		menu_btn = (ImageView) findViewById(R.id.menubtn);
		filter_btn=(ImageView) findViewById(R.id.filterbtn);
		search_btn = (ImageView) findViewById(R.id.searchbtn);
		listView = (ListView) findViewById(R.id.list);
		profheader = (LinearLayout)findViewById(R.id.profile_header);
		proflogin = (LinearLayout)findViewById(R.id.profile_login);
		headerLay = (RelativeLayout) findViewById(R.id.header);
		login = (TextView)findViewById(R.id.login);
		username = (TextView)findViewById(R.id.userName);
		userid = (TextView)findViewById(R.id.userId);
		userImage = (ImageView)findViewById(R.id.userImage);
		titleImage = (ImageView)findViewById(R.id.titleimg);
		gridView = (StaggeredGridView) findViewById(R.id.gridView);
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
		progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
		nullLay = (LinearLayout) findViewById(R.id.nullLay);
		floatingBtn = (ImageView) findViewById(R.id.floatingBtn);
		locationLay = (RelativeLayout) findViewById(R.id.locationLay);
		locationTxt = (TextView) findViewById(R.id.locationTxt);
		filterList = (HorizontalListView) findViewById(R.id.filterList);
		filterView= (View) findViewById(R.id.filterView);
        notifybtn = (ImageView) findViewById(R.id.notifybtn);
		reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
		ratingBar = (RatingBar) findViewById(R.id.ratingBar);
		ratingCount = (TextView) findViewById(R.id.ratingCount);


        notifyBadge = new BadgeView(FragmentMainActivity.this, notifybtn);

		swipeLayout.setProgressViewOffset(false, 0, TextbookTakeoverApplication.dpToPx(FragmentMainActivity.this, 70));

		// Elements Visibility
		filter_btn.setVisibility(View.VISIBLE);
		search_btn.setVisibility(View.VISIBLE);
		titleImage.setVisibility(View.VISIBLE);
		nullLay.setVisibility(View.GONE);
		progress.setVisibility(View.GONE);

		// Adapter for side menu
		adapter = new ItemAdapter(FragmentMainActivity.this, R.layout.menu_list_item, ids);
		listView.setAdapter(adapter);

		toggle =  new ActionBarDrawerToggle(this, drawer,null, R.string.open, R.string.close) {

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				Log.v("Drawer", "Drawer Opened");
                if (GetSet.isLogged()){
                    new getcountdetails().execute();
                }
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				Log.v("Drawer", "Drawer Closed");
				if (mDrawerItemClicked){
					mDrawerItemClicked = false;
					openActivity(Model.GetbyId(Integer.parseInt(ids[mDrawerPosition])).name);
				}
			}
		};

		Constants.filpref = getApplicationContext().getSharedPreferences("FilterPref",
				MODE_PRIVATE);
		Constants.fileditor = Constants.filpref.edit();

		LocationActivity.location = Constants.filpref.getString("location", getString(R.string.world_wide));
		LocationActivity.lat = Double.parseDouble(Constants.filpref.getString("lat", "0"));
		LocationActivity.lon = Double.parseDouble(Constants.filpref.getString("lon", "0"));
		LocationActivity.locationRemoved = Constants.filpref.getBoolean("locationRemoved", false);

		if (SearchAdvance.categoryId.size() > 0 || !SearchAdvance.distance.equals("0") || !SearchAdvance.sortBy.equals("1")
				|| !SearchAdvance.postedWithin.equals("")) {
			filterAry.clear();
			getFilterAry();
			Log.v("filterAry", "filterAry=" + filterAry);
			setFilterAdapter();
			filterList.setVisibility(View.VISIBLE);
			filterView.setVisibility(View.VISIBLE);
			filter_btn.setColorFilter(getResources().getColor(R.color.colorPrimary));

		} else {
			filterAry.clear();
			filterList.setVisibility(View.GONE);
			filterView.setVisibility(View.GONE);
			filter_btn.setColorFilter(getResources().getColor(R.color.colorAccent));
		}

		//drawer.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		drawer.setDrawerListener(toggle);
		drawer.post(new Runnable() {
			@Override
			public void run() {
				toggle.syncState();
			}
		});

		// Home header banner
		LayoutInflater inflater = getLayoutInflater();
		header = inflater.inflate(R.layout.home_banner, null);
		if (homeBanner.equalsIgnoreCase("enable") && bannerAry.size() > 0){
			gridView.addHeaderView(header);
		}

		viewPager = (AutoScrollViewPager) header.findViewById(R.id.view_pager);
		pageIndicator = (LinePageIndicator) header.findViewById(R.id.indicator);

		display = this.getWindowManager().getDefaultDisplay();

		float scale = (float) display.getWidth() / Constants.HOME_BANNER_WIDTH;
		int newHeight = (int) Math.round(Constants.HOME_BANNER_HEIGHT * scale);
		viewPager.getLayoutParams().height = newHeight;

		pagerAdapter = new BannerPagerAdapter(this, bannerAry);
		viewPager.setAdapter(pagerAdapter);
		pageIndicator.setViewPager(viewPager);

		viewPager.addOnPageChangeListener(mOnPageChangeListener);

		// Elements Listener
		listView.setOnItemClickListener(new DrawerItemClickListener());
		gridView.setOnItemClickListener(new ItemClickListener());
		gridView.setOnGridRefreshListener(this);
		login.setOnClickListener(this);
		filter_btn.setOnClickListener(this);
		search_btn.setOnClickListener(this);
		menu_btn.setOnClickListener(this);
		gridView.setOnScrollListener(this);
		swipeLayout.setOnRefreshListener(this);
		locationLay.setOnClickListener(this);
		login.setOnClickListener(this);
		floatingBtn.setOnClickListener(this);
		headerLay.setOnClickListener(this);
        notifybtn.setOnClickListener(this);

		// For Set Login & Logout State
        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
				MODE_PRIVATE);
		Constants.editor = Constants.pref.edit();
		if (Constants.pref.getBoolean("isLogged", false)) {
			GetSet.setLogged(true);
			GetSet.setUserId(Constants.pref.getString("userId", null));
			GetSet.setUserName(Constants.pref.getString("userName", null));
			GetSet.setEmail(Constants.pref.getString("Email", null));
			GetSet.setPassword(Constants.pref.getString("Password", null));
			GetSet.setFullName(Constants.pref.getString("fullName", null));
			GetSet.setImageUrl(Constants.pref.getString("photo", null));
			GetSet.setRating(Constants.pref.getString("rating", "0"));
			profheader.setVisibility(View.VISIBLE);
			proflogin.setVisibility(View.GONE);
		} else{
			profheader.setVisibility(View.GONE);
			proflogin.setVisibility(View.VISIBLE);
		}

		if (GetSet.isLogged()) {
			profheader.setVisibility(View.VISIBLE);
			proflogin.setVisibility(View.GONE);
			username.setText(GetSet.getFullName());
			userid.setText(GetSet.getUserName());
			if (GetSet.getImageUrl() != null && !GetSet.getImageUrl().equals("")){
				Log.v("getImageurl", "getImageurl="+GetSet.getImageUrl());
				Picasso.with(FragmentMainActivity.this).load(GetSet.getImageUrl()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImage);
			}
		}else{
			profheader.setVisibility(View.GONE);
			proflogin.setVisibility(View.VISIBLE);
		}

		if (Constants.BUYNOW){
			reviewLay.setVisibility(View.VISIBLE);
			userid.setVisibility(View.GONE);
			try {
				ratingBar.setRating(Float.parseFloat(GetSet.getRating()));
				ratingCount.setText("(" + GetSet.getRating() + ")");
			} catch (NullPointerException e){
				e.printStackTrace();
			} catch (NumberFormatException e){
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
		} else {
			reviewLay.setVisibility(View.GONE);
			userid.setVisibility(View.VISIBLE);
		}

		Log.v("getRating", "getRating="+GetSet.getRating());

		LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
		stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
		stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
		stars.getDrawable(1).setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);

		swipeLayout.setColorSchemeColors(getResources().getColor(R.color.swipeColor));
		Display display = this.getWindowManager().getDefaultDisplay();
		screenWidth= display.getWidth();
		screenHeight= display.getHeight();
		screenHalf= screenWidth/2;

		// Setting Dialog Title
		alertDialog = new AlertDialog.Builder(FragmentMainActivity.this).create();
		alertDialog.setTitle(getString(R.string.gps_settings));
		alertDialog.setMessage(getString(R.string.gps_notenabled));
		alertDialog.setButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				networkEnable = true;
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivityForResult(intent, 3);
			}
		});
		alertDialog.setButton2(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alertDialog.setCancelable(false);

		gps = new GPSTracker(FragmentMainActivity.this);

		setLocationTxt();
		loadData();
	}

	private void loadData(){
		if(HomeItems.size()==0){
			new homeLoadItems().execute(0);
			homeAdapter = new HomeAdapter(FragmentMainActivity.this, HomeItems);
			gridView.setAdapter(homeAdapter);
		} else {
			homeAdapter = new HomeAdapter(FragmentMainActivity.this, HomeItems);
			gridView.setAdapter(homeAdapter);
		}
	}

	private void swipeRefresh(){
		swipeLayout.post(new Runnable() {
			@Override
			public void run() {
				swipeLayout.setRefreshing(true);
			}
		});
	}

	/** function for get the location from gps **/
	private void setLocationTxt(){
		gps = new GPSTracker(FragmentMainActivity.this);
		if (LocationActivity.locationRemoved){
			locationTxt.setText(getString(R.string.world_wide));
		} else {
			if (LocationActivity.lat == 0  && LocationActivity.lon == 0){
				if (gps.canGetLocation()) {
					if (TextbookTakeoverApplication.isNetworkAvailable(FragmentMainActivity.this)) {
						LocationActivity.lat = gps.getLatitude();
						LocationActivity.lon = gps.getLongitude();
						Log.v("lati", "lat" + LocationActivity.lat);
						Log.v("longi", "longi" + LocationActivity.lon);
						new GetLocationAsync(LocationActivity.lat, LocationActivity.lon).execute();
					}
				} else {
					if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
						ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[]{ ACCESS_FINE_LOCATION,  ACCESS_COARSE_LOCATION}, 102);
					} else {
						if (!alertDialog.isShowing()){
							alertDialog.show();
						}
					}
				}
			} else if(!LocationActivity.location.equals(getString(R.string.world_wide))){
				locationTxt.setText(LocationActivity.location);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	// home items //
	class homeLoadItems extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			int offset = (params[0] * 20);
			String sortid = "1";

			String SOAP_ACTION = Constants.NAMESPACE + Constants.API_HOME;

			SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_HOME);
			req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
			req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
			req.addProperty("type", "search");
			Log.v("LocationActivity.lat", "LocationActivity.lat=" + LocationActivity.lat);
			Log.v("LocationActivity.lon", "LocationActivity.lon=" + LocationActivity.lon);
			if (!Double.toString(LocationActivity.lat).equals("0.0")) {
				req.addProperty("lat", Double.toString(LocationActivity.lat));
			}
			if (!Double.toString(LocationActivity.lon).equals("0.0")) {
				req.addProperty("lon", Double.toString(LocationActivity.lon));
			}
			if (!SearchAdvance.distance.equals("0")) {
				req.addProperty("distance", SearchAdvance.distance);
			}
			if (!SearchAdvance.sortBy.equals("")) {
				req.addProperty("sorting_id", SearchAdvance.sortBy);
			}
			if (!SearchAdvance.postedWithin.equals("") && !SearchAdvance.postedWithin.equals("all")) {
				req.addProperty("posted_within", SearchAdvance.postedWithin);
			}
			if (SearchAdvance.categoryId.size() > 0) {
				ArrayList<String> main = new ArrayList<String>();
				ArrayList<String> sub = new ArrayList<String>();
				for (int i = 0; i < SearchAdvance.categoryId.size(); i++){
					String subc = SearchAdvance.subcategoryId.get(SearchAdvance.categoryId.get(i));
					if (subc == null || subc.equals("") || subc.equals("all")){
						main.add(SearchAdvance.categoryId.get(i));
					} else {
						sub.add(subc);
					}
				}
				if (main.size() > 0){
					req.addProperty("category_id", main.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
				}

				if (sub.size() > 0){
					req.addProperty("subcategory_id", sub.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
				}
			}
			req.addProperty("offset", Integer.toString(offset));
			req.addProperty("limit", "20");
			if (GetSet.isLogged()) {
				req.addProperty("user_id", GetSet.getUserId());
			}

			SOAPParsing soap = new SOAPParsing();
			final String json = soap.getJSONFromUrl(SOAP_ACTION, req);

			if (pulldown){
				HomeItems.clear();
			}
			FragmentMainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ArrayList<HashMap<String,String>> temp=new ArrayList<HashMap<String,String>>();
					ItemsParsing parse = new ItemsParsing(FragmentMainActivity.this);
					temp.addAll(parse.parsing(json));
					if (!HomeItems.contains(temp)){
						HomeItems.addAll(temp);
					}
					Log.v("HomeItems","HomeItems"+HomeItems);
				}
			});
			return null;
		}

		@Override
		protected void onPreExecute() {
			nullLay.setVisibility(View.INVISIBLE);
			if (pulldown) {
				gridView.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
			} else if (HomeItems.size() > 0) {
				gridView.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
				swipeRefresh();
			} else {
				gridView.setVisibility(View.INVISIBLE);
				progress.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onPostExecute(Void unused) {
			if(pulldown){
				pulldown=false;
				loading = true;
			}
			gridView.setVisibility(View.VISIBLE);
			swipeLayout.setRefreshing(false);
			progress.setVisibility(View.GONE);
			FragmentMainActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					homeAdapter.notifyDataSetChanged();
				}
			});
			if(HomeItems.size() == 0){
				header.setVisibility(View.GONE);
				nullLay.setVisibility(View.VISIBLE);
			}else{
				header.setVisibility(View.VISIBLE);
				nullLay.setVisibility(View.INVISIBLE);
			}
		}

	}

	public class HomeAdapter extends BaseAdapter {
		ArrayList<HashMap<String, String>> Items;
		private Context mContext;
		ViewHolder holder = null;

		public HomeAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
			mContext = ctx;
			Items = data;
		}

		@Override
		public int getCount() {

			return Items.size();
		}

		@Override
		public Object getItem(int position) {

			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private class ViewHolder {
			ImageView singleImage;
			TextView itemPrice, itemName, location, postedTime, productType;
			RelativeLayout imageLay;
		}

		@Override
		public View getView(final int position, View convertView,
							ViewGroup parent) {

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

				holder.singleImage.getLayoutParams().height=screenHalf;
				holder.imageLay.getLayoutParams().height=screenHalf;

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			try {
				final HashMap<String, String> tempMap = Items.get(position);

				//	holder.singleImage.setBackgroundColor(Integer.parseInt(tempMap.get(Constants.TAG_COLOR)));
				Picasso.with(FragmentMainActivity.this).load(tempMap.get(Constants.TAG_ITEM_URL_350)).into(holder.singleImage);
				holder.itemName.setText(tempMap.get(Constants.TAG_TITLE).trim());
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
				Log.v("time", "time="+timeAgo.timeAgo(timestamp));

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

	/** adapter for showing the applied filters **/
	public class FilterAdapter extends BaseAdapter {

		private Context mContext;
		ArrayList<HashMap<String, String>> datas;
		ViewHolder holder = null;

		public FilterAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
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
			ImageView crossIcon;
			TextView name;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.home_filter_item, parent, false);//layout
				holder = new ViewHolder();

				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.crossIcon = (ImageView) convertView.findViewById(R.id.cross_icon);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			try {
				final HashMap<String, String> map = datas.get(position);

				holder.name.setText(map.get("name"));

				holder.crossIcon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (map.get("type")){
							case "category":
								if (SearchAdvance.categoryId.size()>0){
									SearchAdvance.categoryId.remove(map.get("categoryId"));
									SearchAdvance.categoryName.remove(map.get("name"));
									SearchAdvance.subcategoryId.remove(map.get("categoryId"));
								}
								break;
							case "distance":
								SearchAdvance.distance = "0";
								SearchAdvance.distanceX = 0;
								break;
							case "postedWithin":
								SearchAdvance.postedWithin = "";
								break;
							case "sortBy":
								SearchAdvance.sortBy = "1";
								break;
						}
						filterAry.remove(position);
						filterAdapter.notifyDataSetChanged();
						swipeRefresh();
						currentPage = 0;
						previousTotal = 0;
						pulldown = true;
						if (TextbookTakeoverApplication.isNetworkAvailable(FragmentMainActivity.this)) {
							new homeLoadItems().execute(0);
						}
						if (filterAry.size() == 0) {
							filterList.setVisibility(View.GONE);
							filterView.setVisibility(View.GONE);
							filter_btn.setColorFilter(getResources().getColor(R.color.primaryText));
						}
						Log.v("filterAry", "filterAry"+ filterAry);
						Log.v("categoryId", "categoryId"+ SearchAdvance.categoryId);
						Log.v("categoryName", "categoryName"+ SearchAdvance.categoryName);
						Log.v("subcategoryId", "subcategoryId"+ SearchAdvance.subcategoryId);
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

	/** function for get the applied filters to Ary **/
	private void getFilterAry(){
		if (SearchAdvance.categoryId.size() > 0) {
			for (int i = 0; i < SearchAdvance.categoryId.size(); i++){
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("type", "category");
				map.put("name", SearchAdvance.categoryName.get(i));
				map.put("categoryId", SearchAdvance.categoryId.get(i));

				filterAry.add(map);
			}
		}

		if (!SearchAdvance.distance.equals("0")){
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("type", "distance");
			map.put("name", "Within "+SearchAdvance.distance + " Miles");
			filterAry.add(map);
		}
		if (!SearchAdvance.postedWithin.equals("")){
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("type", "postedWithin");
			map.put("name", SearchAdvance.postedTxt);
			filterAry.add(map);
		}
		if (!SearchAdvance.sortBy.equals("1")){
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("type", "sortBy");
			map.put("name", SearchAdvance.sortTxt);
			filterAry.add(map);
		}
	}

	private void setFilterAdapter(){
		filterAdapter = new FilterAdapter(FragmentMainActivity.this, filterAry);
		filterList.setAdapter(filterAdapter);
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			drawer.closeDrawers();
			mDrawerItemClicked = true;
			mDrawerPosition = position;
		}
	}

	private class ItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			int pos = (int) gridView.getItemAtPosition(position);
			if (HomeItems.size() > 0){
				Intent i = new Intent(FragmentMainActivity.this,
						DetailActivity.class);
				i.putExtra("data", HomeItems.get(pos));
				i.putExtra("position", pos);
				i.putExtra("from", "home");
				startActivity(i);
			}
		}
	}

	/** Adapter for showing banner image **/
	class BannerPagerAdapter extends PagerAdapter {
		Context context;
		LayoutInflater inflater;
		ArrayList<HashMap<String,String>> data;

		public BannerPagerAdapter(Context act, ArrayList<HashMap<String,String>> newary) {
			this.data = newary;
			this.context = act;
		}

		public int getCount() {
			return data.size();

		}

		public Object instantiateItem(ViewGroup collection, final int position) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View itemView = inflater.inflate(R.layout.banner_image,
					collection, false);

			ImageView image = (ImageView) itemView.findViewById(R.id.image);
			String img = data.get(position).get("image");
			Log.v("banner img", "img="+img);
			if (!img.equals("")){
				Picasso.with(FragmentMainActivity.this).load(img).into(image);
			}

			image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (Patterns.WEB_URL.matcher(data.get(position).get("url")).matches()) {
						Intent b = new Intent(Intent.ACTION_VIEW, Uri.parse(data.get(position).get("url")));
						startActivity(b);
					} else {
						Toast.makeText(FragmentMainActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
					}
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

	ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int state) {
			enableDisableSwipeRefresh( state == ViewPager.SCROLL_STATE_IDLE );
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {

		}
	};

	private void enableDisableSwipeRefresh(boolean enabled){
		if (enabled) {
			swipeLayout.setEnabled(true);
		} else {
			swipeLayout.setEnabled(false);
		}
	}

    /** class for getting notification and chat badge count**/
    class getcountdetails extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_COUNT_DETAILS;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_COUNT_DETAILS);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                JSONObject jobj = new JSONObject(res);
                String status = jobj.getString(Constants.TAG_STATUS);

                if (status.equalsIgnoreCase("true")) {
                    JSONObject result = jobj.getJSONObject(Constants.TAG_RESULT);
                    String notificationCount = DefensiveClass.optString(result, Constants.TAG_NOTIFICATION_COUNT);
                    chatCount = DefensiveClass.optString(result, Constants.TAG_CHAT_COUNT);

                    if (!notificationCount.equals("0") && !notificationCount.equals("")){
                        notifyBadge.setText(notificationCount);
                        notifyBadge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                        notifyBadge.setBadgeMargin(7);
                        notifyBadge.setTextSize(13);
                        notifyBadge.setGravity(Gravity.CENTER);
                        notifyBadge.show();
                    } else {
                        notifyBadge.hide();
                    }

                    if (!chatCount.equals("")){
                        adapter.notifyDataSetChanged();
                    }

                } else {

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

	/** function for open the corresponding activity from sliding menu **/
	public void openActivity(String from) {
        Log.v("from", "from="+from);
		if (from.equals(getString(R.string.sell_your_stuff))) {
			if (GetSet.isLogged()) {
				Intent m = new Intent(FragmentMainActivity.this, CameraActivity.class);
				m.putExtra("from", "camera");
				startActivity(m);
			} else {
				Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
				startActivity(i);
			}
		} else if (from.equals(getString(R.string.scan))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, ScannerActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.chat))) {
			if (GetSet.isLogged()) {
				Intent i = new Intent(FragmentMainActivity.this, MessageActivity.class);
				startActivity(i);
			} else {
				Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
				startActivity(i);
			}
		} else if (from.equals(getString(R.string.categories))) {
			Intent c = new Intent(FragmentMainActivity.this, CategoryActivity.class);
			startActivity(c);
		} else if (from.equals(getString(R.string.myprofile))) {
			if (GetSet.isLogged()) {
				Intent i = new Intent(FragmentMainActivity.this, Profile.class);
				i.putExtra("userId", GetSet.getUserId());
				startActivity(i);
			} else {
				Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
				startActivity(i);
			}
		} else if (from.equals(getString(R.string.myorders_sales))) {
			if (GetSet.isLogged()) {
				Intent i = new Intent(FragmentMainActivity.this, MySalesnOrder.class);
				startActivity(i);
			} else {
				Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
				startActivity(i);
			}
		} else if (from.equals(getString(R.string.myexchange))) {
			if (GetSet.isLogged()) {
				Intent i = new Intent(FragmentMainActivity.this, ExchangeActivity.class);
				startActivity(i);
			} else {
				Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
				startActivity(i);
			}
		} else if (from.equals(getString(R.string.my_promotions))) {
			if (GetSet.isLogged()) {
				Intent i = new Intent(FragmentMainActivity.this, MyPromotions.class);
				startActivity(i);
			} else {
				Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
				startActivity(i);
			}
		} else if (from.equals(getString(R.string.invite_friends))) {
			inviteDialog();
		} else if (from.equals(getString(R.string.help))) {
			Intent Hl = new Intent(FragmentMainActivity.this, Help.class);
			startActivity(Hl);
		}
	}

	/** class for get the address from lat, lon **/
	private class GetLocationAsync extends AsyncTask<String, Void, String> {

		// boolean duplicateResponse;
		double x, y;
		StringBuilder str;

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
				geocoder = new Geocoder(FragmentMainActivity.this, getResources().getConfiguration().locale);
				addresses = geocoder.getFromLocation(x, y, 1);
				str = new StringBuilder();
				if (geocoder.isPresent() && addresses.size() > 0) {

					Address returnAddress = addresses.get(0);

					String localityString = returnAddress.getLocality();
					String city = returnAddress.getCountryName();
					String region_code = returnAddress.getCountryCode();
					String zipcode = returnAddress.getPostalCode();

					str.append(localityString + "");
					str.append(city + "" + region_code + "");
					str.append(zipcode + "");

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
				if (addresses.size() > 0) {
					LocationActivity.location = addresses.get(0).getAddressLine(0)
							+ addresses.get(0).getAddressLine(1) + " ";
					locationTxt.setText(LocationActivity.location);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}
	}

    public void inviteDialog() {
        final Dialog dialog = new Dialog(FragmentMainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.invite_dialog);

        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        RelativeLayout fblay, whatsapplay, emaillay;
        fblay = (RelativeLayout) dialog.findViewById(R.id.fbLay);
        whatsapplay = (RelativeLayout) dialog.findViewById(R.id.whatsaplay);
        emaillay = (RelativeLayout) dialog.findViewById(R.id.emaillay);

		final String inviteContent = getString(R.string.invite_content) + " " + "https://play.google.com/store/apps/details?id=" +
				getApplicationContext().getPackageName();

        fblay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.facebook.orca");
                if (installed) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.facebook.orca");
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(FragmentMainActivity.this, "Facebook Messenger is not currently installed on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });
        whatsapplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.whatsapp");
                if (installed) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(FragmentMainActivity.this, "Whatsapp is not currently installed on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emaillay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.google.android.gm");
                if (installed) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/html");
                    sendIntent.setPackage("com.google.android.gm");
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{});
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + "!!! " + getString(R.string.invite_subject));
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(FragmentMainActivity.this, "Gmail is not currently installed on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							//moveTaskToBack(true);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
								Log.v("finishjb", "finishjb");
								FragmentMainActivity.this.finishAffinity();
							} else {
								Log.v("finish", "finish");
								moveTaskToBack(true);
								FragmentMainActivity.this.finish();
								//ActivityCompat.finishAffinity(FragmentChangeActivity.this);
							}
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getResources().getString(R.string.reallyExit))
					.setPositiveButton(getResources().getString(R.string.exit),
							dialogClickListener)
					.setNegativeButton(getResources().getString(R.string.keep),
							dialogClickListener).show();
		}
		else{
            super.onBackPressed();
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
						 int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem == 0) {
			swipeLayout.setEnabled(true);
		} else {
			swipeLayout.setEnabled(false);
		}
		if (!pulldown){
			if (loading) {
				if (totalItemCount > previousTotal) {
					loading = false;
					previousTotal = totalItemCount;
					currentPage++;
				}
			}

			if (!loading
					&& (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
				// I load the next page of thumbnails using a background task,
				if(currentPage != 0){
					new homeLoadItems().execute(currentPage);
					loading = true;
				}
			}
		}
	}

	@Override
	public void onRefresh() {
		if (!pulldown) {
			currentPage = 0;
			previousTotal = 0;
			pulldown = true;
			if (TextbookTakeoverApplication.isNetworkAvailable(FragmentMainActivity.this)) {
				setLocationTxt();
				new homeLoadItems().execute(0);
                if (GetSet.isLogged()){
                    new getcountdetails().execute();
                }
			} else {
				swipeLayout.setRefreshing(false);
			}
		} else {
			swipeLayout.setRefreshing(false);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.v("onActivityResult", "onActivityResult");
		if (networkEnable && requestCode == 3){
			swipeRefresh();
			Log.v("networkEnable", "networkEnable");
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					gps = new GPSTracker(FragmentMainActivity.this);
					if (gps.canGetLocation()){
						Log.v("gps", "gps");
						networkEnable = false;
						LocationActivity.lat = gps.getLatitude();
						LocationActivity.lon = gps.getLongitude();
						Log.v("lat", "lat=" + LocationActivity.lat);
						Log.v("lon", "lon=" + LocationActivity.lon);
						try {
							new GetLocationAsync(LocationActivity.lat, LocationActivity.lon).execute().get();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
						currentPage = 0;
						previousTotal = 0;
						pulldown = true;
						if (TextbookTakeoverApplication.isNetworkAvailable(FragmentMainActivity.this)) {
							new homeLoadItems().execute(0);
						}
					}
				}
			}, 3000);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// For Internet checking disconnect
		TextbookTakeoverApplication.unregisterReceiver(FragmentMainActivity.this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v("resume", "resume" + HomeItems.size());
		// For Internet checking
		TextbookTakeoverApplication.registerReceiver(FragmentMainActivity.this);

        if (GetSet.isLogged()){
            new getcountdetails().execute();
        }

		if (SearchAdvance.applyFilter) {
			SearchAdvance.applyFilter = false;
			homeAdapter = new HomeAdapter(FragmentMainActivity.this, HomeItems);
			gridView.setAdapter(homeAdapter);
			swipeRefresh();
			currentPage = 0;
			previousTotal = 0;
			pulldown = true;
			if (TextbookTakeoverApplication.isNetworkAvailable(FragmentMainActivity.this)) {
				new homeLoadItems().execute(0);
			}
		}

	}

	@Override
	public void onGridRefresh() {
		Log.v("onGridRefresh", "onGridRefresh");
		HomeItems.clear();
		homeAdapter = new HomeAdapter(FragmentMainActivity.this, HomeItems);
		gridView.setAdapter(homeAdapter);
		swipeRefresh();
		currentPage = 0;
		previousTotal = 0;
		pulldown = true;
		if (TextbookTakeoverApplication.isNetworkAvailable(FragmentMainActivity.this)) {
			new homeLoadItems().execute(0);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Log.v("requestCode", "requestCode=" + requestCode);
		if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
			Toast.makeText(FragmentMainActivity.this, getString(R.string.location_permission_access), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(FragmentMainActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.login:
				Intent i = new Intent(FragmentMainActivity.this,WelcomeActivity.class);
				startActivity(i);
				break;
			case R.id.filterbtn:
				Intent j = new Intent(FragmentMainActivity.this,SearchAdvance.class);
				startActivity(j);
				break;
			case R.id.menubtn:
				drawer.openDrawer(Gravity.LEFT);
				break;
			case R.id.locationLay:
				Intent k = new Intent(FragmentMainActivity.this, LocationActivity.class);
				k.putExtra("from", "home");
				startActivity(k);
				break;
			case R.id.searchbtn:
				Intent l = new Intent(FragmentMainActivity.this, SearchActivity.class);
				startActivity(l);
				break;
			case R.id.floatingBtn:
				if (GetSet.isLogged()) {
					Intent m = new Intent(FragmentMainActivity.this, CameraActivity.class);
					m.putExtra("from", "camera");
					startActivity(m);
				} else {
					Intent m = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
					startActivity(m);
				}
				break;
			case R.id.header:
				drawer.closeDrawers();
				if (GetSet.isLogged()) {
					Intent n = new Intent(FragmentMainActivity.this, Profile.class);
					n.putExtra("userId", GetSet.getUserId());
					startActivity(n);
				} else {
					Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
					startActivity(n);
				}
				break;
            case R.id.notifybtn:
				if (GetSet.isLogged()) {
					Intent o = new Intent(FragmentMainActivity.this, Notification.class);
					startActivity(o);
				} else {
					Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
					startActivity(n);
				}
                break;
		}
	}

}
