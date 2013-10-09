package com.project.handover;

import java.io.*;
import java.net.*;
import android.widget.Toast;

public class TagHandler implements Runnable {
	
	private Socket socket;
	private String patient;
	private final String nurse = "Rose";
	private Toast toast;
	private MainActivity activity;
	
	public TagHandler(String patient, Toast toast, MainActivity actvitity) {
		this.toast = toast;
		this.patient = patient;
		this.activity = actvitity;
	}
	
	public void run() {
		try{
			socket = new Socket("fred.item.ntnu.no", 5000);		
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
