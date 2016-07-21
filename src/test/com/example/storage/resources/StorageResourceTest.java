package com.example.storage.resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;

import javax.ws.rs.InternalServerErrorException;

import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.example.storage.StorageRepository;
import com.example.storage.exceptions.ObjectNotFound;
import com.example.storage.representations.ObjectMetadata;

import io.dropwizard.testing.junit.ResourceTestRule;

public class StorageResourceTest {

	private static final int KB_CHUNK = 1024;
	private static StorageRepository mockRepo = mock(StorageRepository.class);
	@Rule public ExpectedException thrown = ExpectedException.none();

	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
	.addResource(new StorageResource(mockRepo))
	.build();
	
	@After
	public void resetMockProvider() {
		Mockito.reset(mockRepo);
	}

	@Test
	public void get_metadata() throws Exception {
		ObjectMetadata metadata = new ObjectMetadata(1, 1, 1);
		String name = UUID.randomUUID().toString();
		when(mockRepo.readMetadata(eq(name))).thenReturn(metadata);
		
		assertThat(getMetadata(name), equalTo(metadata));
	}

	@Test
	public void get_contents_chunks_lt_batch_size() throws Exception {
		String objectName = UUID.randomUUID().toString();
		final int chunkCount = 1;
		byte[][] randomBytes = buildRandomBytes(chunkCount, KB_CHUNK);
		mockStorageProvider(chunkCount, randomBytes, objectName);

		byte[] retrieved = read(getObjectStream(objectName));
		byte[] expected = flatten(randomBytes);

		assertThat(retrieved, equalTo(expected));
	}

	@Test
	public void get_contents_chunks_gt_batch_size() throws Exception {
		String objectName = UUID.randomUUID().toString();
		final int chunkCount = 100;
		byte[][] randomBytes = buildRandomBytes(chunkCount, KB_CHUNK);
		mockStorageProvider(chunkCount, randomBytes, objectName);

		byte[] retrieved = read(getObjectStream(objectName));
		byte[] expected = flatten(randomBytes);

		assertThat(retrieved, equalTo(expected));
	}

	@Test
	public void invalid_object_throws() throws Exception{
		when(mockRepo.readMetadata(anyString())).thenThrow(new ObjectNotFound());
		thrown.expect(InternalServerErrorException.class);
		getObjectStream("invalid");
	}

	private InputStream getObjectStream(String objectName) {
		return resources.client().target("/storage/" + objectName).request().get(InputStream.class);
	}
	
	private ObjectMetadata getMetadata(String objectName) {
		return resources.client().target("/storage/" + objectName + "/metadata").request().get(ObjectMetadata.class);
	}

	private void mockStorageProvider(int chunkCount, byte[][] randomBytes, String name) throws ObjectNotFound {
		ObjectMetadata metadata = new ObjectMetadata(1, chunkCount, 15);
		when(mockRepo.readMetadata(eq(name))).thenReturn(metadata);
		when(mockRepo.readChunk(eq(name), anyInt())).thenAnswer(new Answer<ByteBuffer>() {

			@Override
			public ByteBuffer answer(InvocationOnMock invocation) throws Throwable {
				int chunkIndex = invocation.getArgumentAt(1, Integer.class);
				return ByteBuffer.wrap(randomBytes[chunkIndex]);
			}

		});
	}

	private byte[] flatten(byte[][] array2d) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int i = 0; i < array2d.length; i++) {
			for (int j = 0; j < array2d[i].length; j++) {
				buffer.write(array2d[i][j]);
			}
		}
		return buffer.toByteArray();
	}

	private byte[] read(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] data = new byte[16384];
		int read;
		while ((read = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, read);
		}
		return buffer.toByteArray();
	}

	private byte[][] buildRandomBytes(int chunkCount, int chunkSize) {
		byte[][] bytes = new byte[chunkCount][];
		for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
			byte[] b = new byte[chunkSize];
			setRandomBytes(b);
			bytes[chunkIndex] = b;
		}
		return bytes;
	}

	private void setRandomBytes(byte[] bytes) {
		new Random().nextBytes(bytes);
	}

}
