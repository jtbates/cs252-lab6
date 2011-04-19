package edu.purdue.cs252.lab6.serverapp;

//Need to implement User class

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import edu.purdue.cs252.lab6.DirectoryCommand;
import edu.purdue.cs252.lab6.User;
import edu.purdue.cs252.lab6.userapp.Call;

public class DirectoryServer {
	static final String SERVERIP = "127.0.0.1";
	static final int SERVERPORT = 25201, MAXC = 10;
	static Integer lastCallID = 0;
	
	// Map for storing the users logged into the directory server
	private final ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<String, User>();
	// Map for storing the client objects (which the thread processing client communications)
	private final ConcurrentHashMap<String, Client> clientMap = new ConcurrentHashMap<String, Client>();
	// Map for all ongoing calls
	private final ConcurrentHashMap<String,Call> callMap = new ConcurrentHashMap<String,Call>(); 
	
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
		private Call call;
		
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
						
							switch(command) {
								case C_LOGOUT:
									logout();
									break;
								case C_DIRECTORY_GET:
									directory_send();
									break;
								case C_CALL_ATTEMPT:
									call_attempt((String) ois.readObject());
									break;
								case C_CALL_ANSWER:
									call_answer((String) ois.readObject());
									break;
								case C_CALL_READY:
									call_ready();
								case C_CALL_HANGUP:
									call_hangup();
								default:
									// error, unrecognized command
									throw new IOException("Unrecognized command: " + command.getCode());
							}
						}
						catch(IOException e) {
							System.out.println("TCP S: Error " + e);
							e.printStackTrace();
						}
						catch(ClassNotFoundException e) {
							System.out.println("TCP S: Error " + e);
							e.printStackTrace();					
						}
					}
				}
			};
			thread.run();
		}
		
		private void call_attempt(String username2) throws IOException {
			Client client2 = clientMap.get(username2);
			if(client2 == null) {
				synchronized(client) {
					oos.writeObject(DirectoryCommand.S_ERROR_USERDOESNOTEXIST);
				}
			}
			else {
				Client clientThread2 = clientMap.get(username2);
				clientThread2.call_incoming(username);
			}
		}
		
		private void call_answer(String username2) throws IOException {
			Client client2 = clientMap.get(username2);
			if(client2 == null) {
				synchronized(client) {
					oos.writeObject(DirectoryCommand.S_ERROR_USERDOESNOTEXIST);
				}
			}
			else {
				Call call = new Call(username,username2);
				callMap.put(username, call);

				client2.call_accepted(username);
				
				synchronized(client) {
					oos.writeObject(DirectoryCommand.S_REDIRECT_INIT);
					oos.writeInt(call.getPort(username));
				}
			}
		}
		
		private void directory_send() throws IOException {
			synchronized(client) {
				oos.writeObject(DirectoryCommand.S_DIRECTORY_SEND);
				oos.writeObject(userMap);
			}
		}
		
		private void call_hangup() throws IOException {
			if(call == null) {
				throw new IllegalStateException("User " + username + " attempted to disconnect from a call not found in callMap");
			}
			else {
				call.disconnect(username);
			}
		}
		
		private void call_ready() throws IOException {
			if(call == null) {
				throw new IllegalStateException("User " + username + " sends C_CALL_READY but no corresponding call in callMap");
			}
			else {
				ArrayList<String> usernameList = call.getUsernameList();
				for(int i=0;i<usernameList.size();i++) {
					String username2 = usernameList.get(i);
					if(!username2.equals(username)) {
						clientMap.get(username2).call_beginSending(username);
					}
				}
			}
		}
		
		
		// methods below do not correspond to commands from the client
		// they are called by another Client object or Call object

		public void call_incoming(String username2) throws IOException {
			synchronized(client) {
				oos.writeObject(DirectoryCommand.S_CALL_INCOMING);
				oos.writeObject(username2);
			}
		}

		public void call_accepted(String username2) throws IOException {
			Call call = callMap.get(username2);
			if(call == null) {
				synchronized(client) {
					oos.writeObject(DirectoryCommand.S_ERROR_CALLFAILED);
				}
			}
			else {
				callMap.put(username, call);
				synchronized(client) {
					oos.writeObject(DirectoryCommand.S_CALL_ACCEPTED);
				}

				synchronized(client) {
					oos.writeObject(DirectoryCommand.S_REDIRECT_INIT);
					oos.writeInt(call.getPort(username));
				}
			}
		}
		
		public void call_disconnect(String username2) throws IOException {
			synchronized(client) {
				oos.writeObject(DirectoryCommand.S_CALL_DISCONNECT);
				oos.writeObject(username2);
			}
		}
		
		public void call_beginSending(String username2) throws IOException {
			synchronized(client) {
				oos.writeObject(DirectoryCommand.S_REDIRECT_READY);
				oos.writeObject(username2);
			}
		}

		public void success(DirectoryCommand command) throws IOException {
			synchronized(client) {
				oos.writeObject(DirectoryCommand.S_STATUS_OK);
				oos.writeObject(command);
			}
		}
	
		
		
		/*private void writeObjects(ArrayList<Object> objList) throws IOException {
			synchronized(client) {
				for(int i=0;i<objList.size();i++) {
					oos.writeObject(objList.get(i));
				}
			}
		}*/
		
		private void logout() throws IOException {
			clientMap.remove(username);
			thread.stop();
			
			synchronized(client) {
				ois.close();
				oos.close();
				client.close();
			}
		}
		
	}

	private class Call {
		HashMap<String,Integer> idMap;
		ArrayList<String> usernameList;
		ArrayList<MulticastSocket> socketList;
		ArrayList<Thread> threadList;
		//ArrayList<InetAddress> groupList;
		
		Call(String username1, String username2) throws IOException {
			connect(username1);
			connect(username2);
		}
		
		int getPort(String username) {
			MulticastSocket redirect = socketList.get(idMap.get(username));
			if(redirect == null) return -1;
			else return redirect.getLocalPort();
		}
		
		ArrayList<String> getUsernameList() {
			return usernameList;
		}
		
		synchronized void connect(String username) throws IOException {
			final int id = usernameList.size();
			usernameList.add(username);
			final MulticastSocket redirectFromSocket = new MulticastSocket();
			socketList.add(redirectFromSocket);
			//InetAddress group = InetAddress.getByName("230.0.0." + (id+1));
			//groupList.add(group);
			// forget multicast for now, maybe implement later
			
			Thread redirectThread = new Thread() {
				public void run() {
					int minSize = 100; // need to decide this
					byte[] buf=new byte[minSize];
					// might be fatally slow; we'll see
					while (true) {
						try {
							DatagramPacket packet = new DatagramPacket(buf, buf.length);
							redirectFromSocket.receive(packet);
							for(int i=0; i<usernameList.size();i++) {
								if(i != id) {
									DatagramSocket redirectToSocket = socketList.get(i);
									redirectToSocket.send(packet);
								}
							}
						}
						catch (IOException e) {
							System.out.println("UDP S: Error" + e);
							e.printStackTrace();
						}
					}
				}
			};
			threadList.add(redirectThread);
			redirectThread.start();
		}
		
		synchronized void disconnect(String user_disconnecting) throws IOException {
			int id = idMap.get(user_disconnecting);
			MulticastSocket socket = socketList.get(id);
			Thread thread = threadList.get(id);
			
			
			for(int i=0;i<usernameList.size();i++) {
				if(i!=id) {
					String username = usernameList.get(i);
					Client client = clientMap.get(username);
					client.call_disconnect(user_disconnecting);
				}
			}
			
			socket.close();
			thread.stop();

			Client client_disconnecting = clientMap.get(user_disconnecting);
			client_disconnecting.success(DirectoryCommand.C_CALL_HANGUP);			
		}
	}
	
	private void login(User user, Socket clientSocket) throws IOException, ClassNotFoundException {
		String username = user.getUserName();
		//if the user name is already taken
		if (userMap.containsKey(username)) {
			//Send message back to client that user name is invalid
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			oos.writeObject(DirectoryCommand.S_ERROR_USERALREADYEXISTS);
		}
		else {
			userMap.put(username,user);
			Client client = new Client(user,clientSocket);
			clientMap.put(username,client);
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			oos.writeObject(DirectoryCommand.S_STATUS_OK);
		}
		
		System.out.println("Login Complete");
	}

}





