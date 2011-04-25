package edu.purdue.cs252.lab6.userapp;

import java.net.DatagramSocket;
import android.util.Log;

public class MessageSender extends Thread {
	private static final String TAG = "VCC";
	static public DatagramSocket socket;
	private String server;
	private int rPort;
	
	MessageSender(String server, int port) {
		super();
		this.server = server;
		this.rPort = port;
		MessageSender.socket = MessageReceiver.socket;
	}
	
	public void run () {
		while (!isInterrupted()) {
			try {
			
				// Read from input box
			
				// Send to stream
			
			}
			catch(Exception e) {
				Log.e(TAG,e.toString());
				this.interrupt();
			}
		}
	}
}