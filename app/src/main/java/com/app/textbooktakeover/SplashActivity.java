package com.app.textbooktakeover;
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

import com.app.utils.SOAPParsing;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

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

		TextbookTakeoverApplication.adminPref = getApplicationContext().getSharedPreferences("JoysaleAdminPref",
				MODE_PRIVATE);
		TextbookTakeoverApplication.adminEditor = TextbookTakeoverApplication.adminPref.edit();

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
					String promotion = DefensiveClass.optString(result, "promotion");
					FragmentMainActivity.homeBanner = DefensiveClass.optString(result, "banner");

					if (buynow.equalsIgnoreCase("enable")){
						TextbookTakeoverApplication.adminEditor.putBoolean("buynow", true);
						TextbookTakeoverApplication.adminEditor.commit();
						Constants.BUYNOW = true;
					} else {
						TextbookTakeoverApplication.adminEditor.putBoolean("buynow", false);
						TextbookTakeoverApplication.adminEditor.commit();
						Constants.BUYNOW = false;
					}

					if (exchange.equalsIgnoreCase("enable")){
						TextbookTakeoverApplication.adminEditor.putBoolean("exchange", true);
						TextbookTakeoverApplication.adminEditor.commit();
						Constants.EXCHANGE = true;
					} else {
						TextbookTakeoverApplication.adminEditor.putBoolean("exchange", false);
						TextbookTakeoverApplication.adminEditor.commit();
						Constants.EXCHANGE = false;
					}

					if (promotion.equalsIgnoreCase("enable")){
						TextbookTakeoverApplication.adminEditor.putBoolean("promotion", true);
						TextbookTakeoverApplication.adminEditor.commit();
						Constants.PROMOTION = true;
					} else {
						TextbookTakeoverApplication.adminEditor.putBoolean("promotion", false);
						TextbookTakeoverApplication.adminEditor.commit();
						Constants.PROMOTION = false;
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
