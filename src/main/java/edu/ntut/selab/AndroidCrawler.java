package edu.ntut.selab;

import edu.ntut.selab.criteria.Timeout;
import edu.ntut.selab.data.GUIState;
import edu.ntut.selab.entity.Device;
import edu.ntut.selab.event.*;
import edu.ntut.selab.exception.*;
import edu.ntut.selab.iterator.Iterator;
import edu.ntut.selab.log.LoggerStore;
import edu.ntut.selab.util.Config;
import edu.ntut.selab.util.Printer;
import edu.ntut.selab.util.Utility;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AndroidCrawler implements Serializable {
    private GUIState currentState = null;
    private GUIState crashState = null;
    private GUIState desktopState = null;
    private GUIState rootState = null;
    private GUIState unrecognizableState = null;
    private GUIState progressBarTimeoutState = null;
    private Device device = null;
    private transient StateGraph stateGraph = null;
    private int restartCount = 0;
    private int guiIndex = 0;
    private long startTime = 0;
    private boolean firstTimeRestart = true;
    private Printer printer;
    private LoggerStore loggerStore;
    private int eventOrder = 1;
    private int nafStateCount = 0, nafCount = 0;

    private enum StateType {UNRECOGNIZABLE_STATE, PROGRESS_BAR_TIMEOUT_EXCEPTION}

    ;

    public AndroidCrawler(Device device, StateGraph stateGraph) throws InterruptedException, DocumentException, ClickTypeErrorException, ExecuteCommandErrorException, IOException, NullPackageNameException, MultipleListOrGridException {
        this.device = device;
        this.loggerStore = new LoggerStore();
        this.stateGraph = stateGraph;
        this.stateGraph.setLoggerStore(this.loggerStore);
        this.restartCount = 0;
        this.guiIndex = 0;
        this.printer = new Printer();
    }

    public void restartApp() throws InterruptedException, ExecuteCommandErrorException, IOException, ClickTypeErrorException, DocumentException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException {
        final String PACKAGE_NAME = Config.PACKAGE_NAME;
        final String ACTIVITY_NAME = Config.LAUNCHABLE_ACTIVITY;
        if (!this.firstTimeRestart && Config.APP_INSTRUMENTED) {
            this.device.sendStopTestBroadcast();
            this.device.pullCoverageReport();
        }
        this.device.stopApp(PACKAGE_NAME);
        this.device.clearAppData(PACKAGE_NAME);
        new BackKeyEvent().executeOn(this.device);
        this.device.pressHome();
        this.device.startApp(PACKAGE_NAME, ACTIVITY_NAME);
        this.updateStateGraph(this.dumpCurrentState());
        if (!firstTimeRestart) {
            this.restartCount++;
            this.printer.restart(restartCount);
        }
        this.firstTimeRestart = false;
        final String SERIAL_PATH = this.getReportPath() + "/serialData";
        new File(SERIAL_PATH).mkdirs();
        this.serialize(new File(SERIAL_PATH + "/serializeStateGraphData.ser"), this.stateGraph);
        this.serialize(new File(SERIAL_PATH + "/serializeAndroidCrawlerData.ser"), this);
    }

    public void executeEvent(AndroidEvent event) throws ExecuteCommandErrorException, InterruptedException, IOException, EventFromStateErrorException {
        if (event.getFromState() != null && event.getFromState() != this.getCurrentState())
            throw new EventFromStateErrorException();
        event.setFromState(this.getCurrentState());
        this.stateGraph.increaseTotalExecutedEventCount();
        this.printer.executeEvent(event, this.stateGraph.getTotalExecutedEventCount());
        event.executeOn(this.device);
        this.device.turnOffSoftKeyboard();
    }

    public void executeEvents(List<AndroidEvent> events) throws ExecuteCommandErrorException, EventFromStateErrorException, InterruptedException, IOException, ClickTypeErrorException, DocumentException, CannotReachTargetStateException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException {
        for (AndroidEvent event : events) {
            // TODO : check currentState exist any input data, if exist, input
            this.executeEvent(event);
            GUIState newState = this.dumpCurrentState();
            try {
                this.updateStateGraph(newState);
            } catch (EquivalentStateException e) {
                event.setToStateId(this.guiIndex - 1);  // mark real to state id to realize whether this event is to original state
                try {
                    this.updateAndCheckNonDeterministicEvent(newState, event); // check if event is nondeterministic event
                } catch (CannotReachTargetStateException e1) {
                    this.restartApp();
                    throw e1;
                }
                this.restartApp();
                break;
            }
            event.setToStateId(this.currentState.getId());
            this.updateAndCheckNonDeterministicEvent(this.currentState, event); // check if event is nondeterministic event
        }
    }

    public void startExplore() throws InterruptedException, ExecuteCommandErrorException, IOException, ClickTypeErrorException, DocumentException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException {
        this.startTime = System.currentTimeMillis();
        this.printer.start(this.startTime);
        new File(System.getProperty("user.dir") + "/" + this.getStatePath()).mkdirs();
        this.device.resetDevice();    // packageName will null after force-stop app, need pressHome to refresh device
        updateStateGraph(this.dumpCurrentState());
        this.desktopState = this.currentState;
        this.desktopState.clearEvents();
        this.restartApp();
        this.rootState = this.currentState;
    }

    public void exploreAllStates(Iterator iterator) throws InterruptedException, ExecuteCommandErrorException, CrawlerControllerInitialErrorException, DocumentException, IOException, ClickTypeErrorException, EventFromStateErrorException, CannotReachTargetStateException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException {
//        this.startExplore();
        for (iterator.first(); !iterator.isDone(); iterator.next()) {
            // TODO : state size out, event size out (implement TerminateCondition class if there are a lot of terminate condition)
            long executeTime = System.currentTimeMillis() - this.startTime;
            Timeout timeout = new Timeout();
            if (timeout.check(executeTime)) {
                this.restartApp();  // restart to serialization and pull coverage report if instrument
                break;
            }

            List<AndroidEvent> eventSequence = iterator.getEventSequence();
            try {
                this.executeEvents(eventSequence);
            } catch (CannotReachTargetStateException e) {
                e.getNondeterministicEvent().increaseAttemptCount();
            }
        }
        this.printer.end();
    }

    public GUIState dumpCurrentState() throws InterruptedException, ExecuteCommandErrorException, IOException, ClickTypeErrorException, DocumentException, NullPackageNameException, ProgressBarTimeoutException {
        final String FILE_PATH = this.getStatePath() + "/" + this.guiIndex + ".xml";

        try {
            this.device.dumpXML(FILE_PATH);
        } catch (UnrecognizableStateException e) {
            return this.createState(StateType.UNRECOGNIZABLE_STATE, FILE_PATH);
        } catch (ProgressBarTimeoutException e) {
            return this.createState(StateType.PROGRESS_BAR_TIMEOUT_EXCEPTION, FILE_PATH);
        }
        return this.createState(null, FILE_PATH);
    }

    private GUIState createState(StateType stateType, final String FILE_PATH) throws InterruptedException, ExecuteCommandErrorException, IOException, DocumentException, ClickTypeErrorException, NullPackageNameException {
        final String PNG_PATH = this.getStatePath() + "/" + this.guiIndex + ".png";
        this.device.getScreenShot(PNG_PATH);
        if (stateType == StateType.UNRECOGNIZABLE_STATE || stateType == StateType.PROGRESS_BAR_TIMEOUT_EXCEPTION) {
            GUIState state = stateType == StateType.UNRECOGNIZABLE_STATE ? this.unrecognizableState : this.progressBarTimeoutState;
            if (state == null) {
                state = new GUIState(DocumentHelper.parseText("<test></test>"), new ArrayList<AndroidEvent>());
                if (stateType == StateType.UNRECOGNIZABLE_STATE)
                    state.setUnrecognizableState(true);
                else if (stateType == StateType.PROGRESS_BAR_TIMEOUT_EXCEPTION)
                    state.setProgressBarTimeoutState(true);
                state.setId(this.guiIndex);
            } else {
                state.setIsEquivalentState(true);
                state.increaseEquivalentStateCount();
            }
            state.addImage(this.guiIndex + ".png");
            if (stateType == StateType.UNRECOGNIZABLE_STATE)
                this.printer.unrecognizableState();
            else if (stateType == StateType.PROGRESS_BAR_TIMEOUT_EXCEPTION)
                this.printer.progressBarTimeoutState();
            return state;
        } else {
            GUIState state = null;
            final String ACTIVITY_NAME = this.device.getActivityName();
            EventOrder eventOrder = new NormalEventOrder();
            state = GUIState.convertDeviceXMLToGUIState(FILE_PATH, eventOrder, ACTIVITY_NAME);
            state.setId(this.guiIndex);
            state.addImage(this.guiIndex + ".png");
            return state;
        }
    }

    public void updateStateGraph(GUIState newState) throws InterruptedException, ExecuteCommandErrorException, IOException, ClickTypeErrorException, DocumentException, NullPackageNameException, MultipleListOrGridException, EquivalentStateException, ProgressBarTimeoutException {
        // update state
        // TODO : maybe crossApp state (loss)
        this.updateNafCount(newState);
        if (this.desktopState != null && this.isDesktopState(newState)) {
            this.currentState = this.desktopState;
        } else if (newState.isCrashState()) {                   // crash state
            if (this.crashState == null) { // first crash state
                this.crashState = newState;
                this.stateGraph.addState(this.crashState);
            }
            this.currentState = this.crashState;
            this.increaseGUIIndex();
        } else if (!this.stateGraph.isInStateList(newState)) {  // new state
            this.stateGraph.addState(newState);
            this.currentState = newState;
            this.increaseGUIIndex();
        } else {                                                // visited state
            this.currentState = this.stateGraph.getState(newState);
            if (this.currentState.isEquivalentState()) {
                if (newState.hasFewerElementThan(this.currentState)) {
                    if (!this.currentState.getImagelist().contains(newState.getImagelist().get(0)))
                        this.currentState.changeImage(newState.getImagelist().get(0));
                }
            }
            this.increaseGUIIndex();
        }
        if (newState.isEquivalentState() && !newState.isUnrecognizableState())
            throw new EquivalentStateException();
    }

    private void updateNafCount(GUIState newState) {
        int count = newState.getNafCount();
        this.nafCount += count;
        if (count > 0)
            this.nafStateCount++;
    }

    private void updateAndCheckNonDeterministicEvent(GUIState newState, AndroidEvent event) throws CannotReachTargetStateException {
        if (!event.isVisited()) {
            event.setToState(this.currentState);
            event.setVisited(true);
            this.stateGraph.updateStatesCrossAppDepth(this.currentState);
        } else if (!newState.isExactlyEquivalentTo(event.getToState())) {
            AndroidEvent nonDeterministicEvent = this.getNondeterministicEventIfExist(this.currentState, event);
            if (nonDeterministicEvent == null) {
                // clone non deterministic event
                event.setNondeterministic(true);
                AndroidEvent duplicatedEvent = event.clone();
                duplicatedEvent.setVisited(true);
                duplicatedEvent.setToState(this.currentState);
                this.stateGraph.updateStatesCrossAppDepth(this.currentState);
                duplicatedEvent.setNondeterministic(true);
                duplicatedEvent.setFromState(event.getFromState());
                duplicatedEvent.setOrder(String.valueOf(this.eventOrder));
                event.getFromState().addEvent(duplicatedEvent);
            } else
                nonDeterministicEvent.setOrder(String.valueOf(this.eventOrder));
            this.eventOrder++;
            if (nonDeterministicEvent != null)
                throw new CannotReachTargetStateException(nonDeterministicEvent);
            else
                throw new CannotReachTargetStateException(event);
        }
        event.setOrder(String.valueOf(this.eventOrder));
        this.eventOrder++;
    }

    private AndroidEvent getNondeterministicEventIfExist(GUIState toState, AndroidEvent originalEvent) {
        List<AndroidEvent> events = originalEvent.getFromState().getEvents();
        for (AndroidEvent event : events) {
            if (event.isNonDeterministic() && event.getToState() == toState && event.getToStateId() == originalEvent.getToStateId())
                return event;
        }
        return null;
    }

    public GUIState getCurrentState() {
        return this.currentState;
    }

    public void setRootState(GUIState state){this.rootState = state;}

    public GUIState getRootState() {
        return this.rootState;
    }

    public StateGraph getStateGraph() {
        return this.stateGraph;
    }

    public boolean isDesktopState(GUIState state) throws NullPackageNameException {
        if (this.desktopState == null)
            throw new NullPointerException();
        boolean packageNameEqual = state.getPackageName().equals(this.desktopState.getPackageName());
        boolean activityNameEqual = state.getActivityName().equals(this.desktopState.getActivityName());
        return packageNameEqual && activityNameEqual;
    }

    public void increaseGUIIndex() {
        this.guiIndex += 1;
    }

    public String getStartTime() {
        return String.valueOf(this.startTime);
    }

    public int getRestartCount() {
        return restartCount;
    }

    private void serialize(File file, Object writeObject) throws IOException {
        FileOutputStream outputFile = new FileOutputStream(file);
        ObjectOutputStream outputOBJ = new ObjectOutputStream(outputFile);
        outputOBJ.writeObject(writeObject);
        outputOBJ.close();
        outputFile.close();
    }

    private final String getStatePath() {
        return this.getReportPath() + "/States";
    }

    // for serialize
    public final String getReportPath() {
        final String REPORT_PATH = Utility.getReportPath();
        return REPORT_PATH;
    }

    // for serialize
    public void setStateGraph(StateGraph stateGraph) {
        this.stateGraph = stateGraph;
    }

    // return the count of states which contains NAF
    public int getNafStateCount() {
        return this.nafStateCount;
    }

    // return the count of NAF
    public int getNafCount() {
        return this.nafCount;
    }
}
