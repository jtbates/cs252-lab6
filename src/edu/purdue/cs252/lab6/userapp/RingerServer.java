package edu.purdue.cs252.lab6.userapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import edu.purdue.cs252.lab6.User;

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
						
						ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
						ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
						
						Call.State cs = (Call.State) ois.readObject();
						
						if(cs == Call.State.OUTGOING)
						{
							if(Call.getState() == Call.State.IDLE)
							{
								User u = (User)ois.readObject();
								// Set call state
								Call.setState(Call.State.INCOMING);
								
								// Send broadcast
								Intent intentIncomingCall = new Intent(ACTION_INCOMINGCALL);
								intentIncomingCall.putExtra("USER", u);
								sendBroadcast(intentIncomingCall);
							}
						}
							
						
						Log.d("TCP", "RS: Done.");

						

					} while (true);
					

				} catch (IOException e) {
					System.out.println("TCP S: Error" + e);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}	
		};
		t.start();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// if killed after returning, restart
		return START_STICKY;
	}
	
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
