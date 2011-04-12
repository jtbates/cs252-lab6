package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActivityCallIncoming extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_incoming);
        
        final Button buttonCallAnswer = (Button) findViewById(R.id.ButtonCallAnswer);
        buttonCallAnswer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Start the voice player server
            	new Thread(new VoicePlayerServer()).start();
            	// TODO: Start voice capture client after confirming caller's voice player server has started
            	// Start the voice capture client
        		new Thread(new VoiceCaptureClient()).start();
            	
            	// Switch to ongoing call activity
                Intent callOngoingIntent = new Intent(v.getContext(), ActivityCallOngoing.class);
                startActivityForResult(callOngoingIntent, 0);
            }
        });
    }
}
