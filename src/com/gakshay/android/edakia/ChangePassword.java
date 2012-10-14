package com.gakshay.android.edakia;

import java.util.ArrayList;
import java.util.List;

import com.gakshay.android.validation.Validator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ChangePassword extends BaseActivity {
	private ProgressDialog progressDialog;
	private String chngPwdResp;
	private EditText mobile;
	private EditText oldPwd;
	private EditText newPwd;
	private EditText newPwdAgain;

	private String authURL = "http://staging.edakia.in/api/users.xml"; //"http://www.edakia.in/api/users.xml";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		((ImageView)findViewById(R.id.errImgMobile)).setVisibility(ImageView.INVISIBLE);
		((ImageView)findViewById(R.id.errImgOldPwd)).setVisibility(ImageView.INVISIBLE);
		((ImageView)findViewById(R.id.errImgNewPwd)).setVisibility(ImageView.INVISIBLE);
		((ImageView)findViewById(R.id.errImgNewPwdAgn)).setVisibility(ImageView.INVISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_change_password, menu);
		return true;
	}

	// Will be connected with the buttons via XML
	public void changePassword(View aview) {
		mobile = ((EditText) findViewById(R.id.mobile));
		oldPwd = ((EditText) findViewById(R.id.oldPwd));
		newPwd = ((EditText) findViewById(R.id.newPwd));
		newPwdAgain = ((EditText) findViewById(R.id.newPwdAgain));

		if(validateInputData()){
			//authenticateUser(authURL, mobile.getText().toString(), oldPwd.getText().toString(),true);
		}
	}

	private boolean validateInputData(){
		boolean isValid = false;
		TextView anErrText = (TextView) findViewById(R.id.Error);
		anErrText.setText(null);
		List<String> textErrors = new ArrayList<String>();
		
		ImageView errImgMob = (ImageView)findViewById(R.id.errImgMobile);
		ImageView errImgPwd = (ImageView)findViewById(R.id.errImgOldPwd);
		ImageView errImgNewPwd = (ImageView)findViewById(R.id.errImgNewPwd);
		ImageView errImgNewPwdAgn = (ImageView)findViewById(R.id.errImgNewPwdAgn);

		//both are mandatory fields.

		//if any of the field is missing.
		if((mobile.getText().toString() == null || "".equalsIgnoreCase(mobile.getText().toString()))
				&& (oldPwd.getText().toString() == null || "".equalsIgnoreCase(oldPwd.getText().toString()))
				&& (newPwd.getText().toString() == null || "".equalsIgnoreCase(newPwd.getText().toString()))
				&& (newPwdAgain.getText().toString() == null || "".equalsIgnoreCase(newPwdAgain.getText().toString()))){

			Toast.makeText(this, "Please provide all rquired inputs.", Toast.LENGTH_LONG).show();

			errImgMob.setImageResource(R.drawable.ic_error);
			errImgPwd.setImageResource(R.drawable.ic_error);
			errImgNewPwd.setImageResource(R.drawable.ic_error);
			errImgNewPwdAgn.setImageResource(R.drawable.ic_error);

			

			errImgMob.setVisibility(ImageView.VISIBLE);
			errImgPwd.setVisibility(ImageView.VISIBLE);
			errImgNewPwd.setVisibility(ImageView.VISIBLE);
			errImgNewPwdAgn.setVisibility(ImageView.VISIBLE);
			textErrors.add("Please provide all rquired inputs.");
			showErrMsg(anErrText, textErrors);
			return false;
		}


		//Validate mobile no.
		int valStatusMob = Validator.validateMobileNumber(mobile.getText().toString()).ordinal();

		//Validate pwd
		int valStatusPwd= Validator.validatePassword(oldPwd.getText().toString()).ordinal();	

		//Validate new password no.
		int valStatusNewPwd= Validator.validatePassword(newPwd.getText().toString()).ordinal();	
		//Validate new password again.
		int valStatusNewPwdAgn= Validator.validatePassword(newPwdAgain.getText().toString()).ordinal();	

		//set error message + image error
		if(valStatusMob == 0 && valStatusPwd == 0 && valStatusNewPwd == 0 && valStatusNewPwdAgn == 0){

			if(!newPwd.getText().toString().equalsIgnoreCase(newPwdAgain.getText().toString())){
				Toast.makeText(this, "New password & Confirm password doesn't match.", Toast.LENGTH_LONG).show();
				textErrors.add("Please correct your password again.");
				newPwd.findFocus();
				errImgNewPwd.setImageResource(R.drawable.ic_error);
				errImgNewPwdAgn.setImageResource(R.drawable.ic_error);
				
				((ImageView)findViewById(R.id.errImgMobile)).setVisibility(ImageView.INVISIBLE);
				((ImageView)findViewById(R.id.errImgOldPwd)).setVisibility(ImageView.INVISIBLE);
				((ImageView)findViewById(R.id.errImgNewPwd)).setVisibility(ImageView.VISIBLE);
				((ImageView)findViewById(R.id.errImgNewPwdAgn)).setVisibility(ImageView.VISIBLE);
				showErrMsg(anErrText, textErrors);
				return false;
			}
			
			errImgMob.setImageResource(R.drawable.ic_success);
			errImgPwd.setImageResource(R.drawable.ic_success);
			errImgNewPwd.setImageResource(R.drawable.ic_success);
			errImgNewPwdAgn.setImageResource(R.drawable.ic_success);
			
			((ImageView)findViewById(R.id.errImgMobile)).setVisibility(ImageView.VISIBLE);
			((ImageView)findViewById(R.id.errImgOldPwd)).setVisibility(ImageView.VISIBLE);
			((ImageView)findViewById(R.id.errImgNewPwd)).setVisibility(ImageView.VISIBLE);
			((ImageView)findViewById(R.id.errImgNewPwdAgn)).setVisibility(ImageView.VISIBLE);

			return true;
		}


		if(valStatusMob == 0){
			errImgMob.setImageResource(R.drawable.ic_success);
		}
		if(valStatusMob == 1){
			textErrors.add("Provide your mobile.");
			mobile.findFocus();
			errImgMob.setImageResource(R.drawable.ic_error);
		}

		if(valStatusMob == 2){
			textErrors.add("Correct your mobile.");
			mobile.findFocus();
			errImgMob.setImageResource(R.drawable.ic_error);
		}
		if(valStatusPwd == 0){
			errImgPwd.setImageResource(R.drawable.ic_success);
		}
		if(valStatusPwd == 5){
			textErrors.add("Provide your old password.");
			oldPwd.findFocus();
			errImgPwd.setImageResource(R.drawable.ic_error);
		}
		if(valStatusPwd == 6){
			textErrors.add("Correct your old password.");
			oldPwd.findFocus();
			errImgPwd.setImageResource(R.drawable.ic_error);
		}
		if(valStatusNewPwd == 0){
			errImgNewPwd.setImageResource(R.drawable.ic_success);
		}
		if(valStatusNewPwd == 5){
			textErrors.add("Provide your new password.");
			oldPwd.findFocus();
			errImgNewPwd.setImageResource(R.drawable.ic_error);
		}
		if(valStatusNewPwd == 6){
			textErrors.add("Correct your new password.");
			oldPwd.findFocus();
			errImgNewPwd.setImageResource(R.drawable.ic_error);
		}
		if(valStatusNewPwdAgn == 0){
			errImgNewPwdAgn.setImageResource(R.drawable.ic_success);
		}
		if(valStatusNewPwdAgn == 5){
			textErrors.add("Provide confirn new password.");
			oldPwd.findFocus();
			errImgNewPwdAgn.setImageResource(R.drawable.ic_error);
		}
		if(valStatusNewPwdAgn == 6){
			textErrors.add("Correct your confirm new password.");
			oldPwd.findFocus();
			errImgNewPwdAgn.setImageResource(R.drawable.ic_error);
		}


		errImgMob.setVisibility(ImageView.VISIBLE);
		errImgPwd.setVisibility(ImageView.VISIBLE);
		errImgNewPwd.setVisibility(ImageView.VISIBLE);
		errImgNewPwdAgn.setVisibility(ImageView.VISIBLE);
		
		showErrMsg(anErrText, textErrors);

		return isValid;	
	}
	
	private void showErrMsg(TextView anErrText, List<String> errMsg){
		String finalErrMsg="";
		for(String anErr : errMsg){
			finalErrMsg+=anErr+"\n";
		}
		anErrText.setText(finalErrMsg);		
	}

}
