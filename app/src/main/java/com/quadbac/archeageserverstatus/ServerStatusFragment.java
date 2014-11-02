package com.quadbac.archeageserverstatus;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.quadbac.archeageserverstatus.R;
import com.quadbac.archeageserverstatus.model.ServerListAdapter;
import com.quadbac.archeageserverstatus.model.ServerStatus;
import com.quadbac.archeageserverstatus.model.StatusReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display the server status list
 */
public class ServerStatusFragment extends Fragment implements OnStatusReadListener{

    StatusReader statusReader;

    ListView serverListView;
    ServerListAdapter serverListAdapter;

    public static ServerStatusFragment newInstance() {
        ServerStatusFragment fragment = new ServerStatusFragment();
        return fragment;
    }

    public ServerStatusFragment() {
        statusReader = new StatusReader(this, StatusReader.EU_SERVERS);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_server_status, container, false);
        serverListView = (ListView) rootView.findViewById(R.id.serverListView);
        statusReader.readStatus();
        return rootView;
    }

    @Override
    public void onStatusRead(ArrayList<ServerStatus> serverList) {
        serverListAdapter = new ServerListAdapter(getActivity(), serverList);
        serverListView.setAdapter(serverListAdapter);
    }
}
