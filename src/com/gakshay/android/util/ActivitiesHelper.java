/**
 * 
 */
package com.gakshay.android.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

/**
 * @author Amitsharma
 *
 */
public class ActivitiesHelper {

	public static HashMap<String, String> fetchValuesFromReponse(String response){
		HashMap<String,String> responseValues = new HashMap<String,String>();
		try{

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			xpp.setInput( new StringReader ( response ) );
			int eventType = xpp.getEventType();
			String tag = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG && xpp.getName() != null){
					tag = xpp.getName();
					eventType = xpp.next();
					responseValues.put(tag, xpp.getText());
				}else{
					eventType = xpp.next();
				}
			}
		}catch(Exception anExcep){
			anExcep.printStackTrace();
		}
		return responseValues;
	}
	
	public static void deleteContentOfFile(File file)
			throws IOException{

		if(file.isDirectory() && file.list() != null && file.list().length != 0){
			//list all the directory contents
			String files[] = file.list();
			for (String temp : files) {
				//construct the file structure
				File fileDelete = new File(file, temp);
				if(fileDelete.isDirectory())				//recursive delete
					deleteContentOfFile(fileDelete);
				else{
					fileDelete.delete();
				}
			}
		}else{
			Log.d("File is already empty",file.getAbsolutePath());
		}
	}

}
