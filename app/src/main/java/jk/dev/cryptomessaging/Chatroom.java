package jk.dev.cryptomessaging;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.base.Charsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jk.dev.cryptomessaging.Utilities.Bluetooth;
import jk.dev.cryptomessaging.Utilities.CategorySorter;
import jk.dev.cryptomessaging.Utilities.Image;
import jk.dev.cryptomessaging.Utilities.ImageListAdapter;
import jk.dev.cryptomessaging.Utilities.Message;
import jk.dev.cryptomessaging.Utilities.MessagesListAdapter;
import jk.dev.cryptomessaging.Utilities.Preferences;

public class Chatroom extends AppCompatActivity {
    private static final String TAG = "jk.dev.cryptomessaging";
    private static final int PICK_IMAGE = 1;

    private Button btnSend;
    private EditText etInputMsg;

    private MessagesListAdapter adapter;
    private List<Message> listMessages;
    private ListView listViewMessages;
    private Bluetooth bt;
    private Context context = this;
    private boolean playSound = false;
    private static StringBuilder sb = new StringBuilder();
    private String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnSend = (Button) findViewById(R.id.btnSend);
        etInputMsg = (EditText) findViewById(R.id.inputMsg);
        listViewMessages = (ListView) findViewById(R.id.list_view_messages);

        listMessages = new ArrayList<Message>();

        if (Preferences.loadPrefsString("PLAY_SOUND", "NO", getApplicationContext()).equals("YES"))
            playSound = true;

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

        try{
            publicKey = getIntent().getExtras().getString("DevicePublicKey");
        }catch (Exception e){
            e.printStackTrace();
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



        if (deviceName != null) { //server
            bt = new Bluetooth(this, mHandler,true);
            connectService(deviceName);
            setTitle(getString(R.string.chatting_with) + deviceName);
        } else if (bluetoothDevice != null) { //client
            bt = new Bluetooth(this, mHandler,false);
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
                    Log.d(TAG, "MESSAGE_READ " + strIncom);
                    sb.append(strIncom);


                    if (strIncom.contains("image")) {
                        String[] parts = strIncom.split("imgSend");
                        Log.d("MESSAGE_RECEIVED", parts[1]);
                        Drawable drawable = new BitmapDrawable(getResources(), StringToBitMap(parts[1]));
                        Message user1 = new Message("user1", strIncom, false, true, drawable);
                        listMessages.add(user1);
                        adapter.notifyDataSetChanged();

                    } else {

                        Message user1 = new Message("user1", strIncom, false);
                        listMessages.add(user1);
                        adapter.notifyDataSetChanged();
                    }
                    if (playSound)
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


    private ArrayList<Image> getPictures() {

        ArrayList<Image> imageList = new ArrayList<>();

        // which image properties are we querying
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA
        };

        // content:// style URI for the "primary" external storage volume
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

       /* // Make the query.
        Cursor cur = managedQuery(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                null        // Ordering
        );*/

        Cursor cur = getContentResolver().query(images, projection, null, null, null);
        //Log.i("ListingImages", " query count=" + cur.getCount());

        if (cur.moveToFirst()) {
            String bucket;
            String date;
            String path;
            int id;
            Bitmap thump;
            int bucketColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            int dateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);

            int image_column_index = cur.getColumnIndex(MediaStore.Images.Media._ID);

            int image_path_index = cur.getColumnIndex(MediaStore.Images.Media.DATA);

            do {
                // Get the field values
                bucket = cur.getString(bucketColumn);
                date = cur.getString(dateColumn);
                id = cur.getInt(image_column_index);
                thump = MediaStore.Images.Thumbnails.getThumbnail(getApplicationContext().getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
                path = cur.getString(image_path_index);
                // Do something with the values.

                imageList.add(new Image(path, bucket, date, thump));
                Log.i("ListingImages", " bucket=" + bucket
                        + "  date_taken=" + date + " path=" + path);
            } while (cur.moveToNext());

            cur.close();
        }
        return imageList;
    }

    public void sendImageClicked(View view) {
        showListAlertDialog(getPictures());
    }

    private void showListAlertDialog(final ArrayList<Image> images) {
        Collections.sort(images, new CategorySorter());
        ImageListAdapter imageListAdapter = new ImageListAdapter(Chatroom.this, images);
        new AlertDialog.Builder(Chatroom.this)
                .setAdapter(imageListAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File imgFile = new File(images.get(which).getPath());
                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            String imageAsString = BitMapToString(myBitmap);
                            imageAsString = "image-imgSend" + imageAsString;
                            Log.d("MESSAGE_SEND", imageAsString);
                            bt.sendMessage(imageAsString);

                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle("Images")
                .setCancelable(false)
                .show();
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

}
