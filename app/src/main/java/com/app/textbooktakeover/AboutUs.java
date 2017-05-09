package com.app.textbooktakeover;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.textbooktakeover.R;

/**
 * Created by hitasoft on 10/6/16.
 **/
public class AboutUs extends AppCompatActivity implements View.OnClickListener{

    ImageView backbtn;
    TextView title, content;
    String pageTitle="", pageContent="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutus);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView)findViewById(R.id.title);
        content = (TextView)findViewById(R.id.content);

        backbtn.setOnClickListener(this);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);

        pageTitle = (String) getIntent().getExtras().get("title");
        pageContent = (String) getIntent().getExtras().get("content");

        title.setText(pageTitle);
        content.setText(TextbookTakeoverApplication.stripHtml(pageContent));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(AboutUs.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(AboutUs.this);
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
