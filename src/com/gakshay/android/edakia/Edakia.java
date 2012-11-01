package com.gakshay.android.edakia;


import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class Edakia extends Activity {

	private static final int ACTIVITY_CHOOSE_FILE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		preparedSharedPref();
		prepareTempResultDialog();
		prepareSimpleDialog();
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
	
	
	private void prepareTempResultDialog(){
		AlertDialog.Builder altDialog= new AlertDialog.Builder(this);
		//set the message on d	ialog.
		Intent intent = getIntent();
		Bundle bundleData = intent.getExtras();
		String tranctMsg = "",dialogMessage = "";
			tranctMsg = getString(R.string.costDialogSentMsg);
			altDialog.setTitle(getString(R.string.costDialogTitle));
			String cost = "5:00";//(String) bundleData.get("transactionCost");
			String userBalance = "2 Rs";//(String)bundleData.get("userBalance");
			dialogMessage = tranctMsg + "\n\n" + getString(R.string.costDialogCostMsg) + " " + cost + "\n" + getString(R.string.costDialogBalanceMsg) + " "+userBalance;

		
			
			altDialog.setInverseBackgroundForced(true);

		
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
		
		AlertDialog resultDialog = altDialog.create();
		resultDialog.requestWindowFeature(Window.PROGRESS_END);
		
		resultDialog.onWindowFocusChanged(false);
		resultDialog.setCancelable(false);
		Window window = resultDialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER;
		wlp.horizontalMargin = Gravity.CENTER;
		wlp.verticalMargin = Gravity.CENTER;
		
		wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wlp);
		
		resultDialog.show();
	}
	
	
	
	
	private void prepareSimpleDialog(){
		  //set up dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.requestWindowFeature(Window.PROGRESS_START);
        dialog.setContentView(R.layout.result_dialog);

        //dialog.setTitle("This is my custom dialog box");
        dialog.setCancelable(true);
        //there are a lot of settings, for dialog, check them all out!
        //set up text
        /*TextView text = (TextView) dialog.findViewById(R.id.TextView01);
        text.setText("This is the text to be shown");
        dialog.getWindow().setTitleColor(1);
        */
        //set up image view
        ImageView img = (ImageView) dialog.findViewById(R.id.ImageView01);
        img.setImageResource(R.drawable.ic_launcher);

        //set up button
        Button button = (Button) dialog.findViewById(R.id.Button01);
       
        button.setOnClickListener(new View.OnClickListener() {
        @Override
            public void onClick(View v) {
            dialog.dismiss();   
        	//finish();
            }
        });
        //now that the dialog is set up, it's time to show it    
        dialog.show();
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
			String userBalance = (String)bundleData.get("userBalance");
			dialogMessage = tranctMsg + "\n\n" + getString(R.string.costDialogCostMsg) + " " + cost + "\n" + getString(R.string.costDialogBalanceMsg) + " "+userBalance;

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
		
		AlertDialog resultDialog = altDialog.create();
		resultDialog.onWindowFocusChanged(false);
		resultDialog.setCancelable(false);
		Window window = resultDialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();

		wlp.gravity = Gravity.CENTER;
		wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wlp);
		
		resultDialog.show();
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
