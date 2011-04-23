package edu.purdue.cs252.lab6.userapp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import edu.purdue.cs252.lab6.User;

public class VoiceCaptureClient implements Runnable {
	static public String SERVERNAME = "127.0.0.1";
	static public int SERVERPORT = 25203;
	static boolean ongoing = true;
	static User usr;
	InetAddress myBcastIP;
	private int sampleRate = 8000;
	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int bufferSize;
	
	public VoiceCaptureClient(User usr)
	{
		this.usr = usr;
	}
	
	public void run() {
		try {
			ongoing = true;
			// Minimum buffer size (can be increased later)
			int minSize=AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
			// Construct instance of AudioRecord
			AudioRecord data = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minSize*2);
			
			Log.d("UDP","VCC: Connecting to " + usr.getUserName() + "...");
			// Create socket
			DatagramSocket socket = new DatagramSocket();
			socket.setBroadcast(true);
			InetAddress serverAddr = InetAddress.getByName(SERVERNAME);
			DatagramPacket packet;
			
			Log.d("UDP","VCC: Connected. Start Recording...");
			data.startRecording();
			// Array of bytes length minSize
			byte[] buf = new byte[minSize];
			// While call has not ended
			while (ongoing) {
				
				//Read data from mic into buf
				data.read(buf,0,minSize);
				//Put buffer in a packet
				packet=new DatagramPacket(buf,buf.length,serverAddr,SERVERPORT);
				
				//Send the packet
				socket.send(packet);
			}
		} catch (Exception e) {
			Log.e("UDP", "C: Error", e);
		}
	}
}