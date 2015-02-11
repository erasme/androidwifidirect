package org.erasme.androidwifidirect;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerSocketHandler extends Thread {
    ServerSocket socket = null;
    private final int THREAD_COUNT = 10;

    public ServerSocketHandler() throws IOException {
        try {
            socket = new ServerSocket(6666);
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdownNow();
            throw e;
        }
    }

    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void run() {
        while (true) {
            try {
                Socket client = socket.accept();
                Log.i("MyApp", "New client connected");

                InputStream input = client.getInputStream();
                OutputStream output = client.getOutputStream();

                // write a message and bye bye client
                byte[] message = "Hello World !\n".getBytes("UTF-8");
                output.write(message);
                output.flush();
                client.close();
            }
            catch (IOException e) {
                try {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (IOException ioe) {

                }
                e.printStackTrace();
                pool.shutdownNow();
                break;
            }
        }
    }
}
