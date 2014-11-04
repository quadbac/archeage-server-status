package com.quadbac.archeageserverstatus.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Steve on 02/11/2014.
 */
public class ServerStatus implements Parcelable{

    private String name;
    private String status;
    private String latency;
    private int region;
    public final static int EU_SERVERS = 1;
    public final static int NA_SERVERS = 2;
    public final static int SERVICES = 3;
    private boolean notify;

   public ServerStatus(String name, String status, String latency, boolean notify, int region) {
        this.name = name;
        this.status = status;
        this.latency = latency;
        this.notify = notify;
       this.region = region;
    }

    public ServerStatus(Parcel in) {
        this.name = in.readString();
        this.status = in.readString();
        this.latency = in.readString();
        this.notify = in.readByte() != 0;
        this.region = in.readInt();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLatency() {
        return latency;
    }

    public void setLatency(String latency) {
        this.latency = latency;
    }

    public boolean isNotify() { return notify; }

    public void setNotify(boolean notify) { this.notify = notify; }

    public int getRegion() { return region; }

    public void setRegion(int region) { this.region = region; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(latency);
        dest.writeByte((byte) (notify ? 1 : 0));
        dest.writeInt(region);
    }
    public static final Parcelable.Creator<ServerStatus> CREATOR
            = new Parcelable.Creator<ServerStatus>() {

        public ServerStatus createFromParcel(Parcel in) {
            return new ServerStatus(in);
        }

        public ServerStatus[] newArray(int size) {
            return new ServerStatus[size];
        }
    };
}
