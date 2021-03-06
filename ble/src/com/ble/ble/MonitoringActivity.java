package com.ble.ble;
import java.util.Collection;
import java.util.Iterator;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.EditText;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

public class MonitoringActivity extends Activity implements IBeaconConsumer {
    protected static final String TAG = "MonitoringActivity";
    private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_monitoring);
        iBeaconManager.bind(this);
    }
    @Override 
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }
    
    private void logToDisplay(final String line) {
            runOnUiThread(new Runnable() {
                public void run() {
                        EditText editText = (EditText)MonitoringActivity.this
                                            .findViewById(R.id.editText1);
                        editText.append(line+"\n");                    
                }
            });
    }
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) {
        	logToDisplay("I just saw an iBeacon for the first time!");
        }

        @Override
        public void didExitRegion(Region region) {
                logToDisplay("I no longer see an iBeacon");
        }

		@Override
		public void didDetermineStateForRegion(int arg0, Region arg1) {
		}
        });

        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", "23542266-18D1-4FE4-B4A1-23F8195B9D39", 1, null));
        } catch (RemoteException e) {   }

    }

}