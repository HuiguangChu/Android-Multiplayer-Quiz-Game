package no.hig.gsd.quizgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;

// Data Access Object lets you interact with the mysql database
public class DAO {
	
	private static final String TAG = "DAO";
	private static DAO singleton;
	
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private Context context;
	
	private DAO(Context context) {
		dbHelper = MySQLiteHelper.getInstance(context);
	}
	
	public static synchronized DAO getInstance(Context context) {
		if (singleton == null) {
			singleton = new DAO(context);
		}
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		singleton.context = context;
		return singleton;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void openWrite() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void openRead() throws SQLException {
		database = dbHelper.getReadableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void drop() {
		openWrite();
		dbHelper.drop(database);
		close();
	}

	// Do a query and return the result as an String[][]
	public String[][] query(String sql) {
		int rows, columns;
		String[][] result;
		openRead();
		Cursor cursor = database.rawQuery(sql, null);
		cursor.moveToFirst();
		close();
		rows = cursor.getCount();
		columns = cursor.getColumnCount();
		result = new String[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				result[i][j] = cursor.getString(j);
			}
			cursor.moveToNext();
		}
		cursor.moveToFirst();
		cursor.close();
		return result;
	}

	// Do a query and print the result in the console
	public void printQuery(String sql) {
		openRead();
		Cursor cursor = database.rawQuery(sql, null);
		cursor.moveToFirst();
		close();
		for (int i = 0; i < cursor.getCount(); i++) {
			for (int j = 0; j < cursor.getColumnCount(); j++) {
				Log.i(TAG,cursor.getString(j));
			}
			cursor.moveToNext();
		}
		cursor.moveToFirst();
		cursor.close();
	}
	
	public String doPost(final String query) {
		String result = post(query);
		if(result == null) alert(new Callback() {
			@Override
			public void onFinished() {
				doPost(query);
			}});
		return result;
	} // TODO add progress Dialog with trying to reestablish connection
	
	public String doGet(final String query) {
		String result = get(query);
		System.out.println("check "+result);
		if(result == null) alert(new Callback() {
			@Override
			public void onFinished() {
				doGet(query);
			}});
		return result;
	}

	
	public void alert(final Callback callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Timeout")
        .setMessage("Please check your connection")
        .setCancelable(false)
        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
                Log.i(TAG,"Retrying connection");
                callback.onFinished();
			}
		})
        .setNegativeButton("Back",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	Log.i(TAG,"Cancel, go back");
                Activity activity = (Activity) context;
                activity.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
	}

	protected String get(final String query) {
		String json = "";
		//final String url = "http://89.250.116.142/Quizgame/jaxrs/quizgame/" + query;  // example query="search"
		String url = "http://www.stud.hig.no/~091379/PHP/getjson.php";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			/** remove StringEntity to work with the freya db **/
			StringEntity se = new StringEntity(query);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"text/plain"));
			post.setEntity(se);
			/***/
    		HttpResponse response = httpclient.execute(post);
    		InputStream in = response.getEntity().getContent();
    		json = convertStreamToString(in);
    		//json = "{"+json+"}"; TODO
		} catch (ClientProtocolException e) {
			//Log.e(TAG,"Error Cannot Establish Connection doGet()");
			return null;
        } catch (IOException e) {
        	//Log.e(TAG,"Error IOException doGet()");
            return null;
        }		
		return json;
	}
	
	protected String post(final String query) {
		String json = "";
		//final String url = "http://89.250.116.142/Quizgame/jaxrs/quizgame/" + query;  // example query="search"
		String url = "http://www.stud.hig.no/~091379/PHP/executeSQL.php";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			/** remove StringEntity to work with the freya db **/
			StringEntity se = new StringEntity(query);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"text/plain"));
			post.setEntity(se);
			/***/
    		HttpResponse response = httpclient.execute(post);
    		InputStream in = response.getEntity().getContent();
    		json = convertStreamToString(in);
		} catch (ClientProtocolException e) {
			//Log.e(TAG,"Error Cannot Establish Connection doPost()");
			return null;
        } catch (IOException e) {
        	//Log.e(TAG,"Error IOException doPost()");
            return null;
        }
		return json;
	}

	private String convertStreamToString(InputStream is) throws UnsupportedEncodingException {
		String line = "";
		StringBuilder total = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
		try {
			while ((line = rd.readLine()) != null) {
				total.append(line);
			}
		} catch (Exception e) {
			Log.e(TAG,"Error convertStreamToString()");
		}
		return total.toString();
	}
	
	@SuppressWarnings("rawtypes")
	protected ArrayList<Map> parseJSON(String a) {
		try {
			JSONArray jsonArray = new JSONArray(a);
			//TODO
			//JSONObject jsona = new JSONObject(a);
			//JSONArray jsonArray = jsona.getJSONArray(jsona.names().get(0).toString());
			ArrayList<Map> list =  new ArrayList<Map>();;
			
			for (int i=0; i<jsonArray.length(); i++) {
				Map<String, String> aMap = new HashMap<String, String>();
				JSONObject json = jsonArray.getJSONObject(i);
				Iterator<?> keys = json.keys();
				while(keys.hasNext()) {
					String key = (String)keys.next();
					aMap.put(key, json.getString(key));
				}
				list.add( aMap );
			}
			return list;
		} catch (JSONException e) {
			Log.e(TAG,"Error cannot parse JSON! parseJSON()");		
			return null;
		}
	}
}
