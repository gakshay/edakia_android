package com.gakshay.android.edakia;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.View;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.InputStream;
//import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringReader;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.gakshay.android.util.CustomizedThread;


public class ReceiveActivity extends Activity {

	private ProgressDialog progressDialog;	
	private Bitmap bitmap = null;
	private String text = null;

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
		switch(view.getId()){
		case R.id.receiveNextButton:
			EditText mobile = (EditText) findViewById(R.id.receiveMobile);
			EditText secretCode = (EditText) findViewById(R.id.secretCode);
			if(mobile.getText().length() == 0){
				Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_LONG).show();
				return;
			}
			if(mobile.getText().length() < 10){
				Toast.makeText(this, "Incorrect Mobile Number", Toast.LENGTH_LONG).show();
				return;
			}
			if(secretCode.getText().length() == 0){
				Toast.makeText(this, "Enter Secret Code", Toast.LENGTH_LONG).show();
				return;
			}
			if(secretCode.getText().length() < 6){
				Toast.makeText(this, "Incorrect Secret Code", Toast.LENGTH_LONG).show();
				return;
			}
			try {// Process the request.
				downloadText(prepareEdakiaURL(mobile,secretCode),false);			
			}
			catch (Exception e) {
				//handle the exception !
				e.printStackTrace();
			}
			Toast.makeText(this, "Thanks fetching Document !! ", Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}

	public String prepareEdakiaURL(EditText mobile, EditText secretCode){
		String edakiaURL = null;
		edakiaURL = "http://edakia.in/transactions/receive.xml?transaction[receiver_mobile]=" + mobile.getText() + "&transaction[document_secret]="+secretCode.getText();	
		return edakiaURL;
	}

	public void readStream(InputStream input){
		// read stream
		InputStreamReader is = new InputStreamReader(input);
		BufferedReader br = new BufferedReader(is);
		try{
			String read = br.readLine();
			while(read != null) {
				System.out.println(read);
				read = br.readLine();
			}
			Log.e("stream", read);
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	private void downloadImage(String urlStr,boolean showProcessingDialog) {
		if(showProcessingDialog)
			progressDialog = ProgressDialog.show(this, "", 
					"Downloading Your Document ........");
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
					"Download Text from " + urlStr);
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
					text = "";
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
				ImageView img = (ImageView) findViewById(R.id.imageview01);
				img.setImageBitmap((Bitmap)(msg.getData().getParcelable("bitmap")));
				
				progressDialog.dismiss();
				try {
					Intent intent = new Intent(ReceiveActivity.this,PrintActivity.class);
					intent.setAction(Intent.ACTION_SEND);
					intent.setData(Uri.parse("http://edakia.in/system/docs/8/original/1081210064_9711335593.jpg"));
					intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("http://edakia.in/system/docs/8/original/1081210064_9711335593.jpg"));
					intent.setType("image/jpeg");

					startActivity(intent);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				break;
			case 2:
				TextView text = (TextView) findViewById(R.id.textview01);
				text.setText(msg.getData().getString("text"));
				
				progressDialog.dismiss();
				try {
					PackageManager pm = getPackageManager();
					Intent intent = pm.getLaunchIntentForPackage("com.dynamixsoftware.printershare");
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse("http://edakia.in/system/docs/8/original/1081210064_9711335593.jpg"), "image/jpg");
					startActivity(intent);
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
			switch (msg.what) {
			case 2:
				String responseXPath = msg.getData().getString("text");
				String documentPath = null;
				//Fetch value of document x path from returned XML string response.

				try{

					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					XmlPullParser xpp = factory.newPullParser();

					xpp.setInput( new StringReader ( responseXPath ) );
					int eventType = xpp.getEventType();
					while (eventType != XmlPullParser.END_DOCUMENT) {
						if(eventType == XmlPullParser.START_TAG && xpp.getName() != null && xpp.getName().equalsIgnoreCase("document_url")){
							xpp.next();
							if(xpp.getText().contains("edakia.in")){
								//document found
								documentPath = xpp.getText();
								
							}else {
								//document not found
							}
							
							break;
						}
						eventType = xpp.next();
					}
										
				//Fetch document.
					if(documentPath.contains(".png") || documentPath.contains(".jpeg") || documentPath.contains(".jpg")){
						downloadImage("http://" + documentPath, true);
					}else {
						downloadText("http://" + documentPath, true);
					}

				}catch(Exception anExcep){

				}
				/*Element element = 
				NodeList node = element.getElementsByTagName("tarif");
				int length = node.getLength();
				for (int j = 0; j < length; j++)
				{
				     Element terrif = (Element) node.item(j);

				     String name = terrif.getAttribute("name");

				     // and so on for other attributes...
				}*/

				break;
			}
			//progressDialog.dismiss();
		}
	};
}
