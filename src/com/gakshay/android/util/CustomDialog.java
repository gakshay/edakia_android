/**
 * 
 */
package com.gakshay.android.util;



import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Amitsharma
 *
 */
public class CustomDialog extends Dialog{

	public CustomDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public static Dialog resultCostDialog(Context dialogContext,int dialogTheme, int dialogTitle, int dialogLayout, int dialogButton,int dialogMsg,String dialogResultMsg){
		final Dialog dialog = new Dialog(dialogContext, dialogTheme);
		dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		dialog.setContentView(dialogLayout);
		dialog.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, dialogTitle);

		((TextView) dialog.findViewById(dialogMsg)).setText(dialogResultMsg);
		
		dialog.setCancelable(false);

		//set up button
		Button button = (Button) dialog.findViewById(dialogButton);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();   
				//finish();
			}
		});
		//now that the dialog is set up, it's time to show it    
		return dialog;	
	}


	public static Dialog resultCostDialog(Context dialogContext,int dialogTheme, int dialogTitle, int dialogLayout, int dialogButton,int dialogMsg,String dialogResultMsg,
			int trnsCost,int trnsAmount,int trnsBalance, String paidAmount, String userBalance, String trnsCostValue){
		final Dialog dialog = new Dialog(dialogContext, dialogTheme);
		dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		dialog.setContentView(dialogLayout);
		dialog.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, dialogTitle);

		dialog.setCancelable(false);

		((TextView) dialog.findViewById(dialogMsg)).setText(dialogResultMsg);
		
		if(trnsCostValue != null && trnsCostValue.contains("null")){
			((TextView) dialog.findViewById(trnsCost)).setVisibility(TextView.GONE);
		}else{
			((TextView) dialog.findViewById(trnsCost)).setText(trnsCostValue);
		}
		
		((TextView) dialog.findViewById(trnsAmount)).setText(paidAmount);
		((TextView) dialog.findViewById(trnsBalance)).setText(userBalance);

		//set up button
		Button button = (Button) dialog.findViewById(dialogButton);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();   
			}
		});
		return dialog;	
	}



	public static Dialog resultChngPwdDialog(Context dialogContext,int dialogTheme, int dialogTitle, int dialogLayout, int dialogButton,int dialogMsg,String dialogResultMsg){
		final Dialog dialog = new Dialog(dialogContext, dialogTheme);
		dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		dialog.setContentView(dialogLayout);
		dialog.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, dialogTitle);
		dialog.setCancelable(false);

		((TextView) dialog.findViewById(dialogMsg)).setText(dialogResultMsg);


		//set up button
		Button button = (Button) dialog.findViewById(dialogButton);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();   
			}
		});
		return dialog;	
	}
	
	
	public static Dialog resultGenericErrorDialog(Context dialogContext,int dialogTheme, int dialogTitle, int dialogLayout, int dialogButton,int dialogMsg,String dialogResultMsg){
		final Dialog dialog = new Dialog(dialogContext, dialogTheme);
		dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		dialog.setContentView(dialogLayout);
		dialog.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, dialogTitle);

		dialog.setCancelable(true);

		((TextView) dialog.findViewById(dialogMsg)).setText(dialogResultMsg);


		//set up button
		Button button = (Button) dialog.findViewById(dialogButton);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();   
			}
		});
		return dialog;	
	}


	/*private void prepareAlertDialog(int dialogTheme,int dialogTitle,int dialogIcon,int dialogButton){
		AlertDialog.Builder altDialog= new AlertDialog.Builder(dialogContext,dialogTheme);
		altDialog.setTitle(gets);

		altDialog.setInverseBackgroundForced(true);

		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.player_info, null, false);
		ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		dialog.setContentView(v,p);

		LayoutInflater inflater = getLayoutInflater();
		View dialoglayout = inflater.inflate(R.layout.basic_dialog_layout, (ViewGroup) getCurrentFocus());
		altDialog.setView(dialoglayout);	

		altDialog.setMessage("Alert Dialog Message"); // here add your message
		altDialog.setCancelable(false);
		altDialog.setIcon(R.drawable.ic_launcher);
		altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//do nothing.
				dialog.cancel();
				dialog.dismiss();
			}
		});

		altDialog.show();
		AlertDialog resultDialog = altDialog.create();
		resultDialog.requestWindowFeature(Window.PROGRESS_END);

		resultDialog.onWindowFocusChanged(false);
		resultDialog.setCancelable(false);
		Window window = resultDialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER;
		//wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wlp);

		resultDialog.show();
	}
	 */

}
