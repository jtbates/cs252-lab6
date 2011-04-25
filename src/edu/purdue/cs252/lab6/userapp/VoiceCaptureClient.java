package edu.purdue.cs252.lab6.userapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class VoiceCaptureClient extends Thread {
	private static final String TAG = "VCC";
	private String server;
	private int port; // redirect port
	//private final int lPort = 25202; // local port
	private int bufferSize;
	private byte[] buffer;

	private int sampleRate = 11025;
	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

	
	VoiceCaptureClient(String server, int port) {
		super();
		this.server = server;
		this.port = port;
		bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
		buffer = new byte[bufferSize];

	}
	
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);		
		 AudioRecord recorder;

		// Create a new recorder
		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize);

		// Start the recording
		recorder.startRecording();
		try {
			InetAddress serverAddr = InetAddress.getByName(server);
			DatagramSocket socket = new DatagramSocket();
 
			// Loop forever recording input
			while (true) {
				// Read from the microphone
				recorder.read(buffer, 0, bufferSize);
				DatagramPacket packet = new DatagramPacket(buffer, bufferSize, serverAddr, port);
				socket.send(packet);

			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
}