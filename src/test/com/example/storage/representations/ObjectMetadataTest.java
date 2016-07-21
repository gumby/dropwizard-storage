package com.example.storage.representations;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;

public class ObjectMetadataTest {
	
	private static final ObjectMapper mapper = Jackson.newObjectMapper();
	
	@Test
	public void serializes_to_json() throws Exception {
		final ObjectMetadata metadata = new ObjectMetadata(3, 3, 3);
		
		final String expectedMetadata = 
				mapper.writeValueAsString(mapper.readValue(fixture("fixtures/metadata.json"), ObjectMetadata.class));
		
		assertThat(mapper.writeValueAsString(metadata), equalTo(expectedMetadata));
	}

}
