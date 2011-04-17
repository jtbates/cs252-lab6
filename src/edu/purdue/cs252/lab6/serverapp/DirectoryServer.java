package edu.purdue.cs252.lab6.serverapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import edu.purdue.cs252.lab6.user.User;

public class DirectoryServer {
	static final String SERVERIP = "127.0.0.1";
	static final int SERVERPORT = 25201;
	
	
	public static void main(String[] args) {
		//UserList stored in an Arraylist
		ArrayList<User> userList = new ArrayList<User>();
        
		//Temp UserNames for testing
		userList.add(new User("User 1"));
		userList.add(new User("User 2"));
		userList.add(new User("User 3"));
		userList.add(new User("User 4"));
		
		try {
			// Create a socket for handling incoming requests
			ServerSocket server = new ServerSocket(SERVERPORT);

			do {
				// Wait for an incoming connection
				System.out.println("TCP S: Waiting for new connection...");
				Socket clientSocket = server.accept();
				System.out.println("TCP S: New connection received.");

				// Read data from the client
				InputStream stream = clientSocket.getInputStream();
				// InputStream is an abstract class. We needed to use a subclass
				BufferedReader data = new BufferedReader(new InputStreamReader(stream));

				// Read a line at a time
				String line;
				while ((line = data.readLine()) != null) {
					
					//If it is a new User logging in the first 5 letters will be login:
					if (line.substring(0,6).equals("login:")) {
						System.out.println("Adding to the user List");
						String name = line.substring(6);
						User temp = new User(name);
						
						//Check if the user is already logged in
						if (userList.contains(temp)) {
							userList.add(new User(name));
						}
						
					}
					//If the directory client is requesting the usernames
					else if (line.equals("GET USERNAMES")) {
						
					}
					
					
					System.out.println("TCP S: Received: '" + line + "'");
				}
				System.out.println("TCP S: Done.");

			} while (true);

		} catch (IOException e) {
			System.out.println("TCP S: Error" + e);
		}
	}
}
