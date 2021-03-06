package com.gakshay.android.edakia;

import java.io.File;
import java.io.IOException;

import com.gakshay.android.util.ActivitiesHelper;
import com.gakshay.android.validation.Validator;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.ContactsContract.Directory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SendActivity extends BaseActivity {
	private String senderMobile;
	private String senderPassword;
	private EditText receiverMobile;
	private EditText receiverEmailAddress;
	private static final int FILE_SELECTED_STATUS = 1;
	private static final int CONFIRM_SEND_STATUS = 2;
	private static final int SCANNED_FILE_SELECTED_STATUS = 3;
	private static final int ACTIVITY_CHOOSE_FILE = 4;
	private FileObserver fileObserver;
	private String scannedFile;
	private String userId;
	private String fileObserverPath ;
	private Button selectedSendButton; 



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send);
		((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
		((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.INVISIBLE);

		enableKeyBoard(((EditText) findViewById(R.id.receiverMobile)),Boolean.parseBoolean(this.getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("enableKeyBoard","true")));
		enableKeyBoard(((EditText) findViewById(R.id.receiverEmail)),Boolean.parseBoolean(this.getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("enableKeyBoard","true")));

		fileObserverPath = Environment.getExternalStorageDirectory().getAbsolutePath() +  
				this.getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("fileObserverPath","/mnt/storage/");
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
			sendIntent.putExtra("userId", userId);
			sendIntent.putExtra("serialNumber", getSerialNumber());
			sendIntent.putExtra("receiverEmail", receiverEmailAddress.getText().toString());

			String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(scannedFile)));
			sendIntent.setType(mimeType);
			startActivityForResult(sendIntent,CONFIRM_SEND_STATUS);
		}else if (requestCode == CONFIRM_SEND_STATUS && resultCode == RESULT_OK){
			if(data != null && "killParentActivity".equalsIgnoreCase(data.getStringExtra("whichAction"))){
				finish();
			}
		} else if (requestCode  == ACTIVITY_CHOOSE_FILE && resultCode == RESULT_OK){
			Uri uri = data.getData();
			scannedFile = uri.getPath();
			Toast.makeText(this, "File :  " + scannedFile, Toast.LENGTH_LONG).show();
			Intent sendIntent = new Intent(SendActivity.this, ConfirmSend.class);
			sendIntent.putExtra("sendMobile", senderMobile);
			sendIntent.putExtra("sendPassword", senderPassword);
			sendIntent.putExtra("receiverMobile", receiverMobile.getText().toString());
			sendIntent.putExtra("file", scannedFile);
			sendIntent.putExtra("userId", userId);
			sendIntent.putExtra("serialNumber", getSerialNumber());
			sendIntent.putExtra("receiverEmail", receiverEmailAddress.getText().toString());

			String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(scannedFile)));
			sendIntent.setType(mimeType);
			startActivityForResult(sendIntent,CONFIRM_SEND_STATUS);
		}
	}

	public void popupInputBox(View aview) {
		CheckBox popUpNumber = ((CheckBox) findViewById(R.id.popupValue));
		if(popUpNumber != null && popUpNumber.isChecked()){
			receiverMobile = ((EditText) findViewById(R.id.receiverMobile));
			Intent intent = getIntent();
			Bundle bundleData = intent.getExtras();

			if(bundleData != null){
				senderMobile =(String) bundleData.get("sendMobile");
			}

			if((senderMobile == null || "".equalsIgnoreCase(senderMobile)) && ((EditText) findViewById(R.id.senderMobNum)) != null){
				senderMobile = ((EditText) findViewById(R.id.senderMobNum)).getText().toString();
			}
			receiverMobile.setText(senderMobile);
		} else if(popUpNumber != null && !popUpNumber.isChecked()){
			receiverMobile.setText(null);
			((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
		}

	}		

	// Will be connected with the buttons via XML
	public void sendFile(View aview) {
		if(!isNetworkConnection()){
			Intent edakiaHome = initiateHomePage(true, getString(R.string.errorDialogInternetNotAvailable));
			startActivity(edakiaHome);
			finish();
			return;
		}
		receiverMobile = ((EditText) findViewById(R.id.receiverMobile));
		receiverEmailAddress = ((EditText) findViewById(R.id.receiverEmail));
		selectedSendButton = (Button)aview;

		Intent intent = getIntent();
		Bundle bundleData = intent.getExtras();
		if(bundleData != null){
			senderMobile =(String) bundleData.get("sendMobile");
			senderPassword =(String) bundleData.get("sendPassword");
			userId =(String) bundleData.get("userId");
		}


		if((senderMobile == null || "".equalsIgnoreCase(senderMobile)) && ((EditText) findViewById(R.id.senderMobNum)) != null){
			senderMobile = ((EditText) findViewById(R.id.senderMobNum)).getText().toString();
		}

		if(validateInputData()){
			//set success images to fields
			((ImageView)findViewById(R.id.errImgEmail)).setImageResource(R.drawable.ic_success);
			((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_success);


			if(selectedSendButton.getTag().toString().equalsIgnoreCase("scanFile")){//File Explorer.	
				//prepare the document folder for this user.
				prepareThisUserDocumentFolder();
				//scan the document using app.
				try {
					intent = new Intent();
					intent.setComponent(ComponentName.unflattenFromString(getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("cannonScanActivity","/default/activity")));
					intent.setAction(Intent.ACTION_VIEW);
					startActivityForResult(intent, SCANNED_FILE_SELECTED_STATUS);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
				scanFileLookup();
			}else if (selectedSendButton.getTag().toString().equalsIgnoreCase("searchFile")){
				//Initiate file selection activity(Android internal activity)
				Intent chooseFile;
				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFile.setType("file/*");
				intent = Intent.createChooser(chooseFile, "Choose a file");
				startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
			}else if (selectedSendButton.getTag().toString().equalsIgnoreCase("EdakiaAccount")) {
				Toast.makeText(this, "Development in progress.....", Toast.LENGTH_LONG).show();
			}else {
				Toast.makeText(this, "You Clicked on wrong option.", Toast.LENGTH_LONG).show();

			}
		}


	}

	private void prepareThisUserDocumentFolder(){
		try {
			ActivitiesHelper.deleteContentOfFile(new File(fileObserverPath));//delete existing any pdf files.
			//ActivitiesHelper.deleteContentOfFile(new File(fileObserverPath+"/scan_image"));//delete existing any scan files.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void scanFileLookup(){
		fileObserver = new FileObserver(fileObserverPath+"/", FileObserver.MOVED_TO) {
			@Override
			public void onEvent(int event, String observedFile) {
				try {
					Log.d("FileObserver", "event "+ event);
					if(event == FileObserver.MOVED_TO){

						fileObserver.stopWatching();

						stopService(getIntent());

						//finishActivity(SCANNED_FILE_SELECTED_STATUS);
						//scannedFile = scanfile.listFiles()[0].getAbsolutePath();
						scannedFile =  fileObserverPath+"/"+observedFile;
						Log.d("file observed is ", scannedFile);
						Intent sendIntent = new Intent(SendActivity.this, ConfirmSend.class);
						sendIntent.putExtra("sendMobile", senderMobile);
						sendIntent.putExtra("sendPassword", senderPassword);
						sendIntent.putExtra("receiverMobile", receiverMobile.getText().toString());
						sendIntent.putExtra("file", scannedFile);
						sendIntent.putExtra("userId", userId);
						sendIntent.putExtra("serialNumber", getSerialNumber());
						sendIntent.putExtra("receiverEmail", receiverEmailAddress.getText().toString());
						String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(scannedFile)));
						sendIntent.setType(mimeType);
						Thread.sleep(3000);
						startActivityForResult(sendIntent,CONFIRM_SEND_STATUS);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		fileObserver.startWatching();
		startService(getIntent());
	}


	private boolean validateInputData(){
		boolean isValid = false;
		TextView text = (TextView) findViewById(R.id.Error);
		text.setText(null);
		text.setVisibility(TextView.INVISIBLE);

		((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.VISIBLE);
		((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.VISIBLE);
		//Either provide mobile no. or email address not both.
		if(receiverEmailAddress.getText().toString() != null && !"".equalsIgnoreCase(receiverEmailAddress.getText().toString())
				&& receiverMobile.getText().toString() != null && !"".equalsIgnoreCase(receiverMobile.getText().toString())){

			//Toast.makeText(this, "Please provide single input,either email address or mobile no.", Toast.LENGTH_LONG).show();
			text.setText(getString(R.string.both_receiver_email_mobile));
			text.setVisibility(TextView.VISIBLE);
			return false;
		}

		//validate sender mobile number
		//Validate mobile no.
		int valStatusCode = Validator.validateMobileNumber(senderMobile).ordinal();
		switch(valStatusCode){
		case 1:
			//Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show();
			text.setText(getString(R.string.empty_sender_mobile));
			text.setVisibility(TextView.VISIBLE);
			if(((EditText) findViewById(R.id.senderMobNum)) != null){
				((EditText) findViewById(R.id.senderMobNum)).findFocus();
				((ImageView)findViewById(R.id.errImgSenderMobNum)).setImageResource(R.drawable.ic_error);
			}
			return false;
		case 2:
			//Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show();
			text.setText(getString(R.string.invalid_sender_mobile));
			text.setVisibility(TextView.VISIBLE);
			if(((EditText) findViewById(R.id.senderMobNum)) != null){
				((EditText) findViewById(R.id.senderMobNum)).findFocus();
				((ImageView)findViewById(R.id.errImgSenderMobNum)).setImageResource(R.drawable.ic_error);
			}
			return false;	

		}


		if((receiverEmailAddress.getText().toString() == null || "".equalsIgnoreCase(receiverEmailAddress.getText().toString()))
				&& (receiverMobile.getText().toString() == null || "".equalsIgnoreCase(receiverMobile.getText().toString()))){
			((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_error);
			((ImageView)findViewById(R.id.errImgEmail)).setImageResource(R.drawable.ic_error);
			//Toast.makeText(this, "Please provide any input,either email address or mobile no.", Toast.LENGTH_LONG).show();
			text.setText(getString(R.string.none_receiver_email_mobile));
			text.setVisibility(TextView.VISIBLE);
			return false;
		}
		valStatusCode = -5;
		if((receiverEmailAddress.getText() == null || "".equalsIgnoreCase(receiverEmailAddress.getText().toString()) || receiverEmailAddress.getText().toString().length() == 0)
				&& (receiverMobile.getText().toString() != null && !"".equalsIgnoreCase(receiverMobile.getText().toString()) && receiverMobile.getText().toString().length() != 0)){

			((ImageView)findViewById(R.id.errImgEmail)).setVisibility(ImageView.INVISIBLE);

			//Validate mobile no.
			valStatusCode = Validator.validateMobileNumber(receiverMobile.getText().toString()).ordinal();
			switch(valStatusCode){
			case 1:
				//Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show();
				text.setText(getString(R.string.empty_receiver_mobile));
				text.setVisibility(TextView.VISIBLE);
				receiverMobile.findFocus();
				((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_error);
				return false;
			case 2:
				//Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show();
				text.setText(getString(R.string.invalid_receiver_mobile));
				text.setVisibility(TextView.VISIBLE);
				receiverMobile.findFocus();
				((ImageView)findViewById(R.id.errImgMob)).setImageResource(R.drawable.ic_error);
				return false;	

			}

		}


		if((receiverMobile.getText() == null || "".equalsIgnoreCase(receiverMobile.getText().toString()) || receiverMobile.getText().toString().length() == 0)
				&& (receiverEmailAddress.getText().toString() != null && !"".equalsIgnoreCase(receiverEmailAddress.getText().toString()) && receiverEmailAddress.getText().toString().length() != 0)){

			((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);

			//Validate email address.
			valStatusCode = Validator.validateEmailAddress(receiverEmailAddress.getText().toString()).ordinal();
			switch (valStatusCode) {
			case 7:
				//Toast.makeText(this, "Incorrect Email Address",Toast.LENGTH_LONG).show();
				text.setText(getString(R.string.invalid_receiver_email));
				text.setVisibility(TextView.VISIBLE);
				receiverEmailAddress.findFocus();
				((ImageView)findViewById(R.id.errImgEmail)).setImageResource(R.drawable.ic_error);

				return false;
			}
		}

		//Validate radio button selected.
		if(selectedSendButton == null || selectedSendButton.getTag() == null || "".equalsIgnoreCase(selectedSendButton.getTag().toString()))
			return false;

		if(valStatusCode == 0)
			isValid = true;
		return isValid;	
	}

}