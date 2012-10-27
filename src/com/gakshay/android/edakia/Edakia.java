package com.gakshay.android.edakia;


import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;


public class Edakia extends Activity {

	private static final int ACTIVITY_CHOOSE_FILE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		preparedSharedPref();
		if(getIntent() != null && getIntent().getExtras() != null){
			if("true".equalsIgnoreCase((String)getIntent().getExtras().get("showCostDialogBox")) || "true".equalsIgnoreCase((String)getIntent().getExtras().get("showResultDialogBox")))
				prepareResultDialog();	
		}	
	}

	public void optionClickHandler(View view) {
		switch(view.getId()){
		case R.id.optionReceive:
			Intent receiveIntent = new Intent(Edakia.this, ReceiveActivity.class);
			Edakia.this.startActivity(receiveIntent);
			break;
		case R.id.optionSend:
			Intent authenticateIntent = new Intent(Edakia.this, AuthenticateActivity.class);
			Edakia.this.startActivity(authenticateIntent);
			break;
		case R.id.optionPrint:
			Intent chooseFile,intent;
			chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
			chooseFile.setType("file/*");
			intent = Intent.createChooser(chooseFile, "Choose a file");
			startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
			break;	
		case R.id.ChngPwdBtn:
			Intent chngPwd = new Intent(Edakia.this, ChangePassword.class);
			Edakia.this.startActivity(chngPwd);
			break;
		default:
			break;
		}
	}

	/** Called when an activity called by using startActivityForResult finishes. */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == ACTIVITY_CHOOSE_FILE && resultCode == RESULT_OK){
			Uri uri = data.getData();
			Toast.makeText(this, "File selected to send :  " + uri.getPath(), Toast.LENGTH_LONG).show();
			String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(uri.getPath())));
			try {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setPackage("com.dynamixsoftware.printershare");
				i.setDataAndType(Uri.fromFile(new File(uri.getPath())), mimeType);
				startActivity(i);


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, "Invoke to printer share App didn't work !!  \n Going to Home Page" , Toast.LENGTH_LONG).show();
			}


		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}

	private void prepareResultDialog(){
		AlertDialog.Builder altDialog= new AlertDialog.Builder(this);
		//set the message on d	ialog.
		Intent intent = getIntent();
		Bundle bundleData = intent.getExtras();
		String tranctMsg = "",dialogMessage = "";
		if("received".equalsIgnoreCase((String) bundleData.get("transactionType"))){
		    tranctMsg = getString(R.string.costDialogReceiveMsg);
		altDialog.setTitle(getString(R.string.costDialogTitle));
		String cost = (String) bundleData.get("transactionCost");
		dialogMessage = tranctMsg + "\n\n" + getString(R.string.costDialogCostMsg) + cost;

		}else if("send".equalsIgnoreCase((String) bundleData.get("transactionType"))){
			tranctMsg = getString(R.string.costDialogSentMsg);
			altDialog.setTitle(getString(R.string.costDialogTitle));
			String cost = (String) bundleData.get("transactionCost");
			dialogMessage = tranctMsg + "\n\n" + getString(R.string.costDialogCostMsg) + cost;

		}else{
			dialogMessage = getString(R.string.chngPwdDialogMsg);
			altDialog.setTitle(getString(R.string.chngPwdDialogTitle));
			 
		}
			
		
		
		altDialog.setMessage(dialogMessage); // here add your message
		altDialog.setCancelable(false);
		altDialog.setIcon(R.drawable.ic_launcher);
		altDialog.setNeutralButton(getString(R.string.resultDialogNeturalBtn), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//do nothing.
				dialog.cancel();
				dialog.dismiss();
			}
		});
		altDialog.show();
	}
	
	
	private void preparedSharedPref(){
		// Read from the /assets directory
		SharedPreferences eDakiaSharedPref = getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE);
		if(eDakiaSharedPref.getBoolean("isFirstBoot", true)){
			try {
				Resources resources = this.getResources();
				AssetManager assetManager = resources.getAssets();
				InputStream inputStream = assetManager.open("eDakia.properties");
				Properties properties = new Properties();
				properties.load(inputStream);

				SharedPreferences.Editor prefsEditor = eDakiaSharedPref.edit();
				for(String aKey : properties.stringPropertyNames()){
					prefsEditor.putString(aKey, properties.getProperty(aKey));
				}
				prefsEditor.putBoolean("isFirstBoot", false);
				prefsEditor.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
