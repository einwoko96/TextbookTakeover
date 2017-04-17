package com.hitasoft.app.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hitasoft.app.textbooktakeover.AddProductDetail;
import com.hitasoft.app.textbooktakeover.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ScannerActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scanBtn, previewBtn, linkBtn, nextBtn;
    private TextView formatTxt, contentTxt;
    private TextView authorText, titleText, descriptionText, dateText, ratingCountText;
    private LinearLayout starLayout;
    private ImageView thumbView, backbtn;
    private TextView title;
    private ImageView[] starViews;
    private Bitmap thumbImg;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner_layout);

        scanBtn = (Button) findViewById(R.id.scan_button);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        previewBtn = (Button) findViewById(R.id.preview_btn);
        linkBtn = (Button) findViewById(R.id.link_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        authorText = (TextView) findViewById(R.id.book_author);
        titleText = (TextView) findViewById(R.id.book_title);
        descriptionText = (TextView) findViewById(R.id.book_description);
        dateText = (TextView) findViewById(R.id.book_date);
        starLayout = (LinearLayout) findViewById(R.id.star_layout);
        ratingCountText = (TextView) findViewById(R.id.book_rating_count);
        thumbView = (ImageView) findViewById(R.id.thumb);

        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        previewBtn.setVisibility(View.GONE);
        linkBtn.setVisibility(View.GONE);
        nextBtn.setVisibility(View.GONE);
        title.setText(getString(R.string.scan));

        previewBtn.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        scanBtn.setOnClickListener(this);
        linkBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);

        starViews = new ImageView[5];
        for (int s = 0; s < starViews.length; s++) {
            starViews[s] = new ImageView(this);
        }

        if (savedInstanceState != null) {
            authorText.setText(savedInstanceState.getString("author"));
            titleText.setText(savedInstanceState.getString("title"));
            descriptionText.setText(savedInstanceState.getString("description"));
            dateText.setText(savedInstanceState.getString("date"));
            ratingCountText.setText(savedInstanceState.getString("ratings"));
            int numStars = savedInstanceState.getInt("stars");//zero if null
            for (int s = 0; s < numStars; s++) {
                starViews[s].setImageResource(R.drawable.star);
                starLayout.addView(starViews[s]);
            }
            starLayout.setTag(numStars);
            thumbImg = (Bitmap) savedInstanceState.getParcelable("thumbPic");
            thumbView.setImageBitmap(thumbImg);
            previewBtn.setTag(savedInstanceState.getString("isbn"));

            if (savedInstanceState.getBoolean("isEmbed")) previewBtn.setEnabled(true);
            else previewBtn.setEnabled(false);
            if (savedInstanceState.getInt("isLink") == View.VISIBLE)
                linkBtn.setVisibility(View.VISIBLE);
            else linkBtn.setVisibility(View.GONE);
            previewBtn.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.scan_button) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            //scan
            scanIntegrator.initiateScan();
        } else if (v.getId() == R.id.backbtn) {
            finish();
        } else if (v.getId() == R.id.link_btn) {
            //get the url tag
            String tag = (String) v.getTag();
            //launch the url
            Intent webIntent = new Intent(Intent.ACTION_VIEW);
            webIntent.setData(Uri.parse(tag));
            startActivity(webIntent);
        } else if (v.getId() == R.id.preview_btn) {
            String tag = (String) v.getTag();
            //launch preview
            Intent intent = new Intent(this, EmbeddedBook.class);
            intent.putExtra("isbn", tag);
            startActivity(intent);
        } else if (v.getId() == R.id.next_btn) {
            String tag = (String) v.getTag();
            //launch preview
            Intent intent = new Intent(this, AddProductDetail.class);
            intent.putExtra("from", "scanner");
            intent.putExtra("isbn", tag);
            intent.putExtra("title", titleText.getText().toString());
            intent.putExtra("author", authorText.getText().toString());
            intent.putExtra("description", descriptionText.getText().toString());
            intent.putExtra("date", dateText.getText().toString());
            intent.putExtra("stars", (Integer) starLayout.getTag());
            intent.putExtra("image", thumbImg);
            startActivity(intent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            //we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            if (scanContent != null && scanFormat != null && scanFormat.equalsIgnoreCase("EAN_13")) {
                content = scanContent;
                //book search
                previewBtn.setTag(scanContent);
                String bookSearchString = "https://www.googleapis.com/books/v1/volumes?" +
                        "q=isbn:" + "9780393913309" + "&key=AIzaSyArIPcFRYG5hKAExwtTrsCNarwniyA77FQ";
                new GetBookInfo().execute(bookSearchString);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Not a valid scan!", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedBundle) {
        savedBundle.putString("title", "" + titleText.getText());
        savedBundle.putString("author", "" + authorText.getText());
        savedBundle.putString("description", "" + descriptionText.getText());
        savedBundle.putString("date", "" + dateText.getText());
        savedBundle.putString("ratings", "" + ratingCountText.getText());
        savedBundle.putParcelable("thumbPic", thumbImg);
        if (starLayout.getTag() != null)
            savedBundle.putInt("stars", Integer.parseInt(starLayout.getTag().toString()));
        savedBundle.putBoolean("isEmbed", previewBtn.isEnabled());
        savedBundle.putInt("isLink", linkBtn.getVisibility());
        if (previewBtn.getTag() != null)
            savedBundle.putString("isbn", previewBtn.getTag().toString());
        super.onSaveInstanceState(savedBundle);
    }

    private class GetBookInfo extends AsyncTask<String, Void, String> {
        //fetch book info

        @Override
        protected String doInBackground(String... bookURLs) {
            //request book info
            StringBuilder bookBuilder = new StringBuilder();
            DataLoader dl = new DataLoader();
            for (String bookSearchURL : bookURLs) {
                //search urls
                HttpClient bookClient = new DefaultHttpClient();
                try {
                    //get the data
                    HttpGet bookGet = new HttpGet(bookSearchURL);
                    HttpResponse bookResponse = dl.secureLoadData(bookSearchURL);
//                    HttpResponse bookResponse = bookClient.execute(bookGet);
                    StatusLine bookSearchStatus = bookResponse.getStatusLine();
                    if (bookSearchStatus.getStatusCode() == 200) {
                        //we have a result
                        HttpEntity bookEntity = bookResponse.getEntity();
                        InputStream bookContent = bookEntity.getContent();
                        InputStreamReader bookInput = new InputStreamReader(bookContent);
                        BufferedReader bookReader = new BufferedReader(bookInput);
                        String lineIn;
                        while ((lineIn = bookReader.readLine()) != null) {
                            bookBuilder.append(lineIn);
                        }
                        return bookBuilder.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        protected void onPostExecute(String result) {
            //parse search results
            try {
                //parse results
                previewBtn.setVisibility(View.VISIBLE);
                nextBtn.setVisibility(View.VISIBLE);
                titleText.setText("set visibility");
                JSONObject resultObject = new JSONObject(result);
                titleText.setText("got JSON");
                JSONArray bookArray = resultObject.getJSONArray("items");
                titleText.setText("got bookArray");
                JSONObject bookObject = bookArray.getJSONObject(0);
                titleText.setText("got bookObject");
                JSONObject volumeObject = bookObject.getJSONObject("volumeInfo");
                titleText.setText("got volumeObject");
                try {
                    titleText.setText("TITLE: " + volumeObject.getString("title"));
                } catch (JSONException jse) {
                    titleText.setText("");
                    jse.printStackTrace();
                }
                StringBuilder authorBuild = new StringBuilder("");
                try {
                    JSONArray authorArray = volumeObject.getJSONArray("authors");
                    for (int a = 0; a < authorArray.length(); a++) {
                        if (a > 0) authorBuild.append(", ");
                        authorBuild.append(authorArray.getString(a));
                    }
                    authorText.setText("AUTHOR(S): " + authorBuild.toString());
                } catch (JSONException jse) {
                    authorText.setText("");
                    jse.printStackTrace();
                }
                try {
                    dateText.setText("PUBLISHED: " + volumeObject.getString("publishedDate"));
                } catch (JSONException jse) {
                    dateText.setText("");
                    jse.printStackTrace();
                }
                try {
                    descriptionText.setText("DESCRIPTION: " + volumeObject.getString("description"));
                } catch (JSONException jse) {
                    descriptionText.setText("");
                    jse.printStackTrace();
                }
                try {
                    //set stars
                    double decNumStars = Double.parseDouble(volumeObject.getString("averageRating"));
                    int numStars = (int) decNumStars;
                    starLayout.setTag(numStars);
                    starLayout.removeAllViews();
                    for (int s = 0; s < numStars; s++) {
                        starViews[s].setImageResource(R.drawable.star);
                        starLayout.addView(starViews[s]);
                    }
                } catch (JSONException jse) {
                    starLayout.removeAllViews();
                    jse.printStackTrace();
                }
                // Begin Stars
                try {
                    ratingCountText.setText(" - " + volumeObject.getString("ratingsCount") + " ratings");
                } catch (JSONException jse) {
                    ratingCountText.setText("");
                    jse.printStackTrace();
                }
                // End Stars
                // Begin Book Preview
                try {
                    boolean isEmbeddable = Boolean.parseBoolean
                            (bookObject.getJSONObject("accessInfo").getString("embeddable"));
                    if (isEmbeddable) previewBtn.setEnabled(true);
                    else previewBtn.setEnabled(false);
                } catch (JSONException jse) {
                    previewBtn.setEnabled(false);
                    jse.printStackTrace();
                }
                // End Book Preview
                // Begin Book URL
                try {
                    linkBtn.setTag(volumeObject.getString("infoLink"));
                    linkBtn.setVisibility(View.VISIBLE);
                } catch (JSONException jse) {
                    linkBtn.setVisibility(View.GONE);
                    jse.printStackTrace();
                }
                // End Book URL
                try {
                    JSONObject imageInfo = volumeObject.getJSONObject("imageLinks");
                    new GetBookThumb().execute(imageInfo.getString("smallThumbnail"));
                } catch (JSONException jse) {
                    thumbView.setImageBitmap(null);
                    jse.printStackTrace();
                }
            } catch (Exception e) {
                //no result
                e.printStackTrace();
                titleText.setText(content + " Not Found on Google Books\n\n" +
                        "Sell your book using the Sell Your Stuff tab");
                authorText.setText("");
                descriptionText.setText("");
                dateText.setText("");
                starLayout.removeAllViews();
                ratingCountText.setText("");
                thumbView.setImageBitmap(null);
                previewBtn.setVisibility(View.GONE);
                nextBtn.setVisibility(View.GONE);
            }
        }
    }

    private class GetBookThumb extends AsyncTask<String, Void, String> {
        //get book thumbnail

        @Override
        protected String doInBackground(String... thumbURLs) {
            //attempt to download image
            try {
                //try to download
                URL thumbURL = new URL(thumbURLs[0]);
                URLConnection thumbConn = thumbURL.openConnection();
                thumbConn.connect();
                InputStream thumbIn = thumbConn.getInputStream();
                BufferedInputStream thumbBuff = new BufferedInputStream(thumbIn);
                thumbImg = BitmapFactory.decodeStream(thumbBuff);
                thumbBuff.close();
                thumbIn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String result) {
            thumbView.setImageBitmap(thumbImg);
        }


    }


    /**
     * Taken from: http://janis.peisenieks.lv/en/76/english-making-an-ssl-connection-via-android/
     */
    public class CustomSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public CustomSSLSocketFactory(KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new CustomX509TrustManager();

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        public CustomSSLSocketFactory(SSLContext context)
                throws KeyManagementException, NoSuchAlgorithmException,
                KeyStoreException, UnrecoverableKeyException {
            super(null);
            sslContext = context;
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port,
                                   boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port,
                    autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }


    public class CustomX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                       String authType) throws CertificateException {

            // Here you can verify the servers certificate. (e.g. against one which is stored on mobile device)

            // InputStream inStream = null;
            // try {
            // inStream = MeaApplication.loadCertAsInputStream();
            // CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // X509Certificate ca = (X509Certificate)
            // cf.generateCertificate(inStream);
            // inStream.close();
            //
            // for (X509Certificate cert : certs) {
            // // Verifing by public key
            // cert.verify(ca.getPublicKey());
            // }
            // } catch (Exception e) {
            // throw new IllegalArgumentException("Untrusted Certificate!");
            // } finally {
            // try {
            // inStream.close();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            // }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    public class DataLoader {

        public HttpResponse secureLoadData(String url)
                throws ClientProtocolException, IOException,
                NoSuchAlgorithmException, KeyManagementException,
                URISyntaxException, KeyStoreException, UnrecoverableKeyException {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new CustomX509TrustManager()},
                    new SecureRandom());

            HttpClient client = new DefaultHttpClient();

            SSLSocketFactory ssf = new CustomSSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            DefaultHttpClient sslClient = new DefaultHttpClient(ccm,
                    client.getParams());

            HttpGet get = new HttpGet(new URI(url));
            HttpResponse response = sslClient.execute(get);

            return response;
        }

    }
}
