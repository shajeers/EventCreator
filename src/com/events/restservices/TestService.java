package com.events.restservices;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test")
public class TestService {
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testService()
	{
		System.out.println("Hai reached Test Service");
		
		return ("Test Service success");
	}

}
