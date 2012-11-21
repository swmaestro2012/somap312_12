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

import android.app.Activity;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.swmaestro.NFCWiFiPresenter.DeviceList.DeviceActionListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetail extends Fragment implements
		ConnectionInfoListener {

	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	public View mContentView = null;
	private WifiP2pDevice device;
	private WifiP2pInfo info;
	private boolean fileSent = false;
	private static final int REQUEST_CODE = 1234;
	private static final int QUIT_CODE = 123;
	private static final String CHOOSER_TITLE = "Select a file";
	public Button chooseFile;

	ProgressDialog progressDialog = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.d("Fragment Created!!!!", "Detail!");

		mContentView = inflater.inflate(R.layout.device_detail, null);
		mContentView.findViewById(R.id.btn_connect).setOnClickListener(
				new View.OnClickListener() {

					public void onClick(View v) {
						WifiP2pConfig config = new WifiP2pConfig();
						config.deviceAddress = device.deviceAddress;
						config.groupOwnerIntent = 15;
						config.wps.setup = WpsInfo.PBC;
						if (progressDialog != null
								&& progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						progressDialog = ProgressDialog.show(getActivity(),
								"Press back to cancel", "Connecting to :"
										+ device.deviceAddress, true, true
						// new DialogInterface.OnCancelListener() {
						//
						// @Override
						// public void onCancel(DialogInterface dialog) {
						// ((DeviceActionListener)
						// getActivity()).cancelDisconnect();
						// }
						// }
								);
						((DeviceActionListener) getActivity()).connect(config);

					}
				});

		mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
				new View.OnClickListener() {

					public void onClick(View v) {
						((DeviceActionListener) getActivity()).disconnect();
					}
				});

		chooseFile = (Button) mContentView.findViewById(R.id.btn_start_client);
		chooseFile.setOnClickListener(
				new View.OnClickListener() {

					public void onClick(View v) {
						// Allow user to pick an image from Gallery or other
						// registered apps
						selectFile();
						
					}
				});

		return mContentView;
	}

	public void selectFile() {

		Intent target = FileUtils.createGetContentIntent();
		Intent intent = Intent.createChooser(target, CHOOSER_TITLE);
		try {
			startActivityForResult(intent, REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
			// The reason for the existence of aFileChooser
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// User has picked an image. Transfer it to group owner i.e peer using
		// FileTransferService.
		if (data == null)
			return;
		switch (requestCode) {
		case REQUEST_CODE:
			if (resultCode == Activity.RESULT_OK) {
				// The URI of the selected file
				final Uri uri = data.getData();
				File file = FileUtils.getFile(uri);
				// Create a File from this Uri
				TextView statusText = (TextView) mContentView
						.findViewById(R.id.status_text);
				statusText.setText("Sending: " + uri);
				Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
				
				Intent remoteIntent = new Intent(getActivity(),
						RemoteActivity.class);
				remoteIntent.putExtra(NetworkModule.EXTRAS_FILE_PATH,
						uri.toString());
				remoteIntent.putExtra(NetworkModule.EXTRAS_FILE_NAME, file.getName());
				/*
				remoteIntent.setAction(FileTransferService.ACTION_SEND_FILE);

				remoteIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
						info.groupOwnerAddress.getHostAddress());

				remoteIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
						8988);
						*/
				Log.d("2", "2");
				startActivityForResult(remoteIntent, QUIT_CODE);
				
				
			}
			break;
		case QUIT_CODE:
			Log.d("QUIT RECEIVED!!!", "QUIT_CODE");
			if(resultCode == Activity.RESULT_OK){
				
				getActivity().finish();
			}
		}
		Log.d("1", "1");
	}

	public WifiP2pInfo getInfo() {
		return info;
	}

	public void onConnectionInfoAvailable(final WifiP2pInfo info) {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		this.info = info;
		this.getView().setVisibility(View.VISIBLE);

		// The owner IP is now known.
		TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
		view.setText(getResources().getString(R.string.group_owner_text)
				+ ((info.isGroupOwner == true) ? getResources().getString(
						R.string.yes) : getResources().getString(R.string.no)));

		// InetAddress from WifiP2pInfo struct.
		view = (TextView) mContentView.findViewById(R.id.device_info);
		view.setText("Group Owner IP - "
				+ info.groupOwnerAddress.getHostAddress());

		// After the group negotiation, we assign the group owner as the file
		// server. The file server is single threaded, single connection server
		// socket.
		if (info.groupFormed && !info.isGroupOwner) {
			//this will be not called
			/*
			new FileServerAsyncTask(getActivity(),
					mContentView.findViewById(R.id.status_text)).execute();
					*/
		} else if (info.groupFormed) {
			// The other device acts as the client. In this case, we enable the
			// get file button.
			mContentView.findViewById(R.id.btn_start_client).setVisibility(
					View.VISIBLE);
			((TextView) mContentView.findViewById(R.id.status_text))
					.setText(getResources().getString(R.string.client_text));
		}

		// hide the connect button
		mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
	}

	/**
	 * Updates the UI with device data
	 * 
	 * @param device
	 *            the device to be displayed
	 */
	public void showDetails(WifiP2pDevice device) {
		this.device = device;
		this.getView().setVisibility(View.VISIBLE);
		TextView view = (TextView) mContentView
				.findViewById(R.id.device_address);
		view.setText(device.deviceAddress);
		view = (TextView) mContentView.findViewById(R.id.device_info);
		view.setText(device.toString());

	}

	/**
	 * Clears the UI fields after a disconnect or direct mode disable operation.
	 */
	public void resetViews() {
		mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
		TextView view = (TextView) mContentView
				.findViewById(R.id.device_address);
		view.setText(R.string.empty);
		view = (TextView) mContentView.findViewById(R.id.device_info);
		view.setText(R.string.empty);
		view = (TextView) mContentView.findViewById(R.id.group_owner);
		view.setText(R.string.empty);
		view = (TextView) mContentView.findViewById(R.id.status_text);
		view.setText(R.string.empty);
		mContentView.findViewById(R.id.btn_start_client).setVisibility(
				View.GONE);
		this.getView().setVisibility(View.GONE);
	}

	public boolean isFileSent() {
		// TODO Auto-generated method stub
		return fileSent;
	}

	public void setFileSent(boolean b) {
		// TODO Auto-generated method stub
		fileSent = b;
		
	}

	public static boolean copyFile(InputStream inputStream, OutputStream out) {
		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				out.write(buf, 0, len);

			}
			out.close();
			inputStream.close();
		} catch (IOException e) {
			Log.d(WiFiDirectActivity.TAG, e.toString());
			return false;
		}
		return true;
	}

	

}
