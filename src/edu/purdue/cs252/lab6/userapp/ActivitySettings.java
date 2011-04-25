package edu.purdue.cs252.lab6.userapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class ActivitySettings extends Activity {
	
	public static String userName;
	public static String serverName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        EditText et = (EditText)findViewById(R.id.EditTextUser);
        et.setText(userName);
        
        et = (EditText)findViewById(R.id.EditTextServer);
        et.setText(serverName);
    }
    
    
    public void myClickHandler(View view) {
    	EditText et = (EditText)findViewById(R.id.EditTextUser);
    	userName = et.getText().toString();
    	
    	et = (EditText)findViewById(R.id.EditTextServer);
    	serverName = et.getText().toString();
    	
    	finish();
    }
    
    
    @Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(ActivityHome.PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("userName", userName);
      editor.putString("serverName", serverName);
      // Commit the edits!
      editor.commit();
    }

}
