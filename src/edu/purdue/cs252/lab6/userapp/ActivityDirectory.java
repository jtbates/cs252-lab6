package edu.purdue.cs252.lab6.userapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import edu.purdue.cs252.lab6.DirectoryCommand;
import edu.purdue.cs252.lab6.User;
import edu.purdue.cs252.lab6.UserList;


/*
 * Class that contains the directory information after the user has logged in
 * The layout displayed is directory.xmll
 */
public class ActivityDirectory extends ListActivity {
	public static final int RESULT_INTERRUPTED = 1;
	public static final int RESULT_FAILED = 2;
	private DirectoryClient dc;
	private Handler handler;
	
	ConcurrentHashMap<String,User> userMap;
	VoipApp appState;
	User user;
	ArrayAdapter<String> adapter;
	
	public void createHandler() {
		handler = new Handler() {
	   		public void handleMessage(Message msg) {
	   			Log.i("AH","adHandler");
		       		if(msg.what == DirectoryCommand.S_DIRECTORY_SEND.getCode()) {
		       			userMap.clear();
					//userMap.putAll((Map<String,User>)msg.obj);
		       			//ArrayList<User> users = (ArrayList<User>)msg.obj;
		       			UserList uList = (UserList)msg.obj;
					for(int i=0; i<uList.size(); i++) {
						User u = uList.get(i);
						userMap.put(u.getUserName(),u);
					}
		       			
					adapter.clear();
					for(String username2 : userMap.keySet()) {
						if(!user.getUserName().equals(username2))
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
		       			Call.setUsername2(username2);
		       			Call.setState(Call.State.INCOMING);
		       			Intent callIncomingIntent = new Intent(ActivityDirectory.this, ActivityCallIncoming.class);
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
	}
	
   	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory);

        appState = (VoipApp) getApplicationContext(); 
        user = appState.getUser();
        Log.d("Login", user.getUserName());
        
        userMap = new ConcurrentHashMap<String,User>();
       	final ArrayList<String> usernameList = new ArrayList<String>();
       	
       	final Spinner s = (Spinner)findViewById(R.id.sort_by);
       	final ArrayAdapter<CharSequence> a = ArrayAdapter.createFromResource(
       	this, R.array.sort_by, android.R.layout.simple_spinner_item);

       	s.setAdapter(a);
		
       	// Create an ArrayAdapter to user for our ListActivity
       	Comparator<String> comparator = Collections.reverseOrder();
       	Collections.sort(usernameList,comparator);
       	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usernameList);
       	final ListActivity thisActivity = this;
		thisActivity.setListAdapter(adapter);
		
		s.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String str = parent.getItemAtPosition(pos).toString();
				if(!str.equals("A-Z")){
					Comparator<String> comparator = Collections.reverseOrder();
					Collections.sort(usernameList,comparator);
					adapter.notifyDataSetChanged();
				}
				if(!str.equals("Z-A")){
					Collections.sort(usernameList);
					adapter.notifyDataSetChanged();
				}
			}

			public void onNothingSelected(AdapterView<?> view) {
				// Do Nothing
			}		
		});
		createHandler();
		
        // get the directory client 
       	dc = appState.getDirectoryClient();
       	dc.setReadHandler(handler);
		dc.getDirectory();

		final Button buttonCall = (Button) findViewById(R.id.ButtonLogout);
        buttonCall.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	// TODO: Logout
            	try {
            	 	dc.logout();
             	} catch (Exception e) {
            	 	// TODO Auto-generated catch block
            	 	e.printStackTrace();
             	}
             	Intent intent = new Intent();
             	setResult(RESULT_OK, intent);
             	finish();
            }
        });
		
	
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
		    		   //TODO : implement the connect to the next userf
		    		   //Write to log to check if it is working
		    		   Log.d("Connect", "to the next user");
		    		   Call.setUsername2(username2);
		    		   Call.setState(Call.State.OUTGOING);
		    		   Intent callOutgoingIntent = new Intent(v.getContext(), ActivityCallOutgoing.class);
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
		dc.setReadHandler(handler);
    	
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
