package com.quadbac.archeageserverstatus.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quadbac.archeageserverstatus.R;

import java.util.ArrayList;

/**
 * Created by Steve on 02/11/2014.
 */
public class ServerListAdapter extends ArrayAdapter<ServerStatus> {

    private final Context context;
    private final ArrayList<ServerStatus> itemsArrayList;

    public ServerListAdapter(Context context, ArrayList<ServerStatus> itemsArrayList) {

        super(context, R.layout.server_list_item, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.server_list_item, parent, false);

        // 3. Get the views from the rowView
        TextView serverNameView = (TextView) rowView.findViewById(R.id.serverNameView);
        ImageView serverStatusImageView = (ImageView) rowView.findViewById(R.id.serverStatusImageView);
        TextView serverLatencyView = (TextView) rowView.findViewById(R.id.serverLatencyView);
        ImageView notifyImageView = (ImageView) rowView.findViewById(R.id.notifyImageView);
        serverNameView.setWidth(((rowView.getWidth()*60)/100));

        // 4. Set the view contents from the ArrayList
        serverNameView.setText(itemsArrayList.get(position).getName());
        if (itemsArrayList.get(position).getStatus().equalsIgnoreCase("up")) {serverStatusImageView.setImageResource(R.drawable.up);}
           else {serverStatusImageView.setImageResource(R.drawable.down);}
        serverLatencyView.setText(itemsArrayList.get(position).getLatency());
        if (itemsArrayList.get(position).isNotify()) {notifyImageView.setVisibility(ImageView.VISIBLE);}
            else {notifyImageView.setVisibility(ImageView.INVISIBLE);}
        // 5. return rowView
        return rowView;
    }
}