package com.app.textbooktakeover;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.external.GPSTracker;
import com.app.external.PlacesAutoCompleteAdapter;
import com.app.utils.Constants;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by hitasoft on 29/3/16.
 */
public class LocationActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, TextWatcher {

    MapView mapView;
    GoogleMap map;
    TextView title, setLoc, remLoc;
    ImageView backbtn, crossIcon, myLocation;
    AlertDialog alertDialog;
    int screenHeight, screenWidth;
    public static boolean got = false, locationRemoved;
    private LatLng center;
    private Geocoder geocoder;
    private List<Address> addresses;
    public static double lat, lon;
    AutoCompleteTextView address;
    public static String location = "World Wide";
    GPSTracker gps;
    String from = "";
    InputMethodManager imm;
    RelativeLayout searchLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(getResources().getConfiguration().locale);
        setContentView(R.layout.location_activity);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        setLoc = (TextView) findViewById(R.id.apply);
        remLoc = (TextView) findViewById(R.id.reset);
        address = (AutoCompleteTextView) findViewById(R.id.address);
        crossIcon = (ImageView) findViewById(R.id.cross_icon);
        myLocation = (ImageView) findViewById(R.id.my_location);
        searchLay = (RelativeLayout) findViewById(R.id.searchLay);

        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        int weight = display.getWidth();
        screenHeight = height * 60 / 100;
        screenWidth = weight * 80 / 100;

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Constants.filpref = getApplicationContext().getSharedPreferences("FilterPref",
                MODE_PRIVATE);
        Constants.fileditor = Constants.filpref.edit();

