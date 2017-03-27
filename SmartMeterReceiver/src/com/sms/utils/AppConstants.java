package com.sms.utils;

public interface AppConstants {

	String DEVICE_NUMBER = "+918547435447";
	String URL_PHONE_NUMBER = "+61408518670";

	String DEVICE_1ST_MESSAGE = "<9995876405";
	String READ_METER_MESSAGE_FROM_SERVER = "issue_bill";
	String READ_METER_MESSAGE_TO_DEVICE = "*";
	String CONNECTION_CUT_MESSAGE = "$0";
	String CONNECTION_ESTABLISH_MESSAGE = "$1";
	String RESET_COUNT_MESSAGE = "#";
	String URL_NUMBER = "URL NUMBER";
	String CONNECTION_CUT_MESSAGE_FROM_SERVER = "main_off";
	String CONNECTION_ESTABLISH_MESSAGE_FROM_SERVER = "main_on";
	String RESET_COUNT_MESSAGE_FROM_SERVER = "reset";

	String THRESHOLD_ALERT_MESSAGE = "Warning!";
	String RESET_SLAB_RANGE = "@";
	
	String SEND_READINGS_TO_ADMIN = "receiveMessage";
	String IP = "192.168.1.200";
	String NAME_SPACE = "http://webservice.smartmeter.com/";
	String URL = "http://" + IP + ":8080/SmartMeter/SmartMeterWebService?wsdl";
	CharSequence READ_METER_MESSAGE_FROM_CONSUMER = "view_units";

}
