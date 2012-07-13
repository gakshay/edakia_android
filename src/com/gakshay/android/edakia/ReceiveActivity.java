package com.gakshay.android.edakia;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.EditText;
import android.util.Log;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
//import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class ReceiveActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(ReceiveActivity.this, Edakia.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void getDocument(View view) {
    	switch(view.getId()){
    	case R.id.receiveNextButton:
    		EditText mobile = (EditText) findViewById(R.id.receiveMobile);
    		EditText secretCode = (EditText) findViewById(R.id.secretCode);
    		if(mobile.getText().length() == 0){
    			Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show();
    			return;
    		}
    		if(mobile.getText().length() < 10){
    			Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show();
    			return;
    		}
    		if(secretCode.getText().length() == 0){
    			Toast.makeText(this, "Enter Secret Code", Toast.LENGTH_LONG).show();
    			return;
    		}
    		if(secretCode.getText().length() < 6){
    			Toast.makeText(this, "Incorrect Secret Code", Toast.LENGTH_LONG).show();
    			return;
    		}
    		try {
	    		URL url = new URL("http://http://edakia.in/transactions/receive.xml");
	    		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	    		urlConnection.setReadTimeout(10000 /* milliseconds */);
	    		urlConnection.setConnectTimeout(15000 /* milliseconds */);
	    		urlConnection.setRequestMethod("GET");
	    		urlConnection.connect();
    		    InputStream in = urlConnection.getInputStream();
    		    readStream(in);
    		    urlConnection.disconnect();
	    		
    		}
    		catch (IOException e) {
                //handle the exception !
    			e.printStackTrace();
    		}
    		Toast.makeText(this, "Thanks fetching Document", Toast.LENGTH_LONG).show();
    		break;
    	default:
    		break;
    	}
    }
    
    public void readStream(InputStream input){
    	// read stream
    	InputStreamReader is = new InputStreamReader(input);
    	BufferedReader br = new BufferedReader(is);
    	try{
	    	String read = br.readLine();
		    while(read != null) {
		    	    System.out.println(read);
		    	    read = br.readLine();
		    	}
	    	Log.e("stream", read);
    	} catch (IOException e){
    		e.printStackTrace();
    	}
    }
    
}
