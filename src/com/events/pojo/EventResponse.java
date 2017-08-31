package com.events.pojo;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EventResponse {
	
	public String status;
	public String respData;
	public List<String> messages;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRespData() {
		return respData;
	}
	public void setRespData(String respData) {
		this.respData = respData;
	}
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	
	
}
