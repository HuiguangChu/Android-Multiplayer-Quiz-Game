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
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

static String status="";
    /**
     * Register this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    public static String register(final Context context,final String regId) {
    	
        Log.i("remote", "registering device (regId = " +regId+ ")");
        String usm=LoginActivity.usm;
        String retSrc="";
        try{
			HttpClient client=new DefaultHttpClient();
			HttpPost post=new HttpPost("http://89.250.116.142/Quizgame/jaxrs/quizgame/gcm");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("username", usm ));
			nameValuePairs.add(new BasicNameValuePair("regId", regId ));
		
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
	
			HttpResponse httpResponse = client.execute(post);  	
			HttpEntity entity = httpResponse.getEntity();
			retSrc= EntityUtils.toString(entity);  
			}catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			if(retSrc.equals("success")){
				return status="success";
				}
			else {
				
				return status="failure";
		}
        
}

    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(final Context context,final String regId) {
        
            
        
    }
}
