/**
 * 
 */
package com.gakshay.android.validation;

import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Amitsharma
 *
 */
public class Validator {


	public static ValidationErrorCodes validateMobileNumber(EditText mobile){

		if(mobile.getText().length() == 0){
			return ValidationErrorCodes.MOBILE_NUMBER_MISSING;
		}
		if(mobile.getText().length() < 10){
			return ValidationErrorCodes.MOBILE_NUMBER_INCORRECT;
		}

		return ValidationErrorCodes.SUCCESS;
	}


	public static ValidationErrorCodes validateSecretNumber(EditText secretCode){

		if(secretCode.getText().length() == 0){
			return ValidationErrorCodes.SECRET_CODE_MISSING;
		}
		if(secretCode.getText().length() < 6){
			return ValidationErrorCodes.SECRET_CODE_INCORRECT;
		}
		return ValidationErrorCodes.SUCCESS;
	}
}
