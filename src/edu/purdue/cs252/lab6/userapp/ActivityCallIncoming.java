package edu.purdue.cs252.lab6.userapp;

// Class for Incoming Calls

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.purdue.cs252.lab6.DirectoryCommand;

public class ActivityCallIncoming extends Activity {
	static final private String TAG = "ACIncoming";
	MediaPlayer mMediaPlayer;
    
	Handler callIncomingHandler;
	DirectoryClient dc;
	
	/** Called when the activity is resumed. */
	
    @Override
    protected void onResume() {
    	super.onResume();
    	dc.setReadHandler(callIncomingHandler);
    }
    
    /** Called when the activity is first created. */
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set layout
        setContentView(R.layout.call_incoming);
        
        final String username2 = Call.getUsername2();
        final TextView textWhosCalling = (TextView)findViewById(R.id.TextWhosCalling);
        textWhosCalling.setText(username2 + " is calling");
        
        final VoipApp appState = (VoipApp) getApplicationContext(); 
        // get the directory client 
       	dc = appState.getDirectoryClient();
       	final Activity thisActivity = ActivityCallIncoming.this;
       	// get the server
       	final String server = dc.getServer();
       	
       	//Initialize ringtone
       	
       	Uri alert = RingtoneManager.getActualDefaultRingtoneUri(getBaseContext(),RingtoneManager.TYPE_RINGTONE);
        mMediaPlayer = new MediaPlayer();
        try {
        	if(alert != null) {
        		mMediaPlayer.setDataSource(this, alert);

        		final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        		if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
        			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        			mMediaPlayer.setLooping(true);
        			mMediaPlayer.prepare();
        			mMediaPlayer.start();
        		}
        	}
        } catch (IllegalArgumentException e1) {
        	// TODO Auto-generated catch block
        	e1.printStackTrace();
        } catch (SecurityException e1) {
        	// TODO Auto-generated catch block
        	e1.printStackTrace();
        } catch (IllegalStateException e1) {
        	// TODO Auto-generated catch block
        	e1.printStackTrace();
        } catch (IOException e1) {
        	// TODO Auto-generated catch block
        	e1.printStackTrace();
        }
        
        callIncomingHandler = new Handler() {
       		public void handleMessage(Message msg) {
       			Log.i(TAG,"callIncomingHandler");
   	       		if(msg.what == DirectoryCommand.S_CALL_INCOMING.getCode()) {
   	       			// ignore
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_REDIRECT_INIT.getCode()) {
   	       			try {
	   	       			int port = msg.arg1;
	   	       			Call.setPort(port);
	   	       			VoicePlayerServer voicePlayerServer = new VoicePlayerServer(server,port);
	   	       			appState.setVoicePlayerServer(voicePlayerServer);
	   	       			// start VoicePlayerServer
	   	       			voicePlayerServer.start();
	   	       			Intent callOngoingIntent = new Intent(thisActivity.getBaseContext(), ActivityCallOngoing.class);
	   	       			startActivityForResult(callOngoingIntent, 0);
   	       			}
   	       			catch(Exception e) {
   	       				// TODO: handle failed call
   	       				Log.e(TAG,"Call failed " + e.toString());
   	       			}
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_REDIRECT_READY.getCode()) {
       				Log.i(TAG,"S_REDIRECT_READY");
   	       			VoiceCaptureClient voiceCaptureClient = new VoiceCaptureClient(server,Call.getPort());
   	       			appState.setVoiceCaptureClient(voiceCaptureClient);
   	       			voiceCaptureClient.start();
   	       		}
   	       		else {
   	       			Log.e(TAG,"unrecognized message " + msg.what + " " + msg.obj.toString());
   	       			// unrecognized message
   	       			// TODO: handle error
   	       		}
       		}
       	};
        dc.setReadHandler(callIncomingHandler);
        
        
        //ActionListener for the Reject Call Button
        //Once the button is clicked stop the ringtone
        //Let the server know the call is being rejected
        //End the current activity
        final Button buttonRejectCall = (Button) findViewById(R.id.ButtonRejectCall);
        buttonRejectCall.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		mMediaPlayer.stop();
        		dc.call_reject(username2);
	       		Call.setState(Call.State.IDLE);
	       		finish();
        	}
        });
       
        //ActionListener for the Answer Call Button
        //Once the button is clicked stop the ringtone
        //Answer call
        
        final Button buttonCallAnswer = (Button) findViewById(R.id.ButtonCallAnswer);
        buttonCallAnswer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Stop Ringtone
            	mMediaPlayer.stop();
            	// Answer Call
            	dc.call_answer(username2);
            }
        });
    }
}
