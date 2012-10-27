package com.gakshay.android.edakia;

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

import com.gakshay.android.util.ActivitiesHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmSend extends BaseActivity {

	private static final int NO_WRAP = 2;
	private ProgressDialog progressDialog;
	private String senderMobile;
	private String senderPassword;
	private String receiverMobile;
	private String sendResponse;
	private String file;
	private String userId;
	private String serialNumber;
	private String receiverEmailAddress;
	private String sendURL =  this.getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("sendURL","http://defaultURL");


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_send);
		prepareConfirmSendDialogBox();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_confirm_send, menu);
		return true;
	}

	private void prepareConfirmSendDialogBox(){
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(getString(R.string.confirmSendDialogTitle));
		adb.setMessage(getString(R.string.confirmSendDialogMsg));
		adb.setCancelable(false);
		adb.setIcon(R.drawable.ic_launcher);
		adb.setPositiveButton(getString(R.string.confirmSendDialogPositiveBtn), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				onDialogPressedOK();
			}
		});


		adb.setNegativeButton(getString(R.string.confirmSendDialogNegativeBtn), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
				onDialogPressedCancel();
			}
		});

		AlertDialog dialog = adb.create();
		dialog.onWindowFocusChanged(false);
		dialog.setCancelable(false);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();

		wlp.gravity = Gravity.CENTER;
		/*wlp.width = 1000;
			wlp.height = 1000;
			WindowManager.LayoutParams params = window.getAttributes();  
		       params.x = -100;  
		       params.height = 70;  
		       params.width = 1000;  
		       params.y = -50;  

		  dialog.getWindow().setAttributes(params); */
		wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wlp);
		dialog.show();
	}


	private void onDialogPressedCancel(){
		//Toast.makeText(this, "Try again !!", Toast.LENGTH_LONG).show();
		finish();
	}


	private void onDialogPressedOK(){
		//Toast.makeText(this, "Sending Your Document.Have Patience......", Toast.LENGTH_LONG).show();
		doSendFile();//don't finish activity here, finish it after you send your file.
	}



	// Will be connected with the buttons via XML
	public void doSendFile() {

		Intent intent = getIntent();
		Bundle bundleData = intent.getExtras();
		senderMobile =(String) bundleData.get("sendMobile");
		senderPassword =(String) bundleData.get("sendPassword");
		receiverMobile =(String) bundleData.get("receiverMobile");
		file =(String) bundleData.get("file");
		serialNumber =(String) bundleData.get("serialNumber");
		receiverEmailAddress =(String) bundleData.get("receiverEmail");
		userId =(String) bundleData.get("userId");

		sendFileToUser(true);

	}


	private String sendToEdakiaServer(String urlStr,String senderMobile, String senderPassword,String receiverMobile,String file,String userId,String serialNumber,String receiverEmailAdd) {
		String response = null;
		HttpClient httpclient = null;
		try {
			// Add your data
			httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(sendURL);
			String authString = senderMobile + ":" + senderPassword;
			byte[] authEncBytes = android.util.Base64.encode(authString.getBytes(), NO_WRAP);
			String authStringEnc = new String(authEncBytes);

			httppost.setHeader("Authorization", "Basic " + authStringEnc);

			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			//Identify MIME type of the document

			String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(file)));
			entity.addPart("transaction[document_attributes][doc]",new FileBody(new File(file) , mimeType));
			entity.addPart("transaction[receiver_mobile]",new StringBody(receiverMobile));
			entity.addPart("transaction[receiver_email]",new StringBody(receiverEmailAdd));
			entity.addPart("transaction[document_attributes][user_id]",new StringBody(userId));
			entity.addPart("transaction[sender_mobile]",new StringBody(senderMobile));
			entity.addPart("serial_number",new StringBody(getSerialNumber()));
			
			httppost.setEntity(entity);
			/*httppost.setHeader("Accept", mimeType);
			httppost.setHeader("Content-Type", mimeType);*/

			HttpResponse httpResponse = httpclient.execute(httppost);
			InputStream respStream = (InputStream)httpResponse.getEntity().getContent();
			InputStreamReader isr = new InputStreamReader(respStream);

			int numCharsRead;
			char[] charArray = new char[2048];
			StringBuffer sb = new StringBuffer();
			while ((numCharsRead = isr.read(charArray)) > 0) {
				sb.append(charArray, 0, numCharsRead);
			}
			response = sb.toString();

			HttpEntity resEntity = httpResponse.getEntity();
			Log.d("Response from server while uploading file : ",httpResponse.getStatusLine().toString());
			Log.d("", response);
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
			progressDialog = ProgressDialog.show(this, "",getString(R.string.sendDocPrgDlg) );
		new Thread() {
			public void run() {
				InputStream in = null;
				Message msg = Message.obtain();
				try {
					sendResponse = sendToEdakiaServer(sendURL, senderMobile, senderPassword,receiverMobile,file,userId,serialNumber,receiverEmailAddress);
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
			//TextView text = (TextView) findViewById(R.id.Error);
			//text.setText(null);
			//text.setVisibility(TextView.INVISIBLE);
			
			Intent homeIntent = new Intent(getApplicationContext(), Edakia.class);
			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			if(sendResponse != null && sendResponse.contains("Exception")){
				//Toast.makeText(ConfirmSend.this, "Could not send your document.\nPlz try again after some time.", Toast.LENGTH_LONG).show();
				//text.setText(getString(R.string.send_error));
				//text.setVisibility(TextView.VISIBLE);
				homeIntent.putExtra("showCostDialogBox", "false");
			}else{
				//Toast.makeText(ConfirmSend.this, "Your document has been sent.Try other transaction.", Toast.LENGTH_LONG).show();
				homeIntent.putExtra("showCostDialogBox", "true");
				homeIntent.putExtra("transactionType", "send");
				homeIntent.putExtra("transactionCost", ActivitiesHelper.fetchValuesFromReponse(sendResponse).get("cost"));

			}
			Intent returnData = new Intent();
			returnData.putExtra("whichAction","killParentActivity");
			setResult(Activity.RESULT_OK,returnData);
			startActivity(homeIntent);
			finish();
		}
	};



}
