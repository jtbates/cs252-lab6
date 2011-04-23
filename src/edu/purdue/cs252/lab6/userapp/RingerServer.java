package edu.purdue.cs252.lab6.userapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class RingerServer extends Service {
	public static final String ACTION_INCOMINGCALL = "edu.purdue.cs252.lab6.ACTION_INCOMINGCALL";
	
	static String SERVERNAME = "10.0.2.2";
	static int SERVERPORT = 25202;
	Thread t;
	
	@Override
	public void onCreate() {
		t = new Thread() {
			public void run() {
				try {
					// Create a socket for handling incoming requests
					ServerSocket server = new ServerSocket(SERVERPORT);

					do {
						// Wait for an incoming connection
						Log.d("TCP", "RS: Waiting for new connection...");
						Socket clientSocket = server.accept();
						Log.d("TCP", "RS: New connection received.");

						// Read data from the client
						InputStream stream = clientSocket.getInputStream();
						// InputStream is an abstract class. We needed to use a subclass
						BufferedReader data = new BufferedReader(new InputStreamReader(stream));

						// Read a line at a time
						String line;
						while ((line = data.readLine()) != null) {
							Log.d("TCP", "RS: Received: '" + line + "'");
						}
						Log.d("TCP", "RS: Done.");

						// Set call state
						Call.setState(Call.State.INCOMING);
						
						// Send broadcast
						Intent intentIncomingCall = new Intent(ACTION_INCOMINGCALL);
						sendBroadcast(intentIncomingCall);

					} while (true);
					

				} catch (IOException e) {
					System.out.println("TCP S: Error" + e);
				} 
			}	
		};
		t.start();
	}
	
	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// if killed after returning, restart
		//return START_STICKY;
	}*/
	
	@Override
	public IBinder onBind(Intent intent) {
		// no binding provided
		return null;
	}
	
	@Override
	public void onDestroy() {
		t.interrupt();
	}
	
}
