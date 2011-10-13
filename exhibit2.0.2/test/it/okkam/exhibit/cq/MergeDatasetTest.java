package it.okkam.exhibit.cq;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class MergeDatasetTest {

	MergeDataset remoteService = null;
	@Before
	public void setUp() throws Exception {
		
		remoteService = new MergeDataset() ;
	}
	

	@Test
	public void testCallServices() throws IOException {
		remoteService.callRemoteService(null);
	}

}
