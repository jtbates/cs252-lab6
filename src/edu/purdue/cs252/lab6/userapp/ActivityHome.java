package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ActivityHome extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        final EditText editTextServer = (EditText) findViewById(R.id.EditTextServer);
        final Button buttonSignIn = (Button) findViewById(R.id.ButtonSignIn);
        buttonSignIn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Set directory server
            	DirectoryClient.SERVERNAME = editTextServer.getText().toString();
            	
            	// Switch to directory activity
                Intent directoryIntent = new Intent(v.getContext(), ActivityDirectory.class);
                startActivity(directoryIntent);

            }
        });
    }
}