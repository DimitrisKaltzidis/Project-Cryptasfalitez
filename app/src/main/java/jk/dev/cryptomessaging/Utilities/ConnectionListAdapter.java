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
    private ArrayList<TrustedDevice> trustedDevices;

    public ConnectionListAdapter(Activity context, ArrayList<BluetoothDevice> bluetoothDevices, ArrayList<TrustedDevice> trustedDevices) {
        super(context, R.layout.row, bluetoothDevices);

        this.context = context;
        this.bluetoothDevices = bluetoothDevices;
        this.trustedDevices = trustedDevices;
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

        BluetoothDevice current = bluetoothDevices.get(position);
        boolean trusted = false;
        for (TrustedDevice trustedDevice : trustedDevices) {
            if (trustedDevice.getMacAddress().equals(current.getAddress())) {
                trusted = true;
                break;
            }
        }

        holder.tvName.setText(current.getName());
        if(trusted){
            holder.tvDescription.setText(R.string.secure);
        }else {
           /* holder.tvDescription.setText(bluetoothDevices.get(position).getAddress());*/
            if(current.getBondState()==BluetoothDevice.BOND_BONDED){
                holder.tvDescription.setText(R.string.not_secure);
            }else
                holder.tvDescription.setText(R.string.not_paired);

        }
        return convertView;

    }

    public void addBluetoothDevice(BluetoothDevice bluetoothDevice) {
        bluetoothDevices.add(bluetoothDevice);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        private TextView tvName, tvDescription;
    }
}
