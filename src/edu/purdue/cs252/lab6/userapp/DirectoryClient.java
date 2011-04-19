package edu.purdue.cs252.lab6.userapp;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs252.lab6.*;

import android.util.Log;

public class DirectoryClient extends Thread {
	static public String SERVERNAME = "10.0.2.2";
	static public int SERVERPORT = 25201;
	static public Object initMonitor = new Object();

	//Current user login information
	public User usr;

	//Stores the userList
	public String[] userList;
	
	//Controls the state of the program
	private DirectoryCommand state = DirectoryCommand.C_WAITING;
	
	InetAddress serverAddr;
	Socket clientSocket;
		
	/*
	* Summary:      Constructor for directory client
	* Parameters:   String usrName
	* Return: 		none
	*/   
	public DirectoryClient(String usrName) throws Exception {
		usr = new User(usrName);
		serverAddr = InetAddress.getByName(SERVERNAME);
		clientSocket = new Socket(serverAddr, SERVERPORT);
		this.start();
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
    	//Because once the user is logged in we have to need to recieve all the directories
    	state = DirectoryCommand.C_DIRECTORY_GET;
	}
	
	/*
	* Summary:      Ask the server for the directories
	* Parameters:   Socket clientSocket
	* Return: 		void
	*/  
	public void getDirectories(Socket clientSocket) throws IOException, ClassNotFoundException {
		//Create the Object output stream and sent the C_DIRECTORY_GET command
		ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
		oos.writeObject((DirectoryCommand)DirectoryCommand.C_DIRECTORY_GET);
		oos.flush();
		
		state = DirectoryCommand.C_WAITING;
	}
	
	
	public void run() {
		
		ReceiveThread receiver = null;
		try {
			receiver = new ReceiveThread(clientSocket, this);
		} catch (Exception e) {
			Log.e("TCP", "C: Error", e);
		}
		
		//Boolean to ensure the receiver is only called once
		boolean started = false;
		
		
		while(true) {
			
			if (!started) {
				receiver.start();
				started = true; 
			}
			
			try {
				switch(state) {
					case C_LOGIN:
						Log.d("Client Command", "C_LOGIN");
						login(clientSocket);
						break;
					case C_DIRECTORY_GET:
						getDirectories(clientSocket);
						Log.d("Client Command", "C_DIRECTORY_GET");
					default:
						//yield();
						break;
				}
				
			} catch (UnknownHostException e) {
				Log.e("TCP", "C: Error", e);
			} catch (IOException e) {
				Log.e("IO", "C: Error", e);
			} catch (ClassNotFoundException e) {
				Log.e("ClassNotFound", "C: Error", e);
			}
		}
		
	}
}

/*
 * Thread for Receiving messages from the directory server
 * 
 */
class ReceiveThread extends Thread {
	   private Socket client = null;
	   private DirectoryClient dClient;

	   /*
		* Summary:      Constructor for ReceiveThread
		* Parameters:   Socket clientSocket, DrectoryClient dc
		* Return: 		void
		*/  
	   public ReceiveThread(Socket clientSocket, DirectoryClient dc) throws Exception{
	     client = clientSocket;
	     this.dClient = dc;
	   }

	   /*
		* Summary:      Receives the HashMap from the server, saves it into the directoryClient userlist and notfity the initMonitor
		* Parameters:  	ObjectInputStream ois
		* Return: 		void
		*/
	   public void recieveDirectories(ObjectInputStream ois) throws StreamCorruptedException, IOException, ClassNotFoundException {
			//Create the ObjectInputstream and get the incoming directory command and HashMap
			ConcurrentHashMap<String,User> temp = (ConcurrentHashMap<String,User>)ois.readObject();

			Iterator i = temp.entrySet().iterator();
			dClient.userList = new String[temp.size()];
			
			int c = 0;
			//Traverse the HashMap
			while(i.hasNext()) {
				Map.Entry<String,User>  me = (Map.Entry<String,User> )i.next();
				String user = me.getKey();
				Log.d("User", user);
				
				//Something to represent yourself on the user list
				if (user.equals(dClient.usr.getUserName())) {
					dClient.userList[c] = me.getKey() + "(You)";
				} else {
					dClient.userList[c] = me.getKey();
				}
				c = c + 1;
			}
			
			// Notify waiting threads that directory has finished loading
			synchronized(dClient.initMonitor) {
				dClient.initMonitor.notifyAll();
			}
	   }
	   
	   /*
		* Summary:      Run method, waits for message from the server and executes the correct command
		* Parameters:   none
		* Return: 		void
		*/
	   public void run() {
		   while(true) {
		      try {
		    	  ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
		    	  DirectoryCommand dc = (DirectoryCommand)ois.readObject();
		    
					switch(dc) {
						case S_STATUS_OK:
							Log.d("Server Command", "S_STATUS_OK");
							break;
						case S_DIRECTORY_SEND:
							Log.d("Server Command", "S_DIRECTORY_SEND");
							recieveDirectories(ois);
						default:
							break;
					}
		       
		      } catch(Exception e) {}       
		   }
	   }
}

