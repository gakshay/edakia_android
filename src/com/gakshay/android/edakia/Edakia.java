package com.gakshay.android.edakia;


import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.gakshay.android.util.CustomDialog;


public class Edakia extends Activity {

	private static final int ACTIVITY_CHOOSE_FILE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		preparedSharedPref();
		if(getIntent() != null && getIntent().getExtras() != null){
			if("true".equalsIgnoreCase((String)getIntent().getExtras().get("showResultDialogBox"))){
				prepareResultDialog();	
				getIntent().putExtra("showResultDialogBox", "false");
			}
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
				i.setPackage(getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("printerShareActivity","com.dynamixsoftware.printershare"));
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

		//set the message on dialog.
		Intent intent = getIntent();
		Bundle bundleData = intent.getExtras();
		String resultMessage;
		if(!(Boolean)bundleData.get("isError")){
			String amountToBePaid,userBalance,trnsCost;
			trnsCost = getString(R.string.costDialogAmount) +  (String) bundleData.get("transactionCost");
			userBalance = getString(R.string.costDialogBalanceMsg) +  (String)bundleData.get("userBalance");
			amountToBePaid = getString(R.string.costDialogCostMsg) +  (String)bundleData.get("paidAmount");

			if("received".equalsIgnoreCase((String) bundleData.get("transactionType"))){
				resultMessage = getString(R.string.costDialogReceiveMsg);
				(CustomDialog.resultCostDialog(this,R.style.Theme_customDialogTitleTheme, R.layout.custom_title, R.layout.result_dialog_cost, R.id.TrnsButton,
						R.id.TrnsResult,resultMessage,R.id.TrnsCost,R.id.TrnsAmount,R.id.TrnsBalance,amountToBePaid,userBalance,trnsCost)).show();
			}else if("send".equalsIgnoreCase((String) bundleData.get("transactionType"))){
				resultMessage = getString(R.string.costDialogSentMsg);
				(CustomDialog.resultCostDialog(this,R.style.Theme_customDialogTitleTheme, R.layout.custom_title, R.layout.result_dialog_cost, R.id.TrnsButton,
						R.id.TrnsResult,resultMessage,R.id.TrnsCost,R.id.TrnsAmount,R.id.TrnsBalance,amountToBePaid,userBalance,trnsCost)).show();
			}else if("chngPwd".equalsIgnoreCase((String) bundleData.get("transactionType"))){
				resultMessage = getString(R.string.chngPwdDialogMsg);
				(CustomDialog.resultChngPwdDialog(this,R.style.Theme_customDialogTitleTheme, R.layout.custom_title, R.layout.result_dialog_chng_pwd, R.id.TrnsButton,
						R.id.TrnsResult,resultMessage)).show();

			}
		}else {// show generic error message.
			resultMessage = getString(R.string.chngPwdDialogMsg);
			(CustomDialog.resultChngPwdDialog(this,R.style.Theme_customDialogTitleTheme, R.layout.custom_title, R.layout.result_dialog_chng_pwd, R.id.TrnsButton,
					R.id.TrnsResult,resultMessage)).show();
		}

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
