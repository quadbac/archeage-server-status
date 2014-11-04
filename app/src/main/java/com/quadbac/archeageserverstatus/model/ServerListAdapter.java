package com.quadbac.archeageserverstatus.model;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.quadbac.archeageserverstatus.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve on 02/11/2014.
 */
public class ServerListAdapter extends ArrayAdapter<ServerStatus> implements Filterable{

    private final Context context;
    private ArrayList<ServerStatus> serverList;
    private ServerFilter serverFilter;
    private int region;

    public ServerListAdapter(Context context, ArrayList<ServerStatus> serverList, int region) {

        super(context, R.layout.server_list_item, serverList);

        this.context = context;
        this.serverList = serverList;
        this.region = region;
    }

    @Override
    public int getCount() {
        return (serverList==null)?0:serverList.size();
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
        ImageView notifyImageView = (ImageView) rowView.findViewById(R.id.notifyImageView);
        serverNameView.setWidth(((rowView.getWidth() * 80) / 100));

        // 4. Set the view contents from the ArrayList
        serverNameView.setText(serverList.get(position).getName());
        if (serverList.get(position).getStatus().equalsIgnoreCase("up")) {
            serverStatusImageView.setImageResource(R.drawable.up);
        } else {
            serverStatusImageView.setImageResource(R.drawable.down);
        }
        if (serverList.get(position).isNotify()) {
            notifyImageView.setVisibility(ImageView.VISIBLE);
        } else {
            notifyImageView.setVisibility(ImageView.INVISIBLE);
        }
        // 5. return rowView
        return rowView;
    }

    @Override
    public ServerStatus getItem(int position) {
        return serverList.get(position);
    }

    @Override
    public Filter getFilter() {
        if (serverFilter == null) serverFilter = new ServerFilter();
        return serverFilter;
    }

    private class ServerFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = serverList;
                results.count = serverList.size();
            } else {
                // We perform filtering operation
                List<ServerStatus> filteredServerList = new ArrayList<ServerStatus>();

                for (ServerStatus server : serverList) {
                    if (Integer.toString(server.getRegion()).equals(constraint))
                        filteredServerList.add(server);
                }

                results.values = filteredServerList;
                results.count = filteredServerList.size();

            }
            return results;
        }

            @Override
            protected void publishResults (CharSequence constraint, FilterResults results){

                // Now we have to inform the adapter about the new list filtered
                if (results.count == 0)
                    notifyDataSetInvalidated();
                else {
                    serverList = (ArrayList<ServerStatus>) results.values;
                    notifyDataSetChanged();
                }
            }
    }
}
