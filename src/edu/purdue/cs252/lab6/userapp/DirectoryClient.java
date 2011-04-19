package edu.purdue.cs252.lab6.userapp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.purdue.cs252.lab6.DirectoryCommand;
import edu.purdue.cs252.lab6.User;

import android.util.Log;

public class DirectoryClient implements Runnable {
	static public String SERVERNAME = "10.0.2.2";
	static public int SERVERPORT = 25201;
	static public Object initMonitor = new Object();

	private User usr;
	private int Mode;
	
	/*
	* Summary:      Constructor for directory client
	* Parameters:   String usrName
	* Return: 		none
	*/   
	public DirectoryClient(String usrName) {
		usr = new User(usrName);
	}

	
	public void run() {
		try {
			InetAddress serverAddr = InetAddress.getByName(SERVERNAME);

			// Connect to the server
			Log.d("TCP", "C: Connecting to " + SERVERNAME + " on " + SERVERPORT);
			Socket clientSocket = new Socket(serverAddr, SERVERPORT);
		
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			oos.writeObject(DirectoryCommand.C_LOGIN);
			oos.writeObject(usr);
			oos.flush();
			
			
			/*
			// Sending a message to the server
			Log.d("TCP", "C: Sending a packet.");
			PrintWriter out = new PrintWriter(new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())), true);
			
			//Send the inital message with the usrName
			out.println("login:" + usr.getUserName());
			Log.d("TCP", "C: Sent.");
			Log.d("TCP", "C: Done.");
			*/
			
			// Close the connection
			//clientSocket.close();
			
			// Notify waiting threads that directory has finished loading
			synchronized(initMonitor) {
				initMonitor.notifyAll();
			}
		} catch (UnknownHostException e) {
			Log.e("TCP", "C: Error", e);
		} catch (IOException e) {
			Log.e("TCP", "C: Error", e);
		}
		
		
	}
}
