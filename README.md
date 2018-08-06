# BLE SCAN

![Bluetooth BLE](https://www.universfreebox.com/UserFiles/image/Bluetoothandroid(2).png)

<p>
Android 4.3 (API level 18) introduces built-in platform support for Bluetooth Low Energy (BLE) in the central role and provides APIs that apps can use to discover devices, query for services, and transmit information.

Common use cases include the following:

<ol>
<li>Transferring small amounts of data between nearby devices.</li>
<li>Interacting with proximity sensors like Google Beacons to give users a customized experience based on their current location.
In contrast to Classic Bluetooth, Bluetooth Low Energy (BLE) is designed to provide significantly lower power consumption. This allows Android apps to communicate with BLE devices that have stricter power requirements, such as proximity sensors, heart rate monitors, and fitness devices.
</li>
<ol>
</p>

<p> For more information On BLE android please visit this link to understand the BLE terms Like GATT ,Service ,etc</p>
<ul>
<li>https://developer.android.com/guide/topics/connectivity/bluetooth-le</li>
</ul>

## Bluetooth Low Energy(BLE) Concept: 
<p>
BLE is based on a specification called “General ATTribute profile” (GATT),
which defines how to transfer and receive short pieces of data known as “attributes” between a server and a client.
</p>

![alt text](http://nilhcem.com/public/images/20170502/01_gatt-diagram.png)

<p>
Directly diving in to the topic while cimmunicatting with a BLE device we will work
with the Services a lot which is inside the Profile . A Profile can have multiple services
and a service can have mutliple charactetistics.

Therefore, to communicate with the BLE we need to discover the services and the characteristics 
on which we can perform write,Readl,notify and indicate .

Each charcateristics has property READ,WRTIE,NOTIFY and INDICATE which you will find on the specification of the 
Device.

After , a small description of the BLE now lets enter in to CODE 

</p>

## Getting Started

The Following steps have to be followed to create a BLE Application .
<ol>
<li>Permission in Manifest</li>
<li>Check Runtime permission for ACESS_FINE_LOCATION</li>
<li>Initilize the BLuetoothCallback </li>
<li>Start The Scan(Discover the Devices)</li>
<li>Connect To The Device</li>
<li>Discover Service and Characteristics BluetoothGatt Callbacks</li>
<li>Activate All notifications before you perform other operations</li>
<li>Once setNotifications Enabled Perform Other perfomance(Read,Write,notify)</li>
<li>Disconnect Once you are done</li>
</ol>

## <li>Permission in Manifest</li>

The Following Permission have to be specified in your manifiest .

```
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <!-- Blutooth Permission -->
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
    
  ```
  
  ## <li>Check Runtime permission for ACESS_FINE_LOCATION</li>
  
  Since , ACCESS_FINE_LOCATION Or ACESS_COARSE_LOCATION  are dangerous so you have to
  check for runtime permission.
  ```
   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
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

  ```
   ## <li>Initialize The BLuetoothCallbacks </li>
```   
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

        // Called on when notification/Indications are Enabled on the characteristics
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

```
  ## <li>Start The Scan(Discover the Devices)</li>
  
  Before you perform scan initialize the Bluetooth Adapter..then call the Scan function ..
   ```
   
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            // IF NOT ENABLED IT MAKES A POPUP TO ENABLE BLUETOOTH
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent  enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            
            // You have  To Initailize the SCan CallBack to Captue All the device Scanned check the MainActivity.java
            
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

```
## <li>Connect To The Device</li>

To connect the Device ..

```
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

```

## <li>Discover Service and Characteristics BluetoothGatt Callbacks</li>

The BLuetooth Callback automatically discovers the service and charactetistics. 
you need to iterte through it to get all service and characteristics.

```
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
```

## <li>Activate All notifications before you perform other operations</li>

Activate the notification on the characteristics and call writedescriptor..please see the 
function enablenotification(Service);
```
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
```

## <li>Once setNotifications Enabled Perform Other perfomance(Read,Write,notify)</li>

Once done you can perform W,R operation . But you need to queue all the operations .

##<li>Disconnect Once you are done</li>
```
  public void disConnectBLE() {
        mbluetoothGatt.disconnect();
    }
    
 
 
 ```
 ## Authors :
 
 Maturi karthik : maturikarthik31@gmail.com
 
 Feel free to contribute to the project.


