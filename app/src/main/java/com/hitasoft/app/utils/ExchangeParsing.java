package com.hitasoft.app.utils;

import android.content.Context;

import com.hitasoft.app.joysale.JoysaleApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 14/7/16.
 */

public class ExchangeParsing  {

    String from="";
    Context context;

    // constructor
    public ExchangeParsing(Context ctx) {
        this.context = ctx;
    }

    public ExchangeParsing(String from, Context ctx) {
        this.from = from;
        this.context = ctx;
    }

    public ArrayList<HashMap<String,String>> parsing(String jsonString) {
        ArrayList<HashMap<String, String>> exchangeAry = new ArrayList<HashMap<String, String>>();
        try{
            JSONObject json =new JSONObject(jsonString);
            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);

            if (response.equalsIgnoreCase("true")) {
                JSONObject result = json
                        .optJSONObject(Constants.TAG_RESULT);
                if (result != null) {
                    JSONArray exchange = result.getJSONArray(Constants.TAG_EXCHANGE);
                    if(exchange != null){
                        for(int i=0 ; i < exchange.length() ; i++){
                            HashMap<String, String> map = new HashMap<String, String>();
                            JSONObject temp = exchange.getJSONObject(i);

                            String mitemId = "",mitemName="",mitemImg="",eitemId = "",eitemName="",eitemImg="";

                            String type = DefensiveClass.optString(temp, Constants.TAG_TYPE);


                            String exchangeId = DefensiveClass.optString(temp, Constants.TAG_EXCHANGEID);
                            String status = DefensiveClass.optString(temp, Constants.TAG_STATUS);
                            String requestByMe = DefensiveClass.optString(temp, Constants.TAG_REQUEST_BY_ME);
                            String exchangeTime = DefensiveClass.optString(temp, Constants.TAG_EXCHANGETIME);
                            String exchangerName = DefensiveClass.optString(temp, Constants.TAG_EXCHANGERNAME);
                            String exchangerUserName = DefensiveClass.optString(temp, Constants.TAG_EXCHANGERUSERNAME);
                            String exchangerId = DefensiveClass.optString(temp, Constants.TAG_EXCHANGERID);
                            String exchangerImg = DefensiveClass.optString(temp, Constants.TAG_EXCHANGERIMG);
                            JSONObject mproduct = temp.getJSONObject(Constants.TAG_MYPRODUCT);
                            if(mproduct != null){
                                mitemId = DefensiveClass.optString(mproduct, Constants.TAG_ITEMID);
                                mitemImg = DefensiveClass.optString(mproduct, Constants.TAG_ITEMIMAGE).replace("100x100", "200");
                                mitemName = DefensiveClass.optString(mproduct, Constants.TAG_ITEM_NAME);
                            }

                            JSONObject eproduct = temp.getJSONObject(Constants.TAG_EXCHANGEPRODUCT);
                            if(eproduct != null){
                                eitemId = DefensiveClass.optString(eproduct, Constants.TAG_ITEMID);
                                eitemImg = DefensiveClass.optString(eproduct, Constants.TAG_ITEMIMAGE).replace("100x100", "200");
                                eitemName = DefensiveClass.optString(eproduct, Constants.TAG_ITEM_NAME);
                            }

                            map.put(Constants.TAG_TYPE, type);
                            map.put(Constants.TAG_EXCHANGEID, exchangeId);
                            map.put(Constants.TAG_STATUS, status);
                            map.put(Constants.TAG_REQUEST_BY_ME, requestByMe);
                            map.put(Constants.TAG_EXCHANGETIME, exchangeTime);
                            map.put(Constants.TAG_EXCHANGERNAME, exchangerName);
                            map.put(Constants.TAG_EXCHANGERUSERNAME, exchangerUserName);
                            map.put(Constants.TAG_EXCHANGERID, exchangerId);
                            map.put(Constants.TAG_EXCHANGERIMG, exchangerImg);
                            map.put("m"+Constants.TAG_ITEMID, mitemId);
                            map.put("m"+Constants.TAG_ITEMIMAGE, mitemImg);
                            map.put("m"+Constants.TAG_ITEM_NAME, mitemName);
                            map.put("e"+Constants.TAG_ITEMID, eitemId);
                            map.put("e"+Constants.TAG_ITEMIMAGE, eitemImg);
                            map.put("e"+Constants.TAG_ITEM_NAME, eitemName);

                            exchangeAry.add(map);
                        }
                    }
                }
            } else if (response.equalsIgnoreCase("error")){
                JoysaleApplication.disabledialog(context, json.optString("message"));
            }

        }catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return exchangeAry;
    }
}
