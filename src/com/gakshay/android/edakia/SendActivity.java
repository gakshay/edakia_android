package com.gakshay.android.edakia;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SendActivity extends Activity {
	private static final int NO_WRAP = 2;
	private String authURL = "http://www.edakia.in/transactions.xml";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_send, menu);
		return true;
	}



	// Will be connected with the buttons via XML
	public void sendFile(View aview) {
		EditText receiverMobile = (EditText) findViewById(R.id.receiverMobile);

		RadioGroup selectFileGroup = (RadioGroup)findViewById(R.id.selectFileGroup);
		int selectedRadioButtonId = selectFileGroup.getCheckedRadioButtonId();
		
		Intent intent = getIntent();
		Bundle bundleData = intent.getExtras();
		String senderMobile =(String) bundleData.get("sendMobile");
		String senderPassword =(String) bundleData.get("sendPassword");
		String file = null;
		if(selectedRadioButtonId == 1){//File Explorer.
			//Assuming recently scanned file has been saved as recentlyScanned.jpg @ path /mnt/sdcard
			file = "/mnt/sdcard/logo.jpg";
		}else if (selectedRadioButtonId == 2){
			Toast.makeText(this, "Development in progress.....", Toast.LENGTH_LONG).show();
			// "/mnt/sdcard/logo.jpg"
		}else if (selectedRadioButtonId == 3) {
			Toast.makeText(this, "Development in progress.....", Toast.LENGTH_LONG).show();

		}else {
			Toast.makeText(this, "You Clicked on wrong option.", Toast.LENGTH_LONG).show();

		}
		
	
		//initialize error text value to null.
		TextView text = (TextView) findViewById(R.id.Error);
		text.setText(null);

		if(sendToEdakiaServer(authURL, senderMobile, senderPassword,receiverMobile.getText().toString(),file).contains("Exception")){
			text.setText("Could not send your document !! \n Please make sure you file is correctly scanned.");
		}else{
			Toast.makeText(this, "Your document has been sent.", Toast.LENGTH_LONG).show();

			Intent sendIntent = new Intent(this, Edakia.class);
			startActivity(sendIntent);
		}

	}



	private String sendToEdakiaServer(String urlStr,String senderMobile, String senderPassword,String receiverMobile,String file) {
		String response = null;
		HttpClient httpclient = null;
		try {
			// Add your data
			httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(authURL);
			String authString = senderMobile + ":" + senderPassword;
			byte[] authEncBytes = android.util.Base64.encode(authString.getBytes(), NO_WRAP);
			String authStringEnc = new String(authEncBytes);

			httppost.setHeader("Authorization", "Basic " + authStringEnc);

			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			entity.addPart("transaction[document_attributes][doc]",new FileBody(new File(file) , "image/jpeg"));
			entity.addPart("transaction[receiver_mobile]",new StringBody(receiverMobile));
			entity.addPart("transaction[sender_mobile]",new StringBody(senderMobile));

			httppost.setEntity(entity);

			HttpResponse httpResponse = httpclient.execute(httppost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));


			HttpEntity resEntity = httpResponse.getEntity();
			Log.d("Response from server while uploading file : ",httpResponse.getStatusLine().toString());

			String line = "";
			while ((line = rd.readLine()) != null) {
				Log.d("Response",line);
				if (line.startsWith("Auth=")) {
					String key = line.substring(5);
					// Do something with the key
				}

			}
			resEntity.consumeContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response="Exception";
		} catch (IOException e) {
			e.printStackTrace();
			response="Exception";
		}finally{
			httpclient.getConnectionManager().shutdown();

		}
		return response;

	}
}