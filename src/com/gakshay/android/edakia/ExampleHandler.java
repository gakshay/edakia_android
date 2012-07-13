package com.gakshay.android.edakia;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 
import android.util.Log;

public class ExampleHandler extends DefaultHandler { 
    StringBuffer buff = null;
    boolean buffering = false; 
    
    @Override
    public void startDocument() throws SAXException {
        // Some sort of setting up work
    } 
    
    @Override
    public void endDocument() throws SAXException {
        // Some sort of finishing up work
    } 
    
    @Override
    public void startElement(String namespaceURI, String localName, String qName, 
            Attributes atts) throws SAXException {
        if (localName.equals("qwerasdf")) {
            buff = new StringBuffer("");
            buffering = true;
        }   
    } 
    
    @Override
    public void characters(char ch[], int start, int length) {
        if(buffering) {
            buff.append(ch, start, length);
        }
    } 
    
    @Override
    public void endElement(String namespaceURI, String localName, String qName) 
    throws SAXException {
        if (localName.equals("blah")) {
            buffering = false; 
            String content = buff.toString();
            Log.e("parse", content);
            // Do something with the full text content that we've just parsed
        }
    }
}