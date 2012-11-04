package com.gakshay.android.edakia;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class FileDownloadAsyncActivity extends Activity {

	private String downloadURL;
	private String absolutefilePath;
	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_download_async);
		downloadURL = ((String)getIntent().getExtras().get("downloadURL"));
		absolutefilePath = ((String)getIntent().getExtras().get("filePath"));
		startDownload();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_file_download_async, menu);
		return true;
	}

	private void startDownload() {
		new DownloadFileAsync().execute(downloadURL);
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Downloading file..");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
		}
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

		@Override
		protected String doInBackground(String... aurl) {
			int count;

			try {

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();
				Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(absolutefilePath);

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress(""+(int)((total*100)/lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {

				Log.d("error", e.toString());
			}
			return null;

		}
		protected void onProgressUpdate(String... progress) {
			Log.d("ANDRO_ASYNC",progress[0]);
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String unused) {
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
			setResult(RESULT_OK);
			finish();
		}
	}
}

