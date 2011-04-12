package edu.purdue.cs252.lab6.userapp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;

public class VoiceCaptureClient implements Runnable {
	static public String SERVERNAME = "10.0.2.2";
	static public int SERVERPORT = 25203;
	
	public void run() {
		try {
			// Retrieve the ServerName
			InetAddress serverAddr = InetAddress.getByName(SERVERNAME);

			Log.d("UDP", "C: Connecting...");
			/* Create new UDP-Socket */
			DatagramSocket socket = new DatagramSocket();

			/* Prepare some data to be sent. */
			byte[] buf = ("Hello from VoiceCaptureClient").getBytes();

			/* Create UDP-packet with
			 * data & destination(url+port) */
			DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, SERVERPORT);
			Log.d("UDP", "C: Sending: '" + new String(buf) + "'");

			/* Send out the packet */
			socket.send(packet);
			Log.d("UDP", "C: Sent.");
			Log.d("UDP", "C: Done.");
		} catch (Exception e) {
			Log.e("UDP", "C: Error", e);
		}
	}
}