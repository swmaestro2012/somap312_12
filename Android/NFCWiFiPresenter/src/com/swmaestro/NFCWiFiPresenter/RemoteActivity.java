package com.swmaestro.NFCWiFiPresenter;

import java.io.BufferedReader;

import java.io.PrintWriter;
import java.net.Socket;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class RemoteActivity extends Activity {

	BufferedReader br;
	PrintWriter pw;
	Socket sock;
	String signal;
	Button back, forward, quit;
	Intent buttonIntent;
	Activity activity;
	BroadcastReceiver br1, br2;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote);
		activity = this;

		back = (Button) findViewById(R.id.buttonB);
		forward = (Button) findViewById(R.id.buttonF);
		quit = (Button) findViewById(R.id.buttonQ);

		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d("Button pressed", "back");
				buttonIntent = new Intent(activity, NetworkModule.class);
				buttonIntent
						.setAction(NetworkModule.ACTION_BUTTON_PRESSED);
				buttonIntent.putExtra(NetworkModule.EXTRAS_BUTTON, "L");
				startService(buttonIntent);
			}
		});

		forward.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d("Button pressed", "forward");
				buttonIntent = new Intent(activity, NetworkModule.class);
				buttonIntent
						.setAction(NetworkModule.ACTION_BUTTON_PRESSED);
				buttonIntent.putExtra(NetworkModule.EXTRAS_BUTTON, "R");
				startService(buttonIntent);
			}
		});

		quit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d("Button pressed", "quit");
				exitDialog();

			}
		});

		Intent receivedIntent = getIntent();

		Log.d("11", "11");
		Intent fileIntent = new Intent(this, NetworkModule.class);
		fileIntent.putExtras(receivedIntent.getExtras());
		fileIntent.setAction(NetworkModule.ACTION_SEND_FILE);
		startService(fileIntent);

		Log.d("22", "22");
		IntentFilter serviceFilter = new IntentFilter(
				NetworkModule.ACTION_SENT_FILE);
		br1 = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d("33", "33");
				Intent remoteIntent = new Intent(activity,
						NetworkModule.class);
				remoteIntent
						.setAction(NetworkModule.ACTION_REMOTE_CONTROL);
				startService(remoteIntent);
				setVisible();
			}
		};
		registerReceiver(br1, serviceFilter);

		IntentFilter serviceFilter2 = new IntentFilter(
				NetworkModule.ACTION_QUIT);
		br2 = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d("44", "44");

				finish();
			}
		};
		registerReceiver(br2, serviceFilter2);

		Intent intent = new Intent(RemoteActivity.this,
				DeviceDetail.class);
		setResult(RESULT_OK, intent);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(br1);
		unregisterReceiver(br2);
	}

	public void setVisible() {
		back.setVisibility(View.VISIBLE);
		forward.setVisibility(View.VISIBLE);
		quit.setVisibility(View.VISIBLE);
	}

	private void exitDialog() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Exit RemoteControl")
				.setMessage("Are you sure you want to close this app?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								buttonIntent = new Intent(activity,
										NetworkModule.class);
								buttonIntent
										.setAction(NetworkModule.ACTION_BUTTON_PRESSED);
								buttonIntent.putExtra(
										NetworkModule.EXTRAS_BUTTON, "Q");
								startService(buttonIntent);
								
								ProgressDialog.show(RemoteActivity.this,
										"Wait for a while", "Terminating..."
												, true, true
								// new DialogInterface.OnCancelListener() {
								//
								// @Override
								// public void onCancel(DialogInterface dialog) {
								// ((DeviceActionListener)
								// getActivity()).cancelDisconnect();
								// }
								// }
										);
							}

						}).setNegativeButton("No", null).show();
	}

	@Override
	public void onBackPressed() {
		exitDialog();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_remote, menu);
		return true;
	}
}
