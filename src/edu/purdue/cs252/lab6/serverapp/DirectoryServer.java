package edu.purdue.cs252.lab6.serverapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class DirectoryServer {
	static final String SERVERIP = "127.0.0.1";
	static final int SERVERPORT = 25201;
	
	public static void main(String[] args) {
		try {
			// Create a socket for handling incoming requests
			ServerSocket server = new ServerSocket(SERVERPORT);

			do {
				// Wait for an incoming connection
				System.out.println("TCP S: Waiting for new connection...");
				Socket clientSocket = server.accept();
				System.out.println("TCP S: New connection received.");

				// Read data from the client
				InputStream stream = clientSocket.getInputStream();
				// InputStream is an abstract class. We needed to use a subclass
				BufferedReader data = new BufferedReader(new InputStreamReader(stream));

				// Read a line at a time
				String line;
				while ((line = data.readLine()) != null) {
					System.out.println("TCP S: Received: '" + line + "'");
				}
				System.out.println("TCP S: Done.");

			} while (true);

		} catch (IOException e) {
			System.out.println("TCP S: Error" + e);
		}
	}
}
