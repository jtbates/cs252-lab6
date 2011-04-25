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
	private static final String TAG = "DC";
	private String server;
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
		this.server = server;
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
					Log.i(TAG,"waiting for handler init");
					writeThread.wait();
				}
				catch(InterruptedException e) {
					// ignore and try again
				}
			}
		}
		Log.i(TAG,"Finished constructor");
	}
	
	/*
	 * Summary:		Returns the server string 
	 * Parameters:	none
	 * Return:		String server
	 */
	
	public String getServer() {
		return server;
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
		
	private class ReadThread extends Thread {
		public void run() {
			while(!isInterrupted()) {
				try {
					DirectoryCommand command = (DirectoryCommand)ois.readObject();
					Log.i(TAG,"Command: " + command.toString());
					Message msg = readHandler.obtainMessage();
					String username;
					switch(command) {
						case S_STATUS_OK:
							DirectoryCommand okCmd = (DirectoryCommand) ois.readObject();
							msg.what = DirectoryCommand.S_STATUS_OK.getCode();
							msg.obj = okCmd;
							readHandler.sendMessage(msg);
							break;
						case S_DIRECTORY_SEND:
							//ConcurrentHashMap<String,User> userMap = (ConcurrentHashMap<String,User>)ois.readObject();
							msg.what = DirectoryCommand.S_DIRECTORY_SEND.getCode();
							//msg.obj = userMap;
							msg.obj = ois.readObject();
							readHandler.sendMessage(msg);
							Log.i(TAG,"Directory message sent to activity");
							break;
						case S_BC_USERLOGGEDIN:
							User user = (User)ois.readObject();
							msg.what = DirectoryCommand.S_BC_USERLOGGEDIN.getCode();
							msg.obj = user;
							readHandler.sendMessage(msg);
							break;
						case S_BC_USERLOGGEDOUT:
							Object obj = ois.readObject();
							Log.i(TAG,obj.toString() + " logged out");
							username = (String)obj;
							msg.what = DirectoryCommand.S_BC_USERLOGGEDOUT.getCode();
							msg.obj = username;
							readHandler.sendMessage(msg);
							Log.i(TAG,username + " logged out");
							break;
						case S_CALL_INCOMING:
							username = (String)ois.readObject();
							Log.i(TAG,"Incoming call from " + username);
							msg.what = DirectoryCommand.S_CALL_INCOMING.getCode();
							msg.obj = username;
							readHandler.sendMessage(msg);
							break;
						case S_CALL_DISCONNECT:
							username = (String)ois.readObject();
							Log.i(TAG,username + " hung up");
							msg.what = DirectoryCommand.S_CALL_DISCONNECT.getCode();
							msg.obj = username;
							readHandler.sendMessage(msg);
							break;
						case S_REDIRECT_INIT:
							int port = ois.readInt();
							Log.i(TAG,"S_REDIRECT_INIT");
							msg.what = DirectoryCommand.S_REDIRECT_INIT.getCode();
							msg.arg1 = port;
							readHandler.sendMessage(msg);
							break;
						case S_REDIRECT_READY:
							Log.i(TAG,"S_REDIRECT_READY");
							username = (String)ois.readObject();
							msg.what = DirectoryCommand.S_REDIRECT_READY.getCode();
							readHandler.sendMessage(msg);
							break;
						default:
							Log.e(TAG,"Read error: unrecognized command " + command.toString());
					}
				} catch (Exception e) {
					Log.e(TAG,"Read error: ", e);
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
						Log.i(TAG,"login write begin...");

						oos.writeObject(DirectoryCommand.C_LOGIN);
						oos.writeObject(user);
						oos.flush();
						Log.i(TAG,"login write finish");

					}
				}
				catch(IOException e) {
					Log.e(TAG, "Write error, login failed");
				}
			}
		});
	}
	
	public void getDirectory() {
		Log.i(TAG,"Getting directory...");
		writeHandler.post(new Runnable() {
			public void run() {
				try {
					synchronized(socket) {
						oos.writeObject(DirectoryCommand.C_DIRECTORY_GET);
						oos.flush();
						Log.i(TAG,"get directory write finish");

					}
				}
				catch(IOException e) {
					Log.e(TAG, "Write error, get directory failed");
				}
			}
		});
	}

	public void call_attempt(final String username2) {
		Log.i(TAG,"call attempt to " + username2);
		writeHandler.post(new Runnable() {
			public void run() {
				try {
					synchronized(socket) {
						oos.writeObject(DirectoryCommand.C_CALL_ATTEMPT);
						oos.writeObject(username2);
						oos.flush();
						Log.i(TAG,"call attempt write finish");
					}
				}
				catch(IOException e) {
					Log.e(TAG, "Write error, call attempt failed");
				}
			}
		});		
	}
	
	public void call_answer(final String username2) {
		Log.i(TAG,"answering call from " + username2);
		writeHandler.post(new Runnable() {
			public void run() {
				try {
					synchronized(socket) {
						oos.writeObject(DirectoryCommand.C_CALL_ANSWER);
						oos.writeObject(username2);
						oos.flush();
						Log.i(TAG,"call answer write finish");

					}
				}
				catch(IOException e) {
					Log.e(TAG, "Write error, call answer failed");
				}
			}
		});		
	}
	
	public void call_ready() {
		Log.i(TAG,"call_ready write");
		writeHandler.post(new Runnable() {
			public void run() {
				try {
					synchronized(socket) {
						oos.writeObject(DirectoryCommand.C_CALL_READY);
						oos.flush();
						Log.i(TAG,"call_ready write finish");
					}
				}
				catch(IOException e) {
					Log.e(TAG, "Write error, call_ready write failed");
				}
			}
		});		
	}

	public void call_hangup() {
		Log.i(TAG,"hanging up from call");
		writeHandler.post(new Runnable() {
			public void run() {
				try {
					synchronized(socket) {
						oos.writeObject(DirectoryCommand.C_CALL_HANGUP);
						oos.flush();
						Log.i(TAG,"call hangup write finish");
					}
				}
				catch(IOException e) {
					Log.e(TAG, "Write error, call hangup failed");
				}
			}
		});		
	}
	
	public void logout() {
		Log.i(TAG,"Logging out...");
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
					Log.e(TAG, "Write error, logout failed");
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
