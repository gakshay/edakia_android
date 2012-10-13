package com.gakshay.android.edakia;

import java.io.File;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gakshay.android.util.ActivitiesHelper;
import com.gakshay.android.util.NetworkOperations;
import com.gakshay.android.validation.Validator;


public class ReceiveActivity extends BaseActivity {

	private ProgressDialog progressDialog;	
	private String documentName;
	private String documentPath;
	private EditText mobile;
	private EditText secretCode;
	private EditText receiverEmailAddress;
	private String docTransCost;
	private static final int PRINT_ACTIVITY = 1;
	private String localEdakiaDocStorage = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EdakiaDocs/ReceiveDocs/";
	private String authURL = "http://staging.edakia.in/api/transactions/receive.xml";//"http://edakia.in/transactions/receive.xml";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receive);
		StrictMode.ThreadPolicy policy = new StrictMode.
				ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
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

		mobile = (EditText) findViewById(R.id.receiveMobile);
		secretCode = (EditText) findViewById(R.id.secretCode);
		receiverEmailAddress = (EditText) findViewById(R.id.receiverEmail);

		if(validateInputData()){
			try {// Process the request further.
				readAndDownloadDocument(prepareEdakiaURL(mobile,secretCode,receiverEmailAddress));			
			}
			catch (Exception e) {
				//handle the exception !
				e.printStackTrace();
			}
		}


	}

	private String prepareEdakiaURL(EditText mobile, EditText secretCode, EditText emailAddress){
		String edakiaURL = null;
		//sample URL 
		//http://staging.edakia.in/api/transactions/receive.xml?transaction[receiver_mobile]=<sender>&transaction[receiver_email]=<email>&transaction[document_secret]=<secret_code>&serial_number=<serial_number>
		edakiaURL = authURL + "?transaction[receiver_mobile]=" + mobile.getText() + "&transaction[document_secret]="+secretCode.getText()
				+ "&transaction[receiver_email]="+emailAddress.getText().toString() 
				+ "&serial_number=" +getSerialNumber();	
		return edakiaURL;
	}

	private void readAndDownloadDocument(final String reqURL) {
		progressDialog = ProgressDialog.show(this, "", getString(R.string.receiveDocPrgDlg) );

		new Thread() {
			public void run() {
				Message msg = Message.obtain();
				msg.what = 1;
				try {
					Bundle b = new Bundle();
					b.putString("response", NetworkOperations.readXMLResponseFromEdakia(reqURL));
					msg.setData(b);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				messageHandlerForXPath.sendMessage(msg);					
			}
		}.start();
	}

	private boolean prepareThisUserDocumentFolder(){
		boolean hasPrepared=false;
		
		try {
			File edakiaDocsHome = new File(localEdakiaDocStorage);
			if(edakiaDocsHome.exists() && edakiaDocsHome.isDirectory()){
				ActivitiesHelper.deleteContentOfFile(new File(localEdakiaDocStorage));
			}else{
				edakiaDocsHome.mkdirs();
			}
			hasPrepared = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hasPrepared;
	}

	private Handler messageHandlerForXPath = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String responseXPath = msg.getData().getString("response");
			//Fetch value of document x path from returned XML string response.
			if(responseXPath != null && !"".equalsIgnoreCase(responseXPath) && !responseXPath.contains("error")){
				progressDialog.dismiss();	
				progressDialog = ProgressDialog.show(ReceiveActivity.this, "","Downloading Your document \n  ................" );
				try{
					documentPath = (ActivitiesHelper.fetchValuesFromReponse(responseXPath)).get("document_url");
					docTransCost = (ActivitiesHelper.fetchValuesFromReponse(responseXPath)).get("cost");
					documentName = (documentPath.split("/"))[documentPath.split("/").length-1];
					if(!prepareThisUserDocumentFolder()){
						throw new Exception("Error while preparing directories for user.");
					}
					//Fetch document.
					boolean isFileCreated;
					if(documentPath.contains(".png") || documentPath.contains(".jpeg") || documentPath.contains(".jpg") || documentPath.contains(".gif")){
						//downloadImage(documentPath, true);
						isFileCreated = NetworkOperations.readAndCreateImageDocumentFromEdakia(documentPath, localEdakiaDocStorage + documentName);
					}else {
						isFileCreated = NetworkOperations.readAndCreateAnyDocumentFromEdakia(documentPath, localEdakiaDocStorage + documentName);
					}
					if(isFileCreated){
						String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(documentPath)));

						try {
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setPackage("com.dynamixsoftware.printershare");
							i.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+ "/" + documentName)), mimeType);
							startActivityForResult(i,PRINT_ACTIVITY);


						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Toast.makeText(ReceiveActivity.this, "Got some exception while trying to invoke printer share App !!  \n Going to Home Page" , Toast.LENGTH_LONG).show();
							startActivity((new Intent(ReceiveActivity.this, Edakia.class)));
						}
						progressDialog.dismiss();
					}else{
						progressDialog.dismiss();
						Toast.makeText(ReceiveActivity.this, "Sorry !! We could not find your document due to some internal error. Please bear with us for some time to serve you again.", Toast.LENGTH_LONG).show();	
					}

				}catch(Exception anExcep){
					progressDialog.dismiss();
					anExcep.printStackTrace();
					Toast.makeText(ReceiveActivity.this, "Sorry !! We could not find your document due to some internal error. Please bear with us for some time to serve you again.", Toast.LENGTH_LONG).show();
				}
			}else if(responseXPath.contains("error") && responseXPath.contains("Document not found")){
				progressDialog.cancel();
				progressDialog.dismiss();
				//could not find any document with this.
				Toast.makeText(ReceiveActivity.this, "Sorry !! We could not find document matching this secret code & mobile number. \n Please make sure you entered correct inputs.", Toast.LENGTH_LONG).show();
			}
			else {
				progressDialog.dismiss();
				progressDialog.cancel();
				//some other error.
				Toast.makeText(ReceiveActivity.this, "Sorry !! We could not find your document due to some internal error. Please bear with us for some time to serve you again.", Toast.LENGTH_LONG).show();
			}
		}
	};

	private boolean validateInputData(){
		boolean isValid = false;
		//initialize error text value to null.

		TextView text = (TextView) findViewById(R.id.Error);
		text.setText(null);
		//Either provide mobile no. or email address not both.
		if(receiverEmailAddress.getText().toString() != null && !"".equalsIgnoreCase(receiverEmailAddress.getText().toString())
				&& mobile.getText().toString() != null && !"".equalsIgnoreCase(mobile.getText().toString())){

			Toast.makeText(this, "Please provide single input,either email address or mobile no.", Toast.LENGTH_LONG).show();
			return false;
		}

		if((receiverEmailAddress.getText().toString() == null || "".equalsIgnoreCase(receiverEmailAddress.getText().toString()))
				&& (mobile.getText().toString() == null || "".equalsIgnoreCase(mobile.getText().toString()))){

			Toast.makeText(this, "Please provide inputs,either email address or mobile no. with secret code.", Toast.LENGTH_LONG).show();
			return false;
		}
		int valStatusCode = -5;
		if((receiverEmailAddress.getText() == null || "".equalsIgnoreCase(receiverEmailAddress.getText().toString()) || receiverEmailAddress.getText().toString().length() == 0)
				&& (mobile.getText().toString() != null && !"".equalsIgnoreCase(mobile.getText().toString()) && mobile.getText().toString().length() != 0)){
			//Validate mobile no.
			valStatusCode = Validator.validateMobileNumber(mobile.getText().toString()).ordinal();
			switch(valStatusCode){
			case 1:
				Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show();
				text.setText("You missed mobile number. Plz enter the same.");
				mobile.findFocus();
				return false;
			case 2:
				Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show();
				text.setText("You entered incorrect mobile number. Plz correct the same.");
				mobile.findFocus();
				return false;		
			}

		}


		if((mobile.getText() == null || "".equalsIgnoreCase(mobile.getText().toString()) || mobile.getText().toString().length() == 0)
				&& (receiverEmailAddress.getText().toString() != null && !"".equalsIgnoreCase(receiverEmailAddress.getText().toString()) && receiverEmailAddress.getText().toString().length() != 0)){
			if (receiverEmailAddress != null && receiverEmailAddress.getText() != null && !"".equalsIgnoreCase(receiverEmailAddress.getText().toString())) {
				//Validate email address.
				valStatusCode = Validator.validateEmailAddress(receiverEmailAddress.getText().toString()).ordinal();
				switch (valStatusCode) {
				case 7:
					Toast.makeText(this, "Incorrect Email Address",Toast.LENGTH_LONG).show();
					text.setText("You entered incorrect email address. Plz correct the same.");
					receiverEmailAddress.findFocus();
					return false;
				}
			}
		}

		//Validate secret no.
		valStatusCode = Validator.validateSecretNumber(secretCode.getText().toString()).ordinal();
		switch(valStatusCode){
		case 3:
			Toast.makeText(this, "Enter Secret Number", Toast.LENGTH_LONG).show();
			text.setText("You missed secret number. Plz enter the same.");
			secretCode.findFocus();
			return false;
		case 4:
			Toast.makeText(this, "Incorrect Secret Code", Toast.LENGTH_LONG).show();
			text.setText("You entered incorrect secret number. Plz correct the same");
			secretCode.findFocus();
			return false;					
		}


		if(valStatusCode == 0)
			isValid = true;
		return isValid;	
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == PRINT_ACTIVITY){
			if(resultCode == RESULT_OK){
				Intent homeIntent = new Intent(getApplicationContext(), Edakia.class);
				homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Toast.makeText(this, "Please recieve your document.", Toast.LENGTH_LONG).show();
				homeIntent.putExtra("showCostDialogBox", "true");
				homeIntent.putExtra("transactionType", "received");
				homeIntent.putExtra("transactionCost", docTransCost);
				startActivity(homeIntent);
				finish();
			}else{
				Intent homeIntent = new Intent(getApplicationContext(), Edakia.class);
				homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				homeIntent.putExtra("showCostDialogBox", "false");
				Toast.makeText(this, "Due to some temporary error, could not receive your document.", Toast.LENGTH_LONG).show();
				homeIntent.putExtra("showCostDialogBox", "false");
				finish();
			}

		}
	}

}
