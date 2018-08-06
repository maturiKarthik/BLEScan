package com.example.dunasys.blescan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    private Context context;

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION  = 425;
    // CONSTANTS TO SCAN THE DEVICES
    public boolean mScanning;
    public ListView device ;
    List<String> found = new ArrayList<String>();
   HashMap<String,String> new_Device_db = new HashMap<String,String>();
    Set<String> hs = new HashSet<>();



    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Log.d("TAG",""+device.getAddress()+"=="+device.getName());
                            //hs.clear();
                            new_Device_db.put(device.getAddress(),device.getName());
                            hs.add(device.getAddress());
                        }
                    });
                }
            };


    // Scanning/Stoping the Device Scan  ..
    private void scanLeDevice(boolean enable) {
        if(enable == true){
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }else{
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            // List Adpater To Display All  The Devices ..
            found.addAll(hs);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item,found);
            device.setAdapter(arrayAdapter);



        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        device = (ListView)findViewById(R.id.list_item);


        // An Alert Dialog To Tell How It Works .. !

        AlertDialog.Builder Help = new AlertDialog.Builder(this);
        Help.setTitle("POC_BLE Welcome Guide");
        Help.setMessage("Step 1 : To Scan Device Go To Menu Start Scan And Stop Scan\nStep 2: You will Find The Device Address\n " +
                "Step 3 : Click On the Device you want to connect.\n Step 4:In the Next Screen you can see the vehicule logs.");
        Help.setPositiveButton("I Understood",null);
        Help.show();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d("TAG","BLE NOT-ENABLED PHONE");
            finish();
        }else{
            Log.d("TAG","DEVICES ENABLED");


            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // IF NOT ENABLED IT MAKES A POPUP TO ENABLE BLUETOOTH
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent  enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }


            // CLIKCING ACTION ON DEVICE ..
            device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getApplicationContext(),""+found.get(i),Toast.LENGTH_LONG).show();
                    // Starting An Intent ..
                    Intent sendData = new Intent(MainActivity.this,DeviceScanDetails.class);
                    sendData.putExtra("data",found.get(i));
                    startActivity(sendData);
                }
            });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, yay! Start the Bluetooth device scan.
                    Log.d("TAG","RUNTIIME PERMISSION OK");
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                    Log.d("TAG","RUNTIIME PERMISSION NOTOK");
                    Toast.makeText(this, "NO PERMISSION ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);

        return true;

    }

    // Menu Events To Start And Stop Scanning ..
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.startScan:

                Log.d("TAG",""+found.isEmpty());

                //Checking the permission..
                boolean status =checkPermission();
                if (status == true){
                    scanLeDevice(true);
                }

                Log.d("TAG","START SCANNING");
                Toast.makeText(this,"STARTED SCANNING",Toast.LENGTH_LONG).show();
                break;
            case R.id.stopScan:
                scanLeDevice(false);
                Log.d("TAG","STOP SCANNING");
                break;

        }
        return true;
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            Log.d("TAG","CHECK PERMISSION - TRUE");
            return true;

        } else {
            Log.d("TAG","CHECK PERMISSION - FALSE");
            return false;

        }
    }


}
