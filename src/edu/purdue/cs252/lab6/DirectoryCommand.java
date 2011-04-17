package edu.purdue.cs252.lab6;

public enum DirectoryCommand {
	// commands that the client sends to the server
	C_LOGIN(0x10), // logs the user in; argument is the client's User class
	C_LOGOUT(0x11), // no argument
	C_GETDIRECTORY(0x12), // gets a listing of all logged in users; no argument
	C_PLACECALL(0x13), // argument is the username of the call recipient (String)
	C_ACCEPTCALL(0x14), // argument is the username of the caller (String)
	
	// commands the server sends to a client 
	S_INCOMINGCALL(0x20), // another user is make an incoming call; argument is the caller's username (String)
	S_CALLACCEPTED(0x21), // the outgoing call was accepted by the recipient; no argument
	S_REDIRECT_INIT(0x22), // specifies the port on the server the user will user for the call; argument is int
	S_REDIRECT_READY(0x23), // tells the client that the other user is ready to receive communications 
	
	// commands the server broadcasts to all clients
	S_BC_USERLOGGEDIN(0x24), // a user logged in; argument is the user's User class
	S_BC_USERLOGGEDOUT(0x25); // a user logged out; argument is the user's username (String)

	private int code;
	
	private DirectoryCommand(int dc) {
		code = dc;
	}
	
	public int getCode() {
		return code;
	}
	
}
