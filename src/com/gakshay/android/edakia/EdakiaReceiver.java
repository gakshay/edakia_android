package com.gakshay.android.edakia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EdakiaReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, Edakia.class);
        context.startService(startServiceIntent);
    }
}