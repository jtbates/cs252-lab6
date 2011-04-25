package edu.purdue.cs252.lab6.userapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

public class VoicePlayerServer extends Thread {
	private static final String TAG = "VPS";
	private int bufferSize;
	private byte[] buffer;

	private int sampleRate = 11025;
	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	private String server;
	private int port;
	
	VoicePlayerServer(String server, int port) throws UnknownHostException, SocketException {
		super();
		this.server = server;
		this.port = port;
		bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
		buffer = new byte[bufferSize];
	}
	
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		AudioTrack speaker = null;
		/**
		 * Class that will play the sound
		 */

		AudioTrack player;
		player = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
		player.play();

		try {
			InetAddress serverAddr = InetAddress.getByName(server);
			DatagramSocket socket = new DatagramSocket(port, serverAddr);
			DatagramPacket packet = new DatagramPacket(buffer, bufferSize);

			// Loop forever playing the audio
			while (true) {
				
				socket.receive(packet);
				player.write(packet.getData(), 0, bufferSize);
			
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
