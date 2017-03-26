package com.hitasoft.app.joysale;
/****************
*
* @author 'Hitasoft Technologies'
* 
* Description:  
* This class is used for displaying splash screen
*
* Revision History:
* Version 1.0 - Initial Version
*
*****************/

import com.hitasoft.app.joysale.R;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.OrderParsing;
import com.hitasoft.app.utils.SOAPParsing;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class SplashActivity extends Activity {

	private static int SPLASH_TIME_OUT = 2000;
	private static Dialog settingsDialog;
	public static SharedPreferences pref;
	public static Editor editor;
	String[] languages, langCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		pref = getApplicationContext().getSharedPreferences("JoysalePref",
				MODE_PRIVATE);
		editor = pref.edit();

		languages = getResources().getStringArray(R.array.languages);
		langCode = getResources().getStringArray(R.array.languageCode);
		String selectedLang = pref.getString("language", Constants.LANGUAGE);

		int index = Arrays.asList(languages).indexOf(selectedLang);
		String languageCode = Arrays.asList(langCode).get(index);
		Log.v("languageCode", "languageCode="+languageCode);

		setLocale(languageCode);

		JoysaleApplication.adminPref = getApplicationContext().getSharedPreferences("JoysaleAdminPref",
				MODE_PRIVATE);
		JoysaleApplication.adminEditor = JoysaleApplication.adminPref.edit();

		new GetAdminDatas().execute();
	}

	@Override
	public void onBackPressed() {

	}

	public void setLocale(String lang) {
		Locale myLocale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
	}

	/**
	 * class for get the admin default datas
	 **/
	class GetAdminDatas extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String SOAP_ACTION = Constants.NAMESPACE + Constants.API_ADMIN_DATAS;

			SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_ADMIN_DATAS);
			req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
			req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);

			SOAPParsing soap = new SOAPParsing();
			final String json = soap.getJSONFromUrl(SOAP_ACTION, req);

			return json;
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onPostExecute(String response) {

			try {
				JSONObject json = new JSONObject(response);
				String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
				if (status.equalsIgnoreCase("true")){
					JSONObject result = json.optJSONObject(Constants.TAG_RESULT);
					String buynow = DefensiveClass.optString(result, "buynow");
					String exchange = DefensiveClass.optString(result, "exchange");
					FragmentMainActivity.homeBanner = DefensiveClass.optString(result, "banner");

					if (buynow.equalsIgnoreCase("enable")){
						JoysaleApplication.adminEditor.putBoolean("buynow", false);
						JoysaleApplication.adminEditor.commit();
						Constants.BUYNOW = false;
					} else {
						JoysaleApplication.adminEditor.putBoolean("buynow", false);
						JoysaleApplication.adminEditor.commit();
						Constants.BUYNOW = false;
					}

					if (exchange.equalsIgnoreCase("enable")){
						JoysaleApplication.adminEditor.putBoolean("exchange", true);
						JoysaleApplication.adminEditor.commit();
						Constants.EXCHANGE = true;
					} else {
						JoysaleApplication.adminEditor.putBoolean("exchange", false);
						JoysaleApplication.adminEditor.commit();
						Constants.EXCHANGE = false;
					}

					JSONArray bannerAry = result.getJSONArray("bannerData");
					for (int i = 0; i < bannerAry.length(); i++){
						JSONObject temp = bannerAry.getJSONObject(i);
						HashMap<String, String> map = new HashMap<String, String>();

						String image = DefensiveClass.optString(temp, "bannerImage");
						String url = DefensiveClass.optString(temp, "bannerURL");

						map.put("image", image);
						map.put("url", url);

						FragmentMainActivity.bannerAry.add(map);
					}

				}
			} catch (NullPointerException e){
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {

					Intent i = new Intent(SplashActivity.this,
							FragmentMainActivity.class);
					startActivity(i);
					finish();

					overridePendingTransition(R.anim.fade_in,
							R.anim.fade_out);
				}
			}, SPLASH_TIME_OUT);
		}

	}
	
}
