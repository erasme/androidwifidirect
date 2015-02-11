package org.erasme.androidwifidirect;

import android.util.*;
import android.content.*;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager.*;
import android.net.NetworkInfo;

import java.io.IOException;


/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers

            mActivity.appendLog("Scan for peers");
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, new PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Log.i("MyApp", "onPeersAvailable size: "+peers.getDeviceList().size());
                        mActivity.appendLog("onPeersAvailable size: "+peers.getDeviceList().size());

                        mActivity.devicesList.removeAllViews();

                        for(WifiP2pDevice device: peers.getDeviceList()) {
                            Log.i("MyApp", "P2p device name: "+device.deviceName);
                            mActivity.appendLog("P2p device name: "+device.deviceName+", address: "+device.deviceAddress);

                            DeviceButton deviceButton = new DeviceButton(mManager, mChannel, mActivity);
                            deviceButton.setText(device.deviceName);
                            deviceButton.setDevice(device);
                            mActivity.devicesList.addView(deviceButton);

                            //ConnectToDevice(device);

                        }
                    }
                });
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                mActivity.appendLog("WIFI P2P Connected");

                // we are connected with the other device, request connection
                // info to find group owner IP

                //DeviceDetailFragment fragment = (DeviceDetailFragment) activity
                //        .getFragmentManager().findFragmentById(R.id.frag_detail);
                //mManager.requestConnectionInfo(mChannel, fragment);
                mManager.requestConnectionInfo(mChannel, new ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        if(info.isGroupOwner) {
                            mActivity.appendLog("groupOwnerAddress: " + info.groupOwnerAddress.getHostAddress()+ " SERVER");

                            try {
                                ServerSocketHandler server = new ServerSocketHandler();
                                server.start();
                            }
                            catch(IOException e) {
                                Log.e("MyApp", "Socket server FAILS");
                            }
                        }
                        else {
                            mActivity.appendLog("groupOwnerAddress: " + info.groupOwnerAddress.getHostAddress()+ " CLIENT");

                            ClientSocketHandler client = new ClientSocketHandler(info.groupOwnerAddress, mActivity);
                            client.start();
                        }
                    }
                });
            } else {
                // It's a disconnect
                mActivity.appendLog("WIFI P2P Disconnected");
                //activity.resetData();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            WifiP2pDevice device = (WifiP2pDevice) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mActivity.appendLog("Device status -" + device.status);
        }
    }
}