package org.wcb.entity;

/**
 * Created by wybe on 7/1/16.
 */
public class EmailServer {
    private String hostname;
    private int port;

    public EmailServer() {}

    public EmailServer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
