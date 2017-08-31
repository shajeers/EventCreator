package com.events.pojo;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EventRequest {
	
	public String reqId;
	public String reqMessage;

	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

	public String getReqMessage() {
		return reqMessage;
	}

	public void setReqMessage(String reqMessage) {
		this.reqMessage = reqMessage;
	}
	
}
