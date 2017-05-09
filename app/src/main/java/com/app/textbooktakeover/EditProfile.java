package com.app.textbooktakeover;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.SOAPParsing;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.app.textbooktakeover.R;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 11/6/16.
 **/
public class EditProfile extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    public static ImageView logout, backBtn, userImage;
    LinearLayout changepassword, logoutLay, parentLay, callLay;
    RelativeLayout editphoto, languageLay;
    ImageView mailverifiedIcon, mobilverifiedIcon, fbverifiedIcon;
    TextView title, mobilverified, mailverified, fbverified, linkfb, save, language, showphoneno, verify;
    EditText username, name, email;
    int count;
    String fullname = "", phonenum = "", countryCode = "", facebookid = "", uploadedImage= "", viewUrl="", confirmedPhone = "", showPhone = "";
    CallbackManager callbackManager;
    HashMap<String, String> profileMap = new HashMap<String, String>();
    HashMap<String, String> fbData = new HashMap<String, String>();
    AVLoadingIndicatorView progress;
    Dialog dialog;
    SwitchCompat callSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        logout = (ImageView) findViewById(R.id.logout);
        editphoto = (RelativeLayout) findViewById(R.id.editphoto);
        changepassword = (LinearLayout) findViewById(R.id.changePassword);
        logoutLay = (LinearLayout) findViewById(R.id.logoutLay);
        username = (EditText) findViewById(R.id.user_name);
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.emailid);
        userImage = (ImageView) findViewById(R.id.user_image);
        mobilverified = (TextView) findViewById(R.id.mobilverified);
        mailverified = (TextView) findViewById(R.id.mailverified);
        fbverified = (TextView) findViewById(R.id.fbverified);
        linkfb = (TextView) findViewById(R.id.linkfb);
        save = (TextView) findViewById(R.id.save);
        languageLay = (RelativeLayout) findViewById(R.id.languageLay);
        language = (TextView) findViewById(R.id.language);
        parentLay = (LinearLayout) findViewById(R.id.parentLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        showphoneno = (TextView) findViewById(R.id.phoneno);
        verify = (TextView) findViewById(R.id.verify);
        mailverifiedIcon = (ImageView) findViewById(R.id.mailverifiedIcon);
        mobilverifiedIcon = (ImageView) findViewById(R.id.mobilverifiedIcon);
        fbverifiedIcon = (ImageView) findViewById(R.id.fbverifiedIcon);
        callLay = (LinearLayout) findViewById(R.id.callLay);
        callSwitch = (SwitchCompat) findViewById(R.id.callSwitch);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.edit_profile));

        Constants.pref = getApplicationContext().getSharedPreferences("TBTakeoverPref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        new getProfileInformation().execute();
        loginToFacebook();

        backBtn.setOnClickListener(this);
        changepassword.setOnClickListener(this);
        editphoto.setOnClickListener(this);
        logoutLay.setOnClickListener(this);
        linkfb.setOnClickListener(this);
        save.setOnClickListener(this);
        verify.setOnClickListener(this);
        languageLay.setOnClickListener(this);
    }

    /** for get profile information of user **/
    private class getProfileInformation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            parentLay.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_PROFILE;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_PROFILE);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            try {
                JSONObject obj = new JSONObject(json);
                String response = DefensiveClass.optString(obj, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    JSONObject result = obj.optJSONObject("result");
                    if (!(result == null)) {
                        String userid = DefensiveClass.optString(result, "user_id");
                        String fullname = DefensiveClass.optString(result, "full_name");
                        String username = DefensiveClass.optString(result, "user_name");
                        String userimage = DefensiveClass.optString(result, "user_img");
                        String email = DefensiveClass.optString(result, "email");
                        String facebook_id = DefensiveClass.optString(result, "facebook_id");
                        String mobile_no = DefensiveClass.optString(result, "mobile_no");
                        String show_mob = DefensiveClass.optString(result, "show_mobile_no");
                        JSONObject verification = result.optJSONObject("verification");
                        String facebook_ver = DefensiveClass.optString(verification, "facebook");
                        String email_ver = DefensiveClass.optString(verification, "email");
                        String mob_ver = DefensiveClass.optString(verification, "mob_no");

                        profileMap.put("user_id", userid);
                        profileMap.put("user_name", username);
                        profileMap.put("full_name", fullname);
                        profileMap.put("user_img", userimage);
                        profileMap.put("email", email);
                        profileMap.put("facebook_id", facebook_id);
                        profileMap.put("mobile_no", mobile_no);
                        profileMap.put("facebook_ver", facebook_ver);
                        profileMap.put("email_ver", email_ver);
                        profileMap.put("mob_ver", mob_ver);
                        profileMap.put("show_mobile_no", show_mob);

                        Log.v("userimage", "userimage" + userimage);

                    } else {

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v("profileMap", "profileMap" + profileMap);
            progress.setVisibility(View.GONE);
            if (profileMap.size() == 0) {
                //    disabledialog(getString(R.string.profile_problem));
                Toast.makeText(EditProfile.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            } else {
                save.setVisibility(View.VISIBLE);
                parentLay.setVisibility(View.VISIBLE);
                setProfileInformation();
            }
        }
    }

    /** set the information to elements **/
    private void setProfileInformation() {
        try {
            language.setText(Constants.pref.getString("language", "English"));
            if (profileMap.size() > 0){
                username.setText(profileMap.get("user_name"));
                name.setText(profileMap.get("full_name"));
                fullname = profileMap.get("full_name").toString();
                email.setText(profileMap.get("email"));
                viewUrl = profileMap.get("user_img");
                Picasso.with(EditProfile.this).load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImage);

                if (profileMap.get("facebook_ver").equals("true")) {
                    fbverified.setText(getString(R.string.verified));
                    fbverifiedIcon.setImageResource(R.drawable.tick);
                    linkfb.setEnabled(false);
                    linkfb.setVisibility(View.GONE);
                } else {
                    fbverified.setText(getString(R.string.unverified));
                    fbverifiedIcon.setImageResource(R.drawable.cancel);
                    fbverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                    linkfb.setVisibility(View.VISIBLE);
                    linkfb.setEnabled(true);
                }
                if (profileMap.get("mob_ver").equals("true")) {
                    verify.setText(getString(R.string.change));
                    mobilverified.setText(getString(R.string.verified));
                    mobilverifiedIcon.setImageResource(R.drawable.tick);
                    showphoneno.setVisibility(View.VISIBLE);
                    showphoneno.setText(profileMap.get("mobile_no") + " has been verified");
                    mobilverified.setEnabled(true);
                    mobilverifiedIcon.setImageResource(R.drawable.tick);
                    callLay.setVisibility(View.VISIBLE);
                } else {
                    verify.setText(getString(R.string.verify));
                    mobilverified.setText(getString(R.string.unverified));
                    mobilverifiedIcon.setImageResource(R.drawable.cancel);
                    mobilverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                    mobilverified.setEnabled(false);
                    callLay.setVisibility(View.GONE);
                }
                if (profileMap.get("email_ver").equals("true")) {
                    mailverified.setText(getString(R.string.verified));
                    mailverifiedIcon.setImageResource(R.drawable.tick);
                } else {
                    mailverified.setText(getString(R.string.unverified));
                    mailverifiedIcon.setImageResource(R.drawable.cancel);
                    mailverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                }

                if (profileMap.get("show_mobile_no").equals("true")) {
                    callSwitch.setChecked(true);
                } else {
                    callSwitch.setChecked(false);
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    /** dialog for confirm the user to signout **/
    public void signoutdialog() {
        final Dialog dialog = new Dialog(EditProfile.this ,R.style.AlertDialog);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(display.getWidth()*90/100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);
        TextView alertCancel = (TextView) dialog.findViewById(R.id.cancel_button);

        alertMsg.setText(getString(R.string.reallySignOut));
        alertOk.setText(getString(R.string.yes));
        alertCancel.setText(getString(R.string.no));

        alertCancel.setVisibility(View.VISIBLE);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TextbookTakeoverApplication aController = new TextbookTakeoverApplication();
                aController.unregister(EditProfile.this);
                Constants.editor.clear();
                Constants.editor.commit();
                GetSet.reset();
                FragmentMainActivity.HomeItems.clear();
                FragmentMainActivity.currentPage = 0;
                MessageActivity.Messagepageitems.clear();
                WelcomeActivity.fromSignout = true;
                finish();
                Intent p = new Intent(EditProfile.this, WelcomeActivity.class);
                startActivity(p);
            }
        });

        alertCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if(!dialog.isShowing()){
            dialog.show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mobilverified.setText(getString(R.string.change));
        mobilverified.setEnabled(true);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.v("RESULT_OK", "");
            if (requestCode == 2) {
                try {
                    Uri selectedImage = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage,
                            filePath, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    String picturePath = c.getString(columnIndex);
                    Log.v("path of gallery", picturePath + "");
                    c.close();
                    Bitmap thumbnail = decodeFile(picturePath);
                    Log.v("gallery code bitmap", "" + thumbnail);
                    new moreLoadItems().execute(picturePath);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError ome) {
                    ome.printStackTrace();
                }
            }
        } else {
            Log.v("else" + requestCode, "result" + resultCode);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private Bitmap decodeFile(String fPath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inDither = false;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        BitmapFactory.decodeFile(fPath, opts);
        final int REQUIRED_SIZE = 1024;
        int scale = 1;

        if (opts.outHeight > REQUIRED_SIZE || opts.outWidth > REQUIRED_SIZE) {
            final int heightRatio = Math.round((float) opts.outHeight
                    / (float) REQUIRED_SIZE);
            final int widthRatio = Math.round((float) opts.outWidth
                    / (float) REQUIRED_SIZE);
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;//
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;
        Bitmap bm = BitmapFactory.decodeFile(fPath, opts).copy(
                Bitmap.Config.RGB_565, false);
        return bm;
    }

    /** save the edited details to server **/
    class Editprofile extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_EDIT_PROFILE;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_EDIT_PROFILE);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("full_name", fullname);
            req.addProperty("user_img", uploadedImage);
            req.addProperty("facebook_id", facebookid);
            req.addProperty("mobile_no", confirmedPhone);
            req.addProperty("show_mobile_no", showPhone);

            if (fbData.size() > 0) {
                req.addProperty("fb_email", fbData.get("email"));
                req.addProperty("fb_firstname", fbData.get("first_name"));
                req.addProperty("fb_lastname", fbData.get("last_name"));
                req.addProperty("fb_phone", "");
                req.addProperty("fb_profileurl", fbData.get("profile_url"));
            }

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String res) {
            Log.v("json", "editprofil" + res);
            try {
                JSONObject json = new JSONObject(res);
                String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                if (status.equalsIgnoreCase("true")){
                    Toast.makeText(EditProfile.this, getString(R.string.your_changes_saved), Toast.LENGTH_SHORT).show();
                    Profile.userName.setText(fullname);
                    Profile.userName2.setText(fullname);
                    Picasso.with(EditProfile.this).load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(Profile.mHeaderLogo);
                    Picasso.with(EditProfile.this).load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(Profile.userImg);
                    Profile.profileMap.put("full_name", fullname);
                    Profile.profileMap.put("user_img", viewUrl);
                    GetSet.setFullName(fullname);
                    GetSet.setImageUrl(viewUrl);
                    FragmentMainActivity.username.setText(GetSet.getFullName());
                    Picasso.with(EditProfile.this).load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(FragmentMainActivity.userImage);
                    Constants.editor.putString("photo", viewUrl);
                    Constants.editor.putString("fullName", fullname);
                    Constants.editor.commit();

                    JSONObject result = json.optJSONObject("result");
                    JSONObject verification = result.optJSONObject("verification");
                    String facebook_ver = DefensiveClass.optString(verification, "facebook");
                    String email_ver = DefensiveClass.optString(verification, "email");
                    String mob_ver = DefensiveClass.optString(verification, "mob_no");
                    Profile.profileMap.put("facebook_ver", facebook_ver);
                    Profile.profileMap.put("email_ver", email_ver);
                    Profile.profileMap.put("mob_ver", mob_ver);
                    if (facebook_ver.equals("true")){
                        Profile.fbVerify.setImageResource(R.drawable.fb_veri);
                    } else {
                        Profile.fbVerify.setImageResource(R.drawable.fb_unveri);
                    }
                    if (email_ver.equals("true")){
                        Profile.mailVerify.setImageResource(R.drawable.mail_veri);
                    } else {
                        Profile.mailVerify.setImageResource(R.drawable.mail_unveri);
                    }
                    if (mob_ver.equals("true")){
                        Profile.mobVerify.setImageResource(R.drawable.mob_veri);
                    } else {
                        Profile.mobVerify.setImageResource(R.drawable.mob_unveri);
                    }

                    finish();
                }
            } catch (JSONException e){
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** class for get otp for mobile verfication**/
    class GetOtp extends AsyncTask<Void, Void, String> {

        JSONObject jsonobject = null;
        String status, otp="";
        ProgressDialog pd;
        @Override
        protected String doInBackground(Void... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_GET_OTP;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_GET_OTP);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("mob_no", phonenum);
            req.addProperty("country_code", countryCode);

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            return json;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(EditProfile.this);
            pd.setMessage("loading");
            pd.show();
        }

        @Override
        protected void onPostExecute(String Json) {
            super.onPostExecute(Json);
            Log.v("json", "OTP" + Json);
            try {
                jsonobject = new JSONObject(Json);
                Log.v("json", "json" + Json);
                status = jsonobject.getString("status");
                if (status.equals("true")) {
                   /* JSONObject image = jsonobject.getJSONObject("Image");
                    String msg = image.getString("Message");
                    String uploadedimgname = image.getString("Name");*/
                    otp = "";
                    //otpDialog("otp");
                    LinearLayout numberLay = (LinearLayout) dialog.findViewById(R.id.numberLay);
                    LinearLayout otpLay = (LinearLayout) dialog.findViewById(R.id.otpLay);
                    numberLay.setVisibility(View.GONE);
                    otpLay.setVisibility(View.VISIBLE);

                } else {
                    otp = "";
                    Toast.makeText(EditProfile.this, DefensiveClass.optString(jsonobject, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
            }
            pd.dismiss();
            mobilverified.setEnabled(true);

            //finish();
        }
    }

    /** confirm the given otp for mobile verfication **/
    class ConfirmOtp extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_CONFIRM_OTP;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_CONFIRM_OTP);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            req.addProperty("user_id", GetSet.getUserId());
            req.addProperty("otp", params[0]);
            req.addProperty("mob_no", phonenum);

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);

            //parsing(json);
            return json;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String unused) {
            super.onPostExecute(unused);
            try {
                JSONObject json = new JSONObject(unused);
                String status = json.getString(Constants.TAG_STATUS);
                String message = json.getString(Constants.TAG_MESSAGE);
                if (status.equalsIgnoreCase("true")) {
                    TextbookTakeoverApplication.dialog(EditProfile.this, getString(R.string.success), message);
                    dialog.dismiss();
                    confirmedPhone = phonenum;
                    mobilverified.setText("Verified");
                    mobilverifiedIcon.setImageResource(R.drawable.tick);
                    showphoneno.setVisibility(View.VISIBLE);
                    showphoneno.setText(profileMap.get("mobile_no")+" has been verified");
                    profileMap.put("mobile_no", phonenum);
                } else {
                    TextbookTakeoverApplication.dialog(EditProfile.this, getString(R.string.alert), "Please enter correct verification code");
                    mobilverified.setText("UnVerified");
                    mobilverifiedIcon.setImageResource(R.drawable.cancel);
                    mobilverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                }
                //	reportItem.setEnabled(true);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void galleryAddPic(String file) {
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        sendBroadcast(mediaScanIntent);
    }

    public File saveBitmapToFile(File file) {
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 2;
            o.inPreferredConfig = Bitmap.Config.RGB_565;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 1024;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPreferredConfig = Bitmap.Config.RGB_565;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file

            //file.createNewFile();
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/" + getString(R.string.app_name));
            dir.mkdirs();
            file = new File(dir, String.valueOf(System.currentTimeMillis()) + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            galleryAddPic(file.toString());
            outputStream.flush();
            outputStream.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** for upload user image **/
    class moreLoadItems extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "";
        String status;
        ProgressDialog pd;

        @Override
        protected Integer doInBackground(String... imgpath) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            StringBuilder builder = new StringBuilder();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String urlString = Constants.API_UPLOAD_USER;
            try {
                String exsistingFileName = imgpath[0];
                Log.v(" exsistingFileName", exsistingFileName);
                FileInputStream fileInputStream = new FileInputStream(saveBitmapToFile(new File(exsistingFileName)));
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + exsistingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                Log.e("MediaPlayer", "Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                Log.v("buffer", "buffer" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v("bytesRead", "bytesRead" + bytesRead);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                Log.v("in", "" + in);
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e("MediaPlayer", "File is written");
                fileInputStream.close();
                Json = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e("MediaPlayer", "Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
            }
            try {
                jsonobject = new JSONObject(Json);
                Log.v("json", "json" + Json);
                status = jsonobject.getString("status");
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    String msg = image.getString("Message");
                    uploadedImage = image.getString("Name");
                    viewUrl = Constants.url + "user/resized/150/" + uploadedImage;
                }

            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(EditProfile.this);
            pd.setMessage("Loading");
            pd.show();
        }

        @Override
        protected void onPostExecute(Integer unused) {
            Log.v("editprofile", "imageupload" + uploadedImage);
            Picasso.with(EditProfile.this).load(viewUrl).into(userImage);
            pd.dismiss();
        }
    }

    /** for user choose the dp from camera or gallery **/
    public void galdialog() {
        final Dialog dialog = new Dialog(EditProfile.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.gallerypopup);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        ImageView gal = (ImageView) dialog.findViewById(R.id.galery);
        ImageView cam = (ImageView) dialog.findViewById(R.id.camra);

        cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditProfile.this, EditProfilePhoto.class);
                startActivity(i);
                dialog.dismiss();
            }
        });
        gal.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(in, 2);
                dialog.dismiss();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void otpDialog() {
        dialog = new Dialog(EditProfile.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.mobotp);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final LinearLayout numberLay = (LinearLayout) dialog.findViewById(R.id.numberLay);
        final LinearLayout otpLay = (LinearLayout) dialog.findViewById(R.id.otpLay);
        final EditText counCode = (EditText) dialog.findViewById(R.id.countryCode);
        final EditText mobile = (EditText) dialog.findViewById(R.id.mobile);
        final ProgressBar progress1 = (ProgressBar) dialog.findViewById(R.id.progress1);
        final ProgressBar progress2 = (ProgressBar) dialog.findViewById(R.id.progress2);
        TextView verify = (TextView) dialog.findViewById(R.id.verify);
        final TextView cancel = (TextView) dialog.findViewById(R.id.cancel);
        final TextView cancel2 = (TextView) dialog.findViewById(R.id.cancel2);
        TextView confirmotp = (TextView) dialog.findViewById(R.id.confirm);
        final EditText otp = (EditText) dialog.findViewById(R.id.otp);

        numberLay.setVisibility(View.VISIBLE);
        otpLay.setVisibility(View.GONE);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phonenum = mobile.getText().toString().trim();
                countryCode = counCode.getText().toString().trim();
                if (!phonenum.isEmpty() && countryCode.length()!=0) {
                    if (!mobile.getText().toString().equals(profileMap.get("mobile_no"))) {
                        TextbookTakeoverApplication.hideSoftKeyboard(EditProfile.this);
                        progress1.setVisibility(View.VISIBLE);
                        cancel.setEnabled(false);
                        new GetOtp().execute();
                    }else{
                        Toast.makeText(EditProfile.this,"Your account is already configured with this mobile number", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(EditProfile.this,getString(R.string.enter_code),Toast.LENGTH_SHORT).show();
                }
            }
        });

        confirmotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otp.getText().toString().trim().length() != 0) {
                    new ConfirmOtp().execute(otp.getText().toString());
                    progress2.setVisibility(View.VISIBLE);
                    cancel2.setEnabled(false);
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        cancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /** for fb confirmation **/
    public void loginToFacebook() {
        Log.v("loginToFacebook", "loginToFacebook");
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.v("loginToFacebook", "onSuccess");
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject profile,
                                            GraphResponse response) {
                                        Log.v("json", "object" + profile);
                                        // Application code
                                        try {
                                            String email = "";
                                            if (profile.has("email")) {
                                                email = profile.getString("email");
                                            } else {
                                                email = "";
                                            }
                                            String userId = profile.getString("id");
                                            String firstName = profile.getString("first_name");
                                            String lastName = profile.getString("last_name");

                                            fbData.put("id",userId);
                                            fbData.put("email", email);
                                            fbData.put("first_name", firstName);
                                            fbData.put("last_name", lastName);
                                            fbData.put("profile_url", "https://www.facebook.com/app_scoped_user_id/"+userId+"/");

                                            facebookid = userId;
                                            fbverified.setText("Verified");
                                            fbverified.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            linkfb.setEnabled(false);
                                            linkfb.setVisibility(View.GONE);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,first_name,last_name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(EditProfile.this, "Facebook - Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.v("loginToFacebook", "onError" + exception);
                        Toast.makeText(EditProfile.this, "Facebook - " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        if (exception instanceof FacebookAuthorizationException) {
                            if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });

    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(EditProfile.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(EditProfile.this);
        if (EditProfilePhoto.editPhoto){
            EditProfilePhoto.editPhoto = false;
            new moreLoadItems().execute(EditProfilePhoto.imgPath);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(EditProfile.this, WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EditProfile.this, new String[]{ WRITE_EXTERNAL_STORAGE }, 102);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("requestCode", "requestCode=" + requestCode);
        if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(EditProfile.this, getString(R.string.storage_permission_access), Toast.LENGTH_SHORT).show();
            //finish();
        } else {
            Toast.makeText(EditProfile.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                fullname = name.getText().toString();
                if (callSwitch.isChecked()){
                    showPhone = "true";
                } else {
                    showPhone = "false";
                }
                new Editprofile().execute();
                break;
            case R.id.editphoto:
                galdialog();
                break;
            case R.id.changePassword:
                Intent j = new Intent(EditProfile.this, ChangePassword.class);
                startActivity(j);
                break;
            case R.id.logoutLay:
                signoutdialog();
                break;
            case R.id.backbtn:
                this.finish();
                break;
            case R.id.verify:
                otpDialog();
                break;
            case R.id.linkfb:
                LoginManager.getInstance().logInWithReadPermissions(EditProfile.this, Arrays.asList("public_profile", "email"));
                break;
            case R.id.languageLay:
                Intent i = new Intent(EditProfile.this, Language.class);
                startActivity(i);
                break;
        }

    }
}
