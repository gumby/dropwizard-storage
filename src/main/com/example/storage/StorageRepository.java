package com.example.storage;

import java.nio.ByteBuffer;

import com.example.storage.exceptions.ObjectNotFound;
import com.example.storage.representations.ObjectMetadata;

public interface StorageRepository {

	public ObjectMetadata readMetadata(String name) throws ObjectNotFound;

	public ByteBuffer readChunk(String objectName, int chunkIndex);

}
