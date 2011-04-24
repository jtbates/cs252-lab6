package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import edu.purdue.cs252.lab6.User;

public class ActivityCallOutgoing extends Activity {
    /** Called when the activity is first created. */
	
	User usr;
	static boolean running = true;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!running) {
        	Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        setContentView(R.layout.call_outgoing);
        Bundle extras = getIntent().getExtras();
        
        final User usr = (User)extras.get("USER");
        final User you = (User)extras.get("YOU");
        
        // TODO: cancel button
        
        try {
        	// Attempt connection to ringer server 
        	Thread ringing = new Thread(new RingerClient(usr,you));
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
