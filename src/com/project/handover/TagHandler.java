package com.project.handover;

import java.io.*;
import java.net.*;
import android.widget.Toast;

public class TagHandler implements Runnable {
	
	private Socket socket;
	private String patient;
	private final String nurse = "Rose";
	private Toast toast;
	
	public TagHandler(String patient, Toast toast) {
		this.toast = toast;
		this.patient = patient;
	}
	
	public void run() {
		try{
			socket = new Socket("192.168.1.102", 5000);		
		} catch(Exception e){
			e.printStackTrace();
			return;
		}
		if(patient == null)
			return;
		else if(nurse == null) {
			startLoginActivity();
		}
		else {
			String response = sendHandover();
			if(response == null)
				return;
			System.out.println(response);
			toast.setText(response);
			toast.show();
		}
	}
	
	public String sendHandover() {
		if(patient == null || nurse == null)
			return null;
		String response = null;
		try{
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
			BufferedReader reader = new BufferedReader(streamReader);
			
			String msgToSend = "Patient:" + patient
								+ ", Nurse:" + nurse;
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
