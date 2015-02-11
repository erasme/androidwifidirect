package org.erasme.androidwifidirect;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocketHandler extends Thread {

    private InetAddress mAddress;
    private MainActivity mActivity;

    public ClientSocketHandler(InetAddress groupOwnerAddress, MainActivity activity) {
        this.mAddress = groupOwnerAddress;
        this.mActivity =  activity;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                    6666), 5000);
            Log.i("MyApp", "Launching CLIENT handler");

            InputStream inputStream = socket.getInputStream();

            // read a buffer and bye bye
            byte[] buffer = new byte[4096];
            int size = inputStream.read(buffer);

            final String receivedMessage = new String(buffer, 0, size, "UTF-8");
            Log.i("MyApp", "MESSAGE RECEIVED: "+receivedMessage);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActivity.appendLog("MESSAGE RECEIVED: "+receivedMessage);
                }
            });

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}