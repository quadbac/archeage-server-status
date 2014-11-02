package com.quadbac.archeageserverstatus.model;

/**
 * Created by Steve on 02/11/2014.
 */
public class ServerStatus {

    private String name;
    private String status;
    private String latency;
    private boolean notify;

   public ServerStatus(String name, String status, String latency, boolean notify) {
        this.name = name;
        this.status = status;
        this.latency = latency;
        this.notify = notify;

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
}
