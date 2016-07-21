package com.example.storage.exceptions;

public class ObjectFetchInterrupted extends InterruptedException {

	private static final long serialVersionUID = 1L;
	
	public ObjectFetchInterrupted(String objectName) {
		super("Fetching interrupted for object: " + objectName);
	}

}
