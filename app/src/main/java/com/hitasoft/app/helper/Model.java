package com.hitasoft.app.helper;

import android.content.Context;

import com.hitasoft.app.joysale.R;
import com.hitasoft.app.utils.Constants;

import java.util.ArrayList;

public class Model {

	public static ArrayList<Item> Items;

	public static void LoadModel(Context ctx) {

		Items = new ArrayList<Item>();

		Items.add(new Item(1, R.drawable.s_cam, ctx.getString(R.string.sell_your_stuff)));
		Items.add(new Item(2, R.drawable.s_message, ctx.getString(R.string.chat)));
		Items.add(new Item(3, R.drawable.s_category, ctx.getString(R.string.categories)));
		Items.add(new Item(4, R.drawable.s_user, ctx.getString(R.string.myprofile)));
		Items.add(new Item(5, R.drawable.s_user, ctx.getString(R.string.scan)));
		if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION){
			Items.add(new Item(6, R.drawable.s_mysale, ctx.getString(R.string.myorders_sales)));
			Items.add(new Item(7, R.drawable.s_exchange, ctx.getString(R.string.myexchange)));
			Items.add(new Item(8, R.drawable.s_promotion, ctx.getString(R.string.my_promotions)));
			Items.add(new Item(9, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
			Items.add(new Item(10, R.drawable.s_help, ctx.getString(R.string.help)));
		} else if (Constants.BUYNOW && Constants.EXCHANGE) {
			Items.add(new Item(6, R.drawable.s_mysale, ctx.getString(R.string.myorders_sales)));
			Items.add(new Item(7, R.drawable.s_exchange, ctx.getString(R.string.myexchange)));
			Items.add(new Item(8, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
			Items.add(new Item(9, R.drawable.s_help, ctx.getString(R.string.help)));
		} else if (Constants.BUYNOW && Constants.PROMOTION) {
			Items.add(new Item(6, R.drawable.s_mysale, ctx.getString(R.string.myorders_sales)));
			Items.add(new Item(7, R.drawable.s_promotion, ctx.getString(R.string.my_promotions)));
			Items.add(new Item(8, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
			Items.add(new Item(9, R.drawable.s_help, ctx.getString(R.string.help)));
		} else if (Constants.EXCHANGE && Constants.PROMOTION) {
			Items.add(new Item(6, R.drawable.s_exchange, ctx.getString(R.string.myexchange)));
			Items.add(new Item(7, R.drawable.s_promotion, ctx.getString(R.string.my_promotions)));
			Items.add(new Item(8, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
			Items.add(new Item(9, R.drawable.s_help, ctx.getString(R.string.help)));
		} else if (Constants.BUYNOW) {
			Items.add(new Item(6, R.drawable.s_mysale, ctx.getString(R.string.myorders_sales)));
			Items.add(new Item(7, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
			Items.add(new Item(8, R.drawable.s_help, ctx.getString(R.string.help)));
		} else if (Constants.EXCHANGE) {
			Items.add(new Item(6, R.drawable.s_exchange, ctx.getString(R.string.myexchange)));
			Items.add(new Item(7, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
			Items.add(new Item(8, R.drawable.s_help, ctx.getString(R.string.help)));
		} else if (Constants.PROMOTION) {
			Items.add(new Item(6, R.drawable.s_promotion, ctx.getString(R.string.my_promotions)));
			Items.add(new Item(7, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
			Items.add(new Item(8, R.drawable.s_help, ctx.getString(R.string.help)));
		} else {
			Items.add(new Item(6, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
			Items.add(new Item(7, R.drawable.s_help, ctx.getString(R.string.help)));
		}
	}

	public static Item GetbyId(int id) {

		for (Item item : Items) {
			if (item.Id == id) {
				return item;
			}
		}

		return null;
	}
}