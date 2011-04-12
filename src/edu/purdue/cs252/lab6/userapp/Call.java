package edu.purdue.cs252.lab6.userapp;

public class Call 
{
	public enum State { IDLE, INCOMING, OUTGOING, ONGOING };
	static State callState = State.IDLE;
	
	static public void setState(State s) {
		synchronized(callState) {
			callState = s;
		}
	}
	
	static public State getState() {
		synchronized(callState) {
			return callState;
		}
	}
}
