package edu.purdue.cs252.lab6.serverapp;

//Need to implement User class

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs252.lab6.DirectoryCommand;
import edu.purdue.cs252.lab6.User;

public class DirectoryServer {
	static final String SERVERIP = "127.0.0.1";
	static final int SERVERPORT = 25201, MAXC = 10;
	
	//Userlist for storing the users logged into the directory server
	private final ConcurrentHashMap<String, User> userList = new ConcurrentHashMap<String, User>();
	
	public static void main(String[] args) {
		DirectoryServer dserver = new DirectoryServer();
		int i = 0;

		try {
			// Create a socket for handling incoming requests
			ServerSocket listener = new ServerSocket(SERVERPORT);
			Socket client;
			
			while((i++ < MAXC) || (MAXC == 0)){
				System.out.println("TCP S: Waiting for new connection...");
		        	client = listener.accept();
				System.out.println("TCP S: New connection received.");
		        	acceptThread connect = dserver.new acceptThread(client);
		        	Thread t = new Thread(connect);
		        	t.start();
		      }
		} catch (IOException e) {
			System.out.println("TCP S: Error" + e);
			e.printStackTrace();
		}
	}
	
	class acceptThread implements Runnable
	{
		private Socket client;
		
		acceptThread(Socket client)
		{
			this.client = client;
		}
		
		public void run()
		{
			try
			{
					// Read data from the client
					InputStream stream = client.getInputStream();
					ObjectInputStream ois = new ObjectInputStream(stream); 
					
					DirectoryCommand command = (DirectoryCommand)ois.readObject();
					if(command != DirectoryCommand.C_LOGIN) 
						throw new IOException("Unrecognized command: " + command.toString());
					
					User u = (User)ois.readObject();
				
					login(u,client);
					
					switch(command) {
						case C_LOGOUT:
							
							break;
						case C_GETDIRECTORY:
						
							break;
						case C_PLACECALL:
							
							break;
						case C_ACCEPTCALL:
							
							break;
						default:
							// error, unrecognized command
					}
					

					
			} catch (IOException e) {
				System.out.println("TCP S: Error" + e);
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	private class clientThread implements Runnable {
		private Socket client;
		
		clientThread(Socket client) {
			
		}
		
		public void run() {
			
		}
	}

	private void login(User u, Socket client) {
		
		//if the user name is already taken
		if (userList.containsKey(u.getUserName())) {
			//Send message back to client that user name is invlaid
		}
		else {
			
		}
		
	}

}





