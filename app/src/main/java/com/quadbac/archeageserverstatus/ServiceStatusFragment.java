package com.quadbac.archeageserverstatus;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quadbac.archeageserverstatus.model.ServerListAdapter;
import com.quadbac.archeageserverstatus.model.ServerStatus;
import com.quadbac.archeageserverstatus.model.ServiceListAdapter;
import com.quadbac.archeageserverstatus.model.StatusReader;

import java.util.ArrayList;

/**
 * Fragment to display the service status list
 */
public class ServiceStatusFragment extends Fragment implements OnStatusReadListener, AdapterView.OnItemClickListener{

    private StatusReader statusReader;

    private ListView serviceListView;
    private ServiceListAdapter serviceListAdapter;
    private ArrayList<ServerStatus> serverList;
    private ArrayList<String> notifyList;

    public static ServiceStatusFragment newInstance(ArrayList<String> notifyList) {
        ServiceStatusFragment fragment = new ServiceStatusFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("notifyList", notifyList);
        fragment.setArguments(bundle);
        return fragment;
    }

    public ServiceStatusFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_service_status, container, false);
        Bundle bundle = getArguments();
        notifyList = bundle.getStringArrayList("notifyList");
        serviceListView = (ListView) rootView.findViewById(R.id.serviceListView);
        serviceListView.setOnItemClickListener(this);
        statusReader = new StatusReader(this, StatusReader.SERVICES, notifyList);
        statusReader.readStatus();
        return rootView;
    }

    @Override
    public void onStatusRead(ArrayList<ServerStatus> newServerList) {
        // Check for possible notifications needed
        for (ServerStatus newStatus : newServerList) {
            if (notifyList.contains(newStatus.getName())) {
                for (ServerStatus oldStatus : serverList) {
                    if ((oldStatus.getName().equals(newStatus.getName())) && !(oldStatus.getStatus().equals(newStatus.getStatus()))) {
                        // Status has changed and server is on the notify list, fire off a notification
                        //
                        //  DO CLEVER STUFF HERE
                        //
                    }
                }
            }
        }
        this.serverList = newServerList;
        serviceListAdapter = new ServiceListAdapter(getActivity(), serverList);
        serviceListView.setAdapter(serviceListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ServerStatus server = serverList.get(position);
        if (server.isNotify()) {
            server.setNotify(false);
            notifyList.remove(server.getName());
            serviceListAdapter.notifyDataSetChanged();
            Log.v("notifyList", notifyList.toString());
        } else {
            server.setNotify(true);
            notifyList.add(server.getName());
            serviceListAdapter.notifyDataSetChanged();
            Log.v("notifyList", notifyList.toString());
        }
    }
}
