package jk.dev.cryptomessaging;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
            }
        });

        bt = new Bluetooth(this, mHandler);

        if (deviceName != null) {
            connectService(deviceName);
        } else if (bluetoothDevice != null) {
            bt.connect(bluetoothDevice);
        }
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
                    int lastChar = strIncom.length()-1;
                    Log.d(TAG, "MESSAGE_READ "+strIncom);
                    sb.append(strIncom);
                    int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                    Message user1 = new Message("user1", strIncom, false);
                    listMessages.add(user1);
                    adapter.notifyDataSetChanged();
                    if (endOfLineIndex > 0) {                                            // if end-of-line,
                        String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                        sb.delete(0, sb.length());                                      // and clear
                        Log.d("READ_FROM_ANDROID", sbprint);
                        //Message temp_msg = new Message("user2", sbprint, false);

                       // adapter.addMessage(temp_msg);
                        playSound();
                        // tvDistance.setText(sbprint + "cm");
                    }
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
        super.onPause();
        bt.stop();
        finish();
    }
}
