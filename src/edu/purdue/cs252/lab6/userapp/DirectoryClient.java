package edu.purdue.cs252.lab6.userapp;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs252.lab6.*;

import android.util.Log;

public class DirectoryClient implements Runnable {
	static public String SERVERNAME = "10.0.2.2";
	static public int SERVERPORT = 25201;
	static public Object initMonitor = new Object();

	private User usr;
	private int Mode;

	//Stores the userList
	public String[] userList;
	
	//Controls the state of the program
	public DirectoryCommand state = null;
	
	/*
	* Summary:      Constructor for directory client
	* Parameters:   String usrName
	* Return: 		none
	*/   
	public DirectoryClient(String usrName) {
		usr = new User(usrName);
	}
	
	/*
	* Summary:      Sets the current state of the directory client
	* Parameters:   DirectoryCommand state
	* Return: 		void
	*/   
	public void setState(DirectoryCommand state) {
		this.state = state;
	}
	
	/*
	* Summary:      Logs the server user into the server
	* Parameters:   Socket clientSocket
	* Return: 		void
	*/  
	public void login(Socket clientSocket) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
		Log.d("Login:", usr.getUserName());
		
		//Write the DirectoryCommand then the user
		oos.writeObject((DirectoryCommand)DirectoryCommand.C_LOGIN);
		oos.writeObject(usr);
    	oos.flush();
    	
    	//Set the state to get the DirectoryCommand.C_Directory_Get
    	state = DirectoryCommand.C_DIRECTORY_GET;
	}
	
	/*
	* Summary:      Get the current users logged into the system
	* Parameters:   Socket clientSocket
	* Return: 		void
	*/  
	public void getDirectories(Socket clientSocket) throws IOException, ClassNotFoundException {
		//Create the Object output stream and sent the C_DIRECTORY_GET command
		ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
		oos.writeObject((DirectoryCommand)DirectoryCommand.C_DIRECTORY_GET);
		oos.flush();

		//Creat the ObjectInputstream and get the incoming directory command and hashmap
		ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
		DirectoryCommand dc = (DirectoryCommand)ois.readObject();
		ConcurrentHashMap<String,User> temp = (ConcurrentHashMap<String,User>)ois.readObject();

		Iterator i = temp.entrySet().iterator();
		userList = new String[temp.size()];
		
		int c = 0;
		//Traverse the Hashmap
		while(i.hasNext()) {
			Map.Entry<String,User>  me = (Map.Entry<String,User> )i.next();
			String user = me.getKey();
			Log.d("User", user);
			
			//Something to represent yourself on the user list
			if (user.equals(usr.getUserName())) {
				userList[c] = me.getKey() + "(You)";
			} else {
				userList[c] = me.getKey();
			}
			c = c + 1;
		}
		state = null;
	}
	
	public void run() {
		
		/*
		 * Server is not in a loop at the moment
		 * First is the user is logged in then the the directory is fetched
		 */
		try {
			InetAddress serverAddr = InetAddress.getByName(SERVERNAME);
			Socket clientSocket = new Socket(serverAddr, SERVERPORT);
			
			if (state == DirectoryCommand.C_LOGIN) {
				login(clientSocket);
			}
			
			if (state == DirectoryCommand.C_DIRECTORY_GET) {
				getDirectories(clientSocket);
				
				// Notify waiting threads that directory has finished loading
				synchronized(initMonitor) {
					initMonitor.notifyAll();
				}
			}		
			
			
		} catch (UnknownHostException e) {
			Log.e("TCP", "C: Error", e);
		} catch (IOException e) {
			Log.e("TCP", "C: Error", e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e("TCP", "C: Error", e);
		}
		
		
	}
}
