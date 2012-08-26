/**
 * 
 */
package com.gakshay.android.util;

import java.io.StringReader;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

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

}
