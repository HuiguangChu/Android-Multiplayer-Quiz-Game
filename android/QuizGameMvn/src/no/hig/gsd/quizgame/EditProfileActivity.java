package no.hig.gsd.quizgame;

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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditProfileActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);
		
		setupUI();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_profile, menu);
		return true;
	}
	
	
	private void setupUI() {
		Button button = (Button) findViewById(R.id.save_profile_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveProfile();
			}
		});
	}
	
	
	private void saveProfile() {
		EditText editText = (EditText) findViewById(R.id.username_edit);
		String username = editText.getText().toString();
		if (! username.isEmpty()) {
			sendNewUsername(username);
		}
		
		editText = (EditText) findViewById(R.id.oldpass_edit);
		String oldPass = editText.getText().toString();
		editText = (EditText) findViewById(R.id.newpass_edit);
		String newPass = editText.getText().toString();
		editText = (EditText) findViewById(R.id.confirm_newpass_edit);
		String newPassConfirm = editText.getText().toString();
		if (newPass.equals(newPassConfirm)) {
			sendNewPassword(newPassConfirm, oldPass);
		}
		
		editText = (EditText) findViewById(R.id.email_edit);
		String email = editText.getText().toString();
		if (! email.isEmpty()) {
			sendNewEmail(email);
		}
	}
	
	
	private void sendNewUsername(String newUsername) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://89.250.116.142/Quizgame/jaxrs/quizgame/??");   
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    		nameValuePairs.add(new BasicNameValuePair("username", ""));
    		nameValuePairs.add(new BasicNameValuePair("new_username", newUsername));
    		
    		post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
              
    		HttpResponse response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();
			String retSrc = EntityUtils.toString(entity);
			if (retSrc.equals("repeat")){
				
			}
			else if(retSrc.equals("success")){
				 
			} else {
				//new_textview.setText("Registration fails");
			}
			 
		} catch (ClientProtocolException e) {
			System.out.println("Please check your provided http address!");
			e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	private void sendNewPassword(String newPass, String oldPass) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://89.250.116.142/Quizgame/jaxrs/quizgame/??");   
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    		nameValuePairs.add(new BasicNameValuePair("username", ""));
    		nameValuePairs.add(new BasicNameValuePair("old_password", oldPass));
    		nameValuePairs.add(new BasicNameValuePair("new_password", newPass));
    		
    		post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
              
    		HttpResponse response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();
			String retSrc = EntityUtils.toString(entity);
			if (retSrc.equals("repeat")){
				
			}
			else if(retSrc.equals("success")){
				 
			} else {
				//new_textview.setText("Registration fails");
			}
			 
		} catch (ClientProtocolException e) {
			System.out.println("Please check your provided http address!");
			e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
	private void sendNewEmail(String newEmail) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://89.250.116.142/Quizgame/jaxrs/quizgame/??");   
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    		nameValuePairs.add(new BasicNameValuePair("username", ""));
    		nameValuePairs.add(new BasicNameValuePair("new_email", newEmail));
    		
    		post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
              
    		HttpResponse response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();
			String retSrc = EntityUtils.toString(entity);
			if (retSrc.equals("repeat")){
				
			}
			else if(retSrc.equals("success")){
				 
			} else {
				//new_textview.setText("Registration fails");
			}
			 
		} catch (ClientProtocolException e) {
			System.out.println("Please check your provided http address!");
			e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
