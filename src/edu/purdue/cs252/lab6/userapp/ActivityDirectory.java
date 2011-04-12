package edu.purdue.cs252.lab6.userapp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.view.View.OnClickListener;

public class ActivityDirectory extends Activity {
	public static final int RESULT_INTERRUPTED = 1;
	public static final int RESULT_FAILED = 2;
	//Map users = new ConcurrentHashMap<String,User>(); 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory);
        
        final ListView listviewDirectory = (ListView) findViewById(R.id.ListViewDirectory);
        
       	
        // Start the directory client
       	DirectoryClient dc = new DirectoryClient();
   		Thread dcThread = new Thread(dc);
   		dcThread.start();
   		try {
   			// wait until directory is loaded
   			synchronized(dc.initMonitor) {
   				dc.initMonitor.wait();
   			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   		
		// Start ringer server
		Intent rsIntent = new Intent(this, RingerServer.class);
		startService(rsIntent);
		
        final Button buttonCall = (Button) findViewById(R.id.ButtonCall);
        buttonCall.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Switch to outgoing call activity
                Intent callOutgoingIntent = new Intent(v.getContext(), ActivityCallOutgoing.class);
                startActivityForResult(callOutgoingIntent, 0);
            }
        });        
    }

    @Override
    public void onResume() {
    	super.onResume();
    	IntentFilter intentFilter = new IntentFilter(RingerServer.ACTION_INCOMINGCALL);
    	registerReceiver(new IncomingCallReceiver(),intentFilter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	// TODO: display notification if call fails
    }
    
    public void addUser() {
    	
    }
    
    public void removeUser() {
    	
    }
}
