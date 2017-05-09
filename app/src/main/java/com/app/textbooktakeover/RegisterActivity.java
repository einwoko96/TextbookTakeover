package com.app.textbooktakeover;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.utils.Constants;
import com.app.utils.SOAPParsing;
import com.app.textbooktakeover.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

public class RegisterActivity extends Activity implements OnClickListener {

    EditText email, password, userName, fullName, confirmpwd;
    TextView register, login, title;
    ImageView backbtn;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    RelativeLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        userName = (EditText) findViewById(R.id.userName);
        fullName = (EditText) findViewById(R.id.fullName);
        login = (TextView) findViewById(R.id.login);
        register = (TextView) findViewById(R.id.register);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        main = (RelativeLayout) findViewById(R.id.main);
        confirmpwd = (EditText) findViewById(R.id.confirmpwd);

        TextbookTakeoverApplication.setupUI(RegisterActivity.this, main);

        backbtn.setOnClickListener(this);
        login.setOnClickListener(this);
        register.setOnClickListener(this);

        email.addTextChangedListener(new MyTextWatcher(email));
        password.addTextChangedListener(new MyTextWatcher(password));
        userName.addTextChangedListener(new MyTextWatcher(userName));
        fullName.addTextChangedListener(new MyTextWatcher(fullName));

        fullName.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        userName.setFilters(new InputFilter[]{filterWithoutSpace, new InputFilter.LengthFilter(30)});
    }

    // register class //
    class registerAsync extends AsyncTask<Void, Void, String> {
        private ProgressDialog dialog = new ProgressDialog(
                RegisterActivity.this);

        @Override
        protected String doInBackground(Void... arg0) {
            // Add your data

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_SIGNUP;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_SIGNUP);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_name", userName.getText().toString().trim());
            req.addProperty("full_name", fullName.getText().toString().trim());
            req.addProperty("email", email.getText().toString().trim());
            req.addProperty("password", password.getText().toString().trim());

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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            try {
                JSONObject jonj = new JSONObject(result);
                if (jonj.getString("status").equalsIgnoreCase(
                        "true")) {
                    String msg = jonj.getString("message");
                    dialog(RegisterActivity.this, getString(R.string.success), msg);

                } else {
                    email.setText("");
                    password.setText("");
                    String msg = jonj.getString("message");
                    TextbookTakeoverApplication.dialog(RegisterActivity.this, getString(R.string.alert), msg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                TextbookTakeoverApplication.dialog(RegisterActivity.this, getString(R.string.error), e.getMessage());
            } catch (NullPointerException e) {
                e.printStackTrace();
                TextbookTakeoverApplication.dialog(RegisterActivity.this, getString(R.string.error), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                TextbookTakeoverApplication.dialog(RegisterActivity.this, getString(R.string.error), e.getMessage());
            }
        }
    }

    public void dialog(Context ctx, String title, String content) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                RegisterActivity.this.finish();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                RegisterActivity.this.finish();
            }
        });

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
            if (view != null) {
                view.setError(null);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    }

    InputFilter filterWithoutSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    InputFilter filterWithSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(RegisterActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(RegisterActivity.this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.register:
                if (!TextbookTakeoverApplication.isNetworkAvailable(this)) {
                    TextbookTakeoverApplication.dialog(RegisterActivity.this, getString(R.string.error), getString(R.string.network_error));
                } else if (fullName.getText().toString().trim().length() == 0) {
                    fullName.setError(getString(R.string.please_fill));
                } else if (userName.getText().toString().trim().length() == 0) {
                    userName.setError(getString(R.string.please_fill));
                } else if ((!email.getText().toString().matches(emailPattern))
                        || (email.getText().toString().trim().length() == 0)) {
                    email.setError(getString(R.string.please_verify_mail));
                } else if (password.getText().toString().trim().length() < 6) {
                    password.setError(getString(R.string.passwordshould));
                } else if (!(password.getText().toString().trim())
                        .equals(confirmpwd.getText().toString().trim())) {
                    password.setError(getString(R.string.passwordmismatched));
                } else {
                    new registerAsync().execute();
                }
                break;
            case R.id.backbtn:
                finish();
                break;
            case R.id.login:
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                break;
        }


    }
}
