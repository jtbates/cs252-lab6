package edu.purdue.cs252.lab6;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;

public class UserList implements Serializable {
	private static final long serialVersionUID = 1L;
	int size;
	User[] uList;
	
	public UserList(Collection<User> c) {
		size = c.size();
		uList = new User[size];
		c.toArray(uList);
	}
	public int size() {
		return size;
	}
	public User get(int i) {
		return uList[i];
	}
	public User[] getList() {
		return uList;
	}
	
	public void writeObject(ObjectOutputStream oos) throws IOException {
			oos.writeInt(size);
			for(int i=0;i<size;i++) {
				oos.writeObject(uList[i]);
			}
	}
	public void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		size = ois.readInt();
		uList = new User[size]; 
		for(int i=0;i<size;i++) {
			uList[i]=(User)ois.readObject();
		}
	}
}
