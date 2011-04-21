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

public class ActivityCallIncoming extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_incoming);
        
        Bundle extras = getIntent().getExtras();
        final String username2 = extras.getString("username2");
        
        final TextView textWhosCalling = (TextView)findViewById(R.id.TextWhosCalling);
        textWhosCalling.setText(username2 + " is calling");
        
        final VoipApp appState = (VoipApp) getApplicationContext(); 
        // get the directory client 
       	final DirectoryClient dc = appState.getDirectoryClient();
       	
       	final Activity thisActivity = ActivityCallIncoming.this;
       	
       	final String server = dc.getServer();
       	
       	Handler aciHandler = new Handler() {
       		int port;
       		public void handleMessage(Message msg) {
       			Log.i("ACI","aciHandler");
   	       		if(msg.what == DirectoryCommand.S_CALL_INCOMING.getCode()) {
   	       			// ignore
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_REDIRECT_INIT.getCode()) {
   	       			port = msg.arg1;
   	       			VoicePlayerServer voicePlayerServer = new VoicePlayerServer(server,port);
   	       			voicePlayerServer.start();
   	       			dc.call_ready();
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_REDIRECT_READY.getCode()) {
   	       			VoiceCaptureClient voiceCaptureClient = new VoiceCaptureClient(server,port);
   	       			voiceCaptureClient.start();
   	       			
   	       			Intent callOngoingIntent = new Intent(thisActivity.getBaseContext(), ActivityCallOngoing.class);
   	       			startActivityForResult(callOngoingIntent, 0);
   	       		}
   	       		else {
   	       			Log.e("ACI","unrecognized message " + msg.what + " " + msg.obj.toString());
   	       			// unrecognized message
   	       			// TODO: handle error
   	       		}
       		}
       	};
        dc.setReadHandler(aciHandler);
        
        final Button buttonCallAnswer = (Button) findViewById(R.id.ButtonCallAnswer);
        buttonCallAnswer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
            	dc.call_answer(username2);
            	// Start the voice player server
            	//new Thread(new VoicePlayerServer()).start();
            	// TODO: Start voice capture client after confirming caller's voice player server has started
            	// Start the voice capture client
        		//new Thread(new VoiceCaptureClient()).start();
            	
            	// Switch to ongoing call activity
            }
        });
    }
}
