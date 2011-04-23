package edu.purdue.cs252.lab6.userapp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class VoicePlayerServer extends Thread {
	static public String SERVERNAME = "10.0.2.2";
	static public int SERVERPORT = 25202;
	static public DatagramSocket socket;

	private String server;
	private int port;
	
	VoicePlayerServer(String server, int port) throws UnknownHostException, SocketException {
		super();
		this.server = server;
		this.port = port;
		// Retrieve the ServerName 
		//InetAddress serverAddr = InetAddress.getByName(server);
		socket = new DatagramSocket();
	}
	
	public void run() {
		try {
			/*boolean ongoing=true;
			
			InetAddress serverAddr = InetAddress.getByName(SERVERNAME);
			DatagramSocket socket = new DatagramSocket(SERVERPORT);
			
			// Minimum buffer size (can be increased later)
			int minSize=AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT);
			
			// Create instance of AudioTrack
			AudioTrack data= new AudioTrack(AudioManager.STREAM_VOICE_CALL,44100,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT,minSize,AudioTrack.MODE_STATIC);
			byte[] buf=new byte[minSize];
			
			while (!isInterrupted()) {
				//Define the packet
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				
				// Receive the packet
				socket.receive(packet);
				
				// Read packet data and write to a speaker
				buf=packet.getData();
				data.write(buf,0,minSize);
			}*/
			
			

			Log.i("UDP", "VPS: Connecting...");
			// Create new UDP-Socket 
			//DatagramSocket socket = new DatagramSocket(SERVERPORT, serverAddr);
			
			// By magic we know, how much data will be waiting for us 
			byte[] buf = new byte[17];
			// Prepare a UDP-Packet that can
			// contain the data we want to receive 
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			Log.i("UDP", "VPS: Receiving...");

			// Receive the UDP-Packet 
			socket.receive(packet);
			Log.i("UDP", "VPS: Received: '" + new String(packet.getData()) + "'");
			Log.i("UDP", "VPS: Done.");
			
			
		} catch (Exception e) {
			Log.e("UDP", "VPS: Error", e);
		}
	}
}
