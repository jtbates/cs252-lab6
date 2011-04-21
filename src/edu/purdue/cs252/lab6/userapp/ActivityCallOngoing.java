package edu.purdue.cs252.lab6.userapp;

import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.purdue.cs252.lab6.User;

public class ActivityCallOngoing extends Activity {
    /** Called when the activity is first created. */
	
	User usr;
	static public int SERVERPORT = 25202;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (this.getIntent().getExtras() != null) {
        	usr = (User)extras.get("USER");
        }
        setContentView(R.layout.call_ongoing);
        
        
        final Button buttonCallEnd = (Button) findViewById(R.id.ButtonCallEnd);
        	buttonCallEnd.setOnClickListener(new OnClickListener() {
        		public void onClick(View v) {
            	// TODO: Code to end call (notify other caller, stop voice capture & voice player)
          
				Intent directoryIntent = new Intent(v.getContext(), ActivityDirectory.class);
    			startActivity(directoryIntent);
            	
            }
        });
    }
}
