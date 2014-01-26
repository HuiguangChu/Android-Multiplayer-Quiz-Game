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

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StatActivity extends Activity {
	//EditText username;
	TextView user;
	String userName = "olav";
			//LoginActivity. usm; 
	TextView getUserName;
	TextView connError;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stat);
		
		//username = (EditText) findViewById(R.id.username);
		user = (TextView) findViewById(R.id.user);
		getUserName = (TextView) findViewById(R.id.score);
		getUserName.setText(userName);
		connError = (TextView) findViewById(R.id.turn);
		
		//loads username if saved in SharedPreferences
		loadSavedPreferences();
		
		setupUI();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stat, menu);
		return true;
	}
	
	public void setupUI(){
		Button statist = (Button) findViewById(R.id.statist);
		
		statist.setOnClickListener(new View.OnClickListener(){  
			@Override  
			public void onClick(View v) {  
		//String retSrc ="test";
		//connError.setText(retSrc);
		
	try {
		HttpClient httpclient = new DefaultHttpClient();
		//HttpPost post = new HttpPost("http://89.250.116.142/Quizgame/jaxrs/quizgame/getstatistic");
		HttpPost post = new HttpPost("http://128.39.81.114:8080/Quizgame/jaxrs/quizgame/getstatistic");   
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user_name", userName));
		
		
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
          
		HttpResponse response = httpclient.execute(post);
		HttpEntity entity = response.getEntity();
		String retSrc = EntityUtils.toString(entity);
		if (retSrc.equals("")){
			connError.setText("Registration fails");
		}
		 else {
			 connError.setText(retSrc);
		}
		 
	} catch (ClientProtocolException e) {
		System.out.println("Please check your provided http address!");
		e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
});
}; 

	
	//function for loading username from SharedPreferences. 
		private void loadSavedPreferences() {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String name = sharedPreferences.getString("storedName", "YourName");
			//username.setText(name);
			user.setText(name);
			}
}