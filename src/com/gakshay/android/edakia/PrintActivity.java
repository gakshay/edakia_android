package com.gakshay.android.edakia;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PrintActivity extends Activity {

	private ProgressDialog progressDialog;	
	private Bitmap bitmap = null;
	private String text = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.print_activity);

		prepareViewPrintActivity();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.print_activity, menu);
		return true;
	}


	// Will be connected with the buttons via XML
	public void onPrintClick(View aview) {
		try {
			/*Intent i = new Intent(Intent.ACTION_VIEW);
			i.setPackage("com.dynamixsoftware.printershare");
			Uri aURI = Uri.parse("http://www.edakia.in/transactions/receive.xml");
			i.setDataAndType(aURI, "text/html");
			startActivity(i);

			 */

			//http://profile.ak.fbcdn.net/hprofile-ak-snc4/373257_302841499757374_157695752_n.jpg

			PackageManager pm1 = getPackageManager();
			Intent intent = pm1.getLaunchIntentForPackage("com.dynamixsoftware.printershare");
		    intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("http://www.edakia.in/images/logo.jpg"), "image/jpeg");
			//intent.setDataAndType(Uri.fromFile(new File("file:///mnt/sdCard/")), "");
			startActivity(intent);

			//Intent intent2 = pm1.getLaunchIntentForPackage("com.android.browser");
			//startActivity(intent2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void prepareViewPrintActivity(){

		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				handleSendText(intent); // Handle text being sent
			} else if (type.startsWith("image/")) {
				handleSendImage(intent); // Handle single image being sent
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				// handleSendMultipleImages(intent); // Handle multiple images being sent
			}
		} else {
			// Handle other intents, such as being started from the home screen
		}
	}


	private void handleSendText(Intent intent){

	}


	private void handleSendImage(Intent intent){
		 Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		    if (imageUri != null) {
		    	downloadImage(imageUri.toString(), false);
		        // Update UI to reflect image being shared
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
				break;
			case 2:
				TextView text = (TextView) findViewById(R.id.textview01);
				text.setText(msg.getData().getString("text"));
				break;
			}
			progressDialog.dismiss();
		}
	};
	
	
}

