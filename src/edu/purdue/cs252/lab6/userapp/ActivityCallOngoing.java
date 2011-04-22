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

public class ActivityCallOngoing extends Activity {
	private static final String TAG = "ACOngoing";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_ongoing);
        Call.setState(Call.State.ONGOING);
        
        final VoipApp appState = (VoipApp) getApplicationContext(); 
        // get the directory client 
       	final DirectoryClient dc = appState.getDirectoryClient();
       	final Activity thisActivity = ActivityCallOngoing.this;

       	final String server = dc.getServer();
       	dc.call_ready();
      	
       	Handler callOngoingHandler = new Handler() {
       		public void handleMessage(Message msg) {
       			Log.i(TAG,"callOngoingHandler");
       			if(msg.what == DirectoryCommand.S_REDIRECT_READY.getCode()) {
       				Log.i(TAG,"S_REDIRECT_READY");
   	       			VoiceCaptureClient voiceCaptureClient = new VoiceCaptureClient(server,Call.getPort());
   	       			voiceCaptureClient.start();
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_CALL_INCOMING.getCode()) {
   	       			// ignore
   	       		}
   	       		else {
   	       			// unrecognized message
   	       			Log.e(TAG,"unrecognized message " + msg.what);
   	       			if(msg.obj != null) Log.e(TAG,msg.obj.toString());
   	       			// TODO: handle error
   	       		}
       		}
       	};
        dc.setReadHandler(callOngoingHandler);

        
        final Button buttonCallEnd = (Button) findViewById(R.id.ButtonCallEnd);
        buttonCallEnd.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// TODO: Code to end call (notify other caller, stop voice capture & voice player)
            	
            	
            }
        });
    }
}
