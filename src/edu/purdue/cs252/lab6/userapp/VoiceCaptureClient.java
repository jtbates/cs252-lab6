package edu.purdue.cs252.lab6.userapp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class VoiceCaptureClient extends Thread {
	static public String SERVERNAME = "10.0.2.2";
	static public int SERVERPORT = 25203;
	private String server;
	private int port;
	
	VoiceCaptureClient(String server, int port) {
		super();
		this.server = server;
		this.port = port;
	}
	
	public void run() {
		try {
			// Minimum buffer size (can be increased later)
			int minSize=AudioRecord.getMinBufferSize(4410,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
			// Construct instance of AudioRecord
			AudioRecord data=new AudioRecord(MediaRecorder.AudioSource.VOICE_CALL,44100,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,minSize);
			
			// Create socket
			DatagramSocket socket = new DatagramSocket();
			InetAddress serverAddr = InetAddress.getByName(SERVERNAME);
			DatagramPacket packet;
			// 
			boolean ongoing=true;
			// Array of bytes length minSize
			byte[] buf = new byte[minSize];
			// While call has not ended
			while (!isInterrupted()) {
				
				//Read data from mic into buf
				data.read(buf,0,minSize);
				//Put buffer in a packet
				packet=new DatagramPacket(buf,buf.length,serverAddr,SERVERPORT);
				
				//Send the packet
				socket.send(packet);
			}
			
			/*
			// Retrieve the ServerName
			InetAddress serverAddr = InetAddress.getByName(SERVERNAME);

			Log.d("UDP", "C: Connecting...");
			// Create new UDP-Socket 
			DatagramSocket socket = new DatagramSocket();
			
			/* Prepare some data to be sent. 
			byte[] buf = ("Hello from VoiceCaptureClient").getBytes();

			/* Create UDP-packet with
			 * data & destination(url+port) 
			DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, SERVERPORT);
			Log.d("UDP", "C: Sending: '" + new String(buf) + "'");

			/* Send out the packet 
			socket.send(packet);
			Log.d("UDP", "C: Sent.");
			Log.d("UDP", "C: Done.");
			*/
		} catch (Exception e) {
			Log.e("UDP", "C: Error", e);
		}
	}
}