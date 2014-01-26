/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.hig.gsd.quizgame;


import no.hig.gsd.quizgame.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
 static String SENDER_ID="303935519882";
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }
   
    @Override
	   public void onRegistered(Context context, String registrationId) {
	        Log.i(TAG, "Device registered: regId = "+registrationId+"");
	        ServerUtilities.register(context, registrationId);
	    }
	  
	 
	 @Override
	    protected void onUnregistered(Context context, String registrationId) {
	        Log.i(TAG, "Device unregistered");
	        if (GCMRegistrar.isRegisteredOnServer(context)) {
	            ServerUtilities.unregister(context, registrationId);
	        } else {
	            // This callback results from the call to unregister made on
	            // ServerUtilities when the registration to the server failed.
	            Log.i(TAG, "Ignoring unregister callback");
	        }
	    }
     
    @Override
   public void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
       String message = intent.getStringExtra("message");
        // notifies user
        generateNotification(context, message);
      
    }
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
   	private static void generateNotification(Context context, String message) {
           int icon = R.drawable.ic_launcher;
           long when = System.currentTimeMillis();
           NotificationManager notificationManager = (NotificationManager)
                   context.getSystemService(Context.NOTIFICATION_SERVICE);
           Notification notification = new Notification(icon, message, when);
           String title = context.getString(R.string.app_name);
           Intent notificationIntent = new Intent(context,QuestionActivity.class);
           // set intent so it does not start a new activity
           notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                   Intent.FLAG_ACTIVITY_SINGLE_TOP);
           PendingIntent intent =
                   PendingIntent.getActivity(context, 0, notificationIntent, 0);
           notification.setLatestEventInfo(context, title, message, intent);
           notification.flags |= Notification.FLAG_AUTO_CANCEL;
           notificationManager.notify(0, notification);
       }
           
           
       }

