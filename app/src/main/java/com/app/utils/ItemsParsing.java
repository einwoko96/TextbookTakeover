package com.app.utils;
/****************
*
* @author 'Hitasoft Technologies'
* 
* Description:  
* This class is common for parsing items and defensive coding also included.
*
* Revision History:
* Version 1.0 - Initial Version
*
*****************/

import android.content.Context;
import android.graphics.Color;
import android.text.Html;

import com.app.textbooktakeover.TextbookTakeoverApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ItemsParsing {
	
	String from="";
	Context context;

	// constructor
    public ItemsParsing(Context ctx) {
		this.context = ctx;
	}
    
    public ItemsParsing(String from, Context ctx) {
       this.from = from;
	   this.context = ctx;
	}
    
    public ArrayList<HashMap<String,String>> parsing(String jsonString){
        ArrayList<HashMap<String, String>> HomePageItems=new ArrayList<HashMap<String,String>>();
		JSONArray items;
		HashMap<String, String> map;
		
		try{
			
			JSONObject json =new JSONObject(jsonString);
			String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
			
				if (response.equalsIgnoreCase("true")) {
					
				JSONObject result = json
						.optJSONObject(Constants.TAG_RESULT);
				if (result != null) {
				items = result.optJSONArray(Constants.TAG_ITEMS);
				if (items != null) {
				for (int i = 0 ;i < items.length(); i++) {
					String itemUrl350=null, itemUrlOrg=null, width="", height="";
					map = new HashMap<String, String>();
					JSONObject temp = items.getJSONObject(i);
					String id = DefensiveClass.optInt(temp, Constants.TAG_ID);
					String item_title = String.valueOf(Html.fromHtml(DefensiveClass.optString(temp, Constants.TAG_TITLE)));
					String description = DefensiveClass.optString(temp, Constants.TAG_ITEM_DES);
					String condition = DefensiveClass.optString(temp, Constants.TAG_ITEM_CONDITION);
					String price = DefensiveClass.optInt(temp, Constants.TAG_PRICE);
					String quantity = DefensiveClass.optInt(temp, Constants.TAG_QUANTITY);
					String itemstatus = DefensiveClass.optString(temp, Constants.TAG_ITEM_STATUS);
					String sellerid = DefensiveClass.optInt(temp, Constants.TAG_SELLERID);
					String sellername = DefensiveClass.optString(temp, Constants.TAG_SELLERNAME);
					String sellerimg = DefensiveClass.optString(temp, Constants.TAG_SELLERIMG);
					String prourl= DefensiveClass.optString(temp, Constants.TAG_PROURL);
					String likescount = DefensiveClass.optInt(temp, Constants.TAG_LIKECOUNT);
					String commentscount = DefensiveClass.optInt(temp, Constants.TAG_COMMENTCOUNT);
					String viewscount = DefensiveClass.optInt(temp, Constants.TAG_VIEWCOUNT);
					String liked = DefensiveClass.optString(temp, Constants.TAG_LIKED);
					String postedtime = DefensiveClass.optString(temp, Constants.TAG_POSTED_TIME);
					String latitude = DefensiveClass.optString(temp, Constants.TAG_LATITUDE);
					String longitude = DefensiveClass.optString(temp, Constants.TAG_LONGITUDE);
					String location = DefensiveClass.optString(temp, Constants.TAG_LOCATION);
					String bestoffer = DefensiveClass.optString(temp, Constants.TAG_BEST_OFFER);
					String buytype = DefensiveClass.optString(temp, Constants.TAG_BUYTYPE);
					String catname = DefensiveClass.optString(temp, Constants.TAG_CATEGORYNAME);
					String catid = DefensiveClass.optString(temp, Constants.TAG_CATEGORYID);
					String subcatname = DefensiveClass.optString(temp, Constants.TAG_SUBCATEGORYNAME);
					String subcatid = DefensiveClass.optString(temp, Constants.TAG_SUBCATEGORYID);
					String paypalid = DefensiveClass.optString(temp, Constants.TAG_PAYPALID);
					String shiptime = DefensiveClass.optString(temp, Constants.TAG_SHIPPING_TIME);
					String report = DefensiveClass.optString(temp, Constants.TAG_REPORT);
					String promotiontype = DefensiveClass.optString(temp, Constants.TAG_PROMOTION_TYPE);
					String exchangebuy = DefensiveClass.optString(temp, Constants.TAG_EXCHANGE_BUY);
					String makeoffer = DefensiveClass.optString(temp, Constants.TAG_MAKE_OFFER);
					String sellerusername = DefensiveClass.optString(temp, Constants.TAG_SELLER_USERNAME);
					String fbverify = DefensiveClass.optString(temp, Constants.TAG_FACEBOOK_VERIFICATION);
					String mobverify = DefensiveClass.optString(temp, Constants.TAG_MOBILE_VERIFICATION);
					String mailverify = DefensiveClass.optString(temp, Constants.TAG_EMAIL_VERIFICATION);
					String countryid = DefensiveClass.optString(temp, Constants.TAG_COUNTRYID);
					String instantbuy = DefensiveClass.optString(temp, Constants.TAG_INSTANT_BUY);
					String shippingcost = DefensiveClass.optInt(temp, Constants.TAG_SHIPPING_COST);
					String mobileno = DefensiveClass.optString(temp, Constants.TAG_MOBILE_NO);
					String showsellermob = DefensiveClass.optString(temp, Constants.TAG_SHOW_SELLER_MOB);
					String item_approve = DefensiveClass.optString(temp, Constants.TAG_ITEM_APPROVE);
					String seller_rating = DefensiveClass.optInt(temp, Constants.TAG_SELLER_RATING);

					String currency = DefensiveClass.optCurrency(temp, Constants.TAG_CURRENCY_CODE);
					String currencysym = "";
					if (currency.contains("-")) {
						String cur[] = currency.split("-");
						currencysym = cur[0];
					} else {
						currencysym = currency;
					}
					
					JSONArray shipdetail = temp.optJSONArray(Constants.TAG_SHIPPING_DETAIL);
					if(shipdetail==null){
						map.put(Constants.TAG_SHIPPING_DETAIL, "");
					}else{
						map.put(Constants.TAG_SHIPPING_DETAIL, shipdetail.toString());
					}
					
					JSONArray size = temp.optJSONArray(Constants.TAG_SIZE);
					if(size==null){
						map.put(Constants.TAG_SIZE, "");
					}else{
						map.put(Constants.TAG_SIZE, size.toString());
					}
					
					JSONArray photos = temp.optJSONArray(Constants.TAG_PHOTOS);
					if(photos==null){
						map.put(Constants.TAG_PHOTOS, "");
					}else{
						map.put(Constants.TAG_PHOTOS, photos.toString());
					for(int j = 0; j < photos.length(); j++){
						JSONObject jph = photos.optJSONObject(j);
						if(j==0){
						   itemUrl350 = DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_350);
						   itemUrlOrg = DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_ORG);
							width = DefensiveClass.optString(jph, Constants.TAG_WIDTH);
							height = DefensiveClass.optString(jph, Constants.TAG_HEIGHT);
						}
					}
					}
					
					Random rnd = new Random(); 
					int color = Color.argb(25, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
				
					map.put(Constants.TAG_ID, id);
					map.put(Constants.TAG_TITLE, item_title);
					map.put(Constants.TAG_ITEM_DES, description);
					map.put(Constants.TAG_ITEM_CONDITION, condition);
					map.put(Constants.TAG_PRICE, price);
					map.put(Constants.TAG_QUANTITY, quantity);
					map.put(Constants.TAG_ITEM_STATUS, itemstatus);
					map.put(Constants.TAG_SELLERID, sellerid);
					map.put(Constants.TAG_SELLERNAME, sellername);
					map.put(Constants.TAG_SELLERIMG, sellerimg);
					map.put(Constants.TAG_CURRENCY_CODE, currency);
					map.put(Constants.TAG_PROURL, prourl);
					map.put(Constants.TAG_LIKECOUNT, likescount);
					map.put(Constants.TAG_COMMENTCOUNT, commentscount);
					map.put(Constants.TAG_VIEWCOUNT, viewscount);
					map.put(Constants.TAG_LIKED, liked);
					map.put(Constants.TAG_POSTED_TIME, postedtime);
					map.put(Constants.TAG_LATITUDE, latitude);
					map.put(Constants.TAG_LONGITUDE, longitude);
					map.put(Constants.TAG_LOCATION, location);
					map.put(Constants.TAG_BEST_OFFER, bestoffer);
					map.put(Constants.TAG_BUYTYPE, buytype);
					map.put(Constants.TAG_ITEM_URL_350, itemUrl350);
					map.put(Constants.TAG_ITEM_URL_ORG, itemUrlOrg);
					map.put(Constants.TAG_CATEGORYNAME, catname);
					map.put(Constants.TAG_CATEGORYID, catid);
					map.put(Constants.TAG_SUBCATEGORYNAME, subcatname);
					map.put(Constants.TAG_SUBCATEGORYID, subcatid);
					map.put(Constants.TAG_PAYPALID, paypalid);
					map.put(Constants.TAG_SHIPPING_TIME, shiptime);
					map.put(Constants.TAG_COLOR, Integer.toString(color));
					map.put(Constants.TAG_REPORT, report);
					map.put(Constants.TAG_WIDTH, width);
					map.put(Constants.TAG_HEIGHT, height);
					map.put(Constants.TAG_PROMOTION_TYPE, promotiontype);
					map.put(Constants.TAG_EXCHANGE_BUY, exchangebuy);
					map.put(Constants.TAG_MAKE_OFFER, makeoffer);
					map.put(Constants.TAG_SELLER_USERNAME, sellerusername);
					map.put(Constants.TAG_FACEBOOK_VERIFICATION, fbverify);
					map.put(Constants.TAG_MOBILE_VERIFICATION, mobverify);
					map.put(Constants.TAG_EMAIL_VERIFICATION, mailverify);
					map.put(Constants.TAG_COUNTRYID, countryid);
					map.put(Constants.TAG_INSTANT_BUY, instantbuy);
					map.put(Constants.TAG_SHIPPING_COST, shippingcost);
					map.put(Constants.TAG_CURRENCY_SYM, currencysym);
					map.put(Constants.TAG_MOBILE_NO, mobileno);
					map.put(Constants.TAG_SHOW_SELLER_MOB, showsellermob);
					map.put(Constants.TAG_ITEM_APPROVE, item_approve);
					map.put(Constants.TAG_SELLER_RATING, seller_rating);
					  
					HomePageItems.add(map);
				}
				}
				}
			} else if (response.equalsIgnoreCase("error")){
			    TextbookTakeoverApplication.disabledialog(context, json.optString("message"));
		    }else{
			  // Toast.makeText(getActivity(), "No More Data Found", Toast.LENGTH_SHORT).show();
			}
		}catch (JSONException e) {
			e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e){
        	e.printStackTrace();
        }
		return HomePageItems;
	}
		
}
