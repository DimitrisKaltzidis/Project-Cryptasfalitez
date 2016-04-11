package jk.dev.cryptomessaging;

import android.bluetooth.BluetoothDevice;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import jk.dev.cryptomessaging.Utilities.ConnectionListAdapter;

public class Connection extends AppCompatActivity {

    private ListView lvPairedDevices;
    private ArrayList<BluetoothDevice> bluetoothDevices = null;
    ConnectionListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        lvPairedDevices = (ListView) findViewById(R.id.lvPairedDevices);


        adapter = new ConnectionListAdapter(this,bluetoothDevices);

        lvPairedDevices.setAdapter(adapter);

        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

    }



}
