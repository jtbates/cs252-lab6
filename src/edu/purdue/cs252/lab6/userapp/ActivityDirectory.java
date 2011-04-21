package edu.purdue.cs252.lab6.userapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs252.lab6.DirectoryCommand;
import edu.purdue.cs252.lab6.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
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

	private DirectoryClient dc;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final VoipApp appState = (VoipApp) getApplicationContext(); 
        User user = appState.getUser();
        Log.d("Login", user.getUserName());
        
        final ConcurrentHashMap<String,User> userMap = new ConcurrentHashMap<String,User>();
       	final ArrayList<String> usernameList = new ArrayList<String>();
		
       	// Create an ArrayAdapter to user for our ListActivity
       	final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usernameList);
       	final ListActivity thisActivity = this;
		thisActivity.setListAdapter(adapter);
		
        // get the directory client 
       	dc = appState.getDirectoryClient();
       	Handler handler = new Handler() {
       		public void handleMessage(Message msg) {
       			Log.i("AH","adHandler");
   	       		if(msg.what == DirectoryCommand.S_DIRECTORY_SEND.getCode()) {
   	       			userMap.clear();
					userMap.putAll((Map<String,User>)msg.obj);
					
					adapter.clear();
					for(String username2 : userMap.keySet()) {
						adapter.add(username2);
						Log.i("AD","directory: " + username2);
					}
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_BC_USERLOGGEDIN.getCode()) {
   	       			User user2 = (User)msg.obj;
   	       			String username2 = user2.getUserName();
   	       			userMap.put(username2, user2);
   	       			adapter.add(username2);
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_BC_USERLOGGEDOUT.getCode()) {
   	       			String username2 = (String)msg.obj;
   	       			userMap.remove(username2);
   	       			adapter.remove(username2);
   	       		}
   	       		else if(msg.what == DirectoryCommand.S_CALL_INCOMING.getCode()) {
   	       			String username2 = (String)msg.obj;
   	       			Intent callIncomingIntent = new Intent(thisActivity.getBaseContext(), ActivityCallIncoming.class);
   	       			callIncomingIntent.putExtra("username2",username2);
   	       			startActivity(callIncomingIntent);
   	       		}
   	       		else {
   	       			Log.e("AD","unrecognized message " + msg.what);
   	       			// unrecognized message
   	       			// TODO: handle error
   	       		}
       		}
       	};
       	dc.setReadHandler(handler);
		dc.getDirectory();

       	

		
	
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
	
	/*
	* Summary:      Function called when a user is selected on the list
	* Parameters:   ListView, l, view v, int position, long id
	* Return: 		void
	*/   
	@Override
	protected void onListItemClick(ListView l, View view, int position, long id) {
		super.onListItemClick(l, view, position, id);
		final View v = view;
		//Get the last item that was clicked and store it into keyword
		Object o = this.getListAdapter().getItem(position);
		final String username2 = o.toString();
		
		//Build the Alert Box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to connect to " + username2 + "?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	   //Clicking Yes on the dialog box
		    	   public void onClick(DialogInterface dialog, int id) {
		    		   //TODO : implement the connect to the next user
		    		   //Write to log to check if it is working
		    		   Log.d("Connect", "to the next user");
		    		   Intent callOutgoingIntent = new Intent(v.getContext(), ActivityCallOutgoing.class);
		    		   callOutgoingIntent.putExtra("username2",username2);
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
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	dc.logout();
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
    public void onResume() {
    	super.onResume();
    	/*
    	// Capture ACTION_INCOMINGCALL broadcast
    	IntentFilter intentFilter = new IntentFilter(RingerServer.ACTION_INCOMINGCALL);
    	registerReceiver(new IncomingCallReceiver(),intentFilter);*/
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	// TODO: display notification if call fails
    }
    
    
    
    /*private class IncomingCallReceiver extends BroadcastReceiver {

    	@Override
    	public void onReceive(Context context, Intent intent) {
        	// Switch to incoming call activity
        	Intent callIncomingIntent = new Intent(context,ActivityCallIncoming.class);
        	((Activity) context).startActivityForResult(callIncomingIntent,0);
    	}

    }*/
}
