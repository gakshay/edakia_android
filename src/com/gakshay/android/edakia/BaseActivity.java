package com.gakshay.android.edakia;

import java.lang.reflect.Method;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.support.v4.app.NavUtils;

public class BaseActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_base);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.activity_base, menu);
		return true;
	}


	public void rediretHomeActivity(View view) {
		Intent homeIntent = new Intent(getApplicationContext(), Edakia.class);
		homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeIntent);
		finish();
	}

	public String getSerialNumber(){
		String serial = android.os.Build.SERIAL;

		if(serial != null && !"".equalsIgnoreCase(serial) && !"unknown".equalsIgnoreCase(serial))
			return serial;

		serial = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		if(serial != null && !"".equalsIgnoreCase(serial))
			return serial;


		if(serial == null || "unknown".equalsIgnoreCase(serial) || "".equalsIgnoreCase(serial)){
			try {
				Class<?> c = Class.forName("android.os.SystemProperties");
				Method get = c.getMethod("get", String.class);
				serial = (String) get.invoke(c, "ro.serialno");
			} catch (Exception ignored) {
			}
		}	

		return serial;
	}

	public void toggleEmail(View view){
		LinearLayout mobile_layout = (LinearLayout)this.findViewById(R.id.optionalMobileLayout);
		LinearLayout email_layout = (LinearLayout)this.findViewById(R.id.optionalEmailLayout);
		mobile_layout.setVisibility(LinearLayout.GONE);
		email_layout.setVisibility(LinearLayout.VISIBLE);
	}

	public void toggleMobile(View view){
		LinearLayout mobile_layout = (LinearLayout)this.findViewById(R.id.optionalMobileLayout);
		LinearLayout email_layout = (LinearLayout)this.findViewById(R.id.optionalEmailLayout);
		mobile_layout.setVisibility(LinearLayout.VISIBLE);
		email_layout.setVisibility(LinearLayout.GONE);
	}

}
