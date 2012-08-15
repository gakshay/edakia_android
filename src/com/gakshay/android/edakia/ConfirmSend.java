package com.gakshay.android.edakia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class ConfirmSend extends Activity {
	
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
        setContentView(R.layout.activity_confirm_send);
		//prepareActivityContent();
		prepareConfirmSendDialogBox();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_confirm_send, menu);
        return true;
    }

    private void prepareConfirmSendDialogBox(){
		 AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Confirm Send");
			adb.setCancelable(false);
			adb.setPositiveButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					onDialogPressedOK();
					//Action for 'Ok' Button
				}
			});


			adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					// Action for 'Cancel' Button
					dialog.cancel();
					onDialogPressedCancel();
				}
			});
			
			AlertDialog dialog = adb.create();
			dialog.onWindowFocusChanged(false);
			dialog.setCancelable(false);
			Window window = dialog.getWindow();
			WindowManager.LayoutParams wlp = window.getAttributes();

			wlp.gravity = Gravity.BOTTOM;
			wlp.width = 1000;
			wlp.height = 1000;
			WindowManager.LayoutParams params = window.getAttributes();  
		       params.x = -100;  
		       params.height = 70;  
		       params.width = 1000;  
		       params.y = -50;  
		  
		       dialog.getWindow().setAttributes(params); 
		       wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
			window.setAttributes(wlp);
			adb.setIcon(R.drawable.ic_launcher_send);
			//adb.show(); 
			dialog.show();
	}

    
    private void onDialogPressedCancel(){
		Toast.makeText(this, "Sending Your Document", Toast.LENGTH_LONG).show();
		doSendFile();
    }
    
    
    private void onDialogPressedOK(){
    	Toast.makeText(this, "Taking you home page.", Toast.LENGTH_LONG).show();
		Intent homeIntent = new Intent(this, Edakia.class);
		startActivity(homeIntent);
		finish();
    }
    
    
    public void prepareActivityContent(){

		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

			if ("text/plain".equals(type)) {
				prepareTextDocument(intent); // Handle text being sent
			} else if (type.startsWith("image/")) {
				prepareImageDocument(intent); // Handle single image being sent
			}
	}

    
    private void prepareImageDocument(Intent intent){

    	String imagePath =((Bundle) intent.getExtras()).getString("file");
		File imgFile = new  File(imagePath.toString());
		if(imgFile.exists())
		{
			Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			ImageView myImage = (ImageView) findViewById(R.id.imageview01);
			myImage.setImageBitmap(myBitmap);

		}
	}
    
    
    private void prepareTextDocument(Intent intent){
		try {
	    	String textPath =((Bundle) intent.getExtras()).getString("file");

			File textFile = new  File(textPath);
			if(textFile.exists()){    
				StringBuffer fileData = new StringBuffer(1000);
				BufferedReader reader = new BufferedReader(new FileReader(textFile));
				char[] buf = new char[1024];
				int numRead = 0;
				while ((numRead = reader.read(buf)) != -1) {
					String readData = String.valueOf(buf, 0, numRead);
					fileData.append(readData);
					buf = new char[1024];
				}
				reader.close();

				TextView text = (TextView) findViewById(R.id.textview01);
				text.setText(fileData.toString());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    

	// Will be connected with the buttons via XML
	public void doSendFile() {
		
		Intent intent = getIntent();
		Bundle bundleData = intent.getExtras();
		senderMobile =(String) bundleData.get("sendMobile");
		senderPassword =(String) bundleData.get("sendPassword");
		receiverMobile =(String) bundleData.get("receiverMobile");
		file =(String) bundleData.get("file");
		sendFileToUser(true);

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
			//text.setText(null);

			if(sendResponse != null && sendResponse.contains("Exception")){
				Toast.makeText(ConfirmSend.this, "Could not send your document.Plz check .", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(ConfirmSend.this, "Your document has been sent.", Toast.LENGTH_LONG).show();
				Intent homeIntent = new Intent(ConfirmSend.this, Edakia.class);
				startActivity(homeIntent);
				finish();
			}

		}
	};



}
