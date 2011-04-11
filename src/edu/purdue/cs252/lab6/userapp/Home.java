package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Home extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EditText editTextServer = (EditText) findViewById(R.id.EditTextServer);
        final Button buttonConnectDS = (Button) findViewById(R.id.ButtonConnectDS);
        buttonConnectDS.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Set server
            	DirectoryClient.SERVERNAME = editTextServer.getText().toString();
                // Start the directory client
        		new Thread(new DirectoryClient()).start();
            }
        });
    }
}