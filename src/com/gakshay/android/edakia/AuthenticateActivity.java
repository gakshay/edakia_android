package com.gakshay.android.edakia;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AuthenticateActivity extends Activity {

    private static final int NO_WRAP = 2;
    private String authURL = "http://edakia.in/api/users.xml";


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
		EditText mobile = (EditText) findViewById(R.id.YourMobile);
		EditText password = (EditText) findViewById(R.id.YourPassword);
		
		//initialize error text value to null.
		TextView text = (TextView) findViewById(R.id.Error);
		text.setText(null);
		
		if(connectToServer(authURL, mobile.getText().toString(), password.getText().toString()).contains("Exception")){
			text.setText("Could not authenticate You !! \n Please make sure you entered correct details.");
		}else{
			Toast.makeText(this, "Authenticated Successfullly.Go Ahead !!", Toast.LENGTH_SHORT).show();

			Intent sendIntent = new Intent(this, SendActivity.class);
			sendIntent.putExtra("sendMobile", mobile.getText().toString());
			sendIntent.putExtra("sendPassword", password.getText().toString());
			startActivity(sendIntent);
		}
		
	}
	
	
	private String connectToServer(String urlStr,String name, String password) {
		String response = null;
		try {
			
			String authString = name + ":" + password;
			
			byte[] authEncBytes = android.util.Base64.encode(authString.getBytes(), NO_WRAP);
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
    
}
