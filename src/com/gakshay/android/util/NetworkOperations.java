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
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * @author Amitsharma
 *
 */
public class NetworkOperations {

	private static final int NO_WRAP = 2;

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

	public static String authorizeHttpConnection(String urlStr, String mobile, String password) {
		InputStream in = null;
		String response = "";
		int resCode = -1;

		try {
			URL url = new URL(urlStr);
			URLConnection urlConn = url.openConnection();
			if (!(urlConn instanceof HttpURLConnection)) {
				throw new IOException ("URL is not an Http URL");
			}


			String authString = mobile + ":" + password;

			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			HttpURLConnection httpConn = (HttpURLConnection)urlConn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setRequestProperty("Authorization", "Basic " + authStringEnc);

			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect(); 

			resCode = httpConn.getResponseCode();                 
			if (resCode == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream(); 
				char[] inputBuffer = new char[2000];
				InputStreamReader isr = new InputStreamReader(in);
				int charRead;

				while ((charRead = isr.read(inputBuffer))>0)
				{      
					String readString = 
							String.copyValueOf(inputBuffer, 0, charRead);                    
					response += readString;
					inputBuffer = new char[2000];
				}
			}else if(resCode == HttpURLConnection.HTTP_UNAUTHORIZED){
				response = "Exception401";
			}else{
				response = "Exception";
			}		
		} catch (MalformedURLException e) {
			response = "Exception";
		} catch (IOException e) {
			response = "Exception";
		}
		return response;
	}

	public static String authorizeToEdakiaServer(String urlStr,String name, String password) {
		String response = null;
		try {

			String authString = name + ":" + password;

			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			URL url = new URL(urlStr);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			InputStream is = urlConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);

			int numCharsRead;
			char[] charArray = new char[1024];
			StringBuffer sb = new StringBuffer();
			while ((numCharsRead = isr.read(charArray)) > 0) {
				sb.append(charArray, 0, numCharsRead);
			}
			response = sb.toString();
			is.close();
			isr.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			response = "Exception";
		} catch (IOException e) {
			if(e.getMessage() != null && e.getMessage().contains("401"))//unauthorize message
				response = "Exception401";
			e.printStackTrace();
			response = "Exception";
		}catch (Exception ex){
			ex.printStackTrace();
			response = "Exception";
		}
		return response;

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


	public static String  chngPwdReqToEdakia(String connURL,String connUsername, String connPassword,String newPassword,String oldPassword) {
		String result = null;
		try {

			String authString = connUsername + ":" + connPassword;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);

			URL url = new URL(connURL);
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);


			// Encode according to application/x-www-form-urlencoded specification
			String content =
					"user[password]=" + URLEncoder.encode(newPassword, "UTF-8") +
					"&user[password_confirmation]=" + URLEncoder.encode(newPassword, "UTF-8");

			urlConnection.setRequestProperty("Content-Type", "text/plain"); 
			urlConnection.setRequestProperty("Content-Length",  "" + content.getBytes().length); 

			// Write body
			OutputStream output = urlConnection.getOutputStream(); 
			output.write(content.getBytes());
			output.flush();
			output.close();

			InputStream is = urlConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);

			int numCharsRead;
			char[] charArray = new char[1024];
			StringBuffer sb = new StringBuffer();
			while ((numCharsRead = isr.read(charArray)) > 0) {
				sb.append(charArray, 0, numCharsRead);
			}
			result = sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}


	public static String sendToEdakiaServer(String urlStr,String senderMobile, String senderPassword,String receiverMobile,String file,String userId,String serialNumber,String receiverEmailAdd) {
		String response = null;
		HttpClient httpclient = null;
		try {
			// Add your data
			httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(urlStr);
			String authString = senderMobile + ":" + senderPassword;
			byte[] authEncBytes = android.util.Base64.encode(authString.getBytes(), NO_WRAP);
			String authStringEnc = new String(authEncBytes);

			httppost.setHeader("Authorization", "Basic " + authStringEnc);

			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			//Identify MIME type of the document

			String mimeType = (MimeTypeMap.getSingleton()).getMimeTypeFromExtension((MimeTypeMap.getFileExtensionFromUrl(file)));
			SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyyHHmm");

			String fileNameToBeSaved = senderMobile.substring(6) + "_" + receiverMobile.substring(6) + "_" + dateFormatter.format(new Date()) + "." + (MimeTypeMap.getFileExtensionFromUrl(file));
			
			entity.addPart("transaction[document_attributes][doc]",new FileBody (new File(file) ,fileNameToBeSaved, mimeType,null));
			entity.addPart("transaction[receiver_mobile]",new StringBody(receiverMobile));
			entity.addPart("transaction[receiver_email]",new StringBody(receiverEmailAdd));
			entity.addPart("transaction[document_attributes][user_id]",new StringBody(userId));
			entity.addPart("transaction[sender_mobile]",new StringBody(senderMobile));
			entity.addPart("serial_number",new StringBody(serialNumber));

			httppost.setEntity(entity);
			/*httppost.setHeader("Accept", mimeType);
			httppost.setHeader("Content-Type", mimeType);*/

			HttpResponse httpResponse = httpclient.execute(httppost);
			InputStream respStream = (InputStream)httpResponse.getEntity().getContent();
			InputStreamReader isr = new InputStreamReader(respStream);

			int numCharsRead;
			char[] charArray = new char[2048];
			StringBuffer sb = new StringBuffer();
			while ((numCharsRead = isr.read(charArray)) > 0) {
				sb.append(charArray, 0, numCharsRead);
			}
			response = sb.toString();

			HttpEntity resEntity = httpResponse.getEntity();
			Log.d("Response from server while uploading file : ",httpResponse.getStatusLine().toString());
			Log.d("", response);
			resEntity.consumeContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response="Exception";
		} catch (IOException e) {
			e.printStackTrace();
			response="Exception";
		}finally{
			httpclient.getConnectionManager().shutdown();

		}
		return response;

	}


	public static String changePassword(String urlStr,String senderMobile, String senderPassword,String newPassword) {
		String response = null;
		HttpClient httpclient = null;
		try {
			// Add your data
			httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(urlStr);
			String authString = senderMobile + ":" + senderPassword;
			byte[] authEncBytes = android.util.Base64.encode(authString.getBytes(), NO_WRAP);
			String authStringEnc = new String(authEncBytes);

			httppost.setHeader("Authorization", "Basic " + authStringEnc);

			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			//Identify MIME type of the document

			entity.addPart("user[password]",new StringBody(newPassword));
			entity.addPart("user[password_confirmation]",new StringBody(newPassword));

			httppost.setEntity(entity);

			HttpResponse httpResponse = httpclient.execute(httppost);
			InputStream respStream = (InputStream)httpResponse.getEntity().getContent();
			InputStreamReader isr = new InputStreamReader(respStream);

			int numCharsRead;
			char[] charArray = new char[2048];
			StringBuffer sb = new StringBuffer();
			while ((numCharsRead = isr.read(charArray)) > 0) {
				sb.append(charArray, 0, numCharsRead);
			}
			response = sb.toString();

			HttpEntity resEntity = httpResponse.getEntity();
			Log.d("Response from server changing password : ",httpResponse.getStatusLine().toString());
			Log.d("", response);
			resEntity.consumeContent();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response="Exception";
		} catch (IOException e) {
			e.printStackTrace();
			response="Exception";
		}finally{
			httpclient.getConnectionManager().shutdown();

		}
		return response;

	}



}
