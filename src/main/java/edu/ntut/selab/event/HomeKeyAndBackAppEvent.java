package edu.ntut.selab.event;

import java.util.ArrayList;
import java.util.List;

public class HomeKeyAndBackAppEvent extends AndroidEvent{
    private String eventName = null;
    private String[] command = null;
    private List<String[]> commands = new ArrayList<>();
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
        return androidEventFactory.createAndroidEvent("Rotation", this.getEventData());
    }

    public void homeKey(){
        this.eventName = "Home Key Event";
        this.command = new String[]{"shell" , "input" , "keyevent" , "KEYCODE_HOME"};
        this.reportLabel = "press(\\\"HomeKey\\\")";
    }

    public List<String[]> backApplication(){
        this.eventName = "Back Application Events";
        this.command = new String[]{"shell" , "input" , "keyevent" , "KEYCODE_APP_SWITCH"};
        this.commands.add(this.command);
        this.command = new String[]{"shell" , "input" , "keyevent" , "19"};
        this.commands.add(this.command);
        this.command = new String[]{"shell" , "input" , "keyevent" , "23"};
        this.commands.add(this.command);
        this.reportLabel = "press(\\\"BackApplicationKey\\\")";
        return this.commands;
    }
}
