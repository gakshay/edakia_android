/**
 * 
 */
package com.gakshay.android.validation;

import java.util.regex.Pattern;

import android.util.Patterns;
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
		if(mobile.length() < 10 || mobile.length() > 13 || (!mobile.startsWith("7") && !mobile.startsWith("8") && !mobile.startsWith("9") && !mobile.startsWith("+91"))){
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
			return ValidationErrorCodes.PASSWORD_MISSING;
		}
		if(password.length() < 4 || password.length() > 6){
			return ValidationErrorCodes.PASSWORD_INCORRECT;
		}
		return ValidationErrorCodes.SUCCESS;
	}
	
	public static ValidationErrorCodes validateEmailAddress(String emailAddress){

		Pattern pattern = Patterns.EMAIL_ADDRESS;
	    if(!pattern.matcher(emailAddress).matches()){
	    	return ValidationErrorCodes.INCORRECT_EMAIL_ADDRESS;
	    }
		return ValidationErrorCodes.SUCCESS;
	}
}
