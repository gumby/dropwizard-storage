package com.example.storage.exceptions;

public class ObjectFetchTimeout extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ObjectFetchTimeout(String objectName) {
		super("Timeout waiting for object to be fetched: " + objectName);
	}

}
