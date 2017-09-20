package edu.ntut.selab.event;

public class RotationEvent extends AndroidEvent {
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
        return androidEventFactory.createAndroidEvent("Rotation", this.getEventData());
    }

    public void rotaionLandscape(){
        this.eventName = "Rotation Event Landscape";
        this.command = new String[]{"shell", "content", "insert", "--uri", "content://settings/system",  "--bind", "name:s:user_rotation", "--bind", "value:i:1"};
        this.reportLabel = "Rotation(\\\"Landscape\\\")";
    }

    public void rotationPortrait(){
        this.eventName = "Rotation Event Landscape";
        this.command = new String[]{"shell", "content", "insert", "--uri", "content://settings/system",  "--bind", "name:s:user_rotation", "--bind", "value:i:0"};
        this.reportLabel = "Rotation(\\\"Portrait\\\")";
    }
}
