package com.app.textbooktakeover;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.utils.Constants;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.google.android.gcm.GCMRegistrar;
import com.app.textbooktakeover.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

public class LoginActivity extends AppCompatActivity implements OnClickListener{

	EditText email, password;
	TextView login, register, forgetPassword;
	ImageView backbtn;
	String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
	private BroadcastReceiver networkStateReceiver;
	Display display;
	RelativeLayout main;
	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		Constants.pref = getApplicationContext().getSharedPreferences("TBTakeoverPref",
				MODE_PRIVATE);
		Constants.editor = Constants.pref.edit();

		email = (EditText) findViewById(R.id.email);
		password = (EditText) findViewById(R.id.password);
		login = (TextView) findViewById(R.id.login);
		register = (TextView) findViewById(R.id.register);
		backbtn = (ImageView) findViewById(R.id.backbtn);
		forgetPassword = (TextView) findViewById(R.id.forgetpassword);
		main=(RelativeLayout) findViewById(R.id.main);

		TextbookTakeoverApplication.setupUI(LoginActivity.this, main);

		login.setOnClickListener(this);
		register.setOnClickListener(this);
		backbtn.setOnClickListener(this);
		forgetPassword.setOnClickListener(this);

		display = this.getWindowManager().getDefaultDisplay();

		dialog = new ProgressDialog(LoginActivity.this);
		dialog.setMessage(getString(R.string.pleasewait));
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

	}

	// for login //
	class loginAsync extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... arg0) {

			String SOAP_ACTION = Constants.NAMESPACE + Constants.API_LOGIN;

			SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_LOGIN);
			req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
			req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
			req.addProperty("email", email.getText().toString());
			req.addProperty("password", password.getText().toString());

			SOAPParsing soap = new SOAPParsing();
			String result = soap.getJSONFromUrl(SOAP_ACTION, req);
			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.show();

		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				dialog.dismiss();
				Log.v("result",""+result);
				JSONObject jonj = new JSONObject(result);
				if (jonj.getString("status").equalsIgnoreCase(
						"true")) {
					GetSet.setLogged(true);
					GetSet.setEmail(email.getText().toString());
					GetSet.setPassword(password.getText().toString());
					GetSet.setUserId(jonj.getString("user_id"));
					GetSet.setUserName(jonj.getString("user_name"));
					GetSet.setFullName(jonj.getString("full_name"));
					GetSet.setImageUrl(jonj.getString("photo"));

					Constants.editor.putBoolean("isLogged", true);
					Constants.editor.putString("userId", GetSet.getUserId());
					Constants.editor.putString("userName", GetSet.getUserName());
					Constants.editor.putString("Email", GetSet.getEmail());
					Constants.editor.putString("Password", GetSet.getPassword());
					Constants.editor.putString("photo", GetSet.getImageUrl());
					Constants.editor.putString("fullName", GetSet.getFullName());
					Constants.editor.putString("language", Constants.LANGUAGE);
					Constants.editor.commit();

					Registernotifi();
					finish();
					Intent i = new Intent(LoginActivity.this, FragmentMainActivity.class);
					startActivity(i);

				} else if (jonj.getString("status").equalsIgnoreCase("error")){
					TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.alert), jonj.getString("message"));
				} else {
					dialog.dismiss();
					TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.alert), jonj.getString("message"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
			} catch (NullPointerException e) {
				e.printStackTrace();
				TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
			} catch (Exception e){
				e.printStackTrace();
				TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
			}
		}
	}

	/**  For register push notification **/
	public void  Registernotifi(){
		Constants.REGISTER_ID = GCMRegistrar.getRegistrationId(getApplicationContext());
		Log.v("enetered push","registerid="+ Constants.REGISTER_ID);
		Constants.editor.putString("registerId", Constants.REGISTER_ID);
		Constants.editor.commit();

		if(Constants.REGISTER_ID=="" || Constants.REGISTER_ID.equals("")){
			GCMRegistrar.register(this, Constants.SENDER_ID);
		}else{
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				Log.v("GCM", "already registered on device");
			} else {
				Log.v("GCM", "Registering in db");
				TextbookTakeoverApplication aController = (TextbookTakeoverApplication) getApplicationContext();
				aController.register(LoginActivity.this);
			}
		}
	}

	/** Dialog for forget password **/
	@SuppressWarnings("deprecation")
	private void forgetPassword(){
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setContentView(R.layout.forget_password);
		dialog.getWindow().setLayout(display.getWidth()*90/100, LayoutParams.WRAP_CONTENT);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);

		TextView title = (TextView) dialog.findViewById(R.id.alert_title);
		final EditText msg = (EditText) dialog.findViewById(R.id.alert_msg);
		TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
		TextView cancel = (TextView) dialog.findViewById(R.id.alert_cancel);

		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!(msg.getText().toString().matches(emailPattern))
						|| (msg.getText().toString().trim().length() == 0)) {
					msg.setError(getString(R.string.please_verify_mail));
				}else{
					dialog.dismiss();
					new resetAsync().execute(msg.getText().toString());
				}
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		if(!dialog.isShowing()){
			dialog.show();
		}
	}

	// for reset password //

	class resetAsync extends AsyncTask<String, Void, String> {
		private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
		@Override
		protected String doInBackground(String... params) {
			String SOAP_ACTION = Constants.NAMESPACE + Constants.API_FORGET_PASSWORD;

			SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_FORGET_PASSWORD);
			req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
			req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
			req.addProperty("email", params[0]);

			SOAPParsing soap = new SOAPParsing();
			String result = soap.getJSONFromUrl(SOAP_ACTION, req);
			return result;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setMessage("Please Wait...");
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			JSONObject jonj;
			try {
				jonj = new JSONObject(result);
				if (jonj.getString("status").equalsIgnoreCase("true")) {
					TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.success), jonj.getString("message"));
				}else{
					TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.alert), jonj.getString("message"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
			}catch (NullPointerException e) {
				e.printStackTrace();
				TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
			} catch (Exception e){
				e.printStackTrace();
				TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
			}

		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		// For Internet checking disconnect
		TextbookTakeoverApplication.unregisterReceiver(LoginActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// For Internet checking
		TextbookTakeoverApplication.registerReceiver(LoginActivity.this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.login:
				if (!TextbookTakeoverApplication.isNetworkAvailable(this)) {
					TextbookTakeoverApplication.dialog(LoginActivity.this, getString(R.string.error), getString(R.string.network_error));
				} else if (email.getText().toString().trim().length() == 0) {
					email.setError(getString(R.string.please_type_mail));
				} else if (!email.getText().toString().matches(emailPattern)) {
					email.setError(getString(R.string.please_verify_mail));
				} else if (password.getText().toString().length() == 0) {
					password.setError(getString(R.string.please_type_password));
				} else {
					new loginAsync().execute();
				}
				break;
			case R.id.register:
				Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(i);
				break;
			case R.id.backbtn:
				finish();
				break;
			case R.id.forgetpassword:
				forgetPassword();
				break;
		}

	}

}