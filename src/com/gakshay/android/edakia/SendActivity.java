package com.gakshay.android.edakia;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SendActivity extends Activity {
	private static final int NO_WRAP = 2;
	private ProgressDialog progressDialog;
	private String senderMobile;
	private String senderPassword;
	private String receiverMobile;
	private String sendResponse;
	private String file;
	private String authURL = "http://www.edakia.in/transactions.xml";
	private static final int REQUEST_STATUS = 1;
	private FileObserver aFileobsFileObserver;
	private String scannedFile;



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

	/** Called when an activity called by using startActivityForResult finishes. */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_STATUS) {
			if (resultCode == RESULT_OK) {
				// A contact was picked.  Here we will just display it
				// to the user.
				file = data.getStringExtra("filePath");
				sendFileToUser(true);
			}
		}
	}


	// Will be connected with the buttons via XML
	public void sendFile(View aview) {
		receiverMobile = ((EditText) findViewById(R.id.receiverMobile)).getText().toString();

		RadioGroup selectFileGroup = (RadioGroup)findViewById(R.id.selectFileGroup);
		int selectedRadioButtonId = selectFileGroup.getCheckedRadioButtonId();
		RadioButton selectedRadioButton = (RadioButton)selectFileGroup.findViewById(selectedRadioButtonId);

		Intent intent = getIntent();
		Bundle bundleData = intent.getExtras();
		senderMobile =(String) bundleData.get("sendMobile");
		senderPassword =(String) bundleData.get("sendPassword");
		String file = null;
		if(selectedRadioButton.getTag().toString().equalsIgnoreCase("Recent")){//File Explorer.
			//Assuming recently scanned file has been saved as recentlyScanned.jpg @ path /mnt/sdcard
			//file = "/mnt/sdcard/recentlyScanned.jpg";

			//prepare the document folder for the user.
			prepareThisUserDocumentFolder();
			//scan the document using app.
			try {
				intent = new Intent();
				intent.setComponent(ComponentName.unflattenFromString("jp.co.canon.bsd.android.aepp.activity/jp.co.canon.bsd.android.aepp.activity.ScannerMainActivity"));
				intent.setAction(Intent.ACTION_VIEW);
				startActivityForResult(intent, REQUEST_STATUS);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
		/*	
			Intent i  = null;
			try {
				i = new Intent(Intent.ACTION_VIEW);
				i.setPackage("com.dynamixsoftware.printershare");
				i.setDataAndType(Uri.fromFile(new File("/mnt/storage/Download/logo.jpg")), "image/jpeg");
				startActivity(i);


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//Toast.makeText(this, "Got some exception trying another way 5 !! ", Toast.LENGTH_LONG).show();

			}*/
			
			scanFileLookup();

		}else if (selectedRadioButton.getTag().toString().equalsIgnoreCase("Browse")){
			// "/mnt/sdcard/logo.jpg"
			Intent selectFile = new Intent(this, FileChooser.class);
			startActivityForResult(selectFile, REQUEST_STATUS);    		
		}else if (selectedRadioButton.getTag().toString().equalsIgnoreCase("Edakia")) {
			Toast.makeText(this, "Development in progress.....", Toast.LENGTH_LONG).show();

		}else {
			Toast.makeText(this, "You Clicked on wrong option.", Toast.LENGTH_LONG).show();

		}
	}

	private void prepareThisUserDocumentFolder(){
		File aScanPDFDir = new File("/mnt/storage/CanonEPP/scan_pdf/");
		Toast.makeText(this, "Preparing user Document", Toast.LENGTH_SHORT).show();
		if(aScanPDFDir.exists() && aScanPDFDir.isDirectory() && aScanPDFDir.list().length != 0){//rename this directory name.
			//aScanPDFDir.renameTo(new File("/mnt/storage/" + receiverMobile + "/sendDocs"));
			File aTempFile =null;
			File[] aTempFilesCollection = aScanPDFDir.listFiles();
			for(int i = 0; i < aTempFilesCollection.length; i++){
				aTempFile = aTempFilesCollection[i];
				aTempFile.delete();
			}
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
			response = "Success";
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


	private void sendFileToUser(boolean showProcessDialog) {
		if(showProcessDialog)
			progressDialog = ProgressDialog.show(this, "", 
					"Sending Your file \n................" );
		new Thread() {
			public void run() {
				InputStream in = null;
				Message msg = Message.obtain();
				try {
					sendResponse = sendToEdakiaServer(authURL, senderMobile, senderPassword,receiverMobile,file);
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

			if(sendResponse != null && sendResponse.contains("Exception")){
				text.setText("Could not send your document !! \n Please make sure you file is correctly scanned.");
			}else{
				Toast.makeText(SendActivity.this, "Your document has been sent.", Toast.LENGTH_LONG).show();
				Intent homeIntent = new Intent(SendActivity.this, Edakia.class);
				startActivity(homeIntent);
			}

		}
	};


	private void scanFileLookup(){
		///mnt/storage/CanonEPP/scan_pdf
		Log.d("FileObserver", "Inside scan file lookup");
		aFileobsFileObserver = new FileObserver("/mnt/storage/CanonEPP/scan_pdf/", FileObserver.MOVED_TO) {
			@Override
			public void onEvent(int event, String path) {
				try {
					Log.d("FileObserver", "event "+ event);
					Log.d("FileObserver", "Directory is being observed");
					if(event == FileObserver.MOVED_TO){
						Log.d("FileObserver", "File created has been observed.");

						aFileobsFileObserver.stopWatching();
						Log.d("FileObserver", "File created stop observing.");

						stopService(getIntent());
						Log.d("FileObserver", "File created stop service.");

						File scanfile = new File("/mnt/storage/CanonEPP/scan_pdf");
						scannedFile = scanfile.listFiles()[0].getAbsolutePath();
						Intent sendIntent = new Intent(SendActivity.this, ConfirmSend.class);
						sendIntent.putExtra("sendMobile", senderMobile);
						sendIntent.putExtra("sendPassword", senderPassword);
						sendIntent.putExtra("receiverMobile", receiverMobile);
						sendIntent.putExtra("file", scannedFile);
						if(scannedFile != null && (scannedFile.contains("jpeg")  || scannedFile.contains("jpg") || scannedFile.contains("png"))){
							sendIntent.setType("image/jpeg");
							
						}else{
							sendIntent.setType("application/pdf");
						}
						Thread.sleep(3000);
						startActivity(sendIntent);
						finish();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		aFileobsFileObserver.startWatching();
		startService(getIntent());
	}

}