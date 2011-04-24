package edu.purdue.cs252.lab6.userapp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import edu.purdue.cs252.lab6.User;

public class VoiceCaptureClient extends Thread {
	static public String SERVERNAME = "192.168.1.117";
	static public int SERVERPORT = 25203;
	static boolean stopped = false;
	static User usr;
	InetAddress myBcastIP;
	private int sampleRate = 8000;
	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	
	
	public VoiceCaptureClient(User usr)
	{
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		this.usr = usr;
		start();
	}
	
	@Override
	public void run() {
		// Initialize
		AudioRecord recorder = null;
		byte[][] buffers = new byte[256][160];
		int ix = 0;
		DatagramPacket packet;
		
		try {
			
			
			// Minimum buffer size (can be increased later)
			int N = AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
			
			// Construct instance of AudioRecord
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,N*10);
			
			Log.d("UDP","VCC: Connecting to " + usr.getUserName() + "...");
			// Create socket
			DatagramSocket socket = new DatagramSocket();
			socket.setBroadcast(true);
			InetAddress serverAddr = InetAddress.getByName(usr.getUserIp());
			
			
			recorder.startRecording();

			// While call has not ended
			while (!stopped) {
				byte[] buffer = buffers[ix++ % buffers.length];
				
				//Read data from mic into buf
				N = recorder.read(buffer,0,buffer.length);
				//Put buffer in a packet
				packet=new DatagramPacket(buffer,buffer.length,serverAddr,SERVERPORT);
				
				//Send the packet
				socket.send(packet);
			}
		} catch (Exception e) {
			Log.e("UDP", "C: Error", e);
		} catch (Throwable t) {
			Log.e("UDP", "Error reading voice audio",t);
		} finally {
			close();
		}
	}
	
	private void close() { 
        stopped = true;
      }

}