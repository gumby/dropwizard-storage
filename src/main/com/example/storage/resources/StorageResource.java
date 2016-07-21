package com.example.storage.resources;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.example.storage.ObjectReader;
import com.example.storage.StorageRepository;
import com.example.storage.exceptions.ObjectNotFound;
import com.example.storage.representations.ObjectMetadata;

@Path("/storage")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StorageResource {

	private final StorageRepository storageProvider;
	
	public StorageResource(StorageRepository storageProvider) {
		this.storageProvider = storageProvider;
	}
	
	@GET
	@Path("/{name}")
	public StreamingOutput getObject(@PathParam("name") String objectName) {
		StreamingOutput output = new StreamingOutput() {

			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				try {
					new ObjectReader(storageProvider, objectName).read(output);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
				output.close();
			}
			
		};
		return output;
	}
	
	@GET
	@Path("/{name}/metadata")
	public ObjectMetadata readMetadata(@PathParam("name") String objectName) {
		try {
			return storageProvider.readMetadata(objectName);
		} catch (ObjectNotFound e) {
			throw new WebApplicationException(e);
		}
	}


}
