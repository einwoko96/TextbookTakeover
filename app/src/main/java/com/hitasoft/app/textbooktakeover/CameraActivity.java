package com.hitasoft.app.textbooktakeover;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hitasoft.app.external.HorizontalListView;
import com.hitasoft.app.external.Preview;
import com.hitasoft.app.utils.Constants;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.hitasoft.app.external.Preview.setCameraDisplayOrientation;

public class CameraActivity extends AppCompatActivity implements OnClickListener {

    ImageView cancelBtn, snapBtn, flashBtn, retake;
    TextView gallery, next;
    SurfaceView surfaceView;
    Preview preview;
    Camera camera;
    ImagesAdapter imagesAdapter;
    String from = "", productName, productDes;
    HorizontalListView listView;
    ArrayList<HashMap<String, Object>> temp = new ArrayList<HashMap<String, Object>>();
    public static ArrayList<HashMap<String, Object>> images = new ArrayList<HashMap<String, Object>>();
    public static boolean flash = false, fromedit;
    int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK, previewWidth, previewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_layout);

        cancelBtn = (ImageView) findViewById(R.id.backbtn);
        gallery = (TextView) findViewById(R.id.gallery);
        next = (TextView) findViewById(R.id.next);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        snapBtn = (ImageView) findViewById(R.id.snap);
        listView = (HorizontalListView) findViewById(R.id.listView);
        flashBtn = (ImageView) findViewById(R.id.flashBtn);
        retake = (ImageView) findViewById(R.id.retakeBtn);

        cancelBtn.setVisibility(View.VISIBLE);

        snapBtn.setOnClickListener(this);
        retake.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        flashBtn.setOnClickListener(this);
        gallery.setOnClickListener(this);
        next.setOnClickListener(this);

        //title.setText(getString(R.string.snaptheproduct));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                preview = new Preview(CameraActivity.this, surfaceView);
                preview.setKeepScreenOn(true);

                imagesAdapter = new ImagesAdapter(CameraActivity.this, temp);
                listView.setAdapter(imagesAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (temp.size() > 0) {
                            final HashMap<String, Object> tempMap = temp.get(position);
                            if (images.contains(tempMap)) {
                                images.remove(tempMap);
                            } else {
                                images.add(tempMap);
                            }
                            imagesAdapter.notifyDataSetChanged();
                        }
                    }
                });

                if (flash) {
                    flashBtn.setSelected(true);
                    flashBtn.setColorFilter(getResources().getColor(R.color.colorPrimary));
                } else {
                    flashBtn.setSelected(false);
                    flashBtn.setColorFilter(null);
                }
            }
        });

        try {
            Bundle i = getIntent().getExtras();
            from = i.getString("from");
            if (from.equals("AddProduct")) {
                productDes = i.getString("productDes");
                productName = i.getString("productName");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        TextbookTakeoverApplication.registerReceiver(CameraActivity.this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int numCams = Camera.getNumberOfCameras();
                if (numCams > 0) {
                    try {
                        if (ContextCompat.checkSelfPermission(CameraActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(CameraActivity.this, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                        } else if (ContextCompat.checkSelfPermission(CameraActivity.this, CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{CAMERA}, 101);
                        } else if (ContextCompat.checkSelfPermission(CameraActivity.this, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
                        } else {
                            camera = Camera.open(0);
                            preview.setCamera(camera, flash);
                            camera.startPreview();
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("requestCode", "requestCode=" + requestCode);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CameraActivity.this, getString(R.string.storage_camera_permisssion), Toast.LENGTH_SHORT).show();
                    finish();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CameraActivity.this, getString(R.string.camera_permission_access), Toast.LENGTH_SHORT).show();
                    finish();
                } else if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CameraActivity.this, getString(R.string.storage_permission_access), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CameraActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CameraActivity.this, getString(R.string.camera_permission_access), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CameraActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case 102:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CameraActivity.this, getString(R.string.storage_permission_access), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CameraActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    camera.stopPreview();
                    preview.setCamera(null, flash);
                    camera.release();
                    camera = null;
                }
            }
        });
    }

    private void resetCam() {
        preview.setCamera(camera, flash);
        camera.startPreview();
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d("onShutter'd", "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("onPictureTaken", "onPictureTaken - raw");
        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask().execute(data);
            resetCam();
            Log.d("onPictureTaken", "onPictureTaken - jpeg");
        }
    };

    /**
     * Save the captured image to gallery
     **/
    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        Bitmap bitmapImage;

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;
            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + "/" + getString(R.string.app_name));
                dir.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);

                //     boolean bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Bitmap realImage = decodeFile(outFile.getAbsolutePath());

                int angleToRotate = getRoatationAngle(CameraActivity.this, 0);

                if (currentCameraId != 0) {
                    Matrix matrix = new Matrix();
                    float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                    Matrix matrixMirrorY = new Matrix();
                    matrixMirrorY.setValues(mirrorY);

                    matrix.postConcat(matrixMirrorY);
                    matrix.postRotate(90);
                    bitmapImage = Bitmap.createBitmap(realImage, 0, 0, realImage.getWidth(), realImage.getHeight(), matrix, true);
                } else {
                    bitmapImage = rotate(realImage, angleToRotate);
                }

                previewWidth = bitmapImage.getWidth();
                previewHeight = bitmapImage.getHeight() * 75 / 100;
                Log.v("previewWidth&Height", "=" + previewWidth + "&" + previewHeight);
                bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, previewWidth, previewHeight);

                refreshGallery(outFile);

                File file = new File(dir, fileName);
                if (file.exists()) file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("onPictureTaken", "onPictureTaken" + outFile.getAbsolutePath());

                refreshGallery(file);

                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("type", "path");
                map.put("image", bitmapImage);
                map.put("path", file.getAbsolutePath());
                temp.add(map);

                //		Log.v("thumbnail",""+thumbnail);

                Log.v("imagesAry", "" + temp);
                Log.d("onPictureTaken", "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            snapBtn.setOnClickListener(CameraActivity.this);
            imagesAdapter.notifyDataSetChanged();
        }

    }

    /**
     * for rotating the captured image to correct angle
     **/
    public static int getRoatationAngle(Activity mContext, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


    private Bitmap decodeFile(String fPath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inDither = false;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        BitmapFactory.decodeFile(fPath, opts);
        final int REQUIRED_SIZE = 1024;
        int scale = 1;

        if (opts.outHeight > REQUIRED_SIZE || opts.outWidth > REQUIRED_SIZE) {
            final int heightRatio = Math.round((float) opts.outHeight
                    / (float) REQUIRED_SIZE);
            final int widthRatio = Math.round((float) opts.outWidth
                    / (float) REQUIRED_SIZE);
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;//
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;
        Bitmap bm = BitmapFactory.decodeFile(fPath, opts).copy(
                Bitmap.Config.RGB_565, false);
        return bm;
    }

    public Bitmap rotate(Bitmap src, float degree) {
        // create new matrix object
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);

        // return new bitmap rotated using matrix
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    // for adding muliple images //
    public class ImagesAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> imgAry;
        private Context mContext;

        public ImagesAdapter(Context ctx, ArrayList<HashMap<String, Object>> data) {
            mContext = ctx;
            imgAry = data;
        }

        @Override
        public int getCount() {
            return imgAry.size();
        }

        @Override
        public Object getItem(int position) {

            return imgAry.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.singleimage, parent, false);//layout
            } else {
                view = convertView;
                view.forceLayout();
            }
            Log.v("position", "" + position);
            try {
                final ImageView singleImage = (ImageView) view.findViewById(R.id.imageView);
                final ImageView gradient = (ImageView) view.findViewById(R.id.imageView2);
                final ImageView tick = (ImageView) view.findViewById(R.id.tick);
                singleImage.setVisibility(View.VISIBLE);

                //   gradient.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.camera_image_selector));
                final HashMap<String, Object> tempMap = imgAry.get(position);

                if (images.contains(tempMap)) {
                    gradient.setVisibility(View.VISIBLE);
                    tick.setVisibility(View.VISIBLE);
                } else {
                    gradient.setVisibility(View.GONE);
                    tick.setVisibility(View.GONE);
                }

                if (tempMap.get("type").equals("path")) {
                    singleImage.setImageBitmap(TextbookTakeoverApplication.getResizedBitmap((Bitmap) tempMap.get("image"), 70));
                } else {
                    Picasso.with(CameraActivity.this).load(tempMap.get(Constants.TAG_ITEM_URL_350).toString()).into(singleImage);
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return view;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Log.v("RESULT_OK", "");
            if (requestCode == 2) {
                try {
                    Log.v("gallery code opened", "");
                    Uri selectedImage = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage,
                            filePath, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    String picturePath = c.getString(columnIndex);
                    Log.v("path of gallery", picturePath + "");
                    c.close();
                    Bitmap thumbnail = decodeFile(picturePath);
                    Log.v("gallery code bitmap", "" + thumbnail);

                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("type", "path");
                    map.put("image", thumbnail);
                    map.put("path", picturePath);
                    temp.add(map);

                    imagesAdapter.notifyDataSetChanged();

                    //	new ImageUploadTask().execute(picturePath);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError ome) {
                    ome.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        TextbookTakeoverApplication.unregisterReceiver(CameraActivity.this);
    }

    @Override
    public void onBackPressed() {
        if (fromedit) {
            fromedit = false;
            finish();
            Intent i = new Intent(CameraActivity.this, AddProductDetail.class);
            i.putExtra("from", from);
            if (productDes != null && productName != null) {
                i.putExtra("productDes", productDes);
                i.putExtra("productName", productName);
            }
            if (from.equals("edit")) {
                i.putExtra("data", AddProductDetail.itemMap);
            }
            startActivity(i);
        } else {
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.snap:
                snapBtn.setOnClickListener(null);
                try {
                    camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.next:
                if (temp.size() == 0) {
                    Toast.makeText(CameraActivity.this, getString(R.string.please_add_image), Toast.LENGTH_SHORT).show();
                } else if (temp.size() == 1) {
                    if (images.size() == 0) {
                        images.addAll(temp);
                    }
                    fromedit = false;
                    finish();
                    Intent i = new Intent(CameraActivity.this, AddProductDetail.class);
                    i.putExtra("from", from);
                    if (from.equals("edit")) {
                        i.putExtra("data", AddProductDetail.itemMap);
                    }
                    if (productDes != null && productName != null) {
                        i.putExtra("productDes", productDes);
                        i.putExtra("productName", productName);
                    }
                    startActivity(i);
                } else if (images.size() == 0) {
                    Toast.makeText(CameraActivity.this, getString(R.string.please_select_images), Toast.LENGTH_SHORT).show();
                } else {
                    fromedit = false;
                    finish();
                    Intent i = new Intent(CameraActivity.this, AddProductDetail.class);
                    i.putExtra("from", from);
                    if (from.equals("edit")) {
                        i.putExtra("data", AddProductDetail.itemMap);
                    }
                    if (productDes != null && productName != null) {
                        i.putExtra("productDes", productDes);
                        i.putExtra("productName", productName);
                    }
                    startActivity(i);
                }
                break;

            case R.id.backbtn:
                if (fromedit) {
                    fromedit = false;
                    finish();
                    Intent i = new Intent(CameraActivity.this, AddProductDetail.class);
                    i.putExtra("from", from);
                    if (from.equals("edit")) {
                        i.putExtra("data", AddProductDetail.itemMap);
                    }
                    if (productDes != null && productName != null) {
                        i.putExtra("productDes", productDes);
                        i.putExtra("productName", productName);
                    }
                    startActivity(i);
                } else {
                    finish();
                }
                break;
            case R.id.flashBtn:
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    if (flashBtn.isSelected()) {
                        flashBtn.setSelected(false);
                        flashBtn.setColorFilter(null);
                        flash = false;
                    } else {
                        flashBtn.setSelected(true);
                        flashBtn.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        flash = true;
                    }
                    resetCam();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.your_device_doesnt_flash), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.gallery:
                Intent in = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(in, 2);
                break;
            case R.id.retakeBtn:
                //NB: if you don't release the current camera before switching, you app will crash
                if (camera != null) {
                    camera.stopPreview();
                    preview.setCamera(null, flash);
                    camera.release();
                    camera = null;
                }
                //swap the id of the camera to be used
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                camera = Camera.open(currentCameraId);
                setCameraDisplayOrientation(CameraActivity.this, currentCameraId, camera);
                try {
                    camera.setPreviewDisplay(preview.getSurfaceHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();

                break;
        }

    }
}
