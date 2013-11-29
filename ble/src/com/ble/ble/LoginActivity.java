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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity {
	boolean testBool;
	int REQUEST_ENABLE_BT = 1;
	String password, user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		testBool = false;
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
    EditText et = (EditText)findViewById(R.id.password);
    password = et.getText().toString(); 
    System.out.println(password);
    et.getEditableText().toString();
    
    EditText et2 = (EditText)findViewById(R.id.username);
    user = et2.getText().toString();
    System.out.println(user);
    et2.getEditableText().toString();
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
    			           
    			           SharedPreferences settings = getSharedPreferences("Credentials", 0);
    			           SharedPreferences.Editor editor = settings.edit();
    			           editor.putString("access_token", id);
    			           editor.putString("username", user);

    			           // Commit the edits!
    			           editor.commit();

    				  }
    			  } catch (Exception e) {
    				  System.out.println("Exception! " + e.getClass());
    			  }
    		  }
    	}.start();
    }
	
	
}
