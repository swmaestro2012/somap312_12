// Copyright 2011 Google Inc. All Rights Reserved.

package com.swmaestro.NFCWiFiPresenter;

import android.app.IntentService;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class NetworkModule extends IntentService {

	public static final String ACTION_SEND_FILE = "com.swmaestro.NFCWiFiPresenter.SEND_FILE";
	public static final String ACTION_SENT_FILE = "com.swmaestro.NFCWiFiPresenter.SENT_FILE";
	public static final String ACTION_REMOTE_CONTROL = "com.swmaestro.NFCWiFiPresenter.REMOTE_CONTROL";
	public static final String ACTION_BUTTON_PRESSED = "com.swmaestro.NFCWiFiPresenter.BUTTON_PRESSED";
	public static final String ACTION_QUIT = "com.swmaestro.NFCWiFiPresenter.QUIT";
	public static final String EXTRAS_FILE_PATH = "file_url";
	public static final String EXTRAS_FILE_NAME = "file_name";
	public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
	public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
	public static final String EXTRAS_BUTTON = "pressed_button";

	static PrintWriter pw;
	static BufferedReader br;

	static NetworkModule fs;

	public NetworkModule(String name) {
		super(name);
		fs = this;
	}

	public NetworkModule() {
		super("FileTransferService");
	}

	/*
	 * (non-Javadoc)url
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */

	@Override
	protected void onHandleIntent(Intent intent) {

		Log.d("FileTransferReceived", intent.getAction());
		Context context = getApplicationContext();
		if (intent.getAction().equals(ACTION_SEND_FILE)) {
			String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
			try {
				Log.d("try to get socket", "not now");
				ServerSocket socket = new ServerSocket(8988);
				Log.d("try to get socket", "opened server socket");
				Socket sock = socket.accept();
				Log.d("get socket", "success");

				try {
					BufferedReader in = new BufferedReader(
							new InputStreamReader(sock.getInputStream()));
					Log.d("try to receive signal", "???");
					String inputSignal = in.readLine();
					Log.d("Received", inputSignal);
					String delims = "/";
					String[] tokens = fileUri.split(delims);
					Log.d("Full URI", fileUri);
					Log.d("File Name",
							intent.getExtras().getString(EXTRAS_FILE_NAME));
					Log.d("Sending name",
							intent.getExtras().getString(EXTRAS_FILE_NAME));
					OutputStream out = sock.getOutputStream();
					PrintWriter outStr = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(out)), true);
					outStr.println(tokens[tokens.length - 1]);
					inputSignal = in.readLine();
					Log.d("Received", inputSignal);
					if (inputSignal.equals("OK") || inputSignal.equals("OK\n")) {
						Log.d("Send Ready", "got OK sign");
						InputStream fileInput = context.getContentResolver()
								.openInputStream(Uri.parse(fileUri));
						DeviceDetail.copyFile(fileInput, out);
					}
				} catch (Exception e) {
					Log.d("communication failure", e.toString());
				} finally {
					sock.close();
					Log.d("Transfer service over", "");
				}
			} catch (Exception e) {
				Log.d("open ServerSocket failure", e.toString());
			}
			Log.d("4", "4");

			Intent remoteIntent = new Intent(ACTION_SENT_FILE);
			sendBroadcast(remoteIntent);

			/*
			 * try {
			 * 
			 * Log.d(WiFiDirectActivity.TAG, "Opening Server socket - ");
			 * socket.bind(null); socket.connect((new InetSocketAddress(host,
			 * port)), SOCKET_TIMEOUT);
			 * 
			 * Log.d(WiFiDirectActivity.TAG, "Client socket - " +
			 * socket.isConnected()); OutputStream stream =
			 * socket.getOutputStream(); ContentResolver cr =
			 * context.getContentResolver();
			 * 
			 * InputStream is = null; try { is =
			 * cr.openInputStream(Uri.parse(fileUri)); } catch
			 * (FileNotFoundException e) { Log.d(WiFiDirectActivity.TAG,
			 * e.toString());
			 * 
			 * } DeviceDetailFragment.copyFile(is, stream);
			 * Log.d(WiFiDirectActivity.TAG, "Client: Data written"); } catch
			 * (IOException e) { Log.e(WiFiDirectActivity.TAG, e.getMessage());
			 * } finally { if (socket != null) { if (socket.isConnected()) { try
			 * { socket.close(); } catch (IOException e) { // Give up
			 * e.printStackTrace(); } } } }
			 */

		} else if (intent.getAction().equals(ACTION_REMOTE_CONTROL)) {
			ServerSocket socket;
			try {
				Log.d("44", "44");
				socket = new ServerSocket(9897);
				Log.d("45", "45");
				Socket sock = socket.accept();
				Log.d("46", "46");
				InputStream is = sock.getInputStream();
				br = new BufferedReader(new InputStreamReader(is));
				OutputStream os = sock.getOutputStream();
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
						os)), true);

				Log.d("5", "5");
				String signal = br.readLine();
				if (signal.equals("Hello")) {

				} else {
					Log.d("Signal fault", signal);
				}
				Log.d("6", "6");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		} else if (intent.getAction().equals(ACTION_BUTTON_PRESSED)) {
			String button_signal = intent.getExtras().getString(EXTRAS_BUTTON);
			char button = button_signal.charAt(0);
			Log.d(button_signal, Character.toString(button));
			try {
				if(pw==null)
					Log.d("Check printwriter!", "pw");
				pw.println(button);
				String receivedSignal = br.readLine();
				Log.d("receivedSignal for button pressed", receivedSignal);
				if (receivedSignal.equals("OK")) {
					if(button == 'Q'){
						Intent remoteIntent = new Intent(ACTION_QUIT);
						sendBroadcast(remoteIntent);
					}
				} else {

				}
			} catch (Exception e) {

			}

		}
	}
}
