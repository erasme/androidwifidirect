package org.erasme.androidwifidirect;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.view.View;
import android.widget.Button;

/**
 * Created by daniel on 06/02/15.
 */
public class DeviceButton extends Button {
    private WifiP2pDevice device;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public DeviceButton(WifiP2pManager manager, WifiP2pManager.Channel channel,
                        MainActivity activity)
    {
        super(activity);
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;

        this.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                ConnectToDevice();
            }
        });
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }

    public WifiP2pDevice getDevice() {
        return this.device;
    }

    public void ConnectToDevice() {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        final String deviceName = device.deviceName;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                mActivity.appendLog("Connect SUCCESS with "+deviceName);
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                mActivity.appendLog("Connect FAILS with "+deviceName+", reason: "+reason);
            }
        });
    }
}
