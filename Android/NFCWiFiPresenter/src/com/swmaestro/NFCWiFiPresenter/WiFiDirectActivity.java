/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swmaestro.NFCWiFiPresenter;

import java.io.UnsupportedEncodingException;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.swmaestro.NFCWiFiPresenter.DeviceList.DeviceActionListener;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends Activity implements ChannelListener,
		DeviceActionListener {

	public static final String TAG = "wifidirectdemo";
	private WifiP2pManager manager;
	private WifiP2pConfig config;
	private boolean isWifiP2pEnabled = false;
	private boolean retryChannel = false;
	static boolean initialTry = true;

	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	public BroadcastReceiver receiver = null;

	private NfcAdapter mNfcAdapter;

	static String targetMac = "00:26:66:4b:a0:f2";

	/**
	 * @param isWifiP2pEnabled
	 *            the isWifiP2pEnabled to set
	 */
	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	@SuppressLint("ShowToast")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.d("creation", "started");

		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
		manager.cancelConnect(channel, new ActionListener() {

			public void onFailure(int arg0) {
				Log.d("clear service", "fail");
			}

			public void onSuccess() {
				Log.d("clear service", "success");
			}

		});

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		if (mNfcAdapter == null) {
			Toast.makeText(this, "No NFC Available", 3000);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {

		setIntent(intent);
	}

	/** register the BroadcastReceiver with the intent values to be matched */
	@Override
	public void onResume() {
		super.onResume();
		receiver = new WFDBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, intentFilter);
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		} else {
			AlertDialog.Builder alertDlg = new AlertDialog.Builder(this) ;   
			alertDlg.setTitle("Alert") ;
			alertDlg.setMessage("Please put the phone on the tag to go") ;   
			alertDlg.setPositiveButton("Close", new DialogInterface.OnClickListener() {
			 public void onClick(DialogInterface dialog, int whichButton) {   
			  finish() ;
			 }
			}) ;   
			alertDlg.show() ;
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	/**
	 * Remove all peers and clear all fields. This is called on
	 * BroadcastReceiver receiving a state change event.
	 */
	public void resetData() {
		DeviceList fragmentList = (DeviceList) getFragmentManager()
				.findFragmentById(R.id.frag_list);
		DeviceDetail fragmentDetails = (DeviceDetail) getFragmentManager()
				.findFragmentById(R.id.frag_detail);
		if (fragmentList != null) {
			fragmentList.clearPeers();
		}
		if (fragmentDetails != null) {
			fragmentDetails.resetViews();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_items, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.atn_direct_enable:
			if (manager != null && channel != null) {

				startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
			} else {
				Log.e(TAG, "channel or manager is null");
			}
			return true;

		case R.id.atn_direct_discover:
			if (!isWifiP2pEnabled) {
				Toast.makeText(WiFiDirectActivity.this,
						R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
				return true;
			}
			final DeviceList fragment = (DeviceList) getFragmentManager()
					.findFragmentById(R.id.frag_list);
			fragment.onInitiateDiscovery();
			discover();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void showDetails(WifiP2pDevice device) {
		DeviceDetail fragment = (DeviceDetail) getFragmentManager()
				.findFragmentById(R.id.frag_detail);
		fragment.showDetails(device);

	}

	private void discover() {

		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

			public void onSuccess() {
				Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
						Toast.LENGTH_SHORT).show();
			}

			public void onFailure(int reasonCode) {
				Toast.makeText(WiFiDirectActivity.this,
						"Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	public void connect(WifiP2pConfig config) {
		Log.d("connect status", "connecting...");

		manager.connect(channel, config, new ActionListener() {

			public void onSuccess() {
				Log.d("connect status", "success");
			}

			public void onFailure(int reason) {
				Log.d("connect status", "failed " + Integer.toString(reason));
				Toast.makeText(WiFiDirectActivity.this,
						"Connect failed. Retry. " + Integer.toString(reason),
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void disconnect() {
		final DeviceDetail fragment = (DeviceDetail) getFragmentManager()
				.findFragmentById(R.id.frag_detail);
		fragment.resetViews();
		manager.removeGroup(channel, new ActionListener() {

			public void onFailure(int reasonCode) {
				Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

			}

			public void onSuccess() {
				fragment.getView().setVisibility(View.GONE);
			}

		});
	}

	public void onChannelDisconnected() {

		if (manager != null && !retryChannel) {
			Toast.makeText(this, "Channel lost. Trying again",
					Toast.LENGTH_LONG).show();
			resetData();
			retryChannel = true;
			manager.initialize(this, getMainLooper(), this);
		} else {
			Toast.makeText(
					this,
					"Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
					Toast.LENGTH_LONG).show();
		}
	}

	public void cancelDisconnect() {

		if (manager != null) {
			final DeviceList fragment = (DeviceList) getFragmentManager()
					.findFragmentById(R.id.frag_list);
			if (fragment.getDevice() == null
					|| fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
				disconnect();
			} else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
					|| fragment.getDevice().status == WifiP2pDevice.INVITED) {

				manager.cancelConnect(channel, new ActionListener() {

					public void onSuccess() {
						Toast.makeText(WiFiDirectActivity.this,
								"Aborting connection", Toast.LENGTH_SHORT)
								.show();
					}

					public void onFailure(int reasonCode) {
						Toast.makeText(
								WiFiDirectActivity.this,
								"Connect abort request failed. Reason Code: "
										+ reasonCode, Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
		}

	}

	void processIntent(Intent intent) {

		discover();
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

		NdefMessage msg = (NdefMessage) rawMsgs[0];

		try {
			byte[] payload = msg.getRecords()[0].getPayload();

			String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8"
					: "UTF-16";

			int languageCodeLength = payload[0] & 0077;
			String languageCode = new String(payload, 1, languageCodeLength,
					"US-ASCII");

			targetMac = new String(payload, languageCodeLength + 1,
					payload.length - languageCodeLength - 1, textEncoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("Got this from tag", targetMac);

		Toast.makeText(getApplicationContext(), targetMac, Toast.LENGTH_LONG)
				.show();

		Log.d("connect status", "33");
		config = new WifiP2pConfig();
		config.deviceAddress = targetMac;
		config.groupOwnerIntent = 15;
		config.wps.setup = WpsInfo.PBC;

		Log.d("connect status", "44");

	}

}
