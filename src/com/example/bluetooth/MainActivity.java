package com.example.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

	protected static final int UPDATE_LIST = 0;
	private static final String TAG = "my_bluetooth";
	private int Devicescount = 0;
	
	private ListView mListview = null;	
	private Context mContext = null;
	private SimpleAdapter mAdapter = null;
	private ArrayAdapter<String> arrayAdapter = null;
	private BluetoothAdapter mBluetoothAdapter= null;
	
	
	//搜索到的远程设备集合
	private ArrayList<HashMap<String, String>> discoveredDevices = new ArrayList<HashMap<String, String>>();
	private ArrayList<String> arrayList = new ArrayList<String>();
	  //蓝牙搜索广播的接收器
  	private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
  		
  		@Override
  		public void onReceive(Context context, Intent intent) {
  			//获取广播的Action
  			String action = intent.getAction();

  			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
  				Log.v(TAG, "ACTION_FOUND");
  				//发现远程蓝牙设备
  				//获取设备
  				HashMap<String, String> discovereDevice = new HashMap<String, String>();
  				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
  				bluetoothDevice.getBondState();
  				discovereDevice.put("name","NAME:" + bluetoothDevice.getName());
  				Log.v(TAG, "bluetoothDevice.getName(): " + bluetoothDevice.getName());
  				discovereDevice.put("mac","MAC:" + bluetoothDevice.getAddress());
  				Short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
  				discovereDevice.put("rssi","RSSI:" + rssi.toString());
  				Log.v(TAG, "EXTRA_RSSI:" + rssi.toString());
  				discoveredDevices.add(discovereDevice);
  				
  				arrayList.add(bluetoothDevice.getAddress());
  				
  			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
  				Log.v(TAG, "ACTION_DISCOVERY_FINISHED");
  				//搜索结束
  				if (discoveredDevices.size() > Devicescount) {
  					for (int i = 0 ;i < Devicescount ;Devicescount--) {
  						discoveredDevices.remove(i);
  					}
  					Devicescount = discoveredDevices.size();
  					mhandler.sendEmptyMessage(UPDATE_LIST);
  				}
  			}
  		}
  	};
	
  	private Handler mhandler = new Handler(){
    	@Override
		public void handleMessage(Message msg) {
    		Log.v(TAG, "handleMessage ");
			//处理消息
			switch (msg.what) {
			case UPDATE_LIST:
				mAdapter.notifyDataSetChanged();
				Log.v(TAG, "discoveredDevices.getCount(): " + discoveredDevices.size());
				break;
			}
			doDiscovery();
		}
    };
  	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListview = (ListView)findViewById(R.id.BTlist);
        mContext = this;
        
        Log.v(TAG, "onCreate");
        //蓝牙适配器
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	getActionBar().setDisplayShowHomeEnabled(false);
    	
    	mListview.setEnabled(false);
    	
		mAdapter = new SimpleAdapter(mContext,
        		discoveredDevices,
				R.layout.btitem, 
				new String[]{"name","mac","rssi"}, 
				new int[]{R.id.bt_name,R.id.bt_mac,R.id.bt_rssi});
        mListview.setAdapter(mAdapter); 
        
        IntentFilter btIntentFilter = new IntentFilter();
        btIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, btIntentFilter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle action bar item clicks here. The action bar will 
 		// automatically handle clicks on the Home/Up button, so long 
 		// as you specify a parent activity in AndroidManifest.xml. 
 		int id = item.getItemId();
 		if (id == R.id.action_settings) {
 			doDiscovery();
 			return true;
 		}
 		return false;
 	} 
    
  	
  	private void doDiscovery() {
  		Log.v(TAG, "go to opened");
  		if(!mBluetoothAdapter.isEnabled()){
  			mBluetoothAdapter.enable();
  		}
		if(mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.cancelDiscovery();
		}
		Log.v(TAG, "startDiscovery Devicescount:" + Devicescount);
		
		mBluetoothAdapter.startDiscovery();
		Log.i(TAG, "opened");
	}
  	
  	@Override
  	protected void onDestroy() {
  		unregisterReceiver(discoveryReceiver);
  		super.onDestroy();
  	}
}
