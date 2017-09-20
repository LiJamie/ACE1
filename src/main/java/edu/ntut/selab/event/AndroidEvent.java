package edu.ntut.selab.event;

import edu.ntut.selab.ExecuteCommandErrorException;
import edu.ntut.selab.TimeHelper;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.util.Config;

import java.io.IOException;
import java.io.Serializable;

public abstract class AndroidEvent implements Serializable {
    private GUIState fromState = null;
    private GUIState toState = null;
    private boolean isVisited;
    private AndroidEvent tempPreviousEvent = null;
    private boolean isNonDeterministicEvent = false;
    private int attemptCount = 0;
    private String tag = "";
    private String text;
    protected int count; // total execute times
    protected String order = ""; // order of execute
    protected boolean orderFlag = false;
    protected int toStateId;


    public abstract String getName();

    //public abstract AndroidEvent clone();
    public abstract String[] getCommand();

    public abstract String getReportLabel();

    public abstract EventData getEventData();

    public abstract AndroidEvent clone();

    protected void execute(Device device) throws InterruptedException, ExecuteCommandErrorException, IOException {
        device.executeADBCommand(this.getCommand());
    }

    public void executeOn(Device device) throws ExecuteCommandErrorException, InterruptedException, IOException {
        this.execute(device);
        TimeHelper.sleep((long) (Config.EVENT_SLEEP_TIMESECOND * 1000));
    }

    public boolean isVisited() {
        return this.isVisited;
    }

    public void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }

    public GUIState getToState() {
        return this.toState;
    }

    public void setToState(GUIState toState) {
        this.toState = toState;
    }

    public GUIState getFromState() {
        return this.fromState;
    }

    public void setFromState(GUIState fromState) {
        this.fromState = fromState;
    }

    public void setNondeterministic(boolean isNondeterministic) {
        this.isNonDeterministicEvent = isNondeterministic;
    }

    public boolean isNonDeterministic() {
        return this.isNonDeterministicEvent;
    }

    public int getAttemptCount() {
        return this.attemptCount;
    }

    public void increaseAttemptCount() {
        this.attemptCount += 1;
    }

    public void setTempPreviousEvent(AndroidEvent preEvent) {
        this.tempPreviousEvent = preEvent;
    }

    public AndroidEvent getTempPreviousEvent() {
        return this.tempPreviousEvent;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }

    public void setOrder(String order) {
        if (this.order.isEmpty())
            this.order = order;
        else
            this.order += ", " + order;
    }

    public String getOrder() {
        return this.order;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return this.tag;
    }

    public int getToStateId() {
        return toStateId;
    }

    public void setToStateId(int toStateId) {
        this.toStateId = toStateId;
    }

    public boolean isToOriginalState() {
        return !this.getToState().isEquivalentState() || this.toStateId == this.toState.getId();
    }

    public boolean isOverAttemptCountThreshold() {
        return attemptCount >= Config.ATTEMPT_COUNT_THRESHOLD;
    }
}
