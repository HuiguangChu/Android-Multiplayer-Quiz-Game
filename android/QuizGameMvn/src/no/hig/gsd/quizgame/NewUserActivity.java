package no.hig.gsd.quizgame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

public class NewUserActivity extends Activity{
	//AsyncTask<Void, Void, Void> mRegisterTask;
	EditText usm;
	EditText psw;
	EditText conpsw;
	EditText eml;
	
	String new_usm="";
	String new_psw="";
	String new_password="";
	String new_conpsw="";
	String new_email="";
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
		setContentView(R.layout.activity_new_user);
		
		Button btn_newusm = (Button) findViewById(R.id.btn_newuser);	
	
		btn_newusm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				usm=(EditText)findViewById(R.id.new_usm);
				psw=(EditText)findViewById(R.id.new_psw);
				conpsw=(EditText)findViewById(R.id.con_psw);
				eml=(EditText)findViewById(R.id.new_email);
				new_usm=usm.getText().toString();
				new_psw=psw.getText().toString();
				new_conpsw=conpsw.getText().toString();
				new_email=eml.getText().toString();
				System.out.print(new_psw);
				System.out.print(new_conpsw);
				TextView new_textview=(TextView)findViewById(R.id.new_txt);
				Matcher m=checkemail(new_email);
				
				if( new_psw.equals(new_conpsw)){
					//Hash the password 
					new_password=hash(new_psw);
					if(!m.matches())
					{
						new_textview.setText("The email is not valid");
					}
					else{
					try{
						
						HttpClient httpclient = new DefaultHttpClient();
						HttpPost post = new HttpPost("http://89.250.116.142/Quizgame/jaxrs/quizgame/newuser");   
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			    		nameValuePairs.add(new BasicNameValuePair("new_name", new_usm ));
			    		nameValuePairs.add(new BasicNameValuePair("new_psw", new_password ));
			    		nameValuePairs.add(new BasicNameValuePair("new_email", new_email ));
			    		
			    		post.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));
			              
			    		HttpResponse response = httpclient.execute(post);

						HttpEntity entity = response.getEntity();
						String retSrc = EntityUtils.toString(entity);
						if (retSrc.equals("repeat")){
							
							new_textview.setText("The username or email you registered is existed already");
						}
						else{
						if(retSrc.equals("success")){
							Intent intent=new Intent();
							intent.setClass(NewUserActivity.this, LoginActivity.class);
							startActivity(intent); 
						} else {
							new_textview.setText("Registration fails");
						}
						}
						 
					}catch (ClientProtocolException e) {
						System.out.println("Please check your provided http address!");
						e.printStackTrace();
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
						}
				} else{
					new_textview.setText("The confirmed password is wrong");
				}
				
				
				
		        
			}
		});
		
	}
	
	public String hash(String s){
		MessageDigest md = null;    
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		md.update(s.getBytes());
		 
		byte byteData[] = md.digest();
		 
		//convert the byte to hex format
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		 
		System.out.println("Hex format:" + sb.toString());
		return sb.toString();
		
	}
	public Matcher checkemail( String email){
		Pattern pattern = Pattern.compile( "^([a-zA-Z0-9_.-])+@([a-zA-Z0-9_.-])+\\.([a-zA-Z])+([a-zA-Z])+");

         Matcher matcher = pattern.matcher(email);
      return matcher;
	}
}

