package com.gakshay.android.edakia;

import java.io.BufferedOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gakshay.android.util.ActivitiesHelper;
import com.gakshay.android.util.CustomizedThread;
import com.gakshay.android.util.NetworkOperations;
import com.gakshay.android.validation.Validator;


public class ReceiveActivity extends BaseActivity {

	private ProgressDialog progressDialog;	
	private Bitmap bitmap;
	private String documentName;
	private String documentPath;
	private EditText mobile;
	private EditText secretCode;
	private EditText receiverEmailAddress;
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
		//Either provide mobile no. or email address not both.
		if(receiverEmailAddress.getText().toString() != null && !"".equalsIgnoreCase(receiverEmailAddress.getText().toString())
				&& mobile.getText().toString() != null && !"".equalsIgnoreCase(mobile.getText().toString())){

			Toast.makeText(this, "Please provide single input,either email address or mobile no.", Toast.LENGTH_LONG).show();
			return;
		}
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
				+ "&serial_number=" + "sdfdsfdsf324sdfsdfds324324";	
		return edakiaURL;
	}


	/*private void downloadImage(final String reqURL, final String documentPath, final boolean showProcessingDialog,final String processingMsg) {
		if(showProcessingDialog)
			progressDialog = ProgressDialog.show(this, "", 
					"Downloading Your Document \n  ................" );
		final String url = urlStr;

		new Thread() {
			public void run() {
				InputStream in = null;
				Message msg = Message.obtain();
				msg.what = 1;
				try {
					in = openHttpConnection(url);
					bitmap = BitmapFactory.decodeStream(in);
					Bundle b = new Bundle();
					b.putParcelable("bitmap", bitmap);
					msg.setData(b);
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				messageHandler.sendMessage(msg);					
			}
		}.start();
	}
	 */

	private void readAndDownloadDocument(final String reqURL) {
		progressDialog = ProgressDialog.show(this, "", 
				"Processing Your request \n  ................" );

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

	/*private void downloadText(final String reqURL, final String documentPath, final boolean showProcessingDialog,final String processingMsg) {

		if(showProcessingDialog)
			progressDialog = ProgressDialog.show(this, "", 
					processingMsg);

		new CustomizedThread(showProcessingDialog) {
			public void run() {
				InputStream in = null;
				Message msg = Message.obtain();
				msg.what=2;
				try {
					in = openHttpConnection(reqURL);
					InputStreamReader isr = new InputStreamReader(in);
					int charRead;
					String text = "";
					char[] inputBuffer = new char[BUFFER_SIZE];
					File outFile = new File("/mnt/sdcard/" +  documentName);
					FileWriter out = new FileWriter(outFile);

					while ((charRead = isr.read(inputBuffer))>0)
					{      
						if(showProcessingDialog){
							out.write(inputBuffer, 0, charRead);
						}
						String readString = 
								String.copyValueOf(inputBuffer, 0, charRead);                    
						text += readString;
						inputBuffer = new char[BUFFER_SIZE];
					}
					//out.flush();
					out.close();
					Bundle b = new Bundle();
					b.putString("text", text);
					msg.setData(b);
					isr.close();
					in.close();

				}catch (IOException e2) {
					e2.printStackTrace();
				}
				if(showProcessingDialog)
					messageHandler.sendMessage(msg);
				else{
					messageHandlerForXPath.sendMessage(msg);
				}
			}
		}.start();    
	}


	 */




	private Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(documentPath)));
			switch (msg.what) {
			case 1:
				progressDialog.dismiss();
				try {
					//save the data into a file.

					OutputStream outStream = null;
					File file = new File(Environment.getExternalStorageDirectory(), documentName);
					outStream = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
					outStream.flush();
					outStream.close();

					documentPath =Environment.getExternalStorageDirectory()+ "/" + documentName;
					try {
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setPackage("com.dynamixsoftware.printershare");
						i.setDataAndType(Uri.fromFile(new File(documentPath)), mimeType);
						startActivity(i);


					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(ReceiveActivity.this, "Got some exception while trying to invoke printer share App !!  \n Going to Home Page" , Toast.LENGTH_LONG).show();
						startActivity((new Intent(ReceiveActivity.this, Edakia.class)));

					}



					//commenting below code as to avoid internal print Activity call.
					/*Intent intent = new Intent(ReceiveActivity.this,PrintActivity.class);
					intent.setAction(Intent.ACTION_SEND);
					intent.setData(Uri.parse(documentPath));
					intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(documentPath));
					intent.setType("image/jpeg");

					startActivity(intent);
					 */
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				break;
			case 2:

				progressDialog.dismiss();
				try {
					/*File outFile = new File(Environment.getExternalStorageDirectory(), documentName);
					FileWriter out = new FileWriter(outFile);
					//		out.write
					Log.d("valu of file text ", msg.getData().getString("text"));
					out.write(msg.getData().getString("text"));
					out.flush();
					out.close();*/

					/*FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), documentName));
					fos.write(((String)msg.getData().getString("text")).getBytes());
					fos.flush();
					fos.close();*/
					documentPath =Environment.getExternalStorageDirectory()+ "/" + documentName;

					try {
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setPackage("com.dynamixsoftware.printershare");
						i.setDataAndType(Uri.fromFile(new File(documentPath)), mimeType);
						startActivity(i);


					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(ReceiveActivity.this, "Got some exception while trying to invoke printer share App !! ", Toast.LENGTH_LONG).show();
						startActivity((new Intent(ReceiveActivity.this, Edakia.class)));


					}

					//commenting below code as to avoid internal print Activity call.

					/*documentPath =Environment.getExternalStorageDirectory()+ "/" + documentName;
					Intent intent = new Intent(ReceiveActivity.this,PrintActivity.class);
					intent.setAction(Intent.ACTION_SEND);
					intent.setData(Uri.parse(documentPath));
					intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(documentPath));
					intent.setType("text/plain");

					startActivity(intent);*/
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				break;
			}

		}
	};


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

					documentName = (documentPath.split("/"))[documentPath.split("/").length-1];
					//Fetch document.
					boolean isFileCreated;
					if(documentPath.contains(".png") || documentPath.contains(".jpeg") || documentPath.contains(".jpg") || documentPath.contains(".gif")){
						//downloadImage(documentPath, true);
						isFileCreated = NetworkOperations.readAndCreateImageDocumentFromEdakia(documentPath, Environment.getExternalStorageDirectory()+ "/" + documentName);
					}else {
						isFileCreated = NetworkOperations.readAndCreateAnyDocumentFromEdakia(documentPath, Environment.getExternalStorageDirectory()+ "/" + documentName);
					}
					if(isFileCreated){
						String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(documentPath)));

						try {
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setPackage("com.dynamixsoftware.printershare");
							i.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+ "/" + documentName)), mimeType);
							startActivity(i);


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


		//Validate mobile no.
		int valStatusCode = Validator.validateMobileNumber(mobile.getText().toString()).ordinal();
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
		}else{//send empty email address
			//receiverEmailAddress.setText("");
		}
		if(valStatusCode == 0)
			isValid = true;
		return isValid;	
	}

}
