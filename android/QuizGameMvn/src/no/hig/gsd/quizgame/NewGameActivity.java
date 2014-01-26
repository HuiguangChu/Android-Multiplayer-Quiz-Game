package no.hig.gsd.quizgame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;

public class NewGameActivity extends Activity {
	
	private ArrayList<String> invited;
	
	public static final int CREATE_GROUP = 100;
	public static final int MIN_NUMBER_OF_TURNS = 1;
	public static final int MAX_NUMBER_OF_TURNS = 20;

	private SetupGame2 game;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_game);
		this.game = new SetupGame2(this);
		setupUI();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_game, menu);
		return true;
	}

	
	private void setupUI() {
		final Spinner spinnerPlayers = (Spinner) findViewById(R.id.players_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.players_array, R.layout.spinner_layout);
		adapter.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		spinnerPlayers.setAdapter(adapter);
		
		final Spinner spinnerGamemode = (Spinner) findViewById(R.id.game_mode_spinner);
		adapter = ArrayAdapter.createFromResource(
				this, R.array.game_mode_array, R.layout.spinner_layout);
		spinnerGamemode.setAdapter(adapter);
		
		final Spinner spinnerDifficulty = (Spinner) findViewById(R.id.difficulty_spinner);
		adapter = ArrayAdapter.createFromResource(
				this, R.array.difficulty_array, R.layout.spinner_layout);
		spinnerDifficulty.setAdapter(adapter);
		
		final NumberPicker turnPicker = (NumberPicker) findViewById(
				R.id.turns_picker);
		turnPicker.setMinValue(MIN_NUMBER_OF_TURNS);
		turnPicker.setMaxValue(MAX_NUMBER_OF_TURNS);
		
		Button button = (Button) findViewById(R.id.create_group_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createGroup();
			}
		});
		
		button = (Button) findViewById(R.id.create_game_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String players = (String) spinnerPlayers.getSelectedItem();
				String gamemode = (String) spinnerGamemode.getSelectedItem();
				String difficulty = (String) spinnerDifficulty.getSelectedItem();
				int turns = turnPicker.getValue();
				//System.out.println(spinnerDifficulty.getSelectedItemId());
				if(invited.size() > 0) {
					game.start(gamemode, difficulty, turns, 4, invited);
				}
				//createNewGame();
			}
		});
		
		invited = new ArrayList<String>();
	}
	
	
	private void createGroup() {
		Intent intent = new Intent(this, CreateGroupActivity.class);
		startActivityForResult(intent, CREATE_GROUP);
	}
	
	
	private void createNewGame() {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://89.250.116.142/Quizgame/jaxrs/quizgame/notification");
			
			JSONArray json = null;
			json = new JSONArray(invited);
			String jdata="{users:"+ json +"}";
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("list", jdata ));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
    		httpclient.execute(post);
			startActivity(new Intent(this, QuestionActivity.class));
			
			
			
		} catch (ClientProtocolException e) {
			Log.d("QuizGameNewGame", "Please check your provided http address!");
        } catch (IOException e) {
            Log.d("QuizGameNewGame", "IOException in Creating Game");
		} 
		
	}


	@Override
	protected void onActivityResult(int requestCode,
			int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case CREATE_GROUP:
			if(resultCode == RESULT_OK) {
				if (data.hasExtra(CreateGroupActivity.INVITED_LIST)) {
					invited = data.getStringArrayListExtra(
							CreateGroupActivity.INVITED_LIST);
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							this, android.R.layout.simple_list_item_1,
							android.R.id.text1, invited);
					ListView list = (ListView) findViewById(R.id.invited_list);
					list.setAdapter(adapter);
				}
			}
			break;
		}
	}
	
	@Override
    public void onContentChanged()
    {
		if(game != null) game.removeListeners();
        super.onContentChanged();   
    }
	
}
