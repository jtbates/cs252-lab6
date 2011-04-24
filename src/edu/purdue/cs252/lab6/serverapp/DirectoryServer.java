package edu.purdue.cs252.lab6.serverapp;

//Need to implement User class

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.purdue.cs252.lab6.DirectoryCommand;
import edu.purdue.cs252.lab6.User;

public class DirectoryServer {
	//static final String SERVERIP = "127.0.0.1";
	static final int SERVERPORT = 25201, MAXC = 10;
	static ServerSocket serverSocket = null;
	static Socket clientSocket = null;

	static ArrayList<User> userList = new ArrayList<User>();
	static ArrayList<Client> clientList = new ArrayList<Client>();
	
	public static void main(String[] args) {
		DirectoryServer dserver = new DirectoryServer();
		int i = 0;

		System.out.println("TCP S: Started...");	
		try {
			// Create a socket for handling incoming requests
			ServerSocket listener = new ServerSocket(SERVERPORT);
			Socket client;

			while((i++ < MAXC) || (MAXC == 0)){
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
				ObjectInputStream ois = new ObjectInputStream(client.getInputStream());				
					
				DirectoryCommand command = (DirectoryCommand)ois.readObject();
				if(command != DirectoryCommand.C_LOGIN)
						throw new IOException("Unrecognized command: " + command.toString());
				
				User u = (User) ois.readObject();
				System.out.println("TCP S: " + u.getUserName() + " logged in.");
				login(u,client);
			} catch (EOFException e) {
				System.out.println("TCP S: Errpr " + e);
				e.printStackTrace();
			} catch (IOException e) {	
				System.out.println("TCP S: Error" + e);
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println("TCP S: Error " + e);
				e.printStackTrace();
			} 
		}
	}

	private class Client {
		private String username;
		private Socket client;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private Thread thread;

		Client(User user, Socket client) throws ClassNotFoundException, StreamCorruptedException, IOException {
			this.client = client;
			this.username = user.getUserName();
			//Changed order you must created outputStream before input or you the program will block.
			this.oos = new ObjectOutputStream(client.getOutputStream());
			this.ois = new ObjectInputStream(client.getInputStream());
			thread = new Thread() {
				public void run() {
					while(true) {
						try {
							DirectoryCommand command = (DirectoryCommand) ois.readObject();
							switch(command) 
							{
								case C_DIRECTORY_GET:
									directory_send();
									break;
								case C_LOGOUT:
									logout();
									break;
								default:
									// error, unrecognized command
									throw new IOException("Unrecognized command: " + command.getCode());
							}
						} catch(IOException e) {
							System.out.println("TCP S: Error " + e);
							e.printStackTrace();
						} catch(ClassNotFoundException e) {
							System.out.println("TCP S: Error " + e);
							e.printStackTrace();
						}
					}
				}
			};
			thread.run();
		}
	
		private void directory_send() throws IOException {
			synchronized(client) {
				oos.writeObject(DirectoryCommand.S_DIRECTORY_SEND);
				oos.writeObject(userList);
			}
		}
		
		private void logout() throws IOException {
			thread.interrupt();

			try {
				synchronized(oos) {
					ois.close();
					oos.close();
				}
			} catch(IOException e) {}
		}
	}
	
	private void login(User user, Socket clientSocket) throws IOException, ClassNotFoundException {
		if(userList.contains(user))
		{
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			//clientSocket.close();
		} else {		
			userList.add(user);
		
			Collections.sort(userList, new Comparator(){
			
				public int compare(Object o1, Object o2)
				{
					User u1 = (User)o1;
					User u2 = (User)o2;
					return u1.getUserName().compareToIgnoreCase(u2.getUserName());
				}
			});
		
			Client client = new Client(user,clientSocket);
			clientList.add(client);
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			oos.writeObject(DirectoryCommand.S_STATUS_OK);
		}
	}
}	