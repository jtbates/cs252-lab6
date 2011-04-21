package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ActivityHome extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        final EditText editTextUser = (EditText) findViewById(R.id.EditTextUser);
        final EditText editTextServer = (EditText) findViewById(R.id.EditTextServer);
        final Button buttonSignIn = (Button) findViewById(R.id.ButtonSignIn);
        
        buttonSignIn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
             // Set directory server
             DirectoryClient.SERVERNAME = editTextServer.getText().toString();
             System.out.println(DirectoryClient.SERVERNAME);
            
            
             //Switch Statement added in case multiple buttons are added to the original screen
             switch (v.getId()) {
             case R.id.ButtonSignIn:
             String username;
             username = editTextUser.getText().toString();
             Intent directoryIntent = new Intent(v.getContext(), ActivityDirectory.class);
             directoryIntent.putExtra("USER", username);
             startActivity(directoryIntent);
             break;
             }
            
               
            }
        });
    }
    
    
}