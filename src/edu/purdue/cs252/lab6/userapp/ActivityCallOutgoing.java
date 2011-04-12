package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActivityCallOutgoing extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_outgoing);
        // TODO: cancel button
        
        try {
        	// Attempt connection to ringer server 
        	Thread ringing = new Thread(new RingerClient());
        	ringing.start();
        	
        	ringing.join();        	
        	// Connection successful
        	
        	// Switch to call ongoing activity
            Intent callOngoingIntent = new Intent(this, ActivityCallOngoing.class);
            startActivity(callOngoingIntent);
        }
        catch (Exception InterruptedException) {
        	Intent intent = new Intent();
        	setResult(ActivityDirectory.RESULT_INTERRUPTED, intent);
        	finish();
        }
    }

}
