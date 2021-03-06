package com.ble.ble;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	boolean testBool;
	int REQUEST_ENABLE_BT = 1;
	String password, user;
	int resultCode;
	SharedPreferences settings;
	Button loginButton;
	TextView loginText;
	EditText nameField, passField;
	ProgressBar snurr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		testBool = false;
		loginButton = (Button)findViewById(R.id.login);
		loginText = (TextView)findViewById(R.id.textView1);
		nameField = (EditText)findViewById(R.id.username);
		passField = (EditText)findViewById(R.id.password);
		snurr = (ProgressBar)findViewById(R.id.progressBar1);
		settings = getSharedPreferences("Credentials", 0);
		resultCode = -1;
		if(settings.getString("access_token", "") != ""){
			proceed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
//	public void clickedButton(View v){
//		testBool = true;
//		Intent intent = new Intent(this, CheckinActivity.class);
//		startActivity(intent);
//	}

	/**
	 * Exit instead of showing login screen when pressing the back button from other activity
	 */
	@Override
	protected void onResume(){
		super.onResume();
		if(testBool){
			finish();
		}
	}
	

	public void Send(View v) {
		password = passField.getText().toString(); 
		System.out.println(password);
		passField.getEditableText().toString();

		user = nameField.getText().toString();
		System.out.println(user);
		nameField.getEditableText().toString();
		nameField.setEnabled(false);
		passField.setEnabled(false);
		snurr.setVisibility(0);
		loginText.setVisibility(0);
		loginButton.setEnabled(false);
		Login(user,password);
    }
    
    
    public void Login(String users, String passwords){
    	
    	new Thread() {
    		  public void run() {
    			  HttpClient httpclient = new DefaultHttpClient();
    			  String url = "http://bleserver.broccomoped.se/api/authenticate";
    			  // Prepare a request object
    			  HttpPost httppost = new HttpPost(url);
    			  // Execute the request
    			  HttpResponse response;
    			  try {
    				  
    				  //System.out.println(user + " Login");
    				  //System.out.println(password);
    				  
    				  JSONObject json = new JSONObject();     
    				  json.put("username", user);
    				  json.put("password", password);
    				  Log.i("jason Object", json.toString());
    				  StringEntity se = new StringEntity(json.toString());
    				  se.setContentEncoding("UTF-8");
    				  se.setContentType("application/json");
    				  httppost.setEntity(se);      

    				  response = httpclient.execute(httppost);
    				  // Examine the response status
    				  Log.d("BOOM",response.getEntity().toString());
    				  // Get hold of the response entity
    				  HttpEntity entity = response.getEntity();
    				  // If the response does not enclose an entity, there is no need
    				  // to worry about connection release
    				  if (entity != null) {
    					  // A Simple JSON Response Read
    					  //InputStream instream = entity.getContent();
    					  //String result = convertStreamToString(instream);
    					  // now you have the string representation of the HTML request
    					  //instream.close();
    					  
    			           String retSrc = EntityUtils.toString(entity);
    			           System.out.println(retSrc);
    			           // parsing JSON
    			           JSONObject result = new JSONObject(retSrc); //Convert String to JSON Object

    			           // Storing each json item in variable
    			           String id = result.getString("access_token");
    			           System.out.println(id);
    			           resultCode = result.getInt("code");
    			           
    			           settings = getSharedPreferences("Credentials", 0);
    			           SharedPreferences.Editor editor = settings.edit();
    			           editor.putString("access_token", id);
    			           editor.putString("username", user);

    			           // Commit the edits!
    			           editor.commit();
    			           if(resultCode == 200){
    			        	   proceed();
    			           } else {
    			        	   LoginActivity.this.runOnUiThread(failed());
    			           }
    				  }
    			  } catch (Exception e) {
    				  System.out.println("Exception! " + e.getClass());
    				  LoginActivity.this.runOnUiThread(failed());
    			  }
    		  }
    	}.start();
    }

    public Runnable failed(){
    	Runnable r = new Runnable(){
    		public void run(){
    			myToast("Login failed, check network connection and try again.");
    			nameField.setEnabled(true);
    			passField.setEnabled(true);
    			snurr.setVisibility(4);
    			loginText.setVisibility(4);
    			loginButton.setEnabled(true);
    		}
    	};
    	return r;
    }
    
	public void proceed(){
		testBool = true;
		Intent intent = new Intent(this, CheckinActivity.class);
		startActivity(intent);
	}
	
	public void myToast(String msg){
		Toast t = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
}
