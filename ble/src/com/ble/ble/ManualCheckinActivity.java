package com.ble.ble;

import java.util.Vector;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;


public class ManualCheckinActivity extends Activity implements IBeaconConsumer{
	static final int CHECKIN_SUCCESS = 1;
	static final int CHECKIN_FAIL = 2;
	ListView listView;
	BluetoothAdapter bta;
	ArrayAdapter<String> adapter;
	Vector<String> uuids;
	ProgressBar p;
	Button b;
	boolean hasBLE = false;
	Vector<Region> regions;
	static final int CHECKIN_REQUEST = 1;
    
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
			
			regions = new Vector<Region>(1,1);
			uuids = new Vector<String>(1,1);
			//Set regions, should be taken from database
			//for regions in database .....
			regions.add(new Region("testRegion", "23542266-18D1-4FE4-B4A1-23F8195B9D39", 1, null));
		}
		
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.listView1);

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written

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
//                  
//                // Show Alert 
//                Toast.makeText(getApplicationContext(),
//                  "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
//                  .show();
             
                Intent intent = new Intent(getApplicationContext(), SendCheckinActivity.class);
                
                intent.putExtra("UUID", uuids.get(position));
                startActivityForResult(intent,CHECKIN_REQUEST);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode <= CHECKIN_REQUEST){
			if(resultCode == CHECKIN_SUCCESS){
				Toast.makeText(getApplicationContext(),
						"Checkin done", Toast.LENGTH_LONG)
						.show();
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						"Checkin failed", Toast.LENGTH_LONG)
						.show();
			}
		}
	}
	
    // Create a BroadcastReceiver
	// Not used with BLE
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
    
    
    //Not used with BLE
    public void scanButton(View v){
    	if(!hasBLE){
    		bta.startDiscovery();
    	}
    }
    
    //action: 1=add, 2 = remove
    private void logToDisplay(final String line, final int action, final String uuid) {
        runOnUiThread(new Runnable() {
            public void run() {
            	if(action == 1){
            		adapter.add(line);
            		uuids.add(uuid);
            		myToast();
            	} else {
                    adapter.remove(line);
                    uuids.remove(uuid);
            	}
            }
        });
}
    
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) {
        	logToDisplay(region.getUniqueId(),1,region.getProximityUuid());
        }

        @Override
        public void didExitRegion(Region region) {
        	logToDisplay(region.getUniqueId(),2,region.getProximityUuid());
        }

		@Override
		public void didDetermineStateForRegion(int arg0, Region arg1) {
		}
        });

        try {
        	for(Region region : regions){
        		iBeaconManager.startMonitoringBeaconsInRegion(region);
        	}
        } catch (RemoteException e) {   }

    }
    
    public void myToast(){
    	Toast.makeText(getApplicationContext(),
                "Found it :D" , Toast.LENGTH_LONG)
                .show();
    }
}