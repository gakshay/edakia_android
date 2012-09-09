package com.gakshay.android.edakia;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class Edakia extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String showCostDialog = "false";
		if(getIntent() != null && getIntent().getExtras() != null)
			showCostDialog = (String)getIntent().getExtras().get("showCostDialogBox");
		
		if("true".equalsIgnoreCase(showCostDialog))
			prepareCostDialogBox();
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
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}

	private void prepareCostDialogBox(){
		AlertDialog.Builder altDialog= new AlertDialog.Builder(this);
		//set the message on d	ialog.
		Intent intent = getIntent();
		Bundle bundleData = intent.getExtras();
		String tranctTitle;
		if("received".equalsIgnoreCase((String) bundleData.get("transactionType")))
			tranctTitle = "Received Successfully";
		else 
			tranctTitle = "Sent Successfully";

		String tranctMsg = (String) bundleData.get("transactionMsg");
		String dialogMessage = tranctTitle + "\n\n" + tranctMsg;
		altDialog.setMessage(dialogMessage); // here add your message
		altDialog.setCancelable(false);
		altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//do nothing.
			}
		});
		altDialog.show();
	}
}
