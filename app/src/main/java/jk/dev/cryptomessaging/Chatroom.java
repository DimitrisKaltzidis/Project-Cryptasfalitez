package jk.dev.cryptomessaging;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jk.dev.cryptomessaging.Utilities.Bluetooth;
import jk.dev.cryptomessaging.Utilities.MessagesListAdapter;

public class Chatroom extends AppCompatActivity {
    private static final String TAG = "jk.dev.cryptomessaging";

    private Button btnSend;
    private EditText etInputMsg;

    private MessagesListAdapter adapter;
    private List<Message> listMessages;
    private ListView listViewMessages;
    private Bluetooth bt;

    private static StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        btnSend = (Button) findViewById(R.id.btnSend);
        etInputMsg = (EditText) findViewById(R.id.inputMsg);
        listViewMessages = (ListView) findViewById(R.id.list_view_messages);

        listMessages = new ArrayList<Message>();

        adapter = new MessagesListAdapter(this, listMessages);
        listViewMessages.setAdapter(adapter);
        String deviceName = null;
        BluetoothDevice bluetoothDevice = null;
        try {
            deviceName = getIntent().getExtras().getString("DeviceName");
        } catch (Exception e) {
            e.printStackTrace();
            deviceName = null;
        }
        try {
            bluetoothDevice = getIntent().getParcelableExtra("btdevice");
            Log.d(TAG, "onReceive: CHATROOM INTENT " + bluetoothDevice.getName());
        } catch (Exception e) {
            e.printStackTrace();
            bluetoothDevice = null;
        }
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message user1 = new Message("user1", etInputMsg.getText().toString(), true);
                listMessages.add(user1);
                adapter.notifyDataSetChanged();
                bt.sendMessage(etInputMsg.getText().toString());
                etInputMsg.setText("");
            }
        });

        bt = new Bluetooth(this, mHandler);

        if (deviceName != null) {
            connectService(deviceName);
            setTitle(getString(R.string.chatting_with) + deviceName);
        } else if (bluetoothDevice != null) {
            bt.connect(bluetoothDevice);
            setTitle(getString(R.string.chatting_with) + bluetoothDevice.getName());
        }

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(afterParingReceiver, filter1);
        this.registerReceiver(afterParingReceiver, filter2);
        this.registerReceiver(afterParingReceiver, filter3);
    }

    public void playSound() {
        try {
            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
                    notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectService(String deviceName) {
        try {

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bt.start();
                bt.connectDevice(deviceName);///device name
                Log.d("BLUETOOTH", "Btservice started - listening");

            } else {
                Log.w("BLUETOOTH", "Btservice started - bluetooth is not enabled");

            }
        } catch (Exception e) {
            Log.e("BLUETOOTH", "Unable to start bt ", e);

        }
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case Bluetooth.MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE ");
                    break;
                case Bluetooth.MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;
                    String strIncom = new String(readBuf, 0, msg.arg1);
                    int lastChar = strIncom.length() - 1;
                    Log.d(TAG, "MESSAGE_READ " + strIncom);
                    sb.append(strIncom);
                    int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                    Message user1 = new Message("user1", strIncom, false);
                    listMessages.add(user1);
                    adapter.notifyDataSetChanged();
                    playSound();
                    break;
                case Bluetooth.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MESSAGE_DEVICE_NAME " + msg);
                    break;
                case Bluetooth.MESSAGE_TOAST:
                    Log.d(TAG, "MESSAGE_TOAST " + msg);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onPause() {
        bt.stop();
        finish();
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        bt.stop();
        unregisterReceiver(afterParingReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        bt.stop();
        finish();
        super.onBackPressed();
    }

    private final BroadcastReceiver afterParingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
               /* //Device has disconnected
                Toast.makeText(getApplicationContext(), device.getName() + " device disconnected", Toast.LENGTH_SHORT).show();*/
                chatLeavePrompt(device);
            }
        }
    };

    private void chatLeavePrompt(final BluetoothDevice bluetoothDevice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Chatroom.this);
        builder.setTitle("Warning");
        builder.setMessage(bluetoothDevice.getName() + " left the chat would you like to return to Available devices");
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                finish();
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
}
