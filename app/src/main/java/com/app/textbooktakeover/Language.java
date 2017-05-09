package com.app.textbooktakeover;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;
import com.app.utils.Constants;

import java.util.Locale;

/**
 * Created by hitasoft on 21/7/16.
 */

public class Language extends AppCompatActivity implements View.OnClickListener{

    String[] languages, langCode;
    ListView listView;
    TextView categoryName, title;
    ImageView backbtn;
    String selectedLang = Constants.LANGUAGE;
    LanguageAdapter languageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sub_category);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        listView = (ListView) findViewById(R.id.listView);
        categoryName = (TextView) findViewById(R.id.categoryName);
        title = (TextView) findViewById(R.id.title);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        categoryName.setVisibility(View.GONE);

        title.setText(getString(R.string.language));

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        backbtn.setOnClickListener(this);

        selectedLang = Constants.pref.getString("language", Constants.LANGUAGE);

        languages = getResources().getStringArray(R.array.languages);
        langCode = getResources().getStringArray(R.array.languageCode);
        languageAdapter = new LanguageAdapter(Language.this, languages);
        listView.setAdapter(languageAdapter);

    }

    /** adapter for set the language to list **/
    public class LanguageAdapter extends BaseAdapter {

        private Context mContext;
        String [] lang;
        ViewHolder holder = null;

        public LanguageAdapter(Context ctx, String [] data) {
            mContext = ctx;
            lang = data;
        }

        @Override
        public int getCount() {
            return lang.length;
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
            ImageView tick;
            TextView name;
            RelativeLayout mainLay;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.filter_row_selection, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.tick = (ImageView) convertView.findViewById(R.id.tick);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {

                if (TextbookTakeoverApplication.isRTL(mContext)){
                    holder.name.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                } else {
                    holder.name.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                }

                holder.name.setText(lang[position]);
                if (lang[position].equals(selectedLang)){
                    holder.tick.setVisibility(View.VISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.tick.setVisibility(View.INVISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedLang = lang[position];
                        languageAdapter.notifyDataSetChanged();

                        Constants.editor.putString("language", selectedLang);
                        Constants.editor.commit();

                        Constants.REGISTER_ID = GCMRegistrar.getRegistrationId(getApplicationContext());
                        Log.v("enetered push","registerid="+Constants.REGISTER_ID);
                        Constants.editor.putString("registerId", Constants.REGISTER_ID);
                        Constants.editor.commit();

                        setLocale(langCode[position]);

                        TextbookTakeoverApplication aController = (TextbookTakeoverApplication) getApplicationContext();
                        aController.register(Language.this);
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }

    /** function for change the selected language **/
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, FragmentMainActivity.class);
        startActivity(refresh);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                finish();
                break;
        }
    }
}
