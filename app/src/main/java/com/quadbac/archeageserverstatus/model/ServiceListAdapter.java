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
public class ServiceListAdapter extends ArrayAdapter<ServerStatus> {

    private final Context context;
    private final ArrayList<ServerStatus> itemsArrayList;

    public ServiceListAdapter(Context context, ArrayList<ServerStatus> itemsArrayList) {

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
        View rowView = inflater.inflate(R.layout.service_list_item, parent, false);

        // 3. Get the views from the rowView
        TextView serviceNameView = (TextView) rowView.findViewById(R.id.serviceNameView);
        ImageView serviceStatusImageView = (ImageView) rowView.findViewById(R.id.serviceStatusImageView);

        // 4. Set the view contents from the ArrayList
        serviceNameView.setText(itemsArrayList.get(position).getName());
        if (itemsArrayList.get(position).getStatus().equalsIgnoreCase("up")) {serviceStatusImageView.setImageResource(R.drawable.up);}
        else {serviceStatusImageView.setImageResource(R.drawable.down);}

        // 5. return rowView
        return rowView;
    }
}