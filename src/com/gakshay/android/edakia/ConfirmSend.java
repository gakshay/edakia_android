package com.gakshay.android.edakia;

import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

import com.gakshay.android.util.ActivitiesHelper;
import com.gakshay.android.util.NetworkOperations;

public class ConfirmSend extends BaseActivity {

	private ProgressDialog progressDialog;
	private String senderMobile;
	private String senderPassword;
	private String receiverMobile;
	private String sendResponse;
	private String file;
	private String userId;
	private String serialNumber;
	private String receiverEmailAddress;
	private String sendURL;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_send);
		StrictMode.ThreadPolicy policy = new StrictMode.
				ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		prepareConfirmSendDialogBox();
		sendURL = this.getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("sendURL","http://defaultURL");
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


	private void sendFileToUser(boolean showProcessDialog) {
		if(showProcessDialog)
			progressDialog = ProgressDialog.show(this, "",getString(R.string.sendDocPrgDlg) );
		new Thread() {
			public void run() {
				InputStream in = null;
				Message msg = Message.obtain();
				try {
					sendResponse = NetworkOperations.sendToEdakiaServer(sendURL, senderMobile, senderPassword,receiverMobile,file,userId,serialNumber,receiverEmailAddress);
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
