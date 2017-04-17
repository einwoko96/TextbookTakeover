package com.hitasoft.app.textbooktakeover;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.hitasoft.app.utils.Constants;

import java.util.Random;

public class GCMIntentService extends GCMBaseIntentService {
 
    private static final String TAG = "GCMIntentService";
     
    private TextbookTakeoverApplication aController = null;
    
    
 
    public GCMIntentService() {
        // Call extended class Constructor GCMBaseIntentService
        super(Constants.SENDER_ID);
    }
 
    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
         
        //Get Global Controller Class object (see application tag in AndroidManifest.xml)
        if(aController == null)
           aController = (TextbookTakeoverApplication) getApplicationContext();
        Constants.REGISTER_ID = registrationId;
        Log.i(TAG, "Device registered: regId = " + registrationId);
      /*  aController.displayMessageOnScreen(context, 
                                           "Your device registred with GCM");*/
       // Log.d("NAME", MainActivity.name);
        aController.register(context);
    }
 
    /**
     * Method called on device unregistred
     * */
   
	@Override
    protected void onUnregistered(Context context, String registrationId) {
        if(aController == null)
            aController = (TextbookTakeoverApplication) getApplicationContext();
        Log.i(TAG, "Device unregistered");
       /* aController.displayMessageOnScreen(context, 
                                            getString(R.string.gcm_unregistered));*/
        aController.unregister(context);
    }
 
    /**
     * Method called on Receiving a new message from GCM server
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
         
        if(aController == null)
            aController = (TextbookTakeoverApplication) getApplicationContext();
         
        Log.v(TAG, "Received message" + intent.getExtras().toString());
        String message = intent.getExtras().getString("price");
        String type = intent.getExtras().getString("type");
         
      //  aController.displayMessageOnScreen(context, message);
        // notifies user
        generateNotification(context, message, type);
    }
 
    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
         
        if(aController == null)
            aController = (TextbookTakeoverApplication) getApplicationContext();
         
        Log.i(TAG, "Received deleted messages notification");
      /*  String message = getString(R.string.gcm_deleted, total);
        aController.displayMessageOnScreen(context, message);*/
        // notifies user
       // generateNotification(context, message);
    }
 
    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
         
        if(aController == null)
            aController = (TextbookTakeoverApplication) getApplicationContext();
         
        Log.i(TAG, "Received error: " + errorId);
       /* aController.displayMessageOnScreen(context, 
                                   getString(R.string.gcm_error, errorId));*/
    }
 
    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
         
        if(aController == null)
            aController = (TextbookTakeoverApplication) getApplicationContext();
         
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
      /*  aController.displayMessageOnScreen(context, 
                        getString(R.string.gcm_recoverable_error,
                        errorId));*/
        return super.onRecoverableError(context, errorId);
    }
 
    /**
     * Create a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
	private  void generateNotification(Context context, String message, String type) {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
     
        int icon = R.drawable.appicon;
        long when = System.currentTimeMillis();
        String title = context.getString(R.string.app_name);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.appicon);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);

        Intent notificationIntent = null;
        boolean stopNotification = false;
        if (type == null || type.equals("notification")){
            notificationIntent = new Intent(context, com.hitasoft.app.textbooktakeover.Notification.class);
        } else {
            String[] msg = message.split(":");
            Log.v("fullname", "fullname="+msg[0]);
            if (ChatActivity.fullName.equals(msg[0].trim())){
                stopNotification = true;
            } else {
                notificationIntent = new Intent(context, MessageActivity.class);
            }
        }

        if (!stopNotification){
            // set intent so it does not start a new activity
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent =
                    PendingIntent.getActivity(context, uniqueInt, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

            Notification notification = new NotificationCompat.Builder(context)
                    .setLargeIcon(bitmap)
                    .setSmallIcon(R.drawable.notifyicon)
                    //.setColor()
                    .setContentTitle(title)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setWhen(when)
                    .setContentText(message)
                    .setContentIntent(intent)
                    .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            // Play default notification sound
            notification.defaults |= Notification.DEFAULT_SOUND;

            // Vibrate if vibrate is enabled
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            notificationManager.notify(m, notification);
        }
    }

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
	}

	
}

