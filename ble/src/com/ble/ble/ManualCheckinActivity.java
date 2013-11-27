package com.ble.ble;

import java.util.ArrayList;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


public class ManualCheckinActivity extends Activity implements IBeaconConsumer{
	ListView listView;
	BluetoothAdapter bta;
	ArrayAdapter<String> adapter;
	ProgressBar p;
	Button b;
	boolean hasBLE = false;
	private ArrayList<BluetoothDevice> leDevices;
	// Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning;
    private Handler mHandler;
    
    //iBeacon stuff
    private IBeaconManager iBeaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual_checkin);
		//this.setTitle("Scanning");
		bta = BluetoothAdapter.getDefaultAdapter();
		p = (ProgressBar) findViewById(R.id.progressBar1);
		p.setVisibility(8);
		
		b = (Button) findViewById(R.id.button1);
		b.setText("Start scan");
		
		//Check if device supports bluetooth low energy
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			hasBLE = true;
			iBeaconManager = IBeaconManager.getInstanceForApplication(this);
			iBeaconManager.bind(this);
			b.setVisibility(8);
		}
		
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.listView1);

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {

              @Override
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {
                
               // ListView Clicked item index
               int itemPosition = position;
               bta.cancelDiscovery();
               // ListView Clicked item value
               String itemValue = (String) listView.getItemAtPosition(position);
                  
                // Show Alert 
                Toast.makeText(getApplicationContext(),
                  "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                  .show();
             
              }

         });

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        
        IntentFilter filterScanDone = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filterScanDone);
        
        IntentFilter filterScanStarted = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filterScanStarted);
        

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manual_checkin, menu);
		return true;
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mReceiver);
		iBeaconManager.unBind(this);
	}

    // Create a BroadcastReceiver
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                adapter.add(device.getName() + "\n" + device.getAddress());
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
            	b.setEnabled(true);
            	p.setVisibility(4);
            	b.setText("Scan again");
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
            	adapter.clear();
            	b.setEnabled(false);
            	p.setVisibility(0);
            	b.setText("Scanning...");
            }
        }
    };
    
    @SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                byte[] scanRecord) {
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
//                   if(!leDevices.contains(device)){
//                	   leDevices.add(device);
            	   if(adapter.getPosition(device.getAddress()) == -1){
            		   adapter.add(device.getAddress());
                	   ParcelUuid[] uuids= device.getUuids();
                	   for(ParcelUuid uuid : uuids){
                		   adapter.add(uuid.getUuid().toString());	
                	   }
                	   adapter.add(device.getName());
            	   }
            	   
//                   }
               }
           });
       }
    };
    
    public void scanButton(View v){
    	if(!hasBLE){
    		bta.startDiscovery();
    	}
    }
    
    //action: 1=add, 2 = remove
    private void logToDisplay(final String line, final int action) {
        runOnUiThread(new Runnable() {
            public void run() {
            	if(action == 1){
            		adapter.add(line);
            	} else {
                    adapter.remove(line);
            	}
            }
        });
}
    
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) {
        	logToDisplay(region.getUniqueId(),1);
        }

        @Override
        public void didExitRegion(Region region) {
        	logToDisplay(region.getUniqueId(),2);
        }

		@Override
		public void didDetermineStateForRegion(int arg0, Region arg1) {
		}
        });

        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("testRegion", "23542266-18D1-4FE4-B4A1-23F8195B9D39", 1, null));
        } catch (RemoteException e) {   }

    }
}
