package com.events.restservices;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.events.kafkaservices.ConsumeMessage;
import com.events.kafkaservices.CreateMessage;
import com.events.pojo.EventRequest;
import com.events.pojo.EventResponse;

@Path("/events")
public class EventService {
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/createEvent")
	public EventResponse createEvent(EventRequest req)
	{

		CreateMessage cmessage = new CreateMessage();
		cmessage.createMessage(req);
		
		EventResponse resp = new EventResponse();
		
		resp.setRespData("Added message to Que");
		resp.setStatus("Success");

		return resp;
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/consumeEvent")
	public EventResponse consumeEvent(EventRequest req)
	{
		ConsumeMessage cmessage = new ConsumeMessage();
		EventResponse resp = cmessage.consumeMessage(req);
		
		return resp;
	}	

}
