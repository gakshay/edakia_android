package com.gakshay.android.edakia;

import java.io.InputStream;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gakshay.android.util.ActivitiesHelper;
import com.gakshay.android.util.NetworkOperations;
import com.gakshay.android.validation.Validator;

public class AuthenticateActivity extends BaseActivity {

	private ProgressDialog progressDialog;
	private String authResponse;
	private EditText mobile;
	private EditText password;
	private String authURL;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authenticate);
		((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
		((ImageView)findViewById(R.id.errImgPwd)).setVisibility(ImageView.INVISIBLE);
		authURL = this.getSharedPreferences("FIRST_TIME_BOOT_PREF", MODE_PRIVATE).getString("authURL","http://defaultURL");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_authenticate, menu);
		return true;
	}

	// Will be connected with the buttons via XML
	public void authenticate(View aview) {
		mobile = ((EditText) findViewById(R.id.YourMobile));
		password = ((EditText) findViewById(R.id.YourPassword));
		if(validateInputData()){
			authenticateUser(prepareEdakiaURL(), mobile.getText().toString(), password.getText().toString(),true);
		}
	}

	private String prepareEdakiaURL(){
		String edakiaURL = null;
		//sample URL 
		//http:mobile:passcode/staging.edakia.in/api/users.xml?serial_number=<serialNumber>
		edakiaURL = authURL + "?serial_number=" + getSerialNumber();
		return edakiaURL;
	}



	private boolean validateInputData(){
		boolean isValid = false;
		TextView text = (TextView) findViewById(R.id.Error);
		text.setText(null);
		ImageView errImgMob = (ImageView)findViewById(R.id.errImgMob);
		ImageView errImgPwd = (ImageView)findViewById(R.id.errImgPwd);

		//both are mandatory fields.

		if((password.getText().toString() == null || "".equalsIgnoreCase(password.getText().toString()))
				&& (mobile.getText().toString() == null || "".equalsIgnoreCase(mobile.getText().toString()))){

			//			Toast.makeText(this, "Please provide both inputs,mobile number and secret number.", Toast.LENGTH_LONG).show();
			text.setText(getString(R.string.empty_mobile_passcode));
			text.setVisibility(TextView.VISIBLE);
			errImgMob.setImageResource(R.drawable.ic_error);
			errImgPwd.setImageResource(R.drawable.ic_error);

			errImgMob.setVisibility(ImageView.VISIBLE);
			errImgPwd.setVisibility(ImageView.VISIBLE);
			return false;
		}


		//Validate mobile no.
		int valStatusMob = Validator.validateMobileNumber(mobile.getText().toString()).ordinal();

		//Validate secret no.
		int valStatusPwd= Validator.validatePassword(password.getText().toString()).ordinal();	

		//set error message + image error
		if(valStatusMob == 0 && valStatusPwd == 0){
			errImgMob.setImageResource(R.drawable.ic_success);
			errImgPwd.setImageResource(R.drawable.ic_success);
			isValid = true;
		}else if(valStatusMob == 0){
			errImgMob.setImageResource(R.drawable.ic_success);
			if(valStatusPwd == 5){
				//Toast.makeText(this, "Missed your password.", Toast.LENGTH_LONG).show();
				text.setText(getString(R.string.empty_passcode));
				password.findFocus();
				errImgPwd.setImageResource(R.drawable.ic_error);
			}else {
				//Toast.makeText(this, "Incorrect password.", Toast.LENGTH_LONG).show();
				text.setText(getString(R.string.invalid_passcode));
				password.findFocus();
				errImgPwd.setImageResource(R.drawable.ic_error);
			}
		}else if(valStatusPwd == 0){
			errImgPwd.setImageResource(R.drawable.ic_success);
			if(valStatusMob == 1){
				//Toast.makeText(this, "Missed your mobile number.", Toast.LENGTH_LONG).show();
				text.setText(getString(R.string.empty_mobile));
				mobile.findFocus();
				errImgMob.setImageResource(R.drawable.ic_error);
			}else {
				//Toast.makeText(this, "Incorrect mobile number.", Toast.LENGTH_LONG).show();
				text.setText(getString(R.string.invalid_mobile));
				mobile.findFocus();
				errImgMob.setImageResource(R.drawable.ic_error);
			}
		}else if(valStatusMob == 1 && valStatusPwd == 5){
			//Toast.makeText(this, "Provide your mobile number & password.", Toast.LENGTH_LONG).show();
			text.setText(getString(R.string.empty_mobile_passcode));
			mobile.findFocus();
			errImgMob.setImageResource(R.drawable.ic_error);
			errImgPwd.setImageResource(R.drawable.ic_error);

		}else if(valStatusMob == 1 && valStatusPwd == 6){
			//Toast.makeText(this, "Missed Your mobile number and Incorrect password.", Toast.LENGTH_LONG).show();
			text.setText(getString(R.string.invalid_mobile_passcode));
			mobile.findFocus();
			errImgPwd.setImageResource(R.drawable.ic_error);
			errImgMob.setImageResource(R.drawable.ic_error);

		}else if(valStatusMob == 2 && valStatusPwd == 5){
			//Toast.makeText(this, "Incorrect Mobile Number and missed password.", Toast.LENGTH_LONG).show();
			text.setText(getString(R.string.invalid_mobile_passcode));
			mobile.findFocus();
			errImgPwd.setImageResource(R.drawable.ic_error);
			errImgMob.setImageResource(R.drawable.ic_error);

		}else if(valStatusMob == 2 && valStatusPwd == 6){
			//Toast.makeText(this, "Incorrect Mobile Number and password.", Toast.LENGTH_LONG).show();
			text.setText(getString(R.string.invalid_mobile_passcode));
			mobile.findFocus();
			errImgPwd.setImageResource(R.drawable.ic_error);
			errImgMob.setImageResource(R.drawable.ic_error);

		}

		errImgMob.setVisibility(ImageView.VISIBLE);
		errImgPwd.setVisibility(ImageView.VISIBLE);

		return isValid;	
	}

	private Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//initialize error text value to null.
			TextView text = (TextView) findViewById(R.id.Error);
			text.setText(null);

			if("Exception".equalsIgnoreCase(authResponse) || authResponse.contains("error") || authResponse.equalsIgnoreCase("Exception401")){
				text.setText(getString(R.string.login_failed));

				((ImageView)findViewById(R.id.errImgMob)).setVisibility(ImageView.INVISIBLE);
				((ImageView)findViewById(R.id.errImgPwd)).setVisibility(ImageView.INVISIBLE);

				password.setText(null);//refresh password text

				Intent homeIntent = new Intent(getApplicationContext(), Edakia.class);
				homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				homeIntent.putExtra("showResultDialogBox", "true");
				homeIntent.putExtra("isError", "true");
				if(authResponse.contains("error"))
					homeIntent.putExtra("errorMessageText", (ActivitiesHelper.fetchValuesFromReponse(authResponse)).get("error"));
				else if(authResponse.equalsIgnoreCase("Exception401")){
					homeIntent.putExtra("errorMessageText", getString(R.string.invalid_mobile_passcode));
				}
				
				homeIntent.putExtra("errorMessageText", getString(R.string.invalid_mobile_passcode));//NEED TO REMOVE , TEMPORARY CHECKED IN

				startActivity(homeIntent);
				finish();
			}else{
				ImageView errImgMob = (ImageView)findViewById(R.id.errImgMob);
				errImgMob.setImageResource(R.drawable.ic_success);
				ImageView errImgPwd = (ImageView)findViewById(R.id.errImgPwd);
				errImgPwd.setImageResource(R.drawable.ic_success);
				text.setVisibility(TextView.INVISIBLE);
				//Toast.makeText(AuthenticateActivity.this, "Authenticated Successfullly.Go Ahead !!", Toast.LENGTH_SHORT).show();
				Intent sendIntent = new Intent(AuthenticateActivity.this, SendActivity.class);
				sendIntent.putExtra("sendMobile", mobile.getText().toString());
				sendIntent.putExtra("sendPassword", password.getText().toString());
				sendIntent.putExtra("userId", (ActivitiesHelper.fetchValuesFromReponse(authResponse)).get("id"));
				startActivity(sendIntent);
				finish();
			}
			progressDialog.dismiss();

		}
	};

	private void authenticateUser(final String authorizationURL,final String mobile,final String password, boolean showProcessDialog) {
		if(showProcessDialog)
			progressDialog = ProgressDialog.show(this, getString(R.string.authUserPrgDlgTitle), getString(R.string.authUserPrgDlg),true, false);
		new Thread() {
			public void run() {
				InputStream in = null;
				Message msg = Message.obtain();
				try {
					authResponse = NetworkOperations.authorizeToEdakiaServer(authorizationURL, mobile, password);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				messageHandler.sendMessage(msg);					
			}
		}.start();
	}    
}
