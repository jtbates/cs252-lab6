package edu.purdue.cs252.lab6.userapp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
		try {
			DatagramPacket packet;
			final InetAddress serverAddr = InetAddress.getByName(server);
			
			while (!isInterrupted()) {
				try {
					byte[] buffer=new byte[140];
					// Read from input box
				
					// Send to server
					packet=new DatagramPacket(buffer,buffer.length,serverAddr,rPort);
					// Write to screen
				
				}
				catch(Exception e) {
					Log.e(TAG,e.toString());
					this.interrupt();
				}
			}
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
}