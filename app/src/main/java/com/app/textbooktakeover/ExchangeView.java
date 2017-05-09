package com.app.textbooktakeover;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class ExchangeView extends Activity implements OnClickListener,ChatCallbackAdapter,TextWatcher, OnScrollListener{

	TextView title,itemName,myitemName,time,failed,success,username,nullText,typing,send;
	ListView listView;
	ImageView userImage, myitemImage,exchangeImage, backBtn;
	String userName="", clickedBtn,chatId, type;
	boolean pulldown=false,loading = false, meTyping, receiverTyping;
	AVLoadingIndicatorView progress, topProgress;
	int black, currentPage=0, position;
	EditText editText;
	public static Chat chat;
	ChatAdapter chatAdapter;
	ViewGroup header, footer;
	ArrayList<HashMap<String, String>> chats= new ArrayList<HashMap<String, String>>();
	HashMap<String,String> datas = new HashMap<String,String>();
	ArrayList<HashMap<String,String>> tempAry=new ArrayList<HashMap<String,String>>();
	InputMethodManager imm;
	Handler handler = new Handler();
	Runnable runnable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exchange_view);
		
		backBtn = (ImageView) findViewById(R.id.backbtn);
		title = (TextView) findViewById(R.id.title);
		listView = (ListView) findViewById(R.id.listView);
		send = (TextView) findViewById(R.id.sendtxt);
		editText = (EditText) findViewById(R.id.editText);
		userImage = (ImageView) findViewById(R.id.userImage);
		exchangeImage = (ImageView) findViewById(R.id.exitemImage);
		myitemImage = (ImageView) findViewById(R.id.myitemImage);
		itemName = (TextView) findViewById(R.id.exitemName);
		myitemName = (TextView) findViewById(R.id.myitemName);
		time = (TextView) findViewById(R.id.time);
		failed = (TextView) findViewById(R.id.failed);
		success = (TextView) findViewById(R.id.success);
		username = (TextView) findViewById(R.id.userName);
		progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		LayoutInflater inflater = getLayoutInflater();
		header = (ViewGroup) inflater.inflate(R.layout.chat_header, null, false); 
		listView.addHeaderView(header, null, false);

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
		
		black = getResources().getColor(R.color.black);
        title.setText(getString(R.string.myexchange));
        datas = (HashMap<String, String>) getIntent().getExtras().get("data");
        position = (int) getIntent().getExtras().get("position");
        type = (String) getIntent().getExtras().get("type");
        
        chat = new Chat(this);
		chat.start();

		Picasso.with(ExchangeView.this).load(datas.get(Constants.TAG_EXCHANGERIMG)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImage);
		Picasso.with(ExchangeView.this).load(datas.get("e"+ Constants.TAG_ITEMIMAGE)).into(exchangeImage);
		Picasso.with(ExchangeView.this).load(datas.get("m"+ Constants.TAG_ITEMIMAGE)).into(myitemImage);
		itemName.setText(datas.get("e"+ Constants.TAG_ITEM_NAME));
		myitemName.setText(datas.get("m"+ Constants.TAG_ITEM_NAME));
        username.setText(datas.get(Constants.TAG_EXCHANGERNAME));
        time.setText(datas.get(Constants.TAG_EXCHANGETIME));
        
        userName = datas.get(Constants.TAG_EXCHANGERUSERNAME);

		Log.v("userName", "userName="+userName);
        String status = datas.get(Constants.TAG_STATUS);
        
		if (datas.get(Constants.TAG_REQUEST_BY_ME).equals("true")) {
            if (status.equals("Pending")) {
                success.setText("Cancel");
                failed.setVisibility(View.GONE);
            } else if (status.equals("Accepted")) {
                failed.setText("Failed");
                success.setText("Success");
            }

		} else {
			if (status.equals("Pending")) {
				failed.setText("Decline");
				success.setText("Accept");
			} else if (status.equals("Accepted")) {
				failed.setText("Failed");
				success.setText("Success");
			}
		}
		
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
				chat.join("exchangejoin",jobj);
		    }
		}, 2000);
		
        backBtn.setOnClickListener(this);
		send.setOnClickListener(this);
		editText.addTextChangedListener(this);
		failed.setOnClickListener(this);
		success.setOnClickListener(this);
		editText.setFilters(new InputFilter[]{TextbookTakeoverApplication.EMOJI_FILTER});

		chatAdapter = new ChatAdapter(ExchangeView.this, chats);
		listView.setAdapter(chatAdapter);

		try{
			new GetChat().execute(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/** for get the chatid between two users **/
    class getChatId extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_CHAT_ID;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_CHAT_ID);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("sender_id", GetSet.getUserId());
            req.addProperty("receiver_id", datas.get(Constants.TAG_EXCHANGERID));

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            try{
                JSONObject jobj = new JSONObject(json);
                String status = jobj.getString(Constants.TAG_STATUS);

                if(status.equals("true")){
                    chatId = jobj.getString("chat_id");
                }else{

                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

	/** for get the last conversation **/
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
				req.addProperty("receiver_id", datas.get(Constants.TAG_EXCHANGERID));
				req.addProperty("type", "exchange");
				req.addProperty("offset", Integer.toString(offset));
				req.addProperty("limit", "20");
				req.addProperty("source_id", datas.get(Constants.TAG_EXCHANGEID));

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
				pulldown=false;
				topProgress.setVisibility(View.GONE);
			} else {
				listView.setVisibility(View.INVISIBLE);
				progress.setVisibility(View.VISIBLE);
			}

		}

		@Override
		protected void onPostExecute(Void unused) {
			try {
				if (chats.size()==0){
					listView.setOnScrollListener(null);
				}else{
					listView.setOnScrollListener(ExchangeView.this);
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
				if (chats.size() > 18){
					if (tempAry.size()==0){
						nullText.setVisibility(View.VISIBLE);
						topProgress.setVisibility(View.GONE);
					}
				} else{

                }
				loading= false;
				topProgress.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
	    		progress.setVisibility(View.GONE);
	    		chatAdapter.notifyDataSetChanged();

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
				chatId = DefensiveClass.optString(json, Constants.TAG_CHAT_ID);
				JSONObject chobj = json.optJSONObject(Constants.TAG_CHATS);
				if(chobj != null){
				JSONArray chat = chobj.optJSONArray(Constants.TAG_CHATS);
				if(chat != null){
					for (int i=0 ; i<chat.length() ; i++){
						HashMap<String, String> map = new HashMap<String, String>();
						JSONObject temp = chat.getJSONObject(i);
						String receiver = DefensiveClass.optString(temp, Constants.TAG_RECEIVER);
						String sender = DefensiveClass.optString(temp, Constants.TAG_SENDER);
						JSONObject msg = temp.getJSONObject(Constants.TAG_MESSAGE);
						String userName = DefensiveClass.optString(msg, Constants.TAG_userName);
						String userImage = DefensiveClass.optString(msg, Constants.TAG_userImage);
						String chatTime = DefensiveClass.optString(msg, Constants.TAG_CHATTIME);
						String message = DefensiveClass.optString(msg, Constants.TAG_MESSAGE);

						map.put("message", message);
						map.put("sender", sender);
						map.put("date", chatTime);
						chats.add(map);
 					}
				}
				}
			} else if (response.equalsIgnoreCase("error")){
				TextbookTakeoverApplication.disabledialog(ExchangeView.this, json.optString("message"));
			} else {
                new getChatId().execute();
            }

		} catch (JSONException e) {
			new getChatId().execute();
			e.printStackTrace();
		} catch (NullPointerException e) {
			new getChatId().execute();
			e.printStackTrace();
		} catch (Exception e) {
			new getChatId().execute();
			e.printStackTrace();
		}
		return chats;
	}


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
			TextView message,date;
			ImageView leftimage;
			RelativeLayout dateLay;
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
				//	holder.message.setPadding(20, 10, 20, 10);

				holder.leftimage.setVisibility(View.GONE);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			try{

				final HashMap<String, String> tempMap=Items.get(position);

				if(tempMap.get("message") != null){
					holder.message.setText(tempMap.get("message"));
				}

				if(tempMap.get("sender").equals(GetSet.getUserName())){
					holder.main.setGravity(Gravity.RIGHT);
					holder.message.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_bg_right));
					holder.leftimage.setVisibility(View.GONE);
				} else{
					holder.main.setGravity(Gravity.LEFT);
					holder.message.setBackgroundDrawable(getResources().getDrawable(R.drawable.chat_bg_left));
					/*if (position > 0){
						if (!Items.get(position-1).get("sender").equals(GetSet.getUserName())){
							holder.leftimage.setVisibility(View.INVISIBLE);
						}else{
							holder.leftimage.setVisibility(View.VISIBLE);
							Picasso.with(ExchangeView.this).load(data.get(Constants.TAG_EXCHANGERIMG).replace("/150/", "/50/")).into(holder.leftimage);
						}
					} else{
						holder.leftimage.setVisibility(View.VISIBLE);
						Picasso.with(ExchangeView.this).load(data.get(Constants.TAG_EXCHANGERIMG).replace("/150/", "/50/")).into(holder.leftimage);
					}*/

				}
				//holder.date.setVisibility(View.VISIBLE);
				//holder.date.setText(getChatDate(tempMap.get("date")));
				String chatDate = getDate(Long.parseLong(tempMap.get("date")));
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

	/** for change the given date to specific format **/
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

	/** for send the message to user **/
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
				req.addProperty("source_id", datas.get(Constants.TAG_EXCHANGEID));
				req.addProperty("type", "exchange");
				req.addProperty("created_date", Long.toString(unixTime));
				req.addProperty("message", params[0]);

				SOAPParsing soap = new SOAPParsing();
				String json = soap.getJSONFromUrl(SOAP_ACTION, req);
			}catch (Exception e){
				runOnUiThread(new Runnable() {
					public void run() {

						chats.remove(chats.size()-1);
						chatAdapter.notifyDataSetChanged();

						TextbookTakeoverApplication.dialog(ExchangeView.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
					}
				});


			}

			return null;
		}
	}

	/** for change the status of exchanges **/
	class changestatus extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			String SOAP_ACTION = Constants.NAMESPACE + Constants.API_CHANGE_EXCHANGE;

			SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_CHANGE_EXCHANGE);
			req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
			req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
			req.addProperty("user_id", GetSet.getUserId());
			req.addProperty("exchange_id", datas.get(Constants.TAG_EXCHANGEID));
			req.addProperty("status", params[0]);

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
				if(status.equals("true")){
					dialog(ExchangeView.this, getResources().getString(R.string.success), getResources().getString(R.string.exchange_status_chngd));
					failed.setEnabled(true);
					success.setEnabled(true);
					Log.d("hai","successstatus"+success.isEnabled());

				}else{
					TextbookTakeoverApplication.dialog(ExchangeView.this, getResources().getString(R.string.alert), jobj.getString("message"));
					failed.setEnabled(true);
					success.setEnabled(true);
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
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setContentView(R.layout.default_dialog);
		dialog.getWindow().setLayout(display.getWidth()*80/100, LayoutParams.WRAP_CONTENT);
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
				dialog.dismiss();
			//	doActionOnClick();
			}
		});

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				doActionOnClick();
			}
		});

		if(!dialog.isShowing()){
			dialog.show();
		}

	}

	private void doActionOnClick(){
		if(failed.getText().toString().equals("Decline") && success.getText().toString().equals("Accept")){
			if(clickedBtn.equals("success")){
				failed.setText("Failed");
				success.setText("Success");
				if (type.equals("incoming")){
					Log.v("hai","checkstatus"+success.isEnabled());
					IncomeExchange.incomingAry.get(position).put(Constants.TAG_STATUS, "Accepted");
					IncomeExchange.exchangeAdapter.notifyDataSetChanged();
				} else if (type.equals("outgoing")){
					OutgoingExchange.outgoingAry.get(position).put(Constants.TAG_STATUS, "Accepted");
					OutgoingExchange.exchangeAdapter.notifyDataSetChanged();
				}
			} else {
				ExchangeActivity.type="failed";
				ExchangeActivity.statusChanged = true;
				finish();
			}

		} else if (failed.getText().toString().equals("Failed") && success.getText().toString().equals("Success")){
			if(clickedBtn.equals("success")){
				ExchangeActivity.type="success";
				ExchangeActivity.statusChanged = true;
				finish();
			}else{
				ExchangeActivity.type="failed";
				ExchangeActivity.statusChanged = true;
				finish();
			}

		} else if(success.getText().toString().equals("Cancel")){
			ExchangeActivity.type="failed";
			ExchangeActivity.statusChanged = true;
			finish();
		}
	}

	/*private String getDate(){
		String date = "";
		Calendar cal = Calendar.getInstance();
		Date currentLocalTime = cal.getTime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM - yyyy");
		String dateformat = simpleDateFormat.format(currentLocalTime);

		SimpleDateFormat simpleday = new SimpleDateFormat("dd");
		String day = simpleday.format(currentLocalTime);
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

		date = dateformat.replace("-", day);
	//	Log.v("dateformat", "dateformat "+dateformat +"/" + day);
		return date;
	}*/

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		chat.disconnect();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.backbtn:
			chat.disconnect();
			finish();
			break;
		case R.id.sendtxt:
			if(editText.getText().toString().trim().length()>0){
				JSONObject jobj = new JSONObject();
				try {
					long unixTime = System.currentTimeMillis() / 1000L;
					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
					JSONObject message = new JSONObject();
					message.put("userName", GetSet.getUserName());
					message.put("userImage", GetSet.getImageUrl().replace("/150/", "/40/"));
					message.put("chatTime", Long.toString(unixTime));
					message.put("message", editText.getText().toString().trim());

					jobj.put("receiverId", GetSet.getUserName());
					jobj.put("senderId", datas.get(Constants.TAG_EXCHANGERUSERNAME));
					jobj.put("sourceId", datas.get(Constants.TAG_EXCHANGEID));
					jobj.put("message", message);
					Log.v("sendJSON", ""+jobj);
					chat.sendMessage("exmessage",jobj);
					HashMap<String,String> hmap = new HashMap<String,String>();
					hmap.put("message", editText.getText().toString().trim());
					hmap.put("sender", GetSet.getUserName());
					hmap.put("date", Long.toString(unixTime));
					chats.add(hmap);
					chatAdapter.notifyDataSetChanged();
					try{
						new sendChat().execute(editText.getText().toString().trim());
					}catch(Exception e){
						e.printStackTrace();
					}
					editText.setText("");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else{
				editText.setError(getResources().getString(R.string.please_enter_message));
			}

			break;
		case R.id.failed:
			failed.setEnabled(false);
			clickedBtn = "failed";

			String status = failed.getText().toString();
			if(status.equals("Failed")){
				new changestatus().execute("failed");
			}else if(status.equals("Decline")){
				new changestatus().execute("decline");
			}
			Log.v("clicked", "clicked");
			break;
		case R.id.success:
			success.setEnabled(false);

			clickedBtn = "success";

			String stat = success.getText().toString();
			Log.v("clicked", "clickedsucces"+stat);
			if(stat.equals("Success")){
				new changestatus().execute("success");
			} else if(stat.equals("Accept")){
				new changestatus().execute("accept");
			} else if(stat.equals("Cancel")){
				new changestatus().execute("cancel");
			}
			else{
				Log.v("hai","check"+stat);
			}

			break;
		}
	}

	@Override
	public void callback(JSONArray data) throws JSONException {
		Log.v("data", "data"+data);
	}

	@Override
	public void on(String event, final JSONObject data) {
		Log.v("ONevent",""+event);
		Log.v("ONdata",""+data);
		if(event.equals("exmessage")){
			try {
				if (data.getString("sourceId").equals(datas.get(Constants.TAG_EXCHANGEID))){
					HashMap<String,String> hmap = new HashMap<String,String>();
					hmap.put("message", data.getJSONObject("message").getString("message"));
					hmap.put("sender", data.getString("receiver"));
					hmap.put("date", data.getJSONObject("message").getString("chatTime"));
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

		if (event.equals("exmessageTyping")){
			runOnUiThread(new Runnable() {
				public void run() {
					try {
						if (data.getString("sourceId").equals(datas.get(Constants.TAG_EXCHANGEID)) && data.getString("message").equals("type")){
							if (!receiverTyping) {
								receiverTyping = true;
								typing.setText(datas.get(Constants.TAG_EXCHANGERNAME) + " " + getString(R.string.is_typing));
								typing.setVisibility(View.VISIBLE);
								if(chats.size()>0){
									listView.setSelection(chats.size()-1);
								}
								typing.startAnimation(AnimationUtils.loadAnimation(ExchangeView.this, R.anim.abc_slide_in_bottom));
							}
						} else {
							receiverTyping = false;
							typing.setVisibility(View.GONE);
							typing.startAnimation(AnimationUtils.loadAnimation(ExchangeView.this, R.anim.abc_slide_out_bottom));
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
		Log.v("disConnect","dis connected");

	}

	@Override
	public void onConnectFailure() {
		Log.v("Connect falied","connection failed");
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Log.v("on typing", "on typing");
		if(runnable != null)
			handler.removeCallbacks(runnable);
		if (!meTyping) {
			meTyping = true;
			JSONObject jobj = new JSONObject();
			try {
				jobj.put("senderId", userName);
				jobj.put("receiverId", GetSet.getUserName());
				jobj.put("sourceId", datas.get(Constants.TAG_EXCHANGEID));
				jobj.put("message", "type");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			chat.setTypingStatus("exmessageTyping", jobj);
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
					jobj.put("sourceId", datas.get(Constants.TAG_EXCHANGEID));
					jobj.put("message", "untype");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				chat.setTypingStatus("exmessageTyping", jobj);
			}
		};
		handler.postDelayed(runnable, 1000);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem == 0 && !(loading)){
			loading = true;
			topProgress.setVisibility(View.VISIBLE);
			nullText.setVisibility(View.GONE);
			currentPage++;
			pulldown = true;
			if (TextbookTakeoverApplication.isNetworkAvailable(ExchangeView.this)) {
				new GetChat().execute(currentPage);
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		// For Internet checking disconnect
		TextbookTakeoverApplication.unregisterReceiver(ExchangeView.this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// For Internet checking
		TextbookTakeoverApplication.registerReceiver(ExchangeView.this);
	}
	
}
