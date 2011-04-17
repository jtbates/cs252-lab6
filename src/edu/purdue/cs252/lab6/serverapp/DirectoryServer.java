package edu.purdue.cs252.lab6.serverapp;

//Need to implement User class

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import edu.purdue.cs252.lab6.User;

public class DirectoryServer {
	static final String SERVERIP = "127.0.0.1";
	static final int SERVERPORT = 25201, MAXC = 10;
	public static void main(String[] args) {
		int i = 0;

		try {
			// Create a socket for handling incoming requests
			ServerSocket listener = new ServerSocket(SERVERPORT);
			Socket server;

			
			while((i++ < MAXC) || (MAXC == 0)){
				System.out.println("TCP S: Waiting for new connection...");
		        	server = listener.accept();
				System.out.println("TCP S: New connection received.");
		        	doComms conn_c= new doComms(server);
		        	Thread t = new Thread(conn_c);
		        	t.start();
		      }
		} catch (IOException e) {
			System.out.println("TCP S: Error" + e);
			e.printStackTrace();
		}
	}
}

class doComms implements Runnable
{
	private Socket server;
	
	doComms(Socket server)
	{
		this.server = server;
	}
	
	public void run()
	{
		try
		{
				// Read data from the client
				InputStream stream = server.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(stream); 

				DirectoryCommand command = ois.readInt();
				
				
				// InputStream is an abstract class. We needed to use a subclass
				BufferedReader data = new BufferedReader(new InputStreamReader(stream));
				
				
				// Read a line at a time
				String line;
				while ((line = data.readLine()) != null) {
					System.out.println("TCP S: Received: '" + line + "'");
					}
				System.out.println("TCP S: Done.");
				server.close();
				
		} catch (IOException e) {
			System.out.println("TCP S: Error" + e);
			e.printStackTrace();
		}
	}
}