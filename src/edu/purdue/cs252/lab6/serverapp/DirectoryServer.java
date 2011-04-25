package edu.purdue.cs252.lab6.serverapp;

//Need to implement User class

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import edu.purdue.cs252.lab6.*;

public class DirectoryServer {
	static final String SERVERIP = "127.0.0.1";
	static public final int SERVERPORT = 25201, MAXC = 10;
	static Integer lastCallID = 0;
	
	// Map for storing the users logged into the directory server
	private static final ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<String, User>();
	// Map for storing the client objects (which the thread processing client communications)
	private static final ConcurrentHashMap<String, Client> clientMap = new ConcurrentHashMap<String, Client>();
	// Map for all ongoing calls
	private static final ConcurrentHashMap<String,Call> callMap = new ConcurrentHashMap<String,Call>(); 
	
	public class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) {}
	}

	public static void main(String[] args) {
		DirectoryServer dserver = new DirectoryServer();
		int i = 0;
		
		/*
		// store a test user
		String testUsername = "testUser";
		User testUser = new User(testUsername);
		userMap.put(testUsername, testUser);
		try {
			Client testClient = dserver.new Client(testUser,new ObjectOutputStream(dserver.new NullOutputStream()),
					new ObjectInputStream(new Socket().getInputStream()));
			clientMap.put(testUsername, testClient);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
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
				// get outputstream and flush before creating inputstream to prevent deadlock
				ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
				oos.flush();
				ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
					
				DirectoryCommand command = (DirectoryCommand)ois.readObject();
				if(command != DirectoryCommand.C_LOGIN) 
					throw new IOException("Unrecognized command: " + command.toString());
				
				User u = (User)ois.readObject();
			
				login(u,oos,ois);
					
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
		//private Socket client;
		final private ObjectOutputStream oos;
		final private ObjectInputStream ois;
		private Thread thread;
		private Call call;
		
		Client(User user, final ObjectOutputStream oos, final ObjectInputStream ois) throws ClassNotFoundException, StreamCorruptedException, IOException {
			//this.client = client;
			this.username = user.getUserName();
			this.oos = oos;
			this.ois = ois;
			thread = new Thread() {
				public void run() {
					while(!isInterrupted()) {
						try { 
							DirectoryCommand command = (DirectoryCommand) ois.readObject();
						
							switch(command) {
								case C_LOGOUT:
									System.out.println("DS: " + username + " logged off");
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
									break;
								case C_CALL_HANGUP:
									call_hangup();
									break;
								default:
									// error, unrecognized command
									System.out.println("Unrecognized command " + command.toString());
									//throw new IOException("Unrecognized command: " + command.toString());
							}
						}
						catch(EOFException e) {
							System.out.println("DS: Client disconnected");
							logout();
							break;
							//TODO: need code to handle unexpected disconnect
						}
						catch(IOException e) {
							System.out.println("DS: Error " + e);
							e.printStackTrace();
						}
						catch(ClassNotFoundException e) {
							System.out.println("DS: Error " + e);
							e.printStackTrace();					
						}
					}
				}
			};
			thread.start();
		}
		
		private void call_attempt(String username2) throws IOException {
			System.out.println(username + " is attempting to call " + username2);
			Client client2 = clientMap.get(username2);
			if(client2 == null) {
				synchronized(oos) {
					oos.writeObject(DirectoryCommand.S_ERROR_USERDOESNOTEXIST);
					oos.flush();
				}
			}
			else {
				Client clientThread2 = clientMap.get(username2);
				clientThread2.call_incoming(username);
			}
		}
		
		private void call_answer(String username2) throws IOException {
			System.out.println(username + " answers " + username2);
			Client client2 = clientMap.get(username2);
			if(client2 == null) {
				synchronized(oos) {
					oos.writeObject(DirectoryCommand.S_ERROR_USERDOESNOTEXIST);
					oos.flush();
				}
			}
			else {
				call = new Call(username,username2);
				callMap.put(username, call);

				client2.call_accepted(username);
				
				synchronized(oos) {
					oos.writeObject(DirectoryCommand.S_REDIRECT_INIT);
					int port = call.getRedirectPort(username);
					oos.writeInt(port);
					oos.flush();
					System.out.println("S_REDIRECT_INIT " + port + " to " + username);
				}
			}
		}
		
		private void directory_send() throws IOException {
			synchronized(oos) {
				oos.writeObject(DirectoryCommand.S_DIRECTORY_SEND);
				//oos.writeObject(userMap);
				oos.writeObject(new UserList(userMap.values()));
				oos.flush();
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
		
		private void logout() {
			// Broadcast to all clients that a user has logged out
			for(String bc_username : userMap.keySet()) {
				if(!bc_username.equals(username)) {
					try {
						Client bc_client = clientMap.get(bc_username);
						bc_client.user_loggedout(username);
					}
					catch(IOException e) {
						// TODO: handle IOException
					}
				}
			}
		
			close();
		}

		private void close() {
			userMap.remove(username);
			clientMap.remove(username);
			thread.interrupt();

			try {
				synchronized(oos) {
					ois.close();
					oos.close();
				}	
			}
			catch(IOException e) {
				// TODO handle error closing stream
			}
		}
		// methods below do not correspond to commands from the client
		// they are called by another Client object or Call object

		public void call_incoming(String username2) throws IOException {
			synchronized(oos) {
				oos.writeObject(DirectoryCommand.S_CALL_INCOMING);
				oos.writeObject(username2);
				oos.flush();
			}
		}

		public void call_accepted(String username2) throws IOException {
			System.out.println(username + "'s call is accepted by " + username2);
			call = callMap.get(username2);
			if(call == null) {
				synchronized(oos) {
					oos.writeObject(DirectoryCommand.S_ERROR_CALLFAILED);
					oos.flush();
				}
			}
			else {
				callMap.put(username, call);
				synchronized(oos) {
					oos.writeObject(DirectoryCommand.S_CALL_ACCEPTED);
					oos.flush();
				}

				synchronized(oos) {
					oos.writeObject(DirectoryCommand.S_REDIRECT_INIT);
					int port = call.getRedirectPort(username);
					oos.writeInt(port);
					oos.flush();
					System.out.println("S_REDIRECT_INIT " + port + " to " + username);
				}
			}
		}
		
		public void call_disconnect(String username2) throws IOException {
			synchronized(oos) {
				oos.writeObject(DirectoryCommand.S_CALL_DISCONNECT);
				oos.writeObject(username2);
				oos.flush();
			}
		}
		
		public void call_beginSending(String username2) throws IOException {
			System.out.println("S_REDIRECT_READY to " + username2);
			synchronized(oos) {
				oos.writeObject(DirectoryCommand.S_REDIRECT_READY);
				oos.writeObject(username2);
				oos.flush();
			}
		}

		public void success(DirectoryCommand command) throws IOException {
			System.out.println("success write " + command.toString() + " begin");
			synchronized(oos) {
				oos.writeObject(DirectoryCommand.S_STATUS_OK);
				oos.writeObject(command);
				oos.flush();
			}
			System.out.println("success write complete");
		}
	
		public void user_loggedin(User user_loggedin) throws IOException {
			synchronized(oos) {
				oos.writeObject(DirectoryCommand.S_BC_USERLOGGEDIN);
				oos.writeObject(user_loggedin);
				oos.flush();
			}
		}
	
		public void user_loggedout(String username_loggedout) throws IOException {
			synchronized(oos) {
				oos.writeObject(DirectoryCommand.S_BC_USERLOGGEDOUT);
				oos.writeObject(username_loggedout);
				oos.flush();
			}
		}
		/*private void writeObjects(ArrayList<Object> objList) throws IOException {
			synchronized(oos) {
				for(int i=0;i<objList.size();i++) {
					oos.writeObject(objList.get(i));
				}
			}
		}*/
		

		
	}

	private class Call {
		HashMap<String,Integer> idMap;
		ArrayList<String> usernameList;
		ArrayList<DatagramSocket> rSocketList; // redirect DatagramSocket
		//ArrayList<InetAddress> nAddressList; // NAT address
		//ArrayList<Integer> nPortList; // NAT port
		ArrayList<SocketAddress> nSocketAddressList;
		ArrayList<Thread> threadList;
		ArrayList<Boolean> readyList;
		
		Call(String username1, String username2) throws IOException {
			idMap = new HashMap<String,Integer>();
			usernameList = new ArrayList<String>();
			rSocketList = new ArrayList<DatagramSocket>();
			//nAddressList = new ArrayList<InetAddress>();
			//nPortList = new ArrayList<Integer>();
			nSocketAddressList = new ArrayList<SocketAddress>();
			threadList = new ArrayList<Thread>();
			readyList = new ArrayList<Boolean>();
			connect(username1);
			connect(username2);
		}
		
		int getRedirectPort(String username) {
			DatagramSocket redirect = rSocketList.get(idMap.get(username));
			if(redirect == null) return -1;
			else return redirect.getLocalPort();
		}
		
		ArrayList<String> getUsernameList() {
			return usernameList;
		}
		
		synchronized void connect(final String username) throws IOException {
			final int id = usernameList.size();
			idMap.put(username, id);
			usernameList.add(username);
			readyList.add(false);
			final DatagramSocket redirectSocket = new DatagramSocket();
			rSocketList.add(redirectSocket);
			nSocketAddressList.add(new InetSocketAddress(0)); //placeholder
			
			Thread redirectThread = new Thread() {
				@Override
				public void run() {
					int minSize = 160; // must be the same as in VCC and VPS 
					byte[] buf=new byte[minSize];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					
					while(!readyList.get(id)) {
						try {
							redirectSocket.receive(packet);
							nSocketAddressList.set(id,packet.getSocketAddress());
							readyList.set(id,true);
							
							System.out.println(username + "'s first UDP packet");
						} catch (IOException e) {
							// try again
						}
					}
					while (!isInterrupted()) {
						try {
							//packet = new DatagramPacket(buf, buf.length);
							redirectSocket.receive(packet);
							System.out.println("Received UDP packet from " + username + " at (" + packet.getAddress() + "," + packet.getPort() + ")");
							for(int i=0; i<usernameList.size();i++) {
								if(i == id && readyList.get(i) == true) {
									packet.setSocketAddress(nSocketAddressList.get(i));
									redirectSocket.send(packet);
									System.out.println("Sending UDP packet to (" + packet.getAddress() + "," + packet.getPort() + ")");
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
			DatagramSocket socket = rSocketList.get(id);
			//Thread thread = threadList.get(id);
			
			
			for(int i=0;i<usernameList.size();i++) {
				if(i!=id) {
					String username = usernameList.get(i);
					Client client = clientMap.get(username);
					client.call_disconnect(user_disconnecting);
					if(i>id) {
						idMap.put(username, i-1);
					}
				}
			}
			
			usernameList.remove(id);
			socket.close();
			rSocketList.remove(id);
			nSocketAddressList.remove(id);
			//nPortList.remove(id);
			readyList.remove(id);
			threadList.get(id).interrupt();
			threadList.remove(id);
			
			Client client_disconnecting = clientMap.get(user_disconnecting);
			client_disconnecting.success(DirectoryCommand.C_CALL_HANGUP);			
		}
		
		private class Caller {
			
		}
		
	}
	
	private void login(User user, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
		String username = user.getUserName();
		//if the user name is already taken
		if (userMap.containsKey(username)) {
			//Send message back to client that user name is invalid
			synchronized(oos) {
				System.out.println("S: " + username + " already logged in");
				oos.writeObject(DirectoryCommand.S_ERROR_USERALREADYEXISTS);
			}
		}
		else {
			userMap.put(username,user);
			Client client = new Client(user,oos,ois);
			clientMap.put(username,client);

			// Broadcast to all clients that a new user has logged in
			for(String bc_username : userMap.keySet()) {
				if(!bc_username.equals(username)) {
					Client bc_client = clientMap.get(bc_username);
					bc_client.user_loggedin(user);
				}
			}
			
			client.success(DirectoryCommand.C_LOGIN);
			System.out.println("S: " + username + " logged in");
		}
	}

}





