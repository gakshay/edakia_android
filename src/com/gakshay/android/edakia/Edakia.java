package com.gakshay.android.edakia;


import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.gakshay.android.util.CustomDialog;


public class Edakia extends BaseActivity {

	private static final int ACTIVITY_CHOOSE_FILE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		preparedSharedPref();
		//prepareSimpleDialog();
		if(getIntent() != null && getIntent().getExtras() != null){
			if("true".equalsIgnoreCase((String)getIntent().getExtras().get("showResultDialogBox"))){
				prepareResultDialog();	
				getIntent().putExtra("showResultDialogBox", "false");
			}
		}	
	}

	public void optionClickHandler(View view) {
		if(!isNetworkConnection()){
			Intent edakiaHome = initiateHomePage(true, getString(R.string.errorDialogInternetNotAvailable));
			startActivity(edakiaHome);
			finish();
			return;
		}
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

	private void prepareSimpleDialog(){
		final Dialog dialog = new Dialog(this,R.style.Theme_customDialogTitleTheme);
		//dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//dialog.requestWindowFeature(Window.PROGRESS_START);
		dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		dialog.setContentView(R.layout.result_dialog_error);
		dialog.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		//dialog.setTitle("This is simple Title of simple dialog");

		//dialog.setTitle("This is my custom dialog box");
		dialog.setCancelable(true);

		//set up image view
		ImageView img = (ImageView) dialog.findViewById(R.id.layoutImage);
		img.setImageResource(R.drawable.ic_error);

		//set up button
		Button button = (Button) dialog.findViewById(R.id.errDialogButton);

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
		String resultMessage = (String)bundleData.get("errorMessageText");
		if(!"true".equalsIgnoreCase((String)bundleData.get("isError"))){
			String amountToBePaid,userBalance,trnsCost;
			trnsCost = getString(R.string.costDialogTrnsCostMsg) + " "+ (String) bundleData.get("transactionCost");//Added space
			userBalance = getString(R.string.costDialogBalanceMsg) + " " +  (String)bundleData.get("userBalance");
			amountToBePaid = getString(R.string.costDialogAmount) + " " +  (String)bundleData.get("paidAmount");

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
			if(resultMessage == null || "".equalsIgnoreCase(resultMessage))
				resultMessage = getString(R.string.errorDialogMsg);
			(CustomDialog.resultChngPwdDialog(this,R.style.Theme_customDialogTitleTheme, R.layout.custom_title, R.layout.result_dialog_error, R.id.errDialogButton,
					R.id.layoutText,resultMessage)).show();
		}

	}	
}
