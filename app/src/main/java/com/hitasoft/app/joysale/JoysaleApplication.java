package com.hitasoft.app.joysale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.ksoap2.serialization.SoapObject;

import com.facebook.FacebookSdk;
import com.google.android.gcm.GCMRegistrar;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.SOAPParsing;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;

@ReportsCrashes(mailTo = "crashlog@hitasoft.com")
public class JoysaleApplication extends Application {

	public static IntentFilter filter;
	public static BroadcastReceiver networkStateReceiver;
	public static Dialog dialog;
	public static SharedPreferences adminPref;
	public static SharedPreferences.Editor adminEditor;

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
		filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		// init facebook //
		FacebookSdk.sdkInitialize(this.getApplicationContext());
		FacebookSdk.setApplicationId(Constants.App_ID);

		Constants.ANDROID_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(),
				Settings.Secure.ANDROID_ID);

		adminPref = getApplicationContext().getSharedPreferences("JoysaleAdminPref",
				MODE_PRIVATE);
		adminEditor = adminPref.edit();

		if (adminPref.getBoolean("buynow", Constants.BUYNOW)) {
			Constants.BUYNOW = true;
		} else {
			Constants.BUYNOW = false;
		}

		if (adminPref.getBoolean("exchange", Constants.EXCHANGE)) {
			Constants.EXCHANGE = true;
		} else {
			Constants.EXCHANGE = false;
		}
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		ACRA.init(this);
		MultiDex.install(this);
	}

	/** function for check the network **/
	public static void registerReceiver(final Context ctx){
		if (networkStateReceiver == null) {
			Log.v("network dialog", "network dialog");
			dialog = new Dialog(ctx ,R.style.PostDialog);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			dialog.setContentView(R.layout.network_dialog);
			dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			dialog.setCanceledOnTouchOutside(true);
			dialog.setCancelable(true);
			networkStateReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo info = cm.getActiveNetworkInfo();
					if (info != null
							&& (info.getState() == NetworkInfo.State.CONNECTED || info.getState() == NetworkInfo.State.CONNECTING)) {
						Log.v("we are connected", "we are connected");
					} else {
						Log.v("Disconnected", "Disconnected");
						try {
							networkError(dialog, context);
						} catch (Exception e){
							e.printStackTrace();
						}
					}
				}
			};

			ctx.registerReceiver(networkStateReceiver, filter);
		}

	}

	/** function unregister the network intent checking **/
	public static void unregisterReceiver(Context ctx){
		if (networkStateReceiver != null) {
			if (dialog != null){
				dialog.cancel();
			}
			dialog = null;
			ctx.unregisterReceiver(networkStateReceiver);
			networkStateReceiver = null;
		}
	}

	private static void networkError(final Dialog dia, final Context ctx){
		try {

			TextView ok = (TextView) dia.findViewById(R.id.alert_button);
			TextView cancel = (TextView) dia.findViewById(R.id.alert_cancel);

			ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (dialog != null){
						dialog.cancel();
					}
					dialog = null;
					Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
					ctx.startActivity(intent);
				}
			});

			cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (dialog != null){
						dialog.cancel();
					}
					dialog = null;
				}
			});
			Log.v("show", "show=" + dia.isShowing());
			dia.show();
		} catch (NullPointerException e){
			e.printStackTrace();
		} catch (WindowManager.BadTokenException e){
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/** To register the device for push notification **/
	void register(Context context) {

		// Once GCM returns a registration id, we need to register on our server
		// As the server might be down, we will retry it a couple
		// times.
		for (int i = 1; i <=  5 ; i++) {

			Log.v("Push-Notification", "Attempt #" + i + " to register");

			Thread th = new Thread(new Runnable() {
				public void run() {
					String SOAP_ACTION = Constants.NAMESPACE + Constants.API_PUSH_REGISTER;

					SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_PUSH_REGISTER);
					req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
					req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
					req.addProperty("deviceId", Constants.ANDROID_ID);
					req.addProperty("userid", GetSet.getUserId());
					req.addProperty("devicetype", "1");
					req.addProperty("devicemode", "1");
					req.addProperty("deviceToken", Constants.REGISTER_ID);

					SOAPParsing soap = new SOAPParsing();
					String json = soap.getJSONFromUrl(SOAP_ACTION, req);

					Log.v("json","json"+json);
				}

			});

			th.start();

			GCMRegistrar.setRegisteredOnServer(context, true);

			return;

		}

	}

	// Unregister this account/device pair within the server.
	void unregister(final Context context) {
		Log.v("Register_Id","Register_Id="+Constants.REGISTER_ID);
		Log.v("unRegister", "unRegister");


		Thread th = new Thread(new Runnable() {
			public void run() {
				String SOAP_ACTION = Constants.NAMESPACE + Constants.API_PUSH_UNREGISTER;

				SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_PUSH_UNREGISTER);
				req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
				req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
				req.addProperty("deviceId", Constants.ANDROID_ID);

				SOAPParsing soap = new SOAPParsing();
				String json = soap.getJSONFromUrl(SOAP_ACTION, req);

				Log.v("json", "json" + json);
				Constants.REGISTER_ID = "";
			}
		});
		th.start();

		//GCMRegistrar.unregister(context);
		GCMRegistrar.setRegisteredOnServer(context, false);
		// At this point the device is unregistered from GCM, but still
		// registered in the our server.
		// We could try to unregister again, but it is not necessary:
		// if the server tries to send a message to the device, it will get
		// a "NotRegistered" error message and should unregister the device.


	}
	
	public static String loadJSONFromAsset(Context context,String name) {
	    String json = null;
	    try {

	        InputStream is = context.getResources().getAssets().open(name);

	        int size = is.available();

	        byte[] buffer = new byte[size];

	        is.read(buffer);

	        is.close();

	        json = new String(buffer, "UTF-8");
	    } catch (IOException ex) {
	        ex.printStackTrace();
	        return null;
	    }
	    return json;

	}
	
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();

			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					Log.i("Class", info[i].getState().toString());
					if (info[i].getState() == NetworkInfo.State.CONNECTED || info[i].getState() == NetworkInfo.State.CONNECTING) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	/** for closing the keyboard while touch outside **/
	public static void setupUI(Context context,View view) {
		final Activity act = (Activity) context;
		// Set up touch listener for non-text box views to hide keyboard.
		if (!(view instanceof EditText)) {

			view.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					hideSoftKeyboard(act);
					return false;
				}

			});
		}

		// If a layout container, iterate over children and seed recursion.
		if (view instanceof ViewGroup) {

			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

				View innerView = ((ViewGroup) view).getChildAt(i);

				setupUI(act,innerView);
			}
		}
	}

	public static void hideSoftKeyboard(Activity context) {
		try {
			InputMethodManager inputMethodManager = (InputMethodManager) context
					.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(context
					.getCurrentFocus().getWindowToken(), 0);
		} catch (NullPointerException npe) {

		} catch (Exception e) {

		}
	}
	
	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


	/** for get the time gap **/
	public static String getTimeAgo(long time, Context ctx) {
	    if (time < 1000000000000L) {
	        // if timestamp given in seconds, convert to millis
	        time *= 1000;
	    }

	    long now = getCurrentTime(ctx);
	    if (time > now || time <= 0) {
	        return null;
	    }
	    final long diff = now - time;
	    if (diff < MINUTE_MILLIS) {
	        return ctx.getResources().getString(R.string.justnow);
	    } else if (diff < 2 * MINUTE_MILLIS) {
	        return ctx.getResources().getString(R.string.aminuteago);
	    } else if (diff < 50 * MINUTE_MILLIS) {
	        return diff / MINUTE_MILLIS + " "+ctx.getResources().getString(R.string.minutesago);
	    } else if (diff < 90 * MINUTE_MILLIS) {
	        return ctx.getResources().getString(R.string.anhourago);
	    } else if (diff < 24 * HOUR_MILLIS) {
	        return diff / HOUR_MILLIS + " "+ctx.getResources().getString(R.string.hoursago);
	    } else if (diff < 48 * HOUR_MILLIS) {
	        return ctx.getResources().getString(R.string.yesterday);
	    } else {
	        return diff / DAY_MILLIS + " "+ctx.getResources().getString(R.string.daysago);
	    }
	}
	private static long getCurrentTime(Context ctx) {
		// TODO Auto-generated method stub
		long dtMili = System.currentTimeMillis();
		return dtMili;
	}
	
	public static void dialog(Context ctx, String title,String content){
		final Dialog dialog = new Dialog(ctx ,R.style.AlertDialog);
		Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setContentView(R.layout.default_dialog);
		dialog.getWindow().setLayout(display.getWidth()*90/100, LayoutParams.WRAP_CONTENT);
		dialog.setCancelable(true);
		
		TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
		TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
		ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
		TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);
		
		alertTitle.setText(title);
		alertMsg.setText(content);
		/*if(title.equals(ctx.getResources().getString(R.string.alert)) || title.equals(ctx.getResources().getString(R.string.error))){
			alertIcon.setImageResource(R.drawable.alert_icon);
		}else if(title.equals(ctx.getResources().getString(R.string.success))){
			alertIcon.setImageResource(R.drawable.success_icon);
		}else if(title.equals(ctx.getResources().getString(R.string.network_error))){
			alertIcon.setImageResource(R.drawable.network_icon);
		}else{
			alertIcon.setVisibility(View.GONE);
		}*/
		
		
		alertOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				
			}
		});
		
		if(!dialog.isShowing()){
			dialog.show();
		}
		
	}

	public static void disabledialog(final Context ctx, final String content){
		((Activity) ctx).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				final Dialog dialog = new Dialog(ctx ,R.style.AlertDialog);
				Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
				dialog.setContentView(R.layout.default_dialog);
				dialog.getWindow().setLayout(display.getWidth()*90/100, LayoutParams.WRAP_CONTENT);
				dialog.setCanceledOnTouchOutside(false);
				dialog.setCancelable(false);

				TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
				TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
				ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
				TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

				//alertTitle.setText(ctx.getResources().getString(R.string.alert));
				alertMsg.setText(content);
				//alertIcon.setImageResource(R.drawable.alert_icon);

				alertOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try{
							Constants.editor.clear();
							Constants.editor.commit();
							GetSet.reset();
							FragmentMainActivity.HomeItems.clear();
							FragmentMainActivity.filterAry.clear();
							FragmentMainActivity.currentPage = 0;
							WelcomeActivity.fromSignout = true;

						} catch(NullPointerException e){
							e.printStackTrace();
						} catch(Exception e){
							e.printStackTrace();
						}
						dialog.dismiss();
						((Activity) ctx).finish();
						Intent intent = new Intent (ctx, WelcomeActivity.class);
						((Activity) ctx).startActivity(intent);
					}
				});

				if(!dialog.isShowing()){
					dialog.show();
				}

			}
		});

	}

	/** to display the text without html content **/
	public static String stripHtml(String html) {
		return Html.fromHtml(html).toString();
	}
	
	public static String format(double number) {
		if(number < 1000){
			int c = (int) number;
			return Integer.toString(c);
		}else{
		String[] suffix = new String[]{"","k", "m", "b", "t"};
		int MAX_LENGTH = 4;
	    String r = new DecimalFormat("##0E0").format(number);
	    r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
	    while(r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")){
	        r = r.substring(0, r.length()-2) + r.substring(r.length() - 1);
	    }
	    return r;
		}
	}

	public static class MyTextWatcher implements TextWatcher {

		private EditText view;
		MyTextWatcher(EditText view) {
			this.view = view;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
									  int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
								  int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			String result = s.toString().replaceAll("  ", " ");
			if (!s.toString().equals(result)) {
				view.setText(result);
				view.setSelection(result.length());
				// alert the user
			}
		}

	}

	public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
		int width = image.getWidth();
		int height = image.getHeight();

		float bitmapRatio = (float)width / (float) height;
		if (bitmapRatio > 0) {
			width = maxSize;
			height = (int) (width / bitmapRatio);
		} else {
			height = maxSize;
			width = (int) (height * bitmapRatio);
		}
		return Bitmap.createScaledBitmap(image, width, height, true);
	}

	public static Bitmap rotateImage(Bitmap src, float degree) {
		// create new matrix object
		Matrix matrix = new Matrix();
		// setup rotation degree
		matrix.postRotate(degree);

		// return new bitmap rotated using matrix
		return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
	}
	/**
	 * To convert the given dp value to pixel
	 **/
	public static int dpToPx(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	public static float pxToDp(Context context, float px) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return dp;
	}

	/** function for avoiding emoji typing in keyboard **/
	public static InputFilter EMOJI_FILTER = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			for (int index = start; index < end; index++) {

				int type = Character.getType(source.charAt(index));

				if (type == Character.SURROGATE) {
					return "";
				}
			}
			return null;
		}
	};
}
