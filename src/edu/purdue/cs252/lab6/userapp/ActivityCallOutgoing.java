package edu.purdue.cs252.lab6.userapp;

import edu.purdue.cs252.lab6.DirectoryCommand;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityCallOutgoing extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_outgoing);

        
        
        
        final VoipApp appState = (VoipApp) getApplicationContext();
       	final Activity thisActivity = ActivityCallOutgoing.this;
        // get the directory client 
       	final DirectoryClient dc = appState.getDirectoryClient();
       	final String server = dc.getServer();
       	String username2 = Call.getUsername2();
       	
       	Handler callOutgoingHandler = new Handler() {
       		public void handleMessage(Message msg) {
       			Log.i("ACO","acoHandler");
   	       		if(msg.what == DirectoryCommand.S_CALL_ACCEPTED.getCode()) {
   	       			Log.i("ACO","call accepted");
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_REDIRECT_INIT.getCode()) {
   	       			int port = msg.arg1;
   	       			Call.setPort(port);
   	       			VoicePlayerServer voicePlayerServer = new VoicePlayerServer(server,port);
   	       			voicePlayerServer.start();
   	       			Intent callOngoingIntent = new Intent(thisActivity.getBaseContext(), ActivityCallOngoing.class);
   	       			startActivityForResult(callOngoingIntent, 0);	
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_CALL_INCOMING.getCode()) {
   	       			// ignore
   	       		}
   	       		else {
   	       			// unrecognized message
   	       			Log.e("ACO","unrecognized message " + msg.what);
   	       			if(msg.obj != null) Log.e("ACO",msg.obj.toString());
   	       			// TODO: handle error
   	       		}
       		}
       	};
        dc.setReadHandler(callOutgoingHandler);
       	dc.call_attempt(username2);
        
        final TextView textCallingWhom = (TextView)findViewById(R.id.TextCallingWhom);
        textCallingWhom.setText("Calling " + username2 + "...");

        
        // TODO: cancel button
        
        try {
        	// Attempt connection to ringer server 
        	//Thread ringing = new Thread(new RingerClient());
        	//ringing.start();
        	
        	//ringing.join();        	
        	// Connection successful
        	
        	// Switch to call ongoing activity
            //Intent callOngoingIntent = new Intent(this, ActivityCallOngoing.class);
            //startActivity(callOngoingIntent);
        }
        catch (Exception InterruptedException) {
        	Intent intent = new Intent();
        	setResult(ActivityDirectory.RESULT_INTERRUPTED, intent);
        	finish();
        }
    }

}
