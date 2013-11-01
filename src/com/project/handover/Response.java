package com.project.handover;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.util.Log;

public class Response {
	
	public static final String loginResponse  = "LOGIN";
	public static final String handoverResponse = "HANDOVER";
	
	private String responseType;
	private String status;
	private String message;
	private String role;
	
	public Response(String rawResponse){
		Log.d("response", rawResponse);
		if(rawResponse == null)
			return;
		try{
			Document document = DocumentHelper.parseText(rawResponse);
			Element response = document.getRootElement();
			if(response == null)
				return;
			status = response.attributeValue("status");
			if(status == null)
				return;
			
			Element command = response.element("command");
			if(command == null)
				return;
			responseType = command.getText();
			
			if(status.equals("200")){
				if(responseType.equals(loginResponse)){
					role = response.elementText("role");
				}
			}
			
			else{
				message = response.elementText("message");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getResponseType() {
		return responseType;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getRole() {
		return role;
	}

	public String getStatus() {
		return status;
	}
	
	public boolean isLoginResponse(){
		return (responseType.equals(loginResponse));
	}
	
	public boolean isHandoverReponse(){
		return (responseType.equals(handoverResponse));
	}
	
	public boolean isPositiveResponse(){
		return (status.equals("200"));
	}

	@Override
	public String toString() {
		String alternate;
		if(isLoginResponse()){
			alternate = ", role=" + role;
		}
		else{
			alternate = ", message=" + message;
		}
		return "Response [responseType=" + responseType + ", status=" + status
				+ alternate + "]";
	}

}
