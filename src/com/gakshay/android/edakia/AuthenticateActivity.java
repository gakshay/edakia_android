package com.gakshay.android.edakia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AuthenticateActivity extends Activity {

    private static final int NO_WRAP = 2;
	private ProgressDialog progressDialog;
	private String authResponse;
	private String mobile;
	private String password;
    private String authURL = "http://www.edakia.in/api/users.xml";


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
		mobile = ((EditText) findViewById(R.id.YourMobile)).getText().toString();
		password = ((EditText) findViewById(R.id.YourPassword)).getText().toString();
		authenticateUser(authURL, mobile, password,true);
	}
	
	
	private String connectToServer(String urlStr,String name, String password) {
		String response = null;
		try {
			
			String authString = name + ":" + password;
			
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			Log.d("set", authStringEnc);
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
		final String response = null;
		new Thread() {
			public void run() {
				InputStream in = null;
				Message msg = Message.obtain();
				try {
					authResponse = connectToServer(authURL, mobile, password);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				messageHandler.sendMessage(msg);					
			}
		}.start();
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
				sendIntent.putExtra("sendMobile", mobile);
				sendIntent.putExtra("sendPassword", password);
				startActivity(sendIntent);
			}

		}
	};


    
}
