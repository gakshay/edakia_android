package com.gakshay.android.edakia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class FileSelectionActivity extends Activity {
	private final static int RESULT_LOAD_IMAGE = 1;
	private final static int RESULT_LOAD_FILE = 2;

	private String selectedFilePath;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_selection);
		prepareConfirmSendDialogBox();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_file_selection, menu);
		return true;
	}

	private void prepareConfirmSendDialogBox(){
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Search Your Storage For :");
		adb.setCancelable(false);
		adb.setPositiveButton("Select a Pic !!", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

				startActivityForResult(i, RESULT_LOAD_IMAGE);

			}
		});


		adb.setNegativeButton("Select any  File !!", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
				Intent selectFile = new Intent(FileSelectionActivity.this, FileChooser.class);
				startActivityForResult(selectFile, RESULT_LOAD_FILE); 
			}
		});

		AlertDialog dialog = adb.create();
		dialog.onWindowFocusChanged(false);
		dialog.setCancelable(false);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();

		//wlp.gravity = Gravity.BOTTOM;
		wlp.width = 1000;
		wlp.height = 1000;
		WindowManager.LayoutParams params = window.getAttributes();  
		params.x = -100;  
		params.height = 70;  
		params.width = 1000;  
		params.y = -50;  

		dialog.getWindow().setAttributes(params); 
		wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wlp);
		adb.setIcon(R.drawable.ic_launcher_send); 
		dialog.show();
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			selectedFilePath = cursor.getString(columnIndex);
			cursor.close();
			Intent returnData = new Intent();
			returnData.putExtra("filePath",selectedFilePath);
			setResult(Activity.RESULT_OK,returnData);
			finish();
		}else if(requestCode == RESULT_LOAD_FILE){
			if (resultCode == RESULT_OK) {
				// Get the path set from file chooser.
				selectedFilePath = data.getStringExtra("filePath");
				Intent returnData = new Intent();
				returnData.putExtra("filePath",selectedFilePath);
				setResult(Activity.RESULT_OK,returnData);
				finish();
			}
		}
	}


	private void prepareConfirmSelectedFile(){
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("Please confirm your selection to send");
		adb.setCancelable(false);
		adb.setPositiveButton("Ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				onDialogPressedOK();
			}
		});


		adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
				onDialogPressedCancel();
			}
		});

		AlertDialog dialog = adb.create();
		dialog.onWindowFocusChanged(false);
		dialog.setCancelable(false);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();

		//wlp.gravity = Gravity.BOTTOM;
		wlp.width = 1000;
		wlp.height = 1000;
		WindowManager.LayoutParams params = window.getAttributes();  
		params.x = -100;  
		params.height = 70;  
		params.width = 1000;  
		params.y = -50;  

		dialog.getWindow().setAttributes(params); 
		wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wlp);
		adb.setIcon(R.drawable.ic_launcher_send); 
		dialog.show();
	}


	private void onDialogPressedCancel(){
		Toast.makeText(this, "Please choose another file :", Toast.LENGTH_LONG).show();
		finish();
	}


	private void onDialogPressedOK(){
		Toast.makeText(this, "Sending Your Document", Toast.LENGTH_LONG).show();
		Intent returnData = new Intent();
		returnData.putExtra("filePath",selectedFilePath);
		setResult(Activity.RESULT_OK,returnData);
		finish();
	}


}
