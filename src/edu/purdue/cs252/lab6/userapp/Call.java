package edu.purdue.cs252.lab6.userapp;

//Class used for setting the state of the call of the progrm
public class Call 
{
	public enum State { IDLE, INCOMING, OUTGOING, ONGOING };
	static State callState = State.IDLE;
	static String username2;
	static String ip;
	static int port;
	
	//Sets the state
	synchronized static public void setState(State s) {
			callState = s;
	}
	
	//Gets the states
	synchronized static public State getState() {
			return callState;
	}
	
	//Set the username of the person they are talking to
	synchronized static public void setUsername2(String un2) {
		username2 = un2;
	}
	
	//returns the undername of the person they are communication with
	synchronized static public String getUsername2() {
		return username2;
	}
	
	//Sets the port of the communication
	synchronized static public void setPort(int p) {
		port = p;
	}
	
	//Gets the port of the communication
	synchronized static public int getPort() {
		return port;
	}
	
	
}
