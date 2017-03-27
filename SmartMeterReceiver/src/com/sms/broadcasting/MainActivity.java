package com.sms.broadcasting;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
