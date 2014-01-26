package no.hig.gsd.quizgame;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import no.hig.gsd.quizgame.R;

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

import com.google.android.gcm.GCMRegistrar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
public class LoginActivity extends Activity {
	static String SENDER_ID="303935519882";
	 static String usm = null;
	 EditText username;
	 private DAO dao;
	 private GameData data;
String status="";
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()     
        		.detectDiskReads()     
        		.detectDiskWrites()     
        		.detectNetwork()   // or .detectAll() for all detectable problems     
        		.penaltyLog()     
        		.build());     
		//set up the virtual machine policy
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()     
                .detectLeakedSqlLiteObjects()     
                //.detectLeakedClosableObjects()     
                .penaltyLog()     
                .penaltyDeath()     
                .build());  
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		dao = DAO.getInstance(this);
		dao.drop();
		data = GameData.getInstance(this);
		Button btn_login = (Button) findViewById(R.id.btn_login);
		//CheckBox  select =(CheckBox) findViewById(R.id.select);
		TextView forgetpsw =(TextView) findViewById(R.id.forgetpassword);
		Button btn_newuser=(Button)findViewById(R.id.newsuer);	
		username = (EditText) findViewById(R.id.username);	
		
		loadSavedPreferences();
		
		btn_newuser.setOnClickListener(new View.OnClickListener(){  
			@Override  
			public void onClick(View v) {  
				Intent intent = new Intent();  
				intent.setClass(LoginActivity.this, NewUserActivity.class);  
				startActivity(intent);        
			}
		});
	
	
		forgetpsw.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent();
				intent.setClass(LoginActivity.this, RetrievePasswordActivity.class);
				startActivity(intent);
				finish();
			}
		});
  
		btn_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				EditText password = (EditText) findViewById(R.id.password); 
				TextView txt=(TextView)findViewById(R.id.display);
				usm=username.getText().toString();
				String psw=password.getText().toString();
				// hash the password
				MessageDigest md = null;
				try {
					md = MessageDigest.getInstance("SHA-256");
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
				}
				md.update(psw.getBytes());
	 
				byte byteData[] = md.digest();
	 
				//convert the byte to hex format method 1
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < byteData.length; i++) {
					sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
				}
	 
				System.out.println("Hex format : " + sb.toString());
				String psword=sb.toString();
		
				//post the user name and password to the server
				try{
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost post = new HttpPost("http://89.250.116.142/Quizgame/jaxrs/quizgame/login");  
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("username", usm ));
					nameValuePairs.add(new BasicNameValuePair("password", psword ));
	    		
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
	    	
					HttpResponse httpResponse = httpclient.execute(post);  	
					HttpEntity entity = httpResponse.getEntity();
					String retSrc = EntityUtils.toString(entity);  
					if(retSrc.equals("success")){
						GCMRegistrar.register(LoginActivity.this,SENDER_ID);
						
						//save to SharePreference
						savePreferences("storedName",username.getText().toString());
						data.addPlayer(usm);
						Intent intent= new Intent();
						intent.setClass(LoginActivity.this, TitleScreenActivity.class);
						startActivity(intent); 
						finish();
						
					}
					else{
				
						
						txt.setText("The username is not existed or the password is wrong!"); 
					
					}

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		}); 
  
	}
	
	private void loadSavedPreferences() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String name = sharedPreferences.getString("storedName", "YourName");
		username.setText(name);
		//textView.setText(name);
		}
	
	private void savePreferences(String key, String value) {
		SharedPreferences sharedPreferences = PreferenceManager
		.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
		}
	
}
