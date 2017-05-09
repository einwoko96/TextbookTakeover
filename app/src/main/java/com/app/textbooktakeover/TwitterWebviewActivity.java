package com.app.textbooktakeover;

import android.app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterWebviewActivity extends Activity {
	private Intent mIntent;
	String call_back = WelcomeActivity.TWITTER_CALLBACK_URL;
	WebView webView;
	String url;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_webview);
		mIntent = getIntent();
		url = (String) mIntent.getExtras().get("URL");
		String newurl=url;
		Log.v("url","twitterweb"+newurl);
		webView = (WebView) findViewById(R.id.webview);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(call_back)) {
					Uri uri = Uri.parse(url);
					String oauthVerifier = uri
							.getQueryParameter("oauth_verifier");
					mIntent.putExtra("oauth_verifier", oauthVerifier);
					setResult(RESULT_OK, mIntent);
					// Intent i =new Intent(TwitterWebviewActivity.this,
					//TwitterPost.class);
					// startActivity(i);
					finish();
					return true;
				}
				return false;
			}
		});
		webView.loadUrl(newurl);
	}
}
