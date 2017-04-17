package com.hitasoft.app.textbooktakeover;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.ItemsParsing;

import com.hitasoft.app.utils.SOAPParsing;

import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CreateExchange extends AppCompatActivity implements OnClickListener,OnScrollListener,OnItemClickListener{

	GridView gridView;
	TextView title,create,cancel;
	boolean loading=true, pulldown=false;
	AVLoadingIndicatorView progress;
	String select=null,itemId;
	ImageView  cancelBtn;
	HomeAdapter homeAdapter;
	int screenWidth, visibleThreshold=0, previousTotal=0, currentPage=0;
	LinearLayout nullLay;
	ArrayList<HashMap<String,String>> HomeItems=new ArrayList<HashMap<String,String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exchange_layout);
		
		gridView = (GridView) findViewById(R.id.gridView);
		nullLay = (LinearLayout) findViewById(R.id.nullLay);
		create = (TextView) findViewById(R.id.post);
		cancel = (TextView) findViewById(R.id.later);
		progress = (AVLoadingIndicatorView)findViewById(R.id.progress);
		cancelBtn = (ImageView) findViewById(R.id.backbtn);
		title = (TextView) findViewById(R.id.title);

		title.setVisibility(View.VISIBLE);
		cancelBtn.setVisibility(View.VISIBLE);
		nullLay.setVisibility(View.GONE);
		progress.setVisibility(View.GONE);

		Display display = getWindowManager().getDefaultDisplay();
		int width= display.getWidth();
		screenWidth= width*50/100;
		
		cancelBtn.setOnClickListener(this);
		create.setOnClickListener(this);
		cancel.setOnClickListener(this);
		gridView.setOnScrollListener(this);
		gridView.setOnItemClickListener(this);
		
		gridView.setSmoothScrollbarEnabled(true);

		title.setText(getString(R.string.exchangetobuy));
		create.setText(getString(R.string.create));
		cancel.setText(getString(R.string.cancel));

		itemId = getIntent().getExtras().getString("itemId");
		
		loadHomeData();
	}
	
	private void loadHomeData() {
		try {
			if(HomeItems.size()==0){
			  try {
		        if (TextbookTakeoverApplication.isNetworkAvailable(CreateExchange.this)) {
					HomeItems.clear();
				   new homeLoadItems().execute(0);
		        }
			  } catch (Exception e) {
					e.printStackTrace();
			  }
			homeAdapter = new HomeAdapter(CreateExchange.this,HomeItems);
			gridView.setAdapter(homeAdapter);
			}else{
				homeAdapter = new HomeAdapter(CreateExchange.this,HomeItems);
				gridView.setAdapter(homeAdapter);
				nullLay.setVisibility(View.GONE);
			}
		} catch(NullPointerException e){
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	     // home items //
		class homeLoadItems extends AsyncTask<Integer, Void, Void> {

			@Override
			protected Void doInBackground(Integer... params) {
				int offset= (params[0]*20);
				if(params[0]==0){
					HomeItems.clear();
					Log.v("cleared","cleared");
				}
				
				String SOAP_ACTION = Constants.NAMESPACE + Constants.API_HOME;

				SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_HOME);
				req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
				req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
				req.addProperty("type", "moreitems");
				req.addProperty("seller_id", GetSet.getUserId());
				req.addProperty("offset", Integer.toString(offset));
				req.addProperty("limit", "20");
				
				SOAPParsing soap = new SOAPParsing();
				String json = soap.getJSONFromUrl(SOAP_ACTION, req);
				
				ItemsParsing parse =new ItemsParsing(CreateExchange.this);
				HomeItems.addAll(parse.parsing(json));
				return null;
			}

			@Override
			protected void onPreExecute() {
				nullLay.setVisibility(View.INVISIBLE);
				if (HomeItems.size() > 0) {
					gridView.setVisibility(View.VISIBLE);
					progress.setVisibility(View.GONE);
				} else {
					gridView.setVisibility(View.INVISIBLE);
					progress.setVisibility(View.VISIBLE);
				}
			}

			@Override
			protected void onPostExecute(Void unused) {
				gridView.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
				homeAdapter.notifyDataSetChanged();
				if(HomeItems.size() == 0){
					nullLay.setVisibility(View.VISIBLE);
				}else{
					nullLay.setVisibility(View.INVISIBLE);
				}
			}
			
		}


		public class HomeAdapter extends BaseAdapter {
			ArrayList<HashMap<String, String>> Items;
			private Context mContext;
			ViewHolder holder = null;
			public HomeAdapter(Context ctx,ArrayList<HashMap<String, String>> data) {
				mContext = ctx;
				Items=data;
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
				ImageView singleImage2,singleImage,tick;
				
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
			      if (convertView == null) {
			    	  LayoutInflater inflater = (LayoutInflater) mContext
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			    	    convertView = inflater.inflate(R.layout.singlegridimg, parent, false);//layout
						holder = new ViewHolder();
						holder.singleImage = (ImageView) convertView.findViewById(R.id.imageView);
						holder.singleImage2 = (ImageView) convertView.findViewById(R.id.imageView2);
						holder.tick = (ImageView) convertView.findViewById(R.id.tick);

						holder.singleImage.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth));
						holder.singleImage2.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenWidth));

						convertView.setTag(holder);
			      } else {
			    	  holder = (ViewHolder) convertView.getTag();
			      }
			      
			    try{
			    	
	            final HashMap<String, String> tempMap=Items.get(position);

	            Picasso.with(CreateExchange.this).load(tempMap.get(Constants.TAG_ITEM_URL_350)).into(holder.singleImage);
	            
	            if(select == null){
					holder.tick.setVisibility(View.GONE);
					holder.singleImage2.setVisibility(View.GONE);
	            }else{
	            	if(select.equals(Integer.toString(position))){
	            		holder.tick.setVisibility(View.VISIBLE);
						holder.singleImage2.setVisibility(View.VISIBLE);
					}else{
						holder.tick.setVisibility(View.GONE);
						holder.singleImage2.setVisibility(View.GONE);
					}
	            }
	           
			    }catch(NullPointerException e){
			    	e.printStackTrace();
			    } catch(Exception e){
			    	e.printStackTrace();
			    }
				return convertView;
			}
			
		}

	/** class for create exchanges **/
		class createexchange extends AsyncTask<Integer, Void, String> {

			@Override
			protected String doInBackground(Integer... params) {
				
				String SOAP_ACTION = Constants.NAMESPACE + Constants.API_CREATE_EXCHANGE;

				SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_CREATE_EXCHANGE);
				req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
				req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
				req.addProperty("user_id", GetSet.getUserId());
				req.addProperty("myitem_id", itemId);
				req.addProperty("exchangeitem_id", HomeItems.get(Integer.parseInt(select)).get(Constants.TAG_ID));
				
				SOAPParsing soap = new SOAPParsing();
				String json = soap.getJSONFromUrl(SOAP_ACTION, req);
				
				return json;
			}

			@Override
			protected void onPreExecute() {

			}

			@Override
			protected void onPostExecute(String result) {
	    		try {
					JSONObject jobj = new JSONObject(result);
					String status = jobj.getString(Constants.TAG_STATUS);
					create.setOnClickListener(CreateExchange.this);
					if(status.equals("true")){
						dialog(CreateExchange.this, getResources().getString(R.string.success), jobj.getString("result"));
					}else{
						TextbookTakeoverApplication.dialog(CreateExchange.this, getResources().getString(R.string.alert), jobj.getString("message"));
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				} catch(NullPointerException e){
					e.printStackTrace();
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}
		
		public void dialog(Context ctx, String title,String content){
			final Dialog dialog = new Dialog(ctx ,R.style.AlertDialog);
			Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			dialog.setContentView(R.layout.default_dialog);
			dialog.getWindow().setLayout(display.getWidth()*90/100, LayoutParams.WRAP_CONTENT);
			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(false);
			
			TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
			TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
			ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
			TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);
			
			alertTitle.setText(title);
			alertMsg.setText(content);
			alertIcon.setImageResource(R.drawable.success_icon);

			alertOk.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					finish();
					dialog.dismiss();
					ExchangeActivity.type="outgoing";
					Intent i = new Intent(CreateExchange.this, ExchangeActivity.class);
					startActivity(i);
				}
			});
			
			if(!dialog.isShowing()){
				dialog.show();
			}
			
		}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
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
			Log.v("hai","ram"+currentPage);
			loading = true;
			new homeLoadItems().execute(currentPage);

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// For Internet checking disconnect
		TextbookTakeoverApplication.unregisterReceiver(CreateExchange.this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// For Internet checking
		TextbookTakeoverApplication.registerReceiver(CreateExchange.this);
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.backbtn:
		case R.id.later:
			finish();
			break;
		case R.id.post:
			if (select == null){
				TextbookTakeoverApplication.dialog(CreateExchange.this, getResources().getString(R.string.alert), getResources().getString(R.string.please_select_exchange));
			} else{
				create.setOnClickListener(null);
				new createexchange().execute();
			}
		
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	
		select = Integer.toString(position);
		Log.v("select","select"+ select);
		homeAdapter.notifyDataSetChanged();
	}
}
