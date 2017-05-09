package com.app.textbooktakeover;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.helper.Chat;
import com.app.helper.ChatCallbackAdapter;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends Activity implements OnClickListener,ChatCallbackAdapter,TextWatcher, OnScrollListener{

	ImageView backBtn, userImg;
	TextView title, nullText, typing, send, dateTxt;
	ListView listView;
	String userName,userId,chatId,userImage,lastDate="",chatUrl="", from="";
	public static Chat chat;
	boolean pulldown=false,loading = false, aboutMessageSent = false, meTyping, receiverTyping;
	int black,currentPage=0, topPadding, leftPadding, rightPadding;
	AVLoadingIndicatorView progress, topProgress;
	EditText editText;
	ChatAdapter chatAdapter;
	ViewGroup header, footer;
	RelativeLayout main;
	LinearLayout bottom;
	ArrayList<HashMap<String, String>> chats= new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String,String>> tempAry=new ArrayList<HashMap<String,String>>();
	Handler handler = new Handler();
	Runnable runnable;
	HashMap<String,String> data = new HashMap<String,String>();
	public static String fullName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_layout);

		try{

			backBtn = (ImageView) findViewById(R.id.backbtn);
			title = (TextView) findViewById(R.id.username);
			listView = (ListView) findViewById(R.id.listView);
			send = (TextView) findViewById(R.id.send);
			editText = (EditText) findViewById(R.id.editText);
			progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
			main = (RelativeLayout) findViewById(R.id.main);
			userImg = (ImageView) findViewById(R.id.userImg);
			dateTxt = (TextView) findViewById(R.id.dateTxt);
			bottom = (LinearLayout) findViewById(R.id.bottom);

			LayoutInflater inflater = getLayoutInflater();
			header = (ViewGroup) inflater.inflate(R.layout.chat_header, null);
			listView.addHeaderView(header);

			LayoutInflater inflater2 = getLayoutInflater();
			footer = (ViewGroup) inflater2.inflate(R.layout.chat_footer, null);
			listView.addFooterView(footer);
			listView.setSmoothScrollbarEnabled(true);
			listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

			topProgress = (AVLoadingIndicatorView) header.findViewById(R.id.topProgress);
			nullText = (TextView) header.findViewById(R.id.nulltext);
			typing = (TextView) footer.findViewById(R.id.typing);

			backBtn.setVisibility(View.VISIBLE);
			title.setVisibility(View.VISIBLE);
			topProgress.setVisibility(View.GONE);
			nullText.setVisibility(View.GONE);
			typing.setVisibility(View.GONE);
			userImg.setVisibility(View.VISIBLE);
			dateTxt.setVisibility(View.GONE);

			topPadding = TextbookTakeoverApplication.dpToPx(this, 10);
			leftPadding = TextbookTakeoverApplication.dpToPx(this, 18);
			rightPadding = TextbookTakeoverApplication.dpToPx(this, 12);

			chat = new Chat(this);
			chat.start();

			//  TextbookTakeoverApplication.setupUI(ChatActivity.this, main);

			userName = getIntent().getExtras().getString("userName");
			userId = getIntent().getExtras().getString("userId");
			chatId = getIntent().getExtras().getString("chatId");
			userImage = getIntent().getExtras().getString("userImage");
			from = getIntent().getExtras().getString("from");
			fullName = getIntent().getExtras().getString("fullName");

			if(from.equals("detail")){
				data = (HashMap<String,String>) getIntent().getExtras().get("data");
			}

			black = getResources().getColor(R.color.black);

			/** Function for join the user to chat **/
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					JSONObject jobj = new JSONObject();
					try {
						jobj.put("joinid", GetSet.getUserName());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					chat.join("join",jobj);
				}
			}, 2000);

			backBtn.setOnClickListener(this);
			send.setOnClickListener(this);
			editText.addTextChangedListener(this);
			editText.setFilters(new InputFilter[]{TextbookTakeoverApplication.EMOJI_FILTER});

			chatAdapter = new ChatAdapter(ChatActivity.this, chats);
			listView.setAdapter(chatAdapter);

			Log.v("userName", "userName="+userName);
			Log.v("userImage", "userImage="+userImage);
			Picasso.with(ChatActivity.this).load(userImage).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);
			title.setText(fullName);

		} catch(NullPointerException e){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}

		try{
			new GetChat().execute(0);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/** class for get the conversation between the selected user **/
	class GetChat extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				int offset= (params[0]*20);

				String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_CHAT;

				SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_CHAT);
				req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
				req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
				req.addProperty("sender_id", GetSet.getUserId());
				req.addProperty("receiver_id", userId);
				req.addProperty("type", "normal");
				req.addProperty("offset", Integer.toString(offset));
				req.addProperty("limit", "20");
				req.addProperty("source_id", "0");

				SOAPParsing soap = new SOAPParsing();
				String json = soap.getJSONFromUrl(SOAP_ACTION, req);
				tempAry.clear();
				tempAry.addAll(parsing(json));
				Collections.reverse(tempAry);
				ArrayList<HashMap<String,String>> backup = new ArrayList<HashMap<String,String>>();
				backup.addAll(chats);
				chats.clear();
				chats.addAll(tempAry);
				chats.addAll(backup);
				Log.v("hai", "chatactivity" + chats);

			} catch (Exception e) {
				e.printStackTrace();
				Log.v("error", e.toString());
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			loading= true;
			if (pulldown) {
				listView.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
			} else {
				listView.setVisibility(View.INVISIBLE);
				progress.setVisibility(View.VISIBLE);
				bottom.setVisibility(View.GONE);
			}

		}

		@Override
		protected void onPostExecute(Void unused) {
			try {

				if (tempAry.size()==0){
					listView.setOnScrollListener(null);
				}else{
					listView.setOnScrollListener(ChatActivity.this);
				}
				if(pulldown){
					pulldown=false;
					listView.setSelection(chats.size()-1);
					chatAdapter.notifyDataSetChanged();
					listView.setSelection(tempAry.size());

				}else{
					chatAdapter.notifyDataSetChanged();
					if(chats.size()>0){
						listView.setSelection(chats.size()-1);
					}
				}

				if (chats.size() == 0){
					dateTxt.setVisibility(View.GONE);
				} else {
					dateTxt.setVisibility(View.VISIBLE);
				}

				if (chats.size() > 18){
					if (tempAry.size()==0){
						nullText.setVisibility(View.GONE);
						topProgress.setVisibility(View.GONE);
						dateTxt.setVisibility(View.GONE);
					}
				}

				loading= false;
				bottom.setVisibility(View.VISIBLE);
				topProgress.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
			} catch (Exception e) {
				Log.d("doIndb", e.toString());
			}
		}
	}

	private ArrayList<HashMap<String,String>> parsing(String url) {
		ArrayList<HashMap<String, String>> chats = new ArrayList<HashMap<String, String>>();
		try {
			JSONObject json = new JSONObject(url);
			String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
			if(response.equalsIgnoreCase("true")){
				String chatId = DefensiveClass.optString(json, Constants.TAG_CHAT_ID);
				chatUrl = DefensiveClass.optString(json, Constants.TAG_CHAT_URL);
				JSONObject chobj = json.optJSONObject(Constants.TAG_CHATS);
				if(chobj != null){
					JSONArray chat = chobj.optJSONArray(Constants.TAG_CHATS);
					if(chat != null){
						for (int i=0 ; i<chat.length() ; i++){
							HashMap<String, String> map = new HashMap<String, String>();
							JSONObject temp = chat.getJSONObject(i);
							String receiver = DefensiveClass.optString(temp, Constants.TAG_RECEIVER);
							String sender = DefensiveClass.optString(temp, Constants.TAG_SENDER);
							String type = DefensiveClass.optString(temp, Constants.TAG_TYPE);
							String itemId = DefensiveClass.optString(temp, Constants.TAG_ITEM_ID);
							String itemTitle = DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE);
							String itemImage = DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE);
							String offerPrice = DefensiveClass.optString(temp, Constants.TAG_OFFER_PRICE);
							JSONObject msg = temp.getJSONObject(Constants.TAG_MESSAGE);
							String userName = DefensiveClass.optString(msg, Constants.TAG_userName);
							String userImage = Constants.url + "user/resized/50/" + DefensiveClass.optString(msg, Constants.TAG_userImage);
							String chatTime = DefensiveClass.optString(msg, Constants.TAG_CHATTIME);
							String message = DefensiveClass.optString(msg, Constants.TAG_MESSAGE);

							map.put("message", message);
							map.put("sender", sender);
							map.put("date", chatTime);
							map.put(Constants.TAG_TYPE, type);
							map.put(Constants.TAG_ITEM_ID, itemId);
							map.put(Constants.TAG_ITEM_TITLE, itemTitle);
							map.put(Constants.TAG_ITEM_IMAGE, itemImage);
							map.put(Constants.TAG_OFFER_PRICE, offerPrice);
							chats.add(map);
						}
					}
				}
			} else if (response.equalsIgnoreCase("error")){
				TextbookTakeoverApplication.disabledialog(ChatActivity.this, json.optString("message"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return chats;
	}

	/**  adapter for list the conversation in listview**/
	public class ChatAdapter extends BaseAdapter {
		ArrayList<HashMap<String, String>> Items;
		private Context mContext;
		ViewHolder holder = null;
		String lastDate="";
		public ChatAdapter(Context ctx,ArrayList<HashMap<String, String>> data) {
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
			LinearLayout main;
			TextView message,date, itemName, aboutDate, price, aboutMsg;
			ImageView leftimage, itemImage;
			RelativeLayout dateLay, itemLay;
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.chat_item, parent, false);//layout

				holder = new ViewHolder();
				holder.message = (TextView) convertView.findViewById(R.id.textView);
				holder.main = (LinearLayout) convertView.findViewById(R.id.main);
				holder.leftimage = (ImageView) convertView.findViewById(R.id.left_image);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.dateLay = (RelativeLayout) convertView.findViewById(R.id.dateLay);
				holder.itemLay = (RelativeLayout) convertView.findViewById(R.id.itemLay);
				holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
				holder.aboutDate = (TextView) convertView.findViewById(R.id.aboutDate);
				holder.price = (TextView) convertView.findViewById(R.id.price);
				holder.itemImage = (ImageView) convertView.findViewById(R.id.itemImage);
				holder.aboutMsg = (TextView) convertView.findViewById(R.id.aboutMsg);
				//	holder.message.setPadding(20, 10, 20, 10);

				holder.leftimage.setVisibility(View.GONE);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			try{
				final HashMap<String, String> tempMap=Items.get(position);

				if(tempMap.get("message") != null){
					holder.message.setText(TextbookTakeoverApplication.stripHtml(tempMap.get("message")));
				}

				String chatDate = getDate(Long.parseLong(tempMap.get("date")));

				holder.main.setVisibility(View.GONE);
				holder.itemLay.setVisibility(View.GONE);

				switch (tempMap.get(Constants.TAG_TYPE)) {
					case "message":
						holder.main.setVisibility(View.VISIBLE);
						holder.itemLay.setVisibility(View.GONE);
						if(tempMap.get("sender").equals(GetSet.getUserName())){
							holder.main.setGravity(Gravity.RIGHT);
							holder.message.setPadding(rightPadding, topPadding, leftPadding, topPadding);
							holder.message.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_bg_right));
							//holder.leftimage.setVisibility(View.GONE);
						} else{
							//Log.v("check","getsetid"+GetSet.getUserName());
							holder.main.setGravity(Gravity.LEFT);
							holder.message.setPadding(leftPadding, topPadding, rightPadding, topPadding);
							holder.message.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_bg_left));
							/*if (position > 0){
								if (!Items.get(position-1).get("sender").equals(GetSet.getUserName())){
									//holder.leftimage.setVisibility(View.INVISIBLE);
								}else{
									//holder.leftimage.setVisibility(View.VISIBLE);
									//Picasso.with(ChatActivity.this).load(userImage).into(holder.leftimage);
								}
							} else{
								//holder.leftimage.setVisibility(View.VISIBLE);
								//Picasso.with(ChatActivity.this).load(userImage).into(holder.leftimage);
							}*/
						}
						break;
					case "about":
						holder.main.setVisibility(View.GONE);
						holder.itemLay.setVisibility(View.VISIBLE);
						holder.price.setVisibility(View.GONE);
						holder.aboutMsg.setVisibility(View.VISIBLE);
						Picasso.with(ChatActivity.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.itemImage);
						String name = "About " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + tempMap.get(Constants.TAG_ITEM_TITLE) + "</font>";
						holder.itemName.setText(Html.fromHtml(name));
						holder.aboutDate.setText(chatDate);
						holder.aboutMsg.setText(TextbookTakeoverApplication.stripHtml(tempMap.get("message")));
						break;
					case "offer":
						holder.main.setVisibility(View.GONE);
						holder.itemLay.setVisibility(View.VISIBLE);
						holder.price.setVisibility(View.VISIBLE);
						holder.aboutMsg.setVisibility(View.VISIBLE);
						Picasso.with(ChatActivity.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.itemImage);
						String name2 = "Sent offer request on " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + tempMap.get(Constants.TAG_ITEM_TITLE) + "</font>";
						holder.itemName.setText(Html.fromHtml(name2));
						holder.aboutDate.setText(chatDate);
						holder.price.setText(tempMap.get(Constants.TAG_OFFER_PRICE));
						holder.aboutMsg.setText(TextbookTakeoverApplication.stripHtml(tempMap.get("message")));
						break;
				}

				//holder.date.setVisibility(View.VISIBLE);
				//holder.date.setText(getChatDate(tempMap.get("date")));

				if (position == 0){
					holder.dateLay.setVisibility(View.VISIBLE);
					holder.date.setText(chatDate);
					//	lastDate = chatDate;
				} else{
					String ldate = getDate(Long.parseLong(Items.get(position-1).get("date")));
					if (ldate.equals(chatDate)){
						holder.dateLay.setVisibility(View.GONE);
						lastDate = chatDate;
					} else{
						Log.v("lastdt"+ldate,"chatdt"+chatDate);
						holder.dateLay.setVisibility(View.VISIBLE);
						holder.date.setText(chatDate);
						lastDate = chatDate;
					}
				}

             /*   if (position != 0 && lastDate.equals(chatDate)){
                    holder.date.setVisibility(View.GONE);
					lastDate = chatDate;
                } else{
                    holder.date.setVisibility(View.VISIBLE);
                    holder.date.setText(chatDate);
                    lastDate = chatDate;
                }*/

			}catch(NullPointerException e){
				e.printStackTrace();
			} catch(Exception e){
				e.printStackTrace();
			}
			return convertView;
		}

	}

	/**
	 * To convert timestamp to Date
	 **/
	private String getDate(long timeStamp) {

		try {
			DateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
			Date netDate = (new Date(timeStamp * 1000));
			return getChatDate(sdf.format(netDate));
		} catch (Exception ex) {
			return "xx";
		}
	}

	/** for converting the date to specific format **/
	private String getChatDate(String date){
		if (date.contains("-")){
			String data[] = date.split("-");
			String day = data[0];
			if(day.equals("01")){
				day = "1st";
			}else if(day.equals("02")){
				day = "2nd";
			} else if(day.equals("03")){
				day = "3rd";
			} else if(day.equals("22")){
				day= day+"nd";
			}else if(day.equals("23")){
				day=day+"rd";
			}else if(day.equals("21")||day.equals("31")){
				day=day+"st";
			}else{
				day = day + "th";
			}

			date = data[1] + " " + day + " " + data[2];
		}
		return date;
	}

	/** class for send the message **/
	class sendChat extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			long unixTime = System.currentTimeMillis() / 1000L;
			try {
				String SOAP_ACTION = Constants.NAMESPACE + Constants.API_SEND_CHAT;

				SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_SEND_CHAT);
				req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
				req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
				req.addProperty("sender_id", GetSet.getUserId());
				req.addProperty("chat_id", chatId);
				if (from.equals("detail") && !aboutMessageSent){
					req.addProperty("source_id", data.get(Constants.TAG_ID));
				} else {
					req.addProperty("source_id", "0");
				}
				req.addProperty("type", "normal");
				req.addProperty("created_date", Long.toString(unixTime));
				req.addProperty("message", params[0]);

				SOAPParsing soap = new SOAPParsing();
				String json = soap.getJSONFromUrl(SOAP_ACTION, req);
			}catch (Exception e)
			{
				params[0]="";
				runOnUiThread(new Runnable() {
					public void run() {
						chats.remove(chats.size() - 1);
						chatAdapter.notifyDataSetChanged();
                        try {
							TextbookTakeoverApplication.dialog(ChatActivity.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
						} catch (Exception e){
							e.printStackTrace();
						}
					}
				});

				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void unused) {
			aboutMessageSent = true;
			send.setOnClickListener(ChatActivity.this);
		}
	}


	@Override
	public void callback(JSONArray data) throws JSONException {
		Log.v("data", "data"+data);
	}

	/** function for receiving the instant messages & typing status **/
	@Override
	public void on(String event, final JSONObject data) {
		Log.v("ONevent",""+event);
		Log.v("ONdata",""+data);
		if(event.equals("message")){
			try {
				if (data.getString("receiver").equals(userName)){
					HashMap<String,String> hmap = new HashMap<String,String>();
					hmap.put("message", data.getJSONObject("message").getString("message"));
					hmap.put("sender", data.getString("receiver"));
					hmap.put("date", data.getJSONObject("message").getString("chatTime"));
					hmap.put("type", "message");
					chats.add(hmap);
					runOnUiThread(new Runnable() {
						public void run(){
							chatAdapter.notifyDataSetChanged();
							if(chats.size()>0){
								listView.setSelection(chats.size()-1);
							}
						}
					});
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if (event.equals("messageTyping")){
			runOnUiThread(new Runnable() {
				public void run() {
					try {
						if (data.getString("receiverId").equals(userName) && data.getString("message").equals("type")){
							if (!receiverTyping) {
								receiverTyping = true;
								typing.setText(fullName + " " + getString(R.string.is_typing));
								typing.setVisibility(View.VISIBLE);
								if(chats.size()>0){
									listView.setSelection(chats.size()-1);
								}
								typing.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, R.anim.abc_slide_in_bottom));
							}
						} else {
							receiverTyping = false;
							typing.setVisibility(View.GONE);
							typing.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, R.anim.abc_slide_out_bottom));
						}
					} catch (JSONException e){
						e.printStackTrace();
					}
				}
			});
		}
	}

	@Override
	public void onChat(String event, JSONObject data, Object... obj) {
		Log.v("CHATevent",""+event);
		Log.v("CHATdata",""+data);
	}

	@Override
	public void onMessage(String message) {
		Log.v("MSG",""+message);

	}

	@Override
	public void onMessage(JSONObject json) {
		Log.v("MSGJS",""+json);

	}

	@Override
	public void onConnect() {
		Log.v("Connect","connected");

	}

		@Override
		public void onDisconnect() {
		Log.v("disConnect", "dis connected");

	}

	@Override
	public void onConnectFailure() {
		Log.v("Connect falied","connection failed");
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) {
		Log.v("before", "before");
	}

	/** function for send typing status to other end user **/
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Log.v("on typing", "on typing");
		if(runnable != null)
			handler.removeCallbacks(runnable);
		if (!meTyping){
			meTyping = true;
			JSONObject jobj = new JSONObject();
			try {
				jobj.put("senderId", userName);
				jobj.put("receiverId", GetSet.getUserName());
				jobj.put("message", "type");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			chat.setTypingStatus("messageTyping", jobj);

		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		Log.v("after", "after");
		runnable = new Runnable() {

			public void run() {
				Log.v("stop typing", "stop typing");
				meTyping = false;
				JSONObject jobj = new JSONObject();
				try {
					jobj.put("senderId", userName);
					jobj.put("receiverId", GetSet.getUserName());
					jobj.put("message", "untype");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				chat.setTypingStatus("messageTyping", jobj);
			}
		};
		handler.postDelayed(runnable, 1000);
	}

	/** function for get the current date to convert specific format **//*
	private String getDate(){
		String date = "";
		Calendar cal = Calendar.getInstance();
		Date currentLocalTime = cal.getTime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM - yyyy");
		String dateformat = simpleDateFormat.format(currentLocalTime);

		SimpleDateFormat simpleday = new SimpleDateFormat("dd");
		String day = simpleday.format(currentLocalTime);
		Log.v("day", "day="+ day);
		if(day.equals("01")){
			day = "01st";
		}else if(day.equals("02")){
			day = "02nd";
		} else if(day.equals("03")){
			day = "03rd";
		} else if(day.equals("22")){
			day= day+"nd";
		}else if(day.equals("23")){
			day=day+"rd";
		}else if(day.equals("21")||day.equals("31")){
			day=day+"st";
		}else{
			day = day + "th";
		}

		date = dateformat.replace("-", day);
		//	Log.v("dateformat", "dateformat "+dateformat +"/" + day);
		return date;
	}*/

	@Override
	protected void onPause() {
		super.onPause();
		fullName = "";
		// For Internet checking disconnect
		TextbookTakeoverApplication.unregisterReceiver(ChatActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// For Internet checking
		TextbookTakeoverApplication.registerReceiver(ChatActivity.this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		fullName = "";
		if (from.equals("message")){
			new GetChat().execute(0);
			MessageActivity.fromChat = true;
		}
		TextbookTakeoverApplication.hideSoftKeyboard(ChatActivity.this);
		chat.disconnect();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.backbtn:
				fullName = "";
				if (from.equals("message")){
					new GetChat().execute(0);
					MessageActivity.fromChat = true;
				}
				TextbookTakeoverApplication.hideSoftKeyboard(ChatActivity.this);
				chat.disconnect();
				finish();
				break;
			case R.id.send:
				if(editText.getText().toString().trim().length()>0){
					String j=editText.getText().toString();
					send.setOnClickListener(null);
					if(j.length()>0){
						if(j.contains("/>")||j.contains("</")){
							Toast.makeText(getApplicationContext(), "hai!! null", Toast.LENGTH_LONG).show();
							Log.d("hai","htmlnull");
							editText.setText("");

						}else if(j.contains(">")&&j.contains("<")){
							Log.d("hai","htmlnull");
							editText.setText("");
						}
					}
					long unixTime = System.currentTimeMillis() / 1000L;
					JSONObject jobj = new JSONObject();
					try {
						JSONObject message = new JSONObject();
						message.put("userName", GetSet.getUserName());
						message.put("userImage", GetSet.getImageUrl().replace("/150/", "/40/"));
						message.put("chatTime", Long.toString(unixTime));
						message.put("message", editText.getText().toString().trim());
						message.put("chatURL", chatUrl);

						jobj.put("receiverId", GetSet.getUserName());
						jobj.put("senderId", userName);
						jobj.put("message", message);
						Log.v("sendJSON", ""+jobj);
						chat.sendMessage("message",jobj);
						HashMap<String,String> hmap = new HashMap<String,String>();
						if (from.equals("detail") && !aboutMessageSent){
						//	aboutMessageSent = true;
							hmap.put("message", editText.getText().toString().trim());
							hmap.put("sender", GetSet.getUserName());
							hmap.put("date", Long.toString(unixTime));
							hmap.put("type", "about");
							hmap.put(Constants.TAG_ITEM_ID, data.get(Constants.TAG_ID));
							hmap.put(Constants.TAG_ITEM_TITLE, data.get(Constants.TAG_ITEM_TITLE));
							hmap.put(Constants.TAG_ITEM_IMAGE, data.get(Constants.TAG_ITEM_URL_350));
							hmap.put(Constants.TAG_OFFER_PRICE, data.get(Constants.TAG_OFFER_PRICE));
						} else {
							hmap.put("message", editText.getText().toString().trim());
							hmap.put("sender", GetSet.getUserName());
							hmap.put("date", Long.toString(unixTime));
							hmap.put("type", "message");
						}

						chats.add(hmap);
						Log.v("Check","checkdatachat"+chats);
						chatAdapter.notifyDataSetChanged();
						try{
							new sendChat().execute(editText.getText().toString().trim());
						}catch(Exception e){
							e.printStackTrace();
						}
						runOnUiThread(new Runnable() {
							public void run(){
								chatAdapter.notifyDataSetChanged();
								if(chats.size()>0){
									listView.setSelection(chats.size() - 1);
								}
							}
						});
						editText.setText("");

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					editText.setError(getString(R.string.please_enter_message));
				}

				break;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			Log.i("a", "scrolling stopped...");
			dateTxt.setVisibility(View.GONE);
		} else {
			if (dateTxt.getVisibility() != View.VISIBLE) {
				dateTxt.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
						 int visibleItemCount, int totalItemCount) {
		try {
			String chatDate = getDate(Long.parseLong(chats.get(firstVisibleItem).get("date")));
			dateTxt.setText(chatDate);
		} catch (NullPointerException e){
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}

		if (firstVisibleItem == 0 && !(loading)){
			loading = true;
			topProgress.setVisibility(View.VISIBLE);
			dateTxt.setVisibility(View.GONE);
			nullText.setVisibility(View.GONE);
			currentPage++;
			pulldown = true;
			if (TextbookTakeoverApplication.isNetworkAvailable(ChatActivity.this)) {
				new GetChat().execute(currentPage);
			}
		}
	}

}