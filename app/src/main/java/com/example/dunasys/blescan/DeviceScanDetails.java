package com.example.dunasys.blescan;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DeviceScanDetails extends Activity implements View.OnClickListener {



    //UI INITIALISATION ..
    TextView DEVICE_NAME, CONNECTION_STATE, Display;
    Button connect, dis_connect, WRITE,canConfig;
    Spinner spinner_query;


    // Dynamic TEXT VIEW EXAMPLE ..#
    TextView test;
    LinearLayout Logger;
    ListView log;
    ArrayList <String> Logs= new ArrayList<>();

    // Array Of Query For Spinner
    String[] query_name = {"SELECT THE QUERY TO WRITE", "Lecture Defauts CMM", "Lecture Defauts OBD", "Effacement defauts",
            "Effacement defauts OBD", "Lecture regime moteur",
            "Lecture nom ECU", "Lecture type de carburant", "Lecture information VIN", "Ouverture Actionneur", "Lecture position EGR"};

    QueryLoader queryLoader;
    byte[] query_data;

    String mDeviceAddress, mDeviceName;

    BluetoothGatt mbluetoothGatt;
    BluetoothAdapter mbluetoothAdapter;
    BluetoothManager mBluetoothManager;
    BluetoothDevice mDevice;

    boolean BLUTOOTH_ADAPTER_ENABLED;
    private static final int NOT_CONNECTED = 0;
    private static final int CONNECTING = 1;
    private static final int CONNECTED = 2;


    // CHAR FOR DLE_BLUETOOTH .. !
    public static final UUID CLIENT_CCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID DLE_SERVICE = UUID.fromString("00002100-1212-efde-1523-785fef13d123");
    public static final UUID DLE_WRITE = UUID.fromString("00002101-1212-efde-1523-785fef13d123");
    public static final UUID DLE_NOTIFY = UUID.fromString("00002102-1212-efde-1523-785fef13d123");

    //.. BlutoothCallBack .. !

    private final BluetoothGattCallback mBluetoothGattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.w("TAG", "THIS CALL BACK IS CALLED" + status + "---" + newState + "---");
            switch (newState) {
                case NOT_CONNECTED:
                    updateUI("NOT CONNECTED", 1);
                    updateUI("[Server]"+mDevice.getAddress()+" connection state : NOT CONNECTED" ,4);

                    break;
                case CONNECTING:
                    updateUI(" CONNECTING", 1);
                    updateUI("[Server]"+mDevice.getAddress()+" connection state : CONNECTING" ,4);
                    break;
                case CONNECTED:
                    updateUI(" CONNECTED", 1);
                    mbluetoothGatt.discoverServices();
                    updateUI("[Server]"+mDevice.getAddress()+" connection state : CONNECTED" ,4);
                    break;

                default:
                    updateUI(" ERROR SOME WHERE", 1);
                    updateUI("[Server]"+mDevice.getAddress()+" connection state : ERROR SOME WHERE" ,4);
                    break;

            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.w("TAG", "SERV DISCOVER  CALL BACK IS CALLED" + status + "---");
            updateUI("gatt.ServiceDiscovered()" ,4);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateUI("ServiceDiscovered with status 0" ,4);
                displayBleService(gatt.getServices());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            super.onCharacteristicRead(gatt, characteristic, status);
            Log.w("TAG", "OOOOOOO1--READ");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.w("TAG", "OOOOOOO2--DESCRI-WRITE");
            Log.w("TAG", "OOOOOOO2--DESCRI-WRITE--------->" + characteristic.getUuid());
            updateUI("OnCharacterWrite :"+characteristic.getUuid() ,4);
            //broadcastUpdate(characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.w("TAG", "OOOOOOO3-CHAR-change--------->" + characteristic.getUuid());

            broadcastUpdate(characteristic);
            Log.w("TAG", "CALLED ENABLE NOTIFICATION ");
           // enableNotification(characteristic, true);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.w("TAG", "OOOOOOO4");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.w("TAG", "OOOOOOO5--ON WRITE DES CALLBACK");
        }
    };


    // BroadCastUpdate ..
    public void broadcastUpdate(BluetoothGattCharacteristic characteristic) {

        if (DLE_NOTIFY.equals(characteristic.getUuid())) {


            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                Log.w("TAG", "DATA==" + new String(data) + "\n" + stringBuilder.toString());

                updateUI(new String(data) + "\n" + stringBuilder.toString(), 2);
                updateUI("Notified On :"+characteristic.getUuid() ,4);
                updateUI("(recieved)"+new String(data) + "\n" + stringBuilder.toString() ,4);
            }


        }

    }

    /**
     *

     * All Click Events Are Registered Here .. !
     */

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.connect:
                Log.w("TAG", "DEVICE=ADDR=" + mDeviceAddress);
                connectBLE(mDeviceAddress);
                break;
            case R.id.dis_connect:
                Log.w("TAG", "DISCONNECT CALLED");
                disConnectBLE();
                break;
            case R.id.WRITE:

                if (query_data == null) {
                    Log.w("TAG", "ERROR NOTHING");
                }
                writeCharacteristics(query_data);
                break;
            case R.id.canConfig:

                // Pop Up TO Enter Tx and Rx value ..

                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);

                // Add a TextView here for the "Title" label, as noted in the comments
                final EditText Tx = new EditText(this);
                Tx.setHint("Format : 0x7DE");
                layout.addView(Tx); // Notice this is an add method

                // Add a TextView here for the "Title" label, as noted in the comments
                final EditText Rx = new EditText(this);
                Rx.setHint("Format : 0x8EF");
                layout.addView(Rx);



                AlertDialog.Builder canPopUp = new AlertDialog.Builder(this);
                canPopUp.setMessage("CAN Tx & Rx :");
                canPopUp.setTitle("Please Enter Tx and Rx conf In UpperCase");
                canPopUp.setView(layout);

                canPopUp.setPositiveButton("write", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String conf_Tx = Tx.getText().toString();
                        byte[] d = conf_Tx.getBytes();
                        Log.w("TAG","D"+d[0]);
                        String conf_Rx = Rx.getText().toString();
                        byte[] r = conf_Rx.getBytes();
                        Log.w("TAG","D"+r[0]);
                        byte[] conf_data = {0x00,0x0B,0x11,0x11,0x00,d[0],r[0]};

                        writeCharacteristics(conf_data);
                        Toast.makeText(DeviceScanDetails.this,"CCC"+conf_data.length,Toast.LENGTH_LONG).show();
                    }
                });
                canPopUp.show();

                break;
        }

    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan_details);

        queryLoader = new QueryLoader();

        DEVICE_NAME = (TextView) findViewById(R.id.device_addr);
        CONNECTION_STATE = (TextView) findViewById(R.id.connection_state);
        Display = (TextView) findViewById(R.id.display_data);
        connect = (Button) findViewById(R.id.connect);
        dis_connect = (Button) findViewById(R.id.dis_connect);
        WRITE = (Button) findViewById(R.id.WRITE);
        canConfig =(Button)findViewById(R.id.canConfig);

        connect.setOnClickListener(this);
        dis_connect.setOnClickListener(this);
        WRITE.setOnClickListener(this);
        canConfig.setOnClickListener(this);

        // Code For Spinner .. !
        spinner_query = (Spinner) findViewById(R.id.spinner_query);


        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra("data");
        mDeviceAddress = intent.getStringExtra("data");


        DEVICE_NAME.setText(mDeviceName);






        //THIS IS A DYNAMIC LINEAR LAYOUT ..FOR APPLICATION LOG .. !
        test = new TextView(DeviceScanDetails.this);
        Logger = (LinearLayout) findViewById(R.id.Logger);
        Logger.addView(test);

        log = (ListView) findViewById(R.id.Log);





        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, query_name);
        spinner_query.setAdapter(arrayAdapter);
        spinner_query.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(DeviceScanDetails.this, "SELECTED :" + query_name[i].toString(), Toast.LENGTH_LONG).show();

                WRITE.setVisibility(View.INVISIBLE);
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(DeviceScanDetails.this);
                alertDialog.setTitle("NAME :" + query_name[i]);
                alertDialog.setMessage("Query :" + queryLoader.getQueryMsg(query_name[i]));
                alertDialog.setPositiveButton("OK", null);
                alertDialog.setNegativeButton("CANCEL", null);


                alertDialog.show();

                // Acessing DATA FROM QueryLoader Class ..
                query_data = queryLoader.getQuery(query_name[i]);
                if (query_data != null) {
                    WRITE.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(DeviceScanDetails.this, "YOU HAVE NOT SELECTED ANY THING", Toast.LENGTH_LONG).show();
                }

                Log.w("TAG", "DATA FROM QUERY LOADER" + queryLoader.getQuery(query_name[i].toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(DeviceScanDetails.this, "YOU HAVE SELECTED NOTHING", Toast.LENGTH_LONG).show();
            }
        });





    }


    @Override
    protected void onResume() {
        super.onResume();
        connectBLE(mDeviceAddress);
    }

    // Function To Write Characteristics .. !
    public void writeCharacteristics(byte[] data) {
        BluetoothGattService Service = mbluetoothGatt.getService(DLE_SERVICE);
        if (Service == null) {
            Log.w("TAG", "service not found!");
            //return false;
        }


        BluetoothGattCharacteristic charc1 = Service.getCharacteristic(DLE_WRITE);
        if (charc1 == null) {
            Log.w("TAG", "char not found!");
            Log.w("TAG", "CHARAC_-TRST" + charc1.getUuid());
            // return false;
        }
        // charc1.setValue(new byte[]{0x00, 0x05, 0x10, 0x01, 0x3E, 0x01, 0x23});

        charc1.setValue(data);
        boolean stat = mbluetoothGatt.writeCharacteristic(charc1);
        updateUI("Write Characteristic :"+charc1.getUuid() + "--"+stat ,4);
        Log.w("TAG", "FINISHED WRITTING CHAR 1 status write :(status)" + stat);

        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));

            updateUI("(sent):"+new String(data) + "\n" + stringBuilder.toString() ,4);
        }


    }


    //Update The UI ..
    public void updateUI(final String info, final int i) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                switch (i) {
                    case 1:
                        // Update Connection state ..!
                        CONNECTION_STATE.setText(info);
                        break;
                    case 2:
                        //Update Date Recieve;
                        Display.setText(info);
                        break;
                    case 3:
                        //Update Date Recieve;
                        test.setText(info);
                        break;
                    case 4:
                        Date d=new Date();
                        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss");

                        Logs.add(sdf.format(d)+"::"+info);
                        ArrayAdapter<String> log_Debug = new  ArrayAdapter<String> (DeviceScanDetails.this,R.layout.support_simple_spinner_dropdown_item,Logs);
                        log.setAdapter(log_Debug);
                        break;

                }


            }
        });

    }

    // Connect Bluetooth .. !
    public void connectBLE(String device_ADDR) {

        BLUTOOTH_ADAPTER_ENABLED = initBluetoothAdapter();
        if (BLUTOOTH_ADAPTER_ENABLED && device_ADDR != null) {
            mDevice = mbluetoothAdapter.getRemoteDevice(device_ADDR);
            Log.w("TAG", "DEVICE==" + mDevice.getName());
            updateUI("DEVICE ::" +  mDevice.getName(),4);

            mbluetoothGatt = mDevice.connectGatt(DeviceScanDetails.this, false, mBluetoothGattCallBack);
            // creating a Bond
            //  mDevice.setPin(new byte[]{(byte)123456});
            boolean bondState = mDevice.createBond();
            //Logs ..
            updateUI("[Server]Device with address"+mDevice.getAddress()+" connected",4);
            Log.w("TAG", "COONCETION _ STATE ==" + bondState);
            // CON_STATE= mBluetoothManager.getConnectionState(device,0);

//            Log.w("TAG","COONCETION _ STATE =="+mbluetoothGatt.getConnectionState(device));
        }
    }


    public void disConnectBLE() {
        mbluetoothGatt.disconnect();
    }


    // INITIALIZE BLUETOOTH ADAPTER
    public boolean initBluetoothAdapter() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.w("TAG", "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mbluetoothAdapter = mBluetoothManager.getAdapter();
        if (mbluetoothAdapter == null) {
            Log.w("TAG", "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    // DISPLAY SERVICES AND CHARACTERISTICS WITH PROPPERTIES..
    public void displayBleService(List<BluetoothGattService> gattServices) {

        List<BluetoothGattCharacteristic> characteristics = new ArrayList<>();
        for (BluetoothGattService services : gattServices) {
            Log.w("TAG", "SERVICES == ." + services.getUuid());
            updateUI("(Service) :"+services.getUuid() ,4);
            characteristics = services.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                Log.w("TAG", "CAHRACTERISTICS  == ." + characteristic.getUuid());
                int charPro = characteristic.getProperties();
                //Logs ..
                updateUI("(Characteristics) :" + characteristic.getUuid(),4);

                enableNotification(characteristic, true);

            }
        }

    }


    //Enable Notifiction ..
    public void enableNotification(BluetoothGattCharacteristic characteristic, boolean enable) {

        final int charaProp = characteristic.getProperties();


        //FOR SPECIFIC CHARACTETISTIC .. !

        List<BluetoothGattDescriptor> bluetoothGattDescriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor1 : bluetoothGattDescriptors) {
            Log.w("TAG", "DESCRIPTOR==" + descriptor1.getUuid());
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CCD);


        // CHAR ONLY TO NOTIFY ..
        if (DLE_NOTIFY.equals(characteristic.getUuid())) {
            Log.w("TAG", "UUID--READ==" + characteristic.getUuid() + "PROP=" + characteristic.getProperties());
            //BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CCD);
            Log.w("TAG", "descriptor==" + descriptor.getUuid());
            mbluetoothGatt.setCharacteristicNotification(characteristic, true);
            descriptor.setValue(true ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            //descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            boolean result = mbluetoothGatt.writeDescriptor(descriptor);
//            test.setText("setNotification Status :"+characteristic.getUuid()+"--> Status :"+result);
            updateUI("setNotification Status :"+characteristic.getUuid()+"--> Status :"+result,4);

            Log.w(" TAG", "DLE_NOTIFY DES " + result);
        }




    }
}
