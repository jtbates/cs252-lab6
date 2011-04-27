package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.purdue.cs252.lab6.DirectoryCommand;
import edu.purdue.cs252.lab6.User;

public class ActivityHome extends Activity {
	public static final String PREFS_NAME = "mySettingsFile";

	VoipApp appState = null;
	
    public void Login(View v) {
    	// Set directory server
    	String server = ActivitySettings.serverName;
		final String username = ActivitySettings.userName;
		
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
	       		
   	       		if(msg.what == DirectoryCommand.S_STATUS_OK.getCode() && msg.obj.equals(DirectoryCommand.C_LOGIN)) {
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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        appState = (VoipApp) getApplicationContext();
        // set call state to idle
        Call.setState(Call.State.IDLE);
       
        SharedPreferences settings = getSharedPreferences(ActivityHome.PREFS_NAME, 0);
        ActivitySettings.userName = settings.getString("userName", "");
        ActivitySettings.serverName = settings.getString("serverName", "");
        
        final Button buttonLogin = (Button) findViewById(R.id.Login);
        buttonLogin.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Login(v);
        	}
        });

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        	case R.id.settings:
        		Intent help = new Intent(ActivityHome.this, ActivitySettings.class);
        		startActivity(help);
        		return true;
        	case R.id.quit:
        		finish();
        		return true;
        	case R.id.help:
        		//set up dialogs
            	final Dialog dialog = new Dialog(ActivityHome.this);
            	dialog.setContentView(R.layout.helpdialog);
            	dialog.setTitle("CS252 Voip");
            	dialog.setCancelable(true);
            	//there are a lot of settings, for dialog, check them all out!
            	//set up text
            	TextView text = (TextView) dialog.findViewById(R.id.TextView01);
            	text.setText(R.string.help_dialog);
            	//set up image view
            	ImageView img = (ImageView) dialog.findViewById(R.id.ImageView01);
            	img.setImageResource(R.drawable.cs252);
            	//set up button
            	Button button = (Button) dialog.findViewById(R.id.Button01);
            	button.setOnClickListener(new OnClickListener() {
                	public void onClick(View v) {
                		dialog.cancel();
                	}
                });
            	//now that the dialog is set up, it's time to show it
            	dialog.show();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
    }
       
    
}