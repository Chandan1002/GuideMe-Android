package com.sms.webservice;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.sms.utils.AppConstants;

public class WebService {
	
	String SOAP_ACTION = "";

	public String sendMessageToAdmin(String message, String deviceNumber,
			String bill) {
		// TODO Auto-generated method stub
		SOAP_ACTION = AppConstants.NAME_SPACE + AppConstants.SEND_READINGS_TO_ADMIN;
		SoapObject soapObject = new SoapObject(AppConstants.NAME_SPACE,
				AppConstants.SEND_READINGS_TO_ADMIN);
		soapObject.addProperty("message", message);
		soapObject.addProperty("meterNumber", deviceNumber);
		soapObject.addProperty("operation", bill);
		String result = upload(soapObject);
		return result;
	}

	private String upload(SoapObject soapObject) {
		// TODO Auto-generated method stub
		SoapSerializationEnvelope soapSerializationEnvelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		soapSerializationEnvelope.setOutputSoapObject(soapObject);
		HttpTransportSE se = new HttpTransportSE(AppConstants.URL);
		try {
			se.call(SOAP_ACTION, soapSerializationEnvelope);
			SoapPrimitive primitive = (SoapPrimitive) soapSerializationEnvelope
					.getResponse();
			return primitive.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}

}
