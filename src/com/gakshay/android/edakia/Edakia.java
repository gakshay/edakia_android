package com.gakshay.android.edakia;


import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.widget.Toast;
import android.content.Intent;


public class Edakia extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void optionClickHandler(View view) {
    	switch(view.getId()){
    	case R.id.optionReceive:
    		Intent receiveIntent = new Intent(Edakia.this, ReceiveActivity.class);
			Edakia.this.startActivity(receiveIntent);
    		break;
    	case R.id.optionSend:
    		Intent authenticateIntent = new Intent(Edakia.this, AuthenticateActivity.class);
			Edakia.this.startActivity(authenticateIntent);
    		break;
    	default:
    		break;
    	}
    }
}
