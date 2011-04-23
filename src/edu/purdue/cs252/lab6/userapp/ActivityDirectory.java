package edu.purdue.cs252.lab6.userapp;

import java.io.IOException;

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
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import edu.purdue.cs252.lab6.User;


/*
 * Class that contains the directory information after the user has logged in
 * The layout displayed is directory.xml
 */
public class ActivityDirectory extends ListActivity {
	public static final int RESULT_INTERRUPTED = 1;
	public static final int RESULT_FAILED = 2;
	
	DirectoryClient dc = null;
	static User selected = null;
	private String array_spinner[];
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		final View view = v;

		//Get the last item that was clicked and store it into keyword
		Object o = this.getListAdapter().getItem(position);
		//String keyword = o.toString();
		selected = (User)o;
		
		//Build the Alert Box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to connect to " + selected + "?\nIP Address: " + selected.getUserIp() + "\nStatus: " + selected.connectionStatus())
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	   //Clicking Yes on the dialog box
		    	   public void onClick(DialogInterface dialog, int id) {
		    		   //TODO : implement the connect to the next user
		    		   //Write to log to check if it is working
		    		   Log.d("Connect", "to the next user");
		    		   Intent callOutgoingIntent = new Intent(view.getContext(), ActivityCallOutgoing.class);
		    		   callOutgoingIntent.putExtra("USER", selected);
		    	       startActivity(callOutgoingIntent);
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		    	   //Clicking No on the dialog box
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
        setContentView(R.layout.directory);
        Bundle extras = getIntent().getExtras();
        
        String userName = "";
       
        if (this.getIntent().getExtras() != null) {
        	
        		userName = extras.getString("USER");
        		try {
            		dc = new DirectoryClient(userName);
            	} catch(Exception e) {}
                
                Log.d("Login", userName);
        	
        
        	
        }   
   		
   		try {
   			// wait until directory is loaded
   			synchronized(dc.initMonitor) {
   				dc.initMonitor.wait();
   			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Create an ArrayAdapter, that will actually ma			userMap.remove(username);ke the Strings above
		// appear in the ListView
		
		array_spinner = new String[2];
		array_spinner[0] = "Alphabetical";
		array_spinner[1] = "Elegant";
		
		final Spinner s = (Spinner)findViewById(R.id.sort_by);
		ArrayAdapter a = new ArrayAdapter(this, android.R.layout.simple_spinner_item, array_spinner);
		s.setAdapter(a);
		
		this.setListAdapter(new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, dc.userList));
		
		final Button buttonCall = (Button) findViewById(R.id.ButtonLogout);
        buttonCall.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// TODO: Logout	
            	try {
					dc.logout();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	Intent intent = new Intent();
            	setResult(RESULT_OK, intent);
            	finish();
            }
        });
    
		
   		
		//Start ringer server
		
		Intent rsIntent = new Intent(this, RingerServer.class);
		startService(rsIntent);
		
             
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
    		Bundle extras = intent.getExtras();
    		final User usr = (User)extras.get("USER");
    		
        	// Switch to incoming call activity
        	Intent callIncomingIntent = new Intent(context,ActivityCallIncoming.class);
        	ActivityCallIncoming.usr = usr;
        	((Activity) context).startActivityForResult(callIncomingIntent,0);
    	}

    }
}
