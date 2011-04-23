package edu.purdue.cs252.lab6.userapp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class VoicePlayerServer implements Runnable {
	static public String SERVERNAME = "127.0.0.1";
	static public int SERVERPORT = 25203;
	static boolean ongoing = true;
	private int sampleRate = 8000;
	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

	public void run() {
		try {
			ongoing = true;
			
			Log.d("UDP", "VPS: Connecting...");
			// Create new UDP-Socket 
			DatagramSocket socket = new DatagramSocket(SERVERPORT);
			Log.d("UDP","VPS: Connected. Initializing AudioTrack...");
			
			// Minimum buffer size (can be increased later)
			int minSize=AudioTrack.getMinBufferSize(sampleRate,channelConfig,audioFormat);
			
			// Create instance of AudioTrack
			AudioTrack data= new AudioTrack(AudioManager.STREAM_VOICE_CALL,sampleRate,channelConfig,audioFormat,minSize,AudioTrack.MODE_STREAM);
			byte[] buf=new byte[minSize];
			
			data.play();
			
			while (ongoing) {
				//Define the packet
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				
				// Receive the packet
				Log.d("UDP","VPS: Receiving packet...");
				socket.receive(packet);
				
				// Read packet data and write to a speaker
				Log.d("UDP","Read Packet. Writing to speaker...");
				buf=packet.getData();
				data.write(buf,0,minSize);
			}
		} catch (Exception e) {
			Log.e("UDP", "VPS: Error", e);
		}
	}
}
