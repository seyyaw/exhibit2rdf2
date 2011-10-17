package it.okkam.exhibit.cq;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class GraphManagerTest {

	GraphManager remoteService = null;
	@Before
	public void setUp() throws Exception {
		
		remoteService = new GraphManager() ;
	}
	

	@Test
	public void testCallServices() throws IOException {
		remoteService.saveJSON(null);
	}

}
