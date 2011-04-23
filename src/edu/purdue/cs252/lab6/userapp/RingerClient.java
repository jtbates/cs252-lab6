package edu.purdue.cs252.lab6.userapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;
import edu.purdue.cs252.lab6.User;

public class RingerClient implements Runnable {
	static public String SERVERNAME = "10.0.2.2";
	static public int SERVERPORT = 25202;
	User usr;
	
	public RingerClient(User usr) {
		this.usr = usr;
	}
	

	public void run() {
		try {
			InetAddress serverAddr = InetAddress.getByName(usr.getUserIp());

			// Connect to the server
			Log.d("TCP", "C: Connecting to " + usr.getUserName() + " on " + usr.getUserIp());
			Socket clientSocket = new Socket(serverAddr, SERVERPORT);
			
			Call.setState(Call.State.OUTGOING);
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
			
			oos.writeObject(Call.getState());
			oos.writeObject(usr);
			oos.flush();
			
			
			Call.setState(Call.State.ONGOING);
			// Start the voice player server
			
	        new Thread(new VoicePlayerServer()).start();
	        // Start voice capture client
	        new Thread(new VoiceCaptureClient(usr)).start();
	        
        	// Close the connection
			//clientSocket.close();
		} catch (UnknownHostException e) {
			Log.e("TCP", "C: Error", e);
		} catch (IOException e) {
			Log.e("TCP", "C: Error", e);
		}
	}
}
