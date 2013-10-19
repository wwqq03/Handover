package com.project.handover;

import java.io.*;
import java.net.*;
import android.widget.Toast;

public class TagHandler implements Runnable {
	
	private Socket socket;
	private String room;
	private final String nurse = "Rose";
	private Toast toast;
	private MainActivity activity;
	
	public TagHandler(String room,Toast toast, MainActivity actvitity) {
		this.toast = toast;
		this.room = room;
		this.activity = actvitity;
	}
	
	public void run() {
		try{
			socket = new Socket("fred.item.ntnu.no", 5000);
			//for debugging in local server
			//socket = new Socket("192.168.1.102", 5000);
		} catch(Exception e){
			e.printStackTrace();
			return;
		}
		if(room == null)
			return;
		else if(nurse == null) {
			startLoginActivity();
		}
		else {
			final String response = sendHandover();
			if(response == null)
				return;
			System.out.println(response);
			
			Runnable r = new Runnable() {
				public void run() {

					toast.setText(response);
					toast.show();
					}
				};
				
			activity.runOnUiThread(r);
		}
	}
	
	public String sendHandover() {
		if(room == null || nurse == null)
			return null;
		String response = null;
		try{
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
			BufferedReader reader = new BufferedReader(streamReader);
			
			Request handoverRequest = new Request(Request.handoverRequest);
			handoverRequest.setRoom(room);
			handoverRequest.setNurse(nurse);
			if(!handoverRequest.isLegal())
				return null;
			
			String msgToSend = handoverRequest.toXML();
			if(msgToSend == null || msgToSend.isEmpty())
				return null;
			writer.println(msgToSend);
			writer.flush();
			
			response = reader.readLine();
			reader.close();					
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			try{
				socket.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}
	
	public void startLoginActivity() {
		
	}

}
