package com.hitasoft.app.joysale;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.SOAPParsing;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

/**
 * Created by hitasoft on 21/6/16.
 **/
public class ChangePassword extends AppCompatActivity implements View.OnClickListener,TextWatcher {
    public static EditText oldpassword,newpassword,confirmpassword;
    TextView save,show, title;
    public static boolean ishow=false;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);

        oldpassword = (EditText) findViewById(R.id.oldPassword);
        newpassword = (EditText) findViewById(R.id.newPassword);
        confirmpassword = (EditText) findViewById(R.id.confirmPassword);
        title = (TextView) findViewById(R.id.title);
        save = (TextView) findViewById(R.id.save);
        show = (TextView) findViewById(R.id.show);
        back = (ImageView)findViewById(R.id.backbtn);

        back.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.changepassword));

        save.setOnClickListener(this);
        back.setOnClickListener(this);
        show.setOnClickListener(this);
        newpassword.addTextChangedListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                finish();
                break;
            case R.id.show:
                if(!ishow) {
                    newpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    newpassword.setSelection(newpassword.length());
                    ishow=true;
                    show.setText(getString(R.string.show));
                }
                else {
                    newpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    newpassword.setSelection(newpassword.length());
                    ishow=false;
                    show.setText(getString(R.string.hide));
                }
                break;
            case R.id.save:
                try {
                    if ((oldpassword.getText().toString().trim()).equals("")){
                        oldpassword.setError(getString(R.string.please_fill));
                        oldpassword.requestFocus();
                    } else if (!oldpassword.getText().toString().equals(GetSet.getPassword())){
                        oldpassword.setError(getString(R.string.wrongpassword));
                    } else if ((newpassword.getText().toString().trim()).equals("")){
                        newpassword.setError(getString(R.string.please_fill));
                        newpassword.requestFocus();
                    } else if(newpassword.getText().length()<6){
                        newpassword.setError(getString(R.string.passwordshould));
                        newpassword.requestFocus();
                    } else if ((oldpassword.getText().toString().trim())
                            .equals(newpassword.getText().toString().trim())){
                        newpassword.setError(getString(R.string.youroldandnew));
                        newpassword.requestFocus();
                    } else if (!(newpassword.getText().toString().trim())
                            .equals(confirmpassword.getText().toString().trim())){
                        confirmpassword.setError(getString(R.string.passwordmismatched));
                        confirmpassword.requestFocus();
                    } else{
                        new changePassword().execute(oldpassword.getText().toString().trim(),newpassword.getText().toString().trim());

                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if(newpassword.getText().length()==0){
            show.setVisibility(View.GONE);
        }else{
            show.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /** class for send new password to server **/
    class changePassword extends AsyncTask<String, Void, String> {

        String newpassword="";
        @Override
        protected String doInBackground(String... params) {

            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_CHANGE_PASSWORD;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_CHANGE_PASSWORD);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("old_password", params[0]);
            req.addProperty("new_password", params[1]);
            newpassword = params[1];

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }
        @Override
        protected void onPostExecute(String result) {
            try{
                JSONObject json = new JSONObject(result);
                String response = json.getString(Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    GetSet.setPassword(newpassword);
                    Constants.editor.putString("Password", GetSet.getPassword());
                    Constants.editor.commit();
                    Toast.makeText(ChangePassword.this, json.getString("message"), Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    JoysaleApplication.dialog(ChangePassword.this, getString(R.string.alert), json.getString("message"));
                }
            }catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
