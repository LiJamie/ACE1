package edu.ntut.selab.event;


import java.util.ArrayList;
import java.util.List;

public class WifiEvent extends AndroidEvent{
    private String eventName = null;
    private String[] command = null;
    private String reportLabel = null;

    public void setReportLabel(String reportLabel){
        this.reportLabel = reportLabel;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getReportLabel() {
        return this.reportLabel;
    }

    @Override
    public String[] getCommand() {
        return this.command;
    }

    @Override
    public EventData getEventData() {
        return new EventData();
    }

    @Override
    public AndroidEvent clone() {
        AndroidEventFactory androidEventFactory = new AndroidEventFactory();
        return androidEventFactory.createAndroidEvent("Wifi", this.getEventData());
    }

    public void EnableWifi(){
        this.eventName = "Wifi Enable";
        this.command = new String[]{"shell","svc" , "wifi" , "enable"};
        this.reportLabel = "Wifi(\\\"Enable\\\")";
    }

    public void DisableWifi(){
        this.eventName = "Wifi Disable";
        this.command = new String[]{"shell","svc" , "wifi" , "disable"};
        this.reportLabel = "Wifi(\\\"Disable\\\")";
    }
}
