package com.project.handover;

import java.io.*;
import java.net.*;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class TagHandler extends IntentService {
	
	private Socket socket;
	private String room;
	private String nurse;
	
	public TagHandler() {
		super("TagHandlerService");
	}
	
	@Override
	protected void onHandleIntent(Intent tagIntent) {
		try{
			socket = new Socket("fred.item.ntnu.no", 5000);
			//for debugging in local server
			//socket = new Socket("192.168.1.102", 5000);
		} catch(Exception e){
			e.printStackTrace();
			return;
		}
		room = tagIntent.getExtras().getString("room");
		nurse = tagIntent.getExtras().getString("nurse");
		if(room == null)
			return;
		else if(nurse == null) {
			startLoginActivity();
		}
		else {
			final String response = sendHandover();
			if(response == null)
				return;
			Log.i("Response", response);
					
			Handler handler = new Handler(Looper.getMainLooper());  
		    handler.post(new Runnable() {  
		        @Override  
		        public void run() {  
		        	Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG ).show();
		        }  
		    });
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
		Intent in = new Intent(getApplicationContext(), LoginActivity.class);
		startActivity(in);
	}

}
