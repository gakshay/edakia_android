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


	public static ValidationErrorCodes validateMobileNumber(String mobile){

		if(mobile.length() == 0){
			return ValidationErrorCodes.MOBILE_NUMBER_MISSING;
		}
		if(mobile.length() < 10 || mobile.length() > 13){
			return ValidationErrorCodes.MOBILE_NUMBER_INCORRECT;
		}

		return ValidationErrorCodes.SUCCESS;
	}


	public static ValidationErrorCodes validateSecretNumber(String secretCode){

		if(secretCode.length() == 0){
			return ValidationErrorCodes.SECRET_CODE_MISSING;
		}
		if(secretCode.length() < 6){
			return ValidationErrorCodes.SECRET_CODE_INCORRECT;
		}
		return ValidationErrorCodes.SUCCESS;
	}
	
	
	
	public static ValidationErrorCodes validatePassword(String password){

		if(password.length() == 0){
			return ValidationErrorCodes.SECRET_CODE_MISSING;
		}
		if(password.length() < 4 || password.length() > 6){
			return ValidationErrorCodes.SECRET_CODE_INCORRECT;
		}
		return ValidationErrorCodes.SUCCESS;
	}
}
