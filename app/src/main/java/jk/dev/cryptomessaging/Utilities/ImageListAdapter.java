package jk.dev.cryptomessaging.Utilities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import jk.dev.cryptomessaging.R;

/**
 * Created by Jim on 13/4/2016.
 */
public class ImageListAdapter extends ArrayAdapter {


    private Activity context;
    private ArrayList<Image> images;

    public ImageListAdapter(Activity context, ArrayList<Image> images) {
        super(context, R.layout.image_row, images);
        this.context = context;
        this.images = images;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.image_row, parent, false);
            holder = new ViewHolder();
            holder.ivImageThumb = (ImageView) convertView.findViewById(R.id.ivImageThumb);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.ivImageThumb.setImageBitmap(images.get(position).getThumb());
        holder.tvDescription.setText(images.get(position).getCategory());
        return convertView;

    }

    static class ViewHolder {
        private TextView tvDescription;
        private ImageView ivImageThumb;
    }
}
