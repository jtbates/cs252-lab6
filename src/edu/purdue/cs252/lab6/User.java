package edu.purdue.cs252.lab6;

public class User {
	private String userIP; //String Containing the userIP
	private String userName; //String containing the username 
	private boolean isConnected; //Boolean value which determines if the user is currently connected to another phone 
	
	/*
	 * Summary:      Constructor for user Class, Sets UserIP to False
	 * Parameters:   String UserName
	 * Return:       void
	 */
	public User(String userName) {
		this.userIP = null;
		this.userName = userName;
		isConnected = false;
	}
	
	
	/*
	 * Summary:      Constructor for user Class
	 * Parameters:   String UserIP, String UserName
	 * Return:       void
	 */
	public User(String userIP, String userName) {
		this.userIP = userIP;
		this.userName = userName;
		isConnected = false;
	}
	
	/*
	* Summary:     Returns the user name
	* Parameters:  none
	* Return:      String
	*/
	public String getUserName() {
		return userName;
	}
	
	
	/*
	* Summary:     Returns the user IP
	* Parameters:  none
	* Return:      String
	*/
	public String getUserIp() {
		return userIP;
	}
	
	/*
	* Summary:     Toggles the value of isConnected to from true to false, and false to true
	* Parameters:  none
	* Return:      void
	*/
	public void changeConnection() {
		isConnected = !isConnected;
	}
}
