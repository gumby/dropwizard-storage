package com.example.storage.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectMetadata {
	
	private int chunkIndex;
	
	private int chunkCount;
	
	private long objectSize;
	
	@SuppressWarnings("unused")
	private ObjectMetadata() {
		// Jackson deserialization
	}
	
	public ObjectMetadata(int chunkIndex, int chunkCount, long objectSize) {
		if (chunkIndex > chunkCount) { 
			throw new IllegalArgumentException(
					"Chunk index (" + chunkIndex + ") cannot be higher than chunk count (" + chunkCount + ")");
		}
		this.chunkIndex = chunkIndex;
		this.chunkCount = chunkCount;
		this.objectSize = objectSize;
	}
	
	@JsonProperty
	public void setChunkIndex(int chunkIndex) {
		this.chunkIndex = chunkIndex;
	}
	
	@JsonProperty
	public int getChunkIndex() {
		return chunkIndex;
	}
	
	@JsonProperty
	public void setChunkCount(int chunkCount) {
		this.chunkCount = chunkCount;
	}
	
	@JsonProperty
	public int getChunkCount() {
		return chunkCount;
	}
	
	@JsonProperty
	public void setObjectSize(long objectSize) {
		this.objectSize = objectSize;
	}
	
	@JsonProperty
	public long getObjectSize() {
		return objectSize;
	}
	
	@Override
	public String toString() {
		return String.format("ObjectMetadata(chunkIndex=%d,chunkCount=%d,objectSize=%d", 
				chunkIndex, chunkCount, objectSize);
	}
	
	@Override
	public int hashCode() {
		return (int) (37 * chunkIndex * chunkCount * objectSize);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (other.getClass() != getClass()) return false;
		if (other == this) return true;
		
		ObjectMetadata otherMetadata = (ObjectMetadata) other;
		return otherMetadata.chunkIndex == this.chunkIndex &&
				otherMetadata.chunkCount == this.chunkCount &&
				otherMetadata.objectSize == this.objectSize;
	}
	
}
