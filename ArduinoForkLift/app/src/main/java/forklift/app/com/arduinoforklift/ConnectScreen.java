package forklift.app.com.arduinoforklift;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConnectScreen extends AppCompatActivity{

    BluetoothAdapter adapterBT;
    ArrayList<BluetoothDevice> bondedDevicesList;
    boolean isEqual;
    ListView deviceList;
    deviceListAdapter listAdapter;
    final int REQUEST_CODE_LOC = 1;

    private void AddDeviceToList(BluetoothDevice device){
        String devName = "";
        if(bondedDevicesList == null){
            bondedDevicesList = new ArrayList<>();
        }
        bondedDevicesList.add(device);
        setAdapter();
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                isEqual = false;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                for (int i = 0; i < bondedDevicesList.size() && !(isEqual); i++) {
                    if (device.getAddress().equals(bondedDevicesList.get(i).getAddress())) {
                        isEqual = true;
                    }
                }
                if (!isEqual) {
                    AddDeviceToList(device);
                }
                String name = device.getName();
                String address = device.getAddress();
                Log.i("Receiver", "[" + name + ", " + address + "] ");
            }else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                //finished Discovery
            }
        }
    };

    public void setAdapter(){
        List<BluetoothDevice> BTDevices = bondedDevicesList.subList(0, bondedDevicesList.size());
        listAdapter = new deviceListAdapter(getApplicationContext(), R.layout.devicelistitem, bondedDevicesList, adapterBT);
        deviceList.setAdapter(listAdapter);
    }

    @Override
    protected void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.connectscreen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        adapterBT = BluetoothAdapter.getDefaultAdapter();
        if(!adapterBT.isEnabled()){
            Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enable, 1);
            while (!adapterBT.isEnabled()) {
                //DO NOTHING
            }
        }
        deviceList = (ListView) findViewById(R.id.devList);
        accessLocationPermission();
        Set<BluetoothDevice> pairedDevices = adapterBT.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() == 0) {
            //tv.setText("NESSUN DISPOSITIVO ACCOPPIATO");
        } else {
            bondedDevicesList = new ArrayList<BluetoothDevice>();
            for(BluetoothDevice device : pairedDevices) {
                AddDeviceToList(device);
            }
            setAdapter();
        }
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        adapterBT.startDiscovery();
    }


    public class deviceListAdapter extends ArrayAdapter<BluetoothDevice>{

        private BluetoothAdapter BTAdapter;

        public deviceListAdapter(Context context, int textViewResourceId, List<BluetoothDevice> devices, BluetoothAdapter BTAdapter){
            super(context, textViewResourceId, devices);
            this.BTAdapter = BTAdapter;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.devicelistitem, null);
            TextView deviceNameText = (TextView) convertView.findViewById(R.id.deviceNameText);
            TextView deviceAddressText = (TextView) convertView.findViewById(R.id.deviceAddressText);
            deviceNameText.setTextColor(Color.BLACK);
            deviceAddressText.setTextColor(Color.BLACK);
            RelativeLayout itemLayout = (RelativeLayout) convertView.findViewById(R.id.item);
            deviceAddressText.setTextSize(17f);
            deviceNameText.setTextSize(13f);
            final BluetoothDevice BTDevice = getItem(position);
            deviceNameText.setText(BTDevice.getName());
            deviceAddressText.setText(BTDevice.getAddress());
            itemLayout.setClickable(true);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Connect to device
                    Intent i = new Intent(ConnectScreen.this, CommandScreen.class);
                    i.putExtra("MacAddress", BTDevice.getAddress());
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    unregisterReceiver(receiver);
                    startActivity(i);
                }
            });
            return convertView;
        }
    }



    private void accessLocationPermission() {
        int accessCoarseLocation, accessFineLocation;
        if (Build.VERSION.SDK_INT >= 23) {
            accessCoarseLocation = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            accessFineLocation = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            List<String> listRequestPermission = new ArrayList<String>();

            if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
                listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (!listRequestPermission.isEmpty()) {
                String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
                requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOC:
                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                }
                break;
            default:
                return;
        }
    }

}