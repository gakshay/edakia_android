/**
 * 
 */
package com.gakshay.android.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

/**
 * @author Amitsharma
 *
 */
public class NetworkOperations {

	private static InputStream connectToEdakiaServer(String reqURL){
		InputStream in = null;
		int resCode = -1;
		HttpURLConnection httpConn  = null;
		try {
			URL url = new URL(reqURL);
			URLConnection urlConn = url.openConnection();

			if (!(urlConn instanceof HttpURLConnection)) {
				throw new IOException ("URL is not an Http URL");
			}

			httpConn = (HttpURLConnection)urlConn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			//httpConn.setDoOutput(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect(); 

			resCode = httpConn.getResponseCode();                 
			if (resCode == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();                                 
			}         

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			//httpConn.disconnect();
		}
		return in;
	}	


	public static String readXMLResponseFromEdakia(String reqURL){
		String responseXML = "";

		try {
			InputStream in = connectToEdakiaServer(reqURL);
			InputStreamReader isr = new InputStreamReader(in);
			int charRead;
			int BUFFER_SIZE = 1024;
			char[] inputBuffer = new char[BUFFER_SIZE];
			while ((charRead = isr.read(inputBuffer))>0)
			{      
				String readString = 
						String.copyValueOf(inputBuffer, 0, charRead);                    
				responseXML += readString;
				inputBuffer = new char[BUFFER_SIZE];
			}
			in.close();
			isr.close();
			inputBuffer = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseXML;
	}


	public static boolean readAndCreateAnyDocumentFromEdakia(String reqURL,String absolutefilePath){
		boolean isFileCreated = false;
		InputStream in = null;
		try {
			in = connectToEdakiaServer(reqURL);
			File file = new File(absolutefilePath);
			if(file.exists())
				file.delete();
			FileOutputStream f = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = in.read(buffer)) > 0) {
				f.write(buffer, 0, len1);
			}
			f.close();
			in.close();
			isFileCreated = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isFileCreated;
	}
	
	public static boolean readAndCreateImageDocumentFromEdakia(String reqURL,String absolutefilePath){
		boolean isFileCreated = false;
		InputStream in = null;
		try {
			in = connectToEdakiaServer(reqURL);
			OutputStream outStream = null;
			File file = new File(absolutefilePath);
			if(file.exists())
				file.delete();
			outStream = new FileOutputStream(file);
			Bitmap bitmap = BitmapFactory.decodeStream(in);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();
			in.close();
			isFileCreated = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isFileCreated;
	}
}
