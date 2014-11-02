package com.quadbac.archeageserverstatus;

import com.quadbac.archeageserverstatus.model.ServerStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve on 02/11/2014.
 */
public interface OnStatusReadListener {
    public void onStatusRead(ArrayList<ServerStatus> serverList);
}
