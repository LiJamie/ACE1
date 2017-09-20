package edu.ntut.selab.event;


public class RestartEvent extends AndroidEvent{
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String[] getCommand() {
        return new String[0];
    }

    @Override
    public String getReportLabel() {
        return "Restart(\\\"App\\\")";
    }

    @Override
    public EventData getEventData() {
        return null;
    }

    @Override
    public AndroidEvent clone() {
        return null;
    }
}
