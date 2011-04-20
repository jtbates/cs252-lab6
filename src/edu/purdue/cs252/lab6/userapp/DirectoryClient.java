package edu.purdue.cs252.lab6.userapp;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.purdue.cs252.lab6.*;
import edu.purdue.cs252.lab6.serverapp.DirectoryServer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class DirectoryClient {
	//private String server;
	//private int port = DirectoryServer.SERVERPORT;
	private User user;
	
	ReadThread readThread;
	WriteThread writeThread;

	private Handler readHandler;
	private Handler writeHandler;
	
	Socket socket;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	/*
	* Summary:      Constructor for directory client
	* Parameters:   String server
	* Return: 		none
	*/   
	public DirectoryClient(String server, User user, Handler readHandler)
			throws UnknownHostException, IOException {
		//this.server = server;
		this.user = user;
		
		InetAddress serverAddr = InetAddress.getByName(server);
		this.socket = new Socket(serverAddr, DirectoryServer.SERVERPORT);
		
		this.readHandler = readHandler;
		
		oos = new ObjectOutputStream(socket.getOutputStream());
		oos.flush();
		ois = new ObjectInputStream(socket.getInputStream());
		
		writeThread = new WriteThread();
		writeThread.start();

		readThread = new ReadThread();
		readThread.start();
		
		// wait for looper to prepare and writeThread's handler to be initialized
		synchronized(writeThread) {
			while(writeHandler == null) {
				try {
					Log.i("DC","waiting for handler init");
					writeThread.wait();
				}
				catch(InterruptedException e) {
					// ignore and try again
				}
			}
		}
		Log.i("DC","Finished constructor");
	}
	
	/*
	 * Summary:		Sets the read handler which the readThread will use to pass messages back to
	 * 				the current activity
	 * Parameters:	Handler handler
	 * Return:		none
	 */
	public synchronized void setReadHandler(Handler handler) {
		this.readHandler = handler;
	}
	
	/*
	* Summary:      Sets the current state of the directory client
	* Parameters:   DirectoryCommand state
	* Return: 		void
	*/   
	/*public void setState(DirectoryCommand state) {
		this.state = state;
	}*/
	
	/*
	* Summary:      Logs the server user into the server
	* Parameters:   Socket clientSocket
	* Return: 		void
	*/  
	/*public void login(Socket clientSocket) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
		Log.d("Login:", user.getUserName());
		
		//Write the DirectoryCommand then the user
		oos.writeObject((DirectoryCommand)DirectoryCommand.C_LOGIN);
		oos.writeObject(user);
    	oos.flush();
    	
    	//Set the state to get the DirectoryCommand.C_Directory_Get
    	state = DirectoryCommand.C_DIRECTORY_GET;
	}*/
	
	/*
	* Summary:      Get the current users logged into the system
	* Parameters:   Socket clientSocket
	* Return: 		void
	*/  
	/*public void getDirectories(Socket clientSocket) throws IOException, ClassNotFoundException {
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
			if (user.equals(user.getUserName())) {
				userList[c] = me.getKey() + "(You)";
			} else {
				userList[c] = me.getKey();
			}
			c = c + 1;
		}
		state = null;
	}*/
		
	private class ReadThread extends Thread {
		public void run() {
			while(!isInterrupted()) {
				try {
					DirectoryCommand command = (DirectoryCommand)ois.readObject();
					Log.i("DC","Command: " + command.toString());
					Message msg = readHandler.obtainMessage();
					switch(command) {
						case S_STATUS_OK:
							DirectoryCommand okCmd = (DirectoryCommand) ois.readObject();
							msg.what = DirectoryCommand.S_STATUS_OK.getCode();
							msg.obj = okCmd;
							readHandler.sendMessage(msg);
							break;
						case S_DIRECTORY_SEND:
							ConcurrentHashMap<String,User> userMap = (ConcurrentHashMap<String,User>)ois.readObject();
							msg.what = DirectoryCommand.S_DIRECTORY_SEND.getCode();
							msg.obj = userMap;
							readHandler.sendMessage(msg);
							Log.i("DC","Directory message sent to activity");
							break;
						case S_BC_USERLOGGEDIN:
							User user = (User)ois.readObject();
							msg.what = DirectoryCommand.S_BC_USERLOGGEDIN.getCode();
							msg.obj = user;
							readHandler.sendMessage(msg);
							break;
						case S_BC_USERLOGGEDOUT:
							Object obj = ois.readObject();
							Log.i("DC",obj.toString() + " logged out");
							String username = (String)obj;
							msg.what = DirectoryCommand.S_BC_USERLOGGEDOUT.getCode();
							msg.obj = username;
							readHandler.sendMessage(msg);
							Log.i("DC",username + " logged out");
							break;
						case S_CALL_INCOMING:
							String caller_username = (String)ois.readObject();
							Log.i("DC","Incoming call from " + caller_username);
							msg.what = DirectoryCommand.S_CALL_INCOMING.getCode();
							msg.obj = caller_username;
							readHandler.sendMessage(msg);
							break;
						default:
							Log.e("DC","Read error: unrecognized command");
					}
				} catch (Exception e) {
					Log.e("DC","Read error: ", e);
				}
			}
		}
	}
	
	public void login() {
		Log.i("TCP","C: Attempting login...");
		writeHandler.post(new Runnable() {
			public void run() {
				try {
					synchronized(socket) {
						Log.i("TCP","C: login write begin...");

						oos.writeObject(DirectoryCommand.C_LOGIN);
						oos.writeObject(user);
						oos.flush();
						Log.i("TCP","C: login write finish");

					}
				}
				catch(IOException e) {
					Log.e("TCP", "C: Write error, login failed");
				}
			}
		});
	}
	
	public void getDirectory() {
		Log.i("DC","C: Getting directory...");
		writeHandler.post(new Runnable() {
			public void run() {
				try {
					synchronized(socket) {
						oos.writeObject(DirectoryCommand.C_DIRECTORY_GET);
						oos.flush();
						Log.i("DC","get directory write finish");

					}
				}
				catch(IOException e) {
					Log.e("DC", "Write error, get directory failed");
				}
			}
		});
	}

	public void call_attempt(final String username2) {
		Log.i("DC","Calling " + username2);
		writeHandler.post(new Runnable() {
			public void run() {
				try {
					synchronized(socket) {
						oos.writeObject(DirectoryCommand.C_CALL_ATTEMPT);
						oos.writeObject(username2);
						oos.flush();
						Log.i("DC","get directory write finish");

					}
				}
				catch(IOException e) {
					Log.e("DC", "Write error, get directory failed");
				}
			}
		});		
	}
	
	public void logout() {
		Log.i("DC","Logging out...");
		writeHandler.post(new Runnable() {
			public void run() {
				try {
					synchronized(socket) {
						readThread.interrupt();
						oos.writeObject(DirectoryCommand.C_LOGOUT);
						oos.flush();
						writeThread.close();
					}
				}
				catch(IOException e) {
					Log.e("DC", "Write error, logout failed");
				}
			}
		});
	}
	
	private class WriteThread extends Thread {
		public void run() {
			Looper.prepare();
			synchronized(this) {
				writeHandler = new Handler();
				notifyAll();
			}
			Looper.loop();
		}
		public void close() {
			Looper.myLooper().quit();
		}
	}
}
