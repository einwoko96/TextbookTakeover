package com.hitasoft.app.textbooktakeover;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.SOAPParsing;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 16/6/16.
 **/
public class Help extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener {

    ListView hlist;
    HelpAdapter helpadapter;
    ImageView backbtn;
    TextView title;
    AVLoadingIndicatorView progress;
    ArrayList<HashMap<String, String>> helpAry = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        hlist = (ListView)findViewById(R.id.hlist);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView)findViewById(R.id.title);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);

        hlist.setOnItemClickListener(this);
        backbtn.setOnClickListener(this);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.help));

        new getHelp().execute();

        helpadapter=new HelpAdapter(Help.this, helpAry);
        hlist.setAdapter(helpadapter);

    }

    /** class for get the help content from admin **/
    private class getHelp extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String SOAP_ACTION = Constants.NAMESPACE + Constants.API_HELP;

            SoapObject req = new SoapObject(Constants.NAMESPACE, Constants.API_HELP);
            req.addProperty(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            req.addProperty(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);

            SOAPParsing soap = new SOAPParsing();
            String json = soap.getJSONFromUrl(SOAP_ACTION, req);
            //final String json = TextbookTakeoverApplication.loadJSONFromAsset(Help.this, "help.json");

            try {
                JSONObject obj = new JSONObject(json);
                String response = DefensiveClass.optString(obj, Constants.TAG_STATUS);
                if (response.equalsIgnoreCase("true")) {
                    JSONArray result = obj.optJSONArray("result");
                    for (int i = 0; i < result.length(); i++){
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject jobj = result.getJSONObject(i);
                        String page_name = DefensiveClass.optString(jobj, "page_name");
                        String page_content = DefensiveClass.optString(jobj, "page_content");

                        map.put("page_name", page_name);
                        map.put("page_content", page_content);

                        helpAry.add(map);
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
            progress.setVisibility(View.GONE);
            helpadapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.backbtn:
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (helpAry.size() > 0){
            Intent i = new Intent(Help.this, AboutUs.class);
            i.putExtra("title", helpAry.get(position).get("page_name"));
            i.putExtra("content", helpAry.get(position).get("page_content"));
            startActivity(i);
        }
    }

    public class HelpAdapter extends BaseAdapter {

        ArrayList<HashMap<String, String>> helpitem;
        private Context mContext;
        ViewHolder holder = null;
        public HelpAdapter(Context ctx,ArrayList<HashMap<String, String>> item) {
            mContext = ctx;
            helpitem=item;
        }
        @Override
        public int getCount() {

            return helpitem.size();
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
            TextView name;
            ImageView next;
            RelativeLayout mainLay;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.filter_row_selection, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.next = (ImageView) convertView.findViewById(R.id.next);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                holder.next.setVisibility(View.VISIBLE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try{
                holder.name.setText(helpitem.get(position).get("page_name"));

            }catch(NullPointerException e){
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }
            return convertView;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(Help.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(Help.this);
    }
}
