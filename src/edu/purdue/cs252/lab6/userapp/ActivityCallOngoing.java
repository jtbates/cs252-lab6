package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ActivityCallOngoing extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_ongoing);
        
        final Button buttonCallEnd = (Button) findViewById(R.id.ButtonCallEnd);
        buttonCallEnd.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// TODO: Code to end call (notify other caller, stop voice capture & voice player)
            	
            	
            }
        });
    }
}
