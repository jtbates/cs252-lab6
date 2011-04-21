package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.purdue.cs252.lab6.User;

public class ActivityCallIncoming extends Activity {
    /** Called when the activity is first created. */
    
    User usr;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        final User usr = (User)extras.get("USER");
        setContentView(R.layout.call_incoming);   
        
        final Button buttonCallAnswer = (Button) findViewById(R.id.ButtonCallAnswer);
        buttonCallAnswer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Start the voice player server
            	new Thread(new VoicePlayerServer()).start();
            	// TODO: Start voice capture client after confirming caller's voice player server has started
            	try
    			{
    				Thread.sleep(500);
    			} catch(InterruptedException e){}
            	// Start the voice capture client
        		new Thread(new VoiceCaptureClient()).start();
        		
            	// Switch to ongoing call activity
        		Call.setState(Call.State.ONGOING);
        		
        		Intent callOngoingIntent = new Intent(v.getContext(), ActivityCallOngoing.class);
                callOngoingIntent.putExtra("USER", usr);
                startActivityForResult(callOngoingIntent, 0);
            }
        });
    }
}
