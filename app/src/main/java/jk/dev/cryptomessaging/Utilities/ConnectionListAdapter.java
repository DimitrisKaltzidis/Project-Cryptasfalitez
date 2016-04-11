package jk.dev.cryptomessaging.Utilities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import jk.dev.cryptomessaging.R;

/**
 * Created by helix on 4/11/16.
 */
public class ConnectionListAdapter extends ArrayAdapter {

    private final Activity context;
    private ArrayList<BluetoothDevice> bluetoothDevices;

    public ConnectionListAdapter(Activity context, ArrayList<BluetoothDevice> bluetoothDevices) {
        super(context, R.layout.row, bluetoothDevices);

        this.context = context;
        this.bluetoothDevices = bluetoothDevices;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row, parent, false);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.tvName.setText(bluetoothDevices.get(position).getName());
        holder.tvDescription.setText(bluetoothDevices.get(position).getAddress());
        return convertView;

    }
    public void addBluetoothDevice (BluetoothDevice bluetoothDevice){
        bluetoothDevices.add(bluetoothDevice);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        private TextView tvName,tvDescription;
    }
}
