package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IncomingCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
    	// Switch to incoming call activity
    	Intent callIncomingIntent = new Intent(context,ActivityCallIncoming.class);
    	((Activity) context).startActivityForResult(callIncomingIntent,0);
	}

}
