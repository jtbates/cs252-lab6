package edu.purdue.cs252.lab6.userapp;

import java.io.IOException;
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
	private static final String TAG = "VPS";
	static public DatagramSocket socket;

	private int sampleRate = 8000;
	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	private byte[] buffer;
	
	VoicePlayerServer(String server, int port) throws UnknownHostException, SocketException {
		super();
		socket = new DatagramSocket();
	}
	
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		AudioTrack speaker = null;
		int ix = 0;
		
		// Minimum buffer size (can be increased later)
		int N =AudioTrack.getMinBufferSize(sampleRate,channelConfig,audioFormat);
		buffer = new byte[N];
		
		// Create instance of AudioTrack
		speaker = new AudioTrack(AudioManager.STREAM_VOICE_CALL,sampleRate,channelConfig,audioFormat,N,AudioTrack.MODE_STREAM);

		speaker.play();
		
		while(!isInterrupted()) {
			try {
				//Define the packet
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

				// Receive the packet
				socket.receive(packet);

				// Read packet data and write to a speaker
				buffer=packet.getData();
				speaker.write(buffer,0,buffer.length);
				Log.i(TAG,"speaker write");
				/*
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
				*/
				
			} catch (IOException e) {
				Log.e(TAG, "Error: "+ e);
			}
		}
	}
}
