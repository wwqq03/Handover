package com.project.handover;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

	private NfcAdapter adapter;
	private PendingIntent pendingIntent;
	private IntentFilter writeTagFilters[];
	private boolean writeMode;
	private Tag mytag;
	private Context ctx;
	private Toast toast;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ctx = getApplicationContext();
		toast = Toast.makeText(ctx, "", Toast.LENGTH_LONG );
		Button btnWrite = (Button) findViewById(R.id.button);
		final TextView message = (TextView)findViewById(R.id.edit_message);

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

		adapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFilters = new IntentFilter[] { tagDetected };

		
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


	@Override
	protected void onNewIntent(Intent intent){
		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
			mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String payload = getPayloadFromIntent(intent);
			if(payload == null || payload.isEmpty())
				return;
			Thread tagHandler = new Thread(new TagHandler(payload, this));
			tagHandler.start();
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		WriteModeOff();
	}

	@Override
	public void onResume(){
		super.onResume();
		WriteModeOn();
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
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
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
}