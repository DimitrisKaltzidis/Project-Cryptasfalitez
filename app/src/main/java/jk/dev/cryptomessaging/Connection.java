package jk.dev.cryptomessaging;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import jk.dev.cryptomessaging.Utilities.Bluetooth;
import jk.dev.cryptomessaging.Utilities.ConnectionListAdapter;
import jk.dev.cryptomessaging.Utilities.Preferences;
import jk.dev.cryptomessaging.Utilities.TrustedDevice;

public class Connection extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "jk.dev.cryptomessaging";
    private ArrayList<BluetoothDevice> bluetoothDevices = null, allBluetoothDevices = null;
    private ArrayList<TrustedDevice> trustedDevices = null;
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

        List<TrustedDevice> trustedDevicesList = getTrustedDeviceListFromPreferences();//getting trusted devices

        if (trustedDevicesList == null) {
            trustedDevices = new ArrayList<>();
        } else
            trustedDevices = new ArrayList<>(trustedDevicesList);

        fabVisibility = (FloatingActionButton) findViewById(R.id.fabVisibility);
        adapter = new ConnectionListAdapter(this, bluetoothDevices, trustedDevices);

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

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice device = bluetoothDevices.get(i);
                TrustedDevice trustedDevice = null;
                for (TrustedDevice currentTrustedDevice : trustedDevices) {
                    if (currentTrustedDevice.getMacAddress().equals(device.getAddress())) {
                        trustedDevice = currentTrustedDevice;
                        break;
                    }
                }
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    pairDevice(device);
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    showChatPrompt(device, trustedDevice);
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
                TrustedDevice trustedDevice = null;
                for (TrustedDevice currentTrustedDevice : trustedDevices) {
                    if (currentTrustedDevice.getMacAddress().equals(device.getAddress())) {
                        trustedDevice = currentTrustedDevice;
                        break;
                    }
                }
                chatRequestPrompt(device, trustedDevice);

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

    private void showChatPrompt(final BluetoothDevice device, final TrustedDevice trustedDevice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Connection.this);
        builder.setTitle(getString(R.string.chat));
        if (trustedDevice != null)
            builder.setMessage(getString(R.string.chat_with) + device.getName());
        else
            builder.setMessage(getString(R.string.chat_with) + device.getName() + getString(R.string.not_secure_connection));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
                Intent intent = new Intent(Connection.this, Chatroom.class);
                intent.putExtra("DeviceName", device.getName());
                intent.putExtra("DevicePublicKey", trustedDevice.getPublicKey());
                Log.d(TAG, "showChatPrompt: showChatPrompt " + device.getName());

                startActivity(intent);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        if (trustedDevice != null)
            builder.setNeutralButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Connection.this, Settings.class);
                    startActivity(intent);
                }
            });
        else
            builder.setNeutralButton(getString(R.string.enter_key), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enterTrustDeviceKeyPrompt(device);
                }
            });
        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void enterTrustDeviceKeyPrompt(final BluetoothDevice bluetoothDevice) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter other user public key");
        // Set up the input
        final EditText input = new EditText(this);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String key = input.getText().toString();
                TrustedDevice trustedDevice = new TrustedDevice(bluetoothDevice.getAddress(), bluetoothDevice.getName(), key);
                List<TrustedDevice> tempTrustedDevices = getTrustedDeviceListFromPreferences();
                tempTrustedDevices.add(trustedDevice);
                saveTrustedDeviceListToPreferences(tempTrustedDevices);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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

    private void chatRequestPrompt(final BluetoothDevice bluetoothDevice, final TrustedDevice trustedDevice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Connection.this);
        builder.setTitle("Chat request");
        if (trustedDevice == null) {
            builder.setMessage(bluetoothDevice.getName() + getString(R.string.enter_key_to_secure));

            builder.setNeutralButton(getString(R.string.enter_key), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enterTrustDeviceKeyPrompt(bluetoothDevice);
                }
            });
        } else {
            builder.setMessage(bluetoothDevice.getName() + getString(R.string.secured));
        }
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent intentOpen = new Intent(Connection.this, Chatroom.class);
                intentOpen.putExtra("btdevice", bluetoothDevice);
                try {
                    intentOpen.putExtra("DevicePublicKey", trustedDevice.getPublicKey());
                } catch (Exception r) {
                    r.printStackTrace();
                }

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

    private List<TrustedDevice> getTrustedDeviceListFromPreferences() {
        final String TRUSTED_DEVICES = "trusted_devices";
        String connectionsJSONString = PreferenceManager.getDefaultSharedPreferences(this).getString(TRUSTED_DEVICES, null);
        Type type = new TypeToken<List<TrustedDevice>>() {
        }.getType();
        List<TrustedDevice> achievements = new Gson().fromJson(connectionsJSONString, type);

        return achievements;
    }


    private void saveTrustedDeviceListToPreferences(List<TrustedDevice> achievementListToSave) {
        final String TRUSTED_DEVICES = "trusted_devices";
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        String connectionsJSONString = new Gson().toJson(achievementListToSave);
        editor.putString(TRUSTED_DEVICES, connectionsJSONString);
        editor.commit();
    }

    public void fabSharePublicWithBluetooth(View view) {
        enterEmailAddressPrompt();
    }

    private void enterEmailAddressPrompt() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter other user email address");
        // Set up the input
        final EditText input = new EditText(this);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String address = input.getText().toString();
                sendEmail(address);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void sendEmail(String emailAddress) {
        String publicKey = Preferences.loadPrefsString("PUBLIC_KEY", "NO_KEY", getApplicationContext());
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        i.putExtra(Intent.EXTRA_SUBJECT, "CRYPTO ASFALITEZ PUBLIC KEY ");
        i.putExtra(Intent.EXTRA_TEXT, publicKey);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Connection.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:
                startActivity(new Intent(Connection.this, Settings.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:
                startActivity(new Intent(Connection.this, Settings.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
