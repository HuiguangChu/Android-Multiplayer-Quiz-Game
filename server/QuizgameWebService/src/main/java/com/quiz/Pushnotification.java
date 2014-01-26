package com.quiz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

public  class Pushnotification {
	public static  ArrayList<String> regIds=new ArrayList<String>();
	final static Executor threadPool = Executors.newFixedThreadPool(5);
	static Sender sender;
	static String status;
	final static int MULTICAST_SIZE = 1000;

	//pushing message to particular user
	public static void send(String messagetitle,String messagecontent){ 
		 sender = new Sender("AIzaSyCRMz2QuyystqFxJg0F5sd_8FxaFOvMfLc");
		List<String> devices = getDevices();
	    if (devices.isEmpty()) {
	    	System.out.println("no devices registered");
	    } else {
	        // send a multicast message using JSON
	        // must split in chunks of 1000 devices (GCM limit)
	    	  int total = devices.size();
	          List<String> partialDevices = new ArrayList<String>(regIds);
	          int counter = 0;
	          int tasks = 0;
	          Message message=new Message.Builder()
	          .collapseKey("1")
	          .addData(messagetitle, messagecontent)
	          .build();
	          MulticastResult result = null;
			try {
				result = sender.send(message, partialDevices,5);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	          for (String device : devices) {
	            counter++;
	            partialDevices.add(device);
	            int partialSize = partialDevices.size();
	            if (partialSize == MULTICAST_SIZE || counter == total) {
	              asyncSend(partialDevices);
	              partialDevices.clear();
	              tasks++;
	            }
	          }
	          status = "Asynchronously sending " + tasks + " multicast messages" +result+ "to" +
	              total + " devices";
	        }
	      }
	    

	 private static void asyncSend(List<String> partialDevices) {
	    // make a copy
	    final List<String> devices = new ArrayList<String>(partialDevices);
	    threadPool.execute(new Runnable() {
	    	Logger logger = Logger.getLogger(getClass().getName());
	      public  void run() {
	        Message message = new Message.Builder().build();
	        MulticastResult multicastResult;
	        try {
	          multicastResult = sender.send(message, devices, 5);
	        } catch (IOException e) {
	          logger.log(Level.SEVERE, "Error posting messages", e);
	          return;
	        }
	        List<Result> results = multicastResult.getResults();
	        // analyze the results
	        for (int i = 0; i < devices.size(); i++) {
	          String regId = devices.get(i);
	          Result result = results.get(i);
	          String messageId = result.getMessageId();
	          if (messageId != null) {
	            logger.fine("Succesfully sent message to device: " + regId +
	                "; messageId = " + messageId);
	          } else {
	            String error = result.getErrorCodeName();
	            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
	              // application has been removed from device - unregister it
	              logger.info("Unregistered device: " + regId);
	            } else {
	              logger.severe("Error sending message to " + regId + ": " + error);
	            }
	          }
	        }
	      }});
	  }
		
	 public static List<String> getDevices() {
		  synchronized (regIds) {
		      return new ArrayList<String>(regIds);
		  }
		
	}


}
