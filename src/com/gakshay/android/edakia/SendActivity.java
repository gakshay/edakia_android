package com.gakshay.android.edakia;

import java.io.File;

import com.gakshay.android.validation.Validator;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SendActivity extends Activity {
	private String senderMobile;
	private String senderPassword;
	private EditText receiverMobile;
	private static final int FILE_SELECTED_STATUS = 1;
	private static final int CONFIRM_SEND_STATUS = 2;
	private static final int SCANNED_FILE_SELECTED_STATUS = 3;
	private FileObserver aFileobsFileObserver;
	private String scannedFile;
	private String fileObserverPath = "/mnt/storage/CanonEPP/scan_pdf";
	private RadioButton selectedRadioButton; 



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
		if (requestCode == FILE_SELECTED_STATUS && resultCode == RESULT_OK) {
			
				// Get the path set from file chooser.
				scannedFile = data.getStringExtra("filePath");
				Intent sendIntent = new Intent(SendActivity.this, ConfirmSend.class);
				sendIntent.putExtra("sendMobile", senderMobile);
				sendIntent.putExtra("sendPassword", senderPassword);
				sendIntent.putExtra("receiverMobile", receiverMobile.getText().toString());
				sendIntent.putExtra("file", scannedFile);
				if(scannedFile != null && (scannedFile.contains("jpeg")  || scannedFile.contains("jpg") || scannedFile.contains("png"))){
					sendIntent.setType("image/jpeg");
					
				}else{
					sendIntent.setType("application/pdf");
				}
				startActivityForResult(sendIntent,CONFIRM_SEND_STATUS);
		}else if (requestCode == CONFIRM_SEND_STATUS && resultCode == RESULT_OK){
			
		}
	}


	// Will be connected with the buttons via XML
	public void sendFile(View aview) {
		receiverMobile = ((EditText) findViewById(R.id.receiverMobile));

		RadioGroup selectFileGroup = (RadioGroup)findViewById(R.id.selectFileGroup);
		int selectedRadioButtonId = selectFileGroup.getCheckedRadioButtonId();
		selectedRadioButton = (RadioButton)selectFileGroup.findViewById(selectedRadioButtonId);

		if(validateInputData()){
			Intent intent = getIntent();
			Bundle bundleData = intent.getExtras();
			senderMobile =(String) bundleData.get("sendMobile");
			senderPassword =(String) bundleData.get("sendPassword");
			if(selectedRadioButton.getTag().toString().equalsIgnoreCase("ScanAndSend")){//File Explorer.	
				//prepare the document folder for this user.
				prepareThisUserDocumentFolder();
				//scan the document using app.
				try {
					intent = new Intent();
					intent.setComponent(ComponentName.unflattenFromString("jp.co.canon.bsd.android.aepp.activity/jp.co.canon.bsd.android.aepp.activity.ScannerMainActivity"));
					intent.setAction(Intent.ACTION_VIEW);
					startActivityForResult(intent, SCANNED_FILE_SELECTED_STATUS);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
				scanFileLookup();
			}else if (selectedRadioButton.getTag().toString().equalsIgnoreCase("LocalFile")){
				// "/mnt/sdcard/logo.jpg"
				Intent fileSelectActivity = new Intent(this,FileSelectionActivity.class);  
				startActivityForResult(fileSelectActivity,FILE_SELECTED_STATUS);
			}else if (selectedRadioButton.getTag().toString().equalsIgnoreCase("EdakiaAccount")) {
				Toast.makeText(this, "Development in progress.....", Toast.LENGTH_LONG).show();

			}else {
				Toast.makeText(this, "You Clicked on wrong option.", Toast.LENGTH_LONG).show();

			}
		}

		
	}

	private void prepareThisUserDocumentFolder(){
		File aScanPDFDir = new File(fileObserverPath);
		Toast.makeText(this, "Preparing user Document", Toast.LENGTH_SHORT).show();
		if(aScanPDFDir.exists() && aScanPDFDir.isDirectory() && aScanPDFDir.list().length != 0){//rename this directory name.
			File aTempFile =null;
			File[] aTempFilesCollection = aScanPDFDir.listFiles();
			for(int i = 0; i < aTempFilesCollection.length; i++){
				aTempFile = aTempFilesCollection[i];
				aTempFile.delete();
			}
		}

	}
	
	

	private void scanFileLookup(){
		///mnt/storage/CanonEPP/scan_pdf
		Log.d("FileObserver", "Inside scan file lookup");
		aFileobsFileObserver = new FileObserver(fileObserverPath + "/", FileObserver.MOVED_TO) {
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

						File scanfile = new File(fileObserverPath);
						scannedFile = scanfile.listFiles()[0].getAbsolutePath();
						Intent sendIntent = new Intent(SendActivity.this, ConfirmSend.class);
						sendIntent.putExtra("sendMobile", senderMobile);
						sendIntent.putExtra("sendPassword", senderPassword);
						sendIntent.putExtra("receiverMobile", receiverMobile.getText().toString());
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
	
	
	private boolean validateInputData(){
		boolean isValid = false;
		TextView text = (TextView) findViewById(R.id.Error);
		text.setText(null);

		//Validate mobile no.
				int valStatusCode = Validator.validateMobileNumber(receiverMobile.getText().toString()).ordinal();
				switch(valStatusCode){
				case 1:
					Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show();
					text.setText("You missed mobile number. Plz enter the same.");
					receiverMobile.findFocus();
					return false;
				case 2:
					Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show();
					text.setText("You entered incorrect mobile number. Plz correct the same.");
					receiverMobile.findFocus();
					return false;		
				}

				//Validate radio button selected.
				if(selectedRadioButton == null || selectedRadioButton.getTag() == null || "".equalsIgnoreCase(selectedRadioButton.getTag().toString()))
					return false;
				
				if(valStatusCode == 0)
					 isValid = true;
			return isValid;	
	}

}