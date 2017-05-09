package com.app.utils;


import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class SOAPParsing {

    String result = null;
	
	// constructor
	public SOAPParsing(){
		
	}
	
	public String getJSONFromUrl(String soapAction, SoapObject parameters){
		
		Log.v("parameters", ""+parameters);
		
		MarshalBase64 mbase = new MarshalBase64();// marshal is used to serialize the byte array

	    SoapSerializationEnvelope envelop = new SoapSerializationEnvelope(SoapEnvelope.VER11);
	    envelop.bodyOut = parameters;
	    envelop.encodingStyle = SoapSerializationEnvelope.ENC;
	    envelop.dotNet = true;
	    envelop.setOutputSoapObject(parameters);

	    HttpTransportSE aht = new HttpTransportSE(Constants.URL);

	    mbase.register(envelop);

	    try {
	    	
			aht.call(soapAction,envelop);
			
			if (envelop.bodyIn != null) {
				Log.v("result", ""+envelop.getResponse());
				result = (String) envelop.getResponse();
		
			}
			
		/*	if (Build.VERSION.SDK != null
					&& Build.VERSION.SDK_INT > 13) {
				Log.v("in if", "in if");
				ServiceConnection connection = aht.getServiceConnection();
				connection.disconnect();
			
			}*/
			
			
	    } catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (NullPointerException e){
			e.printStackTrace();
		}
	    
		return result;
	}
}
