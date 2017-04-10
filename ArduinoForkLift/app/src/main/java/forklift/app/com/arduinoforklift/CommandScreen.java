package forklift.app.com.arduinoforklift;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Orientamento 2 on 08/04/2017.
 */

public class CommandScreen extends AppCompatActivity {

    public BluetoothAdapter BTAdapter;
    private String Devicemac;
    private BluetoothDevice BTDevice;
    private BluetoothSocket bluetoothSocket;
    ProgressDialog PD;
    ImageButton Avanti, Indietro, AvantiDestra, AvantiSinistra, IndietroDestra, IndietroSinistra, Disconnetti;
    sendChar sndChar;

    connectTask task;

    @Override
    protected  void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.command_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Avanti = (ImageButton) findViewById(R.id.Avanti);
        Indietro = (ImageButton) findViewById(R.id.Indietro);
        AvantiDestra = (ImageButton) findViewById(R.id.AvantiDestra);
        AvantiSinistra = (ImageButton) findViewById(R.id.AvantiSinistra);
        IndietroDestra = (ImageButton) findViewById( R.id.IndietroDestra);
        IndietroSinistra = (ImageButton) findViewById(R.id.IndietroSinistra);
        Disconnetti = (ImageButton) findViewById(R.id.Disconnetti) ;
        Avanti.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.freccia_avanti), 96, 96, false));
        Indietro.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.freccia_dietro), 96, 96, false));
        AvantiDestra.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.freccia_destra_avanti), 96, 96, false));
        AvantiSinistra.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.freccia_sinistra_avanti), 96, 96, false));
        IndietroDestra.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.freccia_destra_dietro), 96, 96, false));
        IndietroSinistra.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.freccia_sinistra_dietro), 96, 96, false));
        Disconnetti.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo_arduino), 96, 96, false));
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        Devicemac = getIntent().getStringExtra("MacAddress");
        BTDevice = BTAdapter.getRemoteDevice(Devicemac);
        task = new connectTask(BTDevice, BTAdapter);
        task.execute();
        Disconnetti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });
        ImageButton.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    //button
                    int id = v.getId();
                    String toBeSent = "";
                    switch(id){
                        case R.id.Avanti:
                            toBeSent = "A";
                            break;
                        case R.id.AvantiDestra:
                            toBeSent = "C";
                            break;
                        case R.id.AvantiSinistra:
                            toBeSent = "D";
                            break;
                        case R.id.Indietro:
                            toBeSent = "B";
                            break;
                        case R.id.IndietroDestra:
                            toBeSent = "E";
                            break;
                        case R.id.IndietroSinistra:
                            toBeSent = "F";
                            break;
                    }
                    sndChar = new sendChar(true, task.getBTSocket());
                    sndChar.setToBeSent(toBeSent);
                    sndChar.start();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    if(sndChar.isAlive()){
                        sndChar.setLoop(false);
                    }
                }
                return false;
            }
        };
        Avanti.setOnTouchListener(listener);
        AvantiSinistra.setOnTouchListener(listener);
        AvantiDestra.setOnTouchListener(listener);
        Indietro.setOnTouchListener(listener);
        IndietroSinistra.setOnTouchListener(listener);
        IndietroDestra.setOnTouchListener(listener);
        bluetoothSocket = task.getBTSocket();
    }

    private void sendCommand(String command, BluetoothSocket socket){
        byte[] byteCommand = command.getBytes();
        try{
            OutputStream stream = socket.getOutputStream();
            stream.write(byteCommand);
            Log.i("BluetoothSend", "Sent " + command);
        }catch(IOException e){
            Log.e("BluetoothSend", "IOException: " + e);
            e.printStackTrace();
        }
    }

    private void onProgressDialogShow(String DeviceName, Context context){
        PD = new ProgressDialog(context);
        PD.setCancelable(false);
        PD.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        PD.setMessage("Connecting to " + DeviceName + ": Attempt 1 of 3");
        PD.show();
    }

    private void onProgressDialogUpdate(int Attempts, String DeviceName){
        PD.setMessage("Trying to connect to " + DeviceName + ": Attempt " + Attempts + " of 3");
    }

    private class connectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothDevice BTDevice;
        private BluetoothAdapter BTAdapter;
        private BluetoothSocket BTSocket;
        private Object lock = new Object();
        int attempts;


        public connectTask(BluetoothDevice BTDevice, BluetoothAdapter BTAdapter){
            this.BTDevice = BTDevice;
            this.BTAdapter = BTAdapter;
        }

        @Override
        public void onPreExecute(){
            super.onPreExecute();
            onProgressDialogShow(BTDevice.getName(), CommandScreen.this);
        }

        public Boolean doInBackground(Void... params){
            //connect to the device
            BTAdapter.cancelDiscovery();
            attempts = 0;
            boolean done = false;
            do{
                BluetoothSocket temp = null;
                try{
                    temp = BTDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                }catch(Exception e){
                    e.printStackTrace();
                }
                BTSocket = temp;
                try{
                    BTSocket.connect();
                    Log.i("Connect AsyncTask", "Connected");
                    done = true;
                }catch(IOException e){
                    //Connection error
                    Log.e("Connect AsyncTask", "java.io.IOException: Unable to connect", e);
                    attempts += 1;
                    if(attempts < 3){
                        //UpdateDialog
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onProgressDialogUpdate(attempts + 1, BTDevice.getName());
                            }
                        });
                    }
                    try{
                        BTSocket.close();
                    }catch(IOException ex){
                        Log.e("Connect AsyncTask", "Impossibile close BluetoothSocket", ex);
                    }
                }
                try {
                    synchronized (lock){
                        lock.wait(1000);
                    }
                } catch (InterruptedException e) {
                }
                if (attempts == 3) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CommandScreen.this, "Unable to connect to " + BTDevice.getName() + ": the device did not respond within 5 seconds", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }
            }while(attempts <3 && !done);
            return done;
        }

        @Override
        public void onPostExecute(Boolean result){
            if(PD.isShowing()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PD.dismiss();
                    }
                });
            }
            if(result){
                Toast.makeText(CommandScreen.this, "Connected to " + BTDevice.getName(), Toast.LENGTH_SHORT).show();
                super.onPostExecute(result);
            }
        }

        public BluetoothSocket getBTSocket(){
            return this.BTSocket;
        }
    }

    public class sendChar extends Thread{

        String toBeSent;
        boolean Loop;
        BluetoothSocket socket;

        public sendChar( boolean Loop, BluetoothSocket socket){
            this.Loop = Loop;
            this.socket = socket;
        }

        public void run(){
            while(Loop){
                try{
                    Thread.sleep(35);
                }catch(InterruptedException e){}
                sendCommand(toBeSent, socket);
            }
        }

        public void setLoop(boolean loop){
            this.Loop = loop;
        }

        public void setToBeSent(String toBeSent){
            this.toBeSent = toBeSent;
        }

    }

    public void disconnect(){
        try{
            task.getBTSocket().close();
            finish();
        }catch(IOException e){
            Log.e("Disconnect task", "IOException. Unable to close stream " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed(){
        disconnect();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        disconnect();
    }
}
