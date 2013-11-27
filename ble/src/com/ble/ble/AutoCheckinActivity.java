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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

public class AutoCheckinActivity extends Activity implements IBeaconConsumer{
	BluetoothAdapter bta;
	boolean hasBLE = false;
	Vector<Region> regions;
    
    //iBeacon stuff
    private IBeaconManager iBeaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_checkin);

		bta = BluetoothAdapter.getDefaultAdapter();
		
		//Check if device supports bluetooth low energy
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			hasBLE = true;
			iBeaconManager = IBeaconManager.getInstanceForApplication(this);
			iBeaconManager.bind(this);
			
			regions = new Vector<Region>(1,1);
			//Set regions, should be taken from database
			//for regions in database .....
			regions.add(new Region("testRegion", "23542266-18D1-4FE4-B4A1-23F8195B9D39", 1, null));
		}        

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.auto_checkin, menu);
		return true;
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		iBeaconManager.unBind(this);
	}

       //action: 1=add, 2 = remove
    private void logToDisplay(final String line, final int action) {
        runOnUiThread(new Runnable() {
            public void run() {
            	if(action == 1){
                	myToast("Entered");
            	} else {
            		
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
        	for(Region region : regions){
        		iBeaconManager.startMonitoringBeaconsInRegion(region);
        	}
        } catch (RemoteException e) {   }

    }
    
    public void myToast(String msg){
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
