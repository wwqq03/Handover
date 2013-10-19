package com.project.handover;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class Request {
	
	public static final String loginRequest  = "LOGIN";
	public static final String handoverRequest = "HANDOVER";
	
	private String requestType;
	private String nurse;
	private String room;
	private String name;
	private String password;
	
	public Request (String requestType) {
		this.requestType = requestType;
	}

	public String getRequestType() {
		return requestType;
	}

	public String getNurse() {
		return nurse;
	}

	public void setNurse(String nurse) {
		this.nurse = nurse;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	};
	
	public boolean isLegal(){
		if(requestType == null)
			return false;
		if(requestType.equals(handoverRequest)){
			if(nurse != null && room != null)
				return true;
		}
		else if(requestType.equals(loginRequest)){
			if(name != null && password != null){
				return true;
			}
		}
		return false;
	}
	
	public boolean isLoginRequest(){
		if(requestType == null)
			return false;
		return requestType.equals(loginRequest);
	}
	
	public boolean isHandoverRequest(){
		if(requestType == null)
			return false;
		return requestType.equals(handoverRequest);
	}
	
	@Override
	public String toString() {
		if(requestType == null)
			return null;
		
		String s = ", ";
		if(requestType.equals(handoverRequest)){
			s = s + "nurse=" + nurse + ", room=" + room;
		}
		else if(requestType.equals(loginRequest)){
			s = s + "name=" + name + ", password=" + password;
		}
		else {
			return null;
		}
		return "Request [requestType=" + requestType + s + "]";
	}
	
	public String toXML(){
		try{
			if(!isLegal())
				return null;
			Document document = DocumentHelper.createDocument();
            Element requestElement = document.addElement("request");

            Element commandElement = requestElement.addElement("command");
            commandElement.setText(requestType);
            
            if(isHandoverRequest()){
            	Element roomElement = requestElement.addElement("room");
            	roomElement.setText(room);
            	
            	Element nurseElement = requestElement.addElement("nurse");
            	nurseElement.setText(nurse);
            }
            else if(isLoginRequest()){
            	Element nameElement = requestElement.addElement("name");
            	nameElement.setText(name);
            	
            	Element passwordeElement = requestElement.addElement("password");
            	passwordeElement.setText(password);
            }
            else{
            	return null;
            }
            
            return requestElement.asXML();
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
