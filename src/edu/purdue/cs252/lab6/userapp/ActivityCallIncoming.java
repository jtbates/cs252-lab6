package edu.purdue.cs252.lab6.userapp;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.purdue.cs252.lab6.User;

public class ActivityCallIncoming extends Activity {
    /** Called when the activity is first created. */
    
    static User usr;
    static MediaPlayer mMediaPlayer;
    static boolean running = true;
    Thread server;
    Thread client;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_incoming); 
        //Create ringtone to alert user of incoming call
        Uri alert = RingtoneManager.getActualDefaultRingtoneUri(getBaseContext(),RingtoneManager.TYPE_RINGTONE); 
        mMediaPlayer = new MediaPlayer();
        try {
			mMediaPlayer.setDataSource(this, alert);
		
			final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
				mMediaPlayer.setLooping(true);
				mMediaPlayer.prepare();
				//Call has been ended
				if(running) {
					mMediaPlayer.start();
				} else {
					Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    ActivityCallIncoming.running = false;
                    finish();
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

        
        final Button buttonCallAnswer = (Button) findViewById(R.id.ButtonCallAnswer);
        buttonCallAnswer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Stop ringtone
            	mMediaPlayer.stop();
            	// Start the voice player server
            	new VoicePlayerServer();
            	// TODO: Start voice capture client after confirming caller's voice player server has started
            	
            	// Start the voice capture client
        		new VoiceCaptureClient(usr);
        		
            	// Switch to ongoing call activity
        		Call.setState(Call.State.ONGOING);
        		
        		Intent callOngoingIntent = new Intent(v.getContext(), ActivityCallOngoing.class);
                ActivityCallOngoing.usr = usr;
                startActivityForResult(callOngoingIntent, 0);                
                
            }
        });
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	//Return to ActivityDirectory
    	//Intent intent = new Intent();
        //setResult(RESULT_OK, intent);
        //finish();
    }
}
