package com.gakshay.android.edakia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PrintActivity extends Activity {

	private ProgressDialog progressDialog;	
	private Bitmap bitmap = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.print_activity);
		prepareActivityContent();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.print_activity, menu);
		return true;
	}


	// Will be connected with the buttons via XML
	public void onPrintClick(View aview) {
		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		String documentPath = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				try {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setPackage("com.dynamixsoftware.printershare");
					i.setDataAndType(Uri.fromFile(new File(documentPath)), "text/plain");
					startActivity(i);


				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(this, "Got some exception while trying to invoke printer share App !! ", Toast.LENGTH_LONG).show();
					startActivity((new Intent(this, Edakia.class)));


				}
			} else if (type.startsWith("image/")) {
				try {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setPackage("com.dynamixsoftware.printershare");
					i.setDataAndType(Uri.fromFile(new File(documentPath)), "image/jpeg");
					startActivity(i);


				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(this, "Got some exception while trying to invoke printer share App !!  \n Going to Home Page" , Toast.LENGTH_LONG).show();
					startActivity((new Intent(this, Edakia.class)));

				}
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				// handleSendMultipleImages(intent); // Handle multiple images being sent
			}
		} else {
			// Handle other intents, such as being started from the home screen
		}
	}

	public void prepareActivityContent(){

		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				prepareTextDocument(intent); // Handle text being sent
			} else if (type.startsWith("image/")) {
				prepareImageDocument(intent); // Handle single image being sent
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			//Handle multiple intent functionality.
		} else {
			// Handle other intents, such as being started from the home screen
		}
	}


	private void prepareTextDocument(Intent intent){
		try {
			String textPath = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();

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


	private void prepareImageDocument(Intent intent){

		Uri imagePath =(Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM); 
		
		File imgFile = new  File(imagePath.toString());
		if(imgFile.exists())
		{
			Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			ImageView myImage = (ImageView) findViewById(R.id.imageview01);
			myImage.setImageBitmap(myBitmap);

		}
	}
}

