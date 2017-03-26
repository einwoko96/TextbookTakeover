package com.hitasoft.app.external;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Hashtable;

/**
 * Created by hitasoft on 30/6/15.
 */


public class CustomTextView extends TextView {

    public CustomTextView(Context context) {
        super(context);
    }
    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        CustomFontHelper.setCustomFont(this, context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        CustomFontHelper.setCustomFont(this, context, attrs);
    }

}
