package edu.ntut.selab.event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.data.Point;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.util.Config;

public class LongClickEvent extends AndroidEvent {
    private String eventName = null;
    private String reportLabel = null, tempLabel = null;
    private Point centerPoint = null;
    private String[] command = null;
    private final String DELAY_TIME = String.valueOf(1500);

    public LongClickEvent(EventData eventData) {
        this.centerPoint = eventData.getCenterPoint();
        this.tempLabel = eventData.getTempLabel();
        createLongClickEvent();
    }

    public void createLongClickEvent() {
        this.eventName = "LongClick Event";
        this.combinePointAndlabel();
        this.reportLabel = "longClick(\\\"" + this.tempLabel + "\\\")";
        this.command = new String[]{"shell", "input", "swipe", Integer.toString(this.centerPoint.x()), Integer.toString(this.centerPoint.y()), Integer.toString(this.centerPoint.x()), Integer.toString(this.centerPoint.y()), DELAY_TIME};
    }

    @Override
    public String[] getCommand() {
        return this.command;
    }

    @Override
    public String getReportLabel() {
        return this.reportLabel;
    }

    protected void combinePointAndlabel() {
        tempLabel = tempLabel + "[" + centerPoint.x() + "," + centerPoint.y() + "]";
    }

    protected File getLongPressScript() {
        File scriptFile = new File("gui_pages/monkeyrunner.script");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(scriptFile, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.println("from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice");
        writer.println("device = MonkeyRunner.waitForConnection()");
        writer.println("device.drag((" + centerPoint.x() + "," + centerPoint.y() +
                "),(" + centerPoint.x() + "," + centerPoint.y() + "),2,1)");
        writer.close();
        return scriptFile;
    }

    @Override
    public EventData getEventData() {
        EventData eventData = new EventData();
        eventData.setCenterPoint(centerPoint);
        eventData.setTempLabel(tempLabel);
        return eventData;
    }

    @Override
    public AndroidEvent clone() {
        AndroidEventFactory androidEventFactory = new AndroidEventFactory();
        return androidEventFactory.createAndroidEvent("LongClick", this.getEventData());
    }

    @Override
    public String getName() {
        return this.eventName;
    }

}
