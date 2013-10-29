package com.project.handover;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity{
	
	public static final String LOGINACTIVITY_CLOSE_MESSAGE = "LoginActivity_Close";
	
	private Context ctx;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		ctx = getApplicationContext();
		
		setContentView(R.layout.activity_login);
		
		Button btnLogin = (Button) findViewById(R.id.button_login);
		final TextView user = (TextView)findViewById(R.id.text_name);
		final TextView password = (TextView)findViewById(R.id.text_password);
		
		btnLogin.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				login(user.getText().toString(), password.getText().toString());
			}
		});
	}
	
	private void login(String user, String password){
		if(user == null
			|| user.isEmpty()
			|| password == null
			|| password.isEmpty()){
			Log.d("login", "user:" + user + ", password:" + password);
			return;
		}
		
		LoginHandler loginHandler = new LoginHandler(user, password, this);
		Thread loginThread = new Thread(loginHandler);
		loginThread.start();
	}

}
