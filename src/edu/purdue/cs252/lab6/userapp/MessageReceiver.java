package edu.purdue.cs252.lab6.userapp;

import android.util.Log;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MessageReceiver extends Thread {
	private static final String TAG = "VCC";
	static public DatagramSocket socket;
	
	MessageReceiver(String server, int port) throws UnknownHostException, SocketException {
		super();
		socket = new DatagramSocket();
	}
	
	public void run () {
		while(!isInterrupted()) {
			try {
				// Read packet
					
				// Write to application window
				
			}
			catch (Exception e) {
				Log.e(TAG, "Error: "+ e);
			}
		}		
	}
}
