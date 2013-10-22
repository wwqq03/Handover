package com.project.handover;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


@SuppressLint({ "ParserError", "ParserError" })
public class MainActivity extends Activity{
	
	public static final String PREFS_NAME = "NursePrefsFile";

	private NfcAdapter adapter;
	private PendingIntent pendingIntent;
	private IntentFilter writeTagFilters[];
	private boolean writeMode;
	private Tag mytag;
	private Context ctx;
	private String user;
	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = getApplicationContext();
		prefs = getSharedPreferences(PREFS_NAME, 0);
		user = prefs.getString("user", "-1");
		if(user.equals("-1")){
			startLoginActivity();
			return;
		}
		
		adapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFilters = new IntentFilter[] { tagDetected };

		Intent in = getIntent();
		mytag = in.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Log.d("onCreate", in.toString());
		onResolveIntent(in);
		
		setContentView(R.layout.activity_main);
		Button btnWrite = (Button) findViewById(R.id.botton_change);
		final TextView message = (TextView)findViewById(R.id.text_room);

		String role = prefs.getString("role", "N");
		if(role.equals("N")){
			btnWrite.setVisibility(View.GONE);
			message.setVisibility(View.GONE);
		}
		else{
			btnWrite.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					try {
						if(mytag==null){
							Toast.makeText(ctx, ctx.getString(R.string.error_detected), Toast.LENGTH_LONG ).show();
						}else{
							write(message.getText().toString(),mytag);
							Toast.makeText(ctx, ctx.getString(R.string.ok_writing), Toast.LENGTH_LONG ).show();
						}
					} catch (IOException e) {
						Toast.makeText(ctx, ctx.getString(R.string.error_writing), Toast.LENGTH_LONG ).show();
						e.printStackTrace();
					} catch (FormatException e) {
						Toast.makeText(ctx, ctx.getString(R.string.error_writing) , Toast.LENGTH_LONG ).show();
						e.printStackTrace();
					}
				}
			});
		}
		
		Button btnLogoff = (Button) findViewById(R.id.button_logoff);
		btnLogoff.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.clear();
				editor.commit();
				startLoginActivity();
			}
		});
	}

	

	private void write(String text, Tag tag) throws IOException, FormatException {

		NdefRecord[] records = { createRecord(text) };
		NdefMessage  message = new NdefMessage(records);
		// Get an instance of Ndef for the tag.
		Ndef ndef = Ndef.get(tag);
		// Enable I/O
		ndef.connect();
		// Write the message
		ndef.writeNdefMessage(message);
		// Close the connection
		ndef.close();
	}



	private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
		String lang       = "en";
		byte[] textBytes  = text.getBytes();
		byte[] langBytes  = lang.getBytes("US-ASCII");
		int    langLength = langBytes.length;
		int    textLength = textBytes.length;
		byte[] payload    = new byte[1 + langLength + textLength];

		// set status byte (see NDEF spec for actual bits)
		payload[0] = (byte) langLength;

		// copy langbytes and textbytes into payload
		System.arraycopy(langBytes, 0, payload, 1,              langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

		return recordNFC;
	}


	//@Override
	protected void onResolveIntent(Intent intent){
		Log.d("onResolveIntent", intent.toString());
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
			String payload = getPayloadFromIntent(intent);
			Log.d("Payload", payload);
			if(payload == null || payload.isEmpty())
				return;
			Intent tagHandler = new Intent(getApplicationContext(), TagHandler.class);
			tagHandler.putExtra("room", payload);
			tagHandler.putExtra("nurse", user);
			startService(tagHandler);
			finish();
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent){
		if(user == null || user.equals("-1")){
			startLoginActivity();
			return;
		}
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
			mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		}
		setIntent(intent);
		onResolveIntent(intent);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		//WriteModeOff();
	}

	@Override
	public void onResume(){
		super.onResume();
		//WriteModeOn();
	}

	private void WriteModeOn(){
		writeMode = true;
		adapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
	}

	private void WriteModeOff(){
		writeMode = false;
		adapter.disableForegroundDispatch(this);
	}

	private String getPayloadFromIntent(Intent intent){
		String result = "";
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
	        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
	        if (rawMsgs != null) {
	            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
	            for (int i = 0; i < rawMsgs.length; i++) {
	                msgs[i] = (NdefMessage) rawMsgs[i];
	            }
	            //There is only a short name in the tag message
	            //So we only need the first message here
	            NdefRecord[] records = msgs[0].getRecords();
	            for(int i = 0; i < records.length; i++){
	            	String type = new String(records[i].getType());
	            	//"T" means the payload is text
	            	if(type.equals("T")){
	            		//In the payload, the first two bytes refer to the language of the following data
	            		//So we don't need the first two bytes
	            		String payload = new String(records[i].getPayload()).substring(3);
	            		result += payload;
	            	}
	            }
	        }
	    }
		return result;
	}
	
	private void startLoginActivity(){
		Intent loginActivity = new Intent(ctx, LoginActivity.class);
		startActivity(loginActivity);
	}
}