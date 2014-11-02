package com.quadbac.archeageserverstatus;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.quadbac.archeageserverstatus.model.ServerListAdapter;
import com.quadbac.archeageserverstatus.model.ServerStatus;
import com.quadbac.archeageserverstatus.model.ServiceListAdapter;
import com.quadbac.archeageserverstatus.model.StatusReader;

import java.util.ArrayList;

/**
 * Fragment to display the service status list
 */
public class ServiceStatusFragment extends Fragment implements OnStatusReadListener{

    StatusReader statusReader;

    ListView serviceListView;
    ServiceListAdapter serviceListAdapter;

    public static ServiceStatusFragment newInstance() {
        ServiceStatusFragment fragment = new ServiceStatusFragment();
        return fragment;
    }

    public ServiceStatusFragment() {
        statusReader = new StatusReader(this, StatusReader.SERVICES);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_service_status, container, false);
        serviceListView = (ListView) rootView.findViewById(R.id.serviceListView);
        statusReader.readStatus();
        return rootView;
    }

    @Override
    public void onStatusRead(ArrayList<ServerStatus> serverList) {
        serviceListAdapter = new ServiceListAdapter(getActivity(), serverList);
        serviceListView.setAdapter(serviceListAdapter);
    }
}
