package com.quadbac.archeageserverstatus;
import android.app.Fragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quadbac.archeageserverstatus.model.ServerListAdapter;
import com.quadbac.archeageserverstatus.model.ServerStatus;
import com.quadbac.archeageserverstatus.model.StatusReader;

import java.util.ArrayList;

/**
 * Fragment to display the server status list
 */
public class ServerStatusFragment extends Fragment implements AdapterView.OnItemClickListener, OnStatusReadListener {

    private RelativeLayout noItemsLayout;
    private Button retryButton;
    private TextView titleNameView;
    private ListView serverListView;
    private ServerListAdapter serverListAdapter;
    private ArrayList<ServerStatus> serverList;
    private ArrayList<String> notifyList;
    private int region;

    public static ServerStatusFragment newInstance(ArrayList<ServerStatus> serverList, ArrayList<String> notifyList, int region) {
        ServerStatusFragment fragment = new ServerStatusFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("notifyList", notifyList);
        bundle.putInt("region", region);
        bundle.putParcelableArrayList("serverList", serverList);
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
        noItemsLayout = (RelativeLayout) rootView.findViewById(R.id.noItemsLayout);
        retryButton = (Button) rootView.findViewById(R.id.retryButton);
        retryButton.getBackground().setColorFilter(0xFF22DD22, PorterDuff.Mode.ADD);
        titleNameView = (TextView) rootView.findViewById(R.id.titleNameView);
        if (region == ServerStatus.SERVICES) titleNameView.setText("Service");
        if (serverList.size()==0) {noItemsLayout.setVisibility(View.VISIBLE);} else {noItemsLayout.setVisibility(View.GONE);}
        serverListAdapter = new ServerListAdapter(getActivity(), serverList, region);
        serverListAdapter.getFilter().filter(Integer.toString(region));
        serverListView.setAdapter(serverListAdapter);
        serverListView.setClickable(true);
        serverListView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ServerStatus server = (ServerStatus)adapterView.getAdapter().getItem(position);
        if (server.isNotify()) {
            server.setNotify(false);
            notifyList.remove(server.getName());
        } else {
            server.setNotify(true);
            notifyList.add(server.getName());
        }
        ((MainActivity)getActivity()).saveNotifyList(notifyList);
        serverListAdapter.notifyDataSetChanged();

    }

    @Override
    public void onStatusRead(ArrayList<ServerStatus> serverList) {
        this.serverList = serverList;
        if (serverList.size()==0) {noItemsLayout.setVisibility(View.VISIBLE);} else {noItemsLayout.setVisibility(View.GONE);}
        serverListAdapter = new ServerListAdapter(getActivity(), this.serverList, region);
        serverListAdapter.getFilter().filter(Integer.toString(region));
        serverListView.setAdapter(serverListAdapter);
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
