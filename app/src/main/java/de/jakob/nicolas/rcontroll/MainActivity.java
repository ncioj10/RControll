package de.jakob.nicolas.rcontroll;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    public BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new BluetoothFragment())
                    .commit();

            bt = new BluetoothSPP(this);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class BluetoothFragment extends Fragment {

        private static final String LOG_BT_FRAG = "Button Fragment";
        public BluetoothSPP bt;
        EditText editText;
        Button btnSend;
        Intent lastConnected;


        public BluetoothFragment() {


        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Button buttonConnect = (Button) rootView.findViewById(R.id.button_connect);
            bt = new BluetoothSPP(getActivity());



            editText = (EditText) rootView.findViewById(R.id.editText);
            buttonConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                        bt.disconnect();
                    } else {
                        Intent intent = new Intent(getActivity().getApplicationContext(), DeviceList.class);
                        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                    }
                }
            });

            bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                public void onDataReceived(byte[] data, String message) {
                    Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                }
            });

            bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                public void onDeviceConnected(String name, String address) {
                    Toast.makeText(getActivity().getApplicationContext()
                            , "Connected to " + name + "\n" + address
                            , Toast.LENGTH_SHORT).show();
                }

                public void onDeviceDisconnected() {
                    Toast.makeText(getActivity().getApplicationContext()
                            , "Connection lost", Toast.LENGTH_SHORT).show();
                }

                public void onDeviceConnectionFailed() {
                    Toast.makeText(getActivity().getApplicationContext()
                            , "Unable to connect", Toast.LENGTH_SHORT).show();
                }
            });


            return rootView;


        }

        public void onStart() {
            super.onStart();
            if (!bt.isBluetoothEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
            } else {
                if (!bt.isServiceAvailable()) {
                    bt.setupService();
                    bt.startService(BluetoothState.DEVICE_OTHER);
                    setup();
                }
            }


        }

        public void onResume(){
            super.onResume();


        }

        public void onPause(){
            super.onPause();
            bt.stopService();

        }


        public void setup() {
            btnSend = (Button) getActivity().findViewById(R.id.button_send);
            btnSend.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    bt.send(editText.getText().toString(), true);
                    Log.e("ButtonSEND", "BUTTON SEND PRESSED" + editText.getText().toString());
                }
            });

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent data) {

            if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (bt != null && data != null) {
                        bt.connect(data);
                        lastConnected = data;
                    }


                } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
                    if (resultCode == Activity.RESULT_OK) {
                        bt.setupService();
                        bt.startService(BluetoothState.DEVICE_OTHER);

                    } else {
                        Toast.makeText(getActivity().getApplicationContext()
                                , "Bluetooth was not enabled."
                                , Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }
            }
        }



    }
}
