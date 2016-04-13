package jk.dev.cryptomessaging;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

import jk.dev.cryptomessaging.Utilities.ConnectionListAdapter;

public class Connection extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "jk.dev.cryptomessaging";
    private ArrayList<BluetoothDevice> bluetoothDevices = null, allBluetoothDevices = null;
    private ConnectionListAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private ProgressDialog progressDialog = null;
    private FloatingActionButton fabVisibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        ListView lvDevices = (ListView) findViewById(R.id.lvPairedDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothDevices = new ArrayList<>();
        allBluetoothDevices = new ArrayList<>();

        fabVisibility = (FloatingActionButton) findViewById(R.id.fabVisibility);
        adapter = new ConnectionListAdapter(this, bluetoothDevices);

        lvDevices.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.scanning_for_devices));
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                bluetoothAdapter.cancelDiscovery();
            }
        });

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = bluetoothDevices.get(i);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    pairDevice(device);
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    showChatPrompt(device);
                } else {
                    showToast(getString(R.string.pair_in_progress));
                }

            }
        });

        lvDevices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                unPairDevicePrompt(bluetoothDevices.get(position));
                return false;
            }
        });

        if (bluetoothAdapter == null) {
            showToast(getString(R.string.no_bt_support));
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {


                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);

            } else {
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(mReceiver, filter);
                IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                registerReceiver(mPairReceiver, intent);

                IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
                IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                this.registerReceiver(afterParingReceiver, filter1);
                this.registerReceiver(afterParingReceiver, filter2);
                this.registerReceiver(afterParingReceiver, filter3);
            }
        }

    }

    private void showToast(String toastMessage) {
        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                progressDialog.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog
                progressDialog.hide();
                //showToast("Searching finished. Found " + bluetoothDevices.size() + " devices.");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!bluetoothDevices.contains(device)) {

                    adapter.addBluetoothDevice(device);
                    allBluetoothDevices.add(device);
                }

            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothDevice.ERROR);
                if (state == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    fabVisibility.setImageDrawable(getResources().getDrawable(R.drawable.invisible));
                }
            }
        }
    };

    private final BroadcastReceiver afterParingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                chatRequestPrompt(device);

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                Toast.makeText(getApplicationContext(), device.getName() + " device disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast(getString(R.string.paired));
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    showToast(getString(R.string.unpaired));
                }

            }
        }
    };

    public void fabSearchForDevicesClicked(View view) {
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
            progressDialog.show();
        }
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(mReceiver);
            unregisterReceiver(mPairReceiver);
            unregisterReceiver(afterParingReceiver);
        } catch (Exception r) {
            r.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
            IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            this.registerReceiver(afterParingReceiver, filter1);
            this.registerReceiver(afterParingReceiver, filter2);
            this.registerReceiver(afterParingReceiver, filter3);
        } catch (Exception r) {
            r.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(afterParingReceiver);
        } catch (Exception r) {
            r.printStackTrace();
        }

    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unPairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showChatPrompt(final BluetoothDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Connection.this);
        builder.setTitle(getString(R.string.chat));
        builder.setMessage(getString(R.string.chat_with) + device.getName());
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
                Intent intent = new Intent(Connection.this, Chatroom.class);
                intent.putExtra("DeviceName", device.getName());
                Log.d(TAG, "showChatPrompt: showChatPrompt " + device.getName());

                startActivity(intent);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Connection.this, Settings.class);
                startActivity(intent);
            }
        });
        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void unPairDevicePrompt(final BluetoothDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Connection.this);
        builder.setTitle(getString(R.string.unpair_device));
        builder.setMessage(getString(R.string.unpair_with) + device.getName());
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                unPairDevice(device);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void chatRequestPrompt(final BluetoothDevice bluetoothDevice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Connection.this);
        builder.setTitle("Chat request");
        builder.setMessage(bluetoothDevice.getName() + " wants to chat with you");
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent intentOpen = new Intent(Connection.this, Chatroom.class);
                intentOpen.putExtra("btdevice", bluetoothDevice);
                Log.d(TAG, "onReceive: BroadcastReceiver " + bluetoothDevice.getName());
                startActivity(intentOpen);
                // Toast.makeText(getApplicationContext(), "INCOMING CONNECTION " + bluetoothDevice.getName(), Toast.LENGTH_LONG).show();

            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void fabToggleBtVisibility(View view) {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            FloatingActionButton fabTemp = (FloatingActionButton) view;
            fabTemp.setImageDrawable(getResources().getDrawable(R.drawable.visible));
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

        } else {
            showToast("Your device is already visible to other devices");
        }
    }


}
