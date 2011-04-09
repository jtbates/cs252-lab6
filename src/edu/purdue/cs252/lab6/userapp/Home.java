package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Home extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Button button = (Button) findViewById(R.id.ButtonConnectDS);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Start the directory client
        		new Thread(new DirectoryClient()).start();
            }
        });
    }
}