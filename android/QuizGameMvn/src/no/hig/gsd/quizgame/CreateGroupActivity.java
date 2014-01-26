package no.hig.gsd.quizgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("DefaultLocale")
public class CreateGroupActivity extends Activity {
	
	private String searchUrl = "http://89.250.116.142/Quizgame/jaxrs/quizgame/search";
	
	private ArrayList<String> friends;
	private ArrayList<String> invited;
	
	public final static String INVITED_LIST = "no.hig.gsd.quizgame.invited";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_group);
		
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
		
		setupUI();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_group, menu);
		return true;
	}
	
	
	@Override
	protected void onPause() {
		SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("Friends", friends.size());
		for (int i = 0; i < friends.size(); i++) {
			editor.putString("Friend_"+i, friends.get(i));
		}
		friends.clear();
		editor.commit();
		super.onPause();
	}


	@Override
	protected void onResume() {
		SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
		String friend = null;
		int numberOfFriends = sharedPref.getInt("Friends", 0);
		for (int i = 0; i < numberOfFriends; i++) {
			friend = sharedPref.getString("Friend_"+i, null);
			friends.add(friend);
		}
		FriendsList friendsAdapter = new FriendsList(this, friends);
		ListView list = (ListView) findViewById(R.id.friends_list);
		list.setAdapter(friendsAdapter);
		
		super.onResume();
	}


	private void setupUI() {
		friends = new ArrayList<String>();
		invited = new ArrayList<String>();
		Button button = (Button) findViewById(R.id.search_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchUsers();
			}
		});
		
		button = (Button) findViewById(R.id.cancel_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		button = (Button) findViewById(R.id.ok_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (! invited.isEmpty()) {
					Intent data = new Intent();
					data.putStringArrayListExtra(INVITED_LIST, invited);
					setResult(RESULT_OK, data);
				}
				finish();
			}
		});
		
	}
	
	
	private void searchUsers() {
		EditText editText = (EditText) findViewById(R.id.search_text);
		String username = editText.getText().toString();
		InputStream is = null;
		JSONObject jObject = null;
		String json = "";
		JSONArray users = null;
		String current_user=LoginActivity.usm;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(searchUrl);       		          
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("username", current_user ));
		
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			HttpResponse response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			
		} catch (ClientProtocolException e) {
			Log.d("QuizGame", "Please check the http adress!");
        } catch (IOException e) {
            Log.d("QuizGame", "IOException");
        }
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = "{ " + sb.toString() + " }";
			
		} catch (Exception e) {
			Log.d("QuizGame", "Exception in reading from result!");
		}
		
		try {
			jObject = new JSONObject(json);
		} catch (JSONException e) {
			Log.d("QuizGame", "JSONException in converting from result!");
		}
		
		ArrayList<String> values = new ArrayList<String>();
		try {
			users = jObject.getJSONArray("allusers");
			for(int i = 0; i < users.length(); i++) {
				String name = users.getJSONObject(i).getString("username");
				if (name.toLowerCase().contains(username.toLowerCase())) {
					values.add(name);
				}
			}
		} catch (JSONException e) {
			Log.d("QuizGame", "JSONException in reading JSONObject!");
		}
		
		SearchList adapter = new SearchList(this, values);
		ListView list = (ListView) findViewById(R.id.search_list);
		list.setAdapter(adapter);
		
	}
	
	
	/**
	 * 
	 * @author LarsErik
	 *
	 */
	class SearchList extends ArrayAdapter<String>{

		private final Activity context;
		private final ArrayList<String> usernames;
		
		public SearchList(Activity context, ArrayList<String> usernames) {
			super(context, R.layout.search_item_layout, usernames);
			this.context = context;
			this.usernames = usernames;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View rowView = inflater.inflate(
					R.layout.search_item_layout, null, true);
			TextView username = (TextView) rowView.findViewById(
					R.id.username_text);
			username.setText(usernames.get(position));
			
			Button button = (Button) rowView.findViewById(
					R.id.friend_button);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View view = (View) v.getParent();
					TextView textView = (TextView) view.findViewById(
							R.id.username_text);
					String username = textView.getText().toString();
					if (! friends.contains(username)) {
						friends.add(username);
					}
					FriendsList friendsAdapter = new FriendsList(context, friends);
					ListView list = (ListView) findViewById(R.id.friends_list);
					list.setAdapter(friendsAdapter);
				}
			});
			
			button = (Button) rowView.findViewById(R.id.invite_button);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View view = (View) v.getParent();
					TextView textView = (TextView) view.findViewById(R.id.username_text);
					String username = textView.getText().toString();
					if (! invited.contains(username)) {
						invited.add(username);
					}
					InvitedList invitedAdapter = new InvitedList(context, invited);
					ListView list = (ListView) findViewById(R.id.invited_list);
					list.setAdapter(invitedAdapter);
				}
			});
			
			return rowView;
		}
	}
	
	
	/**
	 * 
	 * @author LarsErik
	 *
	 */
	class FriendsList extends ArrayAdapter<String>{

		private final Activity context;
		private final ArrayList<String> usernames;
		
		public FriendsList(Activity context, ArrayList<String> usernames) {
			super(context, R.layout.friends_item_layout, usernames);
			this.context = context;
			this.usernames = usernames;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View rowView = inflater.inflate(R.layout.friends_item_layout, null, true);
			TextView username = (TextView) rowView.findViewById(R.id.username_text);
			username.setText(usernames.get(position));
			
			Button button = (Button) rowView.findViewById(R.id.unfriend_button);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View view = (View) v.getParent();
					TextView textView = (TextView) view.findViewById(R.id.username_text);
					String username = textView.getText().toString();
					friends.remove(username);
					FriendsList friendsAdapter = new FriendsList(context, friends);
					ListView list = (ListView) findViewById(R.id.friends_list);
					list.setAdapter(friendsAdapter);
				}
			});
			
			button = (Button) rowView.findViewById(R.id.invite_button);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View view = (View) v.getParent();
					TextView textView = (TextView) view.findViewById(R.id.username_text);
					String username = textView.getText().toString();
					if (! invited.contains(username)) {
						invited.add(username);
					}
					InvitedList invitedAdapter = new InvitedList(context, invited);
					ListView list = (ListView) findViewById(R.id.invited_list);
					list.setAdapter(invitedAdapter);
				}
			});
			
			return rowView;
		}
	}
	
	
	/**
	 * 
	 * @author LarsErik
	 *
	 */
	class InvitedList extends ArrayAdapter<String>{

		private final Activity context;
		private final ArrayList<String> usernames;
		
		public InvitedList(Activity context, ArrayList<String> usernames) {
			super(context, R.layout.invited_item_layout, usernames);
			this.context = context;
			this.usernames = usernames;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View rowView = inflater.inflate(R.layout.invited_item_layout, null, true);
			TextView username = (TextView) rowView.findViewById(R.id.username_text);
			username.setText(usernames.get(position));
			
			Button button = (Button) rowView.findViewById(R.id.uninvite_button);
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View view = (View) v.getParent();
					TextView textView = (TextView) view.findViewById(R.id.username_text);
					String username = textView.getText().toString();
					invited.remove(username);
					InvitedList invitedAdapter = new InvitedList(context, invited);
					ListView list = (ListView) findViewById(R.id.invited_list);
					list.setAdapter(invitedAdapter);
				}
			});
			
			return rowView;
		}
	}
	
}
