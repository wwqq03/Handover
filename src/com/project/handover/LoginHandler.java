package com.project.handover;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class LoginHandler implements Runnable{
	
	private String name;
	private String password;
	private LoginActivity loginActivity;
	
	public LoginHandler(String name, String password, LoginActivity loginActivity){
		this.name = name;
		this.password = password;
		this.loginActivity = loginActivity;
	}
	
	public void run(){
		if(name == null
			|| name.isEmpty()
			|| password == null
			|| password.isEmpty()){
			Log.d("login", "user:" + name + ", password:" + password);
			return;
		}
		
		try{
			Socket socket = new Socket("fred.item.ntnu.no", 5000);
			//for debugging in local server
			//Socket socket = new Socket("192.168.1.102", 5000);
			
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
			BufferedReader reader = new BufferedReader(streamReader);
			Log.d("run", "before new request");
			Request loginRequest = new Request(Request.loginRequest);
			loginRequest.setName(name);
			loginRequest.setPassword(password);
			
			if(!loginRequest.isLegal()){
				Log.d("run", "illegal loginRequest:" + loginRequest.toString());
				return;
			}
			Log.d("run", "before set request to xml");
			String msgToSend = loginRequest.toXML();
			if(msgToSend == null || msgToSend.isEmpty()){
				Log.d("run", "msgToSend is null or empty");
				return;
			}
			writer.println(msgToSend);
			writer.flush();
			
			String rawResponse = reader.readLine();
			reader.close();
			if(rawResponse == null){
				Log.d("run", "response is null or empty");
				return;
			}
			
			Response response = new Response(rawResponse);
					
			Log.i("Response", response.toString());
			
			if(response.isPositiveResponse()){
				SharedPreferences prefs = loginActivity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
				SharedPreferences.Editor editor = prefs.edit();
				
				editor.putString("user", name);
				if(response.getRole() != null && response.getRole().equals("C")){
					editor.putString("role", "C");
				}
				else{
					editor.putString("role", "N");
				}
				editor.commit();
				
				Intent in = new Intent(loginActivity.getApplicationContext(), MainActivity.class);
				loginActivity.startActivity(in);
			}
			else{
				Runnable r = new Runnable() {
                    public void run() {
                    	Toast.makeText(loginActivity.getApplicationContext(), "Failed login", Toast.LENGTH_LONG ).show();
                            }
                    };             
                    loginActivity.runOnUiThread(r);
				
			}
		} catch(Exception e){
			e.printStackTrace();
			return;
		}
	}
}
