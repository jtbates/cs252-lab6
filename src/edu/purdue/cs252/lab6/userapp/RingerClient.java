package edu.purdue.cs252.lab6.userapp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class RingerClient implements Runnable {
	static public String SERVERNAME = "10.0.2.2";
	static public int SERVERPORT = 25202;
	
	public void run() {
		try {
			InetAddress serverAddr = InetAddress.getByName(SERVERNAME);

			// Connect to the server
			Log.d("TCP", "C: Connecting to " + SERVERNAME + " on " + SERVERPORT);
			Socket clientSocket = new Socket(serverAddr, SERVERPORT);
			
			// Sending a message to the server
			Log.d("TCP", "C: Sending a packet.");
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(clientSocket.getOutputStream())), true);
			out.println("Hello from RingerClient");
			Log.d("TCP", "C: Sent.");
			Log.d("TCP", "C: Done.");

			// Start the voice player server
        	//new Thread(new VoicePlayerServer()).start();
			
        	// Start voice capture client
        	//new Thread(new VoiceCaptureClient()).start();
			
        	// Close the connection
			clientSocket.close();
		} catch (UnknownHostException e) {
			Log.e("TCP", "C: Error", e);
		} catch (IOException e) {
			Log.e("TCP", "C: Error", e);
		}
	}
}
