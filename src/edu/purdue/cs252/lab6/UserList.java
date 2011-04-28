package edu.purdue.cs252.lab6;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

//Stores the userlist which can be sent from server to client
public class UserList implements Serializable {
	private static final long serialVersionUID = 1L;
	int size;
	User[] uList;
	
	//Constructor which itializes it with the new collection
	public UserList(Collection<User> c) {
		size = c.size();
		uList = new User[size];
		c.toArray(uList);
	}
	
	//returns size
	public int size() {
		return size;
	}
	
	//Gets a certain element
	public User get(int i) {
		return uList[i];
	}
	public User[] getList() {
		return uList;
	}
	
	//Writes the Object to the object output stream
	public void writeObject(ObjectOutputStream oos) throws IOException {
			oos.writeInt(size);
			for(int i=0;i<size;i++) {
				oos.writeObject(uList[i]);
			}
	}
	
	//Reads the object from the object input stream
	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		size = ois.readInt();
		uList = new User[size]; 
		for(int i=0;i<size;i++) {
			uList[i]=(User)ois.readObject();
		}
	}
}
