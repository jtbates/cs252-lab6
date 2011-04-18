package edu.purdue.cs252.lab6;

public enum DirectoryCommand {
	// commands that the client sends to the server
	C_LOGIN(0x10), // logs the user in; argument is the client's User class
	C_LOGOUT(0x11), // no argument
	C_DIRECTORY_GET(0x12), // requests a listing of all logged in users; no argument
	C_CALL_ATTEMPT(0x13), // argument is the username of the call recipient (String)
	C_CALL_ANSWER(0x14), // argument is the username of the caller (String)
	C_CALL_HANGUP(0x15), // disconnects from the call
	
	// commands the server sends to a client 
	S_CALL_INCOMING(0x20), // another user is make an incoming call; argument is the caller's username (String)
	S_CALL_ACCEPTED(0x21), // the outgoing call was accepted by the recipient; no argument
	S_REDIRECT_INIT(0x22), // specifies the port on the server the user will user for the call; argument is an integer for the port
	S_REDIRECT_READY(0x23), // tells the client that the other user is ready to receive communications; no argument
	S_DIRECTORY_SEND(0x24), // sends a list of all logged in users; argument is map of users
	S_CALL_DISCONNECT(0x25), // informs the client that the call has been ended; argument is the username of the user that disconnected

	// status messages the server sends to a client
	S_ERROR_USERALREADYEXISTS(0x30), // login failed because the username is already taken; no argument
	S_ERROR_USERDOESNOTEXIST(0x31), // call failed because the user is not logged in; no argument
	S_ERROR_CALLFAILED(0x32), // call failed for unknown reason
	S_STATUS_OK(0x33), // command was successful; argument is the DirectoryCommand
	
	
	// commands the server broadcasts to all clients
	S_BC_USERLOGGEDIN(0x40), // a user logged in; argument is the user's User class
	S_BC_USERLOGGEDOUT(0x41); // a user logged out; argument is the user's username (String)
	


	private int code;
	
	private DirectoryCommand(int dc) {
		code = dc;
	}
	
	public int getCode() {
		return code;
	}
	
}
