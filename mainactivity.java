package com.think.guideme_activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import app.akexorcist.bluetoothspp.BluetoothSPP;
import app.akexorcist.bluetoothspp.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetoothspp.BluetoothSPP.BluetoothStateListener;
import app.akexorcist.bluetoothspp.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetoothspp.BluetoothState;
import app.akexorcist.bluetoothspp.DeviceList;

import com.think.guideme.R;
import com.think.guideme_reciever.GpsSerivces;
import com.think.guideme_reciever.SendGPSCordinatesServices;
import com.think.guideme_util.EmailValidator;
import com.think.guideme_util.GPSUtil;

public class MainActivity extends Activity implements OnInitListener,
		OnClickListener, BluetoothConnectionListener, BluetoothStateListener,
		OnDataReceivedListener {
	public static final String USER_EMAIL_ID = "USER_EMAIL_ID";
	public static final String USER_MAIL_PASSWORD = "USER_MAIL_PASSWORD";
	private static final int TIME_THRESHOLD = 1000;
	private static final int FORCE_THRESHOLD = 700;
	public static final String MY_DIVICE_INTENT = "MY_DIVICE_INTENT";
	private long mLastTime;
	Handler handler = null;
	Vibrator vibrator = null;
	AudioManager manager = null;
	TextView sendSmstTextView = null;
	TextView sendEmailTextView = null;
	TextView makeCallTextView = null;
	TextView readMailsTextView = null;
	TextView captureImageTextView = null;
	TextView sendMyLocationTextView = null;
	FrameLayout changingBackFrameLayout = null;
	BatteryBroadCast batteryBroadCast = null;

	TextView exitTextView = null;
	int[] options = null;
	int selection = 0;
	TextView[] allTextViews;
	TextToSpeech speech = null;
	int selected;
	Button showContactButton = null;
	Button showEmailsButton = null;
	Button configButton = null;
	BluetoothSPP bt = null;
	Button myEmailButton = null;
	private Handler hand;

	int colorFlag = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		handler = new Handler();
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		manager = (AudioManager) getSystemService(AUDIO_SERVICE);
		setUI();
		batteryBroadCast = new BatteryBroadCast();
		IntentFilter filter = new IntentFilter(
				"android.intent.action.BATTERY_LOW");
		registerReceiver(batteryBroadCast, filter);

		GPSUtil gpsUtil = new GPSUtil(this);
		if (!gpsUtil.isProviderEnabled()) {
			gpsUtil.enableGPSSettings();
		}

		startService(new Intent(this, GpsSerivces.class));
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				MediaPlayer create = MediaPlayer.create(MainActivity.this,
						R.raw.please_select_your_options_using_volume_key);
				if (create.isPlaying()) {
					create.stop();
				} else {
					create.start();
					vibrator.vibrate(2100);
				}
			}
		}, 1000 * 3);

	}

	private void setUI() {
		sendSmstTextView = (TextView) findViewById(R.id.sendSmSTextView);
		sendEmailTextView = (TextView) findViewById(R.id.sendEmailTextView);
		makeCallTextView = (TextView) findViewById(R.id.makeCallTextView);
		exitTextView = (TextView) findViewById(R.id.exitTextView);
		readMailsTextView = (TextView) findViewById(R.id.readMailsTextView);
		captureImageTextView = (TextView) findViewById(R.id.captureImageTextView);
		sendMyLocationTextView = (TextView) findViewById(R.id.sendMyLocationTextView);
		showContactButton = (Button) findViewById(R.id.showContactButton);
		showEmailsButton = (Button) findViewById(R.id.showEmailsButton);
		configButton = (Button) findViewById(R.id.configButton);
		myEmailButton = (Button) findViewById(R.id.myEmailAddressButton);
		changingBackFrameLayout = (FrameLayout) findViewById(R.id.changingBackground);

		showContactButton.setOnClickListener(this);
		showEmailsButton.setOnClickListener(this);
		configButton.setOnClickListener(this);
		myEmailButton.setOnClickListener(this);

		bt = new BluetoothSPP(this);
		bt.setBluetoothConnectionListener(this);
		bt.setBluetoothStateListener(this);
		bt.setOnDataReceivedListener(this);
		if (!bt.isBluetoothAvailable()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
		}

		// load values
		options = new int[7];
		options[0] = R.raw.send_sms;
		options[1] = R.raw.make_a_call;
		options[2] = R.raw.send_email_messages;
		options[3] = R.raw.read_emails;
		options[4] = R.raw.capture_image;
		options[5] = R.raw.send_my_location;
		options[6] = R.raw.exit;

		allTextViews = new TextView[7];
		allTextViews[0] = sendSmstTextView;
		allTextViews[1] = makeCallTextView;
		allTextViews[2] = sendEmailTextView;
		allTextViews[3] = readMailsTextView;
		allTextViews[4] = captureImageTextView;
		allTextViews[5] = sendMyLocationTextView;
		allTextViews[6] = exitTextView;
		speech = new TextToSpeech(this, this);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (!bt.isBluetoothEnabled()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
		} else {
			bt.setupService();
			bt.startService(BluetoothState.DEVICE_OTHER);
			if (bt.getServiceState() != BluetoothState.STATE_CONNECTED) {
				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(this);
				String string = preferences.getString(MY_DIVICE_INTENT, "");

				if (!string.equals("")) {
					try {
						Intent parseUri = Intent.parseUri(string, 0);
						bt.connect(parseUri);
						Log.d("TAG", "Connetced");
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						Log.e("TAG", "Error");
						e.printStackTrace();
					}
				}
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == BluetoothState.REQUEST_ENABLE_BT
				&& resultCode == RESULT_OK) {
			bt.setupService();
			bt.startService(BluetoothState.DEVICE_OTHER);
		} else if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE
				&& resultCode == RESULT_OK) {
			String uri = data.toURI();
			PreferenceManager.getDefaultSharedPreferences(this).edit()
					.putString(MY_DIVICE_INTENT, uri).commit();
			Log.d("TAG", " URI : " + uri);
			bt.connect(data);

		}

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		MediaPlayer player = null;
		int streamMaxVolume = manager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				Toast.makeText(getApplicationContext(), "UP",
						Toast.LENGTH_SHORT).show();
				Log.d("SELECTION", "UP :" + selection);
				player = MediaPlayer.create(this, options[selection]);
				selected = selection;
				bt.send(allTextViews[selection].getText().toString(), true);
				allTextViews[selection].setTextColor(Color.RED);

				if (player.isPlaying()) {
					player.stop();
				} else {
					player.start();
				}

				if (selection == 0) {
					selection = 0;
				} else {

					selection = selection - 1;

				}

			}
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				Toast.makeText(getApplicationContext(), "Down",
						Toast.LENGTH_SHORT).show();
				Log.d("SELECTION", "Down :" + selection);
				allTextViews[selection].setTextColor(Color.BLACK);

				player = MediaPlayer.create(this, options[selection]);
				bt.send(allTextViews[selection].getText().toString(), true);
				selected = selection;
				if (player.isPlaying()) {
					player.stop();
				} else {
					player.start();
				}
				if (selection == 6) {
					selection = 6;
				} else {
					allTextViews[selection].setTextColor(Color.BLACK);
					selection = selection + 1;

				}

			}
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (!allTextViews[selected].getText().toString()
						.equals(readMailsTextView.getText().toString())) {
					speech.speak("you selected "
							+ allTextViews[selected].getText().toString(),
							TextToSpeech.QUEUE_FLUSH, null);
				}

				if (allTextViews[selected].getText().toString()
						.equals(sendSmstTextView.getText().toString())) {
					startActivity(new Intent(this, MainSmsctivity.class));
				} else if (allTextViews[selected].getText().toString()
						.equals(sendEmailTextView.getText().toString())) {
					startActivity(new Intent(this, MailSendingActivity.class));
				} else if (allTextViews[selected].getText().toString()
						.equals(makeCallTextView.getText().toString())) {
					startActivity(new Intent(this, CallActivity.class));
				} else if (allTextViews[selected].getText().toString()
						.equals(exitTextView.getText().toString())) {

					speech.speak("your application now exitted ",
							TextToSpeech.QUEUE_FLUSH, null);
					try {
						Thread.sleep(1000 * 2);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// finish();

					// timer = new Timer();
					// timer.schedule(task = new TimerTask() {
					//
					// @Override
					// public void run() {
					// // TODO Auto-generated method stub
					//
					// }
					// }, 0L, 500);
					finish();

				} else if (allTextViews[selected].getText().toString()
						.equals(readMailsTextView.getText().toString())) {
					// calls asynch task
					SharedPreferences preferences = PreferenceManager
							.getDefaultSharedPreferences(this);

					new ReadLastMail().execute(
							preferences.getString(USER_EMAIL_ID, ""),
							preferences.getString(USER_MAIL_PASSWORD, ""));

					MediaPlayer.create(this, R.raw.mail_is_reading).start();
				} else if (allTextViews[selected].getText().toString()
						.equals(captureImageTextView.getText().toString())) {
					startActivity(new Intent(this, CameraImage.class));
				} else if (allTextViews[selected].getText().toString()
						.equals(sendMyLocationTextView.getText().toString())) {
					startService(new Intent(this,
							SendGPSCordinatesServices.class));
				}
			}
		} else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				Toast.makeText(getApplicationContext(), "Event back",
						Toast.LENGTH_SHORT).show();
			}
		}

		manager.setStreamVolume(AudioManager.STREAM_MUSIC, streamMaxVolume,
				AudioManager.FLAG_VIBRATE);
		return super.dispatchKeyEvent(event);
	}

	private void changeBackGround(Handler handl) {
		// TODO Auto-generated method stub
		handl.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (colorFlag == 0) {
					changingBackFrameLayout.setBackgroundColor(Color.RED);
					colorFlag = 1;
				} else {
					changingBackFrameLayout.setBackgroundColor(Color.BLUE);
					colorFlag = 0;
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status != TextToSpeech.ERROR) {
			speech.setLanguage(Locale.US);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (speech != null) {
			speech.shutdown();
		}
		if (batteryBroadCast != null) {
			unregisterReceiver(batteryBroadCast);
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreatePanelMenu(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == R.id.addEmailItem) {
			startActivity(new Intent(this, AddEmailActivity.class));
		} else if (item.getItemId() == R.id.addContactItem) {
			startActivity(new Intent(this, AddContactActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == showContactButton.getId()) {
			startActivity(new Intent(this, ShowContactActivity.class));
		} else if (v.getId() == showEmailsButton.getId()) {
			startActivity(new Intent(this, ShowEmailActivity.class));
		} else if (v.getId() == configButton.getId()) {
			startActivityForResult(new Intent(this, DeviceList.class),
					BluetoothState.REQUEST_CONNECT_DEVICE);
		} else if (v.getId() == myEmailButton.getId()) {
			showDialoge("Add Mail");
		}
	}

	private void showDialoge(String title) {
		// TODO Auto-generated method stub
		View view = getLayoutInflater().inflate(R.layout.dialoge_view, null);
		final EditText emailEditText = (EditText) view
				.findViewById(R.id.nameEmailEditText);
		final EditText passwordEditText = (EditText) view
				.findViewById(R.id.editText2);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(MainActivity.this);
		String email = preferences.getString(USER_EMAIL_ID, "");
		String pass = preferences.getString(USER_MAIL_PASSWORD, "");

		if (!email.equals("") && !pass.equals("")) {
			emailEditText.setText(email);
			passwordEditText.setText(pass);
		}

		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setPositiveButton("OK", null)
				.setNegativeButton("Cancel", null).setTitle(title)
				.setView(view).create();
		alertDialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				// TODO Auto-generated method stub
				Button okButton = alertDialog
						.getButton(AlertDialog.BUTTON_POSITIVE);
				Button cancelButton = alertDialog
						.getButton(AlertDialog.BUTTON_NEGATIVE);
				okButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (emailEditText.getText().toString().equals("")) {
							emailEditText.setError("Enter email id");
						} else if (!EmailValidator.emailValidator(emailEditText
								.getText().toString())) {
							emailEditText.setError("Enter a valid email id");
						} else if (passwordEditText.getText().toString()
								.equals("")) {
							passwordEditText.setError("Enter password");
						} else {
							alertDialog.dismiss();
							PreferenceManager
									.getDefaultSharedPreferences(
											MainActivity.this)
									.edit()
									.putString(MainActivity.USER_EMAIL_ID,
											emailEditText.getText().toString())
									.putString(
											MainActivity.USER_MAIL_PASSWORD,
											passwordEditText.getText()
													.toString()).commit();
						}
					}

				});

				cancelButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						alertDialog.dismiss();
					}

				});
			}

		});
		alertDialog.show();
	}

	@Override
	public void onDeviceConnected(String name, String address) {
		// TODO Auto-generated method stub

		Toast.makeText(getApplicationContext(), "Device Connected to " + name,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDeviceDisconnected() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Device Disconnected",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDeviceConnectionFailed() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Device connection failed",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onServiceStateChanged(int state) {
		// TODO Auto-generated method stub
		if (state == BluetoothState.STATE_CONNECTED) {
			// Do something when successfully connected
			Toast.makeText(getApplicationContext(), "Device connected",
					Toast.LENGTH_SHORT).show();
		} else if (state == BluetoothState.STATE_CONNECTING) {
			// Do something while connecting
			Toast.makeText(getApplicationContext(), "Device connecting",
					Toast.LENGTH_SHORT).show();
		} else if (state == BluetoothState.STATE_LISTEN) {
			// Do something when device is waiting for connection
		} else if (state == BluetoothState.STATE_NONE) {
			// Do something when device don't have any connection
			Toast.makeText(getApplicationContext(), "Device not connected",
					Toast.LENGTH_SHORT).show();

		}
	}

	@Override
	public void onDataReceived(byte[] data, String message) {
		// TODO Auto-generated method stub
		long now = System.currentTimeMillis();
		if ((now - mLastTime) > TIME_THRESHOLD) {
			long diff = now - mLastTime;
			long l = diff * 10000;
			Log.d("TAG", "Reading : " + message);
			if (l > FORCE_THRESHOLD) {
				if (message.contains("R")) {
					try {
						String[] split = message.split("R");
						Log.d("TAG", "Count : " + split.length);
						String string = split[1];
						Log.d("TAG", string);
						int parseInt = Integer.parseInt(string);
						if (parseInt < 100) {
							vibrator.vibrate(1000);
							MediaPlayer.create(this, R.raw.obstacle).start();
							Calendar nowCalendar = Calendar.getInstance();

							int hour = nowCalendar.get(Calendar.HOUR_OF_DAY); // Get
																				// hour
																				// in
																				// 24
																				// hour
																				// format
							int minute = nowCalendar.get(Calendar.MINUTE);

							Date date = parseDate(hour + ":" + minute);
							Date dateCompareOne = parseDate("07:00 PM");
							
							
							
							
							
							
							
							//////      SOS ALERT 

							if (date.before(dateCompareOne)) {
								Log.d("TAG", " MY_TIME");
								hand = new Handler();
								changeBackGround(hand);
							} else {
								Log.d("TAG", "NOT MY_TIME");
								changingBackFrameLayout
										.setBackgroundColor(Color.WHITE);
							}

						}
					} catch (Exception e) {
						Log.e("TAG", "Reading : " + message);
					}
				}

			}
			mLastTime = now;
		}

	}

	private Date parseDate(String date) {

		final String inputFormat = "HH:mm";
		SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat,
				Locale.US);
		try {
			return inputParser.parse(date);
		} catch (java.text.ParseException e) {
			return new Date(0);
		}
	}

	private class ReadLastMail extends AsyncTask<String, Void, String> {

		private Folder folder;
		private Address address;
		private String messageContent;

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			try {

				String protocol = "pop3";
				String host = "pop.gmail.com";
				String port = "995";
				String userName = params[0];
				String password = params[1];
				Properties properties = getServerProperties(protocol, host,
						port);
				Session session = Session.getDefaultInstance(properties);
				Store store = session.getStore(protocol);
				store.connect(host, userName, password);
				folder = store.getFolder("Inbox");
				folder.open(Folder.READ_ONLY);
				int messageCount = folder.getMessageCount();
				String msg = "";
				String subject = "";
				if (messageCount > 0) {
					Message message = folder.getMessage(folder
							.getMessageCount());
					Address[] from = message.getFrom();
					address = from[0];
					subject = message.getSubject();
					if (message.getContent() instanceof InputStream) {
						DataHandler dataHandler = message.getDataHandler();
						DataSource dataSource = dataHandler.getDataSource();
						MimeMultipart mimeMultipart = new MimeMultipart(
								dataSource);

						Part part1 = mimeMultipart.getBodyPart(0);
						msg = IOUtils.toString(
								(InputStream) part1.getContent(), "gb2312");

					}
					folder.close(false);
					store.close();
					return subject + " content  " + msg;

				}

			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("ERROR", "" + e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			if (result != null) {
				speech.speak("From " + address + ".   subject  " + result,
						TextToSpeech.QUEUE_FLUSH, null);
			} else {
				speech.speak("No emails", TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	private class BatteryBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("android.intent.action.BATTERY_LOW")) {
				vibrator.vibrate(3000);
				MediaPlayer.create(context, R.raw.battery_low);
			}
		}

	}

	private Properties getServerProperties(String protocol, String host,
			String port) {
		Properties properties = new Properties();

		// server setting
		properties.put(String.format("mail.%s.host", protocol), host);
		properties.put(String.format("mail.%s.port", protocol), port);

		// SSL setting
		properties.setProperty(
				String.format("mail.%s.socketFactory.class", protocol),
				"javax.net.ssl.SSLSocketFactory");
		properties.setProperty(
				String.format("mail.%s.socketFactory.fallback", protocol),
				"false");
		properties.setProperty(
				String.format("mail.%s.socketFactory.port", protocol),
				String.valueOf(port));

		return properties;
	}
}
