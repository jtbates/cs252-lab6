package edu.purdue.cs252.lab6;

public enum DirectoryCommands {
	// commands that the client sends to the server
	C_LOGIN, // logs the user in; argument is the client's User class
	C_LOGOUT, // no argument
	C_GETDIRECTORY, // gets a listing of all logged in users; no argument
	C_PLACECALL, // argument is the username (String)
	C_ACCEPTCALL, // no argument
	
	// commands the server sends to a client 
	S_INCOMINGCALL, // another user is make an incoming call; argument is the caller's username (String)
	S_CALLACCEPTED, // the outgoing call was accepted by the recipient; no argument
	S_REDIRECT_INIT, // specifies the port on the server the user will user for the call; argument is int
	S_REDIRECT_READY, // tells the client that the other user is ready to receive communications 
	
	// commands the server broadcasts to all clients
	S_BC_USERLOGGEDIN, // a user logged in; argument is the user's User class
	S_BC_USERLOGGEDOUT // a user logged out; argument is the user's username (String)
}
