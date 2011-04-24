package edu.purdue.cs252.lab6.userapp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class VoicePlayerServer extends Thread {
	static public String SERVERNAME = "127.0.0.1";
	static public int SERVERPORT = 25203;
	static boolean ongoing = true;
	private int sampleRate = 8000;
	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	
	public VoicePlayerServer()
	{
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		start();
	}

	public void run() {
		AudioTrack speaker = null;
		byte[][] buffers = new byte[256][160];
		int ix = 0;
		try {
			Log.d("UDP", "VPS: Connecting...");
			// Create new UDP-Socket 
			DatagramSocket socket = new DatagramSocket(SERVERPORT);
			
			// Minimum buffer size (can be increased later)
			int N =AudioTrack.getMinBufferSize(sampleRate,channelConfig,audioFormat);
			
			// Create instance of AudioTrack
			speaker = new AudioTrack(AudioManager.STREAM_VOICE_CALL,sampleRate,channelConfig,audioFormat,N,AudioTrack.MODE_STREAM);
			
			speaker.play();
			
			while (ongoing) {
				byte[] buffer = buffers[ix++ % buffers.length];
				
				//Define the packet
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				// Receive the packet
				socket.receive(packet);
				
				// Read packet data and write to a speaker
				buffer=packet.getData();
				speaker.write(buffer,0,buffer.length);
			}
		} catch (Exception e) {
			Log.e("UDP", "VPS: Error", e);
		}
	}
}
