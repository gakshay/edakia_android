package com.gakshay.android.edakia;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gakshay.android.util.ActivitiesHelper;
import com.gakshay.android.validation.Validator;

public class AuthenticateActivity extends BaseActivity {

	private ProgressDialog progressDialog;
	private String authResponse;
	private EditText mobile;
	private EditText password;
	private String userId;
	private String authURL = "http://staging.edakia.in/api/users.xml"; //"http://www.edakia.in/api/users.xml";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authenticate);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_authenticate, menu);
		return true;
	}

	// Will be connected with the buttons via XML
	public void authenticate(View aview) {
		mobile = ((EditText) findViewById(R.id.YourMobile));
		password = ((EditText) findViewById(R.id.YourPassword));
		if(validateInputData())
			authenticateUser(authURL, mobile.getText().toString(), password.getText().toString(),true);			
	}


	private boolean validateInputData(){
		boolean isValid = false;
		TextView text = (TextView) findViewById(R.id.Error);
		text.setText(null);

		//both are mandatory fields.
		if((password.getText().toString() == null || "".equalsIgnoreCase(password.getText().toString()))
				|| (mobile.getText().toString() == null || "".equalsIgnoreCase(mobile.getText().toString()))){

			Toast.makeText(this, "Please provide both inputs,mobile number and secret number.", Toast.LENGTH_LONG).show();
			return false;
		}
		//Validate mobile no.
		int valStatusCode = Validator.validateMobileNumber(mobile.getText().toString()).ordinal();
		switch(valStatusCode){
		case 1:
			Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show();
			text.setText("You missed mobile number. Plz enter the same.");
			mobile.findFocus();
			return false;
		case 2:
			Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show();
			text.setText("You entered incorrect mobile number. Plz correct the same.");
			mobile.findFocus();
			return false;		
		}

		//Validate secret no.
		valStatusCode = Validator.validatePassword(password.getText().toString()).ordinal();
		switch(valStatusCode){
		case 5:
			Toast.makeText(this, "Enter your password.", Toast.LENGTH_LONG).show();
			text.setText("You missed password number. Plz enter the same.");
			password.findFocus();
			return false;
		case 6:
			Toast.makeText(this, "Incorrect password.", Toast.LENGTH_LONG).show();
			text.setText("You entered incorrect password . Plz correct the same");
			password.findFocus();
			return false;					
		}		

		if(valStatusCode == 0)
			isValid = true;
		return isValid;	
	}

	private Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			progressDialog.dismiss();
			//initialize error text value to null.
			TextView text = (TextView) findViewById(R.id.Error);
			text.setText(null);

			if(authResponse.contains("Exception")){
				text.setText("Could not authenticate You !! \n Please make sure you entered correct details.");
			}else{
				Toast.makeText(AuthenticateActivity.this, "Authenticated Successfullly.Go Ahead !!", Toast.LENGTH_SHORT).show();
				Intent sendIntent = new Intent(AuthenticateActivity.this, SendActivity.class);
				sendIntent.putExtra("sendMobile", mobile.getText().toString());
				sendIntent.putExtra("sendPassword", password.getText().toString());
				sendIntent.putExtra("userId", userId);
				startActivity(sendIntent);
				finish();
			}

		}
	};


	private String connectToServer(String urlStr,String name, String password) {
		String response = null;
		try {

			String authString = name + ":" + password;

			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			URL url = new URL(urlStr);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			InputStream is = urlConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);

			int numCharsRead;
			char[] charArray = new char[1024];
			StringBuffer sb = new StringBuffer();
			while ((numCharsRead = isr.read(charArray)) > 0) {
				sb.append(charArray, 0, numCharsRead);
			}
			response = sb.toString();
			is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			response = "Exception";
		} catch (IOException e) {
			e.printStackTrace();
			response = "Exception";
		}catch (Exception ex){
			ex.printStackTrace();
			response = "Exception";
		}
		return response;

	}



	private void authenticateUser(final String authURL,final String mobile,final String password, boolean showProcessDialog) {
		if(showProcessDialog)
			progressDialog = ProgressDialog.show(this, "", 
					"Authenticating Your identity \n................" );
		new Thread() {
			public void run() {
				InputStream in = null;
				Message msg = Message.obtain();
				try {
					authResponse = connectToServer(authURL, mobile, password);
					if(authResponse != null && !"Exception".equalsIgnoreCase(authResponse))
						userId = (ActivitiesHelper.fetchValuesFromReponse(authResponse)).get("id");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				messageHandler.sendMessage(msg);					
			}
		}.start();
	}    
}