        alertDialog = new AlertDialog.Builder(LocationActivity.this).create();
        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.gps_settings));
        // Setting Dialog Message
        alertDialog.setMessage(getString(R.string.gps_notenabled));
        // On pressing Settings button
        alertDialog.setButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setButton2(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setCancelable(false);

        from = (String) getIntent().getExtras().get("from");

        loadData();

        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        crossIcon.setVisibility(View.GONE);

        title.setText(getString(R.string.location));
        backbtn.setOnClickListener(this);
        setLoc.setOnClickListener(this);
        remLoc.setOnClickListener(this);
        crossIcon.setOnClickListener(this);
        address.setOnItemClickListener(this);
        myLocation.setOnClickListener(this);
        address.addTextChangedListener(this);

        address.setAdapter(new PlacesAutoCompleteAdapter(LocationActivity.this, R.layout.dropdown_layout));

        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        Log.v("map", "map==" + map);
        if (map != null) {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                map.setMyLocationEnabled(true);
            }
        }

        /*View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        locationButton.setBackgroundResource(R.drawable.my_location);
        // and next place it, for exemple, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);*/

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),
                16));
        MarkerOptions a = new MarkerOptions()
                .position(new LatLng(latitude,longitude));
        map.addMarker(a);*/

        // Updates the location and zoom of the MapView
        if (from.equals("home")) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15);
            map.animateCamera(cameraUpdate);
        } else if (from.equals("add")) {
            if (AddProductDetail.lat == 0 && AddProductDetail.lon == 0) {
                if (gps.canGetLocation()) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 15);
                    map.animateCamera(cameraUpdate);
                }
            } else {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(AddProductDetail.lat, AddProductDetail.lon), 15);
                map.animateCamera(cameraUpdate);
            }
        }

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {
                // TODO Auto-generated method stub
                center = map.getCameraPosition().target;
                Log.v("Center", "lattitude=" + center.latitude + " &longitude=" + center.longitude);
                map.clear();
            }
        });

        address.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    try {
                        imm.hideSoftInputFromWindow(address.getWindowToken(), 0);
                        Double latn[] = new Double[2];
                        if (address.getText().toString().trim().length() != 0) {
                            latn = new getLocationFromString().execute(address.getText().toString().trim()).get();
                            double lat = latn[0];
                            double lon = latn[1];

                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15);
                            map.animateCamera(cameraUpdate);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                }
                return false;
            }
        });

        ViewTreeObserver observer = searchLay.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.v("searchLay.getWidth()", "searchLay.getWidth()==" + searchLay.getWidth());
                if (searchLay.getWidth() > 0) {
                    address.setDropDownWidth(searchLay.getWidth());
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        searchLay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        searchLay.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });
    }

    /**
     * for get the lat, lon from gps
     **/
    private void loadData() {
        gps = new GPSTracker(LocationActivity.this);
        if (from.equals("home")) {
            if (lat == 0 && lon == 0) {
                if (gps.canGetLocation()) {
                    if (TextbookTakeoverApplication.isNetworkAvailable(LocationActivity.this)) {
                        lat = gps.getLatitude();
                        lon = gps.getLongitude();
                        Log.v("lati", "lat" + lat);
                        Log.v("longi", "longi" + lon);
                    } else {
                        // TextbookTakeoverApplication.dialog(LocationActivity.this, "Error!", "Please check your connection and try again");
                    }
                } else {
                    if (!alertDialog.isShowing()) {
                        alertDialog.show();
                    }
                    got = true;
                }
            }
        } else if (from.equals("add")) {
            if (AddProductDetail.lat == 0 && AddProductDetail.lon == 0) {
                if (gps.canGetLocation()) {
                    if (TextbookTakeoverApplication.isNetworkAvailable(LocationActivity.this)) {
                        Log.v("lati", "lat" + AddProductDetail.lat);
                        Log.v("longi", "longi" + AddProductDetail.lat);
                    } else {
                        //  TextbookTakeoverApplication.dialog(LocationActivity.this, "Error!", "Please check your connection and try again");
                    }
                } else {
                    if (!alertDialog.isShowing()) {
                        alertDialog.show();
                    }
                    got = true;
                }
            }
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            crossIcon.setVisibility(View.VISIBLE);
        } else {
            crossIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * for get the address from lat, lon
     **/
    private class GetLocationAsync extends AsyncTask<String, Void, String> {

        // boolean duplicateResponse;
        double x, y;
        StringBuilder str;

        public GetLocationAsync(double latitude, double longitude) {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                geocoder = new Geocoder(LocationActivity.this, getResources().getConfiguration().locale);
                addresses = geocoder.getFromLocation(x, y, 1);
                str = new StringBuilder();
                if (geocoder.isPresent() && addresses.size() > 0) {

                    Address returnAddress = addresses.get(0);

                    String localityString = returnAddress.getLocality();
                    String city = returnAddress.getCountryName();
                    String region_code = returnAddress.getCountryCode();
                    String zipcode = returnAddress.getPostalCode();

                    str.append(localityString + "");
                    str.append(city + "" + region_code + "");
                    str.append(zipcode + "");

                } else {
                }
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (addresses != null && !addresses.isEmpty()) {
                    if (from.equals("home")) {
                        location = addresses.get(0).getAddressLine(0) + ", "
                                + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                        if (FragmentMainActivity.locationTxt != null) {
                            FragmentMainActivity.locationTxt.setText(location);
                        }
                        Constants.fileditor.putString("location", location);
                        Constants.fileditor.commit();
                    } else if (from.equals("add")) {
                        AddProductDetail.loc = addresses.get(0).getAddressLine(0) + ", "
                                + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                        if (AddProductDetail.location != null) {
                            AddProductDetail.location.setText(AddProductDetail.loc);
                            AddProductDetail.location.setTextColor(getResources().getColor(R.color.primaryText));
                            AddProductDetail.locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    /**
     * for get the lat, lon from address
     **/
    class getLocationFromString extends AsyncTask<String, Void, Double[]> {

        @Override
        protected Double[] doInBackground(String... params) {
            final Double latn[] = new Double[2];
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder("http://maps.google.com/maps/api/geocode/json");
                sb.append("?address=" + URLEncoder.encode(params[0], "utf8"));
                sb.append("&ka&sensor=false");
                sb.append("&language=" + getResources().getConfiguration().locale.getLanguage());
                URL url = new URL(sb.toString());

                Log.v("MAP URL", "" + url);

                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                Log.e("Error", "Error processing Places API URL", e);
                return latn;
            } catch (IOException e) {
                Log.e("Error", "Error connecting to Places API", e);
                return latn;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                JSONObject jsonObject = new JSONObject(jsonResults.toString());
                Log.v("jsonObject", "jsonObject=" + jsonObject);
                latn[1] = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");
                latn[0] = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");
                Log.v("lat & lon", " lat = " + latn[0] + " &lon = " + latn[1]);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return latn;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        crossIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(LocationActivity.this);

        mapView.onResume();
        gps = new GPSTracker(LocationActivity.this);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(LocationActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.reset:
                if (from.equals("home")) {
                    lat = 0;
                    lon = 0;
                    location = getString(R.string.world_wide);
                    locationRemoved = true;
                    Constants.fileditor.putString("location", location);
                    Constants.fileditor.putString("lat", String.valueOf(lat));
                    Constants.fileditor.putString("lon", String.valueOf(lon));
                    Constants.fileditor.putBoolean("locationRemoved", locationRemoved);
                    Constants.fileditor.commit();

                    FragmentMainActivity.currentPage = 0;
                    FragmentMainActivity.HomeItems.clear();
                    if (FragmentMainActivity.homeAdapter != null) {
                        FragmentMainActivity.homeAdapter.notifyDataSetChanged();
                    }
                    finish();
                    Intent i = new Intent(LocationActivity.this, FragmentMainActivity.class);
                    startActivity(i);
                } else if (from.equals("add")) {
                    AddProductDetail.lat = 0;
                    AddProductDetail.lon = 0;
                    AddProductDetail.loc = "";
                    if (AddProductDetail.location != null) {
                        AddProductDetail.location.setText(getString(R.string.world_wide));
                        AddProductDetail.location.setTextColor(getResources().getColor(R.color.secondaryText));
                        AddProductDetail.locArrow.setColorFilter(getResources().getColor(R.color.secondaryText));
                    }
                    finish();
                }

                break;
            case R.id.apply:
                if (from.equals("home")) {
                    try {
                        if (address.getText().toString().trim().length() == 0) {
                            new GetLocationAsync(center.latitude, center.longitude).execute().get();
                            lat = center.latitude;
                            lon = center.longitude;
                            locationRemoved = false;
                            Constants.fileditor.putString("lat", String.valueOf(lat));
                            Constants.fileditor.putString("lon", String.valueOf(lon));
                            Constants.fileditor.putBoolean("locationRemoved", locationRemoved);
                            Constants.fileditor.commit();
                        } else {
                            Double latn[] = new Double[2];
                            latn = new getLocationFromString().execute(address.getText().toString().trim()).get();
                            lat = latn[0];
                            lon = latn[1];
                            location = address.getText().toString().trim();
                            locationRemoved = false;
                            Constants.fileditor.putString("location", location);
                            Constants.fileditor.putString("lat", String.valueOf(lat));
                            Constants.fileditor.putString("lon", String.valueOf(lon));
                            Constants.fileditor.putBoolean("locationRemoved", locationRemoved);
                            Constants.fileditor.commit();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    FragmentMainActivity.currentPage = 0;
                    FragmentMainActivity.HomeItems.clear();
                    if (FragmentMainActivity.homeAdapter != null) {
                        FragmentMainActivity.homeAdapter.notifyDataSetChanged();
                    }
                    finish();
                    Intent j = new Intent(LocationActivity.this, FragmentMainActivity.class);
                    startActivity(j);
                } else {
                    try {
                        if (address.getText().toString().trim().length() == 0) {
                            new GetLocationAsync(center.latitude, center.longitude).execute().get();
                            AddProductDetail.lat = center.latitude;
                            AddProductDetail.lon = center.longitude;
                        } else {
                            Double latn[] = new Double[2];
                            latn = new getLocationFromString().execute(address.getText().toString().trim()).get();
                            AddProductDetail.lat = latn[0];
                            AddProductDetail.lon = latn[1];
                            AddProductDetail.loc = address.getText().toString().trim();
                            if (AddProductDetail.location != null) {
                                AddProductDetail.location.setText(AddProductDetail.loc);
                                AddProductDetail.location.setTextColor(getResources().getColor(R.color.primaryText));
                                AddProductDetail.locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }
                break;
            case R.id.cross_icon:
                address.setText("");
                crossIcon.setVisibility(View.GONE);
                break;
            case R.id.my_location:
                if (gps.canGetLocation()) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new
                            LatLng(gps.getLatitude(), gps.getLongitude()), 15));
                } else {
                    if (!alertDialog.isShowing()) {
                        alertDialog.show();
                    }
                    got = true;
                }
                break;
        }
    }
}
