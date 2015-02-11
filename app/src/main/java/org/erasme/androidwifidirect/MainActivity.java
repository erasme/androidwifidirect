package org.erasme.androidwifidirect;

import java.util.*;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.nsd.*;
import android.net.wifi.p2p.WifiP2pManager.*;


/**
 * Created by daniel on 05/02/15.
 */
public class MainActivity extends Activity {
    WifiP2pManager mManager;
    Channel mChannel;
    BroadcastReceiver mReceiver;
    WifiP2pDnsSdServiceRequest serviceRequest;

    TextView textView;
    IntentFilter mIntentFilter;
    LinearLayout devicesList;

    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_mojmoc";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Log.i("MyApp", "Hello World !");

        LinearLayout vbox = new LinearLayout(this);
        vbox.setOrientation(LinearLayout.VERTICAL);
        setContentView(vbox);

        LinearLayout hbox = new LinearLayout(this);
        hbox.setOrientation(LinearLayout.HORIZONTAL);
        vbox.addView(hbox);

        Button button = new Button(this);
        button.setText("Discover peers");
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Log.i("MyApp", "Try discover peers");
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i("MyApp", "discoverPeers onSuccess");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Log.i("MyApp", "discoverPeers onFailure");
                    }
                });
            }
        });
        hbox.addView(button);

        Button clearButton = new Button(this);
        clearButton.setText("Clear logs");
        clearButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Log.i("MyApp", "textView: "+textView.getText());
                textView.setText("\n");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        textView.setText("");
//                    }
//                });
            }
        });
        hbox.addView(clearButton);

        LinearLayout hbox2 = new LinearLayout(this);
        hbox2.setOrientation(LinearLayout.HORIZONTAL);
        vbox.addView(hbox2);

        textView = new TextView(this);
        //textView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        textView.setText("\n");
        //textView.setGravity(Gravity.FILL_HORIZONTAL);
        hbox2.addView(textView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        devicesList = new LinearLayout(this);
        //devicesList.getLayoutParams().width = 300;
        //devicesList.setGravity(Gravity.RIGHT);
        devicesList.setOrientation(LinearLayout.VERTICAL);
        hbox2.addView(devicesList, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2));

        devicesList.addView(new Button(this));

        // handle Bonjour
        startRegistrationAndDiscovery();

    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void appendLog(String text) {
        Log.i("MyApp", "appendLog/ "+text + "\n" + textView.getText());
        textView.setText(text + "\n" + textView.getText());
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistrationAndDiscovery() {
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        mManager.addLocalService(mChannel, service, new ActionListener() {
            @Override
            public void onSuccess() {
                appendLog("Added Local Service");
            }
            @Override
            public void onFailure(int error) {
                appendLog("Failed to add a service");
            }
        });
        discoverService();
    }

    private void discoverService() {
        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */
        mManager.setDnsSdResponseListeners(mChannel,
                new DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {
                        // A service has been discovered. Is this our app?
                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

                            // update the UI and add the item the discovered
                            // device.
                            /*WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
                                    .findFragmentByTag("services");
                            if (fragment != null) {
                                WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
                                        .getListAdapter());
                                WiFiP2pService service = new WiFiP2pService();
                                service.device = srcDevice;
                                service.instanceName = instanceName;
                                service.serviceRegistrationType = registrationType;
                                adapter.add(service);
                                adapter.notifyDataSetChanged();
                                appendLog("onBonjourServiceAvailable " + instanceName);
                            }*/
                            appendLog("onBonjourServiceAvailable " + instanceName);
                        }
                    }
                }, new DnsSdTxtRecordListener() {
                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        appendLog(device.deviceName + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
                    }
                });
        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest,
                new ActionListener() {
                    @Override
                    public void onSuccess() {
                        appendLog("Added service discovery request");
                    }
                    @Override
                    public void onFailure(int arg0) {
                        appendLog("Failed adding service discovery request");
                    }
                });
        mManager.discoverServices(mChannel, new ActionListener() {
            @Override
            public void onSuccess() {
                appendLog("Service discovery initiated");
            }
            @Override
            public void onFailure(int arg0) {
                appendLog("Service discovery failed");
            }
        });
    }

}
