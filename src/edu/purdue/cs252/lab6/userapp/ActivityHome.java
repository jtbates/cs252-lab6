package edu.purdue.cs252.lab6.userapp;

import org.apache.http.auth.AuthenticationException;

import edu.purdue.cs252.lab6.DirectoryCommand;
import edu.purdue.cs252.lab6.User;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
        
        // global state
        final VoipApp appState = (VoipApp) getApplicationContext();
        // set call state to idle
        Call.setState(Call.State.IDLE);
        
        final EditText editTextUser = (EditText) findViewById(R.id.EditTextUser);
        final EditText editTextServer = (EditText) findViewById(R.id.EditTextServer);
        final Button buttonSignIn = (Button) findViewById(R.id.ButtonSignIn);
        
        buttonSignIn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Set directory server
            	String server = editTextServer.getText().toString();
       			final String username = editTextUser.getText().toString();
       			User user = new User(username);
       			appState.setUser(user);
       			
       	       	final ProgressDialog connectDialog = new ProgressDialog(ActivityHome.this);
       	       	connectDialog.setMessage("Connecting...");
       	       	connectDialog.setCancelable(true);
       	       	connectDialog.show();
       	       	Log.i("AH","Connect dialog.show");
       	       	
       	       	final View clickView = v;
       	       	final DirectoryClient dc;
       			
       	       	Handler loginHandler = new Handler() {
       	       		public void handleMessage(Message msg) {
       	       			Log.i("AH","loginHandler");
       	       			connectDialog.dismiss();
	       	       		if(msg.what == DirectoryCommand.S_STATUS_OK.getCode() && 
	       	       					msg.obj.equals(DirectoryCommand.C_LOGIN)) {
	       	       			Intent directoryIntent = new Intent(clickView.getContext(), ActivityDirectory.class);
	       	       			startActivity(directoryIntent);
	       	       		}
	       	       		else {
		   					CharSequence text = "Login failed";
		   					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
		   					toast.show();
	       	       		}
       	       		}
       	       	};
       	       	try {
            		dc = new DirectoryClient(server,user,loginHandler);
            		appState.setDirectoryClient(dc);
            		connectDialog.setMessage("Logging in...");
           	       	Log.i("AH","DirectoryClient constructed");
            		dc.login();
            	}
            	catch(Exception e) {
            		Log.e("AH",e.toString());
            		connectDialog.dismiss();
            		CharSequence text = "Could not connect to server";
            		Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
            		toast.show();
            	}

            	
               
            }
        });
    }
    
    
}