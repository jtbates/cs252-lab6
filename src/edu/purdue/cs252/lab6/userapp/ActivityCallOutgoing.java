package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import edu.purdue.cs252.lab6.DirectoryCommand;

public class ActivityCallOutgoing extends Activity {
	private static final String TAG = "ACOutgoing";
    /** Called when the activity is first created. */
	DirectoryClient dc;
	Handler callOutgoingHandler;
	ToneGenerator ringback = null;
    
	@Override
	protected void onResume() {
		super.onResume();
		dc.setReadHandler(callOutgoingHandler);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_outgoing);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        final VoipApp appState = (VoipApp) getApplicationContext();
       	final Activity thisActivity = ActivityCallOutgoing.this;
        // get the directory client 
       	dc = appState.getDirectoryClient();
       	final String server = dc.getServer();
       	String username2 = Call.getUsername2();
       	
       	
       	
       	AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
       	if(ringback == null)
       	{
       		ringback = new ToneGenerator(AudioManager.STREAM_VOICE_CALL,ToneGenerator.MAX_VOLUME);
       		ringback.startTone(ToneGenerator.TONE_SUP_RINGTONE);
       	}
       	
       	callOutgoingHandler = new Handler() {
       		public void handleMessage(Message msg) {
       			Log.i(TAG,"callOutgoingHandler");
   	       		if(msg.what == DirectoryCommand.S_CALL_ACCEPTED.getCode()) {
   	       			ringback.stopTone();
   	       			ringback.release();
   	       			ringback = null;
   	       			Log.i(TAG,"call accepted");
   	       		}
   	       		else if (msg.what == DirectoryCommand.S_CALL_REJECT.getCode()) {
   	       			Log.i(TAG, "call rejected");
   	       			ringback.stopTone();
   	       			ringback.release();
   	       			ringback = null;
   	       			Call.setState(Call.State.IDLE);
   	       			finish();
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_REDIRECT_INIT.getCode()) {
   	       			try {
	   	       			int port = msg.arg1;
	   	       			ringback.stopTone();
	   	       			ringback.release();
	   	       			ringback = null;
	   	       			Call.setPort(port);
	   	       			VoicePlayerServer voicePlayerServer = new VoicePlayerServer(server,port);
	   	       			appState.setVoicePlayerServer(voicePlayerServer);
	   	       			voicePlayerServer.start();
	   	       			Intent callOngoingIntent = new Intent(thisActivity.getBaseContext(), ActivityCallOngoing.class);
	   	       			startActivityForResult(callOngoingIntent, 0);	
   	       			}
   	       			catch(Exception e) {
   	       				Log.e(TAG,"Call failed " + e.toString());
   	       				// TODO: handle failed call
   	       			}
   	       		} else if(msg.what == DirectoryCommand.S_REDIRECT_READY.getCode()) {
       				Log.i(TAG,"S_REDIRECT_READY");
       				ringback.stopTone();
       				ringback.release();
       				ringback = null;
   	       			VoiceCaptureClient voiceCaptureClient = new VoiceCaptureClient(server,Call.getPort());
   	       			appState.setVoiceCaptureClient(voiceCaptureClient);
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
        	
        	// Switch to call ongoing activi
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
