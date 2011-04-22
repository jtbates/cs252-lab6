package edu.purdue.cs252.lab6.userapp;

public class Call 
{
	public enum State { IDLE, INCOMING, OUTGOING, ONGOING };
	static State callState = State.IDLE;
	static String username2;
	static String ip;
	static int port;
	
	synchronized static public void setState(State s) {
			callState = s;
	}
	
	synchronized static public State getState() {
			return callState;
	}
	
	synchronized static public void setUsername2(String un2) {
		username2 = un2;
	}
	
	synchronized static public String getUsername2() {
		return username2;
	}
	synchronized static public void setPort(int p) {
		port = p;
	}
	synchronized static public int getPort() {
		return port;
	}
	
	
}
