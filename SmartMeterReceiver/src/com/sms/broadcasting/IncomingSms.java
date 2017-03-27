package com.sms.broadcasting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.sms.utils.AppConstants;

public class IncomingSms extends BroadcastReceiver {
		
	Context context;

	final SmsManager sms = SmsManager.getDefault();

	/** Get the object of SmsManager **/

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		int count = 0;
		final Bundle bundle = intent.getExtras();
		if (count == 0) {

			//sendFirstMessage();
			/*try {
				String msg = AppConstants.DEVICE_1ST_MESSAGE;
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(AppConstants.DEVICE_NUMBER, null,
						msg, null, null);

				Toast.makeText(context, "DEVICE REGISTERATION MESSAGE SEND SUCCESSFULLY", Toast.LENGTH_LONG).show();
				Log.d("TAG", "DEVICE REGISTERATION MESSAGE SEND SUCCESSFULLY");
				count = count + 1;

			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(context, "SMS faild, please try again.",
						Toast.LENGTH_LONG).show();

				e.printStackTrace();
			}*/
		}
	
		try {
			if (bundle != null) {
				final Object[] pdusObj = (Object[]) bundle.get("pdus");
				for (int i = 0; i < pdusObj.length; i++) {
					SmsMessage curreSmsMessage = SmsMessage
							.createFromPdu((byte[]) pdusObj[i]);
					String phoneNumber = curreSmsMessage
							.getDisplayOriginatingAddress();

					String senderNumber = phoneNumber.trim();
					String message = curreSmsMessage.getDisplayMessageBody();
					Log.d("TAG", "PHONE NUMBER = " + phoneNumber
							+ " MESSAGE = " + message);

					// if (message.contains("#connect#")) {
					Toast.makeText(
							context,
							"senderNum: " + senderNumber + ", message: "
									+ message, Toast.LENGTH_LONG).show();
				}
			}
					
				
					
					
				
			
		} catch (Exception e) {
			// TODO: handle exception
			Log.d("SMSRECEIVER", "Exception smsReceiver" + e);
		}

	}
		}
	
	

