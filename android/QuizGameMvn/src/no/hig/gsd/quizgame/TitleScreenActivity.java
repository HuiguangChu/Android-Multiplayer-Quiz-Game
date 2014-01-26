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

import com.google.android.gcm.GCMRegistrar;

import no.hig.gsd.quizgame.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TitleScreenActivity extends Activity {
	private SetupGame2 game;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_title_screen);
		game = new SetupGame2(this);
		setupUI();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}

	
	private void setupUI() {
		Button button = (Button) findViewById(R.id.new_game_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleNewGameIntent();
			}
		});
		
		button = (Button) findViewById(R.id.existing_game_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleExistingGameIntent();
			}
		});
		
		button = (Button) findViewById(R.id.options_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleOptionsIntent();
			}
		});
		
		button = (Button) findViewById(R.id.logout_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String username=LoginActivity.usm;
					
				try {
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost post = new HttpPost("http://89.250.116.142/Quizgame/jaxrs/quizgame/logout");  
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("username", username ));
		    		
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
		    	
					HttpResponse httpResponse = httpclient.execute(post);  	
					HttpEntity entity = httpResponse.getEntity();
					String retSrc = EntityUtils.toString(entity);  
						
					if (retSrc.equals("success")){
						gcm_unregister();
						logout();
					}
				} catch (ClientProtocolException e) {
					Log.d("QuizGameTitleScreen", "Please check the provided http address!");
				} catch (IOException e) {
					Log.d("QuizGameTitleScreen", "IOException in logout");
				} 
					
					
					
			}
		});
	}
	
	
	private void handleNewGameIntent() {
		startActivity(new Intent(this, NewGameActivity.class));
	}
	
	private void handleExistingGameIntent() {
		
	}
	
	private void handleOptionsIntent() {
		startActivity(new Intent(this, OptionsActivity.class));
	}
	
	private void logout() {
		System.exit(0);

	}
	public void gcm_unregister(){
		String regId=GCMRegistrar.getRegistrationId(TitleScreenActivity.this);
		ServerUtilities.unregister(TitleScreenActivity.this, regId);
		GCMRegistrar.unregister(TitleScreenActivity.this);
		
	}
		
	@Override
    public void onContentChanged()
    {
		if(game != null) game.removeListeners();
        super.onContentChanged();   
    }	
	
	
}
