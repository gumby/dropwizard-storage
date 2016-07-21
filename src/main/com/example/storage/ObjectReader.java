package com.example.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

import com.example.storage.exceptions.ObjectFetchInterrupted;
import com.example.storage.exceptions.ObjectFetchTimeout;
import com.example.storage.exceptions.ObjectNotFound;
import com.example.storage.representations.ObjectMetadata;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ObjectReader {

	private int concurrencyLevel = 4;
	private int batchSize = 11;
	private long fetchWaitTime = 60;

	private final StorageRepository storageRepo;
	private final String objectName;

	public ObjectReader(StorageRepository repo, String objectName) {
		this.storageRepo = repo;
		this.objectName = objectName;
	}

	public ObjectReader withConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
		return this;
	}

	public ObjectReader withBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public void read(OutputStream os) 
			throws ObjectNotFound, ObjectFetchTimeout, ObjectFetchInterrupted, IOException 
	{
		ObjectMetadata metadata = storageRepo.readMetadata(objectName);
		// TODO: verify the whole object is written so we can read it
		// metadata.isValidForRead()

		List<Integer> chunkIndexes = Lists.newArrayList();
		final AtomicLong totalBytesRead = new AtomicLong();
		for (int chunkIndex = 0; chunkIndex < metadata.getChunkCount(); chunkIndex++) {
			chunkIndexes.add(chunkIndex);
			if (chunkIndexes.size() == batchSize || chunkIndex == (metadata.getChunkCount() - 1)) {
				final AtomicReferenceArray<ByteBuffer> chunks = 
						new AtomicReferenceArray<ByteBuffer>(chunkIndexes.size());
				readChunks(chunkIndexes, chunks, totalBytesRead);
				writeOut(chunks, os);
				chunkIndexes.clear();
			}
		}
	}

	private void writeOut(AtomicReferenceArray<ByteBuffer> chunks, OutputStream os) throws IOException {
		for (int chunkIndex = 0; chunkIndex < chunks.length(); chunkIndex++) {
			os.write(chunks.get(chunkIndex).array());
			os.flush();
		}
	}

	private void readChunks(
			List<Integer> chunkIndexes, AtomicReferenceArray<ByteBuffer> chunks, AtomicLong totalBytesRead) 
					throws ObjectFetchTimeout, ObjectFetchInterrupted 
	{
		ExecutorService executor = Executors.newFixedThreadPool(
				concurrencyLevel,
				new ThreadFactoryBuilder().setDaemon(true)
				.setNameFormat("ChunkReader-" + objectName + "-%d").build());
		final int beginningIndex = chunkIndexes.get(0);
		Collections.shuffle(chunkIndexes);
		try {
			for (final int chunkIndex : chunkIndexes) {
				executor.submit(new Runnable() {

					@Override
					public void run() {
						ByteBuffer chunk = storageRepo.readChunk(objectName, chunkIndex);
						totalBytesRead.addAndGet(chunk.remaining());
						chunks.set(chunkIndex - beginningIndex, chunk);
					}

				});
			}
		} finally {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(fetchWaitTime , TimeUnit.SECONDS)) {
					throw new ObjectFetchTimeout(objectName);
				}
			} catch (InterruptedException e) {
				throw new ObjectFetchInterrupted(objectName);
			}
		}
	}

}
