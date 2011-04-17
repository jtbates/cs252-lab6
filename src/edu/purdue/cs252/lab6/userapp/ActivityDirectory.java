package edu.purdue.cs252.lab6.userapp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs252.lab6.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View.OnClickListener;


/*
 * Class that contains the directory information after the user has logged in
 * The layout displayed is directory.xml
 */
public class ActivityDirectory extends ListActivity {
	public static final int RESULT_INTERRUPTED = 1;
	public static final int RESULT_FAILED = 2;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		//Get the last item that was clicked and store it into keyword
		Object o = this.getListAdapter().getItem(position);
		String keyword = o.toString();
		
		//Build the Alert Box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to connect to " + keyword + "?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id) {
		    		   //TODO : implement the connect to the next user
		    		   //Write to log to check if it is working
		    		   Log.d("Connect", "to the next user");
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               //cancel dialog action 
		        	   dialog.cancel();
		           }
		       });
		
		//Create and show dialog box
		AlertDialog alert = builder.create();
		alert.show();
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        
        //Temp user list stored
        String[] names = new String[] { "User 1", "User 2", "User 3", "User 4"};
		// Create an ArrayAdapter, that will actually make the Strings above
		// appear in the ListView
		this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names));
    
        //Get the username sent from the previous activity
        String userName = "";
        if (this.getIntent().getExtras() != null) {
        	userName = extras.getString("USER");
        }
        Log.d("Login", userName);
        
        
        // Start the directory client
       	DirectoryClient dc = new DirectoryClient(userName);
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
   		
		/*Start ringer server
		
		//Intent rsIntent = new Intent(this, RingerServer.class);
		//startService(rsIntent);
		
        final Button buttonCall = (Button) findViewById(R.id.ButtonCall);
        buttonCall.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// Switch to outgoing call activity
                Intent callOutgoingIntent = new Intent(v.getContext(), ActivityCallOutgoing.class);
                startActivityForResult(callOutgoingIntent, 0);
            }
        });   */     
    }

	@Override
    public void onResume() {
    	super.onResume();
    	
    	// Capture ACTION_INCOMINGCALL broadcast
    	IntentFilter intentFilter = new IntentFilter(RingerServer.ACTION_INCOMINGCALL);
    	registerReceiver(new IncomingCallReceiver(),intentFilter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	// TODO: display notification if call fails
    }
    
    
    
    private class IncomingCallReceiver extends BroadcastReceiver {

    	@Override
    	public void onReceive(Context context, Intent intent) {
        	// Switch to incoming call activity
        	Intent callIncomingIntent = new Intent(context,ActivityCallIncoming.class);
        	((Activity) context).startActivityForResult(callIncomingIntent,0);
    	}

    }
}
