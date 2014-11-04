package com.quadbac.archeageserverstatus;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.quadbac.archeageserverstatus.model.ServerListAdapter;
import com.quadbac.archeageserverstatus.model.ServerStatus;
import com.quadbac.archeageserverstatus.model.StatusReader;

import java.util.ArrayList;

/**
 * Fragment to display the server status list
 */
public class ServerStatusFragment extends Fragment implements AdapterView.OnItemClickListener, OnStatusReadListener {

    private TextView titleNameView;
    private ListView serverListView;
    private ServerListAdapter serverListAdapter;
    private ArrayList<ServerStatus> serverList = new ArrayList<ServerStatus>();
    private ArrayList<String> notifyList;
    private int region;

    public static ServerStatusFragment newInstance(ArrayList<String> notifyList, int region) {
        ServerStatusFragment fragment = new ServerStatusFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("notifyList", notifyList);
        bundle.putInt("region", region);
        bundle.putParcelableArrayList("serverList", new ArrayList<ServerStatus>());
        fragment.setArguments(bundle);
        return fragment;
    }

    public ServerStatusFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_server_status, container, false);
        Bundle bundle = getArguments();
        notifyList = bundle.getStringArrayList("notifyList");
        region = bundle.getInt("region");
        serverList = bundle.getParcelableArrayList("serverList");
        serverListView = (ListView) rootView.findViewById(R.id.serverListView);
        titleNameView = (TextView) rootView.findViewById(R.id.titleNameView);
        if (region == ServerStatus.SERVICES) titleNameView.setText("Service");
        serverListAdapter = new ServerListAdapter(getActivity(), serverList, region);
        serverListView.setAdapter(serverListAdapter);
        serverListAdapter.getFilter().filter(Integer.toString(region));
        serverListView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ServerStatus server = (ServerStatus)adapterView.getAdapter().getItem(position);
        if (server.isNotify()) {
            server.setNotify(false);
            notifyList.remove(server.getName());
            serverListAdapter.notifyDataSetChanged();
        } else {
            server.setNotify(true);
            notifyList.add(server.getName());
            serverListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStatusRead(ArrayList<ServerStatus> serverList) {
        this.serverList = serverList;
        serverListAdapter.clear();
        serverListAdapter.addAll(serverList);
        serverListAdapter.getFilter().filter(Integer.toString(region));
    }

    @Override
    public void onPause() {
        super.onPause();
        Bundle bundle = getArguments();
        bundle.putStringArrayList("notifyList", notifyList);
        bundle.putInt("region", region);
        bundle.putParcelableArrayList("serverList", serverList);
    }
}
