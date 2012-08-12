package com.gakshay.android.edakia;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gakshay.android.util.CustomizedThread;
import com.gakshay.android.validation.Validator;


public class ReceiveActivity extends Activity {

	private ProgressDialog progressDialog;	
	private Bitmap bitmap;
	private String documentName;
	private String documentPath;

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

		EditText mobile = (EditText) findViewById(R.id.receiveMobile);
		EditText secretCode = (EditText) findViewById(R.id.secretCode);

		//initialize error text value to null.
		TextView text = (TextView) findViewById(R.id.textView3);


		//Validate mobile no.
		int valStatusCode = Validator.validateMobileNumber(mobile).ordinal();
		switch(valStatusCode){
		case 1:
			Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show();
			text.setText("You missed mobile number. Plz enter the same.");
			mobile.findFocus();
			return;
		case 2:
			Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show();
			text.setText("You entered incorrect mobile number. Plz correct the same.");
			mobile.findFocus();
			return;		
		}

		//Validate secret no.
		valStatusCode = Validator.validateSecretNumber(secretCode).ordinal();
		switch(valStatusCode){
		case 3:
			Toast.makeText(this, "Enter Secret Number", Toast.LENGTH_LONG).show();
			text.setText("You missed secret number. Plz enter the same.");
			secretCode.findFocus();
			return;
		case 4:
			Toast.makeText(this, "Incorrect Secret Code", Toast.LENGTH_LONG).show();
			text.setText("You entered incorrect secret number. Plz correct the same");
			secretCode.findFocus();
			return;					
		}

		try {// Process the request further.
			downloadText(prepareEdakiaURL(mobile,secretCode),false);			
		}
		catch (Exception e) {
			//handle the exception !
			e.printStackTrace();
		}
	}

	private String prepareEdakiaURL(EditText mobile, EditText secretCode){
		String edakiaURL = null;
		edakiaURL = "http://edakia.in/transactions/receive.xml?transaction[receiver_mobile]=" + mobile.getText() + "&transaction[document_secret]="+secretCode.getText();	
		return edakiaURL;
	}


	private void downloadImage(String urlStr,boolean showProcessingDialog) {
		if(showProcessingDialog)
			progressDialog = ProgressDialog.show(this, "", 
					"Downloading Your Document \n प्राप्त हो रहा है \n ................" );
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

	private void downloadText(String urlStr, final boolean showProcessingDialog) {

		if(showProcessingDialog)
			progressDialog = ProgressDialog.show(this, "", 
					"Downloading Your Document \n प्राप्त हो रहा है \n ................" + urlStr);
		final String url = urlStr;

		new CustomizedThread(showProcessingDialog) {
			public void run() {
				int BUFFER_SIZE = 2000;
				InputStream in = null;
				Message msg = Message.obtain();
				msg.what=2;
				try {
					in = openHttpConnection(url);

					InputStreamReader isr = new InputStreamReader(in);
					int charRead;
					String text = "";
					char[] inputBuffer = new char[BUFFER_SIZE];

					while ((charRead = isr.read(inputBuffer))>0)
					{                    
						String readString = 
								String.copyValueOf(inputBuffer, 0, charRead);                    
						text += readString;
						inputBuffer = new char[BUFFER_SIZE];
					}
					Bundle b = new Bundle();
					b.putString("text", text);
					msg.setData(b);
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


	private InputStream openHttpConnection(String urlStr){
		InputStream in = null;
		int resCode = -1;

		try {
			URL url = new URL(urlStr);
			URLConnection urlConn = url.openConnection();

			if (!(urlConn instanceof HttpURLConnection)) {
				throw new IOException ("URL is not an Http URL");
			}

			HttpURLConnection httpConn = (HttpURLConnection)urlConn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect(); 

			resCode = httpConn.getResponseCode();                 
			if (resCode == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();                                 
			}         
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return in;
	}




	private Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
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
						i.setDataAndType(Uri.fromFile(new File(documentPath)), "image/jpeg");
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
					//save the text file @local
					/*OutputStream outStream = null;
					File file = new File(Environment.getExternalStorageDirectory(), documentName);
					outStream = new FileOutputStream(file);
					outStream.write(buffer)
					outStream.flush();
					outStream.close();*/
					
					
					File outFile = new File(Environment.getExternalStorageDirectory(), documentName);
					FileWriter out = new FileWriter(outFile);
					//		out.write
					out.write(msg.getData().getString("text"));
					out.flush();
					out.close();

					try {
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setPackage("com.dynamixsoftware.printershare");
						i.setDataAndType(Uri.fromFile(new File(documentPath)), "text/plain");
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
			String responseXPath = msg.getData().getString("text");
			String documentPath = null;
			//Fetch value of document x path from returned XML string response.
			if(responseXPath != null && !"".equalsIgnoreCase(responseXPath) && !responseXPath.contains("error")){
				try{

					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					XmlPullParser xpp = factory.newPullParser();

					xpp.setInput( new StringReader ( responseXPath ) );
					int eventType = xpp.getEventType();
					while (eventType != XmlPullParser.END_DOCUMENT) {
						if(eventType == XmlPullParser.START_TAG && xpp.getName() != null && xpp.getName().equalsIgnoreCase("document_url")){
							xpp.next();
							if(xpp.getText().contains("edakia.in"))
								//document found
								documentPath = xpp.getText();
							break;
						}
						eventType = xpp.next();
					}

					documentName = (documentPath.split("/"))[documentPath.split("/").length-1];
					//Fetch document.
					Toast.makeText(ReceiveActivity.this, "Thanks fetching Document !! ", Toast.LENGTH_LONG).show();		
					if(documentPath.contains(".png") || documentPath.contains(".jpeg") || documentPath.contains(".jpg")){
						downloadImage("http://" + documentPath, true);
					}else {
						downloadText("http://" + documentPath, true);
					}

				}catch(Exception anExcep){
					anExcep.printStackTrace();
				}
			}else if(responseXPath.contains("error") && responseXPath.contains("Document not found")){
				//could not find any document with this.
				Toast.makeText(ReceiveActivity.this, "Sorry !! We could not find document matching this secret code & mobile number. \n Please make sure you entered correct inputs.", Toast.LENGTH_LONG).show();
			}
			else {
				//some other error.
				Toast.makeText(ReceiveActivity.this, "Sorry !! We could not find your document due to some internal error. Please bear with us for some time to serve you again.", Toast.LENGTH_LONG).show();
			}
		}
	};
}
